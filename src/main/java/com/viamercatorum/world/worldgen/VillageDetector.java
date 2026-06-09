package com.viamercatorum.world.worldgen;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.block.VillageHallBlockEntity;
import com.viamercatorum.block.ModBlocks;
import com.viamercatorum.core.economy.TradeGood;
import com.viamercatorum.core.economy.TradeGoodRegistry;
import com.viamercatorum.core.world.RouteGraph;
import com.viamercatorum.core.world.VillageNode;
import com.viamercatorum.data.VillageProfile;
import com.viamercatorum.data.VillageProfileLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.*;

public class VillageDetector {
    private final Random random = new Random();

    public void onStructureStart(ServerLevel world, StructureStart start) {
        Structure structure = start.getStructure();

        String structureKeyStr = structure.type().toString();

        String path = structureKeyStr;
        if (structureKeyStr.contains("/")) {
            path = structureKeyStr.substring(structureKeyStr.indexOf("/") + 1);
        }
        if (!path.contains("village")) return;

        int pieceCount = start.getPieces().size();
        BlockPos center = start.getBoundingBox().getCenter();

        int chunkX = center.getX() >> 4;
        int chunkZ = center.getZ() >> 4;

        Holder<Biome> biomeHolder = world.getBiome(center);
        Optional<ResourceKey<Biome>> biomeKey = biomeHolder.unwrapKey();
        Identifier biomeId = biomeKey.map(ResourceKey::identifier)
            .orElse(Identifier.fromNamespaceAndPath("minecraft", "plains"));

        VillageNode.VillageType type = determineType(pieceCount);
        String name = generateVillageName(path, type);

        VillageNode node = new VillageNode(
            "village_" + chunkX + "_" + chunkZ,
            name, type, chunkX, chunkZ, biomeId.toString()
        );

        VillageProfileLoader loader = ViaMercatorum.getInstance().getVillageProfileLoader();
        VillageProfile profile = loader.getProfileForBiome(biomeId.toString());

        if (profile == null) {
            profile = loader.getProfileForBiome("minecraft:plains");
        }

        if (profile != null) {
            applyProfile(node, profile);
        } else {
            applyDefaultGoods(node, biomeId);
        }

        RouteGraph graph = ViaMercatorum.getInstance().getRouteGraph();

        String nodeId = "village_" + chunkX + "_" + chunkZ;
        if (graph.getVillage(nodeId) != null) return;

        graph.addVillage(node);

        connectNearbyVillages(node, graph);

        assignVillageHall(world, center, nodeId);
    }

