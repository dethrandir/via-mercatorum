package com.viamercatorum.compat;

public class SereneSeasonsCompat {
    private boolean loaded;

    public SereneSeasonsCompat() {
        try {
            Class.forName("sereneseasons.api.SSItems");
            loaded = true;
        } catch (ClassNotFoundException e) {
            loaded = false;
        }
    }

    public boolean isLoaded() { return loaded; }

    public Season getCurrentSeason() {
        if (!loaded) return Season.NEUTRAL;
        return Season.NEUTRAL;
    }

    public enum Season {
        SPRING, SUMMER, AUTUMN, WINTER, NEUTRAL
    }

    public double getSeasonalHarvestModifier(String biome) {
        if (!loaded) return 1.0;
        Season season = getCurrentSeason();
        return switch (season) {
            case SPRING -> 1.2;
            case SUMMER -> 1.5;
            case AUTUMN -> 1.3;
            case WINTER -> 0.5;
            case NEUTRAL -> 1.0;
        };
    }
}
