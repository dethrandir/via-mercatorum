package com.viamercatorum.core.world;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.config.ViaMercatorumConfig;
import com.viamercatorum.core.economy.TradeGood;
import com.viamercatorum.core.economy.TradeGoodRegistry;
import com.viamercatorum.core.simulation.EconomyEvent;
import com.viamercatorum.data.GoodAmount;
import com.viamercatorum.data.WorkshopDefinition;
import com.viamercatorum.data.WorkshopDefinitionLoader;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WorkshopManager {
    private final Map<String, WorkshopInstance> instances = new ConcurrentHashMap<>();

    public WorkshopInstance createWorkshop(String villageId, String workshopId, String ownerUuid) {
        WorkshopDefinitionLoader loader = ViaMercatorum.getInstance().getWorkshopDefinitionLoader();
        if (loader == null) return null;

        WorkshopDefinition def = loader.getDefinition(workshopId);
        if (def == null) return null;

        WorkshopInstance instance = new WorkshopInstance(workshopId, villageId, ownerUuid);
        instances.put(instance.getInstanceId(), instance);

        VillageNode village = ViaMercatorum.getInstance().getSimulationTick().getRouteGraph().getVillage(villageId);
        if (village != null) {
            village.addWorkshop(instance.getInstanceId());
        }

        var bus = ViaMercatorum.getInstance().getEventBus();
        bus.emit(new EconomyEvent.WorkshopCreated(villageId,
            ViaMercatorum.getInstance().getSimulationTick().getTickCounter(),
            instance.getInstanceId(), workshopId, ownerUuid));

        return instance;
    }

    public WorkshopInstance createWorkshop(String villageId, String workshopId) {
        return createWorkshop(villageId, workshopId, null);
    }

    public void destroyWorkshop(String instanceId) {
        WorkshopInstance instance = instances.remove(instanceId);
        if (instance == null) return;

        VillageNode village = ViaMercatorum.getInstance().getSimulationTick().getRouteGraph().getVillage(instance.getVillageId());
        if (village != null) {
            village.removeWorkshop(instanceId);
        }

        var bus = ViaMercatorum.getInstance().getEventBus();
        bus.emit(new EconomyEvent.WorkshopDestroyed(instance.getVillageId(),
            ViaMercatorum.getInstance().getSimulationTick().getTickCounter(),
            instanceId, instance.getWorkshopId()));
    }

    public boolean upgradeWorkshop(String instanceId) {
        WorkshopInstance instance = instances.get(instanceId);
        if (instance == null) return false;

        WorkshopDefinitionLoader loader = ViaMercatorum.getInstance().getWorkshopDefinitionLoader();
        if (loader == null) return false;

        WorkshopDefinition def = loader.getDefinition(instance.getWorkshopId());
        if (def == null) return false;

        if (instance.getLevel() >= def.maxLevel()) return false;

        instance.setLevel(instance.getLevel() + 1);

        var bus = ViaMercatorum.getInstance().getEventBus();
        bus.emit(new EconomyEvent.WorkshopUpgraded(instance.getVillageId(),
            ViaMercatorum.getInstance().getSimulationTick().getTickCounter(),
            instanceId, instance.getWorkshopId(), instance.getLevel()));

        return true;
    }

    public void tick(RouteGraph routeGraph) {
        WorkshopDefinitionLoader loader = ViaMercatorum.getInstance().getWorkshopDefinitionLoader();
        if (loader == null) return;

        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        if (!config.workshop.enabled) return;

        for (WorkshopInstance instance : instances.values()) {
            if (!instance.isEnabled()) continue;

            WorkshopDefinition def = loader.getDefinition(instance.getWorkshopId());
            if (def == null) continue;

            VillageNode village = routeGraph.getVillage(instance.getVillageId());
            if (village == null) continue;

            instance.incrementTick();
            if (instance.getTickCounter() % def.productionTickInterval() != 0) continue;

            int levelMultiplier = instance.getLevel();
            boolean allAvailable = true;

            for (GoodAmount input : def.inputGoods()) {
                double needed = input.amount() * levelMultiplier;

                if (instance.isAutoBuyMaterials()) {
                    var supply = village.getSupply(input.goodId());
                    if (supply == null || supply.getStock() < needed) {
                        allAvailable = false;
                        break;
                    }
                } else {
                    double available = instance.getStoredInput().getOrDefault(input.goodId(), 0.0);
                    if (available < needed) {
                        allAvailable = false;
                        break;
                    }
                }
            }

            if (!allAvailable) {
                var bus = ViaMercatorum.getInstance().getEventBus();
                bus.emit(new EconomyEvent.WorkshopProductionFailed(instance.getVillageId(),
                    ViaMercatorum.getInstance().getSimulationTick().getTickCounter(),
                    instance.getInstanceId(), instance.getWorkshopId()));
                continue;
            }

            for (GoodAmount input : def.inputGoods()) {
                double needed = input.amount() * levelMultiplier;

                if (instance.isAutoBuyMaterials()) {
                    var supply = village.getSupply(input.goodId());
                    supply.consume(needed);
                } else {
                    instance.getStoredInput().merge(input.goodId(), -needed, Double::sum);
                    double current = instance.getStoredInput().getOrDefault(input.goodId(), 0.0);
                    if (current <= 0.01) {
                        instance.getStoredInput().remove(input.goodId());
                    }
                }
            }

            for (GoodAmount output : def.outputGoods()) {
                double produced = output.amount() * levelMultiplier;

                if (instance.isAutoSellOutput()) {
                    var supply = village.getSupply(output.goodId());
                    if (supply != null) {
                        supply.addStock(produced);
                    } else {
                        TradeGood good = TradeGoodRegistry.get(output.goodId());
                        if (good != null) {
                            village.addSupply(good, produced, 0,
                                config.productionScale.defaultMaxCapacity);
                        }
                    }
                } else {
                    double maxOutput = def.maxAccumulatedOutput() * levelMultiplier;
                    double current = instance.getAccumulatedOutput().getOrDefault(output.goodId(), 0.0);
                    double room = maxOutput - current;
                    double toStore = Math.min(produced, room);
                    if (toStore > 0) {
                        instance.getAccumulatedOutput().merge(output.goodId(), toStore, Double::sum);
                    }
                }
            }

            var bus = ViaMercatorum.getInstance().getEventBus();
            bus.emit(new EconomyEvent.WorkshopProductionComplete(instance.getVillageId(),
                ViaMercatorum.getInstance().getSimulationTick().getTickCounter(),
                instance.getInstanceId(), instance.getWorkshopId(), levelMultiplier));
        }
    }

    public WorkshopInstance getInstance(String instanceId) {
        return instances.get(instanceId);
    }

    public Collection<WorkshopInstance> getAllInstances() {
        return instances.values();
    }

    public List<WorkshopInstance> getWorkshopsForVillage(String villageId) {
        return instances.values().stream()
            .filter(i -> i.getVillageId().equals(villageId))
            .toList();
    }
}