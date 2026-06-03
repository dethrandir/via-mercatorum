package com.viamercatorum.entity;

import com.google.common.collect.ImmutableSet;
import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.block.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.village.poi.PoiType;

public class ModPOIs {
    public static final PoiType MARKET_STALL = Registry.register(
        BuiltInRegistries.POINT_OF_INTEREST_TYPE,
        Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "market_stall"),
        new PoiType(
            ImmutableSet.copyOf(ModBlocks.MARKET_STALL.getStateDefinition().getPossibleStates()),
            1, 1
        )
    );

    public static void register() {
    }
}
