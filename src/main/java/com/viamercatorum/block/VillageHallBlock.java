package com.viamercatorum.block;

import com.viamercatorum.player.VillageHallScreenHandler;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VillageHallBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 12, 16);

    public VillageHallBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(VillageHallBlock::new);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VillageHallBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, net.minecraft.world.level.Level world, BlockPos pos,
                                                Player player, BlockHitResult hit) {
        if (!world.isClientSide()) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof VillageHallBlockEntity hall) {
                player.openMenu(hall);
                if (player instanceof ServerPlayer serverPlayer && player.containerMenu instanceof VillageHallScreenHandler handler) {
                    handler.refreshData();
                    handler.sendFullDataToClient(serverPlayer);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}