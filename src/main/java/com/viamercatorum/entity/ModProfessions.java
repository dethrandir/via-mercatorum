package com.viamercatorum.entity;

import com.google.common.collect.ImmutableSet;
import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.block.ModBlocks;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

public class ModProfessions {
    private static final Identifier MERCHANT_ID = Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "merchant");
    private static final Identifier GUARD_ID = Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "caravan_guard");
    private static final ResourceKey<VillagerProfession> MERCHANT_KEY = ResourceKey.create(
        BuiltInRegistries.VILLAGER_PROFESSION.key(), MERCHANT_ID
    );
    private static final ResourceKey<VillagerProfession> GUARD_KEY = ResourceKey.create(
        BuiltInRegistries.VILLAGER_PROFESSION.key(), GUARD_ID
    );

    public static final VillagerProfession MERCHANT = Registry.register(
        BuiltInRegistries.VILLAGER_PROFESSION,
        MERCHANT_KEY,
        new VillagerProfession(
            Component.translatable("entity.viamercatorum.merchant"),
            holder -> holder.value().equals(ModPOIs.MARKET_STALL),
            holder -> holder.value().equals(ModPOIs.MARKET_STALL),
            ImmutableSet.of(),
            ImmutableSet.of(ModBlocks.MARKET_STALL),
            SoundEvents.VILLAGER_WORK_ARMORER,
            Int2ObjectMaps.emptyMap()
        )
    );

    public static final VillagerProfession CARAVAN_GUARD = Registry.register(
        BuiltInRegistries.VILLAGER_PROFESSION,
        GUARD_KEY,
        new VillagerProfession(
            Component.translatable("entity.viamercatorum.caravan_guard"),
            holder -> false,
            holder -> false,
            ImmutableSet.of(),
            ImmutableSet.of(),
            SoundEvents.VILLAGER_WORK_WEAPONSMITH,
            Int2ObjectMaps.emptyMap()
        )
    );

    public static void register() {
    }
}
