package com.viamercatorum.core.simulation;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.config.ViaMercatorumConfig;
import com.viamercatorum.core.economy.EconomyHelper;
import com.viamercatorum.core.economy.Market;
import com.viamercatorum.core.economy.Supply;
import com.viamercatorum.core.economy.TradeGood;
import com.viamercatorum.data.ConsumptionProfile;
import com.viamercatorum.data.ConsumptionProfileLoader;
import com.viamercatorum.core.world.*;
import com.viamercatorum.world.spawn.RaidEventCache;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimulationTick {
    private final RouteGraph routeGraph;
    private final Market globalMarket;
    private final RaidEventCache raidEventCache;
    private final SeasonManager seasonManager;
    private final ActiveModifierManager modifierManager;
    private final EventEngine eventEngine;
    private final WorkshopManager workshopManager;
    private final VillageGoalEngine goalEngine;
    private final CharacterAI characterAI;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "SEK-Simulation-Thread");
        t.setDaemon(true);
        return t;
    });
    private final Random random = new Random();

    private long tickCounter = 0;
    private boolean running = false;

    public SimulationTick(RouteGraph routeGraph) {
        this.routeGraph = routeGraph;
        this.globalMarket = new Market();
        this.raidEventCache = new RaidEventCache();
        this.seasonManager = new SeasonManager();
        this.modifierManager = new ActiveModifierManager();
        this.eventEngine = new EventEngine(modifierManager);
        this.workshopManager = new WorkshopManager();
        this.goalEngine = new VillageGoalEngine();
        this.characterAI = new CharacterAI();
    }

    public SimulationTick(RouteGraph routeGraph, Market globalMarket) {
        this.routeGraph = routeGraph;
        this.globalMarket = globalMarket;
        this.raidEventCache = new RaidEventCache();
        this.seasonManager = new SeasonManager();
        this.modifierManager = new ActiveModifierManager();
        this.eventEngine = new EventEngine(modifierManager);
        this.workshopManager = new WorkshopManager();
        this.goalEngine = new VillageGoalEngine();
        this.characterAI = new CharacterAI();
    }

    public SimulationTick(RouteGraph routeGraph, Market globalMarket, RaidEventCache raidEventCache) {
        this.routeGraph = routeGraph;
        this.globalMarket = globalMarket;
        this.raidEventCache = raidEventCache;
        this.seasonManager = new SeasonManager();
        this.modifierManager = new ActiveModifierManager();
        this.eventEngine = new EventEngine(modifierManager);
        this.workshopManager = new WorkshopManager();
        this.goalEngine = new VillageGoalEngine();
        this.characterAI = new CharacterAI();
    }

    public SimulationTick(RouteGraph routeGraph, Market globalMarket, RaidEventCache raidEventCache, SeasonManager seasonManager) {
        this.routeGraph = routeGraph;
        this.globalMarket = globalMarket;
        this.raidEventCache = raidEventCache;
        this.seasonManager = seasonManager;
        this.modifierManager = new ActiveModifierManager();
        this.eventEngine = new EventEngine(modifierManager);
        this.workshopManager = new WorkshopManager();
        this.goalEngine = new VillageGoalEngine();
        this.characterAI = new CharacterAI();
    }

    public void start() {
        if (running) return;
        running = true;
        executor.scheduleAtFixedRate(this::tick, 0, 50, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        running = false;
        executor.shutdown();
    }

    private void tick() {
        tickCounter++;
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();

        seasonManager.tick();

        globalMarket.tickAll();

        SeasonManager.Season season = seasonManager.getCurrentSeason();
        String seasonName = season.name();
        ConsumptionProfileLoader consLoader = ViaMercatorum.getInstance().getConsumptionProfileLoader();

        for (VillageNode village : routeGraph.getAllVillages()) {
            double harvestModifier = seasonManager.getHarvestModifier(village.getBiome());

            Map<String, Double> productionModifiers = modifierManager.getProductionModifiers(
                village.getId(), village.getRegionId());

            if (config.productionScale.enabled) {
                double scale = village.calculateProductionScale();
                if (scale != 1.0) {
                    productionModifiers.put("_scale", scale);
                }
            }

            if (village.getMobilization() == MilitaryPower.MobilizationState.MOBILIZED) {
                productionModifiers.put("_mobilize", config.mobilization.mobilizeProductionPenalty);
            } else if (village.getMobilization() == MilitaryPower.MobilizationState.STANDBY) {
                productionModifiers.put("_standby", config.mobilization.standbyProductionPenalty);
            }

            village.tickProduction(harvestModifier, productionModifiers);

            if (config.consumption.enabled) {
                ConsumptionProfile consProfile = null;
                if (consLoader != null) {
                    consProfile = consLoader.getProfile(village.getType().name());
                }
                village.tickConsumption(consProfile, seasonName);
            }

            checkDisaster(village, config);
            checkFamine(village, config);
        }

        if (config.population.enabled && tickCounter % config.population.growthIntervalTicks == 0) {
            tickPopulationGrowth(config);
        }

        if (tickCounter % config.consumption.demandUpdateInterval == 0) {
            globalMarket.updateDemands(routeGraph);
        }

        routeGraph.tickAllCaravans();

        if (tickCounter % 20 == 0) {
            routeGraph.processArrivedCaravans();
        }

        if (tickCounter % 50 == 0) {
            tickPiracy(config);
        }

        if (tickCounter % 100 == 0) {
            tickAI(config);
        }

        if (tickCounter % 200 == 0) {
            tickCaravanRaids(config);
        }

        if (tickCounter % config.ai.villageRaidInterval == 0) {
            tickVillageRaids(config);
        }

        if (tickCounter % 1200 == 0) {
            routeGraph.spawnNewCaravans();
        }

        if (tickCounter % config.seaTrade.caravanSpawnInterval == 0) {
            routeGraph.spawnNewSeaCaravans();
        }

        if (tickCounter % 6000 == 0) {
            tickResettlement();
        }

        if (tickCounter % config.roadMaintenance.degradationTickInterval == 0) {
            tickRoadDegradation(config);
        }

        if (tickCounter % 100 == 0) {
           ViaMercatorum.getInstance().getBankManager().tickLoans();
        }

        if (config.event.enabled && tickCounter % config.event.eventTickInterval == 0) {
            eventEngine.tick(routeGraph, seasonManager, tickCounter);
        }

        if (config.workshop.enabled) {
            workshopManager.tick(routeGraph);
        }

        if (config.goal.enabled && tickCounter % config.goal.goalTickInterval == 0) {
            goalEngine.tick(routeGraph, tickCounter);
        }

        if (tickCounter % 100 == 0) {
            characterAI.tickCharacters(routeGraph);
        }

        modifierManager.tick(tickCounter);
    }

    private void tickPiracy(ViaMercatorumConfig config) {
        for (TradeRoute route : routeGraph.getAllRoutes()) {
            route.tickPiracyDecay(config);

            double fluctuation = (random.nextDouble() - 0.5) * config.piracy.randomFluctuation;
            route.addPiracy(fluctuation);

            double wealth = EconomyHelper.totalWealth(route.getTo());
            if (wealth > config.piracy.wealthThreshold) {
                route.addPiracy(config.piracy.wealthPiracyIncrease);
            }
        }
    }

    private void tickAI(ViaMercatorumConfig config) {
        for (VillageNode village : routeGraph.getAllVillages()) {
            VillageAI.optimizeProduction(village, routeGraph);
        }
    }

    private void tickCaravanRaids(ViaMercatorumConfig config) {
        for (Caravan caravan : routeGraph.getAllCaravans()) {
            TradeRoute route = routeGraph.getRoute(caravan.getRouteId());
            if (route == null) continue;

            if (route.getPiracyScore() < config.piracy.raidThreshold) continue;

            double raidChance = route.getPiracyScore() * config.piracy.raidChanceMultiplier;
            if (random.nextDouble() >= raidChance) continue;

            int raiderCount = 3 + (int) (route.getPiracyScore() * 5);
            raiderCount = Math.min(raiderCount, 8);

            int guardCount = getGuardCount(caravan, route);
            boolean raidersWon = resolveCombat(guardCount, raiderCount, random);

            if (raidersWon) {
                Map<String, Double> lootedGoods = new HashMap<>();
                for (var entry : caravan.getCargo().entrySet()) {
                    double looted = entry.getValue() * (0.5 + random.nextDouble() * 0.5);
                    lootedGoods.put(entry.getKey(), looted);
                }

                route.addPiracy(config.piracy.raidPenalty);
                route.setLastRaidTime(tickCounter);

                int cx = (int) Math.floor(caravan.getOriginNode().getChunkX()
                    + (caravan.getDestinationNode().getChunkX() - caravan.getOriginNode().getChunkX()) * caravan.getProgress());
                int cz = (int) Math.floor(caravan.getOriginNode().getChunkZ()
                    + (caravan.getDestinationNode().getChunkZ() - caravan.getOriginNode().getChunkZ()) * caravan.getProgress());

                RaidEventCache.CachedRaid cachedRaid = new RaidEventCache.CachedRaid(
                    cx, cz, true, caravan.getId(), route.getId(),
                    caravan.getDestinationNode().getId(),
                    tickCounter, raiderCount, true, lootedGoods
                );
                raidEventCache.addRaid(cachedRaid);

                var bus = ViaMercatorum.getInstance().getEventBus();
                bus.emit(new EconomyEvent.RaidOccurred(
                    caravan.getDestinationNode().getId(), tickCounter,
                    route.getId(), caravan.getId(),
                    caravan.getDestinationNode().getId(), true,
                    lootedGoods, cx, cz, raiderCount, true
                ));

                routeGraph.removeCaravan(caravan.getId());
            } else {
                route.addPiracy(config.piracy.banditsClearedBonus);

                int cx = (int) Math.floor(caravan.getOriginNode().getChunkX()
                    + (caravan.getDestinationNode().getChunkX() - caravan.getOriginNode().getChunkX()) * caravan.getProgress());
                int cz = (int) Math.floor(caravan.getOriginNode().getChunkZ()
                    + (caravan.getDestinationNode().getChunkZ() - caravan.getOriginNode().getChunkZ()) * caravan.getProgress());

                RaidEventCache.CachedRaid cachedRaid = new RaidEventCache.CachedRaid(
                    cx, cz, true, caravan.getId(), route.getId(),
                    caravan.getDestinationNode().getId(),
                    tickCounter, raiderCount, false, Map.of()
                );
                raidEventCache.addRaid(cachedRaid);

                var bus = ViaMercatorum.getInstance().getEventBus();
                bus.emit(new EconomyEvent.BanditsCleared(
                    caravan.getDestinationNode().getId(), tickCounter,
                    route.getId(), raiderCount
                ));
            }
        }
    }

    private void tickVillageRaids(ViaMercatorumConfig config) {
        for (VillageNode village : routeGraph.getAllVillages()) {
            if (village.getType() != VillageNode.VillageType.HAMLET
                && village.getType() != VillageNode.VillageType.VILLAGE) continue;

            double wealth = EconomyHelper.totalWealth(village);
            if (wealth >= config.ai.villageRaidWealthThreshold) continue;

            if (random.nextDouble() >= config.ai.villageRaidChance) continue;

            int raiderCount = 3 + random.nextInt(4);
            int guardCount = getVillageGuardCount(village);
            boolean raidersWon = resolveCombat(guardCount, raiderCount, random);

            if (raidersWon) {
                for (Supply supply : village.getSupplies().values()) {
                    double loss = supply.getStock() * (0.1 + random.nextDouble() * 0.2);
                    supply.consume(loss);
                }

                RaidEventCache.CachedRaid cachedRaid = new RaidEventCache.CachedRaid(
                    village.getChunkX(), village.getChunkZ(), false,
                    null, null, village.getId(),
                    tickCounter, raiderCount, true, Map.of()
                );
                raidEventCache.addRaid(cachedRaid);

                var bus = ViaMercatorum.getInstance().getEventBus();
                bus.emit(new EconomyEvent.RaidOccurred(
                    village.getId(), tickCounter,
                    null, null, village.getId(), false,
                    Map.of(), village.getChunkX(), village.getChunkZ(),
                    raiderCount, true
                ));
            } else {
                RaidEventCache.CachedRaid cachedRaid = new RaidEventCache.CachedRaid(
                    village.getChunkX(), village.getChunkZ(), false,
                    null, null, village.getId(),
                    tickCounter, raiderCount, false, Map.of()
                );
                raidEventCache.addRaid(cachedRaid);

                var bus = ViaMercatorum.getInstance().getEventBus();
                bus.emit(new EconomyEvent.BanditsCleared(
                    village.getId(), tickCounter, null, raiderCount
                ));
            }
        }
    }

    private boolean resolveCombat(int guardCount, int raiderCount, Random rng) {
        double guardPower = guardCount * 0.35 + rng.nextDouble() * 0.3;
        double raiderPower = raiderCount * 0.65 + rng.nextDouble() * 0.4;
        return raiderPower >= guardPower;
    }

    private int getGuardCount(Caravan caravan, TradeRoute route) {
        VillageNode.VillageType type = caravan.getOriginNode().getType();
        return switch (type) {
            case HAMLET -> 1;
            case VILLAGE -> 2;
            case TOWN -> 3;
            case CITY, FORTIFIED -> 4;
        };
    }

    private int getVillageGuardCount(VillageNode village) {
        return switch (village.getType()) {
            case HAMLET -> 1;
            case VILLAGE -> 2;
            case TOWN -> 3;
            case CITY, FORTIFIED -> 5;
        };
    }

    public long getTickCounter() { return tickCounter; }
    public void setTickCounter(long tickCounter) { this.tickCounter = tickCounter; }
    public boolean isRunning() { return running; }

    public void fastForward(long ticks) {
        for (long i = 0; i < ticks; i++) {
            tickCounter++;
            tick();
        }
    }
    public RouteGraph getRouteGraph() { return routeGraph; }
    public Market getGlobalMarket() { return globalMarket; }
    public RaidEventCache getRaidEventCache() { return raidEventCache; }
    public SeasonManager getSeasonManager() { return seasonManager; }
    public ActiveModifierManager getModifierManager() { return modifierManager; }
    public EventEngine getEventEngine() { return eventEngine; }
    public WorkshopManager getWorkshopManager() { return workshopManager; }
    public VillageGoalEngine getGoalEngine() { return goalEngine; }
    public CharacterAI getCharacterAI() { return characterAI; }

    private void checkDisaster(VillageNode village, ViaMercatorumConfig config) {
        if (random.nextDouble() >= config.season.disasterChancePerTick) return;

        double modifier = seasonManager.getHarvestModifier(village.getBiome());
        if (modifier >= 1.0) return;

        double famineRisk = (1.0 - modifier) * 0.5;
        if (EconomyHelper.hasFoodCrisis(village, config.economy.foodCrisisThreshold)) {
            famineRisk += 0.3;
        }

        if (random.nextDouble() < famineRisk) {
            String disasterType = seasonManager.isFamineSeason(village.getBiome()) ? "famine" : "crop_failure";
            int severity = 1 + random.nextInt(3);

            for (Supply supply : village.getSupplies().values()) {
                if (supply.getGood().category() == TradeGood.Category.FOOD) {
                    double loss = supply.getStock() * (config.productionScale.disasterSeverityLossRate * severity);
                    supply.consume(loss);
                }
            }

            var bus = ViaMercatorum.getInstance().getEventBus();
            bus.emit(new EconomyEvent.DisasterEvent(village.getId(), tickCounter, disasterType, severity));
        }
    }

    private void checkFamine(VillageNode village, ViaMercatorumConfig config) {
        double foodRatio = village.getFoodRatio();
        if (foodRatio <= 0.001) {
            village.setPopulation(Math.max(1, village.getPopulation() - config.consumption.starvationPopulationLossRate));
            village.addReputation(-config.consumption.starvationReputationLossRate);
        } else if (foodRatio < config.season.famineThreshold) {
            double popLoss = config.consumption.faminePopulationLossRate * (1.0 - foodRatio);
            double repLoss = -config.consumption.famineReputationLossRate * (1.0 - foodRatio);
            village.setPopulation(Math.max(1, village.getPopulation() - popLoss));
            village.addReputation(repLoss);
        }
    }

    private void tickRoadDegradation(ViaMercatorumConfig config) {
        for (TradeRoute route : routeGraph.getAllRoutes()) {
            double maintenanceLevel = route.getMaintenanceLevel();
            double traffic = route.getTraffic();

            if (traffic >= config.roadMaintenance.minTrafficToMaintain) {
                maintenanceLevel += config.roadMaintenance.maintenanceFromTraffic;
            } else {
                maintenanceLevel -= config.roadMaintenance.degradationRate;
            }

            maintenanceLevel = Math.max(0, Math.min(1, maintenanceLevel));
            route.setMaintenanceLevel(maintenanceLevel);

            if (maintenanceLevel < config.roadMaintenance.degradeThreshold && route.getQuality() != TradeRoute.RouteQuality.DIRT) {
                String oldQuality = route.getQuality().name();
                route.degradeQuality();
                var bus = ViaMercatorum.getInstance().getEventBus();
                String villageId = route.getTo().getId();
                bus.emit(new EconomyEvent.RoadDegraded(
                    villageId, tickCounter, route.getId(),
                    oldQuality, route.getQuality().name(), maintenanceLevel
                ));
            }
        }
    }

    private void tickResettlement() {
        long resettleTicks = ViaMercatorum.getInstance().getConfig().population.resettleTicks;
        for (VillageNode village : routeGraph.getAllVillages()) {
            if (village.isRaided() && village.canResettle(tickCounter, resettleTicks)) {
                village.resettle();
                var bus = ViaMercatorum.getInstance().getEventBus();
                bus.emit(new EconomyEvent.VillageResettled(
                    village.getId(), tickCounter, village.getType()
                ));
            }
        }
    }

    private void tickPopulationGrowth(ViaMercatorumConfig config) {
        for (VillageNode village : routeGraph.getAllVillages()) {
            double maxPop = village.getMaxPopulation();
            if (village.getPopulation() >= maxPop) continue;

            double foodRatio = village.getFoodRatio();
            if (foodRatio > config.population.growthFoodRatioThreshold) {
                double satisfaction = (foodRatio - config.population.growthFoodRatioThreshold) /
                    (1.0 - config.population.growthFoodRatioThreshold);
                double growth = config.population.growthRate * village.getPopulation() * (1.0 + satisfaction);
                village.setPopulation(Math.min(maxPop, village.getPopulation() + growth));
            }
        }
    }
}