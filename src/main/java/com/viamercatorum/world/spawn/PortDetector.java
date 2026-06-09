package com.viamercatorum.world.spawn;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PortDetector {
    private static final int WATER_SCAN_RADIUS = 5;

    public static boolean isCoastalChunk(ServerLevel world, int chunkX, int chunkZ) {
        int baseX = chunkX * 16 + 8;
        int baseZ = chunkZ * 16 + 8;

        for (int dx = -WATER_SCAN_RADIUS; dx <= WATER_SCAN_RADIUS; dx++) {
            for (int dz = -WATER_SCAN_RADIUS; dz <= WATER_SCAN_RADIUS; dz++) {
                int cx = chunkX + dx;
                int cz = chunkZ + dz;
                if (hasWaterInChunk(world, cx, cz)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasWaterInChunk(ServerLevel world, int chunkX, int chunkZ) {
        if (!world.hasChunk(chunkX, chunkZ)) {
            return false;
        }

        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int seaLevel = world.getSeaLevel();
        for (int y = seaLevel - 5; y <= seaLevel + 1; y++) {
            for (int xOff = 0; xOff < 16; xOff += 4) {
                for (int zOff = 0; zOff < 16; zOff += 4) {
                    pos.set(baseX + xOff, y, baseZ + zOff);
                    BlockState state = world.getBlockState(pos);
                    if (state.is(Blocks.WATER)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}