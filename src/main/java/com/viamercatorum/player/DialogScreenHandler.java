package com.viamercatorum.player;

import com.viamercatorum.network.DialogPayload;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class DialogScreenHandler extends AbstractContainerMenu {
    private String dialogId;
    private String characterId;

    public DialogScreenHandler(int syncId, Inventory inv) {
        this(syncId, inv, null, null);
    }

    public DialogScreenHandler(int syncId, Inventory inv, String dialogId, String characterId) {
        super(ModScreenHandlers.DIALOG, syncId);
        this.dialogId = dialogId;
        this.characterId = characterId;
    }

    public String getDialogId() { return dialogId; }
    public String getCharacterId() { return characterId; }

    public void receiveFromServer(DialogPayload payload) {
        this.dialogId = payload.dialogId();
        this.characterId = payload.characterId();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}