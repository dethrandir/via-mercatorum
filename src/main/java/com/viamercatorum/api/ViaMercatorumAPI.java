package com.viamercatorum.api;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.economy.PriceCalculator;
import com.viamercatorum.core.economy.TradeGood;
import com.viamercatorum.core.world.RouteGraph;
import com.viamercatorum.core.world.VillageNode;
import com.viamercatorum.player.ReputationSystem;
import com.viamercatorum.player.SkillSystem;

import java.util.UUID;

public final class ViaMercatorumAPI {
    private static final ViaMercatorum MOD = ViaMercatorum.getInstance();

    private ViaMercatorumAPI() {}

    public static RouteGraph getRouteGraph() {
        return MOD.getRouteGraph();
    }

    public static double getBuyPrice(TradeGood good, double stock, double demand, double reputation) {
        return PriceCalculator.calculateBuyPrice(good, stock, demand, reputation);
    }

    public static double getSellPrice(TradeGood good, double stock, double demand, double reputation) {
        return PriceCalculator.calculateSellPrice(good, stock, demand, reputation);
    }

    public static boolean registerVillage(VillageNode village) {
        MOD.getRouteGraph().addVillage(village);
        return true;
    }

    public static void addReputation(UUID player, String villageId, double amount) {
        ReputationSystem repSystem = MOD.getReputationSystem();
        if (repSystem != null) {
            repSystem.addGlobalReputation(player, amount);
            repSystem.addVillageReputation(player, villageId, amount);
        }
    }
}
