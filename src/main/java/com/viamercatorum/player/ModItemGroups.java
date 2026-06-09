package com.viamercatorum.player;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.entity.ModEntities;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

public class ModItemGroups {
    public static final CreativeModeTab MAIN = Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "main"),
        CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .icon(() -> new ItemStack(ModItems.MERCHANT_MAP))
            .title(Component.translatable("itemGroup.viamercatorum.main"))
            .displayItems((params, output) -> {
                output.accept(ModItems.MERCHANT_MAP);
                output.accept(ModItems.NEWSPAPER);
                output.accept(ModItems.DEBT_NOTE);
                output.accept(ModItems.STATS_ITEM);
                output.accept(ModItems.VILLAGE_HALL_ITEM);
                output.accept(ModItems.RAID_REMNANT_ITEM);
            })
            .build()
    );

    public static final CreativeModeTab SPAWN_EGGS = Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "spawn_eggs"),
        CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1)
            .icon(() -> new ItemStack(ModSpawnEggs.CARAVAN_SPAWN_EGG))
            .title(Component.translatable("itemGroup.viamercatorum.spawn_eggs"))
            .displayItems((params, output) -> {
                output.accept(ModSpawnEggs.CARAVAN_SPAWN_EGG);
                output.accept(ModSpawnEggs.CARAVAN_GUARD_SPAWN_EGG);
                output.accept(ModSpawnEggs.CARAVAN_RAIDER_SPAWN_EGG);
                output.accept(ModSpawnEggs.MERCHANT_SHIP_SPAWN_EGG);
                output.accept(ModSpawnEggs.LOAN_SHARK_SPAWN_EGG);
                output.accept(ModSpawnEggs.CHARACTER_SPAWN_EGG);
            })
            .build()
    );

    public static void register() {
    }
}