package com.viamercatorum.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.EnumSet;

public class CaravanGuardEntity extends Villager {
    private GuardVariant variant = GuardVariant.SWORD;

    public enum GuardVariant {
        SWORD, BOW
    }

    public CaravanGuardEntity(EntityType<? extends CaravanGuardEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Villager.createAttributes();
    }

    public void setVariant(GuardVariant variant) {
        this.variant = variant;
        if (variant == GuardVariant.SWORD) {
            this.setItemSlot(EquipmentSlot.MAINHAND, Items.IRON_SWORD.getDefaultInstance());
        } else {
            this.setItemSlot(EquipmentSlot.MAINHAND, Items.BOW.getDefaultInstance());
        }
    }

    public GuardVariant getGuardVariant() { return variant; }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8));

        if (variant == GuardVariant.SWORD) {
            this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        } else {
            this.goalSelector.addGoal(1, new BowAttackGoal(this, 1.0, 20, 15));
        }

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, false));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("variant", variant.ordinal());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        int v = input.getIntOr("variant", 0);
        setVariant(v == 1 ? GuardVariant.BOW : GuardVariant.SWORD);
    }

    static class BowAttackGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private final CaravanGuardEntity guard;
        private final double speed;
        private final int attackInterval;
        private final float range;
        private int cooldown;

        public BowAttackGoal(CaravanGuardEntity guard, double speed, int attackInterval, float range) {
            this.guard = guard;
            this.speed = speed;
            this.attackInterval = attackInterval;
            this.range = range;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return guard.getTarget() != null && guard.getTarget().isAlive();
        }

        @Override
        public void tick() {
            net.minecraft.world.entity.LivingEntity target = guard.getTarget();
            if (target == null) return;

            double dist = guard.distanceToSqr(target);
            boolean canSee = guard.getSensing().hasLineOfSight(target);

            guard.getLookControl().setLookAt(target, 30, 30);

            if (dist <= range * range && canSee) {
                guard.getNavigation().stop();
                cooldown--;
                if (cooldown <= 0) {
                    var arrow = net.minecraft.world.entity.projectile.ProjectileUtil.getMobArrow(guard, Items.ARROW.getDefaultInstance(), 1.6f, Items.BOW.getDefaultInstance());
                    double dx = target.getX() - guard.getX();
                    double dz = target.getZ() - guard.getZ();
                    double dy = target.getY() + target.getEyeHeight() - guard.getY() - guard.getEyeHeight();
                    double horizontal = Math.sqrt(dx * dx + dz * dz);
                    arrow.shoot(dx, dy + horizontal * 0.2, dz, 1.6f, 1.0f);
                    guard.level().addFreshEntity(arrow);
                    cooldown = attackInterval;
                }
            } else {
                guard.getNavigation().moveTo(target, speed);
            }
        }

        @Override
        public void start() {
            cooldown = 0;
        }
    }
}