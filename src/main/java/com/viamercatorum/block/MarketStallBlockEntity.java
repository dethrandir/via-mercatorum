package com.viamercatorum.block;

import com.viamercatorum.player.MerchantScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MarketStallBlockEntity extends BlockEntity implements MenuProvider {
    private String villageId;

    public MarketStallBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MARKET_STALL, pos, state);
    }

    public void setVillageId(String villageId) {
        this.villageId = villageId;
    }

    public String getVillageId() { return villageId; }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.viamercatorum.market_stall");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new MerchantScreenHandler(syncId, inv, villageId);
    }
}
