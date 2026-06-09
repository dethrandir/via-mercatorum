package com.viamercatorum.client.player;

import com.viamercatorum.network.DialogChoicePayload;
import com.viamercatorum.network.DialogPayload;
import com.viamercatorum.player.DialogScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class DialogScreen extends AbstractContainerScreen<DialogScreenHandler> {
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath("viamercatorum", "textures/gui/dialog.png");
    private static final int PORTRAIT_SIZE = 64;

    private String dialogId = "";
    private String characterId = "";
    private String characterName = "";
    private String characterTypeId = "";
    private String text = "";
    private List<DialogPayload.DialogChoice> choices = new ArrayList<>();

    public DialogScreen(DialogScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, title, 320, 200);
    }

    public void handleDialogPayload(DialogPayload payload) {
        this.dialogId = payload.dialogId();
        this.characterId = payload.characterId();
        this.characterName = payload.characterName();
        this.characterTypeId = payload.characterTypeId();
        this.text = payload.text();
        this.choices = payload.choices();
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xC0000000);
        graphics.fill(x, y, x + this.imageWidth, y + 1, 0xFFAAAAAA);
        graphics.fill(x, y, x + 1, y + this.imageHeight, 0xFFAAAAAA);
        graphics.fill(x + this.imageWidth - 1, y, x + this.imageWidth, y + this.imageHeight, 0xFFAAAAAA);
        graphics.fill(x, y + this.imageHeight - 1, x + this.imageWidth, y + this.imageHeight, 0xFFAAAAAA);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        Font font = Minecraft.getInstance().font;
        int x = this.leftPos;
        int y = this.topPos;

        int portraitX = x + 10;
        int portraitY = y + 10;
        graphics.fill(portraitX, portraitY, portraitX + PORTRAIT_SIZE, portraitY + PORTRAIT_SIZE, 0xFF333333);
        graphics.fill(portraitX, portraitY, portraitX + PORTRAIT_SIZE, portraitY + 1, 0xFF666666);
        graphics.fill(portraitX, portraitY, portraitX + 1, portraitY + PORTRAIT_SIZE, 0xFF666666);
        graphics.fill(portraitX + PORTRAIT_SIZE - 1, portraitY, portraitX + PORTRAIT_SIZE, portraitY + PORTRAIT_SIZE, 0xFF666666);
        graphics.fill(portraitX, portraitY + PORTRAIT_SIZE - 1, portraitX + PORTRAIT_SIZE, portraitY + PORTRAIT_SIZE, 0xFF666666);

        String portraitLabel = characterTypeId.isEmpty() ? "?" : characterTypeId.substring(0, Math.min(3, characterTypeId.length())).toUpperCase();
        int labelWidth = font.width(portraitLabel);
        graphics.text(font, portraitLabel, portraitX + (PORTRAIT_SIZE - labelWidth) / 2, portraitY + PORTRAIT_SIZE / 2 - 4, 0xFFFFFFFF);

        if (!characterName.isEmpty()) {
            graphics.text(font, characterName, x + PORTRAIT_SIZE + 20, y + 10, 0xFFD700);
        }
        if (!characterTypeId.isEmpty()) {
            graphics.text(font, "[" + characterTypeId + "]", x + PORTRAIT_SIZE + 20, y + 22, 0xFFAAAAAA);
        }

        int textX = x + PORTRAIT_SIZE + 20;
        int textY = y + 40;
        int maxTextWidth = this.imageWidth - PORTRAIT_SIZE - 35;
        drawWrappedText(graphics, font, text, textX, textY, maxTextWidth, 0xFFE0E0E0);

        int choiceY = y + this.imageHeight - 10 - choices.size() * 22;
        for (int i = 0; i < choices.size(); i++) {
            DialogPayload.DialogChoice choice = choices.get(i);
            int cy = choiceY + i * 22;
            int hoveredChoice = getHoveredChoice(mouseX, mouseY, choiceY);
            int bgColor = i == hoveredChoice ? 0xFF555555 : 0xFF222222;
            graphics.fill(x + 20, cy, x + this.imageWidth - 20, cy + 20, bgColor);
            graphics.fill(x + 20, cy, x + this.imageWidth - 20, cy + 1, 0xFF888888);
            graphics.fill(x + 20, cy + 19, x + this.imageWidth - 20, cy + 20, 0xFF888888);
            graphics.fill(x + 20, cy, x + 21, cy + 20, 0xFF888888);
            graphics.fill(x + this.imageWidth - 21, cy, x + this.imageWidth - 20, cy + 20, 0xFF888888);
            graphics.text(font, choice.text(), x + 28, cy + 6, 0xFFE0E0E0);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bool) {
        if (event.button() == 0) {
            int x = this.leftPos;
            int y = this.topPos;
            int choiceY = y + this.imageHeight - 10 - choices.size() * 22;

            for (int i = 0; i < choices.size(); i++) {
                int cy = choiceY + i * 22;
                if (event.x() >= x + 20 && event.x() <= x + this.imageWidth - 20
                    && event.y() >= cy && event.y() <= cy + 20) {
                    DialogPayload.DialogChoice choice = choices.get(i);
                    if (choice.nextNodeId().isEmpty() || choice.nextNodeId().equals("end") || choice.nextNodeId().equals("exit")) {
                        this.onClose();
                        return true;
                    }
                    ClientPlayNetworking.send(new DialogChoicePayload(dialogId, characterId, i));
                    return true;
                }
            }
        }
        return super.mouseClicked(event, bool);
    }

    private int getHoveredChoice(int mouseX, int mouseY, int choiceStartY) {
        int x = this.leftPos;
        for (int i = 0; i < choices.size(); i++) {
            int cy = choiceStartY + i * 22;
            if (mouseX >= x + 20 && mouseX <= x + this.imageWidth - 20
                && mouseY >= cy && mouseY <= cy + 20) {
                return i;
            }
        }
        return -1;
    }

    private void drawWrappedText(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int maxWidth, int color) {
        StringBuilder line = new StringBuilder();
        int currentY = y;
        String[] words = text.split(" ");
        for (String word : words) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (font.width(candidate) > maxWidth && !line.isEmpty()) {
                graphics.text(font, line.toString(), x, currentY, color);
                currentY += font.lineHeight + 1;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (!line.isEmpty()) {
            graphics.text(font, line.toString(), x, currentY, color);
        }
    }
}