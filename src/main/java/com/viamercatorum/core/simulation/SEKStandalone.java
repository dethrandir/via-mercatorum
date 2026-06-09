package com.viamercatorum.core.simulation;

import com.viamercatorum.core.config.ViaMercatorumConfig;
import com.viamercatorum.core.economy.Market;
import com.viamercatorum.core.economy.MarketRegionManager;
import com.viamercatorum.core.economy.TradeGood;
import com.viamercatorum.core.economy.TradeGoodRegistry;
import com.viamercatorum.core.economy.Supply;
import com.viamercatorum.core.economy.EconomyHelper;
import com.viamercatorum.core.world.RouteGraph;
import com.viamercatorum.core.world.TradeRoute;
import com.viamercatorum.core.world.VillageNode;
import com.viamercatorum.world.spawn.RaidEventCache;

import java.util.*;

public class SEKStandalone {
    private static final int NUM_VILLAGES = 120;
    private static final int NUM_SEA_VILLAGES = 25;
    private static final long SIMULATION_TICKS = 5000;
    private static final Random RANDOM = new Random(42);

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║   Via Mercatorum — SEK Standalone Simulation  ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        System.out.println();

        TradeGoodRegistry.getAll();
        ViaMercatorumConfig config = ViaMercatorumConfig.load(java.nio.file.Path.of("."));
        RouteGraph routeGraph = new RouteGraph();
        Market market = new Market();

        String[] biomes = {"minecraft:plains", "minecraft:desert", "minecraft:savanna", "minecraft:taiga", "minecraft:snowy", "minecraft:jungle", "minecraft:swamp"};
        VillageNode.VillageType[] types = VillageNode.VillageType.values();

        List<VillageNode> villages = new ArrayList<>();
        List<VillageNode> coastalVillages = new ArrayList<>();

        for (int i = 0; i < NUM_VILLAGES; i++) {
            String biome = biomes[RANDOM.nextInt(biomes.length)];
            VillageNode.VillageType type = types[RANDOM.nextInt(types.length)];
            int cx = RANDOM.nextInt(200) - 100;
            int cz = RANDOM.nextInt(200) - 100;
            String id = "village_" + cx + "_" + cz;
            String name = "Village_" + i;

            VillageNode village = new VillageNode(id, name, type, cx, cz, biome);

            TradeGood[] goods = {TradeGoodRegistry.WHEAT, TradeGoodRegistry.IRON_INGOT, TradeGoodRegistry.COAL, TradeGoodRegistry.FISH, TradeGoodRegistry.BREAD};
            double pop = type == VillageNode.VillageType.CITY ? 50 : type == VillageNode.VillageType.TOWN ? 25 : type == VillageNode.VillageType.VILLAGE ? 15 : 5;
            village.setPopulation(pop);

            for (TradeGood good : goods) {
                double prodRate = 2.0 + RANDOM.nextDouble() * 10.0;
                double maxCap = 50.0 + RANDOM.nextDouble() * 400.0;
                village.addSupply(good, 20 + RANDOM.nextDouble() * 80, prodRate, maxCap);
            }

            routeGraph.addVillage(village);
            villages.add(village);

            if (i < NUM_SEA_VILLAGES) {
                village.setCoastal(true);
                coastalVillages.add(village);
            }
        }

        for (int i = 0; i < villages.size(); i++) {
            for (int j = i + 1; j < villages.size(); j++) {
                double dist = Math.sqrt(Math.pow(villages.get(i).getChunkX() - villages.get(j).getChunkX(), 2)
                    + Math.pow(villages.get(i).getChunkZ() - villages.get(j).getChunkZ(), 2));
                if (dist < 30 && RANDOM.nextDouble() < 0.3) {
                    String routeId = "route_" + i + "_" + j;
                    TradeRoute route = new TradeRoute(routeId, villages.get(i), villages.get(j));
                    route.applyBiomePiracyModifier();
                    routeGraph.addRoute(route);
                }
            }
        }

        for (int i = 0; i < coastalVillages.size(); i++) {
            for (int j = i + 1; j < coastalVillages.size(); j++) {
                if (RANDOM.nextDouble() < 0.4) {
                    String routeId = "sea_" + i + "_" + j;
                    TradeRoute seaRoute = new TradeRoute(routeId, coastalVillages.get(i), coastalVillages.get(j), TradeRoute.RouteType.SEA);
                    routeGraph.addRoute(seaRoute);
                }
            }
        }

        RaidEventCache raidCache = new RaidEventCache();
        SimulationTick simTick = new SimulationTick(routeGraph, market, raidCache);
        SeasonManager seasonManager = simTick.getSeasonManager();
        ActiveModifierManager modifierManager = simTick.getModifierManager();

        MarketRegionManager regionManager = new MarketRegionManager();
        regionManager.assignVillagesToRegions(routeGraph.getAllVillages().stream()
            .collect(java.util.stream.Collectors.toMap(VillageNode::getId, v -> v, (a, b) -> a, java.util.LinkedHashMap::new)));

        System.out.println("Simulation Parameters:");
        System.out.println("  Villages: " + villages.size());
        System.out.println("  Coastal Villages: " + coastalVillages.size());
        System.out.println("  Land Routes: " + routeGraph.getAllRoutes().stream().filter(r -> !r.isSeaRoute()).count());
        System.out.println("  Sea Routes: " + routeGraph.getAllRoutes().stream().filter(TradeRoute::isSeaRoute).count());
        System.out.println("  Market Regions: " + regionManager.getAllRegions().size());
        System.out.println("  Season: " + seasonManager.getCurrentSeason());
        System.out.println("  Active Modifiers: " + modifierManager.size());
        System.out.println("  Simulation Ticks: " + SIMULATION_TICKS);
        System.out.println();

