package com.viamercatorum.world.worldgen;

import com.viamercatorum.core.world.TradeRoute;
import com.viamercatorum.core.world.VillageNode;
import com.viamercatorum.data.RoadProfile;

import java.util.*;

public class RoadGenerator {
    private final Set<String> generatedSegments = new HashSet<>();
    private final PerlinNoise noise = new PerlinNoise(42);

    public record RoadSegment(int startX, int startZ, int endX, int endZ, String quality) {}

    public List<RoadSegment> generateRoad(TradeRoute route, HeightProvider heightProvider,
                                           List<VillageNode> nearbyVillages, RoadProfile profile) {
        List<RoadSegment> segments = new ArrayList<>();

        int fromX = route.getFrom().getChunkX();
        int fromZ = route.getFrom().getChunkZ();
        int toX = route.getTo().getChunkX();
        int toZ = route.getTo().getChunkZ();

        List<int[]> path = findPath(fromX, fromZ, toX, toZ, heightProvider, nearbyVillages);

        for (int i = 0; i < path.size() - 1; i++) {
            int[] start = path.get(i);
            int[] end = path.get(i + 1);
            String key = segmentKey(start[0], start[1], end[0], end[1]);
            if (!generatedSegments.contains(key)) {
                segments.add(new RoadSegment(start[0], start[1], end[0], end[1], profile.quality()));
                generatedSegments.add(key);
            }
        }

        return segments;
    }

    private List<int[]> findPath(int startX, int startZ, int endX, int endZ,
                                  HeightProvider height, List<VillageNode> attractors) {
        PriorityQueue<Node> open = new PriorityQueue<>();
        Set<String> closed = new HashSet<>();
        Map<String, Node> nodeMap = new HashMap<>();

        Node start = new Node(startX, startZ, null, 0, heuristic(startX, startZ, endX, endZ));
        open.add(start);
        nodeMap.put(key(startX, startZ), start);

        int maxIterations = 10000;
        int iter = 0;

        while (!open.isEmpty() && iter++ < maxIterations) {
            Node current = open.poll();
            String cKey = key(current.x, current.z);

            if (closed.contains(cKey)) continue;
            closed.add(cKey);

            if (current.x == endX && current.z == endZ) {
                return reconstructPath(current);
            }

            for (int[] neighbor : getNeighbors(current.x, current.z)) {
                int nx = neighbor[0], nz = neighbor[1];
                String nKey = key(nx, nz);
                if (closed.contains(nKey)) continue;

                double stepCost = calculateStepCost(current.x, current.z, nx, nz, height, attractors);
                double g = current.g + stepCost;
                double h = heuristic(nx, nz, endX, endZ);
                double f = g + h;

                Node existing = nodeMap.get(nKey);
                if (existing != null && existing.g <= g) continue;

                Node next = new Node(nx, nz, current, g, f);
                open.add(next);
                nodeMap.put(nKey, next);
            }
        }

        return bresenhamFallback(startX, startZ, endX, endZ);
    }

    private double calculateStepCost(int fromX, int fromZ, int toX, int toZ,
                                      HeightProvider height, List<VillageNode> attractors) {
        double baseCost = 1.0;

        double fromH = height.getHeight(fromX, fromZ);
        double toH = height.getHeight(toX, toZ);
        double slope = Math.abs(toH - fromH);

        double slopeCost = 1.0 + slope * slope * 2.0;
        baseCost *= slopeCost;

        double noiseVal = noise.noise(toX * 0.1, toZ * 0.1);
        double noiseCost = 1.0 + (noiseVal - 0.5) * 0.3;
        baseCost *= noiseCost;

        if (attractors != null) {
            for (VillageNode village : attractors) {
                double dx = toX - village.getChunkX();
                double dz = toZ - village.getChunkZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist < 10) {
                    double attraction = (1.0 - dist / 10.0) * 0.4;
                    baseCost -= attraction;
                }
            }
        }

        return Math.max(0.1, baseCost);
    }

    private double heuristic(int x, int z, int tx, int tz) {
        double dx = tx - x;
        double dz = tz - z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    private List<int[]> getNeighbors(int x, int z) {
        return List.of(
            new int[]{x + 1, z}, new int[]{x - 1, z},
            new int[]{x, z + 1}, new int[]{x, z - 1},
            new int[]{x + 1, z + 1}, new int[]{x - 1, z - 1},
            new int[]{x - 1, z + 1}, new int[]{x + 1, z - 1}
        );
    }

    private List<int[]> reconstructPath(Node node) {
        LinkedList<int[]> path = new LinkedList<>();
        Node current = node;
        while (current != null) {
            path.addFirst(new int[]{current.x, current.z});
            current = current.parent;
        }
        return path;
    }

    private List<int[]> bresenhamFallback(int x0, int z0, int x1, int z1) {
        List<int[]> path = new ArrayList<>();
        int dx = Math.abs(x1 - x0);
        int dz = Math.abs(z1 - z0);
        int sx = x0 < x1 ? 1 : -1;
        int sz = z0 < z1 ? 1 : -1;
        int err = dx - dz;
        int cx = x0, cz = z0;
        while (true) {
            path.add(new int[]{cx, cz});
            if (cx == x1 && cz == z1) break;
            int e2 = 2 * err;
            if (e2 > -dz) { err -= dz; cx += sx; }
            if (e2 < dx) { err += dx; cz += sz; }
        }
        return path;
    }

    private String key(int x, int z) {
        return x + "," + z;
    }

    private String segmentKey(int x1, int z1, int x2, int z2) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        return minX + "_" + minZ + "_" + maxX + "_" + maxZ;
    }

    private record Node(int x, int z, Node parent, double g, double f) implements Comparable<Node> {
        @Override
        public int compareTo(Node o) {
            return Double.compare(this.f, o.f);
        }
    }

    public interface HeightProvider {
        double getHeight(int chunkX, int chunkZ);
    }

    public void resetSegments() {
        generatedSegments.clear();
    }

    private static class PerlinNoise {
        private final int[] perm = new int[512];

        public PerlinNoise(int seed) {
            int[] p = new int[256];
            Random rng = new Random(seed);
            for (int i = 0; i < 256; i++) p[i] = i;
            for (int i = 255; i > 0; i--) {
                int j = rng.nextInt(i + 1);
                int tmp = p[i]; p[i] = p[j]; p[j] = tmp;
            }
            System.arraycopy(p, 0, perm, 0, 256);
            System.arraycopy(p, 0, perm, 256, 256);
        }

        public double noise(double x, double z) {
            int xi = (int) Math.floor(x) & 255;
            int zi = (int) Math.floor(z) & 255;
            double xf = x - Math.floor(x);
            double zf = z - Math.floor(z);
            double u = fade(xf);
            double v = fade(zf);
            int aa = perm[perm[xi] + zi];
            int ab = perm[perm[xi] + zi + 1];
            int ba = perm[perm[xi + 1] + zi];
            int bb = perm[perm[xi + 1] + zi + 1];
            double x1 = lerp(grad(aa, xf, zf), grad(ba, xf - 1, zf), u);
            double x2 = lerp(grad(ab, xf, zf - 1), grad(bb, xf - 1, zf - 1), u);
            return lerp(x1, x2, v);
        }

        private double fade(double t) { return t * t * t * (t * (t * 6 - 15) + 10); }
        private double lerp(double a, double b, double t) { return a + t * (b - a); }
        private double grad(int hash, double x, double z) {
            int h = hash & 3;
            return (h & 1) == 0 ? x : -x + ((h & 2) == 0 ? z : -z);
        }
    }
}
