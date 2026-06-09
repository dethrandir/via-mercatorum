package com.viamercatorum.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

public class CaravanRaiderEntity extends Monster {
    private int despawnTimer;
    private RaiderVariant variant = RaiderVariant.AXE;

    public enum RaiderVariant {
        AXE, CROSSBOW
    }

    public CaravanRaiderEntity(EntityType<? extends CaravanRaiderEntity> type, Level level) {
        super(type, level);
        this.despawnTimer = 30 * 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 24.0)
            .add(Attributes.MOVEMENT_SPEED, 0.35)
            .add(Attributes.ATTACK_DAMAGE, 5.0)
            .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    public void setVariant(RaiderVariant variant) {
        this.variant = variant;
        if (variant == RaiderVariant.AXE) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
        } else {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
        }
    }

    public RaiderVariant getVariant() { return variant; }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        if (variant == RaiderVariant.AXE) {
            this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, true));
        } else {
            this.goalSelector.addGoal(1, new RaiderRangedGoal(this, 1.0, 20, 15.0f));
        }
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.4));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8));

        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, CaravanEntity.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, CaravanGuardEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Villager.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            despawnTimer--;
            if (despawnTimer <= 0) {
                this.discard();
            }
        }
    }

    static class RaiderRangedGoal extends Goal {
        private final Mob mob;
        private final double speed;
        private final int attackInterval;
        private final float range;
        private int cooldown;

        RaiderRangedGoal(Mob mob, double speed, int attackInterval, float range) {
            this.mob = mob;
            this.speed = speed;
            this.attackInterval = attackInterval;
            this.range = range;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return mob.getTarget() != null && mob.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = mob.getTarget();
            if (target == null) return;

            double dist = mob.distanceToSqr(target);
            boolean canSee = mob.getSensing().hasLineOfSight(target);

            mob.getLookControl().setLookAt(target, 30, 30);

            if (dist <= range * range && canSee) {
                mob.getNavigation().stop();
                cooldown--;
                if (cooldown <= 0) {
                    var arrow = ProjectileUtil.getMobArrow(mob, Items.ARROW.getDefaultInstance(), 1.6f, Items.CROSSBOW.getDefaultInstance());
                    double dx = target.getX() - mob.getX();
                    double dz = target.getZ() - mob.getZ();
                    double dy = target.getY() + target.getEyeHeight() - mob.getY() - mob.getEyeHeight();
                    double horizontal = Math.sqrt(dx * dx + dz * dz);
                    arrow.shoot(dx, dy + horizontal * 0.2, dz, 1.6f, 1.0f);
                    mob.level().addFreshEntity(arrow);
                    cooldown = attackInterval;
                }
            } else {
                mob.getNavigation().moveTo(target, speed);
            }
        }

        @Override
        public void start() {
            cooldown = 0;
        }
    }
}