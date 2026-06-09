package com.viamercatorum.core.simulation;

import com.viamercatorum.core.economy.TradeGood;
import com.viamercatorum.core.economy.TradeGoodRegistry;
import com.viamercatorum.core.economy.Supply;
import com.viamercatorum.core.world.RouteGraph;
import com.viamercatorum.core.world.TradeRoute;
import com.viamercatorum.core.world.VillageNode;

import java.util.*;

public class CharacterEconomyTest {
    public static void main(String[] args) {
        System.out.println("=== Character Economy Integration Test ===");
        System.out.println();

        TradeGoodRegistry.getAll();
        RouteGraph routeGraph = new RouteGraph();
        Random rng = new Random(42);

        VillageNode.VillageType[] types = VillageNode.VillageType.values();
        String[] biomes = {"minecraft:plains", "minecraft:desert", "minecraft:savanna", "minecraft:taiga", "minecraft:snowy", "minecraft:jungle", "minecraft:swamp"};
        List<VillageNode> villages = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            String biome = biomes[rng.nextInt(biomes.length)];
            VillageNode.VillageType type = types[rng.nextInt(types.length)];
            int cx = rng.nextInt(200) - 100;
            int cz = rng.nextInt(200) - 100;
            String id = "village_" + cx + "_" + cz;

            VillageNode village = new VillageNode(id, "Village_" + i, type, cx, cz, biome);
            double pop = switch (type) {
                case HAMLET -> 5;
                case VILLAGE -> 15;
                case TOWN -> 25;
                case CITY -> 50;
                case FORTIFIED -> 20;
            };
            village.setPopulation(pop);

            TradeGood[] goods = {TradeGoodRegistry.WHEAT, TradeGoodRegistry.IRON_INGOT, TradeGoodRegistry.COAL, TradeGoodRegistry.FISH, TradeGoodRegistry.BREAD};
            for (TradeGood good : goods) {
                double prodRate = 2.0 + rng.nextDouble() * 10.0;
                double maxCap = 50.0 + rng.nextDouble() * 400.0;
                village.addSupply(good, 50 + rng.nextDouble() * 150, prodRate, maxCap);
            }
            if (i < 8) village.setCoastal(true);

            routeGraph.addVillage(village);
            villages.add(village);
        }

        for (int i = 0; i < villages.size(); i++) {
            for (int j = i + 1; j < villages.size(); j++) {
                double dist = Math.sqrt(Math.pow(villages.get(i).getChunkX() - villages.get(j).getChunkX(), 2)
                    + Math.pow(villages.get(i).getChunkZ() - villages.get(j).getChunkZ(), 2));
                if (dist < 30 && rng.nextDouble() < 0.3) {
                    String routeId = "route_" + i + "_" + j;
                    TradeRoute route = new TradeRoute(routeId, villages.get(i), villages.get(j));
                    routeGraph.addRoute(route);
                }
            }
        }

        CharacterManager charManager = new CharacterManager();

        for (VillageNode village : villages) {
            charManager.ensureVillageCharacters(village);

            String leaderId = charManager.getLeaderOfVillage(village.getId());
            if (leaderId != null) {
                Character leader = charManager.getCharacter(leaderId);
                if (leader != null) {
                    double wealthBase = switch (village.getType()) {
                        case HAMLET -> 50.0;
                        case VILLAGE -> 200.0;
                        case TOWN -> 500.0;
                        case CITY -> 1000.0;
                        case FORTIFIED -> 300.0;
                    };
                    leader.setWealth(wealthBase * (1.0 + rng.nextDouble() * 2.0));
                    leader.recalculatePower(charManager.getTypeRegistry());
                }
            }

            for (Character merchant : charManager.getMerchantsOfVillage(village.getId())) {
                merchant.setWealth(30.0 + rng.nextDouble() * 120.0);
                String[] goodIds = {"wheat", "iron_ingot", "coal", "fish", "bread", "leather", "wood"};
                for (int g = 0; g < 2 + rng.nextInt(3); g++) {
                    String goodId = goodIds[rng.nextInt(goodIds.length)];
                    double amount = 5.0 + rng.nextDouble() * 30.0;
                    merchant.addGoods(goodId, amount);
                }
                merchant.recalculatePower(charManager.getTypeRegistry());
            }
        }

        System.out.println("=== Running CharacterAI for 200 ticks ===");
        CharacterAI characterAI = new CharacterAI();

