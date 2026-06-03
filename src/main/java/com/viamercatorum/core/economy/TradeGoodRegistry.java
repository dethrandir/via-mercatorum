package com.viamercatorum.core.economy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TradeGoodRegistry {
    private static final Map<String, TradeGood> GOODS = new ConcurrentHashMap<>();

    public static final TradeGood WHEAT = register(new TradeGood("wheat", "Wheat", 2.0, TradeGood.Category.FOOD, true));
    public static final TradeGood CARROT = register(new TradeGood("carrot", "Carrot", 2.5, TradeGood.Category.FOOD, true));
    public static final TradeGood POTATO = register(new TradeGood("potato", "Potato", 2.5, TradeGood.Category.FOOD, true));
    public static final TradeGood BEEF = register(new TradeGood("beef", "Beef", 5.0, TradeGood.Category.FOOD, true));
    public static final TradeGood FISH = register(new TradeGood("fish", "Fish", 4.0, TradeGood.Category.FOOD, true));
    public static final TradeGood BREAD = register(new TradeGood("bread", "Bread", 3.0, TradeGood.Category.FOOD, true));

    public static final TradeGood IRON_INGOT = register(new TradeGood("iron_ingot", "Iron Ingot", 10.0, TradeGood.Category.RAW_MATERIAL, false));
    public static final TradeGood GOLD_INGOT = register(new TradeGood("gold_ingot", "Gold Ingot", 25.0, TradeGood.Category.RAW_MATERIAL, false));
    public static final TradeGood COPPER_INGOT = register(new TradeGood("copper_ingot", "Copper Ingot", 5.0, TradeGood.Category.RAW_MATERIAL, false));
    public static final TradeGood OAK_LOG = register(new TradeGood("oak_log", "Oak Log", 3.0, TradeGood.Category.RAW_MATERIAL, false));
    public static final TradeGood SPRUCE_LOG = register(new TradeGood("spruce_log", "Spruce Log", 3.5, TradeGood.Category.RAW_MATERIAL, false));
    public static final TradeGood STONE = register(new TradeGood("stone", "Stone", 2.0, TradeGood.Category.RAW_MATERIAL, false));
    public static final TradeGood COAL = register(new TradeGood("coal", "Coal", 4.0, TradeGood.Category.RAW_MATERIAL, false));
    public static final TradeGood LEATHER = register(new TradeGood("leather", "Leather", 6.0, TradeGood.Category.RAW_MATERIAL, false));

    public static final TradeGood IRON_SWORD = register(new TradeGood("iron_sword", "Iron Sword", 20.0, TradeGood.Category.WEAPON, false));
    public static final TradeGood IRON_PICKAXE = register(new TradeGood("iron_pickaxe", "Iron Pickaxe", 18.0, TradeGood.Category.TOOL, false));
    public static final TradeGood IRON_HELMET = register(new TradeGood("iron_helmet", "Iron Helmet", 22.0, TradeGood.Category.ARMOR, false));
    public static final TradeGood IRON_CHESTPLATE = register(new TradeGood("iron_chestplate", "Iron Chestplate", 35.0, TradeGood.Category.ARMOR, false));

    public static final TradeGood EMERALD = register(new TradeGood("emerald", "Emerald", 30.0, TradeGood.Category.LUXURY, false));
    public static final TradeGood DIAMOND = register(new TradeGood("diamond", "Diamond", 50.0, TradeGood.Category.LUXURY, false));

    public static final TradeGood COOKIE = register(new TradeGood("cookie", "Cookie", 1.0, TradeGood.Category.FOOD, true));
    public static final TradeGood PUMPKIN = register(new TradeGood("pumpkin", "Pumpkin", 3.0, TradeGood.Category.FOOD, true));
    public static final TradeGood MELON = register(new TradeGood("melon", "Melon", 3.0, TradeGood.Category.FOOD, true));
    public static final TradeGood PAPER = register(new TradeGood("paper", "Paper", 2.0, TradeGood.Category.SPECIAL, false));
    public static final TradeGood BOOK = register(new TradeGood("book", "Book", 5.0, TradeGood.Category.SPECIAL, false));
    public static final TradeGood LAPIS = register(new TradeGood("lapis", "Lapis Lazuli", 8.0, TradeGood.Category.LUXURY, false));
    public static final TradeGood REDSTONE = register(new TradeGood("redstone", "Redstone", 6.0, TradeGood.Category.SPECIAL, false));
    public static final TradeGood COOKED_BEEF = register(new TradeGood("cooked_beef", "Cooked Beef", 7.0, TradeGood.Category.FOOD, true));
    public static final TradeGood COOKED_FISH = register(new TradeGood("cooked_fish", "Cooked Fish", 6.0, TradeGood.Category.FOOD, true));
    public static final TradeGood CACTUS = register(new TradeGood("cactus", "Cactus", 3.0, TradeGood.Category.RAW_MATERIAL, false));
    public static final TradeGood SPIDER_EYE = register(new TradeGood("spider_eye", "Spider Eye", 4.0, TradeGood.Category.SPECIAL, true));
    public static final TradeGood CLAY = register(new TradeGood("clay", "Clay", 3.0, TradeGood.Category.RAW_MATERIAL, false));
    public static final TradeGood ACACIA_LOG = register(new TradeGood("acacia_log", "Acacia Log", 3.5, TradeGood.Category.RAW_MATERIAL, false));
    public static final TradeGood COCOA = register(new TradeGood("cocoa", "Cocoa", 5.0, TradeGood.Category.FOOD, true));

    private static TradeGood register(TradeGood good) {
        GOODS.put(good.id(), good);
        return good;
    }

    public static TradeGood get(String id) {
        return GOODS.get(id);
    }

    public static Map<String, TradeGood> getAll() {
        return Map.copyOf(GOODS);
    }
}
