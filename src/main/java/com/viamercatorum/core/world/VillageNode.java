package com.viamercatorum.core.world;

import com.viamercatorum.core.economy.Supply;
import com.viamercatorum.core.economy.TradeGood;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VillageNode {
    private final String id;
    private final String name;
    private VillageType type;
    private final int chunkX;
    private final int chunkZ;
    private final String biome;
    private final Map<String, Supply> supplies = new ConcurrentHashMap<>();
    private double population;
    private double reputation;

    public enum VillageType {
        HAMLET, VILLAGE, TOWN, CITY, FORTIFIED
    }

    public VillageNode(String id, String name, VillageType type, int chunkX, int chunkZ, String biome) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.biome = biome;
        this.population = 10;
        this.reputation = 0;
    }

    public void addSupply(TradeGood good, double stock, double prodRate, double maxCap) {
        supplies.put(good.id(), new Supply(good, stock, prodRate, maxCap));
    }

    public void tickProduction() {
        for (Supply supply : supplies.values()) {
            supply.tickProduction();
        }
    }

    public Supply getSupply(String goodId) {
        return supplies.get(goodId);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public VillageType getType() { return type; }
    public void setType(VillageType type) { this.type = type; }
    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }
    public String getBiome() { return biome; }
    public double getPopulation() { return population; }
    public void setPopulation(double population) { this.population = population; }
    public double getReputation() { return reputation; }
    public void setReputation(double reputation) { this.reputation = Math.max(-100, Math.min(100, reputation)); }
    public void addReputation(double amount) { this.reputation = Math.max(-100, Math.min(100, this.reputation + amount)); }
    public Map<String, Supply> getSupplies() { return supplies; }
}
