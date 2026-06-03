package com.viamercatorum.client.entity;

import com.viamercatorum.entity.CaravanEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;

public class CaravanEntityRenderer extends VillagerRenderer {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("viamercatorum", "textures/entity/caravan_trader.png");

    public CaravanEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTextureLocation(VillagerRenderState state) {
        return TEXTURE;
    }
}
