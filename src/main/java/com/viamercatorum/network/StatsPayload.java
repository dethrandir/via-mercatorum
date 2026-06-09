package com.viamercatorum.network;

import com.viamercatorum.ViaMercatorum;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record StatsPayload(
    int totalVillages,
    int totalRoutes,
    int totalCaravans,
    int totalCharacters,
    int leaderCount,
    int merchantCount,
    int caravanLeaderCount,
    double totalCharacterWealth,
    double avgFoodRatio,
    List<VillageStat> topVillages,
    List<CharacterStat> topCharacters,
    String mostPiratedRoute,
    double maxPiracy,
    String lowestFoodVillage,
    double lowestFoodRatio
) implements CustomPacketPayload {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "stats");
    public static final Type<StatsPayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, StatsPayload> STREAM_CODEC = StreamCodec.of(
        (buf, p) -> p.write(buf), StatsPayload::read
    );

    public record VillageStat(String name, String type, double population, double maxPop, double foodRatio, double gdp, double reputation) {}
    public record CharacterStat(String name, String type, String role, double wealth, double power, double reputation, String home, int workshopCount) {}

    public static StatsPayload read(RegistryFriendlyByteBuf buf) {
        int totalVillages = buf.readVarInt();
        int totalRoutes = buf.readVarInt();
        int totalCaravans = buf.readVarInt();
        int totalCharacters = buf.readVarInt();
        int leaderCount = buf.readVarInt();
        int merchantCount = buf.readVarInt();
        int caravanLeaderCount = buf.readVarInt();
        double totalCharWealth = buf.readDouble();
        double avgFoodRatio = buf.readDouble();

        int vsCount = buf.readVarInt();
        List<VillageStat> topVillages = new ArrayList<>();
        for (int i = 0; i < vsCount; i++) {
            topVillages.add(new VillageStat(buf.readUtf(), buf.readUtf(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }

        int csCount = buf.readVarInt();
        List<CharacterStat> topCharacters = new ArrayList<>();
        for (int i = 0; i < csCount; i++) {
            topCharacters.add(new CharacterStat(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readUtf(), buf.readVarInt()));
        }

        return new StatsPayload(totalVillages, totalRoutes, totalCaravans, totalCharacters,
            leaderCount, merchantCount, caravanLeaderCount, totalCharWealth, avgFoodRatio,
            topVillages, topCharacters,
            buf.readUtf(), buf.readDouble(), buf.readUtf(), buf.readDouble());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(totalVillages);
        buf.writeVarInt(totalRoutes);
        buf.writeVarInt(totalCaravans);
        buf.writeVarInt(totalCharacters);
        buf.writeVarInt(leaderCount);
        buf.writeVarInt(merchantCount);
        buf.writeVarInt(caravanLeaderCount);
        buf.writeDouble(totalCharacterWealth);
        buf.writeDouble(avgFoodRatio);

        buf.writeVarInt(topVillages.size());
        for (var v : topVillages) {
            buf.writeUtf(v.name()); buf.writeUtf(v.type()); buf.writeDouble(v.population()); buf.writeDouble(v.maxPop());
            buf.writeDouble(v.foodRatio()); buf.writeDouble(v.gdp()); buf.writeDouble(v.reputation());
        }

        buf.writeVarInt(topCharacters.size());
        for (var c : topCharacters) {
            buf.writeUtf(c.name()); buf.writeUtf(c.type()); buf.writeUtf(c.role()); buf.writeDouble(c.wealth());
            buf.writeDouble(c.power()); buf.writeDouble(c.reputation()); buf.writeUtf(c.home()); buf.writeVarInt(c.workshopCount());
        }

        buf.writeUtf(mostPiratedRoute);
        buf.writeDouble(maxPiracy);
        buf.writeUtf(lowestFoodVillage);
        buf.writeDouble(lowestFoodRatio);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}