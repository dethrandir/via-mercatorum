package com.viamercatorum.core.economy;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class GoodItemMapping {
    private static final Map<String, Item> GOOD_TO_ITEM = new HashMap<>();

    static {
        GOOD_TO_ITEM.put("wheat", Items.WHEAT);
        GOOD_TO_ITEM.put("carrot", Items.CARROT);
        GOOD_TO_ITEM.put("potato", Items.POTATO);
        GOOD_TO_ITEM.put("beef", Items.BEEF);
        GOOD_TO_ITEM.put("fish", Items.COD);
        GOOD_TO_ITEM.put("bread", Items.BREAD);
        GOOD_TO_ITEM.put("iron_ingot", Items.IRON_INGOT);
        GOOD_TO_ITEM.put("gold_ingot", Items.GOLD_INGOT);
        GOOD_TO_ITEM.put("copper_ingot", Items.COPPER_INGOT);
        GOOD_TO_ITEM.put("oak_log", Items.OAK_LOG);
        GOOD_TO_ITEM.put("spruce_log", Items.SPRUCE_LOG);
        GOOD_TO_ITEM.put("stone", Items.STONE);
        GOOD_TO_ITEM.put("coal", Items.COAL);
        GOOD_TO_ITEM.put("leather", Items.LEATHER);
        GOOD_TO_ITEM.put("iron_sword", Items.IRON_SWORD);
        GOOD_TO_ITEM.put("iron_pickaxe", Items.IRON_PICKAXE);
        GOOD_TO_ITEM.put("iron_helmet", Items.IRON_HELMET);
        GOOD_TO_ITEM.put("iron_chestplate", Items.IRON_CHESTPLATE);
        GOOD_TO_ITEM.put("emerald", Items.EMERALD);
        GOOD_TO_ITEM.put("diamond", Items.DIAMOND);
        GOOD_TO_ITEM.put("cookie", Items.COOKIE);
        GOOD_TO_ITEM.put("pumpkin", Items.PUMPKIN);
        GOOD_TO_ITEM.put("melon", Items.MELON);
        GOOD_TO_ITEM.put("paper", Items.PAPER);
        GOOD_TO_ITEM.put("book", Items.BOOK);
        GOOD_TO_ITEM.put("lapis", Items.LAPIS_LAZULI);
        GOOD_TO_ITEM.put("redstone", Items.REDSTONE);
        GOOD_TO_ITEM.put("cooked_beef", Items.COOKED_BEEF);
        GOOD_TO_ITEM.put("cooked_fish", Items.COOKED_COD);
        GOOD_TO_ITEM.put("cactus", Items.CACTUS);
        GOOD_TO_ITEM.put("spider_eye", Items.SPIDER_EYE);
        GOOD_TO_ITEM.put("clay", Items.CLAY);
        GOOD_TO_ITEM.put("acacia_log", Items.ACACIA_LOG);
        GOOD_TO_ITEM.put("cocoa", Items.COCOA_BEANS);
    }

    public static ItemStack goodToItemStack(String goodId, int count) {
        Item item = GOOD_TO_ITEM.get(goodId);
        if (item == null) return ItemStack.EMPTY;
        return new ItemStack(item, count);
    }

    public static String itemStackToGoodId(ItemStack stack) {
        if (stack.isEmpty()) return null;
        for (var entry : GOOD_TO_ITEM.entrySet()) {
            if (entry.getValue().equals(stack.getItem())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static boolean isEmerald(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem().equals(Items.EMERALD);
    }
}
