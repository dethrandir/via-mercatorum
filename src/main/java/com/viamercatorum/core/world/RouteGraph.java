package com.viamercatorum.core.world;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.economy.Supply;
import com.viamercatorum.core.economy.TradeGood;
import com.viamercatorum.core.economy.TradeGoodRegistry;
import com.viamercatorum.core.config.ViaMercatorumConfig;
import com.viamercatorum.core.simulation.EconomyEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RouteGraph {
    private final Map<String, VillageNode> villages = new ConcurrentHashMap<>();
    private final Map<String, TradeRoute> routes = new ConcurrentHashMap<>();
    private final Map<String, Caravan> caravans = new ConcurrentHashMap<>();

    public void addVillage(VillageNode village) {
        villages.put(village.getId(), village);
    }

    public void addRoute(TradeRoute route) {
        routes.put(route.getId(), route);
    }

    public void addCaravan(Caravan caravan) {
        caravans.put(caravan.getId(), caravan);
    }

    public void tickAllCaravans() {
        List<String> arrived = new ArrayList<>();
        for (Caravan caravan : caravans.values()) {
            caravan.tick();
            if (caravan.hasArrived()) {
                arrived.add(caravan.getId());
            }
        }
    }

    public void processArrivedCaravans() {
        List<String> toRemove = new ArrayList<>();
        for (Caravan caravan : caravans.values()) {
            if (caravan.hasArrived()) {
                TradeRoute route = routes.get(caravan.getRouteId());
                if (route != null) {
                    VillageNode dest = caravan.getDestinationNode();
                    double defaultMaxCapacity = ViaMercatorum.getInstance().getConfig().productionScale.defaultMaxCapacity;
                    for (var entry : caravan.getCargo().entrySet()) {
                        var supply = dest.getSupply(entry.getKey());
                        if (supply != null) {
                            supply.addStock(entry.getValue());
                        } else {
                            TradeGood good = TradeGoodRegistry.get(entry.getKey());
                            if (good != null) {
                                dest.addSupply(good, entry.getValue(), 0, defaultMaxCapacity);
                            }
                        }
                    }
                    double trafficIncrement = ViaMercatorum.getInstance().getConfig().caravan.trafficIncrement;
                    double upgradeThreshold = ViaMercatorum.getInstance().getConfig().caravan.trafficUpgradeThreshold;
                    route.addTraffic(trafficIncrement);
                    if (route.getTraffic() > upgradeThreshold && route.getQuality().ordinal() < TradeRoute.RouteQuality.values().length - 1) {
                        route.improveQuality();
                    }
                    var bus = ViaMercatorum.getInstance().getEventBus();
                    bus.emit(new EconomyEvent.CaravanArrived(
                        dest.getId(), System.currentTimeMillis(),
                        caravan.getId(), caravan.getOriginNode().getId(), dest.getId()
                    ));
                }
                toRemove.add(caravan.getId());
            }
        }
        toRemove.forEach(id -> {
            ViaMercatorum.getInstance().getCharacterManager().removeCaravanLeaders(id);
            caravans.remove(id);
        });
    }

    public void spawnNewCaravans() {
        if (routes.isEmpty() || villages.isEmpty()) return;

        List<VillageNode> villageList = new ArrayList<>(villages.values());
        VillageNode from = villageList.get(new Random().nextInt(villageList.size()));

        List<TradeRoute> candidates = new ArrayList<>();
        for (TradeRoute r : routes.values()) {
            if (r.getFrom().equals(from) || r.getTo().equals(from)) {
                candidates.add(r);
            }
        }
        if (candidates.isEmpty()) {
            from = villageList.get(new Random().nextInt(villageList.size()));
            for (TradeRoute r : routes.values()) {
                if (r.getFrom().equals(from) || r.getTo().equals(from)) {
                    candidates.add(r);
                }
            }
            if (candidates.isEmpty()) return;
        }

        TradeRoute route = CaravanAI.chooseRoute(from, candidates);
        if (route == null) return;

        VillageNode origin = route.getFrom();
        VillageNode dest = route.getTo();
        Map<String, Double> cargo = CaravanAI.chooseCargo(origin, dest);

        for (var entry : cargo.entrySet()) {
            Supply supply = origin.getSupply(entry.getKey());
            if (supply != null) {
                double consume = Math.min(supply.getStock(), entry.getValue());
                supply.consume(consume);
                entry.setValue(consume);
            }
        }
        cargo.values().removeIf(v -> v <= 0);

        String caravanId = "caravan_" + UUID.randomUUID().toString().substring(0, 8);
        Caravan caravan = new Caravan(caravanId, route.getId(), origin, dest, cargo, route.getTravelTime());
        caravans.put(caravanId, caravan);

        var bus = ViaMercatorum.getInstance().getEventBus();
        bus.emit(new EconomyEvent.CaravanDeparted(
            origin.getId(), System.currentTimeMillis(),
            caravanId, dest.getId()
        ));
    }

    public void removeCaravan(String caravanId) {
        ViaMercatorum.getInstance().getCharacterManager().removeCaravanLeaders(caravanId);
        caravans.remove(caravanId);
    }

    public void spawnNewSeaCaravans() {
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();

        List<VillageNode> coastalVillages = villages.values().stream()
            .filter(VillageNode::isCoastal)
            .toList();

        if (coastalVillages.size() < 2) return;

        List<TradeRoute> seaRoutes = routes.values().stream()
            .filter(TradeRoute::isSeaRoute)
            .toList();

        if (seaRoutes.isEmpty()) return;

        TradeRoute route = seaRoutes.get(new Random().nextInt(seaRoutes.size()));
        VillageNode origin = route.getFrom();
        VillageNode dest = route.getTo();

        Map<String, Double> cargo = CaravanAI.chooseCargo(origin, dest);

        for (var entry : cargo.entrySet()) {
            Supply supply = origin.getSupply(entry.getKey());
            if (supply != null) {
                double consume = Math.min(supply.getStock(), entry.getValue());
                supply.consume(consume);
                entry.setValue(consume);
            }
        }
        cargo.values().removeIf(v -> v <= 0);

        String caravanId = "ship_" + UUID.randomUUID().toString().substring(0, 8);
        double cargoBonus = 1.0 + config.seaTrade.seaCargoCapacityBonus;
        Caravan caravan = new Caravan(caravanId, route.getId(), origin, dest, cargo, route.getTravelTime());
        caravans.put(caravanId, caravan);

        var bus = ViaMercatorum.getInstance().getEventBus();
        bus.emit(new EconomyEvent.CaravanDeparted(
            origin.getId(), System.currentTimeMillis(),
            caravanId, dest.getId()
        ));
    }

    public void createSeaRoutes() {
        List<VillageNode> coastalVillages = villages.values().stream()
            .filter(VillageNode::isCoastal)
            .toList();

        for (int i = 0; i < coastalVillages.size(); i++) {
            for (int j = i + 1; j < coastalVillages.size(); j++) {
                VillageNode a = coastalVillages.get(i);
                VillageNode b = coastalVillages.get(j);

                String routeId = "sea_" + a.getId() + "_" + b.getId();
                if (routes.containsKey(routeId)) continue;
                if (routes.containsKey("sea_" + b.getId() + "_" + a.getId())) continue;

                double distance = Math.sqrt(
                    Math.pow(a.getChunkX() - b.getChunkX(), 2) +
                    Math.pow(a.getChunkZ() - b.getChunkZ(), 2)
                );

                ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
                if (distance > config.seaTrade.minCoastalDistance * 3) continue;

                TradeRoute seaRoute = new TradeRoute(routeId, a, b, TradeRoute.RouteType.SEA);
                routes.put(routeId, seaRoute);

                var bus = ViaMercatorum.getInstance().getEventBus();
                bus.emit(new EconomyEvent.SeaRouteCreated(
                    a.getId(), System.currentTimeMillis(), routeId, a.getId(), b.getId()
                ));
            }
        }
    }

    public Collection<VillageNode> getAllVillages() { return villages.values(); }
    public Collection<TradeRoute> getAllRoutes() { return routes.values(); }
    public Collection<Caravan> getAllCaravans() { return caravans.values(); }
    public VillageNode getVillage(String id) { return villages.get(id); }
    public TradeRoute getRoute(String id) { return routes.get(id); }
    public Caravan getCaravan(String id) { return caravans.get(id); }
}
