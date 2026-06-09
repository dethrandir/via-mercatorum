package com.viamercatorum.core.economy;

import com.viamercatorum.core.world.VillageNode;

import java.util.*;

public class MarketRegion {
    private final String id;
    private final String name;
    private final List<String> memberVillageIds = new ArrayList<>();
    private String marketCenterId;
    private final Map<String, Double> aggregatedSupply = new HashMap<>();
    private final Map<String, Double> aggregatedDemand = new HashMap<>();

    public MarketRegion(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addVillage(String villageId) {
        if (!memberVillageIds.contains(villageId)) {
            memberVillageIds.add(villageId);
        }
    }

    public void removeVillage(String villageId) {
        memberVillageIds.remove(villageId);
    }

    public void recalculate(Map<String, VillageNode> villages) {
        aggregatedSupply.clear();
        aggregatedDemand.clear();

        double maxGdp = 0;
        String centerId = null;

        for (String vid : memberVillageIds) {
            VillageNode village = villages.get(vid);
            if (village == null) continue;

            for (var entry : village.getSupplies().entrySet()) {
                String goodId = entry.getKey();
                Supply supply = entry.getValue();
                aggregatedSupply.merge(goodId, supply.getStock(), Double::sum);
                aggregatedDemand.merge(goodId, supply.getMaxCapacity() * 0.5, Double::sum);
            }

            double gdp = EconomyHelper.gdp(village);
            if (gdp > maxGdp) {
                maxGdp = gdp;
                centerId = vid;
            }
        }

        if (centerId != null) {
            marketCenterId = centerId;
        }
    }

    public double getRegionalPrice(String goodId, double basePrice) {
        double supply = aggregatedSupply.getOrDefault(goodId, 0.0);
        double demand = aggregatedDemand.getOrDefault(goodId, 1.0);
        double stockRatio = supply / Math.max(1, demand);
        double priceModifier = 1.0 + 0.5 * (0.5 - stockRatio);
        return basePrice * Math.max(0.2, priceModifier);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<String> getMemberVillageIds() { return Collections.unmodifiableList(memberVillageIds); }
    public String getMarketCenterId() { return marketCenterId; }
    public Map<String, Double> getAggregatedSupply() { return Collections.unmodifiableMap(aggregatedSupply); }
    public Map<String, Double> getAggregatedDemand() { return Collections.unmodifiableMap(aggregatedDemand); }
    public int size() { return memberVillageIds.size(); }
}