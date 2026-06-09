package com.viamercatorum.network;

import com.viamercatorum.ViaMercatorum;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DialogChoicePayload(
    String dialogId,
    String characterId,
    int choiceIndex
) implements CustomPacketPayload {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "dialog_choice");
    public static final Type<DialogChoicePayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, DialogChoicePayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> payload.write(buf), DialogChoicePayload::read
    );

    public static DialogChoicePayload read(RegistryFriendlyByteBuf buf) {
        return new DialogChoicePayload(buf.readUtf(), buf.readUtf(), buf.readInt());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(dialogId);
        buf.writeUtf(characterId);
        buf.writeInt(choiceIndex);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}