package com.viamercatorum.core.economy;

import com.viamercatorum.core.world.RouteGraph;
import com.viamercatorum.core.world.VillageNode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Market {
    private final Map<String, Supply> globalSupplies = new ConcurrentHashMap<>();
    private final Map<String, Double> globalDemands = new ConcurrentHashMap<>();

    public void registerGood(TradeGood good, double initialStock, double productionRate, double maxCapacity) {
        globalSupplies.put(good.id(), new Supply(good, initialStock, productionRate, maxCapacity));
        globalDemands.put(good.id(), 100.0);
    }

    public Supply getSupply(String goodId) {
        return globalSupplies.get(goodId);
    }

    public double getDemand(String goodId) {
        return globalDemands.getOrDefault(goodId, 100.0);
    }

    public void setDemand(String goodId, double demand) {
        globalDemands.put(goodId, demand);
    }

    public void tickAll() {
        for (Supply supply : globalSupplies.values()) {
            supply.tickProduction();
        }
    }

    public void updateDemands(RouteGraph routeGraph) {
        Map<String, Double> newDemands = new ConcurrentHashMap<>();
        for (VillageNode village : routeGraph.getAllVillages()) {
            for (var entry : village.getLastDemand().entrySet()) {
                newDemands.merge(entry.getKey(), entry.getValue(), Double::sum);
            }
        }
        for (var entry : newDemands.entrySet()) {
            globalDemands.put(entry.getKey(), entry.getValue());
        }
    }

    public Map<String, Supply> getAllSupplies() {
        return globalSupplies;
    }

    public Map<String, Double> getAllDemands() {
        return globalDemands;
    }
}
