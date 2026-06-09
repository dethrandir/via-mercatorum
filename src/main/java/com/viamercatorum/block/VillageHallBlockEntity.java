package com.viamercatorum.block;

import com.viamercatorum.player.VillageHallScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class VillageHallBlockEntity extends BlockEntity implements MenuProvider {
    private String villageId;

    public VillageHallBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VILLAGE_HALL, pos, state);
    }

    public void setVillageId(String villageId) {
        this.villageId = villageId;
    }

    public String getVillageId() { return villageId; }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.viamercatorum.village_hall");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new VillageHallScreenHandler(syncId, inv, villageId);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (villageId != null) {
            output.putString("villageId", villageId);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.villageId = input.getStringOr("villageId", "");
    }
}