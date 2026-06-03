package com.viamercatorum.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ViaMercatorumConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @SerializedName("piracy")
    public PiracyConfig piracy = new PiracyConfig();

    @SerializedName("economy")
    public EconomyConfig economy = new EconomyConfig();

    @SerializedName("reputation")
    public ReputationConfig reputation = new ReputationConfig();

    @SerializedName("ai")
    public AIConfig ai = new AIConfig();

    @SerializedName("exotic_item_desire")
    public Map<String, Double> exoticItemDesire = new HashMap<>();

    public static class PiracyConfig {
        @SerializedName("decay_rate")
        public double decayRate = 0.001;

        @SerializedName("random_fluctuation")
        public double randomFluctuation = 0.004;

        @SerializedName("raid_penalty")
        public double raidPenalty = 0.3;

        @SerializedName("bandits_cleared_bonus")
        public double banditsClearedBonus = -0.1;

        @SerializedName("player_bandit_kill_bonus")
        public double playerBanditKillBonus = -0.05;

        @SerializedName("wealth_threshold")
        public double wealthThreshold = 1000.0;

        @SerializedName("wealth_piracy_increase")
        public double wealthPiracyIncrease = 0.001;

        @SerializedName("risk_premium_multiplier")
        public double riskPremiumMultiplier = 0.3;

        @SerializedName("raid_threshold")
        public double raidThreshold = 0.4;

        @SerializedName("raid_chance_multiplier")
        public double raidChanceMultiplier = 0.1;

        @SerializedName("biome_modifier")
        public Map<String, Double> biomeModifier = new HashMap<>();

        @SerializedName("road_quality_modifier")
        public Map<String, Double> roadQualityModifier = new HashMap<>();
    }

    public static class EconomyConfig {
        @SerializedName("caravan_price_markup")
        public double caravanPriceMarkup = 1.2;

        @SerializedName("caravan_sell_rate")
        public double caravanSellRate = 0.7;

        @SerializedName("food_crisis_threshold")
        public double foodCrisisThreshold = 0.2;

        @SerializedName("profitable_goods_from_threshold")
        public double profitableGoodsFromThreshold = 0.6;

        @SerializedName("profitable_goods_to_threshold")
        public double profitableGoodsToThreshold = 0.3;
    }

    public static class ReputationConfig {
        @SerializedName("trade_reputation_gain")
        public double tradeReputationGain = 0.5;

        @SerializedName("bandit_kill_reputation_gain")
        public double banditKillReputationGain = 2.0;

        @SerializedName("caravan_defense_reputation_gain")
        public double caravanDefenseReputationGain = 5.0;

        @SerializedName("village_raid_reputation_loss")
        public double villageRaidReputationLoss = -10.0;

        @SerializedName("caravan_raid_reputation_loss")
        public double caravanRaidReputationLoss = -20.0;

        @SerializedName("max_rep_discount")
        public double maxRepDiscount = 0.3;

        @SerializedName("rep_per_unit")
        public double repPerUnit = 0.003;
    }

    public static class AIConfig {
        @SerializedName("production_optimization_interval")
        public int productionOptimizationInterval = 100;

        @SerializedName("production_rate_boost")
        public double productionRateBoost = 0.1;

        @SerializedName("max_production_multiplier")
        public double maxProductionMultiplier = 2.0;

        @SerializedName("route_random_factor")
        public double routeRandomFactor = 0.2;

        @SerializedName("food_crisis_stay_multiplier")
        public double foodCrisisStayMultiplier = 2.5;

        @SerializedName("min_stock_for_export")
        public double minStockForExport = 0.6;

        @SerializedName("max_stock_for_import")
        public double maxStockForImport = 0.3;

        @SerializedName("village_raid_interval")
        public int villageRaidInterval = 1000;

        @SerializedName("village_raid_chance")
        public double villageRaidChance = 0.01;

        @SerializedName("village_raid_wealth_threshold")
        public double villageRaidWealthThreshold = 500.0;
    }

    public static ViaMercatorumConfig load(Path configDir) {
        Path configFile = configDir.resolve("viamercatorum.json");
        if (Files.exists(configFile)) {
            try {
                String json = Files.readString(configFile);
                ViaMercatorumConfig config = GSON.fromJson(json, ViaMercatorumConfig.class);
                if (config != null) {
                    config.fillDefaults();
                    return config;
                }
            } catch (IOException e) {
                // fall through to defaults
            }
        }
        ViaMercatorumConfig config = new ViaMercatorumConfig();
        config.fillDefaults();
        config.save(configDir);
        return config;
    }

    public void save(Path configDir) {
        try {
            Files.createDirectories(configDir);
            Path configFile = configDir.resolve("viamercatorum.json");
            Files.writeString(configFile, GSON.toJson(this));
        } catch (IOException e) {
            // silently fail
        }
    }

    private void fillDefaults() {
        if (piracy.biomeModifier.isEmpty()) {
            piracy.biomeModifier.put("minecraft:desert", 0.05);
            piracy.biomeModifier.put("minecraft:savanna", 0.05);
            piracy.biomeModifier.put("minecraft:jungle", 0.08);
            piracy.biomeModifier.put("minecraft:swamp", 0.08);
            piracy.biomeModifier.put("minecraft:plains", 0.0);
            piracy.biomeModifier.put("minecraft:taiga", -0.02);
            piracy.biomeModifier.put("minecraft:snowy", -0.02);
        }
        if (piracy.roadQualityModifier.isEmpty()) {
            piracy.roadQualityModifier.put("DIRT", 0.1);
            piracy.roadQualityModifier.put("GRAVEL", 0.05);
            piracy.roadQualityModifier.put("STONE", 0.0);
            piracy.roadQualityModifier.put("PAVED", -0.05);
        }
        if (exoticItemDesire.isEmpty()) {
            exoticItemDesire.put("minecraft:desert", 1.5);
            exoticItemDesire.put("minecraft:jungle", 2.0);
            exoticItemDesire.put("minecraft:swamp", 1.3);
            exoticItemDesire.put("minecraft:snowy", 1.2);
            exoticItemDesire.put("minecraft:plains", 1.0);
            exoticItemDesire.put("minecraft:savanna", 1.1);
            exoticItemDesire.put("minecraft:taiga", 1.0);
        }
    }
}
