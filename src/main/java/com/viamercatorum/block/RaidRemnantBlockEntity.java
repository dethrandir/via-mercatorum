package com.viamercatorum.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class RaidRemnantBlockEntity extends BlockEntity {
    private static final long SPAWN_DELAY_TICKS = 3L * 24000L;
    private static final long LIFESPAN_TICKS = 14L * 24000L;

    private long createdAt = -1;

    public RaidRemnantBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RAID_REMNANT, pos, state);
    }

    public void setCreatedAt(long gameTime) {
        this.createdAt = gameTime;
        setChanged();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean shouldExpire(long currentGameTime) {
        if (createdAt < 0) return false;
        return currentGameTime - createdAt >= LIFESPAN_TICKS;
    }

    public static boolean isOldEnoughToSpawn(long createdAt, long currentGameTime) {
        return currentGameTime - createdAt >= SPAWN_DELAY_TICKS;
    }

    public List<ItemStack> generateLoot(RandomSource random) {
        List<ItemStack> drops = new ArrayList<>();

        if (random.nextDouble() < 0.2) {
            return drops;
        }

        int count = 1 + random.nextInt(3);
        for (int i = 0; i < count; i++) {
            ItemStack drop = generateRandomDrop(random);
            if (!drop.isEmpty()) {
                drops.add(drop);
            }
        }

        return drops;
    }

    private ItemStack generateRandomDrop(RandomSource random) {
        double roll = random.nextDouble();
        if (roll < 0.25) {
            return new ItemStack(Items.ROTTEN_FLESH, 1 + random.nextInt(2));
        } else if (roll < 0.45) {
            return new ItemStack(Items.BONE, 1 + random.nextInt(2));
        } else if (roll < 0.60) {
            return new ItemStack(Items.STRING);
        } else if (roll < 0.75) {
            return new ItemStack(Items.COAL);
        } else if (roll < 0.85) {
            return new ItemStack(Items.STONE, 1 + random.nextInt(3));
        } else if (roll < 0.92) {
            return new ItemStack(Items.OAK_LOG);
        } else {
            return new ItemStack(Items.STICK, 1 + random.nextInt(2));
        }
    }
}