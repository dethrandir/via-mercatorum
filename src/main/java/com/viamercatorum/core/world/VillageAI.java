package com.viamercatorum.core.world;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.config.ViaMercatorumConfig;
import com.viamercatorum.core.economy.EconomyHelper;
import com.viamercatorum.core.economy.Supply;

import java.util.*;
import java.util.stream.Collectors;

public class VillageAI {
    private VillageAI() {}

    public static void optimizeProduction(VillageNode village, RouteGraph graph) {
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        Map<String, Double> demandWeights = new HashMap<>();

        for (TradeRoute route : graph.getAllRoutes()) {
            VillageNode other;
            if (route.getFrom().equals(village)) {
                other = route.getTo();
            } else if (route.getTo().equals(village)) {
                other = route.getFrom();
            } else {
                continue;
            }

            var profitable = EconomyHelper.profitableGoods(village, other,
                config.ai.minStockForExport, config.ai.maxStockForImport);
            for (var entry : profitable) {
                demandWeights.merge(entry.getKey(), 1.0, Double::sum);
            }
        }

        for (var entry : village.getSupplies().entrySet()) {
            String goodId = entry.getKey();
            Supply supply = entry.getValue();
            double weight = demandWeights.getOrDefault(goodId, 0.0);

            if (weight > 0) {
                double boost = supply.getProductionRate() * config.ai.productionRateBoost * weight;
                supply.setProductionRate(Math.min(
                    supply.getProductionRate() * config.ai.maxProductionMultiplier,
                    supply.getProductionRate() + boost
                ));
            }
        }
    }

    public static VillageNeed assessNeeds(VillageNode village) {
        if (EconomyHelper.hasFoodCrisis(village)) {
            return VillageNeed.FOOD_CRISIS;
        }

        double totalDefense = 0;
        for (Supply supply : village.getSupplies().values()) {
            if (supply.getGood().category() == com.viamercatorum.core.economy.TradeGood.Category.WEAPON
                || supply.getGood().category() == com.viamercatorum.core.economy.TradeGood.Category.ARMOR) {
                totalDefense += supply.getStock();
            }
        }
        if (totalDefense < 5) {
            return VillageNeed.DEFENSE_CRISIS;
        }

        double surplusCount = 0;
        for (Supply supply : village.getSupplies().values()) {
            if (supply.getStock() / Math.max(1, supply.getMaxCapacity()) > 0.7) {
                surplusCount++;
            }
        }
        if (surplusCount >= village.getSupplies().size() * 0.5) {
            return VillageNeed.SURPLUS;
        }

        return VillageNeed.STABLE;
    }

    public static List<TradeRoute> prioritizeRoutes(VillageNode village, Collection<TradeRoute> routes) {
        VillageNeed need = assessNeeds(village);
        List<TradeRoute> prioritized = new ArrayList<>(routes);

        prioritized.sort((a, b) -> {
            VillageNode otherA = a.getFrom().equals(village) ? a.getTo() : a.getFrom();
            VillageNode otherB = b.getFrom().equals(village) ? b.getTo() : b.getFrom();
            double scoreA = routeScore(village, otherA, need);
            double scoreB = routeScore(village, otherB, need);
            return Double.compare(scoreB, scoreA);
        });

        return prioritized;
    }

    private static double routeScore(VillageNode village, VillageNode other, VillageNeed need) {
        return switch (need) {
            case FOOD_CRISIS -> {
                double foodScore = 0;
                for (var entry : other.getSupplies().entrySet()) {
                    if (entry.getValue().getGood().category() == com.viamercatorum.core.economy.TradeGood.Category.FOOD) {
                        foodScore += entry.getValue().getStock();
                    }
                }
                yield foodScore;
            }
            case DEFENSE_CRISIS -> {
                double defScore = 0;
                for (var entry : other.getSupplies().entrySet()) {
                    var cat = entry.getValue().getGood().category();
                    if (cat == com.viamercatorum.core.economy.TradeGood.Category.WEAPON
                        || cat == com.viamercatorum.core.economy.TradeGood.Category.ARMOR) {
                        defScore += entry.getValue().getStock();
                    }
                }
                yield defScore;
            }
            case SURPLUS -> EconomyHelper.totalWealth(other) * (-1);
            case STABLE -> EconomyHelper.totalWealth(other);
        };
    }

    public enum VillageNeed {
        FOOD_CRISIS,
        DEFENSE_CRISIS,
        SURPLUS,
        STABLE
    }
}
