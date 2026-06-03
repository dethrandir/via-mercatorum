package com.viamercatorum.client.entity;

import com.viamercatorum.entity.CaravanGuardEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;

public class CaravanGuardEntityRenderer extends VillagerRenderer {
    private static final Identifier SWORD_TEXTURE = Identifier.fromNamespaceAndPath("viamercatorum", "textures/entity/caravan_guard_sword.png");
    private static final Identifier BOW_TEXTURE = Identifier.fromNamespaceAndPath("viamercatorum", "textures/entity/caravan_guard_bow.png");

    public CaravanGuardEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTextureLocation(VillagerRenderState state) {
        return SWORD_TEXTURE;
    }
}
