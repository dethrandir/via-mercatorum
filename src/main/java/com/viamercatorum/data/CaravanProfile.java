package com.viamercatorum.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Map;

public record CaravanProfile(
    String biome,
    int baseVillagers,
    int baseGuards,
    Map<String, Integer> mounts,
    double speedMultiplier
) {
    public static final Codec<CaravanProfile> CODEC = RecordCodecBuilder.create(inst ->
        inst.group(
            Codec.STRING.fieldOf("biome").forGetter(CaravanProfile::biome),
            Codec.INT.optionalFieldOf("base_villagers", 3).forGetter(CaravanProfile::baseVillagers),
            Codec.INT.optionalFieldOf("base_guards", 2).forGetter(CaravanProfile::baseGuards),
            Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("mounts").forGetter(CaravanProfile::mounts),
            Codec.DOUBLE.optionalFieldOf("speed_multiplier", 1.0).forGetter(CaravanProfile::speedMultiplier)
        ).apply(inst, CaravanProfile::new)
    );
}
