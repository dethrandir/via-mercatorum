package com.viamercatorum.world.sync;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.config.ViaMercatorumConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Set;

public class RoadSpeedHandler {
    private static final Set<Block> PAVED_BLOCKS = Set.of(
        Blocks.STONE_BRICKS,
        Blocks.STONE_BRICK_SLAB,
        Blocks.POLISHED_ANDESITE,
        Blocks.POLISHED_GRANITE,
        Blocks.POLISHED_DIORITE
    );

    private static final Set<Block> STONE_BLOCKS = Set.of(
        Blocks.COBBLESTONE,
        Blocks.COBBLESTONE_SLAB,
        Blocks.COBBLESTONE_STAIRS,
        Blocks.STONE,
        Blocks.ANDESITE
    );

    private static final Set<Block> GRAVEL_BLOCKS = Set.of(
        Blocks.GRAVEL,
        Blocks.COARSE_DIRT
    );

    private static final Set<Block> DIRT_BLOCKS = Set.of(
        Blocks.DIRT,
        Blocks.DIRT_PATH,
        Blocks.GRASS_BLOCK,
        Blocks.PODZOL,
        Blocks.MYCELIUM,
        Blocks.MOSS_BLOCK
    );

    private int tickCounter = 0;

    public void tick(ServerLevel world) {
        tickCounter++;
        if (tickCounter % 20 != 0) return;

        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();

        for (var player : world.players()) {
            BlockPos below = player.blockPosition().below();
            Block block = world.getBlockState(below).getBlock();

            int speedLevel = 0;
            if (PAVED_BLOCKS.contains(block) && config.roadMaintenance.pavedSpeedLevel > 0) {
                speedLevel = config.roadMaintenance.pavedSpeedLevel;
            } else if (STONE_BLOCKS.contains(block) && config.roadMaintenance.stoneSpeedLevel > 0) {
                speedLevel = config.roadMaintenance.stoneSpeedLevel;
            } else if (GRAVEL_BLOCKS.contains(block) && config.roadMaintenance.gravelSpeedLevel > 0) {
                speedLevel = config.roadMaintenance.gravelSpeedLevel;
            } else if (DIRT_BLOCKS.contains(block) && config.roadMaintenance.dirtSpeedLevel > 0) {
                speedLevel = config.roadMaintenance.dirtSpeedLevel;
            }

            if (speedLevel > 0) {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, speedLevel - 1, false, false, true));
            }
        }
    }
}