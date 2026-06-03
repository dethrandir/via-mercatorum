package com.viamercatorum.world.sync;

import com.viamercatorum.core.world.Caravan;
import com.viamercatorum.core.world.RouteGraph;
import com.viamercatorum.core.world.VillageNode;
import com.viamercatorum.world.spawn.SpawnManager;

public class SEKSyncManager {
    private final RouteGraph routeGraph;
    private final SpawnManager spawnManager;

    public SEKSyncManager(RouteGraph routeGraph, SpawnManager spawnManager) {
        this.routeGraph = routeGraph;
        this.spawnManager = spawnManager;
    }

    public void onChunkLoad(net.minecraft.server.level.ServerLevel world, int chunkX, int chunkZ) {
        spawnManager.onChunkLoaded(world, chunkX, chunkZ);
    }

    public void onChunkUnload(net.minecraft.server.level.ServerLevel world, int chunkX, int chunkZ) {
        spawnManager.onChunkUnloaded(world, chunkX, chunkZ);
    }

    public CaravanSnapshot getCaravanAtPosition(int chunkX, int chunkZ) {
        for (Caravan caravan : routeGraph.getAllCaravans()) {
            VillageNode origin = caravan.getOriginNode();
            VillageNode dest = caravan.getDestinationNode();
            double progress = caravan.getProgress();

            double x = origin.getChunkX() + (dest.getChunkX() - origin.getChunkX()) * progress;
            double z = origin.getChunkZ() + (dest.getChunkZ() - origin.getChunkZ()) * progress;

            if ((int) Math.floor(x) == chunkX && (int) Math.floor(z) == chunkZ) {
                return new CaravanSnapshot(caravan.getId(), x, z, caravan.getCargo());
            }
        }
        return null;
    }

    public record CaravanSnapshot(String id, double x, double z, java.util.Map<String, Double> cargo) {}
}
