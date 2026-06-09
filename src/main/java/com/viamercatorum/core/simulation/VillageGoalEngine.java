package com.viamercatorum.core.simulation;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.config.ViaMercatorumConfig;
import com.viamercatorum.core.economy.Supply;
import com.viamercatorum.core.economy.TradeGood;
import com.viamercatorum.core.world.MilitaryPower;
import com.viamercatorum.core.world.RouteGraph;
import com.viamercatorum.core.world.TradeRoute;
import com.viamercatorum.core.world.VillageNode;
import com.viamercatorum.core.world.WorkshopInstance;
import com.viamercatorum.core.world.WorkshopManager;
import com.viamercatorum.data.VillageGoal;
import com.viamercatorum.data.VillageGoalLoader;

import java.util.*;

public class VillageGoalEngine {
    private final Random random = new Random();

    public void tick(RouteGraph routeGraph, long currentTick) {
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        if (!config.goal.enabled) return;

        VillageGoalLoader loader = ViaMercatorum.getInstance().getVillageGoalLoader();
        if (loader == null || loader.getGoals().isEmpty()) return;

        for (VillageNode village : routeGraph.getAllVillages()) {
            VillageGoal bestGoal = selectGoal(village, routeGraph, loader, config);
            if (bestGoal != null) {
                String previousGoal = village.getActiveGoalId();
                if (!bestGoal.goalId().equals(previousGoal)) {
                    village.setActiveGoalId(bestGoal.goalId());
                    var bus = ViaMercatorum.getInstance().getEventBus();
                    bus.emit(new EconomyEvent.VillageGoalSet(
                        village.getId(), currentTick, bestGoal.goalId(), previousGoal));
                }
                applyActions(bestGoal, village, routeGraph, currentTick, config);
            }

            tickMobilization(village, currentTick, config);
        }
    }

    private VillageGoal selectGoal(VillageNode village, RouteGraph routeGraph,
                                     VillageGoalLoader loader, ViaMercatorumConfig config) {
        double bestScore = -1;
        VillageGoal bestGoal = null;

        for (VillageGoal goal : loader.getGoals().values()) {
            if (!evaluateConditions(goal.conditions(), village, routeGraph, config)) continue;

            double score = goal.priority();
            score *= getUrgencyMultiplier(goal.category(), village, routeGraph, config);

            if (score > bestScore) {
                bestScore = score;
                bestGoal = goal;
            }
        }

        return bestGoal;
    }

    public boolean evaluateConditions(List<VillageGoal.GoalCondition> conditions,
                                         VillageNode village, RouteGraph routeGraph,
                                         ViaMercatorumConfig config) {
        for (VillageGoal.GoalCondition cond : conditions) {
            if (!evaluateCondition(cond, village, routeGraph, config)) return false;
        }
        return true;
    }

    private boolean evaluateCondition(VillageGoal.GoalCondition cond,
                                        VillageNode village, RouteGraph routeGraph,
                                        ViaMercatorumConfig config) {
        double threshold = cond.threshold().orElse(0.5);
        return switch (cond.type()) {
            case "food_ratio_below" -> village.getFoodRatio() < threshold;
            case "food_ratio_above" -> village.getFoodRatio() > threshold;
            case "population_below" -> village.getPopulation() < threshold;
            case "population_above" -> village.getPopulation() > threshold;
            case "population_below_max_ratio" -> village.getPopulation() < village.getMaxPopulation() * threshold;
            case "military_power_below" -> MilitaryPower.calculateVillagePower(village, routeGraph) < threshold;
            case "military_power_above" -> MilitaryPower.calculateVillagePower(village, routeGraph) > threshold;
            case "piracy_above" -> {
                double maxPiracy = 0;
                for (TradeRoute route : routeGraph.getAllRoutes()) {
                    if (route.getFrom().getId().equals(village.getId()) || route.getTo().getId().equals(village.getId())) {
                        maxPiracy = Math.max(maxPiracy, route.getPiracyScore());
                    }
                }
                yield maxPiracy > threshold;
            }
            case "is_coastal" -> village.isCoastal();
            case "village_type" -> cond.value().isPresent() && village.getType().name().equalsIgnoreCase(cond.value().get());
            case "has_workshop_type" -> {
                String workshopId = cond.value().orElse("");
                WorkshopManager wm = ViaMercatorum.getInstance().getWorkshopManager();
                yield wm != null && wm.getWorkshopsForVillage(village.getId()).stream()
                    .anyMatch(w -> w.getWorkshopId().equals(workshopId));
            }
            case "reputation_below" -> village.getReputation() < threshold;
            case "reputation_above" -> village.getReputation() > threshold;
            default -> true;
        };
    }

