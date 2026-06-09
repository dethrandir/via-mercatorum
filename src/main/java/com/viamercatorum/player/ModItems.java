package com.viamercatorum.player;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.block.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final NewspaperItem NEWSPAPER =
        registerItem("newspaper", new NewspaperItem(itemProps("newspaper")));

    public static final MerchantMapItem MERCHANT_MAP =
        registerItem("merchant_map", new MerchantMapItem(itemProps("merchant_map")));

    public static final DebtNoteItem DEBT_NOTE =
        registerItem("debt_note", new DebtNoteItem(itemProps("debt_note")));

    public static final StatsItem STATS_ITEM =
        registerItem("stats_item", new StatsItem(itemProps("stats_item")));

    public static final BlockItem VILLAGE_HALL_ITEM =
        registerItem("village_hall", new BlockItem(ModBlocks.VILLAGE_HALL, itemProps("village_hall")));

    public static final BlockItem RAID_REMNANT_ITEM =
        registerItem("raid_remnant", new BlockItem(ModBlocks.RAID_REMNANT, itemProps("raid_remnant")));

    private static Item.Properties itemProps(String name) {
        return new Item.Properties().setId(ResourceKey.create(
            BuiltInRegistries.ITEM.key(),
            Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, name)));
    }

    public static void register() {
    }

    private static <T extends Item> T registerItem(String name, T item) {
        return Registry.register(BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, name), item);
    }
}