        for (int t = 0; t < 200; t++) {
            for (VillageNode village : villages) {
                double harvestMod = 1.0;
                village.tickProduction(harvestMod);
                double consumptionRate = village.getPopulation() * 0.01;
                for (Supply supply : village.getSupplies().values()) {
                    supply.consume(consumptionRate);
                }
            }

            if (t % 100 == 0) {
                characterAI.tickCharacters(routeGraph);
            }
        }

        System.out.println();
        System.out.println("=== After 200 ticks with CharacterAI ===");
        System.out.println();

        List<Character> sorted = new ArrayList<>(charManager.getAllCharacters());
        sorted.sort((a, b) -> Double.compare(b.getWealth(), a.getWealth()));

        System.out.println("Top 3 Richest Characters:");
        for (int i = 0; i < Math.min(3, sorted.size()); i++) {
            Character c = sorted.get(i);
            CharacterTypeRegistry.CharacterType type = charManager.getTypeRegistry().getType(c.getCharacterTypeId());
            String typeName = type != null ? type.name() : "?";
            VillageNode home = routeGraph.getVillage(c.getHomeVillageId());

            System.out.println("  #" + (i + 1) + ": " + c.getName() + " [" + typeName + "]");
            System.out.println("       Role: " + c.getRole());
            if (c.getLeadershipTitle() != null) System.out.println("       Title: " + c.getLeadershipTitle());
            System.out.println("       Wealth: " + String.format("%.1f", c.getWealth()) + " emeralds");
            System.out.println("       Power Score: " + String.format("%.1f", c.getPowerScore()));
            System.out.println("       Reputation: " + String.format("%.1f", c.getReputation()));
            System.out.println("       Home: " + (home != null ? home.getName() + " (" + home.getType() + ")" : c.getHomeVillageId()));
            if (!c.getInventory().isEmpty()) {
                System.out.println("       Inventory:");
                for (var entry : c.getInventory().entrySet()) {
                    System.out.println("         - " + entry.getKey() + ": " + String.format("%.1f", entry.getValue()));
                }
            } else {
                System.out.println("       Inventory: (empty)");
            }
            if (!c.getOwnedWorkshopIds().isEmpty()) {
                System.out.println("       Owned Workshops: " + c.getOwnedWorkshopIds());
            }
            System.out.println();
        }

        long leadersAlive = sorted.stream().filter(Character::isLeader).count();
        long merchantsAlive = sorted.stream().filter(Character::isMerchant).count();
        System.out.println("=== Summary ===");
        System.out.println("  Total Characters: " + sorted.size());
        System.out.println("  Leaders: " + leadersAlive);
        System.out.println("  Merchants: " + merchantsAlive);
        System.out.println("  Avg Wealth: " + String.format("%.1f", sorted.stream().mapToDouble(Character::getWealth).average().orElse(0)));
        System.out.println("  Total Wealth: " + String.format("%.1f", sorted.stream().mapToDouble(Character::getWealth).sum()) + " emeralds");
        System.out.println("  Characters with Inventory: " + sorted.stream().filter(c -> !c.getInventory().isEmpty()).count());
        System.out.println("  Characters with Workshops: " + sorted.stream().filter(c -> !c.getOwnedWorkshopIds().isEmpty()).count());

        System.out.println();
        System.out.println("=== Village Economy Snapshot ===");
        for (VillageNode v : villages.stream().sorted((a, b) -> Double.compare(
            com.viamercatorum.core.economy.EconomyHelper.gdp(b),
            com.viamercatorum.core.economy.EconomyHelper.gdp(a)
        )).limit(5).toList()) {
            double gdp = com.viamercatorum.core.economy.EconomyHelper.gdp(v);
            System.out.println("  " + v.getName() + " (" + v.getType() + "): GDP=" + String.format("%.1f", gdp)
                + " Pop=" + String.format("%.0f", v.getPopulation())
                + " Food=" + String.format("%.2f", v.getFoodRatio())
                + " Rep=" + String.format("%.1f", v.getReputation()));

            var merchants = charManager.getMerchantsOfVillage(v.getId());
            if (!merchants.isEmpty()) {
                double avgWealth = merchants.stream().mapToDouble(Character::getWealth).average().orElse(0);
                System.out.println("    Merchants: " + merchants.size() + " avg wealth: " + String.format("%.1f", avgWealth));
            }
        }
    }
}