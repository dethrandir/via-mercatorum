package com.viamercatorum.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ConsumptionProfile(
    String villageType,
    double populationScale,
    Map<String, Double> seasonModifiers,
    List<ConsumptionEntry> entries
) {

    public record ConsumptionEntry(
        Optional<String> goodId,
        Optional<String> category,
        double baseRate
    ) {
        public static final Codec<ConsumptionEntry> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                Codec.STRING.optionalFieldOf("good_id").forGetter(ConsumptionEntry::goodId),
                Codec.STRING.optionalFieldOf("category").forGetter(ConsumptionEntry::category),
                Codec.DOUBLE.fieldOf("base_rate").forGetter(ConsumptionEntry::baseRate)
            ).apply(inst, ConsumptionEntry::new)
        );
    }

    public static final Codec<ConsumptionProfile> CODEC = RecordCodecBuilder.create(inst ->
        inst.group(
            Codec.STRING.fieldOf("village_type").forGetter(ConsumptionProfile::villageType),
            Codec.DOUBLE.optionalFieldOf("population_scale", 1.0).forGetter(ConsumptionProfile::populationScale),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("season_modifiers", Map.of()).forGetter(ConsumptionProfile::seasonModifiers),
            ConsumptionEntry.CODEC.listOf().fieldOf("entries").forGetter(ConsumptionProfile::entries)
        ).apply(inst, ConsumptionProfile::new)
    );
}