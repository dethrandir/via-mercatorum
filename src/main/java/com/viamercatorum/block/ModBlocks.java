package com.viamercatorum.block;

import com.viamercatorum.ViaMercatorum;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public class ModBlocks {
    public static final VillageHallBlock VILLAGE_HALL = Registry.register(
        BuiltInRegistries.BLOCK,
        Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "village_hall"),
        new VillageHallBlock()
    );

    public static final RaidRemnantBlock RAID_REMNANT = Registry.register(
        BuiltInRegistries.BLOCK,
        Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "raid_remnant"),
        new RaidRemnantBlock()
    );

    public static void register() {
    }
}