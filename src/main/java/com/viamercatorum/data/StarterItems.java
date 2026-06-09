package com.viamercatorum.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record StarterItems(
    List<Entry> items,
    Optional<String> welcomeMessage
) {
    public static final Codec<StarterItems> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Entry.CODEC.listOf().fieldOf("items").forGetter(StarterItems::items),
        Codec.STRING.optionalFieldOf("welcome_message").forGetter(StarterItems::welcomeMessage)
    ).apply(instance, StarterItems::new));

    public record Entry(
        String itemId,
        int count
    ) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("item_id").forGetter(Entry::itemId),
            Codec.INT.fieldOf("count").forGetter(Entry::count)
        ).apply(instance, Entry::new));
    }
}