package com.viamercatorum.world.spawn;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.block.ModBlocks;
import com.viamercatorum.block.RaidRemnantBlockEntity;
import com.viamercatorum.core.simulation.EconomyEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RaidRemnantManager {
    private final List<PendingRemnant> pendingRemnants = new CopyOnWriteArrayList<>();
    private final List<PlacedRemnant> placedRemnants = new CopyOnWriteArrayList<>();

    public record PendingRemnant(int chunkX, int chunkZ, long createdAt) {}
    public record PlacedRemnant(int chunkX, int chunkZ, long createdAt, long expiresAt) {}

    public RaidRemnantManager() {
        ViaMercatorum.getInstance().getEventBus().register(EconomyEvent.RaidOccurred.class, this::onRaidOccurred);
    }

    private long getSpawnDelayTicks() {
        return (long) ViaMercatorum.getInstance().getConfig().raid.remnantSpawnDelayDays * 24000L;
    }

    private long getLifespanTicks() {
        return (long) ViaMercatorum.getInstance().getConfig().raid.remnantLifespanDays * 24000L;
    }

    private void onRaidOccurred(EconomyEvent.RaidOccurred event) {
        PendingRemnant pending = new PendingRemnant(event.getChunkX(), event.getChunkZ(), event.getGameTime());
        pendingRemnants.add(pending);
    }

    public void tick(ServerLevel world) {
        long gameTime = world.getGameTime();

        List<PendingRemnant> spawned = new CopyOnWriteArrayList<>();
        for (PendingRemnant pending : pendingRemnants) {
            if (gameTime - pending.createdAt() < getSpawnDelayTicks()) {
                continue;
            }

            if (world.isLoaded(new BlockPos(pending.chunkX() * 16 + 8, 64, pending.chunkZ() * 16 + 8))) {
                placeRemnant(world, pending);
                spawned.add(pending);
            }
        }
        pendingRemnants.removeAll(spawned);

        List<PlacedRemnant> expired = new CopyOnWriteArrayList<>();
        for (PlacedRemnant placed : placedRemnants) {
            if (gameTime >= placed.expiresAt()) {
                removeRemnant(world, placed);
                expired.add(placed);
            }
        }
        placedRemnants.removeAll(expired);
    }

    private void placeRemnant(ServerLevel world, PendingRemnant pending) {
        RandomSource rng = world.getRandom();
        int bx = pending.chunkX() * 16 + 8 + rng.nextInt(8) - 4;
        int bz = pending.chunkZ() * 16 + 8 + rng.nextInt(8) - 4;
        int surfaceY = world.getHeight(Heightmap.Types.WORLD_SURFACE, bx, bz);

        BlockPos pos = new BlockPos(bx, surfaceY, bz);
        if (world.getBlockState(pos).isAir()) {
            BlockState state = ModBlocks.RAID_REMNANT.defaultBlockState();
            world.setBlock(pos, state, 3);

            if (world.getBlockEntity(pos) instanceof RaidRemnantBlockEntity be) {
                be.setCreatedAt(pending.createdAt());
            }

            long expiresAt = pending.createdAt() + getLifespanTicks();
            placedRemnants.add(new PlacedRemnant(pending.chunkX(), pending.chunkZ(), pending.createdAt(), expiresAt));
        }
    }

    private void removeRemnant(ServerLevel world, PlacedRemnant placed) {
        int bx = placed.chunkX() * 16 + 8;
        int bz = placed.chunkZ() * 16 + 8;
        int surfaceY = world.getHeight(Heightmap.Types.WORLD_SURFACE, bx, bz);

        BlockPos pos = new BlockPos(bx, surfaceY, bz);
        if (world.getBlockState(pos).getBlock() == ModBlocks.RAID_REMNANT) {
            world.removeBlock(pos, false);
        }
    }
}