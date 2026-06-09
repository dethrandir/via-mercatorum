package com.viamercatorum.core.world;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.config.ViaMercatorumConfig;
import com.viamercatorum.core.economy.Supply;
import com.viamercatorum.core.economy.TradeGood;
import com.viamercatorum.core.economy.TradeGoodRegistry;
import com.viamercatorum.data.ConsumptionProfile;

import java.util.ArrayList;
import java.util.List;
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
    private final Map<String, Double> lastDemand = new ConcurrentHashMap<>();
    private double population;
    private double reputation;
    private boolean isCoastal;
    private String regionId;
    private boolean isRaided;
    private long raidedAtTick;
    private final List<String> workshopInstanceIds = new ArrayList<>();
    private MilitaryPower.MobilizationState mobilization = MilitaryPower.MobilizationState.DEMILITARIZED;
    private long mobilizedAtTick;
    private String activeGoalId;

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
        this.isCoastal = false;
        this.regionId = null;
    }

    public void addSupply(TradeGood good, double stock, double prodRate, double maxCap) {
        supplies.put(good.id(), new Supply(good, stock, prodRate, maxCap));
    }

    public void tickProduction() {
        for (Supply supply : supplies.values()) {
            supply.tickProduction();
        }
    }

    public void tickProduction(double harvestModifier) {
        for (Supply supply : supplies.values()) {
            if (supply.getGood().category() == TradeGood.Category.FOOD) {
                supply.tickProductionWithModifier(harvestModifier);
            } else {
                supply.tickProduction();
            }
        }
    }

    public void tickProduction(double harvestModifier, Map<String, Double> productionModifiers) {
        for (Supply supply : supplies.values()) {
            double modifier = harvestModifier;
            String goodId = supply.getGood().id();
            String categoryKey = supply.getGood().category().name().toLowerCase();

            if (supply.getGood().category() == TradeGood.Category.FOOD) {
                // harvest modifier applies to food
            } else {
                modifier = 1.0;
            }

            Double goodMod = productionModifiers.get(goodId);
            Double catMod = productionModifiers.get(categoryKey);
            if (goodMod != null) modifier *= goodMod;
            else if (catMod != null) modifier *= catMod;

            Double allMod = productionModifiers.get("*");
            if (allMod != null) modifier *= allMod;

            Double scaleMod = productionModifiers.get("_scale");
            if (scaleMod != null) modifier *= scaleMod;

            Double mobilizeMod = productionModifiers.get("_mobilize");
            if (mobilizeMod != null) modifier *= mobilizeMod;

            Double standbyMod = productionModifiers.get("_standby");
            if (standbyMod != null) modifier *= standbyMod;

            if (modifier == 1.0) {
                supply.tickProduction();
            } else {
                supply.tickProductionWithModifier(modifier);
            }
        }
    }

    public void tickConsumption(ConsumptionProfile profile, String seasonName) {
        if (profile == null) return;

        double popScale = profile.populationScale();
        double seasonMod = profile.seasonModifiers().getOrDefault(seasonName, 1.0);
        double consumptionMultiplier = popScale * seasonMod * Math.max(1.0, population / 10.0);

        for (ConsumptionProfile.ConsumptionEntry entry : profile.entries()) {
            double rate = entry.baseRate() * consumptionMultiplier;
            double consumed = 0;

            if (entry.goodId().isPresent()) {
                String goodId = entry.goodId().get();
                Supply supply = supplies.get(goodId);
                if (supply != null) {
                    consumed = Math.min(supply.getStock(), rate);
                    supply.consume(consumed);
                }
                lastDemand.put(goodId, rate);
            } else if (entry.category().isPresent()) {
                String categoryName = entry.category().get();
                for (Supply supply : supplies.values()) {
                    if (categoryMatches(supply.getGood().category(), categoryName)) {
                        double goodRate = rate / Math.max(1, countGoodsInCategory(categoryName));
                        double actual = Math.min(supply.getStock(), goodRate);
                        supply.consume(actual);
                        consumed += actual;
                        lastDemand.put(supply.getGood().id(), goodRate);
                    }
                }
            }
        }
    }

    private boolean categoryMatches(TradeGood.Category category, String categoryName) {
        return category.name().equalsIgnoreCase(categoryName);
    }

    private long countGoodsInCategory(String categoryName) {
        return supplies.values().stream()
            .filter(s -> categoryMatches(s.getGood().category(), categoryName))
            .count();
    }

    public double getFoodRatio() {
        double totalFoodStock = 0;
        double totalFoodCapacity = 0;
        for (Supply supply : supplies.values()) {
            if (supply.getGood().category() == TradeGood.Category.FOOD) {
                totalFoodStock += supply.getStock();
                totalFoodCapacity += supply.getMaxCapacity();
            }
        }
        if (totalFoodCapacity <= 0) return 1.0;
        return totalFoodStock / totalFoodCapacity;
    }

    public double getFoodConsumptionRate() {
        double total = 0;
        for (var entry : lastDemand.entrySet()) {
            TradeGood good = TradeGoodRegistry.get(entry.getKey());
            if (good != null && good.category() == TradeGood.Category.FOOD) {
                total += entry.getValue();
            }
        }
        return total;
    }

    public double getSupplyRatio(String goodId) {
        Supply supply = supplies.get(goodId);
        if (supply == null || supply.getMaxCapacity() <= 0) return 1.0;
        return supply.getStock() / supply.getMaxCapacity();
    }

    public double calculateProductionScale() {
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        if (!config.productionScale.enabled) return 1.0;

        double scale = 1.0;

        double foodRatio = getFoodRatio();
        if (foodRatio < config.productionScale.foodStarvationThreshold) {
            double penalty = config.productionScale.foodStarvationPenalty * (1.0 - foodRatio / config.productionScale.foodStarvationThreshold);
            scale -= penalty;
        } else if (foodRatio > config.productionScale.foodSatisfactionThreshold) {
            double bonus = (foodRatio - config.productionScale.foodSatisfactionThreshold) * config.productionScale.foodProductionBonus;
            scale += bonus;
        }

        for (String toolId : config.productionScale.toolGoods) {
            Supply supply = supplies.get(toolId);
            if (supply != null && supply.getStock() > 0) {
                scale += config.productionScale.toolBonus;
            }
        }

        for (String matId : config.productionScale.rawMaterialGoods) {
            Supply supply = supplies.get(matId);
            if (supply != null && supply.getMaxCapacity() > 0) {
                double ratio = supply.getStock() / supply.getMaxCapacity();
                if (ratio > 0.3) {
                    scale += config.productionScale.rawMaterialBonus;
                }
            }
        }

        return Math.max(config.productionScale.minScale, Math.min(config.productionScale.maxScale, scale));
    }

    public double getMaxPopulation() {
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        String key = type.name();
        return config.population.maxPopulationScale.getOrDefault(key, 50.0);
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
    public Map<String, Double> getLastDemand() { return lastDemand; }
    public boolean isCoastal() { return isCoastal; }
    public void setCoastal(boolean coastal) { this.isCoastal = coastal; }
    public String getRegionId() { return regionId; }
    public void setRegionId(String regionId) { this.regionId = regionId; }

    public boolean isRaided() { return isRaided; }
    public void setRaided(boolean raided, long tick) {
        this.isRaided = raided;
        if (raided) this.raidedAtTick = tick;
    }
    public boolean canResettle(long currentTick, long resettleTicks) {
        return isRaided && (currentTick - raidedAtTick) >= resettleTicks;
    }
    public void resettle() {
        this.isRaided = false;
        this.raidedAtTick = 0;
    }

    public void addWorkshop(String instanceId) {
        if (!workshopInstanceIds.contains(instanceId)) {
            workshopInstanceIds.add(instanceId);
        }
    }

    public void removeWorkshop(String instanceId) {
        workshopInstanceIds.remove(instanceId);
    }

    public List<String> getWorkshopInstanceIds() {
        return workshopInstanceIds;
    }

    public MilitaryPower.MobilizationState getMobilization() { return mobilization; }

    public void setMobilization(MilitaryPower.MobilizationState mobilization, long currentTick) {
        this.mobilization = mobilization;
        this.mobilizedAtTick = currentTick;
    }

    public long getMobilizedAtTick() { return mobilizedAtTick; }

    public boolean canMobilize(long currentTick) {
        if (mobilization == MilitaryPower.MobilizationState.DEMILITARIZED) return true;
        if (mobilization == MilitaryPower.MobilizationState.STANDBY) {
            ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
            return (currentTick - mobilizedAtTick) >= config.mobilization.standbyToMobilizeTicks;
        }
        return false;
    }

    public boolean canDemobilize(long currentTick) {
        if (mobilization == MilitaryPower.MobilizationState.MOBILIZED) {
            ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
            return (currentTick - mobilizedAtTick) >= config.mobilization.mobilizeDurationTicks;
        }
        if (mobilization == MilitaryPower.MobilizationState.STANDBY) return true;
        return false;
    }

    public String getActiveGoalId() { return activeGoalId; }
    public void setActiveGoalId(String goalId) { this.activeGoalId = goalId; }
}
