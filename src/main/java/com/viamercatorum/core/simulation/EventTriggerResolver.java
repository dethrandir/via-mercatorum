package com.viamercatorum.core.simulation;

import com.viamercatorum.core.economy.EconomyHelper;
import com.viamercatorum.core.economy.TradeGoodRegistry;
import com.viamercatorum.core.economy.TradeGood;
import com.viamercatorum.core.world.RouteGraph;
import com.viamercatorum.core.world.VillageNode;
import com.viamercatorum.data.WorldEventProfile;

import java.util.List;
import java.util.Random;

public class EventTriggerResolver {

    private final Random random;

    public EventTriggerResolver() {
        this.random = new Random();
    }

    public boolean evaluateTriggers(List<WorldEventProfile.EventTrigger> triggers,
                                      VillageNode village, RouteGraph routeGraph,
                                      SeasonManager seasonManager) {
        for (WorldEventProfile.EventTrigger trigger : triggers) {
            if (!evaluateTrigger(trigger, village, routeGraph, seasonManager)) {
                return false;
            }
        }
        return true;
    }

    private boolean evaluateTrigger(WorldEventProfile.EventTrigger trigger,
                                     VillageNode village, RouteGraph routeGraph,
                                     SeasonManager seasonManager) {
        return switch (trigger.type()) {
            case "season_is" -> evaluateSeasonIs(trigger, seasonManager);
            case "village_type" -> evaluateVillageType(trigger, village);
            case "population_above" -> evaluatePopulationAbove(trigger, village);
            case "population_below" -> evaluatePopulationBelow(trigger, village);
            case "food_ratio_above" -> evaluateFoodRatioAbove(trigger, village);
            case "food_ratio_below" -> evaluateFoodRatioBelow(trigger, village);
            case "supply_ratio_above" -> evaluateSupplyRatioAbove(trigger, village);
            case "supply_ratio_below" -> evaluateSupplyRatioBelow(trigger, village);
            case "piracy_above" -> evaluatePiracyAbove(trigger, village, routeGraph);
            case "village_is_coastal" -> village.isCoastal();
            case "random_chance" -> evaluateRandomChance(trigger);
            default -> {
                com.viamercatorum.ViaMercatorum.LOGGER.warn("Unknown trigger type: {}", trigger.type());
                yield true;
            }
        };
    }

    private boolean evaluateSeasonIs(WorldEventProfile.EventTrigger trigger, SeasonManager seasonManager) {
        if (trigger.value().isEmpty()) return true;
        String targetSeason = trigger.value().get();
        String current = seasonManager.getCurrentSeason().name();
        return current.equalsIgnoreCase(targetSeason);
    }

    private boolean evaluateVillageType(WorldEventProfile.EventTrigger trigger, VillageNode village) {
        if (trigger.value().isEmpty()) return true;
        return village.getType().name().equalsIgnoreCase(trigger.value().get());
    }

    private boolean evaluatePopulationAbove(WorldEventProfile.EventTrigger trigger, VillageNode village) {
        double threshold = trigger.threshold().orElse(0.0);
        return village.getPopulation() > threshold;
    }

    private boolean evaluatePopulationBelow(WorldEventProfile.EventTrigger trigger, VillageNode village) {
        double threshold = trigger.threshold().orElse(Double.MAX_VALUE);
        return village.getPopulation() < threshold;
    }

    private boolean evaluateFoodRatioAbove(WorldEventProfile.EventTrigger trigger, VillageNode village) {
        double threshold = trigger.threshold().orElse(0.0);
        return village.getFoodRatio() > threshold;
    }

    private boolean evaluateFoodRatioBelow(WorldEventProfile.EventTrigger trigger, VillageNode village) {
        double threshold = trigger.threshold().orElse(1.0);
        return village.getFoodRatio() < threshold;
    }

    private boolean evaluateSupplyRatioAbove(WorldEventProfile.EventTrigger trigger, VillageNode village) {
        if (trigger.value().isEmpty()) return true;
        String goodId = trigger.value().get();
        double threshold = trigger.threshold().orElse(0.0);
        return village.getSupplyRatio(goodId) > threshold;
    }

    private boolean evaluateSupplyRatioBelow(WorldEventProfile.EventTrigger trigger, VillageNode village) {
        if (trigger.value().isEmpty()) return true;
        String goodId = trigger.value().get();
        double threshold = trigger.threshold().orElse(1.0);
        return village.getSupplyRatio(goodId) < threshold;
    }

    private boolean evaluatePiracyAbove(WorldEventProfile.EventTrigger trigger, VillageNode village, RouteGraph routeGraph) {
        double threshold = trigger.threshold().orElse(0.5);
        for (var route : routeGraph.getAllRoutes()) {
            if ((route.getFrom().getId().equals(village.getId()) || route.getTo().getId().equals(village.getId()))
                && route.getPiracyScore() > threshold) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluateRandomChance(WorldEventProfile.EventTrigger trigger) {
        double chance = trigger.chance().orElse(1.0);
        return random.nextDouble() < chance;
    }
}