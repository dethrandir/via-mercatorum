package com.viamercatorum.core.simulation;

import com.viamercatorum.core.economy.EconomyHelper;
import com.viamercatorum.core.economy.Supply;
import com.viamercatorum.core.economy.TradeGood;
import com.viamercatorum.core.economy.TradeGoodRegistry;
import com.viamercatorum.core.world.RouteGraph;
import com.viamercatorum.core.world.TradeRoute;
import com.viamercatorum.core.world.VillageNode;
import com.viamercatorum.data.WorldEventProfile;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class EventEffectResolver {

    private final Random random;

    public EventEffectResolver() {
        this.random = new Random();
    }

    public void applyEffects(List<WorldEventProfile.EventEffect> effects,
                              WorldEventProfile profile,
                              VillageNode village, RouteGraph routeGraph,
                              ActiveModifierManager modifierManager,
                              long currentTick) {
        for (WorldEventProfile.EventEffect effect : effects) {
            applyEffect(effect, profile, village, routeGraph, modifierManager, currentTick);
        }
    }

    public void applyGlobalEffects(List<WorldEventProfile.EventEffect> effects,
                                    WorldEventProfile profile,
                                    RouteGraph routeGraph,
                                    ActiveModifierManager modifierManager,
                                    long currentTick) {
        for (var village : routeGraph.getAllVillages()) {
            applyEffects(effects, profile, village, routeGraph, modifierManager, currentTick);
        }
    }

    private void applyEffect(WorldEventProfile.EventEffect effect,
                              WorldEventProfile profile,
                              VillageNode village, RouteGraph routeGraph,
                              ActiveModifierManager modifierManager,
                              long currentTick) {
        switch (effect.type()) {
            case "modify_supply" -> applyModifySupply(effect, village);
            case "modify_supply_category" -> applyModifySupplyCategory(effect, village);
            case "modify_production_rate" -> applyModifyProductionRate(effect, profile, village, modifierManager, currentTick);
            case "modify_production_category" -> applyModifyProductionCategory(effect, profile, village, modifierManager, currentTick);
            case "modify_population" -> applyModifyPopulation(effect, village);
            case "modify_piracy" -> applyModifyPiracy(effect, profile, village, routeGraph, modifierManager, currentTick);
            case "modify_demand" -> applyModifyDemand(effect, profile, village, modifierManager, currentTick);
            case "trigger_raid" -> applyTriggerRaid(effect, village, routeGraph);
            case "degrade_road" -> applyDegradeRoad(effect, village, routeGraph);
            case "change_reputation" -> applyChangeReputation(effect, village);
            case "cancel_trades" -> applyCancelTrades(village, routeGraph);
            default -> com.viamercatorum.ViaMercatorum.LOGGER.warn("Unknown effect type: {}", effect.type());
        }
    }

    private void applyModifySupply(WorldEventProfile.EventEffect effect, VillageNode village) {
        if (effect.target().isEmpty()) return;
        String goodId = effect.target().get();
        Supply supply = village.getSupply(goodId);
        if (supply == null) return;

        if (effect.percentage().isPresent()) {
            double change = supply.getStock() * effect.percentage().get();
            if (change < 0) {
                supply.consume(Math.abs(change));
            } else {
                supply.addStock(change);
            }
        } else if (effect.amount().isPresent()) {
            double amount = effect.amount().get();
            if (amount < 0) {
                supply.consume(Math.abs(amount));
            } else {
                supply.addStock(amount);
            }
        }
    }

    private void applyModifySupplyCategory(WorldEventProfile.EventEffect effect, VillageNode village) {
        if (effect.target().isEmpty()) return;
        String categoryName = effect.target().get();
        TradeGood.Category category = parseCategory(categoryName);
        if (category == null) return;

        double percentage = effect.percentage().orElse(0.0);
        for (Supply supply : village.getSupplies().values()) {
            if (supply.getGood().category() == category) {
                double change = supply.getStock() * percentage;
                if (change < 0) {
                    supply.consume(Math.abs(change));
                } else {
                    supply.addStock(change);
                }
            }
        }
    }

    private void applyModifyProductionRate(WorldEventProfile.EventEffect effect, WorldEventProfile profile,
                                             VillageNode village, ActiveModifierManager modifierManager,
                                             long currentTick) {
        if (effect.target().isEmpty()) return;
        String goodId = effect.target().get();
        double percentage = effect.percentage().orElse(0.0);

        if (effect.durationTicks().isPresent()) {
            ActiveModifier mod = ActiveModifier.create(
                profile.eventId(), "VILLAGE", village.getId(),
                "modify_production_rate", goodId, percentage, 0.0,
                currentTick, effect.durationTicks().get()
            );
            modifierManager.addModifier(mod);
        } else {
            Supply supply = village.getSupply(goodId);
            if (supply != null) {
                double newRate = supply.getProductionRate() * (1.0 + percentage);
                supply.setProductionRate(Math.max(0.01, newRate));
            }
        }
    }

    private void applyModifyProductionCategory(WorldEventProfile.EventEffect effect, WorldEventProfile profile,
                                                 VillageNode village, ActiveModifierManager modifierManager,
                                                 long currentTick) {
        if (effect.target().isEmpty()) return;
        String categoryName = effect.target().get();
        double percentage = effect.percentage().orElse(0.0);

        if (effect.durationTicks().isPresent()) {
            ActiveModifier mod = ActiveModifier.create(
                profile.eventId(), "VILLAGE", village.getId(),
                "modify_production_category", categoryName.toLowerCase(), percentage, 0.0,
                currentTick, effect.durationTicks().get()
            );
            modifierManager.addModifier(mod);
        } else {
            TradeGood.Category category = parseCategory(categoryName);
            if (category == null) return;
            for (Supply supply : village.getSupplies().values()) {
                if (supply.getGood().category() == category) {
                    double newRate = supply.getProductionRate() * (1.0 + percentage);
                    supply.setProductionRate(Math.max(0.01, newRate));
                }
            }
        }
    }

    private void applyModifyPopulation(WorldEventProfile.EventEffect effect, VillageNode village) {
        if (effect.percentage().isPresent()) {
            double change = village.getPopulation() * effect.percentage().get();
            village.setPopulation(Math.max(1, village.getPopulation() + change));
        } else if (effect.amount().isPresent()) {
            double change = effect.amount().get();
            village.setPopulation(Math.max(1, village.getPopulation() + change));
        }
    }

    private void applyModifyPiracy(WorldEventProfile.EventEffect effect, WorldEventProfile profile,
                                     VillageNode village, RouteGraph routeGraph,
                                     ActiveModifierManager modifierManager, long currentTick) {
        double percentage = effect.percentage().orElse(0.0);

        if (effect.durationTicks().isPresent()) {
            ActiveModifier mod = ActiveModifier.create(
                profile.eventId(), "VILLAGE", village.getId(),
                "modify_piracy", "*", percentage, 0.0,
                currentTick, effect.durationTicks().get()
            );
            modifierManager.addModifier(mod);
        } else {
            for (TradeRoute route : routeGraph.getAllRoutes()) {
                if (route.getFrom().getId().equals(village.getId()) || route.getTo().getId().equals(village.getId())) {
                    route.addPiracy(percentage);
                }
            }
        }
    }

    private void applyModifyDemand(WorldEventProfile.EventEffect effect, WorldEventProfile profile,
                                     VillageNode village, ActiveModifierManager modifierManager,
                                     long currentTick) {
        if (effect.target().isEmpty()) return;
        String goodId = effect.target().get();
        double percentage = effect.percentage().orElse(0.0);

        if (effect.durationTicks().isPresent()) {
            ActiveModifier mod = ActiveModifier.create(
                profile.eventId(), "VILLAGE", village.getId(),
                "modify_demand", goodId, percentage, 0.0,
                currentTick, effect.durationTicks().get()
            );
            modifierManager.addModifier(mod);
        }
    }

    private void applyTriggerRaid(WorldEventProfile.EventEffect effect, VillageNode village, RouteGraph routeGraph) {
        int severity = effect.severity().orElse(1);
        int raiderCount = 3 + severity * 2;

        for (Supply supply : village.getSupplies().values()) {
            double lossRatio = 0.1 + severity * 0.05;
            double loss = supply.getStock() * lossRatio;
            supply.consume(loss);
        }

        village.setRaided(true, 0);
    }

    private void applyDegradeRoad(WorldEventProfile.EventEffect effect, VillageNode village, RouteGraph routeGraph) {
        int levels = effect.severity().orElse(1);
        for (TradeRoute route : routeGraph.getAllRoutes()) {
            if (route.getFrom().getId().equals(village.getId()) || route.getTo().getId().equals(village.getId())) {
                for (int i = 0; i < levels; i++) {
                    if (route.getQuality() != TradeRoute.RouteQuality.DIRT) {
                        route.degradeQuality();
                    }
                }
            }
        }
    }

    private void applyChangeReputation(WorldEventProfile.EventEffect effect, VillageNode village) {
        double amount = effect.amount().orElse(0.0);
        village.addReputation(amount);
    }

    private void applyCancelTrades(VillageNode village, RouteGraph routeGraph) {
        routeGraph.getAllCaravans().stream()
            .filter(c -> c.getOriginNode().getId().equals(village.getId())
                || c.getDestinationNode().getId().equals(village.getId()))
            .forEach(c -> routeGraph.removeCaravan(c.getId()));
    }

    private TradeGood.Category parseCategory(String name) {
        try {
            return TradeGood.Category.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}