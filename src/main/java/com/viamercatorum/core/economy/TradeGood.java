package com.viamercatorum.core.economy;

public record TradeGood(String id, String name, double basePrice, Category category, boolean isPerishable) {
    public enum Category {
        FOOD, RAW_MATERIAL, CRAFTED, ARMOR, WEAPON, TOOL, LUXURY, SPECIAL
    }

    public double calculateExoticPrice(double distanceFromOrigin, double exoticMultiplier) {
        return basePrice * (1.0 + distanceFromOrigin * exoticMultiplier);
    }
}
