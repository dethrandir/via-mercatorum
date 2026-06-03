package com.viamercatorum.player;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.config.ViaMercatorumConfig;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReputationSystem {
    private final Map<UUID, Double> globalReputation = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Double>> villageReputations = new ConcurrentHashMap<>();
    private final Map<UUID, ReputationProfile> profiles = new ConcurrentHashMap<>();

    public record ReputationProfile(
        double villagerRep,
        double caravanRep,
        int totalRaidsStopped,
        double totalTradeVolume
    ) {
        public static ReputationProfile EMPTY = new ReputationProfile(0, 0, 0, 0);
    }

    public ReputationProfile getProfile(UUID player) {
        return profiles.getOrDefault(player, ReputationProfile.EMPTY);
    }

    public double getGlobalReputation(UUID player) {
        return globalReputation.getOrDefault(player, 0.0);
    }

    public double getVillageReputation(UUID player, String villageId) {
        return villageReputations.computeIfAbsent(player, k -> new ConcurrentHashMap<>())
            .getOrDefault(villageId, 0.0);
    }

    public void addGlobalReputation(UUID player, double amount) {
        globalReputation.merge(player, amount, Double::sum);
    }

    public void addVillageReputation(UUID player, String villageId, double amount) {
        villageReputations.computeIfAbsent(player, k -> new ConcurrentHashMap<>())
            .merge(villageId, amount, Double::sum);
    }

    public void onTradeCompleted(UUID player, String villageId, double volume) {
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        addGlobalReputation(player, config.reputation.tradeReputationGain);
        addVillageReputation(player, villageId, config.reputation.tradeReputationGain);
        profiles.merge(player, new ReputationProfile(0, 0, 0, volume),
            (old, _) -> new ReputationProfile(
                old.villagerRep + config.reputation.tradeReputationGain,
                old.caravanRep,
                old.totalRaidsStopped,
                old.totalTradeVolume + volume
            ));
    }

    public void onCaravanRaided(UUID player) {
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        addGlobalReputation(player, config.reputation.caravanRaidReputationLoss);
        profiles.merge(player, ReputationProfile.EMPTY,
            (old, _) -> new ReputationProfile(
                old.villagerRep,
                old.caravanRep + config.reputation.caravanRaidReputationLoss,
                old.totalRaidsStopped,
                old.totalTradeVolume
            ));
    }

    public void onBanditsCleared(UUID player, String villageId) {
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        addGlobalReputation(player, config.reputation.banditKillReputationGain);
        addVillageReputation(player, villageId, config.reputation.banditKillReputationGain);
        profiles.merge(player, ReputationProfile.EMPTY,
            (old, _) -> new ReputationProfile(
                old.villagerRep + config.reputation.banditKillReputationGain,
                old.caravanRep,
                old.totalRaidsStopped + 1,
                old.totalTradeVolume
            ));
    }

    public void onVillageRaided(UUID player, String villageId) {
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        addGlobalReputation(player, config.reputation.villageRaidReputationLoss);
        addVillageReputation(player, villageId, config.reputation.villageRaidReputationLoss);
    }

    public void onCaravanDefended(UUID player) {
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        addGlobalReputation(player, config.reputation.caravanDefenseReputationGain);
        profiles.merge(player, ReputationProfile.EMPTY,
            (old, _) -> new ReputationProfile(
                old.villagerRep,
                old.caravanRep + config.reputation.caravanDefenseReputationGain,
                old.totalRaidsStopped,
                old.totalTradeVolume
            ));
    }
}
