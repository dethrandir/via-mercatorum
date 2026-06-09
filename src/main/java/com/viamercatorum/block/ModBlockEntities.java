package com.viamercatorum.block;

import com.viamercatorum.ViaMercatorum;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static final BlockEntityType<VillageHallBlockEntity> VILLAGE_HALL = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "village_hall"),
        FabricBlockEntityTypeBuilder.create(VillageHallBlockEntity::new, ModBlocks.VILLAGE_HALL).build()
    );

    public static final BlockEntityType<RaidRemnantBlockEntity> RAID_REMNANT = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "raid_remnant"),
        FabricBlockEntityTypeBuilder.create(RaidRemnantBlockEntity::new, ModBlocks.RAID_REMNANT).build()
    );

    public static void register() {
    }
}