package com.viamercatorum.player;

import com.viamercatorum.core.world.VillageNode;

import java.util.*;

public class MerchantMap {
    private final Map<String, DiscoveredVillage> discoveredVillages = new LinkedHashMap<>();
    private final List<MapEvent> recentEvents = new ArrayList<>();

    public record DiscoveredVillage(
        String id, String name, VillageNode.VillageType type,
        int chunkX, int chunkZ, long discoveredAt,
        Map<String, Double> availableGoods
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
            discoveredVillages.put(village.getId(), new DiscoveredVillage(
                village.getId(), village.getName(), village.getType(),
                village.getChunkX(), village.getChunkZ(),
                System.currentTimeMillis(), goods
            ));
        }
    }

    public void addEvent(MapEvent event) {
        recentEvents.add(event);
        if (recentEvents.size() > 50) {
            recentEvents.remove(0);
        }
    }

    public Collection<DiscoveredVillage> getDiscoveredVillages() {
        return discoveredVillages.values();
    }

    public List<MapEvent> getRecentEvents() {
        return List.copyOf(recentEvents);
    }
}
