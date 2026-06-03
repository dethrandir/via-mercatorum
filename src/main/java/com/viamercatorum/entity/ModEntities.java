package com.viamercatorum.entity;

import com.viamercatorum.ViaMercatorum;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    private static final Identifier CARAVAN_ID = Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "caravan");
    private static final Identifier CARAVAN_GUARD_ID = Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "caravan_guard");

    public static final EntityType<CaravanEntity> CARAVAN = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        CARAVAN_ID,
        FabricEntityTypeBuilder.createMob()
            .entityFactory(CaravanEntity::new)
            .defaultAttributes(CaravanEntity::createAttributes)
            .dimensions(EntityType.VILLAGER.getDimensions())
            .build(ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), CARAVAN_ID))
    );

    public static final EntityType<CaravanGuardEntity> CARAVAN_GUARD = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        CARAVAN_GUARD_ID,
        FabricEntityTypeBuilder.createMob()
            .entityFactory(CaravanGuardEntity::new)
            .defaultAttributes(CaravanGuardEntity::createAttributes)
            .dimensions(EntityType.VILLAGER.getDimensions())
            .build(ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), CARAVAN_GUARD_ID))
    );

    public static void register() {
    }
}
