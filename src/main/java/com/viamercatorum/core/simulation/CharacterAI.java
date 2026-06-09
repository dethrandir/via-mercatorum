package com.viamercatorum.core.simulation;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.config.ViaMercatorumConfig;
import com.viamercatorum.core.economy.EconomyHelper;
import com.viamercatorum.core.economy.PriceCalculator;
import com.viamercatorum.core.economy.Supply;
import com.viamercatorum.core.economy.TradeGood;
import com.viamercatorum.core.economy.TradeGoodRegistry;
import com.viamercatorum.core.world.MilitaryPower;
import com.viamercatorum.core.world.RouteGraph;
import com.viamercatorum.core.world.TradeRoute;
import com.viamercatorum.core.world.VillageNode;
import com.viamercatorum.core.world.WorkshopInstance;
import com.viamercatorum.core.world.WorkshopManager;
import com.viamercatorum.data.WorkshopDefinition;
import com.viamercatorum.data.WorkshopDefinitionLoader;

import java.util.*;

public class CharacterAI {

    private final Random random = new Random();

    public void tickCharacters(RouteGraph routeGraph) {
        ViaMercatorum vm = ViaMercatorum.getInstance();
        CharacterManager charManager = vm.getCharacterManager();
        if (charManager == null) return;
        ViaMercatorumConfig config = vm.getConfig();

        for (Character character : charManager.getAllCharacters()) {
            if (!character.isAlive()) continue;

            switch (character.getRole()) {
                case LEADER -> tickLeader(character, routeGraph, config, vm);
                case MERCHANT -> tickMerchant(character, routeGraph, config, vm);
                case CARAVAN_LEADER -> tickCaravanLeader(character, routeGraph, config, vm);
            }
        }
    }

    private void tickLeader(Character leader, RouteGraph routeGraph, ViaMercatorumConfig config, ViaMercatorum vm) {
        String villageId = leader.getGovernedVillageId();
        if (villageId == null) villageId = leader.getHomeVillageId();
        VillageNode village = routeGraph.getVillage(villageId);
        if (village == null) return;

        leader.addWealth(config.economy.leaderPassiveIncome * 0.01);

        if (random.nextDouble() < 0.002) {
            double foodRatio = village.getFoodRatio();
            if (foodRatio < 0.3 && leader.getWealth() > 50) {
                double investment = Math.min(leader.getWealth() * 0.1, 30);
                leader.addWealth(-investment);
                for (Supply supply : village.getSupplies().values()) {
                    if (supply.getGood().category() == TradeGood.Category.FOOD && supply.getStock() < supply.getMaxCapacity() * 0.5) {
                        double bought = Math.min(investment / supply.getGood().basePrice(), supply.getMaxCapacity() * 0.2);
                        supply.addStock(bought);
                        investment -= bought * supply.getGood().basePrice();
                        if (investment <= 0) break;
                    }
                }
            }
        }

        if (random.nextDouble() < 0.001) {
            double surplusGoods = 0;
            String surplusGoodId = null;
            double maxSurplus = 0;
            for (Supply supply : village.getSupplies().values()) {
                double ratio = supply.getMaxCapacity() > 0 ? supply.getStock() / supply.getMaxCapacity() : 0;
                if (ratio > 0.7 && supply.getGood().basePrice() > maxSurplus) {
                    maxSurplus = supply.getGood().basePrice();
                    surplusGoodId = supply.getGood().id();
                    surplusGoods = supply.getStock() * 0.05;
                }
            }
            if (surplusGoodId != null && surplusGoods > 0) {
                double revenue = surplusGoods * maxSurplus * 0.7;
                leader.addWealth(revenue);
                village.getSupply(surplusGoodId).consume(surplusGoods);
            }
        }

        if (random.nextDouble() < 0.005) {
            WorkshopManager wm = vm.getWorkshopManager();
            WorkshopDefinitionLoader wdl = vm.getWorkshopDefinitionLoader();
            if (wm != null && wdl != null) {
                long villageWorkshopCount = wm.getWorkshopsForVillage(villageId).size();
                int maxWorkshops = switch (village.getType()) {
                    case HAMLET -> 1;
                    case VILLAGE -> 2;
                    case TOWN -> 3;
                    case CITY -> 5;
                    case FORTIFIED -> 3;
                };
                if (villageWorkshopCount < maxWorkshops && leader.getWealth() > 200) {
                    String[] workshopTypes = {"carpenter", "bakery", "smithy"};
                    String workshopId = workshopTypes[random.nextInt(workshopTypes.length)];
                    WorkshopDefinition def = wdl.getDefinition(workshopId);
                    if (def != null && leader.getWealth() >= def.emeraldConstructionCost()) {
                        leader.addWealth(-def.emeraldConstructionCost());
                        WorkshopInstance inst = wm.createWorkshop(villageId, workshopId, leader.getId());
                        if (inst != null) {
                            leader.addOwnedWorkshop(inst.getInstanceId());
                        }
                    }
                }
            }
        }

        if (random.nextDouble() < 0.0005 && village.getMobilization() == MilitaryPower.MobilizationState.DEMILITARIZED) {
            double threatLevel = 0;
            for (TradeRoute route : routeGraph.getAllRoutes()) {
                if (route.getFrom().getId().equals(villageId) || route.getTo().getId().equals(villageId)) {
                    threatLevel += route.getPiracyScore();
                }
            }
            if (threatLevel > 2.0 && village.canMobilize(vm.getSimulationTick().getTickCounter())) {
                village.setMobilization(MilitaryPower.MobilizationState.STANDBY,
                    vm.getSimulationTick().getTickCounter());
            }
        }

        leader.recalculatePower(vm.getCharacterManager().getTypeRegistry());
    }

