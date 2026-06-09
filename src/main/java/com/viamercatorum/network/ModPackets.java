package com.viamercatorum.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ModPackets {
    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(VillageHallDataPayload.TYPE, VillageHallDataPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(DialogPayload.TYPE, DialogPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(StatsPayload.TYPE, StatsPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(DialogChoicePayload.TYPE, DialogChoicePayload.STREAM_CODEC);
    }
}