package com.viamercatorum.network;

import com.viamercatorum.ViaMercatorum;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record DialogPayload(
    String dialogId,
    String characterId,
    String characterName,
    String characterTypeId,
    String text,
    List<DialogChoice> choices
) implements CustomPacketPayload {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "dialog");
    public static final Type<DialogPayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, DialogPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> payload.write(buf), DialogPayload::read
    );

    public static DialogPayload read(RegistryFriendlyByteBuf buf) {
        String dialogId = buf.readUtf();
        String characterId = buf.readUtf();
        String characterName = buf.readUtf();
        String characterTypeId = buf.readUtf();
        String text = buf.readUtf();
        int choiceCount = buf.readVarInt();
        List<DialogChoice> choices = new ArrayList<>();
        for (int i = 0; i < choiceCount; i++) {
            choices.add(new DialogChoice(buf.readInt(), buf.readUtf(), buf.readUtf(), buf.readUtf()));
        }
        return new DialogPayload(dialogId, characterId, characterName, characterTypeId, text, choices);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(dialogId);
        buf.writeUtf(characterId);
        buf.writeUtf(characterName);
        buf.writeUtf(characterTypeId);
        buf.writeUtf(text);
        buf.writeVarInt(choices.size());
        for (var c : choices) {
            buf.writeInt(c.index()); buf.writeUtf(c.text()); buf.writeUtf(c.nextNodeId()); buf.writeUtf(c.action());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public record DialogChoice(int index, String text, String nextNodeId, String action) {}
}