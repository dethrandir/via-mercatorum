package com.viamercatorum.core.economy;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.config.ViaMercatorumConfig;
import com.viamercatorum.core.world.VillageNode;

import java.util.*;

public class MarketRegionManager {
    private final Map<String, MarketRegion> regions = new HashMap<>();
    private final Map<String, String> villageToRegion = new HashMap<>();

    public void assignVillagesToRegions(Map<String, VillageNode> villages) {
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        int gridSize = config.marketRegion.regionGridSize;

        Map<String, List<String>> gridCells = new HashMap<>();

        for (var entry : villages.entrySet()) {
            String villageId = entry.getKey();
            VillageNode village = entry.getValue();

            int cellX = village.getChunkX() / gridSize;
            int cellZ = village.getChunkZ() / gridSize;
            String cellKey = cellX + "_" + cellZ;

            gridCells.computeIfAbsent(cellKey, k -> new ArrayList<>()).add(villageId);
        }

        for (var cellEntry : gridCells.entrySet()) {
            String cellKey = cellEntry.getKey();
            List<String> villageIds = cellEntry.getValue();

            MarketRegion region = new MarketRegion("region_" + cellKey, "Region " + cellKey.replace("_", " "));

            for (String vid : villageIds) {
                region.addVillage(vid);
                villageToRegion.put(vid, region.getId());
            }

            if (region.size() > 0) {
                region.recalculate(villages);
                regions.put(region.getId(), region);
            }
        }
    }

    public void recalculateAll(Map<String, VillageNode> villages) {
        for (MarketRegion region : regions.values()) {
            region.recalculate(villages);
        }
    }

    public MarketRegion getRegionForVillage(String villageId) {
        String regionId = villageToRegion.get(villageId);
        if (regionId == null) return null;
        return regions.get(regionId);
    }

    public MarketRegion getRegion(String regionId) {
        return regions.get(regionId);
    }

    public Collection<MarketRegion> getAllRegions() {
        return Collections.unmodifiableCollection(regions.values());
    }

    public String getRegionIdForVillage(String villageId) {
        return villageToRegion.get(villageId);
    }

    public void clear() {
        regions.clear();
        villageToRegion.clear();
    }
}