        simTick.start();

        for (long t = 0; t < SIMULATION_TICKS; t++) {
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                break;
            }

            if (t % 500 == 0) {
                printStatus(simTick, routeGraph, seasonManager, regionManager, modifierManager, t);
            }
        }

        simTick.stop();

        System.out.println();
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║            Final Summary                       ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        printStatus(simTick, routeGraph, seasonManager, regionManager, modifierManager, SIMULATION_TICKS);

        VillageNode richest = null;
        VillageNode poorest = null;
        double maxGdp = Double.MIN_VALUE;
        double minGdp = Double.MAX_VALUE;
        double maxPiracy = 0;
        String mostPiratedArea = "N/A";
        double lowestFoodRatio = Double.MAX_VALUE;
        String famineVillage = "N/A";

        for (VillageNode v : routeGraph.getAllVillages()) {
            double gdp = EconomyHelper.gdp(v);
            if (gdp > maxGdp) { maxGdp = gdp; richest = v; }
            if (gdp < minGdp) { minGdp = gdp; poorest = v; }
            double foodRatio = v.getFoodRatio();
            if (foodRatio < lowestFoodRatio) {
                lowestFoodRatio = foodRatio;
                famineVillage = v.getName();
            }
        }

        for (TradeRoute r : routeGraph.getAllRoutes()) {
            if (r.getPiracyScore() > maxPiracy) {
                maxPiracy = r.getPiracyScore();
                mostPiratedArea = r.getFrom().getName();
            }
        }

        System.out.println();
        System.out.println("  Richest Village: " + (richest != null ? richest.getName() + " (GDP: " + String.format("%.1f", maxGdp) + ")" : "N/A"));
        System.out.println("  Poorest Village: " + (poorest != null ? poorest.getName() + " (GDP: " + String.format("%.1f", minGdp) + ")" : "N/A"));
        System.out.println("  Most Pirated Area: " + mostPiratedArea + " (Piracy: " + String.format("%.3f", maxPiracy) + ")");
        System.out.println("  Hungriest Village: " + famineVillage + " (Food Ratio: " + String.format("%.2f", lowestFoodRatio) + ")");
        System.out.println("  Active Caravans: " + routeGraph.getAllCaravans().size());
        System.out.println("  Active Modifiers: " + modifierManager.size());
        System.out.println("  Regions: " + regionManager.getAllRegions().size());

        System.out.println();
        CharacterManager charManager = new CharacterManager();
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║         Character Wealth Report               ║");
        System.out.println("╚════════════════════════════════════════════════╝");

        for (VillageNode village : villages) {
            charManager.ensureVillageCharacters(village);
        }

        List<Character> allChars = new ArrayList<>(charManager.getAllCharacters());
        allChars.sort((a, b) -> Double.compare(b.getWealth(), a.getWealth()));

        System.out.println("  Total Characters: " + allChars.size());
        System.out.println("  Leaders: " + charManager.getCharactersByRole(CharacterRole.LEADER).size());
        System.out.println("  Merchants: " + charManager.getCharactersByRole(CharacterRole.MERCHANT).size());
        System.out.println();

        for (int i = 0; i < Math.min(3, allChars.size()); i++) {
            Character c = allChars.get(i);
            CharacterTypeRegistry.CharacterType type = charManager.getTypeRegistry().getType(c.getCharacterTypeId());
            String typeName = type != null ? type.name() : "?";
            System.out.println("  #" + (i + 1) + " " + c.getName() + " [" + typeName + "] (" + c.getRole() + ")");
            System.out.println("       Wealth: " + String.format("%.1f", c.getWealth()) + " emeralds");
            System.out.println("       Power Score: " + String.format("%.1f", c.getPowerScore()));
            System.out.println("       Reputation: " + String.format("%.1f", c.getReputation()));
            System.out.println("       Home: " + c.getHomeVillageId());
            if (c.isLeader()) System.out.println("       Title: " + c.getLeadershipTitle());
            if (!c.getInventory().isEmpty()) {
                System.out.println("       Inventory:");
                for (var entry : c.getInventory().entrySet()) {
                    System.out.println("         " + entry.getKey() + ": " + String.format("%.1f", entry.getValue()));
                }
            } else {
                System.out.println("       Inventory: (empty)");
            }
            System.out.println();
        }
    }

    private static void printStatus(SimulationTick tick, RouteGraph graph, SeasonManager sm, MarketRegionManager rm, ActiveModifierManager mm, long t) {
        double avgFoodRatio = graph.getAllVillages().stream()
            .mapToDouble(VillageNode::getFoodRatio)
            .average().orElse(0);

        System.out.printf("[Tick %5d] Season: %-7s | Villages: %d | Caravans: %d | Routes: %d (Sea: %d) | Regions: %d | FoodRatio: %.2f | Modifiers: %d%n",
            t, sm.getCurrentSeason(),
            graph.getAllVillages().size(),
            graph.getAllCaravans().size(),
            graph.getAllRoutes().size(),
            graph.getAllRoutes().stream().filter(TradeRoute::isSeaRoute).count(),
            rm.getAllRegions().size(),
            avgFoodRatio,
            mm.size());
    }
}