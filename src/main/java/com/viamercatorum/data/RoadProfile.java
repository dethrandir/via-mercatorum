package com.viamercatorum.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record RoadProfile(
    String quality,
    double speedMultiplier,
    double trafficThreshold,
    SurfaceBlock surface,
    SurfaceBlock foundation,
    int width,
    List<String> requiredBiomes,
    Optional<String> nextQuality
) {
    public record SurfaceBlock(String block, Optional<String> slab) {
        public static final Codec<SurfaceBlock> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                Codec.STRING.fieldOf("block").forGetter(SurfaceBlock::block),
                Codec.STRING.optionalFieldOf("slab").forGetter(SurfaceBlock::slab)
            ).apply(inst, SurfaceBlock::new)
        );
    }

    public static final Codec<RoadProfile> CODEC = RecordCodecBuilder.create(inst ->
        inst.group(
            Codec.STRING.fieldOf("quality").forGetter(RoadProfile::quality),
            Codec.DOUBLE.fieldOf("speed_multiplier").forGetter(RoadProfile::speedMultiplier),
            Codec.DOUBLE.fieldOf("traffic_threshold").forGetter(RoadProfile::trafficThreshold),
            SurfaceBlock.CODEC.fieldOf("surface").forGetter(RoadProfile::surface),
            SurfaceBlock.CODEC.fieldOf("foundation").forGetter(RoadProfile::foundation),
            Codec.INT.optionalFieldOf("width", 1).forGetter(RoadProfile::width),
            Codec.STRING.listOf().optionalFieldOf("required_biomes", List.of()).forGetter(RoadProfile::requiredBiomes),
            Codec.STRING.optionalFieldOf("next_quality").forGetter(RoadProfile::nextQuality)
        ).apply(inst, RoadProfile::new)
    );
}
