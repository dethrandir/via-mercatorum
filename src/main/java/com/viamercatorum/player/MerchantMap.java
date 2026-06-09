package com.viamercatorum.player;

import com.viamercatorum.core.economy.EconomyHelper;
import com.viamercatorum.core.world.VillageNode;

import java.util.*;

public class MerchantMap {
    private final Map<String, DiscoveredVillage> discoveredVillages = new LinkedHashMap<>();
    private final List<MapEvent> recentEvents = new ArrayList<>();

    public record DiscoveredVillage(
        String id, String name, VillageNode.VillageType type,
        int chunkX, int chunkZ, long discoveredAt,
        Map<String, Double> availableGoods,
        boolean isCoastal,
        String regionId,
        double gdp,
        double prosperity
    ) {}

    public record MapEvent(
        String type, String villageId, String description,
        long gameTime, boolean active
    ) {}

    public void discoverVillage(VillageNode village) {
        if (!discoveredVillages.containsKey(village.getId())) {
            Map<String, Double> goods = new HashMap<>();
            village.getSupplies().forEach((id, supply) ->
                goods.put(id, supply.getStock())
            );

            double gdp = EconomyHelper.gdp(village);
            double prosperity = EconomyHelper.prosperityIndex(village);

            discoveredVillages.put(village.getId(), new DiscoveredVillage(
                village.getId(), village.getName(), village.getType(),
                village.getChunkX(), village.getChunkZ(),
                System.currentTimeMillis(), goods,
                village.isCoastal(),
                village.getRegionId(),
                gdp, prosperity
            ));
        }
    }

    public void addEvent(MapEvent event) {
        recentEvents.add(event);
        if (recentEvents.size() > 50) {
            recentEvents.remove(0);
        }
    }

    public void updateVillage(VillageNode village) {
        if (discoveredVillages.containsKey(village.getId())) {
            discoverVillage(village);
        }
    }

    public Collection<DiscoveredVillage> getDiscoveredVillages() {
        return discoveredVillages.values();
    }

    public List<MapEvent> getRecentEvents() {
        return List.copyOf(recentEvents);
    }

    public DiscoveredVillage getVillage(String id) {
        return discoveredVillages.get(id);
    }

    public List<DiscoveredVillage> getCoastalVillages() {
        return discoveredVillages.values().stream()
            .filter(DiscoveredVillage::isCoastal)
            .toList();
    }

    public List<DiscoveredVillage> getVillagesByRegion(String regionId) {
        return discoveredVillages.values().stream()
            .filter(v -> regionId.equals(v.regionId()))
            .toList();
    }
}