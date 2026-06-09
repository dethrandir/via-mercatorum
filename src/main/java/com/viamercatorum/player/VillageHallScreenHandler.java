package com.viamercatorum.player;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.config.ViaMercatorumConfig;
import com.viamercatorum.core.economy.EconomyHelper;
import com.viamercatorum.core.economy.PriceCalculator;
import com.viamercatorum.core.economy.Supply;
import com.viamercatorum.core.economy.TradeGood;
import com.viamercatorum.core.economy.TradeGoodRegistry;
import com.viamercatorum.core.simulation.VillageGoalEngine;
import com.viamercatorum.core.world.MilitaryPower;
import com.viamercatorum.core.world.RouteGraph;
import com.viamercatorum.core.world.TradeRoute;
import com.viamercatorum.core.world.VillageNode;
import com.viamercatorum.core.world.WorkshopInstance;
import com.viamercatorum.core.world.WorkshopManager;
import com.viamercatorum.data.VillageGoal;
import com.viamercatorum.data.VillageGoalLoader;
import com.viamercatorum.data.WorkshopDefinitionLoader;
import com.viamercatorum.network.VillageHallDataPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class VillageHallScreenHandler extends AbstractContainerMenu {
    private String villageId;
    private VillageHallTab activeTab = VillageHallTab.TRADE;

    public static final int DATA_TAB = 0;
    public static final int DATA_POPULATION = 1;
    public static final int DATA_FOOD_RATIO = 2;
    public static final int DATA_REPUTATION = 3;
    public static final int DATA_PRODUCTION_SCALE = 4;
    public static final int DATA_MILITARY_POWER = 5;
    public static final int DATA_MOBILIZATION = 6;
    public static final int DATA_CAN_MOBILIZE = 7;
    public static final int DATA_CAN_DEMOBILIZE = 8;
    public static final int DATA_VILLAGE_TYPE = 9;
    public static final int DATA_IS_COASTAL = 10;
    public static final int DATA_IS_RAIDED = 11;
    public static final int DATA_SUPPLY_COUNT = 12;
    public static final int DATA_WORKSHOP_COUNT = 13;
    public static final int DATA_GOAL_COUNT = 14;
    public static final int DATA_MAX_POP = 15;
    public static final int DATA_GDP = 16;
    public static final int DATA_GDP_PER_CAPITA = 17;
    public static final int DATA_COUNT = 18;

    public final DataSlot[] dataSlots = new DataSlot[DATA_COUNT];

    private List<SupplyEntry> supplyEntries = List.of();
    private List<WorkshopEntry> workshopEntries = List.of();
    private List<TradeEntry> tradeEntries = List.of();
    private List<GoalEntry> goalEntries = List.of();
    private Map<String, Double> militaryBreakdown = Map.of();

    public record TradeEntry(String goodId, String name, double buyPrice, double sellPrice, double stock, double maxCapacity) {}
    public record SupplyEntry(String goodId, String name, double stock, double maxCapacity, double productionRate, double demand) {}
    public record WorkshopEntry(String instanceId, String workshopDefId, String displayName, int level, boolean enabled, boolean autoBuy, boolean autoSell, boolean isPlayerOwned) {}
    public record GoalEntry(String goalId, String name, String category, double priority, double urgencyScore, boolean conditionsMet) {}
    public record MilitaryEntry(String component, double value) {}

    public VillageHallScreenHandler(int syncId, Inventory inv) {
        this(syncId, inv, (String) null);
    }

    public VillageHallScreenHandler(int syncId, Inventory inv, String villageId) {
        super(ModScreenHandlers.VILLAGE_HALL, syncId);
        this.villageId = villageId;
        addPlayerSlots(inv);
        initializeDataSlots();
        if (villageId != null) {
            refreshData();
        }
    }

    private void addPlayerSlots(Inventory inv) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(inv, j + i * 9 + 9, 108 + j * 18, 140 + i * 18));
            }
        }
        for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(inv, j, 108 + j * 18, 198));
        }
    }

    private void initializeDataSlots() {
        for (int i = 0; i < DATA_COUNT; i++) {
            final int idx = i;
            dataSlots[i] = new DataSlot() {
                private int value;
                @Override
                public int get() { return value; }
                @Override
                public void set(int v) { value = v; }
            };
            addDataSlot(dataSlots[idx]);
        }
    }

    public void refreshData() {
        if (villageId == null) return;
        RouteGraph graph = ViaMercatorum.getInstance().getRouteGraph();
        VillageNode village = graph.getVillage(villageId);
        if (village == null) return;

        dataSlots[DATA_TAB].set(activeTab.getIndex());
        dataSlots[DATA_POPULATION].set((int) (village.getPopulation() * 1000));
        dataSlots[DATA_FOOD_RATIO].set((int) (village.getFoodRatio() * 1000));
        dataSlots[DATA_REPUTATION].set((int) (village.getReputation() * 100));
        dataSlots[DATA_PRODUCTION_SCALE].set((int) (village.calculateProductionScale() * 1000));
        dataSlots[DATA_MILITARY_POWER].set((int) (MilitaryPower.calculateVillagePower(village, graph) * 100));
        dataSlots[DATA_MOBILIZATION].set(village.getMobilization().ordinal());
        dataSlots[DATA_CAN_MOBILIZE].set(village.canMobilize(ViaMercatorum.getInstance().getSimulationTick().getTickCounter()) ? 1 : 0);
        dataSlots[DATA_CAN_DEMOBILIZE].set(village.canDemobilize(ViaMercatorum.getInstance().getSimulationTick().getTickCounter()) ? 1 : 0);
        dataSlots[DATA_VILLAGE_TYPE].set(village.getType().ordinal());
        dataSlots[DATA_IS_COASTAL].set(village.isCoastal() ? 1 : 0);
        dataSlots[DATA_IS_RAIDED].set(village.isRaided() ? 1 : 0);
        dataSlots[DATA_MAX_POP].set((int) (village.getMaxPopulation() * 1000));
        dataSlots[DATA_GDP].set((int) (EconomyHelper.gdp(village) * 100));
        dataSlots[DATA_GDP_PER_CAPITA].set((int) (EconomyHelper.gdpPerCapita(village) * 1000));

        supplyEntries = buildSupplyEntries(village);
        workshopEntries = buildWorkshopEntries(village);
        tradeEntries = buildTradeEntries(village, graph);
        goalEntries = buildGoalEntries(village, graph);
        militaryBreakdown = buildMilitaryBreakdown(village, graph);

        dataSlots[DATA_SUPPLY_COUNT].set(supplyEntries.size());
        dataSlots[DATA_WORKSHOP_COUNT].set(workshopEntries.size());
        dataSlots[DATA_GOAL_COUNT].set(goalEntries.size());
    }

    private List<SupplyEntry> buildSupplyEntries(VillageNode village) {
        List<SupplyEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Supply> entry : village.getSupplies().entrySet()) {
            Supply supply = entry.getValue();
            TradeGood good = supply.getGood();
            double demand = village.getLastDemand().getOrDefault(good.id(), 0.0);
            entries.add(new SupplyEntry(good.id(), good.name(), supply.getStock(),
                supply.getMaxCapacity(), supply.getProductionRate(), demand));
        }
        entries.sort(Comparator.comparing(SupplyEntry::name));
        return entries;
    }

    private List<TradeEntry> buildTradeEntries(VillageNode village, RouteGraph graph) {
        double reputation = village.getReputation();
        List<TradeEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Supply> entry : village.getSupplies().entrySet()) {
            Supply supply = entry.getValue();
            TradeGood good = supply.getGood();
            double demand = supply.getMaxCapacity() * 0.5;
            double buyPrice = PriceCalculator.calculateBuyPrice(good, supply.getStock(), demand, reputation);
            double sellPrice = PriceCalculator.calculateSellPrice(good, supply.getStock(), demand, reputation);
            entries.add(new TradeEntry(good.id(), good.name(), buyPrice, sellPrice,
                supply.getStock(), supply.getMaxCapacity()));
        }
        entries.sort(Comparator.comparing(TradeEntry::name));
        return entries;
    }

    private List<WorkshopEntry> buildWorkshopEntries(VillageNode village) {
        WorkshopManager wm = ViaMercatorum.getInstance().getWorkshopManager();
        WorkshopDefinitionLoader loader = ViaMercatorum.getInstance().getWorkshopDefinitionLoader();
        if (wm == null || loader == null) return List.of();

        List<WorkshopEntry> entries = new ArrayList<>();
        for (WorkshopInstance inst : wm.getWorkshopsForVillage(village.getId())) {
            var def = loader.getDefinition(inst.getWorkshopId());
            String displayName = def != null ? def.name() : inst.getWorkshopId();
            entries.add(new WorkshopEntry(inst.getInstanceId(), inst.getWorkshopId(), displayName,
                inst.getLevel(), inst.isEnabled(), inst.isAutoBuyMaterials(), inst.isAutoSellOutput(),
                inst.isPlayerOwned()));
        }
        return entries;
    }

    private List<GoalEntry> buildGoalEntries(VillageNode village, RouteGraph graph) {
        VillageGoalLoader loader = ViaMercatorum.getInstance().getVillageGoalLoader();
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        if (loader == null || loader.getGoals().isEmpty()) return List.of();

        List<GoalEntry> entries = new ArrayList<>();
        VillageGoalEngine engine = ViaMercatorum.getInstance().getSimulationTick().getGoalEngine();

        for (VillageGoal goal : loader.getGoals().values()) {
            boolean conditionsMet = engine != null && engine.evaluateConditions(goal.conditions(), village, graph, config);
            double urgency = engine != null ? engine.getUrgencyMultiplier(goal.category(), village, graph, config) : 1.0;
            entries.add(new GoalEntry(goal.goalId(), goal.name(), goal.category(),
                goal.priority(), urgency * goal.priority(), conditionsMet));
        }
        entries.sort(Comparator.comparingDouble(GoalEntry::urgencyScore).reversed());
        return entries;
    }

    private Map<String, Double> buildMilitaryBreakdown(VillageNode village, RouteGraph graph) {
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        Map<String, Double> breakdown = new LinkedHashMap<>();
        breakdown.put("population", village.getPopulation() * config.military.populationPowerFactor);
        breakdown.put("type_bonus", config.military.typePowerBonus.getOrDefault(village.getType().name(), 1.0));

        double weaponPower = 0;
        for (Map.Entry<String, Double> entry : config.military.weaponPowerFactor.entrySet()) {
            Supply supply = village.getSupply(entry.getKey());
            if (supply != null && supply.getStock() > 0) {
                weaponPower += Math.min(supply.getStock(), 10.0) * entry.getValue();
            }
        }
        breakdown.put("weapons", weaponPower);

        int guardCount = 0;
        for (var caravan : graph.getAllCaravans()) {
            if (caravan.getOriginNode().getId().equals(village.getId())
                || caravan.getDestinationNode().getId().equals(village.getId())) {
                guardCount += MilitaryPower.getGuardCountForType(village.getType());
            }
        }
        breakdown.put("guards", guardCount * config.military.caravanGuardFactor);

        double roadSecurity = 0;
        int routeCount = 0;
        for (TradeRoute route : graph.getAllRoutes()) {
            if (route.getFrom().getId().equals(village.getId()) || route.getTo().getId().equals(village.getId())) {
                roadSecurity += (1.0 - route.getPiracyScore());
                routeCount++;
            }
        }
        if (routeCount > 0) {
            breakdown.put("road_security", (roadSecurity / routeCount) * config.military.roadSecurityFactor * routeCount);
        }

        double mobilizeMultiplier = 1.0;
        if (village.getMobilization() == MilitaryPower.MobilizationState.MOBILIZED) {
            mobilizeMultiplier = config.military.mobilizeMultiplier;
        } else if (village.getMobilization() == MilitaryPower.MobilizationState.STANDBY) {
            mobilizeMultiplier = config.military.standbyMultiplier;
        }
        breakdown.put("mobilization_mult", mobilizeMultiplier);

        if (village.isRaided()) {
            breakdown.put("raided_penalty", config.military.raidedPenalty);
        }

        return breakdown;
    }

    public VillageHallTab getActiveTab() { return activeTab; }
    public String getVillageId() { return villageId; }
    public List<SupplyEntry> getSupplyEntries() { return supplyEntries; }
    public List<WorkshopEntry> getWorkshopEntries() { return workshopEntries; }
    public List<TradeEntry> getTradeEntries() { return tradeEntries; }
    public List<GoalEntry> getGoalEntries() { return goalEntries; }
    public Map<String, Double> getMilitaryBreakdown() { return militaryBreakdown; }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button >= 0 && button < VillageHallTab.values().length) {
            activeTab = VillageHallTab.fromIndex(button);
            refreshData();
            return true;
        }

        if (button >= 100 && button < 200) {
            int workshopIndex = button - 100;
            if (workshopIndex < workshopEntries.size()) {
                WorkshopEntry entry = workshopEntries.get(workshopIndex);
                WorkshopManager wm = ViaMercatorum.getInstance().getWorkshopManager();
                if (wm != null) {
                    WorkshopInstance inst = wm.getInstance(entry.instanceId());
                    if (inst != null) {
                        inst.setEnabled(!inst.isEnabled());
                        refreshData();
                        return true;
                    }
                }
            }
        }

        if (button == 200) {
            if (villageId != null) {
                VillageNode village = ViaMercatorum.getInstance().getRouteGraph().getVillage(villageId);
                if (village != null && village.canMobilize(ViaMercatorum.getInstance().getSimulationTick().getTickCounter())) {
                    if (village.getMobilization() == MilitaryPower.MobilizationState.DEMILITARIZED) {
                        village.setMobilization(MilitaryPower.MobilizationState.STANDBY,
                            ViaMercatorum.getInstance().getSimulationTick().getTickCounter());
                    } else if (village.getMobilization() == MilitaryPower.MobilizationState.STANDBY) {
                        village.setMobilization(MilitaryPower.MobilizationState.MOBILIZED,
                            ViaMercatorum.getInstance().getSimulationTick().getTickCounter());
                    }
                    refreshData();
                    return true;
                }
            }
        }

        if (button == 201) {
            if (villageId != null) {
                VillageNode village = ViaMercatorum.getInstance().getRouteGraph().getVillage(villageId);
                if (village != null && village.canDemobilize(ViaMercatorum.getInstance().getSimulationTick().getTickCounter())) {
                    village.setMobilization(MilitaryPower.MobilizationState.DEMILITARIZED,
                        ViaMercatorum.getInstance().getSimulationTick().getTickCounter());
                    refreshData();
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public void receiveDataFromServer(VillageHallDataPayload payload) {
        this.villageId = payload.villageId();
        this.supplyEntries = payload.supplies().stream()
            .map(e -> new SupplyEntry(e.goodId(), e.name(), e.stock(), e.maxCapacity(), e.productionRate(), e.demand()))
            .toList();
        this.workshopEntries = payload.workshops().stream()
            .map(e -> new WorkshopEntry(e.instanceId(), e.workshopDefId(), e.displayName(), e.level(), e.enabled(), e.autoBuy(), e.autoSell(), e.playerOwned()))
            .toList();
        this.tradeEntries = payload.trades().stream()
            .map(e -> new TradeEntry(e.goodId(), e.name(), e.buyPrice(), e.sellPrice(), e.stock(), e.maxCapacity()))
            .toList();
        this.goalEntries = payload.goals().stream()
            .map(e -> new GoalEntry(e.goalId(), e.name(), e.category(), e.priority(), e.urgencyScore(), e.conditionsMet()))
            .toList();
        this.militaryBreakdown = new LinkedHashMap<>();
        for (var e : payload.militaryBreakdown()) {
            militaryBreakdown.put(e.component(), e.value());
        }
    }

    public void sendFullDataToClient(ServerPlayer player) {
        VillageHallDataPayload payload = buildPayload();
        if (payload != null) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    private VillageHallDataPayload buildPayload() {
        if (villageId == null) return null;
        RouteGraph graph = ViaMercatorum.getInstance().getRouteGraph();
        VillageNode village = graph.getVillage(villageId);
        if (village == null) return null;
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();

        List<VillageHallDataPayload.SupplyEntry> supplyList = supplyEntries.stream()
            .map(e -> new VillageHallDataPayload.SupplyEntry(e.goodId(), e.name(), e.stock(), e.maxCapacity(), e.productionRate(), e.demand()))
            .toList();
        List<VillageHallDataPayload.WorkshopEntry> workshopList = workshopEntries.stream()
            .map(e -> new VillageHallDataPayload.WorkshopEntry(e.instanceId(), e.workshopDefId(), e.displayName(), e.level(), e.enabled(), e.autoBuy(), e.autoSell(), e.isPlayerOwned()))
            .toList();
        List<VillageHallDataPayload.TradeEntry> tradeList = tradeEntries.stream()
            .map(e -> new VillageHallDataPayload.TradeEntry(e.goodId(), e.name(), e.buyPrice(), e.sellPrice(), e.stock(), e.maxCapacity()))
            .toList();
        List<VillageHallDataPayload.GoalEntry> goalList = goalEntries.stream()
            .map(e -> new VillageHallDataPayload.GoalEntry(e.goalId(), e.name(), e.category(), e.priority(), e.urgencyScore(), e.conditionsMet()))
            .toList();
        List<VillageHallDataPayload.MilEntry> milList = militaryBreakdown.entrySet().stream()
            .map(e -> new VillageHallDataPayload.MilEntry(e.getKey(), e.getValue()))
            .toList();

        return new VillageHallDataPayload(
            villageId, village.getName(), village.getType().name(),
            village.getPopulation(), village.getMaxPopulation(),
            village.getFoodRatio(), village.getReputation(),
            village.calculateProductionScale(),
            EconomyHelper.gdp(village),
            MilitaryPower.calculateVillagePower(village, graph),
            village.getMobilization().name(),
            village.isCoastal(), village.isRaided(),
            supplyList, workshopList, tradeList, goalList, milList
        );
    }
}