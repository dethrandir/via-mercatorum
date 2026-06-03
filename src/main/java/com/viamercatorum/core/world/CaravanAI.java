package com.viamercatorum.core.world;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.config.ViaMercatorumConfig;
import com.viamercatorum.core.economy.EconomyHelper;
import com.viamercatorum.core.economy.Supply;
import com.viamercatorum.core.economy.TradeGood;
import com.viamercatorum.core.economy.TradeGoodRegistry;

import java.util.*;
import java.util.stream.Collectors;

public class CaravanAI {
    private static final Random RNG = new Random();

    private CaravanAI() {}

    public static TradeRoute chooseRoute(VillageNode from, Collection<TradeRoute> candidates) {
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        List<TradeRoute> list = new ArrayList<>(candidates);
        if (list.isEmpty()) return null;

        list.sort(Comparator.comparingDouble(route -> {
            double piracyScore = route.getPiracyScore();
            double qualityBonus = route.getQuality().speedMultiplier;
            double randomFactor = RNG.nextDouble() * config.ai.routeRandomFactor;
            return piracyScore * 10 - qualityBonus * 2 + randomFactor;
        }));

        return list.get(0);
    }

    public static Map<String, Double> chooseCargo(VillageNode from, VillageNode to) {
        var profitable = EconomyHelper.profitableGoods(from, to);
        Map<String, Double> cargo = new HashMap<>();
        double totalCapacity = 50.0;
        double remaining = totalCapacity;

        int count = Math.min(3, profitable.size());
        for (int i = 0; i < count && remaining > 0; i++) {
            var entry = profitable.get(i);
            Supply supply = entry.getValue();
            double amount = Math.min(supply.getStock() * 0.3, Math.min(remaining, 20.0));
            if (amount > 1) {
                cargo.put(entry.getKey(), amount);
                remaining -= amount;
            }
        }

        if (remaining > 5) {
            List<Map.Entry<String, Supply>> surplus = from.getSupplies().entrySet().stream()
                .filter(e -> !cargo.containsKey(e.getKey())
                    && e.getValue().getStock() / Math.max(1, e.getValue().getMaxCapacity()) > 0.5)
                .collect(Collectors.toList());
            if (!surplus.isEmpty()) {
                var extra = surplus.get(RNG.nextInt(surplus.size()));
                double amount = Math.min(extra.getValue().getStock() * 0.2, remaining);
                if (amount > 1) {
                    cargo.put(extra.getKey(), amount);
                }
            }
        }

        return cargo;
    }

    public static int determineStayDuration(Caravan caravan, VillageNode currentVillage) {
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        int baseDuration = 1;

        if (EconomyHelper.hasFoodCrisis(currentVillage)) {
            boolean hasFood = caravan.getCargo().keySet().stream()
                .anyMatch(id -> {
                    TradeGood good = TradeGoodRegistry.get(id);
                    return good != null && good.category() == TradeGood.Category.FOOD;
                });
            if (hasFood) {
                baseDuration = (int) Math.ceil(baseDuration * config.ai.foodCrisisStayMultiplier);
            }
        }

        return baseDuration;
    }
}
