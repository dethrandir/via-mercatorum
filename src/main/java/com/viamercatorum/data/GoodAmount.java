package com.viamercatorum.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record GoodAmount(
    String goodId,
    double amount
) {
    public static final Codec<GoodAmount> CODEC = RecordCodecBuilder.create(inst ->
        inst.group(
            Codec.STRING.fieldOf("good_id").forGetter(GoodAmount::goodId),
            Codec.DOUBLE.fieldOf("amount").forGetter(GoodAmount::amount)
        ).apply(inst, GoodAmount::new)
    );
}