package com.viamercatorum.core.simulation;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.compat.SereneSeasonsCompat;
import com.viamercatorum.core.config.ViaMercatorumConfig;

import java.util.Map;

public class SeasonManager {
    private Season currentSeason = Season.SPRING;
    private long seasonTick = 0;
    private long yearTick = 0;
    private SereneSeasonsCompat sereneSeasonsCompat;

    private static final Map<Season, Map<String, Double>> BIOME_SEASON_MODIFIERS = Map.of(
        Season.SPRING, Map.of(
            "minecraft:plains", 1.2,
            "minecraft:desert", 1.0,
            "minecraft:savanna", 1.1,
            "minecraft:taiga", 1.3,
            "minecraft:snowy", 0.8,
            "minecraft:jungle", 1.4,
            "minecraft:swamp", 1.3
        ),
        Season.SUMMER, Map.of(
            "minecraft:plains", 1.5,
            "minecraft:desert", 0.7,
            "minecraft:savanna", 1.0,
            "minecraft:taiga", 1.4,
            "minecraft:snowy", 1.2,
            "minecraft:jungle", 1.3,
            "minecraft:swamp", 1.2
        ),
        Season.AUTUMN, Map.of(
            "minecraft:plains", 1.3,
            "minecraft:desert", 1.1,
            "minecraft:savanna", 1.2,
            "minecraft:taiga", 1.0,
            "minecraft:snowy", 0.7,
            "minecraft:jungle", 1.2,
            "minecraft:swamp", 1.1
        ),
        Season.WINTER, Map.of(
            "minecraft:plains", 0.5,
            "minecraft:desert", 0.9,
            "minecraft:savanna", 0.7,
            "minecraft:taiga", 0.4,
            "minecraft:snowy", 0.3,
            "minecraft:jungle", 0.8,
            "minecraft:swamp", 0.6
        )
    );

    public enum Season {
        SPRING, SUMMER, AUTUMN, WINTER
    }

    public SeasonManager() {
        this.sereneSeasonsCompat = new SereneSeasonsCompat();
    }

    public SeasonManager(SereneSeasonsCompat sereneSeasonsCompat) {
        this.sereneSeasonsCompat = sereneSeasonsCompat;
    }

    public void tick() {
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        seasonTick++;
        yearTick++;

        if (config.season.useSereneSeasons && sereneSeasonsCompat.isLoaded()) {
            String ssSeason = sereneSeasonsCompat.getCurrentSeasonName();
            if (ssSeason != null) {
                Season parsed = switch (ssSeason) {
                    case "SPRING" -> Season.SPRING;
                    case "SUMMER" -> Season.SUMMER;
                    case "AUTUMN" -> Season.AUTUMN;
                    case "WINTER" -> Season.WINTER;
                    default -> null;
                };
                if (parsed != null && parsed != currentSeason) {
                    currentSeason = parsed;
                    seasonTick = 0;
                    emitSeasonChanged();
                }
            }
        } else {
            long ticksPerSeason = config.season.ticksPerSeason;
            if (seasonTick >= ticksPerSeason) {
                seasonTick = 0;
                advanceSeason();
            }
        }

        if (yearTick >= config.season.ticksPerSeason * 4) {
            yearTick = 0;
        }
    }

    private void advanceSeason() {
        currentSeason = switch (currentSeason) {
            case SPRING -> Season.SUMMER;
            case SUMMER -> Season.AUTUMN;
            case AUTUMN -> Season.WINTER;
            case WINTER -> Season.SPRING;
        };
        emitSeasonChanged();
    }

    private void emitSeasonChanged() {
        var bus = ViaMercatorum.getInstance().getEventBus();
        bus.emit(new EconomyEvent.SeasonChanged(
            "global",
            ViaMercatorum.getInstance().getSimulationTick().getTickCounter(),
            currentSeason.name()
        ));
    }

    public void forceSeason(Season season) {
        this.currentSeason = season;
        this.seasonTick = 0;
    }

    public double getHarvestModifier(String biome) {
        Map<String, Double> seasonMap = BIOME_SEASON_MODIFIERS.getOrDefault(currentSeason, Map.of());
        return seasonMap.getOrDefault(biome, 1.0);
    }

    public Season getCurrentSeason() {
        return currentSeason;
    }

    public long getSeasonTick() {
        return seasonTick;
    }

    public long getYearTick() {
        return yearTick;
    }

    public double getSeasonProgress() {
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        return (double) seasonTick / config.season.ticksPerSeason;
    }

    public boolean isFamineSeason(String biome) {
        double modifier = getHarvestModifier(biome);
        return modifier < 0.5;
    }
}