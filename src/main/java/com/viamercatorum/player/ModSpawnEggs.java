package com.viamercatorum.player;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.entity.ModEntities;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;

public class ModSpawnEggs {

    public static final Item CARAVAN_SPAWN_EGG = registerEgg("caravan_spawn_egg", ModEntities.CARAVAN);
    public static final Item CARAVAN_GUARD_SPAWN_EGG = registerEgg("caravan_guard_spawn_egg", ModEntities.CARAVAN_GUARD);
    public static final Item CARAVAN_RAIDER_SPAWN_EGG = registerEgg("caravan_raider_spawn_egg", ModEntities.CARAVAN_RAIDER);
    public static final Item MERCHANT_SHIP_SPAWN_EGG = registerEgg("merchant_ship_spawn_egg", ModEntities.MERCHANT_SHIP);
    public static final Item LOAN_SHARK_SPAWN_EGG = registerEgg("loan_shark_spawn_egg", ModEntities.LOAN_SHARK);
    public static final Item CHARACTER_SPAWN_EGG = registerEgg("character_spawn_egg", ModEntities.CHARACTER);

    private static Item registerEgg(String name, EntityType<?> entityType) {
        Identifier id = Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, name);
        Item.Properties props = new Item.Properties()
            .component(DataComponents.ENTITY_DATA,
                TypedEntityData.of(entityType, new CompoundTag()));
        Item item = new SpawnEggItem(props);
        return Registry.register(BuiltInRegistries.ITEM, id, item);
    }

    public static void register() {
    }
}