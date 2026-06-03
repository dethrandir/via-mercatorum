package com.viamercatorum.core.simulation;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.config.ViaMercatorumConfig;
import com.viamercatorum.core.economy.EconomyHelper;
import com.viamercatorum.core.economy.Market;
import com.viamercatorum.core.world.*;
import com.viamercatorum.core.world.TradeRoute;
import com.viamercatorum.core.world.VillageNode;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimulationTick {
    private final RouteGraph routeGraph;
    private final Market globalMarket;
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
    }

    public SimulationTick(RouteGraph routeGraph, Market globalMarket) {
        this.routeGraph = routeGraph;
        this.globalMarket = globalMarket;
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

        globalMarket.tickAll();

        for (VillageNode village : routeGraph.getAllVillages()) {
            village.tickProduction();
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

        if (tickCounter % 1200 == 0) {
            routeGraph.spawnNewCaravans();
        }
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

    public long getTickCounter() { return tickCounter; }
    public boolean isRunning() { return running; }
    public RouteGraph getRouteGraph() { return routeGraph; }
    public Market getGlobalMarket() { return globalMarket; }
}
