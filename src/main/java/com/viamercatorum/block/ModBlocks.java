package com.viamercatorum.block;

import com.viamercatorum.ViaMercatorum;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {
    public static final VillageHallBlock VILLAGE_HALL = register("village_hall",
        new VillageHallBlock(BlockBehaviour.Properties.of()
            .setId(key("village_hall"))
            .strength(2.0f)
            .noOcclusion()));

    public static final RaidRemnantBlock RAID_REMNANT = register("raid_remnant",
        new RaidRemnantBlock(BlockBehaviour.Properties.of()
            .setId(key("raid_remnant"))
            .strength(1.5f)
            .noOcclusion()));

    private static ResourceKey<Block> key(String name) {
        return ResourceKey.create(BuiltInRegistries.BLOCK.key(),
            Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, name));
    }

    private static <T extends Block> T register(String name, T block) {
        return Registry.register(BuiltInRegistries.BLOCK,
            Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, name), block);
    }

    public static void register() {
    }
}