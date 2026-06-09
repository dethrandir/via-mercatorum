package com.viamercatorum.core.simulation;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.config.ViaMercatorumConfig;
import com.viamercatorum.core.world.RouteGraph;
import net.minecraft.server.level.ServerLevel;

public class SEKPreloader {

    public static void preload(ServerLevel overworld, RouteGraph routeGraph,
                                SimulationTick simulationTick, CharacterManager characterManager,
                                ViaMercatorumConfig config) {
        if (!config.preload.enabled) return;
        if (overworld == null) return;

        long startTime = System.currentTimeMillis();

        int radius = config.preload.chunkRadius;
        if (radius <= 0) radius = 15;

        ViaMercatorum.LOGGER.info("Starting preload: radius={} chunks (~{} total)...", radius,
            (2L * radius + 1) * (2L * radius + 1));

        int generatedChunks = 0;
        int cx = 0;
        int cz = 0;
        int dx = 0;
        int dy = -1;

        for (int i = 0; i < (2 * radius + 1) * (2 * radius + 1); i++) {
            if (-radius <= cx && cx <= radius && -radius <= cz && cz <= radius) {
                try {
                    overworld.getChunk(cx, cz);
                    generatedChunks++;
                } catch (Exception e) {
                    ViaMercatorum.LOGGER.warn("Failed to generate chunk [{}, {}]: {}", cx, cz, e.getMessage());
                }
            }
            if (cx == cz || (cx < 0 && cx == -cz) || (cx > 0 && cx == 1 - cz)) {
                int tmp = dx;
                dx = -dy;
                dy = tmp;
            }
            cx += dx;
            cz += dy;
        }

        long genTime = System.currentTimeMillis();

        int villageCount = routeGraph.getAllVillages().size();
        int routeCount = routeGraph.getAllRoutes().size();

        ViaMercatorum.LOGGER.info("Chunk generation done in {}ms: {} chunks, {} villages, {} routes",
            genTime - startTime, generatedChunks, villageCount, routeCount);

        if (villageCount > 0) {
            for (var village : routeGraph.getAllVillages()) {
                characterManager.ensureVillageCharacters(village);
            }

            int fastTicks = config.preload.fastForwardTicks;
            if (fastTicks > 0) {
                simulationTick.fastForward(fastTicks);
            }

            ViaMercatorum.LOGGER.info("Preload complete: {} ticks simulated, {} characters created",
                fastTicks, characterManager.getAllCharacters().size());
        } else {
            ViaMercatorum.LOGGER.warn("No villages found during preload - economy may start empty");
        }

        long totalTime = System.currentTimeMillis() - startTime;
        ViaMercatorum.LOGGER.info("Total preload time: {}ms", totalTime);
    }
}