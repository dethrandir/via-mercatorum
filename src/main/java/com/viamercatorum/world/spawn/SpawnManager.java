package com.viamercatorum.world.spawn;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.simulation.Character;
import com.viamercatorum.core.simulation.CharacterManager;
import com.viamercatorum.core.simulation.SimulationTick;
import com.viamercatorum.core.world.Caravan;
import com.viamercatorum.core.world.RouteGraph;
import com.viamercatorum.core.world.TradeRoute;
import com.viamercatorum.core.world.VillageNode;
import com.viamercatorum.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.equine.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpawnManager {
    private final RouteGraph routeGraph;
    private final SimulationTick simulationTick;
    private final Map<String, Set<Entity>> activeCaravanEntities = new ConcurrentHashMap<>();
    private final Map<String, Set<Entity>> activeVillageEntities = new ConcurrentHashMap<>();
    private final CaravanComposition composition = new CaravanComposition();

    private static final int VILLAGE_SPAWN_RADIUS = 3;

    public SpawnManager(RouteGraph routeGraph, SimulationTick simulationTick) {
        this.routeGraph = routeGraph;
        this.simulationTick = simulationTick;
    }

    public void onChunkLoaded(ServerLevel world, int chunkX, int chunkZ) {
        for (VillageNode village : routeGraph.getAllVillages()) {
            if (isInVillageRange(village, chunkX, chunkZ)) {
                spawnVillageEntities(world, village);
            }
        }
        for (Caravan caravan : routeGraph.getAllCaravans()) {
            if (isCaravanInChunk(caravan, chunkX, chunkZ)) {
                spawnCaravanEntities(world, caravan);
            }
        }

        RaidEventCache raidCache = simulationTick.getRaidEventCache();
        List<RaidEventCache.CachedRaid> raids = raidCache.drainRaidsForChunk(chunkX, chunkZ, 3);
        for (RaidEventCache.CachedRaid raid : raids) {
            spawnRaidEntities(world, raid);
        }
    }

    public void onChunkUnloaded(ServerLevel world, int chunkX, int chunkZ) {
        String vKey = "v_" + chunkX + "," + chunkZ;
        Set<Entity> villageEntities = activeVillageEntities.remove(vKey);
        if (villageEntities != null) {
            for (Entity e : villageEntities) {
                if (e != null && e.isAlive()) e.discard();
            }
        }

        String cKey = "c_" + chunkX + "," + chunkZ;
        Set<Entity> caravanEntities = activeCaravanEntities.remove(cKey);
        if (caravanEntities != null) {
            for (Entity e : caravanEntities) {
                if (e != null && e.isAlive()) e.discard();
            }
        }
    }

    public void repositionCaravans(ServerLevel world) {
        for (var entry : activeCaravanEntities.entrySet()) {
            String key = entry.getKey();
            Set<Entity> entities = entry.getValue();
            if (entities.isEmpty()) continue;

            String[] parts = key.split("_");
            if (parts.length < 2) continue;
            String[] coords = parts[1].split(",");
            if (coords.length < 2) continue;

            for (Caravan caravan : routeGraph.getAllCaravans()) {
                VillageNode origin = caravan.getOriginNode();
                VillageNode dest = caravan.getDestinationNode();
                double progress = caravan.getProgress();
                double wx = (origin.getChunkX() + (dest.getChunkX() - origin.getChunkX()) * progress) * 16 + 8;
                double wz = (origin.getChunkZ() + (dest.getChunkZ() - origin.getChunkZ()) * progress) * 16 + 8;

                int cx = (int) Math.floor(progress == 1.0 ? dest.getChunkX() : (origin.getChunkX() + (dest.getChunkX() - origin.getChunkX()) * progress));
                int cz = (int) Math.floor(progress == 1.0 ? dest.getChunkZ() : (origin.getChunkZ() + (dest.getChunkZ() - origin.getChunkZ()) * progress));

                if (key.equals("c_" + cx + "," + cz)) {
                    int yi = getSurfaceY(world, (int) wx, (int) wz);
                    for (Entity e : entities) {
                        double dx = e.getX() - (entities.iterator().next().getX());
                        double dz = e.getZ() - (entities.iterator().next().getZ());
                        e.teleportTo(wx + dx, yi, wz + dz);
                    }
                }
            }
        }
    }

    private void spawnRaidEntities(ServerLevel world, RaidEventCache.CachedRaid raid) {
        int bx = raid.chunkX() * 16 + 8;
        int bz = raid.chunkZ() * 16 + 8;
        int surfaceY = world.getHeight(Heightmap.Types.WORLD_SURFACE, bx, bz);

        Set<Entity> spawned = new HashSet<>();
        Random rng = new Random(raid.gameTime());

        for (int i = 0; i < raid.raiderCount(); i++) {
            CaravanRaiderEntity raider = new CaravanRaiderEntity(ModEntities.CARAVAN_RAIDER, world);
            raider.setVariant(i % 2 == 0 ? CaravanRaiderEntity.RaiderVariant.AXE : CaravanRaiderEntity.RaiderVariant.CROSSBOW);
            double offsetX = (rng.nextDouble() - 0.5) * 10;
            double offsetZ = (rng.nextDouble() - 0.5) * 10;
            raider.setPos(bx + offsetX, surfaceY, bz + offsetZ);
            world.addFreshEntity(raider);
            spawned.add(raider);
        }

        if (raid.isCaravanRaid()) {
            String key = "raid_c_" + raid.chunkX() + "," + raid.chunkZ();
            activeCaravanEntities.put(key, spawned);
        } else {
            String key = "raid_v_" + raid.chunkX() + "," + raid.chunkZ();
            activeVillageEntities.put(key, spawned);
        }
    }

    private boolean isInVillageRange(VillageNode village, int chunkX, int chunkZ) {
        int dx = Math.abs(village.getChunkX() - chunkX);
        int dz = Math.abs(village.getChunkZ() - chunkZ);
        return dx <= VILLAGE_SPAWN_RADIUS && dz <= VILLAGE_SPAWN_RADIUS;
    }

    private boolean isCaravanInChunk(Caravan caravan, int chunkX, int chunkZ) {
        VillageNode origin = caravan.getOriginNode();
        VillageNode dest = caravan.getDestinationNode();
        double progress = caravan.getProgress();

        double x = origin.getChunkX() + (dest.getChunkX() - origin.getChunkX()) * progress;
        double z = origin.getChunkZ() + (dest.getChunkZ() - origin.getChunkZ()) * progress;

        int caravanChunkX = (int) Math.floor(x);
        int caravanChunkZ = (int) Math.floor(z);

        return caravanChunkX == chunkX && caravanChunkZ == chunkZ;
    }

    private void spawnVillageEntities(ServerLevel world, VillageNode village) {
        String key = "v_" + village.getChunkX() + "," + village.getChunkZ();
        if (activeVillageEntities.containsKey(key)) return;

        Set<Entity> spawned = new HashSet<>();

        int guardCount = switch (village.getType()) {
            case HAMLET -> 0;
            case VILLAGE -> 1;
            case TOWN -> 2;
            case CITY -> 4;
            case FORTIFIED -> 6;
        };

        for (int i = 0; i < guardCount; i++) {
            CaravanGuardEntity guard = new CaravanGuardEntity(ModEntities.CARAVAN_GUARD, world);
            guard.setVariant(i % 2 == 0 ? CaravanGuardEntity.GuardVariant.SWORD : CaravanGuardEntity.GuardVariant.BOW);
            guard.setPersistenceRequired();
            BlockPos pos = findSpawnPos(world, village.getChunkX(), village.getChunkZ());
            if (pos != null) {
                guard.setPos(pos.getX() + (i - guardCount / 2.0) * 2, pos.getY(), pos.getZ());
            }
            world.addFreshEntity(guard);
            spawned.add(guard);
        }

        activeVillageEntities.put(key, spawned);

        CharacterManager charManager = ViaMercatorum.getInstance().getCharacterManager();
        if (charManager != null) {
            charManager.ensureVillageCharacters(village);
        }

        spawnVillageCharacters(world, village, spawned);
    }

    private void spawnVillageCharacters(ServerLevel world, VillageNode village, Set<Entity> spawned) {
        CharacterManager charManager = ViaMercatorum.getInstance().getCharacterManager();
        if (charManager == null) return;

        var characters = charManager.getCharactersByVillage(village.getId());
        for (Character character : characters) {
            BlockPos pos = findSpawnPos(world, village.getChunkX(), village.getChunkZ());
            if (pos == null) continue;

            CharacterEntity entity = new CharacterEntity(ModEntities.CHARACTER, world);
            entity.setCharacterId(character.getId());
            entity.setPos(pos.getX() + RandomSource.create().nextDouble() * 4 - 2, pos.getY(), pos.getZ() + RandomSource.create().nextDouble() * 4 - 2);
            entity.setPersistenceRequired();
            world.addFreshEntity(entity);
            spawned.add(entity);
        }
    }

private void spawnCaravanEntities(ServerLevel world, Caravan caravan) {
        VillageNode origin = caravan.getOriginNode();
        VillageNode dest = caravan.getDestinationNode();
        double progress = caravan.getProgress();

        double worldX = (origin.getChunkX() + (dest.getChunkX() - origin.getChunkX()) * progress) * 16 + 8;
        double worldZ = (origin.getChunkZ() + (dest.getChunkZ() - origin.getChunkZ()) * progress) * 16 + 8;

        int chunkX = (int) Math.floor(progress == 1.0 ? dest.getChunkX() : (origin.getChunkX() + (dest.getChunkX() - origin.getChunkX()) * progress));
        int chunkZ = (int) Math.floor(progress == 1.0 ? dest.getChunkZ() : (origin.getChunkZ() + (dest.getChunkZ() - origin.getChunkZ()) * progress));

        String key = "c_" + chunkX + "," + chunkZ;
        if (activeCaravanEntities.containsKey(key)) return;

        Set<Entity> spawned = new HashSet<>();
        CaravanComposition.Composition comp = composition.determine(origin);
        int surfaceY = getSurfaceY(world, (int) worldX, (int) worldZ);

        CaravanEntity leader = new CaravanEntity(ModEntities.CARAVAN, world);
        leader.setCaravanData(caravan.getId(), caravan.getRouteId());
        leader.setPos(worldX, surfaceY, worldZ);
        leader.setPersistenceRequired();
        world.addFreshEntity(leader);
        spawned.add(leader);

        CharacterManager charManager = ViaMercatorum.getInstance().getCharacterManager();
        if (charManager != null) {
            Character caravanLeader = charManager.createCaravanLeaderForCaravan(caravan.getId(), origin);
            BlockPos spawnPos = new BlockPos((int) worldX, surfaceY, (int) worldZ);
            CharacterEntity leaderChar = new CharacterEntity(ModEntities.CHARACTER, world);
            leaderChar.setCharacterId(caravanLeader.getId());
            leaderChar.setPos(worldX + 1, surfaceY, worldZ + 1);
            leaderChar.setPersistenceRequired();
            world.addFreshEntity(leaderChar);
            spawned.add(leaderChar);
        }

        Random rng = new Random(caravan.getId().hashCode());

        for (int i = 0; i < comp.swordGuardCount(); i++) {
            CaravanGuardEntity guard = new CaravanGuardEntity(ModEntities.CARAVAN_GUARD, world);
            guard.setVariant(CaravanGuardEntity.GuardVariant.SWORD);
            guard.setPos(worldX + (rng.nextDouble() - 0.5) * 4, surfaceY, worldZ + (rng.nextDouble() - 0.5) * 4);
            guard.setPersistenceRequired();
            world.addFreshEntity(guard);
            spawned.add(guard);
        }

        for (int i = 0; i < comp.bowGuardCount(); i++) {
            CaravanGuardEntity guard = new CaravanGuardEntity(ModEntities.CARAVAN_GUARD, world);
            guard.setVariant(CaravanGuardEntity.GuardVariant.BOW);
            guard.setPos(worldX + (rng.nextDouble() - 0.5) * 4, surfaceY, worldZ + (rng.nextDouble() - 0.5) * 4);
            guard.setPersistenceRequired();
            world.addFreshEntity(guard);
            spawned.add(guard);
        }

        for (var mountEntry : comp.mounts().entrySet()) {
            for (int i = 0; i < mountEntry.getValue(); i++) {
                Entity mount = createMount(mountEntry.getKey(), world);
                if (mount != null) {
                    mount.setPos(worldX + (rng.nextDouble() - 0.5) * 5, surfaceY, worldZ + (rng.nextDouble() - 0.5) * 5);
                    if (mount instanceof Mob) ((Mob)mount).setPersistenceRequired();
                    world.addFreshEntity(mount);
                    spawned.add(mount);
                }
            }
        }

        activeCaravanEntities.put(key, spawned);
    }

    private Entity createMount(String entityTypeId, ServerLevel world) {
        return switch (entityTypeId) {
            case "minecraft:horse" -> new Horse(EntityType.HORSE, world);
            case "minecraft:donkey" -> new Donkey(EntityType.DONKEY, world);
            case "minecraft:mule" -> new Mule(EntityType.MULE, world);
            case "minecraft:camel" -> new Camel(EntityType.CAMEL, world);
            case "minecraft:llama" -> new Llama(EntityType.LLAMA, world);
            default -> null;
        };
    }

    private BlockPos findSpawnPos(ServerLevel world, int chunkX, int chunkZ) {
        int bx = chunkX * 16 + 8;
        int bz = chunkZ * 16 + 8;
        int y = getSurfaceY(world, bx, bz);
        if (y > 0) return new BlockPos(bx, y, bz);
        return null;
    }

    private int getSurfaceY(net.minecraft.world.level.Level world, int x, int z) {
        return world.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
    }
}