package com.viamercatorum.core.world;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.config.ViaMercatorumConfig;
import com.viamercatorum.core.economy.Supply;

import java.util.Map;

public class MilitaryPower {

    public enum MobilizationState {
        DEMILITARIZED, STANDBY, MOBILIZED
    }

    private MilitaryPower() {}

    public static double calculateVillagePower(VillageNode village, RouteGraph routeGraph) {
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        double power = 0;

        power += village.getPopulation() * config.military.populationPowerFactor;

        double typeBonus = config.military.typePowerBonus.getOrDefault(village.getType().name(), 1.0);
        power += typeBonus;

        for (Map.Entry<String, Double> entry : config.military.weaponPowerFactor.entrySet()) {
            Supply supply = village.getSupply(entry.getKey());
            if (supply != null && supply.getStock() > 0) {
                power += Math.min(supply.getStock(), 10.0) * entry.getValue();
            }
        }

        int guardCount = 0;
        for (var caravan : routeGraph.getAllCaravans()) {
            if (caravan.getOriginNode().getId().equals(village.getId())
                || caravan.getDestinationNode().getId().equals(village.getId())) {
                guardCount += getGuardCountForType(caravan.getOriginNode().getType());
            }
        }
        power += guardCount * config.military.caravanGuardFactor;

        double roadSecurity = 0;
        int routeCount = 0;
        for (var route : routeGraph.getAllRoutes()) {
            if (route.getFrom().getId().equals(village.getId()) || route.getTo().getId().equals(village.getId())) {
                roadSecurity += (1.0 - route.getPiracyScore());
                routeCount++;
            }
        }
        if (routeCount > 0) {
            power += (roadSecurity / routeCount) * config.military.roadSecurityFactor * routeCount;
        }

        String regionId = village.getRegionId();
        if (regionId != null) {
            var regionManager = ViaMercatorum.getInstance().getMarketRegionManager();
            var region = regionManager != null ? regionManager.getRegion(regionId) : null;
            if (region != null) {
                for (String allyId : region.getMemberVillageIds()) {
                    if (!allyId.equals(village.getId())) {
                        VillageNode ally = routeGraph.getVillage(allyId);
                        if (ally != null) {
                            double allyPower = calculateRawVillagePower(ally, config);
                            power += allyPower * config.military.allyContributionRatio;
                        }
                    }
                }
            }
        }

        if (village.getMobilization() == MobilizationState.MOBILIZED) {
            power *= config.military.mobilizeMultiplier;
        } else if (village.getMobilization() == MobilizationState.STANDBY) {
            power *= config.military.standbyMultiplier;
        }

        if (village.isRaided()) {
            power *= config.military.raidedPenalty;
        }

        return power;
    }

    private static double calculateRawVillagePower(VillageNode village, ViaMercatorumConfig config) {
        double power = village.getPopulation() * config.military.populationPowerFactor;
        power += config.military.typePowerBonus.getOrDefault(village.getType().name(), 1.0);

        for (Map.Entry<String, Double> entry : config.military.weaponPowerFactor.entrySet()) {
            Supply supply = village.getSupply(entry.getKey());
            if (supply != null && supply.getStock() > 0) {
                power += Math.min(supply.getStock(), 10.0) * entry.getValue();
            }
        }
        return power;
    }

    public static int getGuardCountForType(VillageNode.VillageType type) {
        return switch (type) {
            case HAMLET -> 1;
            case VILLAGE -> 2;
            case TOWN -> 3;
            case CITY, FORTIFIED -> 4;
        };
    }
}