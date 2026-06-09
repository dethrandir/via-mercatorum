package com.viamercatorum.compat;

public class SereneSeasonsCompat {
    private boolean loaded;

    public SereneSeasonsCompat() {
        try {
            Class.forName("sereneseasons.api.SeasonHelper");
            loaded = true;
        } catch (ClassNotFoundException e) {
            loaded = false;
        }
    }

    public boolean isLoaded() { return loaded; }

    public String getCurrentSeasonName() {
        if (!loaded) return null;
        try {
            Object season = Class.forName("sereneseasons.api.SeasonHelper")
                .getMethod("getCurrentSeason")
                .invoke(null);
            return season != null ? season.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public double getSeasonalHarvestModifier(String biome) {
        if (!loaded) return 1.0;
        String seasonName = getCurrentSeasonName();
        if (seasonName == null) return 1.0;
        return switch (seasonName) {
            case "SPRING" -> 1.2;
            case "SUMMER" -> 1.5;
            case "AUTUMN" -> 1.3;
            case "WINTER" -> 0.5;
            default -> 1.0;
        };
    }
}