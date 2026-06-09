package com.viamercatorum.player;

import com.viamercatorum.ViaMercatorum;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class ModScreenHandlers {
    public static final MenuType<MerchantScreenHandler> MERCHANT = Registry.register(
        BuiltInRegistries.MENU,
        Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "merchant"),
        new MenuType<>(MerchantScreenHandler::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static final MenuType<VillageHallScreenHandler> VILLAGE_HALL = Registry.register(
        BuiltInRegistries.MENU,
        Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "village_hall"),
        new MenuType<>(VillageHallScreenHandler::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static final MenuType<DialogScreenHandler> DIALOG = Registry.register(
        BuiltInRegistries.MENU,
        Identifier.fromNamespaceAndPath(ViaMercatorum.MOD_ID, "dialog"),
        new MenuType<>(DialogScreenHandler::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static void register() {
    }
}