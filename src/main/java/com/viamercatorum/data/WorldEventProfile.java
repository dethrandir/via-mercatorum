package com.viamercatorum.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record WorldEventProfile(
    String eventId,
    String name,
    String scope,
    double meanTimeToHappen,
    List<EventTrigger> triggers,
    List<EventEffect> effects,
    long cooldownTicks,
    Optional<String> prerequisiteEvent,
    double weight,
    boolean repeatable
) {

    public record EventTrigger(
        String type,
        Optional<String> value,
        Optional<Double> threshold,
        Optional<Double> chance
    ) {
        public static final Codec<EventTrigger> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                Codec.STRING.fieldOf("type").forGetter(EventTrigger::type),
                Codec.STRING.optionalFieldOf("value").forGetter(EventTrigger::value),
                Codec.DOUBLE.optionalFieldOf("threshold").forGetter(EventTrigger::threshold),
                Codec.DOUBLE.optionalFieldOf("chance").forGetter(EventTrigger::chance)
            ).apply(inst, EventTrigger::new)
        );
    }

    public record EventEffect(
        String type,
        Optional<String> target,
        Optional<Double> amount,
        Optional<Double> percentage,
        Optional<Long> durationTicks,
        Optional<Integer> severity
    ) {
        public static final Codec<EventEffect> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                Codec.STRING.fieldOf("type").forGetter(EventEffect::type),
                Codec.STRING.optionalFieldOf("target").forGetter(EventEffect::target),
                Codec.DOUBLE.optionalFieldOf("amount").forGetter(EventEffect::amount),
                Codec.DOUBLE.optionalFieldOf("percentage").forGetter(EventEffect::percentage),
                Codec.LONG.optionalFieldOf("duration_ticks").forGetter(EventEffect::durationTicks),
                Codec.INT.optionalFieldOf("severity").forGetter(EventEffect::severity)
            ).apply(inst, EventEffect::new)
        );
    }

    public static final Codec<WorldEventProfile> CODEC = RecordCodecBuilder.create(inst ->
        inst.group(
            Codec.STRING.fieldOf("event_id").forGetter(WorldEventProfile::eventId),
            Codec.STRING.fieldOf("name").forGetter(WorldEventProfile::name),
            Codec.STRING.fieldOf("scope").forGetter(WorldEventProfile::scope),
            Codec.DOUBLE.fieldOf("mean_time_to_happen").forGetter(WorldEventProfile::meanTimeToHappen),
            EventTrigger.CODEC.listOf().optionalFieldOf("triggers", List.of()).forGetter(WorldEventProfile::triggers),
            EventEffect.CODEC.listOf().fieldOf("effects").forGetter(WorldEventProfile::effects),
            Codec.LONG.optionalFieldOf("cooldown_ticks", 10000L).forGetter(WorldEventProfile::cooldownTicks),
            Codec.STRING.optionalFieldOf("prerequisite_event").forGetter(WorldEventProfile::prerequisiteEvent),
            Codec.DOUBLE.optionalFieldOf("weight", 1.0).forGetter(WorldEventProfile::weight),
            Codec.BOOL.optionalFieldOf("repeatable", true).forGetter(WorldEventProfile::repeatable)
        ).apply(inst, WorldEventProfile::new)
    );
}