    private void assignVillageHall(ServerLevel world, BlockPos center, String villageId) {
        int searchRadius = 32;
        for (int dx = -searchRadius; dx <= searchRadius; dx += 3) {
            for (int dz = -searchRadius; dz <= searchRadius; dz += 3) {
                for (int dy = -8; dy <= 8; dy += 2) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (world.getBlockEntity(pos) instanceof VillageHallBlockEntity be) {
                        be.setVillageId(villageId);
                        be.setChanged();
                        return;
                    }
                }
            }
        }
    }

    private VillageNode.VillageType determineType(int pieceCount) {
        if (pieceCount < 5) return VillageNode.VillageType.HAMLET;
        if (pieceCount < 10) return VillageNode.VillageType.VILLAGE;
        if (pieceCount < 20) return VillageNode.VillageType.TOWN;
        return VillageNode.VillageType.CITY;
    }

    private void applyProfile(VillageNode node, VillageProfile profile) {
        node.setPopulation(profile.populationBase());

        for (VillageProfile.ProductionEntry spec : profile.specializations()) {
            TradeGood good = TradeGoodRegistry.get(spec.goodId());
            if (good != null) {
                node.addSupply(good, spec.maxCapacity() * 0.5, spec.productionRate(), spec.maxCapacity());
            }
        }

        for (VillageProfile.BaseProductionEntry base : profile.baseProduction()) {
            TradeGood good = TradeGoodRegistry.get(base.goodId());
            if (good != null && node.getSupply(base.goodId()) == null) {
                node.addSupply(good, base.maxCapacity() * 0.3, base.productionRate(), base.maxCapacity());
            }
        }
    }

    private void applyDefaultGoods(VillageNode node, Identifier biomeId) {
        String biome = biomeId.getPath();
        Map<String, Double> specGoods = getBiomeSpecialization(biome);

        for (var entry : specGoods.entrySet()) {
            TradeGood good = TradeGoodRegistry.get(entry.getKey());
            if (good != null) {
                double rate = entry.getValue();
                double maxCap = rate * 40;
                node.addSupply(good, maxCap * 0.5, rate, maxCap);
            }
        }

        addCommonGoods(node);
    }

    private void addCommonGoods(VillageNode node) {
        Map<String, Double> commons = Map.of(
            "stone", 1.0,
            "oak_log", 1.5,
            "bread", 2.0,
            "coal", 0.5,
            "iron_ingot", 0.3
        );

        for (var entry : commons.entrySet()) {
            TradeGood good = TradeGoodRegistry.get(entry.getKey());
            if (good != null && node.getSupply(entry.getKey()) == null) {
                double rate = entry.getValue();
                node.addSupply(good, rate * 20, rate, rate * 20);
            }
        }
    }

    private void connectNearbyVillages(VillageNode node, RouteGraph graph) {
        double maxConnectionDistance = 80;

        for (VillageNode other : graph.getAllVillages()) {
            if (other.getId().equals(node.getId())) continue;

            double dx = node.getChunkX() - other.getChunkX();
            double dz = node.getChunkZ() - other.getChunkZ();
            double dist = Math.sqrt(dx * dx + dz * dz);

            if (dist < maxConnectionDistance) {
                String routeId = "route_" + node.getId() + "_" + other.getId();
                String reverseId = "route_" + other.getId() + "_" + node.getId();
                if (graph.getRoute(routeId) != null || graph.getRoute(reverseId) != null) continue;

                ViaMercatorum.getInstance().getVillageModifier()
                    .establishTradeRoute(node, other, graph);
            }
        }
    }

    private String generateVillageName(String biomePath, VillageNode.VillageType type) {
        String[] prefixes = switch (type) {
            case HAMLET -> new String[]{"Little", "Small", "North", "Old"};
            case VILLAGE -> new String[]{"New", "East", "West", "Port", "South"};
            case TOWN -> new String[]{"Great", "Upper", "Lower", "Fort", "Market"};
            case CITY -> new String[]{"Grand", "High", "Central", "Royal", "Free"};
            case FORTIFIED -> new String[]{"Fort", "Castle", "Keep", "Stronghold", "Garrison"};
        };

        String[] suffixes = switch (biomePath) {
            case "plains" -> new String[]{"field", "meadow", "croft", "dale"};
            case "desert" -> new String[]{"oasis", "dune", "sands", "mirage"};
            case "savanna" -> new String[]{"plain", "savanna", "acacia", "safari"};
            case "taiga" -> new String[]{"pines", "taiga", "fir", "spruce"};
            case "snowy" -> new String[]{"frost", "peak", "tundra", "ice"};
            case "jungle" -> new String[]{"canopy", "vines", "temple", "grove"};
            case "swamp" -> new String[]{"mire", "bog", "marsh", "fen"};
            default -> new String[]{"hollow", "haven", "burg", "shire"};
        };

        String p = prefixes[random.nextInt(prefixes.length)];
        String s = suffixes[random.nextInt(suffixes.length)];
        return p + " " + capitalize(s);
    }

    private String capitalize(String s) {
        if (s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private Map<String, Double> getBiomeSpecialization(String biome) {
        return switch (biome) {
            case "plains" -> Map.of("wheat", 8.0, "carrot", 5.0, "potato", 5.0);
            case "desert" -> Map.of("gold_ingot", 2.0, "lapis", 3.0);
            case "savanna" -> Map.of("leather", 4.0, "beef", 3.0, "acacia_log", 5.0);
            case "taiga" -> Map.of("spruce_log", 10.0, "coal", 4.0, "fish", 3.0);
            case "snowy" -> Map.of("iron_ingot", 3.0, "coal", 5.0, "fish", 2.0);
            case "jungle" -> Map.of("melon", 6.0, "cocoa", 4.0);
            case "swamp" -> Map.of("redstone", 3.0, "clay", 4.0, "fish", 2.0);
            default -> Map.of("wheat", 3.0, "stone", 2.0);
        };
    }
}
