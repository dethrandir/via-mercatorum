package com.viamercatorum.world.worldgen;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.data.RoadProfile;
import com.viamercatorum.data.RoadProfileLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.List;

public class RoadFeature {
    private static final int SEARCH_RADIUS = 20;

    public void placeRoadSegmentInChunk(LevelWriter world, List<RoadGenerator.RoadSegment> segments,
                                         RoadGenerator.HeightProvider heightProvider, int chunkX, int chunkZ) {
        for (RoadGenerator.RoadSegment segment : segments) {
            placeSingleSegmentInChunk(world, segment, heightProvider, chunkX, chunkZ);
        }
    }

    public void placeRoadSegment(LevelWriter world, List<RoadGenerator.RoadSegment> segments,
                                   RoadGenerator.HeightProvider heightProvider) {
        for (RoadGenerator.RoadSegment segment : segments) {
            placeSingleSegment(world, segment, heightProvider);
        }
    }

    private void placeSingleSegment(LevelWriter world, RoadGenerator.RoadSegment segment,
                                     RoadGenerator.HeightProvider heightProvider) {
        RoadProfileLoader loader = ViaMercatorum.getInstance().getRoadProfileLoader();
        RoadProfile profile = loader.getProfile(segment.quality());
        if (profile == null) return;

        String surfaceBlockStr = profile.surface().block();
        String foundationBlockStr = profile.foundation().block();

        Block surfaceBlock = getBlock(surfaceBlockStr);
        Block foundationBlock = getBlock(foundationBlockStr);
        if (surfaceBlock == null || foundationBlock == null) return;

        int minX = Math.min(segment.startX(), segment.endX());
        int maxX = Math.max(segment.startX(), segment.endX());
        int minZ = Math.min(segment.startZ(), segment.endZ());
        int maxZ = Math.max(segment.startZ(), segment.endZ());

        int width = profile.width();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int w = -width; w <= width; w++) {
                    int wx = x;
                    int wz = z;
                    if (segment.startX() == segment.endX()) {
                        wz += w;
                    } else {
                        wx += w;
                    }

                    int surfaceY = (int) heightProvider.getHeight(wx, wz);
                    if (surfaceY < 0) continue;

                    BlockPos surfacePos = new BlockPos(wx * 16 + 8, surfaceY, wz * 16 + 8);

                    world.setBlock(surfacePos, surfaceBlock.defaultBlockState(), 3);
                    world.setBlock(surfacePos.below(), foundationBlock.defaultBlockState(), 3);

                    if (profile.surface().slab().isPresent()) {
                        Block slabBlock = getBlock(profile.surface().slab().get());
                        if (slabBlock instanceof SlabBlock) {
                            world.setBlock(surfacePos, slabBlock.defaultBlockState()
                                .setValue(SlabBlock.TYPE, SlabType.BOTTOM), 3);
                        }
                    }
                }
            }
        }
    }

    private void placeSingleSegmentInChunk(LevelWriter world, RoadGenerator.RoadSegment segment,
                                            RoadGenerator.HeightProvider heightProvider, int chunkX, int chunkZ) {
        RoadProfileLoader loader = ViaMercatorum.getInstance().getRoadProfileLoader();
        RoadProfile profile = loader.getProfile(segment.quality());
        if (profile == null) return;

        Block surfaceBlock = getBlock(profile.surface().block());
        Block foundationBlock = getBlock(profile.foundation().block());
        if (surfaceBlock == null || foundationBlock == null) return;

        int minX = Math.min(segment.startX(), segment.endX());
        int maxX = Math.max(segment.startX(), segment.endX());
        int minZ = Math.min(segment.startZ(), segment.endZ());
        int maxZ = Math.max(segment.startZ(), segment.endZ());

        int width = profile.width();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int w = -width; w <= width; w++) {
                    int wx = x;
                    int wz = z;
                    if (segment.startX() == segment.endX()) {
                        wz += w;
                    } else {
                        wx += w;
                    }

                    int bx = wx * 16 + 8;
                    int bz = wz * 16 + 8;
                    if ((bx >> 4) != chunkX || (bz >> 4) != chunkZ) continue;

                    int surfaceY = (int) heightProvider.getHeight(wx, wz);
                    if (surfaceY < 0) continue;

                    BlockPos surfacePos = new BlockPos(bx, surfaceY, bz);

                    world.setBlock(surfacePos, surfaceBlock.defaultBlockState(), 3);
                    world.setBlock(surfacePos.below(), foundationBlock.defaultBlockState(), 3);

                    if (profile.surface().slab().isPresent()) {
                        Block slabBlock = getBlock(profile.surface().slab().get());
                        if (slabBlock instanceof SlabBlock) {
                            world.setBlock(surfacePos, slabBlock.defaultBlockState()
                                .setValue(SlabBlock.TYPE, SlabType.BOTTOM), 3);
                        }
                    }
                }
            }
        }
    }

    private Block getBlock(String id) {
        if (id == null) return null;
        return switch (id) {
            case "minecraft:grass_path" -> Blocks.DIRT_PATH;
            case "minecraft:dirt" -> Blocks.DIRT;
            case "minecraft:gravel" -> Blocks.GRAVEL;
            case "minecraft:cobblestone" -> Blocks.COBBLESTONE;
            case "minecraft:stone_bricks" -> Blocks.STONE_BRICKS;
            case "minecraft:mossy_cobblestone" -> Blocks.MOSSY_COBBLESTONE;
            case "minecraft:smooth_stone" -> Blocks.SMOOTH_STONE;
            case "minecraft:stone" -> Blocks.STONE;
            case "minecraft:sandstone" -> Blocks.SANDSTONE;
            case "minecraft:smooth_sandstone" -> Blocks.SMOOTH_SANDSTONE;
            case "minecraft:red_sandstone" -> Blocks.RED_SANDSTONE;
            case "minecraft:smooth_red_sandstone" -> Blocks.SMOOTH_RED_SANDSTONE;
            case "minecraft:oak_planks" -> Blocks.OAK_PLANKS;
            case "minecraft:polished_andesite" -> Blocks.POLISHED_ANDESITE;
            case "minecraft:snow_block" -> Blocks.SNOW_BLOCK;
            case "minecraft:packed_ice" -> Blocks.PACKED_ICE;
            default -> null;
        };
    }
}