    public double getUrgencyMultiplier(String category, VillageNode village,
                                          RouteGraph routeGraph, ViaMercatorumConfig config) {
        return switch (category) {
            case "FOOD_SECURITY" -> village.getFoodRatio() < 0.3 ? 3.0 : village.getFoodRatio() < 0.5 ? 2.0 : 1.0;
            case "MILITARY" -> village.isRaided() ? 3.0 : 1.0;
            case "TRADE" -> village.isCoastal() ? 1.5 : 1.0;
            case "GROWTH" -> village.getPopulation() < village.getMaxPopulation() * 0.3 ? 2.0 : 1.0;
            case "ROAD_SECURITY" -> {
                double maxPiracy = 0;
                for (TradeRoute route : routeGraph.getAllRoutes()) {
                    if (route.getFrom().getId().equals(village.getId()) || route.getTo().getId().equals(village.getId())) {
                        maxPiracy = Math.max(maxPiracy, route.getPiracyScore());
                    }
                }
                yield maxPiracy > 0.5 ? 2.5 : 1.0;
            }
            case "PRODUCTION" -> 1.0;
            default -> 1.0;
        };
    }

    private void applyActions(VillageGoal goal, VillageNode village, RouteGraph routeGraph,
                               long currentTick, ViaMercatorumConfig config) {
        for (VillageGoal.GoalAction action : goal.actions()) {
            applyAction(action, goal, village, routeGraph, currentTick, config);
        }
    }

    private void applyAction(VillageGoal.GoalAction action, VillageGoal goal, VillageNode village,
                              RouteGraph routeGraph, long currentTick, ViaMercatorumConfig config) {
        double intensity = action.intensity().orElse(1.0);

        switch (action.type()) {
            case "increase_production" -> applyIncreaseProduction(action, village, intensity);
            case "build_workshop" -> applyBuildWorkshop(action, village, currentTick);
            case "mobilize" -> applyMobilize(village, currentTick, config);
            case "demobilize" -> applyDemobilize(village, currentTick);
            case "prioritize_trade" -> { }
            case "improve_roads" -> applyImproveRoads(village, routeGraph);
            default -> ViaMercatorum.LOGGER.warn("Unknown goal action type: {}", action.type());
        }
    }

    private void applyIncreaseProduction(VillageGoal.GoalAction action, VillageNode village, double intensity) {
        String target = action.target().orElse("*");
        ActiveModifierManager modManager = ViaMercatorum.getInstance().getSimulationTick().getModifierManager();
        ActiveModifier mod = ActiveModifier.create(
            "goal_" + village.getId(), "VILLAGE", village.getId(),
            "modify_production_category", target, intensity * 0.2, 0.0,
            ViaMercatorum.getInstance().getSimulationTick().getTickCounter(), 2000L
        );
        modManager.addModifier(mod);
    }

    private void applyBuildWorkshop(VillageGoal.GoalAction action, VillageNode village, long currentTick) {
        String workshopId = action.target().orElse(null);
        if (workshopId == null) return;

        WorkshopManager wm = ViaMercatorum.getInstance().getWorkshopManager();
        if (wm == null) return;

        long count = wm.getWorkshopsForVillage(village.getId()).stream()
            .filter(w -> w.getWorkshopId().equals(workshopId))
            .count();
        if (count > 0) return;

        if (wm.getWorkshopsForVillage(village.getId()).size() >= ViaMercatorum.getInstance().getConfig().workshop.maxWorkshopsPerVillage) return;

        wm.createWorkshop(village.getId(), workshopId);
    }

    private void applyMobilize(VillageNode village, long currentTick, ViaMercatorumConfig config) {
        if (village.getMobilization() == MilitaryPower.MobilizationState.DEMILITARIZED) {
            village.setMobilization(MilitaryPower.MobilizationState.STANDBY, currentTick);
            var bus = ViaMercatorum.getInstance().getEventBus();
            bus.emit(new EconomyEvent.VillageMobilized(
                village.getId(), currentTick, MilitaryPower.MobilizationState.STANDBY.name()));
        } else if (village.getMobilization() == MilitaryPower.MobilizationState.STANDBY
                && village.canMobilize(currentTick)) {
            village.setMobilization(MilitaryPower.MobilizationState.MOBILIZED, currentTick);
            var bus = ViaMercatorum.getInstance().getEventBus();
            bus.emit(new EconomyEvent.VillageMobilized(
                village.getId(), currentTick, MilitaryPower.MobilizationState.MOBILIZED.name()));
        }
    }

    private void applyDemobilize(VillageNode village, long currentTick) {
        if (village.canDemobilize(currentTick)) {
            village.setMobilization(MilitaryPower.MobilizationState.DEMILITARIZED, currentTick);
            var bus = ViaMercatorum.getInstance().getEventBus();
            bus.emit(new EconomyEvent.VillageMobilized(
                village.getId(), currentTick, MilitaryPower.MobilizationState.DEMILITARIZED.name()));
        }
    }

    private void applyImproveRoads(VillageNode village, RouteGraph routeGraph) {
        for (TradeRoute route : routeGraph.getAllRoutes()) {
            if (route.getFrom().getId().equals(village.getId()) || route.getTo().getId().equals(village.getId())) {
                if (route.getMaintenanceLevel() < 0.5) {
                    route.setMaintenanceLevel(route.getMaintenanceLevel() + 0.01);
                }
            }
        }
    }

    private void tickMobilization(VillageNode village, long currentTick, ViaMercatorumConfig config) {
        if (village.getMobilization() == MilitaryPower.MobilizationState.MOBILIZED) {
            if ((currentTick - village.getMobilizedAtTick()) > config.mobilization.mobilizeDurationTicks) {
                village.setMobilization(MilitaryPower.MobilizationState.STANDBY, currentTick);
            }
        }
    }
}