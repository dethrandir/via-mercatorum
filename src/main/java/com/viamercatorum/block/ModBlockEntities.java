package com.viamercatorum.block;

import com.viamercatorum.ViaMercatorum;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static final BlockEntityType<MarketStallBlockEntity> MARKET_STALL = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "market_stall"),
        FabricBlockEntityTypeBuilder.create(MarketStallBlockEntity::new, ModBlocks.MARKET_STALL).build()
    );

    public static void register() {
    }
}
