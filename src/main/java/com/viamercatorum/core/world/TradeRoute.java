package com.viamercatorum.core.world;

import com.viamercatorum.ViaMercatorum;

public class TradeRoute {
    private final String id;
    private final VillageNode from;
    private final VillageNode to;
    private RouteQuality quality;
    private double traffic;
    private double piracyScore;
    private long lastRaidTime;

    public enum RouteQuality {
        DIRT(1.0), GRAVEL(1.3), STONE(1.6), PAVED(2.0);

        public final double speedMultiplier;

        RouteQuality(double speedMultiplier) {
            this.speedMultiplier = speedMultiplier;
        }
    }

    public TradeRoute(String id, VillageNode from, VillageNode to) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.quality = RouteQuality.DIRT;
        this.traffic = 0;
        this.piracyScore = 0;
        this.lastRaidTime = 0;
    }

    public void tickPiracyDecay(com.viamercatorum.core.config.ViaMercatorumConfig config) {
        piracyScore = Math.max(0, piracyScore - config.piracy.decayRate);
    }

    public void addPiracy(double amount) {
        piracyScore = Math.max(0, Math.min(1, piracyScore + amount));
    }

    public double getRiskPremium() {
        return 1.0 + piracyScore * ViaMercatorum.getInstance().getConfig().piracy.riskPremiumMultiplier;
    }

    public double getDistance() {
        double dx = from.getChunkX() - to.getChunkX();
        double dz = from.getChunkZ() - to.getChunkZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    public double getTravelTime() {
        return getDistance() / quality.speedMultiplier;
    }

    public void improveQuality() {
        int next = quality.ordinal() + 1;
        if (next < RouteQuality.values().length) {
            quality = RouteQuality.values()[next];
        }
    }

    public void addTraffic(double amount) { this.traffic += amount; }
    public String getId() { return id; }
    public VillageNode getFrom() { return from; }
    public VillageNode getTo() { return to; }
    public RouteQuality getQuality() { return quality; }
    public void setQuality(RouteQuality quality) { this.quality = quality; }
    public double getTraffic() { return traffic; }
    public double getPiracyScore() { return piracyScore; }
    public void setPiracyScore(double score) { this.piracyScore = Math.max(0, Math.min(1, score)); }
    public long getLastRaidTime() { return lastRaidTime; }
    public void setLastRaidTime(long time) { this.lastRaidTime = time; }

    public void applyBiomePiracyModifier() {
        var config = ViaMercatorum.getInstance().getConfig();
        double biomeMod = config.piracy.biomeModifier.getOrDefault(to.getBiome(), 0.0);
        double roadMod = config.piracy.roadQualityModifier.getOrDefault(quality.name(), 0.0);
        addPiracy(biomeMod + roadMod);
    }
}
