package com.viamercatorum.core.economy;

import com.viamercatorum.core.world.VillageNode;

import java.util.*;
import java.util.stream.Collectors;

public class EconomyHelper {
    private EconomyHelper() {}

    public static double totalWealth(VillageNode village) {
        double wealth = 0;
        for (Supply supply : village.getSupplies().values()) {
            wealth += supply.getStock() * supply.getGood().basePrice();
        }
        return wealth;
    }

    public static double gdp(VillageNode village) {
        double gdp = 0;
        for (Supply supply : village.getSupplies().values()) {
            gdp += supply.getProductionRate() * supply.getGood().basePrice();
        }
        return gdp;
    }

    public static double gdpPerCapita(VillageNode village) {
        double pop = Math.max(1, village.getPopulation());
        return gdp(village) / pop;
    }

    public static double prosperityIndex(VillageNode village) {
        double wealth = totalWealth(village);
        double gpc = gdpPerCapita(village);
        double wealthNorm = Math.min(1.0, wealth / 10000.0);
        double gdpNorm = Math.min(1.0, gpc / 20.0);
        return wealthNorm * 0.5 + gdpNorm * 0.5;
    }

    public static double prosperityIndex(double wealth, double gdpPerCapita) {
        double wealthNorm = Math.min(1.0, wealth / 10000.0);
        double gdpNorm = Math.min(1.0, gdpPerCapita / 20.0);
        return wealthNorm * 0.5 + gdpNorm * 0.5;
    }

    public static boolean hasFoodCrisis(VillageNode village, double threshold) {
        double totalFoodStock = 0;
        double totalFoodCapacity = 0;
        for (Supply supply : village.getSupplies().values()) {
            if (supply.getGood().category() == TradeGood.Category.FOOD) {
                totalFoodStock += supply.getStock();
                totalFoodCapacity += supply.getMaxCapacity();
            }
        }
        if (totalFoodCapacity <= 0) return false;
        return (totalFoodStock / totalFoodCapacity) < threshold;
    }

    public static boolean hasFoodCrisis(VillageNode village) {
        return hasFoodCrisis(village, 0.2);
    }

    public static List<Map.Entry<String, Supply>> profitableGoods(VillageNode from, VillageNode to,
                                                                    double fromThreshold, double toThreshold) {
        List<Map.Entry<String, Supply>> result = new ArrayList<>();
        for (Map.Entry<String, Supply> entry : from.getSupplies().entrySet()) {
            String goodId = entry.getKey();
            Supply fromSupply = entry.getValue();
            Supply toSupply = to.getSupply(goodId);

            if (fromSupply.getStock() / Math.max(1, fromSupply.getMaxCapacity()) >= fromThreshold) {
                if (toSupply == null || toSupply.getStock() / Math.max(1, toSupply.getMaxCapacity()) <= toThreshold) {
                    result.add(entry);
                }
            }
        }
        return result;
    }

    public static List<Map.Entry<String, Supply>> profitableGoods(VillageNode from, VillageNode to) {
        return profitableGoods(from, to, 0.6, 0.3);
    }

    public static List<String> urgentGoods(VillageNode village, int limit) {
        return village.getSupplies().entrySet().stream()
            .filter(e -> {
                double ratio = e.getValue().getStock() / Math.max(1, e.getValue().getMaxCapacity());
                return ratio < 0.3;
            })
            .sorted(Comparator.comparingDouble(e ->
                e.getValue().getStock() / Math.max(1, e.getValue().getMaxCapacity())))
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
}
