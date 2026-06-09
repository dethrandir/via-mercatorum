package com.viamercatorum.world.worldgen;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.world.RouteGraph;
import com.viamercatorum.core.world.TradeRoute;
import com.viamercatorum.core.world.VillageNode;
import com.viamercatorum.data.RoadProfile;
import com.viamercatorum.data.RoadProfileLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VillageModifier {
    private final RoadGenerator roadGenerator = new RoadGenerator();
    private final RoadFeature roadFeature = new RoadFeature();
    private final Set<String> processedRoutes = ConcurrentHashMap.newKeySet();
    private final Map<String, List<RoadGenerator.RoadSegment>> roadCache = new ConcurrentHashMap<>();

    public void onVillageGenerated(VillageNode node, RouteGraph graph) {
        graph.addVillage(node);
        connectNearbyVillages(node, graph);
    }

    public void establishTradeRoute(VillageNode from, VillageNode to, RouteGraph graph) {
        String routeId = "route_" + from.getId() + "_" + to.getId();
        String reverseId = "route_" + to.getId() + "_" + from.getId();
        if (graph.getRoute(routeId) != null || graph.getRoute(reverseId) != null) return;
        if (processedRoutes.contains(routeId) || processedRoutes.contains(reverseId)) return;

        TradeRoute route = new TradeRoute(routeId, from, to);
        route.applyBiomePiracyModifier();
        graph.addRoute(route);
        processedRoutes.add(routeId);
    }

    public void buildRoad(ServerLevel world, TradeRoute route) {
        String routeKey = route.getFrom().getId() + "_" + route.getTo().getId();
        if (processedRoutes.contains("road_" + routeKey)) return;
        processedRoutes.add("road_" + routeKey);

        RoadProfileLoader loader = ViaMercatorum.getInstance().getRoadProfileLoader();
        RoadProfile profile = loader.getProfile(route.getQuality().name().toLowerCase());
        if (profile == null) {
            profile = loader.getProfile("dirt");
        }
        if (profile == null) return;

        RoadGenerator.HeightProvider heightProvider = (chunkX, chunkZ) -> {
            int bx = chunkX * 16 + 8;
            int bz = chunkZ * 16 + 8;
            BlockPos testPos = new BlockPos(bx, 0, bz);
            if (world.isLoaded(testPos)) {
                return world.getHeight(Heightmap.Types.WORLD_SURFACE, bx, bz);
            }
            return 64;
        };

        List<VillageNode> nearbyVillages = findNearbyVillages(route, ViaMercatorum.getInstance().getRouteGraph());

        List<RoadGenerator.RoadSegment> segments = roadGenerator.generateRoad(
            route, heightProvider, nearbyVillages, profile
        );

        roadFeature.placeRoadSegment(world, segments, heightProvider);
    }

    public void placeRoadsInChunk(ServerLevel world, int chunkX, int chunkZ) {
        RouteGraph graph = ViaMercatorum.getInstance().getRouteGraph();
        for (TradeRoute route : graph.getAllRoutes()) {
            String routeId = route.getId();
            List<RoadGenerator.RoadSegment> segments = roadCache.get(routeId);
            if (segments == null) continue;

            for (RoadGenerator.RoadSegment seg : segments) {
                if (segmentTouchesChunk(seg, chunkX, chunkZ)) {
                    List<RoadGenerator.RoadSegment> single = List.of(
                        new RoadGenerator.RoadSegment(seg.startX(), seg.startZ(), seg.endX(), seg.endZ(), seg.quality())
                    );
                    RoadGenerator.HeightProvider hp = getDefaultHeightProvider(world);
                    roadFeature.placeRoadSegmentInChunk(world, single, hp, chunkX, chunkZ);
                }
            }
        }
    }

    public void cacheRoadPath(TradeRoute route, ServerLevel world) {
        String routeId = route.getId();
        if (roadCache.containsKey(routeId)) return;

        RoadProfileLoader loader = ViaMercatorum.getInstance().getRoadProfileLoader();
        RoadProfile profile = loader.getProfile(route.getQuality().name().toLowerCase());
        if (profile == null) {
            profile = loader.getProfile("dirt");
        }
        if (profile == null) return;

        RoadGenerator.HeightProvider hp = getDefaultHeightProvider(world);
        List<VillageNode> nearby = findNearbyVillages(route, ViaMercatorum.getInstance().getRouteGraph());
        List<RoadGenerator.RoadSegment> segments = roadGenerator.generateRoad(route, hp, nearby, profile);
        roadCache.put(routeId, segments);
    }

    private boolean segmentTouchesChunk(RoadGenerator.RoadSegment seg, int chunkX, int chunkZ) {
        int minX = Math.min(seg.startX(), seg.endX());
        int maxX = Math.max(seg.startX(), seg.endX());
        int minZ = Math.min(seg.startZ(), seg.endZ());
        int maxZ = Math.max(seg.startZ(), seg.endZ());
        return chunkX >= minX && chunkX <= maxX && chunkZ >= minZ && chunkZ <= maxZ;
    }

    private RoadGenerator.HeightProvider getDefaultHeightProvider(ServerLevel world) {
        return (chunkX, chunkZ) -> {
            int bx = chunkX * 16 + 8;
            int bz = chunkZ * 16 + 8;
            BlockPos testPos = new BlockPos(bx, 0, bz);
            if (world.isLoaded(testPos)) {
                return world.getHeight(Heightmap.Types.WORLD_SURFACE, bx, bz);
            }
            return 64;
        };
    }

    private List<VillageNode> findNearbyVillages(TradeRoute route, RouteGraph graph) {
        List<VillageNode> nearby = new ArrayList<>();
        double midX = (route.getFrom().getChunkX() + route.getTo().getChunkX()) / 2.0;
        double midZ = (route.getFrom().getChunkZ() + route.getTo().getChunkZ()) / 2.0;

        for (VillageNode village : graph.getAllVillages()) {
            if (village.getId().equals(route.getFrom().getId()) ||
                village.getId().equals(route.getTo().getId())) continue;

            double dx = village.getChunkX() - midX;
            double dz = village.getChunkZ() - midZ;
            double dist = Math.sqrt(dx * dx + dz * dz);

            if (dist < 15) {
                nearby.add(village);
            }
        }
        return nearby;
    }

    private void connectNearbyVillages(VillageNode node, RouteGraph graph) {
        double maxConnectionDistance = 80;

        for (VillageNode other : graph.getAllVillages()) {
            if (other.getId().equals(node.getId())) continue;

            double dx = node.getChunkX() - other.getChunkX();
            double dz = node.getChunkZ() - other.getChunkZ();
            double dist = Math.sqrt(dx * dx + dz * dz);

            if (dist < maxConnectionDistance) {
                establishTradeRoute(node, other, graph);
            }
        }
    }
}
