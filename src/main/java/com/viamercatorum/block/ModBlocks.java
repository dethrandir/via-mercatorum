package com.viamercatorum.block;

import com.viamercatorum.ViaMercatorum;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public class ModBlocks {
    public static final MarketStallBlock MARKET_STALL = Registry.register(
        BuiltInRegistries.BLOCK,
        Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "market_stall"),
        new MarketStallBlock()
    );

    public static void register() {
    }
}
