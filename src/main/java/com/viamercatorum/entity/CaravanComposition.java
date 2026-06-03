package com.viamercatorum.entity;

import com.viamercatorum.core.world.VillageNode;

import java.util.*;

public class CaravanComposition {
    public record Composition(
        int villagerCount,
        int guardCount,
        int swordGuardCount,
        int bowGuardCount,
        Map<String, Integer> mounts
    ) {
        public int totalEntities() {
            return villagerCount + guardCount + mounts.values().stream().mapToInt(i -> i).sum();
        }
    }

    public Composition determine(VillageNode origin) {
        String biome = origin.getBiome();
        VillageNode.VillageType type = origin.getType();

        int baseSize = switch (type) {
            case HAMLET -> 2;
            case VILLAGE -> 4;
            case TOWN -> 8;
            case CITY -> 15;
            case FORTIFIED -> 10;
        };

        int guardRatio = switch (type) {
            case FORTIFIED -> 0;
            case CITY -> 2;
            case TOWN -> 3;
            case VILLAGE -> 4;
            case HAMLET -> 5;
        };

        Random rng = new Random(biome.hashCode() ^ type.hashCode());
        int variation = rng.nextInt(3) - 1;

        int villagers = Math.max(1, baseSize + variation);
        int guards = Math.max(0, baseSize / guardRatio + variation);
        int swordGuards = guards / 2 + (guards % 2);
        int bowGuards = guards / 2;

        Map<String, Integer> mounts = getMountsForBiome(biome, baseSize, rng);

        return new Composition(villagers, guards, swordGuards, bowGuards, mounts);
    }

    private Map<String, Integer> getMountsForBiome(String biome, int baseSize, Random rng) {
        Map<String, Integer> mounts = new LinkedHashMap<>();
        int totalMounts = Math.max(1, baseSize / 3 + (rng.nextBoolean() ? 1 : 0));

        return switch (biome) {
            case "minecraft:desert" -> {
                int camels = totalMounts * 70 / 100;
                int donkeys = totalMounts - camels;
                if (camels > 0) mounts.put("minecraft:camel", camels);
                if (donkeys > 0) mounts.put("minecraft:donkey", donkeys);
                yield mounts;
            }
            case "minecraft:savanna" -> {
                int horses = totalMounts * 50 / 100;
                int donkeys = totalMounts - horses;
                if (horses > 0) mounts.put("minecraft:horse", horses);
                if (donkeys > 0) mounts.put("minecraft:donkey", donkeys);
                yield mounts;
            }
            case "minecraft:plains" -> {
                int horses = totalMounts * 40 / 100;
                int mules = totalMounts * 30 / 100;
                int donkeys = totalMounts - horses - mules;
                if (horses > 0) mounts.put("minecraft:horse", horses);
                if (mules > 0) mounts.put("minecraft:mule", mules);
                if (donkeys > 0) mounts.put("minecraft:donkey", donkeys);
                yield mounts;
            }
            case "minecraft:taiga" -> {
                int horses = totalMounts * 60 / 100;
                int donkeys = totalMounts - horses;
                if (horses > 0) mounts.put("minecraft:horse", horses);
                if (donkeys > 0) mounts.put("minecraft:donkey", donkeys);
                yield mounts;
            }
            case "minecraft:snowy" -> {
                int horses = totalMounts * 50 / 100;
                int llamas = totalMounts * 30 / 100;
                int mules = totalMounts - horses - llamas;
                if (horses > 0) mounts.put("minecraft:horse", horses);
                if (llamas > 0) mounts.put("minecraft:llama", llamas);
                if (mules > 0) mounts.put("minecraft:mule", mules);
                yield mounts;
            }
            case "minecraft:jungle" -> {
                int llamas = totalMounts * 60 / 100;
                int donkeys = totalMounts - llamas;
                if (llamas > 0) mounts.put("minecraft:llama", llamas);
                if (donkeys > 0) mounts.put("minecraft:donkey", donkeys);
                yield mounts;
            }
            case "minecraft:swamp" -> {
                int donkeys = totalMounts * 60 / 100;
                int llamas = totalMounts - donkeys;
                if (donkeys > 0) mounts.put("minecraft:donkey", donkeys);
                if (llamas > 0) mounts.put("minecraft:llama", llamas);
                yield mounts;
            }
            default -> {
                mounts.put("minecraft:horse", totalMounts);
                yield mounts;
            }
        };
    }
}
