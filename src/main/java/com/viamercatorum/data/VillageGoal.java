package com.viamercatorum.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record VillageGoal(
    String goalId,
    String name,
    String category,
    double priority,
    List<GoalCondition> conditions,
    List<GoalAction> actions
) {

    public record GoalCondition(
        String type,
        Optional<String> value,
        Optional<Double> threshold
    ) {
        public static final Codec<GoalCondition> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                Codec.STRING.fieldOf("type").forGetter(GoalCondition::type),
                Codec.STRING.optionalFieldOf("value").forGetter(GoalCondition::value),
                Codec.DOUBLE.optionalFieldOf("threshold").forGetter(GoalCondition::threshold)
            ).apply(inst, GoalCondition::new)
        );
    }

    public record GoalAction(
        String type,
        Optional<String> target,
        Optional<Double> intensity
    ) {
        public static final Codec<GoalAction> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                Codec.STRING.fieldOf("type").forGetter(GoalAction::type),
                Codec.STRING.optionalFieldOf("target").forGetter(GoalAction::target),
                Codec.DOUBLE.optionalFieldOf("intensity").forGetter(GoalAction::intensity)
            ).apply(inst, GoalAction::new)
        );
    }

    public static final Codec<VillageGoal> CODEC = RecordCodecBuilder.create(inst ->
        inst.group(
            Codec.STRING.fieldOf("goal_id").forGetter(VillageGoal::goalId),
            Codec.STRING.fieldOf("name").forGetter(VillageGoal::name),
            Codec.STRING.fieldOf("category").forGetter(VillageGoal::category),
            Codec.DOUBLE.optionalFieldOf("priority", 1.0).forGetter(VillageGoal::priority),
            GoalCondition.CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(VillageGoal::conditions),
            GoalAction.CODEC.listOf().optionalFieldOf("actions", List.of()).forGetter(VillageGoal::actions)
        ).apply(inst, VillageGoal::new)
    );
}