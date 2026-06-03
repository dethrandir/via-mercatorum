package com.viamercatorum.client;

import com.viamercatorum.client.entity.CaravanEntityRenderer;
import com.viamercatorum.client.entity.CaravanGuardEntityRenderer;
import com.viamercatorum.client.player.MerchantScreen;
import com.viamercatorum.entity.ModEntities;
import com.viamercatorum.player.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;

@SuppressWarnings("deprecation")
public class ViaMercatorumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.CARAVAN, CaravanEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.CARAVAN_GUARD, CaravanGuardEntityRenderer::new);
        MenuScreens.register(ModScreenHandlers.MERCHANT, MerchantScreen::new);
    }
}
