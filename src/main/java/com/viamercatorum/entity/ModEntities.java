package com.viamercatorum.entity;

import com.viamercatorum.ViaMercatorum;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;

public class ModEntities {
    private static final Identifier CARAVAN_ID = Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "caravan");
    private static final Identifier CARAVAN_GUARD_ID = Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "caravan_guard");
    private static final Identifier CARAVAN_RAIDER_ID = Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "caravan_raider");
    private static final Identifier MERCHANT_SHIP_ID = Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "merchant_ship");
    private static final Identifier LOAN_SHARK_ID = Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "loan_shark");
    private static final Identifier CHARACTER_ID = Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "character");

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

    public static final EntityType<CaravanRaiderEntity> CARAVAN_RAIDER = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        CARAVAN_RAIDER_ID,
        FabricEntityTypeBuilder.createMob()
            .entityFactory(CaravanRaiderEntity::new)
            .defaultAttributes(CaravanRaiderEntity::createAttributes)
            .dimensions(EntityType.PILLAGER.getDimensions())
            .build(ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), CARAVAN_RAIDER_ID))
    );

    public static final EntityType<MerchantShipEntity> MERCHANT_SHIP = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        MERCHANT_SHIP_ID,
        FabricEntityTypeBuilder.createMob()
            .entityFactory(MerchantShipEntity::new)
            .defaultAttributes(MerchantShipEntity::createAttributes)
            .dimensions(EntityDimensions.fixed(1.5f, 0.6f))
            .build(ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), MERCHANT_SHIP_ID))
    );

    public static final EntityType<LoanSharkEntity> LOAN_SHARK = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        LOAN_SHARK_ID,
        FabricEntityTypeBuilder.createMob()
            .entityFactory(LoanSharkEntity::new)
            .defaultAttributes(LoanSharkEntity::createAttributes)
            .dimensions(EntityDimensions.fixed(0.6f, 1.95f))
            .build(ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), LOAN_SHARK_ID))
    );

    public static final EntityType<CharacterEntity> CHARACTER = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        CHARACTER_ID,
        FabricEntityTypeBuilder.createMob()
            .entityFactory(CharacterEntity::new)
            .defaultAttributes(CharacterEntity::createAttributes)
            .dimensions(EntityType.VILLAGER.getDimensions())
            .build(ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), CHARACTER_ID))
    );

    public static void register() {
    }
}