    private void tickMerchant(Character merchant, RouteGraph routeGraph, ViaMercatorumConfig config, ViaMercatorum vm) {
        String villageId = merchant.getHomeVillageId();
        VillageNode village = routeGraph.getVillage(villageId);
        if (village == null) return;

        merchant.addWealth(config.economy.merchantPassiveIncome * 0.01);

        if (random.nextDouble() < 0.01) {
            Map<String, Double> inventory = merchant.getInventory();
            List<String> toSell = new ArrayList<>();
            for (Map.Entry<String, Double> entry : inventory.entrySet()) {
                Supply villageSupply = village.getSupply(entry.getKey());
                if (villageSupply != null) {
                    double sellPrice = PriceCalculator.calculateSellPrice(
                        TradeGoodRegistry.get(entry.getKey()),
                        villageSupply.getStock(),
                        villageSupply.getMaxCapacity() * 0.5,
                        village.getReputation()
                    );
                    if (sellPrice > 0) {
                        double amount = Math.min(entry.getValue(), 5.0);
                        villageSupply.addStock(amount);
                        double revenue = amount * sellPrice;
                        merchant.addWealth(revenue);
                        merchant.removeGoods(entry.getKey(), amount);
                        break;
                    }
                }
            }
        }

        if (random.nextDouble() < 0.008) {
            String bestGoodId = null;
            double bestProfit = 0;

            for (Supply supply : village.getSupplies().values()) {
                double stockRatio = supply.getMaxCapacity() > 0 ? supply.getStock() / supply.getMaxCapacity() : 1;
                if (stockRatio > 0.6) {
                    double buyPrice = PriceCalculator.calculateBuyPrice(
                        supply.getGood(), supply.getStock(),
                        supply.getMaxCapacity() * 0.5, village.getReputation()
                    );
                    double otherVillageBestPrice = 0;
                    for (VillageNode other : routeGraph.getAllVillages()) {
                        if (other.getId().equals(villageId)) continue;
                        Supply otherSupply = other.getSupply(supply.getGood().id());
                        if (otherSupply != null) {
                            double otherPrice = PriceCalculator.calculateSellPrice(
                                supply.getGood(), otherSupply.getStock(),
                                otherSupply.getMaxCapacity() * 0.5, other.getReputation()
                            );
                            if (otherPrice > otherVillageBestPrice) {
                                otherVillageBestPrice = otherPrice;
                            }
                        }
                    }
                    double profit = otherVillageBestPrice - buyPrice;
                    if (profit > bestProfit && buyPrice < merchant.getWealth() * 0.3) {
                        bestProfit = profit;
                        bestGoodId = supply.getGood().id();
                    }
                }
            }

            if (bestGoodId != null && merchant.getWealth() > 10) {
                Supply supply = village.getSupply(bestGoodId);
                if (supply != null) {
                    double buyPrice = PriceCalculator.calculateBuyPrice(
                        supply.getGood(), supply.getStock(),
                        supply.getMaxCapacity() * 0.5, village.getReputation()
                    );
                    double amount = Math.min(5.0, merchant.getWealth() * 0.2 / Math.max(1, buyPrice));
                    double cost = amount * buyPrice;
                    if (cost <= merchant.getWealth() && amount <= supply.getStock()) {
                        supply.consume(amount);
                        merchant.addGoods(bestGoodId, amount);
                        merchant.addWealth(-cost);
                    }
                }
            }
        }

        if (random.nextDouble() < 0.005) {
            WorkshopManager wm = vm.getWorkshopManager();
            WorkshopDefinitionLoader wdl = vm.getWorkshopDefinitionLoader();
            if (wm != null && wdl != null && merchant.canOwnWorkshop()) {
                long ownedCount = merchant.getOwnedWorkshopIds().size();
                int maxOwned = 2;
                if (ownedCount < maxOwned && merchant.getWealth() > 150) {
                    String[] workshopTypes = {"carpenter", "bakery", "smithy"};
                    String workshopId = workshopTypes[random.nextInt(workshopTypes.length)];
                    WorkshopDefinition def = wdl.getDefinition(workshopId);
                    if (def != null && merchant.getWealth() >= def.emeraldConstructionCost()) {
                        merchant.addWealth(-def.emeraldConstructionCost());
                        WorkshopInstance inst = wm.createWorkshop(villageId, workshopId, merchant.getId());
                        if (inst != null) {
                            merchant.addOwnedWorkshop(inst.getInstanceId());
                            inst.setAutoBuyMaterials(true);
                            inst.setAutoSellOutput(true);
                        }
                    }
                }
            }
        }

        if (random.nextDouble() < 0.003) {
            for (String workshopId : new ArrayList<>(merchant.getOwnedWorkshopIds())) {
                WorkshopInstance inst = vm.getWorkshopManager().getInstance(workshopId);
                if (inst != null && inst.getLevel() < 3 && merchant.getWealth() > inst.getLevel() * 100) {
                    double upgradeCost = inst.getLevel() * 100.0;
                    merchant.addWealth(-upgradeCost);
                    vm.getWorkshopManager().upgradeWorkshop(workshopId);
                }
            }
        }

        if (random.nextDouble() < 0.002) {
            double revenueFromWorkshops = 0;
            WorkshopManager wm = vm.getWorkshopManager();
            if (wm != null) {
                for (String workshopId : merchant.getOwnedWorkshopIds()) {
                    WorkshopInstance inst = wm.getInstance(workshopId);
                    if (inst != null && inst.isAutoSellOutput() && !inst.getAccumulatedOutput().isEmpty()) {
                        VillageNode instVillage = routeGraph.getVillage(inst.getVillageId());
                        if (instVillage != null) {
                            for (Map.Entry<String, Double> output : inst.getAccumulatedOutput().entrySet()) {
                                double amount = output.getValue();
                                if (amount > 0) {
                                    TradeGood good = TradeGoodRegistry.get(output.getKey());
                                    if (good != null) {
                                        revenueFromWorkshops += amount * good.basePrice() * 0.8;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (revenueFromWorkshops > 0) {
                merchant.addWealth(revenueFromWorkshops * 0.1);
            }
        }

        merchant.recalculatePower(vm.getCharacterManager().getTypeRegistry());
    }

    private void tickCaravanLeader(Character caravanLeader, RouteGraph routeGraph, ViaMercatorumConfig config, ViaMercatorum vm) {
        if (caravanLeader.getCaravanId() == null) return;

        caravanLeader.addWealth(config.economy.caravanLeaderPassiveIncome * 0.01);

        com.viamercatorum.core.world.Caravan caravan = routeGraph.getCaravan(caravanLeader.getCaravanId());
        if (caravan == null) return;

        if (random.nextDouble() < 0.01) {
            VillageNode dest = caravan.getDestinationNode();
            double profitExpectation = 0;
            for (Map.Entry<String, Double> entry : caravan.getCargo().entrySet()) {
                Supply destSupply = dest.getSupply(entry.getKey());
                if (destSupply != null) {
                    double sellPrice = PriceCalculator.calculateSellPrice(
                        TradeGoodRegistry.get(entry.getKey()),
                        destSupply.getStock(),
                        destSupply.getMaxCapacity() * 0.5,
                        dest.getReputation()
                    );
                    profitExpectation += entry.getValue() * sellPrice;
                }
            }
            caravanLeader.addWealth(profitExpectation * 0.05);
        }

        caravanLeader.recalculatePower(vm.getCharacterManager().getTypeRegistry());
    }
}