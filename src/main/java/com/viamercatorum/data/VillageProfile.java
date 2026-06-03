package com.viamercatorum.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record VillageProfile(
    String biome,
    String type,
    double populationBase,
    double harvestModifier,
    List<ProductionEntry> specializations,
    List<TradeGoodEntry> tradeGoods,
    List<BaseProductionEntry> baseProduction
) {
    public enum VillageType {
        HAMLET, VILLAGE, TOWN, CITY, FORTIFIED
    }

    public record ProductionEntry(
        String goodId,
        double productionRate,
        double maxCapacity,
        double weight
    ) {
        public static final Codec<ProductionEntry> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                Codec.STRING.fieldOf("good_id").forGetter(ProductionEntry::goodId),
                Codec.DOUBLE.fieldOf("production_rate").forGetter(ProductionEntry::productionRate),
                Codec.DOUBLE.fieldOf("max_capacity").forGetter(ProductionEntry::maxCapacity),
                Codec.DOUBLE.optionalFieldOf("weight", 1.0).forGetter(ProductionEntry::weight)
            ).apply(inst, ProductionEntry::new)
        );
    }

    public record TradeGoodEntry(
        String goodId,
        double basePriceModifier,
        boolean isPerishable
    ) {
        public static final Codec<TradeGoodEntry> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                Codec.STRING.fieldOf("good_id").forGetter(TradeGoodEntry::goodId),
                Codec.DOUBLE.optionalFieldOf("base_price_modifier", 1.0).forGetter(TradeGoodEntry::basePriceModifier),
                Codec.BOOL.optionalFieldOf("perishable", false).forGetter(TradeGoodEntry::isPerishable)
            ).apply(inst, TradeGoodEntry::new)
        );
    }

    public record BaseProductionEntry(
        String goodId,
        double productionRate,
        double maxCapacity
    ) {
        public static final Codec<BaseProductionEntry> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                Codec.STRING.fieldOf("good_id").forGetter(BaseProductionEntry::goodId),
                Codec.DOUBLE.fieldOf("production_rate").forGetter(BaseProductionEntry::productionRate),
                Codec.DOUBLE.fieldOf("max_capacity").forGetter(BaseProductionEntry::maxCapacity)
            ).apply(inst, BaseProductionEntry::new)
        );
    }

    public static final Codec<VillageProfile> CODEC = RecordCodecBuilder.create(inst ->
        inst.group(
            Codec.STRING.fieldOf("biome").forGetter(VillageProfile::biome),
            Codec.STRING.fieldOf("type").forGetter(VillageProfile::type),
            Codec.DOUBLE.optionalFieldOf("population_base", 10.0).forGetter(VillageProfile::populationBase),
            Codec.DOUBLE.optionalFieldOf("harvest_modifier", 1.0).forGetter(VillageProfile::harvestModifier),
            ProductionEntry.CODEC.listOf().fieldOf("specializations").forGetter(VillageProfile::specializations),
            TradeGoodEntry.CODEC.listOf().fieldOf("trade_goods").forGetter(VillageProfile::tradeGoods),
            BaseProductionEntry.CODEC.listOf().optionalFieldOf("base_production", List.of()).forGetter(VillageProfile::baseProduction)
        ).apply(inst, VillageProfile::new)
    );
}
