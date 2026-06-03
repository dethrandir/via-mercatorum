package com.viamercatorum.client.player;

import com.viamercatorum.player.MerchantScreenHandler;
import com.viamercatorum.player.MerchantScreenHandler.TradeEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class MerchantScreen extends AbstractContainerScreen<MerchantScreenHandler> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("viamercatorum", "textures/gui/market_stall.png");
    private static final int ENTRY_HEIGHT = 20;
    private int scrollOffset = 0;
    private boolean isCaravan;

    public MerchantScreen(MerchantScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, title, 256, 232);
        this.isCaravan = handler.isCaravan();
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 108;
        this.inventoryLabelY = 128;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        Font font = Minecraft.getInstance().font;
        graphics.text(font, this.title, this.titleLabelX, this.titleLabelY, 0x404040);
        graphics.text(font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040);

        if (isCaravan) {
            String balance = Component.translatable("container.viamercatorum.balance").getString() + ": 0 \u2603";
            graphics.text(font, balance, 108, 18, 0x404040);
        }

        List<TradeEntry> entries = getMenu().getTradeEntries().stream()
            .filter(e -> !e.goodId().equals("emerald"))
            .toList();
        int startY = 28;
        int drawLimit = 5;

        for (int i = scrollOffset; i < Math.min(entries.size(), scrollOffset + drawLimit); i++) {
            TradeEntry entry = entries.get(i);
            int ey = startY + (i - scrollOffset) * ENTRY_HEIGHT;

            graphics.text(font, entry.name(), 10, ey, 0x404040);
            graphics.text(font, Component.translatable("container.viamercatorum.buy").getString()
                + " $" + String.format("%.1f", entry.buyPrice()), 100, ey, 0x006600);
            graphics.text(font, Component.translatable("container.viamercatorum.sell").getString()
                + " $" + String.format("%.1f", entry.sellPrice()), 170, ey, 0x660000);

            String stockStr = (int) entry.stock() + "x";
            graphics.text(font, stockStr, 232, ey, 0x404040);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bool) {
        if (isCaravan && event.button() == 0) {
            List<TradeEntry> entries = getMenu().getTradeEntries().stream()
                .filter(e -> !e.goodId().equals("emerald"))
                .toList();
            int startY = 28;
            int drawLimit = 5;
            double mouseX = event.x();
            double mouseY = event.y();

            for (int i = scrollOffset; i < Math.min(entries.size(), scrollOffset + drawLimit); i++) {
                int ey = startY + (i - scrollOffset) * ENTRY_HEIGHT;
                int sy = this.topPos + ey;
                int sx = this.leftPos + 100;
                int sw = 60;

                if (mouseX >= sx && mouseX <= sx + sw && mouseY >= sy && mouseY <= sy + ENTRY_HEIGHT) {
                    Minecraft.getInstance().gameMode.handleInventoryButtonClick(this.menu.containerId, i);
                    return true;
                }

                sx = this.leftPos + 170;
                if (mouseX >= sx && mouseX <= sx + sw && mouseY >= sy && mouseY <= sy + ENTRY_HEIGHT) {
                    Minecraft.getInstance().gameMode.handleInventoryButtonClick(this.menu.containerId, 100 + i);
                    return true;
                }
            }
        }

        return super.mouseClicked(event, bool);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<TradeEntry> entries = getMenu().getTradeEntries().stream()
            .filter(e -> !e.goodId().equals("emerald"))
            .toList();
        int maxScroll = Math.max(0, entries.size() - 5);
        scrollOffset = Math.clamp(scrollOffset - (int) Math.round(scrollY), 0, maxScroll);
        return true;
    }
}
