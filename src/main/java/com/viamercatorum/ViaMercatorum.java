package com.viamercatorum;

import com.viamercatorum.block.ModBlockEntities;
import com.viamercatorum.block.ModBlocks;
import com.viamercatorum.core.config.ViaMercatorumConfig;
import com.viamercatorum.core.economy.TradeGoodRegistry;
import com.viamercatorum.core.simulation.SEKEventBus;
import com.viamercatorum.core.simulation.SimulationTick;
import com.viamercatorum.core.world.RouteGraph;
import com.viamercatorum.core.world.TradeRoute;
import com.viamercatorum.data.CaravanProfileLoader;
import com.viamercatorum.data.RoadProfileLoader;
import com.viamercatorum.data.VillageProfileLoader;
import com.viamercatorum.entity.ModEntities;
import com.viamercatorum.entity.ModPOIs;
import com.viamercatorum.entity.ModProfessions;
import com.viamercatorum.player.ModScreenHandlers;
import com.viamercatorum.player.ReputationSystem;
import com.viamercatorum.world.spawn.SpawnManager;
import com.viamercatorum.world.worldgen.VillageDetector;
import com.viamercatorum.world.worldgen.VillageModifier;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.ChunkPos;

import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViaMercatorum implements ModInitializer {
    public static final String MOD_ID = "viamercatorum";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static ViaMercatorum instance;
    private ViaMercatorumConfig config;
    private SEKEventBus eventBus;
    private SimulationTick simulationTick;
    private RouteGraph routeGraph;
    private SpawnManager spawnManager;
    private ReputationSystem reputationSystem;
    private VillageProfileLoader villageProfileLoader;
    private RoadProfileLoader roadProfileLoader;
    private CaravanProfileLoader caravanProfileLoader;
    private VillageDetector villageDetector;
    private VillageModifier villageModifier;
    private MinecraftServer server;

    public static ViaMercatorum getInstance() {
        return instance;
    }

    @Override
    public void onInitialize() {
        instance = this;
        LOGGER.info("Via Mercatorum — ticaret yollari canlaniyor...");

        ModEntities.register();
        ModProfessions.register();
        ModPOIs.register();
        ModBlocks.register();
        ModBlockEntities.register();
        ModScreenHandlers.register();

        TradeGoodRegistry.getAll();

        this.config = ViaMercatorumConfig.load(Path.of("config"));
        this.eventBus = new SEKEventBus();
        this.routeGraph = new RouteGraph();
        this.simulationTick = new SimulationTick(routeGraph);
        this.spawnManager = new SpawnManager(routeGraph, simulationTick);
        this.reputationSystem = new ReputationSystem();
        this.villageDetector = new VillageDetector();
        this.villageModifier = new VillageModifier();

        this.villageProfileLoader = new VillageProfileLoader();
        this.roadProfileLoader = new RoadProfileLoader();
        this.caravanProfileLoader = new CaravanProfileLoader();

        ResourceManagerHelper.get(PackType.SERVER_DATA)
            .registerReloadListener(villageProfileLoader);
        ResourceManagerHelper.get(PackType.SERVER_DATA)
            .registerReloadListener(roadProfileLoader);
        ResourceManagerHelper.get(PackType.SERVER_DATA)
            .registerReloadListener(caravanProfileLoader);

        registerCommands();

        simulationTick.start();

        ServerChunkEvents.CHUNK_GENERATE.register((world, chunk) -> {
            if (!chunk.getAllStarts().isEmpty()) {
                for (var entry : chunk.getAllStarts().entrySet()) {
                    villageDetector.onStructureStart(world, entry.getValue());
                }
            }

            ChunkPos pos = chunk.getPos();
            int cx = pos.getMiddleBlockX() >> 4;
            int cz = pos.getMiddleBlockZ() >> 4;

            for (TradeRoute route : routeGraph.getAllRoutes()) {
                villageModifier.cacheRoadPath(route, world);
            }

            villageModifier.placeRoadsInChunk(world, cx, cz);
        });

        ServerChunkEvents.CHUNK_LOAD.register((world, chunk, isNew) -> {
            ChunkPos pos = chunk.getPos();
            int cx = pos.getMiddleBlockX() >> 4;
            int cz = pos.getMiddleBlockZ() >> 4;
            spawnManager.onChunkLoaded(world, cx, cz);
        });

        ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            ChunkPos pos = chunk.getPos();
            int cx = pos.getMiddleBlockX() >> 4;
            int cz = pos.getMiddleBlockZ() >> 4;
            spawnManager.onChunkUnloaded(world, cx, cz);
        });

        LOGGER.info("Via Mercatorum hazir. Hadi ticaret baslasin!");
    }

    private void registerCommands() {
    }

    public ViaMercatorumConfig getConfig() { return config; }
    public SEKEventBus getEventBus() { return eventBus; }
    public SimulationTick getSimulationTick() { return simulationTick; }
    public RouteGraph getRouteGraph() { return routeGraph; }
    public SpawnManager getSpawnManager() { return spawnManager; }
    public ReputationSystem getReputationSystem() { return reputationSystem; }
    public void setServer(MinecraftServer server) { this.server = server; }
    public MinecraftServer getServer() { return server; }
    public VillageProfileLoader getVillageProfileLoader() { return villageProfileLoader; }
    public RoadProfileLoader getRoadProfileLoader() { return roadProfileLoader; }
    public CaravanProfileLoader getCaravanProfileLoader() { return caravanProfileLoader; }
    public VillageDetector getVillageDetector() { return villageDetector; }
    public VillageModifier getVillageModifier() { return villageModifier; }
}
