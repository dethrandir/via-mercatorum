package com.viamercatorum.world.spawn;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RaidEventCache {
    private final List<CachedRaid> pendingRaids = new java.util.ArrayList<>();

    public record CachedRaid(
        int chunkX, int chunkZ,
        boolean isCaravanRaid,
        String caravanId, String routeId, String villageId,
        long gameTime,
        int raiderCount,
        boolean raidersWon,
        Map<String, Double> lootedGoods
    ) {}

    public synchronized void addRaid(CachedRaid raid) {
        pendingRaids.add(raid);
    }

    public synchronized List<CachedRaid> drainRaidsForChunk(int chunkX, int chunkZ, int radius) {
        List<CachedRaid> result = new ArrayList<>();
        Iterator<CachedRaid> it = pendingRaids.iterator();
        while (it.hasNext()) {
            CachedRaid raid = it.next();
            if (Math.abs(raid.chunkX() - chunkX) <= radius && Math.abs(raid.chunkZ() - chunkZ) <= radius) {
                result.add(raid);
                it.remove();
            }
        }
        return result;
    }

    public synchronized List<CachedRaid> drainAllRaids() {
        List<CachedRaid> result = new ArrayList<>(pendingRaids);
        pendingRaids.clear();
        return result;
    }

    public synchronized boolean hasRaidNear(int chunkX, int chunkZ, int radius) {
        for (CachedRaid raid : pendingRaids) {
            if (Math.abs(raid.chunkX() - chunkX) <= radius && Math.abs(raid.chunkZ() - chunkZ) <= radius) {
                return true;
            }
        }
        return false;
    }

    public synchronized int size() {
        return pendingRaids.size();
    }
}