package com.viamercatorum.player;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.block.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class ModItems {
    private static final Item.Properties DEFAULT_PROPS = new Item.Properties();

    public static final NewspaperItem NEWSPAPER = new NewspaperItem();
    public static final MerchantMapItem MERCHANT_MAP = new MerchantMapItem();
    public static final DebtNoteItem DEBT_NOTE = new DebtNoteItem();
    public static final StatsItem STATS_ITEM = new StatsItem();

    public static final BlockItem VILLAGE_HALL_ITEM = new BlockItem(ModBlocks.VILLAGE_HALL, new Item.Properties());
    public static final BlockItem RAID_REMNANT_ITEM = new BlockItem(ModBlocks.RAID_REMNANT, new Item.Properties());

    public static void register() {
        registerItem("newspaper", NEWSPAPER);
        registerItem("merchant_map", MERCHANT_MAP);
        registerItem("debt_note", DEBT_NOTE);
        registerItem("stats_item", STATS_ITEM);
        registerItem("village_hall", VILLAGE_HALL_ITEM);
        registerItem("raid_remnant", RAID_REMNANT_ITEM);
    }

    private static void registerItem(String name, Item item) {
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, name), item);
    }
}