package com.viamercatorum.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record NamePool(
    String poolId,
    List<String> names
) {
    public static final Codec<NamePool> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("pool_id").forGetter(NamePool::poolId),
        Codec.STRING.listOf().fieldOf("names").forGetter(NamePool::names)
    ).apply(instance, NamePool::new));
}