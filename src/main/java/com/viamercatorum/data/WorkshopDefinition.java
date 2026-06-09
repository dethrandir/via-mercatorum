package com.viamercatorum.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record WorkshopDefinition(
    String workshopId,
    String name,
    List<GoodAmount> inputGoods,
    List<GoodAmount> outputGoods,
    List<GoodAmount> constructionCost,
    double emeraldConstructionCost,
    double tickCostEmerald,
    int productionTickInterval,
    int maxLevel,
    double upgradeCostMultiplier,
    double maxStoredInput,
    double maxAccumulatedOutput
) {
    public static final Codec<WorkshopDefinition> CODEC = RecordCodecBuilder.create(inst ->
        inst.group(
            Codec.STRING.fieldOf("workshop_id").forGetter(WorkshopDefinition::workshopId),
            Codec.STRING.fieldOf("name").forGetter(WorkshopDefinition::name),
            GoodAmount.CODEC.listOf().fieldOf("input_goods").forGetter(WorkshopDefinition::inputGoods),
            GoodAmount.CODEC.listOf().fieldOf("output_goods").forGetter(WorkshopDefinition::outputGoods),
            GoodAmount.CODEC.listOf().optionalFieldOf("construction_cost", List.of()).forGetter(WorkshopDefinition::constructionCost),
            Codec.DOUBLE.optionalFieldOf("emerald_construction_cost", 0.0).forGetter(WorkshopDefinition::emeraldConstructionCost),
            Codec.DOUBLE.optionalFieldOf("tick_cost_emerald", 0.0).forGetter(WorkshopDefinition::tickCostEmerald),
            Codec.INT.optionalFieldOf("production_tick_interval", 100).forGetter(WorkshopDefinition::productionTickInterval),
            Codec.INT.optionalFieldOf("max_level", 3).forGetter(WorkshopDefinition::maxLevel),
            Codec.DOUBLE.optionalFieldOf("upgrade_cost_multiplier", 2.0).forGetter(WorkshopDefinition::upgradeCostMultiplier),
            Codec.DOUBLE.optionalFieldOf("max_stored_input", 200.0).forGetter(WorkshopDefinition::maxStoredInput),
            Codec.DOUBLE.optionalFieldOf("max_accumulated_output", 200.0).forGetter(WorkshopDefinition::maxAccumulatedOutput)
        ).apply(inst, WorkshopDefinition::new)
    );
}