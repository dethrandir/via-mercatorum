package com.viamercatorum.network;

import com.viamercatorum.ViaMercatorum;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record VillageHallDataPayload(
    String villageId,
    String villageName,
    String villageType,
    double population,
    double maxPopulation,
    double foodRatio,
    double reputation,
    double productionScale,
    double gdp,
    double militaryPower,
    String mobilization,
    boolean isCoastal,
    boolean isRaided,
    List<SupplyEntry> supplies,
    List<WorkshopEntry> workshops,
    List<TradeEntry> trades,
    List<GoalEntry> goals,
    List<MilEntry> militaryBreakdown
) implements CustomPacketPayload {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "village_hall_data");
    public static final Type<VillageHallDataPayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, VillageHallDataPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> payload.write(buf), VillageHallDataPayload::read
    );

    public static VillageHallDataPayload read(RegistryFriendlyByteBuf buf) {
        String villageId = buf.readUtf();
        String villageName = buf.readUtf();
        String villageType = buf.readUtf();
        double population = buf.readDouble();
        double maxPopulation = buf.readDouble();
        double foodRatio = buf.readDouble();
        double reputation = buf.readDouble();
        double productionScale = buf.readDouble();
        double gdp = buf.readDouble();
        double militaryPower = buf.readDouble();
        String mobilization = buf.readUtf();
        boolean isCoastal = buf.readBoolean();
        boolean isRaided = buf.readBoolean();

        int supplyCount = buf.readVarInt();
        List<SupplyEntry> supplies = new ArrayList<>();
        for (int i = 0; i < supplyCount; i++) {
            supplies.add(new SupplyEntry(buf.readUtf(), buf.readUtf(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }

        int workshopCount = buf.readVarInt();
        List<WorkshopEntry> workshops = new ArrayList<>();
        for (int i = 0; i < workshopCount; i++) {
            workshops.add(new WorkshopEntry(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readInt(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean()));
        }

        int tradeCount = buf.readVarInt();
        List<TradeEntry> trades = new ArrayList<>();
        for (int i = 0; i < tradeCount; i++) {
            trades.add(new TradeEntry(buf.readUtf(), buf.readUtf(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }

        int goalCount = buf.readVarInt();
        List<GoalEntry> goals = new ArrayList<>();
        for (int i = 0; i < goalCount; i++) {
            goals.add(new GoalEntry(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readDouble(), buf.readDouble(), buf.readBoolean()));
        }

        int milCount = buf.readVarInt();
        List<MilEntry> militaryBreakdown = new ArrayList<>();
        for (int i = 0; i < milCount; i++) {
            militaryBreakdown.add(new MilEntry(buf.readUtf(), buf.readDouble()));
        }

        return new VillageHallDataPayload(villageId, villageName, villageType, population, maxPopulation, foodRatio,
            reputation, productionScale, gdp, militaryPower, mobilization, isCoastal, isRaided,
            supplies, workshops, trades, goals, militaryBreakdown);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(villageId);
        buf.writeUtf(villageName);
        buf.writeUtf(villageType);
        buf.writeDouble(population);
        buf.writeDouble(maxPopulation);
        buf.writeDouble(foodRatio);
        buf.writeDouble(reputation);
        buf.writeDouble(productionScale);
        buf.writeDouble(gdp);
        buf.writeDouble(militaryPower);
        buf.writeUtf(mobilization);
        buf.writeBoolean(isCoastal);
        buf.writeBoolean(isRaided);

        buf.writeVarInt(supplies.size());
        for (var s : supplies) {
            buf.writeUtf(s.goodId()); buf.writeUtf(s.name()); buf.writeDouble(s.stock());
            buf.writeDouble(s.maxCapacity()); buf.writeDouble(s.productionRate()); buf.writeDouble(s.demand());
        }

        buf.writeVarInt(workshops.size());
        for (var w : workshops) {
            buf.writeUtf(w.instanceId()); buf.writeUtf(w.workshopDefId()); buf.writeUtf(w.displayName());
            buf.writeInt(w.level()); buf.writeBoolean(w.enabled()); buf.writeBoolean(w.autoBuy());
            buf.writeBoolean(w.autoSell()); buf.writeBoolean(w.playerOwned());
        }

        buf.writeVarInt(trades.size());
        for (var t : trades) {
            buf.writeUtf(t.goodId()); buf.writeUtf(t.name()); buf.writeDouble(t.buyPrice());
            buf.writeDouble(t.sellPrice()); buf.writeDouble(t.stock()); buf.writeDouble(t.maxCapacity());
        }

        buf.writeVarInt(goals.size());
        for (var g : goals) {
            buf.writeUtf(g.goalId()); buf.writeUtf(g.name()); buf.writeUtf(g.category());
            buf.writeDouble(g.priority()); buf.writeDouble(g.urgencyScore()); buf.writeBoolean(g.conditionsMet());
        }

        buf.writeVarInt(militaryBreakdown.size());
        for (var m : militaryBreakdown) {
            buf.writeUtf(m.component()); buf.writeDouble(m.value());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public record SupplyEntry(String goodId, String name, double stock, double maxCapacity, double productionRate, double demand) {}
    public record WorkshopEntry(String instanceId, String workshopDefId, String displayName, int level, boolean enabled, boolean autoBuy, boolean autoSell, boolean playerOwned) {}
    public record TradeEntry(String goodId, String name, double buyPrice, double sellPrice, double stock, double maxCapacity) {}
    public record GoalEntry(String goalId, String name, String category, double priority, double urgencyScore, boolean conditionsMet) {}
    public record MilEntry(String component, double value) {}
}