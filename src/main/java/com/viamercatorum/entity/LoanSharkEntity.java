package com.viamercatorum.entity;

import com.viamercatorum.ViaMercatorum;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class LoanSharkEntity extends Monster {
    private int despawnTimer = 0;
    private static final int DESPAWN_TICKS = 1200;

    public LoanSharkEntity(EntityType<? extends LoanSharkEntity> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.literal("Armed Collector"));
        this.setCustomNameVisible(true);
    }

    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 30.0)
            .add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, 0.32)
            .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, 6.0)
            .add(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            despawnTimer++;
            if (despawnTimer > DESPAWN_TICKS) {
                discard();
            }
        }
    }

    @Override
    public boolean isAggressive() {
        return true;
    }
}