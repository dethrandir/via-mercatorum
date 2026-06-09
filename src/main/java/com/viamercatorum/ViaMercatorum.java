package com.viamercatorum;

import com.viamercatorum.block.ModBlockEntities;
import com.viamercatorum.block.ModBlocks;
import com.viamercatorum.core.config.ViaMercatorumConfig;
import com.viamercatorum.core.economy.MarketRegionManager;
import com.viamercatorum.core.economy.BankManager;
import com.viamercatorum.core.economy.TradeGoodRegistry;
import com.viamercatorum.core.simulation.CharacterManager;
import com.viamercatorum.core.simulation.DialogManager;
import com.viamercatorum.core.simulation.SEKPreloader;
import com.viamercatorum.core.simulation.SEKEventBus;
import com.viamercatorum.data.DialogTree;
import com.viamercatorum.network.DialogChoicePayload;
import com.viamercatorum.network.DialogPayload;
import com.viamercatorum.network.ModPackets;
import com.viamercatorum.core.simulation.SimulationTick;
import com.viamercatorum.core.world.RouteGraph;
import com.viamercatorum.core.world.TradeRoute;
import com.viamercatorum.data.CaravanProfileLoader;
import com.viamercatorum.data.ConsumptionProfileLoader;
import com.viamercatorum.data.DialogTreeLoader;
import com.viamercatorum.data.LeaderProfileLoader;
import com.viamercatorum.data.NamePoolLoader;
import com.viamercatorum.data.RoadProfileLoader;
import com.viamercatorum.data.StarterItems;
import com.viamercatorum.data.StarterItemsLoader;
import com.viamercatorum.data.VillageProfileLoader;
import com.viamercatorum.data.WorkshopDefinitionLoader;
import com.viamercatorum.data.WorldEventProfileLoader;
import com.viamercatorum.data.VillageGoalLoader;
import com.viamercatorum.core.world.WorkshopManager;
import com.viamercatorum.entity.ModEntities;
import com.viamercatorum.entity.LoanSharkEntity;
import com.viamercatorum.entity.ModPOIs;
import com.viamercatorum.entity.ModProfessions;
import com.viamercatorum.player.MerchantMap;
import com.viamercatorum.player.ModItemGroups;
import com.viamercatorum.player.ModScreenHandlers;
import com.viamercatorum.player.ModItems;
import com.viamercatorum.player.ModSpawnEggs;
import com.viamercatorum.player.NewspaperSystem;
import com.viamercatorum.player.ReputationSystem;
import com.viamercatorum.world.spawn.RaidRemnantManager;
import com.viamercatorum.world.spawn.SpawnManager;
import com.viamercatorum.world.sync.RoadSpeedHandler;
import com.viamercatorum.world.worldgen.VillageDetector;
import com.viamercatorum.world.worldgen.VillageModifier;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
    private RaidRemnantManager raidRemnantManager;
    private ReputationSystem reputationSystem;
    private MarketRegionManager marketRegionManager;
    private RoadSpeedHandler roadSpeedHandler;
    private BankManager bankManager;
    private NewspaperSystem newspaperSystem;
    private MerchantMap merchantMap;
    private VillageProfileLoader villageProfileLoader;
    private RoadProfileLoader roadProfileLoader;
    private CaravanProfileLoader caravanProfileLoader;
    private ConsumptionProfileLoader consumptionProfileLoader;
    private WorldEventProfileLoader worldEventProfileLoader;
    private WorkshopDefinitionLoader workshopDefinitionLoader;
    private WorkshopManager workshopManager;
    private VillageGoalLoader villageGoalLoader;
    private DialogTreeLoader dialogTreeLoader;
    private LeaderProfileLoader leaderProfileLoader;
    private NamePoolLoader namePoolLoader;
    private StarterItemsLoader starterItemsLoader;
    private final java.util.Set<java.util.UUID> firstJoinPlayers = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private VillageDetector villageDetector;
    private VillageModifier villageModifier;
    private CharacterManager characterManager;
    private DialogManager dialogManager;
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
        ModItems.register();
        ModItemGroups.register();
        ModSpawnEggs.register();
        ModPackets.register();

        ServerPlayNetworking.registerGlobalReceiver(DialogChoicePayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                DialogManager dialogManager = getInstance().getDialogManager();
                DialogManager.ActiveDialog active = dialogManager.getActiveDialog(player.getUUID());
                if (active == null) return;
                if (!active.dialogId().equals(payload.dialogId())) return;
                if (!active.characterId().equals(payload.characterId())) return;

                DialogTree tree = getInstance().getDialogTreeLoader().getDialogTree(active.dialogId());
                if (tree == null) return;

                DialogManager.ActiveDialog next = dialogManager.chooseOption(player.getUUID(), payload.choiceIndex(), tree);
                if (next == null) {
                    dialogManager.endDialog(player.getUUID());
                    return;
                }

                var nodeOpt = tree.nodes().stream()
                    .filter(n -> n.nodeId().equals(next.currentNodeId()))
                    .findFirst();
                if (nodeOpt.isEmpty()) {
                    dialogManager.endDialog(player.getUUID());
                    return;
                }

                var node = nodeOpt.get();
                var character = getInstance().getCharacterManager().getCharacter(active.characterId());
                String charName = character != null ? character.getName() : "???";
                String charType = character != null ? character.getCharacterTypeId() : "";

                java.util.List<DialogPayload.DialogChoice> choices = node.choices().stream()
                    .map(c -> new DialogPayload.DialogChoice(c.index(), c.text(), c.nextNodeId(), c.action().orElse("")))
                    .toList();

                DialogPayload response = new DialogPayload(
                    tree.treeId(), active.characterId(), charName, charType,
                    node.text(), choices
                );
                ServerPlayNetworking.send((net.minecraft.server.level.ServerPlayer) player, response);
            });
        });

        TradeGoodRegistry.getAll();

        this.config = ViaMercatorumConfig.load(Path.of("config"));
        this.eventBus = new SEKEventBus();
        this.routeGraph = new RouteGraph();
        this.simulationTick = new SimulationTick(routeGraph);
        this.spawnManager = new SpawnManager(routeGraph, simulationTick);
        this.raidRemnantManager = new RaidRemnantManager();
        this.reputationSystem = new ReputationSystem();
        this.marketRegionManager = new MarketRegionManager();
        this.roadSpeedHandler = new RoadSpeedHandler();
        this.bankManager = new BankManager();
        this.newspaperSystem = new NewspaperSystem();
        this.merchantMap = new MerchantMap();
        this.villageDetector = new VillageDetector();
        this.villageModifier = new VillageModifier();
        this.characterManager = new CharacterManager();
        this.dialogManager = new DialogManager();

        this.villageProfileLoader = new VillageProfileLoader();
        this.roadProfileLoader = new RoadProfileLoader();
        this.caravanProfileLoader = new CaravanProfileLoader();
        this.consumptionProfileLoader = new ConsumptionProfileLoader();
        this.worldEventProfileLoader = new WorldEventProfileLoader();
        this.workshopDefinitionLoader = new WorkshopDefinitionLoader();
        this.workshopManager = new WorkshopManager();
        this.villageGoalLoader = new VillageGoalLoader();
        this.dialogTreeLoader = new DialogTreeLoader();
        this.leaderProfileLoader = new LeaderProfileLoader();
        this.namePoolLoader = new NamePoolLoader();
        this.starterItemsLoader = new StarterItemsLoader();

        ResourceManagerHelper.get(PackType.SERVER_DATA)
            .registerReloadListener(villageProfileLoader);
        ResourceManagerHelper.get(PackType.SERVER_DATA)
            .registerReloadListener(roadProfileLoader);
        ResourceManagerHelper.get(PackType.SERVER_DATA)
            .registerReloadListener(caravanProfileLoader);
        ResourceManagerHelper.get(PackType.SERVER_DATA)
            .registerReloadListener(consumptionProfileLoader);
        ResourceManagerHelper.get(PackType.SERVER_DATA)
            .registerReloadListener(worldEventProfileLoader);
        ResourceManagerHelper.get(PackType.SERVER_DATA)
            .registerReloadListener(workshopDefinitionLoader);
        ResourceManagerHelper.get(PackType.SERVER_DATA)
            .registerReloadListener(villageGoalLoader);
        ResourceManagerHelper.get(PackType.SERVER_DATA)
            .registerReloadListener(dialogTreeLoader);
        ResourceManagerHelper.get(PackType.SERVER_DATA)
            .registerReloadListener(leaderProfileLoader);
        ResourceManagerHelper.get(PackType.SERVER_DATA)
            .registerReloadListener(namePoolLoader);
        ResourceManagerHelper.get(PackType.SERVER_DATA)
            .registerReloadListener(starterItemsLoader);

        registerCommands();

        simulationTick.start();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            this.server = server;
            SEKPreloader.preload(server.overworld(), routeGraph, simulationTick,
                characterManager, config);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            if (config.starterGifts.enabled && firstJoinPlayers.add(player.getUUID())) {
                StarterItems items = starterItemsLoader.getItems();
                for (StarterItems.Entry entry : items.items()) {
                    Identifier itemId = Identifier.tryParse(entry.itemId());
                    if (itemId != null) {
                        Item item = BuiltInRegistries.ITEM.get(itemId).map(r -> r.value()).orElse(null);
                        if (item != null && item != Items.AIR) {
                            player.getInventory().add(new ItemStack(item, entry.count()));
                        }
                    }
                }
                if (config.starterGifts.showWelcomeMessage && items.welcomeMessage().isPresent()) {
                    player.sendSystemMessage(Component.literal(items.welcomeMessage().get()));
                }
            }
        });

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

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            this.server = server;
            raidRemnantManager.tick(server.overworld());
            roadSpeedHandler.tick(server.overworld());
            spawnLoanSharks(server.overworld());
        });

        LOGGER.info("Via Mercatorum hazir. Hadi ticaret baslasin!");
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            com.viamercatorum.command.ViaMercatorumCommand.register(dispatcher);
        });
    }

    private void spawnLoanSharks(net.minecraft.server.level.ServerLevel world) {
        var config = this.config;
        if (world.getGameTime() % 200 != 0) return;
        if (world.players().isEmpty()) return;

        var bankManager = this.bankManager;
        for (var player : world.players()) {
            if (bankManager.hasOverdueLoan(player.getStringUUID())) {
                double chance = Math.min(config.bank.assassinSpawnChance, 0.05);
                var random = net.minecraft.util.RandomSource.create();
                if (random.nextDouble() < chance) {
                    double angle = random.nextDouble() * Math.PI * 2;
                    double dist = 20 + random.nextDouble() * 15;
                    double spawnX = player.getX() + Math.cos(angle) * dist;
                    double spawnZ = player.getZ() + Math.sin(angle) * dist;
                    double spawnY = player.getY() + 5;

                    var shark = new LoanSharkEntity(ModEntities.LOAN_SHARK, world);
                    shark.setPos(spawnX, spawnY, spawnZ);
                    world.addFreshEntity(shark);

                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cYou sense someone watching you... Armed men are asking around about you.")
                        .withStyle(net.minecraft.ChatFormatting.RED));
                }
                break;
            }
        }
    }

    public ViaMercatorumConfig getConfig() { return config; }
    public void reloadConfig() { this.config = ViaMercatorumConfig.load(Path.of("config")); }
    public SEKEventBus getEventBus() { return eventBus; }
    public SimulationTick getSimulationTick() { return simulationTick; }
    public RouteGraph getRouteGraph() { return routeGraph; }
    public SpawnManager getSpawnManager() { return spawnManager; }
    public RaidRemnantManager getRaidRemnantManager() { return raidRemnantManager; }
    public ReputationSystem getReputationSystem() { return reputationSystem; }
    public MarketRegionManager getMarketRegionManager() { return marketRegionManager; }
    public void setServer(MinecraftServer server) { this.server = server; }
    public MinecraftServer getServer() { return server; }
    public RoadSpeedHandler getRoadSpeedHandler() { return roadSpeedHandler; }
    public BankManager getBankManager() { return bankManager; }
    public NewspaperSystem getNewspaperSystem() { return newspaperSystem; }
    public MerchantMap getMerchantMap() { return merchantMap; }
    public VillageProfileLoader getVillageProfileLoader() { return villageProfileLoader; }
    public RoadProfileLoader getRoadProfileLoader() { return roadProfileLoader; }
    public CaravanProfileLoader getCaravanProfileLoader() { return caravanProfileLoader; }
    public ConsumptionProfileLoader getConsumptionProfileLoader() { return consumptionProfileLoader; }
    public WorldEventProfileLoader getWorldEventProfileLoader() { return worldEventProfileLoader; }
    public WorkshopDefinitionLoader getWorkshopDefinitionLoader() { return workshopDefinitionLoader; }
    public WorkshopManager getWorkshopManager() { return workshopManager; }
    public VillageGoalLoader getVillageGoalLoader() { return villageGoalLoader; }
    public VillageDetector getVillageDetector() { return villageDetector; }
    public VillageModifier getVillageModifier() { return villageModifier; }
    public CharacterManager getCharacterManager() { return characterManager; }
    public DialogManager getDialogManager() { return dialogManager; }
    public DialogTreeLoader getDialogTreeLoader() { return dialogTreeLoader; }
    public LeaderProfileLoader getLeaderProfileLoader() { return leaderProfileLoader; }
    public NamePoolLoader getNamePoolLoader() { return namePoolLoader; }
    public StarterItemsLoader getStarterItemsLoader() { return starterItemsLoader; }
}