package com.viamercatorum.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @SerializedName("raid")
    public RaidConfig raid = new RaidConfig();

    @SerializedName("season")
    public SeasonConfig season = new SeasonConfig();

    @SerializedName("market_region")
    public MarketRegionConfig marketRegion = new MarketRegionConfig();

    @SerializedName("sea_trade")
    public SeaTradeConfig seaTrade = new SeaTradeConfig();

    @SerializedName("road_maintenance")
    public RoadMaintenanceConfig roadMaintenance = new RoadMaintenanceConfig();

    @SerializedName("bank")
    public BankConfig bank = new BankConfig();

    @SerializedName("exotic_item_desire")
    public Map<String, Double> exoticItemDesire = new HashMap<>();

    @SerializedName("consumption")
    public ConsumptionConfig consumption = new ConsumptionConfig();

    @SerializedName("event")
    public EventConfig event = new EventConfig();

    @SerializedName("population")
    public PopulationConfig population = new PopulationConfig();

    @SerializedName("production_scale")
    public ProductionScaleConfig productionScale = new ProductionScaleConfig();

    @SerializedName("caravan")
    public CaravanConfig caravan = new CaravanConfig();

    @SerializedName("workshop")
    public WorkshopConfig workshop = new WorkshopConfig();

    @SerializedName("military")
    public MilitaryConfig military = new MilitaryConfig();

    @SerializedName("mobilization")
    public MobilizationConfig mobilization = new MobilizationConfig();

    @SerializedName("goal")
    public GoalConfig goal = new GoalConfig();

    @SerializedName("character")
    public CharacterConfig character = new CharacterConfig();

    @SerializedName("preload")
    public PreloadConfig preload = new PreloadConfig();

    @SerializedName("starter_gifts")
    public StarterGiftsConfig starterGifts = new StarterGiftsConfig();

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

        @SerializedName("leader_passive_income")
        public double leaderPassiveIncome = 5.0;

        @SerializedName("merchant_passive_income")
        public double merchantPassiveIncome = 2.0;

        @SerializedName("caravan_leader_passive_income")
        public double caravanLeaderPassiveIncome = 3.0;

        @SerializedName("character_trade_chance")
        public double characterTradeChance = 0.01;

        @SerializedName("character_workshop_invest_chance")
        public double characterWorkshopInvestChance = 0.005;
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

    public static class RaidConfig {
        @SerializedName("caravan_raid_interval")
        public int caravanRaidInterval = 200;

        @SerializedName("min_raider_count")
        public int minRaiderCount = 3;

        @SerializedName("max_raider_count")
        public int maxRaiderCount = 8;

        @SerializedName("guard_combat_power")
        public double guardCombatPower = 0.35;

        @SerializedName("raider_combat_power")
        public double raiderCombatPower = 0.65;

        @SerializedName("random_combat_factor")
        public double randomCombatFactor = 0.35;

        @SerializedName("village_supply_loss_min")
        public double villageSupplyLossMin = 0.1;

        @SerializedName("village_supply_loss_max")
        public double villageSupplyLossMax = 0.3;

        @SerializedName("remnant_spawn_delay_days")
        public int remnantSpawnDelayDays = 3;

        @SerializedName("remnant_lifespan_days")
        public int remnantLifespanDays = 14;

        @SerializedName("raider_despawn_seconds")
        public int raiderDespawnSeconds = 30;
    }

    public static class SeasonConfig {
        @SerializedName("ticks_per_season")
        public long ticksPerSeason = 24000;

        @SerializedName("biome_season_multiplier")
        public Map<String, Double> biomeSeasonMultiplier = new HashMap<>();

        @SerializedName("famine_threshold")
        public double famineThreshold = 0.5;

        @SerializedName("disaster_chance_per_tick")
        public double disasterChancePerTick = 0.0001;

        @SerializedName("use_serene_seasons")
        public boolean useSereneSeasons = true;
    }

    public static class MarketRegionConfig {
        @SerializedName("min_villages_per_region")
        public int minVillagesPerRegion = 6;

        @SerializedName("max_villages_per_region")
        public int maxVillagesPerRegion = 12;

        @SerializedName("region_grid_size")
        public int regionGridSize = 10;

        @SerializedName("recalc_interval_ticks")
        public long recalcIntervalTicks = 500;

        @SerializedName("price_convergence_rate")
        public double priceConvergenceRate = 0.1;
    }

    public static class SeaTradeConfig {
        @SerializedName("sea_route_speed_multiplier")
        public double seaRouteSpeedMultiplier = 0.8;

        @SerializedName("sea_cargo_capacity_bonus")
        public double seaCargoCapacityBonus = 0.5;

        @SerializedName("sea_piracy_multiplier")
        public double seaPiracyMultiplier = 1.5;

        @SerializedName("caravan_spawn_interval")
        public long caravanSpawnInterval = 1800;

        @SerializedName("min_coastal_distance")
        public int minCoastalDistance = 3;

        @SerializedName("port_detection_range")
        public int portDetectionRange = 5;
    }

    public static class RoadMaintenanceConfig {
        @SerializedName("degradation_rate")
        public double degradationRate = 0.0005;

        @SerializedName("maintenance_from_traffic")
        public double maintenanceFromTraffic = 0.001;

        @SerializedName("degrade_threshold")
        public double degradeThreshold = 0.2;

        @SerializedName("repair_threshold")
        public double repairThreshold = 0.8;

        @SerializedName("min_traffic_to_maintain")
        public double minTrafficToMaintain = 10.0;

        @SerializedName("degradation_tick_interval")
        public long degradationTickInterval = 100;

        @SerializedName("paved_speed_level")
        public int pavedSpeedLevel = 2;

        @SerializedName("stone_speed_level")
        public int stoneSpeedLevel = 1;

        @SerializedName("gravel_speed_level")
        public int gravelSpeedLevel = 1;

        @SerializedName("dirt_speed_level")
        public int dirtSpeedLevel = 1;
    }

    public static class BankConfig {
        @SerializedName("interest_rate")
        public double interestRate = 0.02;

        @SerializedName("overdue_grace_ticks")
        public long overdueGraceTicks = 24000;

        @SerializedName("rep_loss_on_overdue")
        public double repLossOnOverdue = -5.0;

        @SerializedName("assassin_spawn_chance")
        public double assassinSpawnChance = 0.01;

        @SerializedName("max_loan_amount")
        public double maxLoanAmount = 64.0;

        @SerializedName("rep_bonus_on_repay")
        public double repBonusOnRepay = 2.0;
    }

    public static class ConsumptionConfig {
        @SerializedName("enabled")
        public boolean enabled = true;

        @SerializedName("consumption_tick_interval")
        public long consumptionTickInterval = 10;

        @SerializedName("demand_update_interval")
        public long demandUpdateInterval = 500;

        @SerializedName("famine_population_loss_rate")
        public double faminePopulationLossRate = 0.001;

        @SerializedName("famine_reputation_loss_rate")
        public double famineReputationLossRate = 0.01;

        @SerializedName("starvation_population_loss_rate")
        public double starvationPopulationLossRate = 0.01;

        @SerializedName("starvation_reputation_loss_rate")
        public double starvationReputationLossRate = 0.05;
    }

    public static class EventConfig {
        @SerializedName("enabled")
        public boolean enabled = true;

        @SerializedName("event_tick_interval")
        public long eventTickInterval = 50;

        @SerializedName("mtth_modifier")
        public double mtthModifier = 1.0;

        @SerializedName("max_active_modifiers")
        public int maxActiveModifiers = 50;
    }

    public static class PopulationConfig {
        @SerializedName("enabled")
        public boolean enabled = true;

        @SerializedName("growth_rate")
        public double growthRate = 0.00002;

        @SerializedName("growth_food_ratio_threshold")
        public double growthFoodRatioThreshold = 0.5;

        @SerializedName("decline_starvation_rate")
        public double declineStarvationRate = 0.001;

        @SerializedName("min_population")
        public double minPopulation = 1.0;

        @SerializedName("growth_interval_ticks")
        public long growthIntervalTicks = 1000;

        @SerializedName("max_population_scale")
        public Map<String, Double> maxPopulationScale = new HashMap<>();

        @SerializedName("resettle_ticks")
        public long resettleTicks = 480000;
    }

    public static class ProductionScaleConfig {
        @SerializedName("enabled")
        public boolean enabled = true;

        @SerializedName("tool_bonus")
        public double toolBonus = 0.15;

        @SerializedName("raw_material_bonus")
        public double rawMaterialBonus = 0.1;

        @SerializedName("food_satisfaction_threshold")
        public double foodSatisfactionThreshold = 0.5;

        @SerializedName("food_production_bonus")
        public double foodProductionBonus = 0.1;

        @SerializedName("food_starvation_threshold")
        public double foodStarvationThreshold = 0.25;

        @SerializedName("food_starvation_penalty")
        public double foodStarvationPenalty = 0.5;

        @SerializedName("min_scale")
        public double minScale = 0.3;

        @SerializedName("max_scale")
        public double maxScale = 2.0;

        @SerializedName("tool_goods")
        public List<String> toolGoods = new ArrayList<>();

        @SerializedName("raw_material_goods")
        public List<String> rawMaterialGoods = new ArrayList<>();

        @SerializedName("default_max_capacity")
        public double defaultMaxCapacity = 100.0;

        @SerializedName("disaster_severity_loss_rate")
        public double disasterSeverityLossRate = 0.1;
    }

    public static class CaravanConfig {
        @SerializedName("cargo_capacity")
        public double cargoCapacity = 50.0;

        @SerializedName("export_fraction")
        public double exportFraction = 0.3;

        @SerializedName("max_per_good")
        public double maxPerGood = 20.0;

        @SerializedName("surplus_threshold")
        public double surplusThreshold = 0.5;

        @SerializedName("surplus_export_fraction")
        public double surplusExportFraction = 0.2;

        @SerializedName("demand_import_priority")
        public double demandImportPriority = 2.0;

        @SerializedName("travel_speed")
        public double travelSpeed = 0.05;

        @SerializedName("traffic_upgrade_threshold")
        public double trafficUpgradeThreshold = 100.0;

        @SerializedName("traffic_increment")
        public double trafficIncrement = 1.0;
    }

    public static class WorkshopConfig {
        @SerializedName("enabled")
        public boolean enabled = true;

        @SerializedName("max_workshops_per_village")
        public int maxWorkshopsPerVillage = 5;
    }

    public static class MilitaryConfig {
        @SerializedName("population_power_factor")
        public double populationPowerFactor = 0.1;

        @SerializedName("type_power_bonus")
        public Map<String, Double> typePowerBonus = new HashMap<>();

        @SerializedName("weapon_power_factor")
        public Map<String, Double> weaponPowerFactor = new HashMap<>();

        @SerializedName("road_security_factor")
        public double roadSecurityFactor = 0.3;

        @SerializedName("caravan_guard_factor")
        public double caravanGuardFactor = 0.5;

        @SerializedName("ally_contribution_ratio")
        public double allyContributionRatio = 0.2;

        @SerializedName("mobilize_multiplier")
        public double mobilizeMultiplier = 1.5;

        @SerializedName("standby_multiplier")
        public double standbyMultiplier = 1.2;

        @SerializedName("raided_penalty")
        public double raidedPenalty = 0.5;
    }

    public static class MobilizationConfig {
        @SerializedName("standby_to_mobilize_ticks")
        public long standbyToMobilizeTicks = 5000;

        @SerializedName("mobilize_duration_ticks")
        public long mobilizeDurationTicks = 50000;

        @SerializedName("mobilize_production_penalty")
        public double mobilizeProductionPenalty = 0.7;

        @SerializedName("standby_production_penalty")
        public double standbyProductionPenalty = 0.9;

        @SerializedName("demobilize_cooldown_ticks")
        public long demobilizeCooldownTicks = 30000;
    }

    public static class CharacterConfig {
        @SerializedName("leader_enabled")
        public boolean leaderEnabled = true;

        @SerializedName("merchant_enabled")
        public boolean merchantEnabled = true;

        @SerializedName("caravan_leader_enabled")
        public boolean caravanLeaderEnabled = true;

        @SerializedName("auto_spawn_leaders")
        public boolean autoSpawnLeaders = true;

        @SerializedName("leader_respawn_delay_ticks")
        public long leaderRespawnDelayTicks = 6000;

        @SerializedName("merchant_count_hamlet")
        public int merchantCountHamlet = 1;

        @SerializedName("merchant_count_village")
        public int merchantCountVillage = 1;

        @SerializedName("merchant_count_town")
        public int merchantCountTown = 2;

        @SerializedName("merchant_count_city")
        public int merchantCountCity = 3;

        @SerializedName("merchant_count_fortified")
        public int merchantCountFortified = 2;

        @SerializedName("merchant_wealth_base")
        public double merchantWealthBase = 50.0;

        @SerializedName("leader_death_allowed")
        public boolean leaderDeathAllowed = true;

        @SerializedName("merchant_death_allowed")
        public boolean merchantDeathAllowed = true;
    }

    public static class PreloadConfig {
        @SerializedName("enabled")
        public boolean enabled = true;

        @SerializedName("chunk_radius")
        public int chunkRadius = 15;

        @SerializedName("fast_forward_ticks")
        public int fastForwardTicks = 2000;
    }

    public static class StarterGiftsConfig {
        @SerializedName("enabled")
        public boolean enabled = true;

        @SerializedName("show_welcome_message")
        public boolean showWelcomeMessage = true;
    }

    public static class GoalConfig {
        @SerializedName("enabled")
        public boolean enabled = true;

        @SerializedName("goal_tick_interval")
        public long goalTickInterval = 200;

        @SerializedName("goal_change_cooldown_ticks")
        public long goalChangeCooldownTicks = 5000;
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
        if (season.biomeSeasonMultiplier.isEmpty()) {
            season.biomeSeasonMultiplier.put("minecraft:desert", 0.8);
            season.biomeSeasonMultiplier.put("minecraft:savanna", 0.85);
            season.biomeSeasonMultiplier.put("minecraft:jungle", 1.2);
            season.biomeSeasonMultiplier.put("minecraft:swamp", 1.1);
        }
        if (population.maxPopulationScale.isEmpty()) {
            population.maxPopulationScale.put("HAMLET", 20.0);
            population.maxPopulationScale.put("VILLAGE", 40.0);
            population.maxPopulationScale.put("TOWN", 80.0);
            population.maxPopulationScale.put("CITY", 150.0);
            population.maxPopulationScale.put("FORTIFIED", 60.0);
        }
        if (productionScale.toolGoods.isEmpty()) {
            productionScale.toolGoods.add("iron_pickaxe");
            productionScale.toolGoods.add("iron_sword");
            productionScale.toolGoods.add("iron_helmet");
            productionScale.toolGoods.add("iron_chestplate");
        }
if (productionScale.rawMaterialGoods.isEmpty()) {
            productionScale.rawMaterialGoods.add("iron_ingot");
            productionScale.rawMaterialGoods.add("coal");
            productionScale.rawMaterialGoods.add("oak_log");
            productionScale.rawMaterialGoods.add("stone");
            productionScale.rawMaterialGoods.add("copper_ingot");
        }
        if (military.typePowerBonus.isEmpty()) {
            military.typePowerBonus.put("HAMLET", 1.0);
            military.typePowerBonus.put("VILLAGE", 3.0);
            military.typePowerBonus.put("TOWN", 5.0);
            military.typePowerBonus.put("CITY", 8.0);
            military.typePowerBonus.put("FORTIFIED", 10.0);
        }
        if (military.weaponPowerFactor.isEmpty()) {
            military.weaponPowerFactor.put("iron_sword", 2.0);
            military.weaponPowerFactor.put("iron_pickaxe", 1.0);
            military.weaponPowerFactor.put("iron_helmet", 1.5);
            military.weaponPowerFactor.put("iron_chestplate", 2.5);
        }
    }
}
