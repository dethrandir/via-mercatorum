package com.viamercatorum.core.economy;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.config.ViaMercatorumConfig;

public class PriceCalculator {
    private static final double ELASTICITY = 0.5;
    private static final double STOCK_RATIO_REF = 0.5;

    public static double calculatePrice(TradeGood good, double stock, double demand) {
        double stockRatio = stock / Math.max(1, demand);
        double priceModifier = 1.0 + ELASTICITY * (STOCK_RATIO_REF - stockRatio);
        return good.basePrice() * Math.max(0.2, priceModifier);
    }

    public static double calculateReputationDiscount(double reputation) {
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        return 1.0 - Math.min(config.reputation.maxRepDiscount, reputation * config.reputation.repPerUnit);
    }

    public static double calculateBuyPrice(TradeGood good, double stock, double demand, double reputation) {
        double base = calculatePrice(good, stock, demand);
        return base * calculateReputationDiscount(reputation);
    }

    public static double calculateSellPrice(TradeGood good, double stock, double demand, double reputation) {
        double base = calculatePrice(good, stock, demand) * 0.7;
        return base * (1.0 + Math.min(0.3, reputation * 0.002));
    }

    public static double calculateCaravanBuyPrice(TradeGood good, double riskPremium) {
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        return good.basePrice() * config.economy.caravanPriceMarkup * riskPremium;
    }

    public static double calculateCaravanSellPrice(TradeGood good, double riskPremium) {
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        return good.basePrice() * config.economy.caravanSellRate / riskPremium;
    }

    public static double calculateRegionalBuyPrice(TradeGood good, MarketRegion region, double reputation) {
        double regionalBase = region.getRegionalPrice(good.id(), good.basePrice());
        return regionalBase * calculateReputationDiscount(reputation);
    }

    public static double calculateRegionalSellPrice(TradeGood good, MarketRegion region, double reputation) {
        double regionalBase = region.getRegionalPrice(good.id(), good.basePrice()) * 0.7;
        return regionalBase * (1.0 + Math.min(0.3, reputation * 0.002));
    }
}
