package com.viamercatorum.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class MerchantShipEntity extends Mob {

    private int despawnTimer = 0;
    private static final int DESPAWN_SECONDS = 60;

    public MerchantShipEntity(EntityType<? extends MerchantShipEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.2);
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            despawnTimer++;
            if (despawnTimer > DESPAWN_SECONDS * 20) {
                discard();
                return;
            }

            if (!level().getBlockState(blockPosition().below()).is(Blocks.WATER)
                && !level().getBlockState(blockPosition().below(2)).is(Blocks.WATER)) {
                discard();
            }
        }
    }

    @Override
    public void travel(Vec3 movementInput) {
        if (!level().isClientSide() && !level().getBlockState(blockPosition().below()).is(Blocks.WATER)) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, -0.01, 0));
        }
        super.travel(movementInput);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("despawnTimer", despawnTimer);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        despawnTimer = input.getIntOr("despawnTimer", 0);
    }
}