package com.viamercatorum.client;

import com.viamercatorum.client.entity.CaravanEntityRenderer;
import com.viamercatorum.client.entity.CaravanGuardEntityRenderer;
import com.viamercatorum.client.entity.CaravanRaiderRenderer;
import com.viamercatorum.client.entity.CharacterEntityRenderer;
import com.viamercatorum.client.entity.LoanSharkRenderer;
import com.viamercatorum.client.entity.MerchantShipRenderer;
import com.viamercatorum.client.player.DialogScreen;
import com.viamercatorum.client.player.MerchantScreen;
import com.viamercatorum.client.player.StatsScreen;
import com.viamercatorum.client.player.VillageHallScreen;
import com.viamercatorum.entity.ModEntities;
import com.viamercatorum.network.DialogPayload;
import com.viamercatorum.network.StatsPayload;
import com.viamercatorum.network.VillageHallDataPayload;
import com.viamercatorum.player.ModScreenHandlers;
import com.viamercatorum.player.VillageHallScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;

@SuppressWarnings("deprecation")
public class ViaMercatorumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.CARAVAN, CaravanEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.CARAVAN_GUARD, CaravanGuardEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.CARAVAN_RAIDER, CaravanRaiderRenderer::new);
        EntityRendererRegistry.register(ModEntities.MERCHANT_SHIP, MerchantShipRenderer::new);
        EntityRendererRegistry.register(ModEntities.LOAN_SHARK, LoanSharkRenderer::new);
        EntityRendererRegistry.register(ModEntities.CHARACTER, CharacterEntityRenderer::new);

        MenuScreens.register(ModScreenHandlers.MERCHANT, MerchantScreen::new);
        MenuScreens.register(ModScreenHandlers.VILLAGE_HALL, VillageHallScreen::new);
        MenuScreens.register(ModScreenHandlers.DIALOG, DialogScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(VillageHallDataPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().screen instanceof VillageHallScreen screen) {
                    screen.handleDataPayload(payload);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(DialogPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().screen instanceof DialogScreen screen) {
                    screen.handleDialogPayload(payload);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(StatsPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                StatsScreen screen = new StatsScreen();
                screen.setStats(payload);
                context.client().setScreen(screen);
            });
        });
    }
}