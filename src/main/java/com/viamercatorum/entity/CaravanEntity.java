package com.viamercatorum.entity;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.world.Caravan;
import com.viamercatorum.core.world.RouteGraph;
import com.viamercatorum.player.MerchantScreenHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class CaravanEntity extends Villager {
    private String caravanId;
    private String routeId;

    public CaravanEntity(EntityType<? extends CaravanEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public void setCaravanData(String caravanId, String routeId) {
        this.caravanId = caravanId;
        this.routeId = routeId;
    }

    public String getCaravanId() { return caravanId; }
    public String getRouteId() { return routeId; }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new CaravanFollowPathGoal(this, 0.6));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.5));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (caravanId == null) {
            return super.mobInteract(player, hand);
        }
        player.openMenu(new SimpleMenuProvider(
            (syncId, inv, p) -> new MerchantScreenHandler(syncId, inv, caravanId, true),
            Component.translatable("container.viamercatorum.market_stall")
        ));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && caravanId != null) {
            RouteGraph graph = ViaMercatorum.getInstance().getRouteGraph();
            Caravan caravan = graph.getCaravan(caravanId);
            if (caravan == null || caravan.hasArrived()) {
                this.discard();
            }
        }
    }
}
