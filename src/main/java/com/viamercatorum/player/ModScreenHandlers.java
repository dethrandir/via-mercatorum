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

    public static void register() {
    }
}
