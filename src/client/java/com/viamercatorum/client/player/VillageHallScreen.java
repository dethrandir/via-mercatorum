package com.viamercatorum.client.player;

import com.viamercatorum.network.VillageHallDataPayload;
import com.viamercatorum.player.VillageHallScreenHandler;
import com.viamercatorum.player.VillageHallScreenHandler.*;
import com.viamercatorum.player.VillageHallTab;
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
import java.util.Map;

public class VillageHallScreen extends AbstractContainerScreen<VillageHallScreenHandler> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("viamercatorum", "textures/gui/village_hall.png");
    private static final int TAB_WIDTH = 40;
    private static final int TAB_HEIGHT = 14;
    private static final int ENTRY_HEIGHT = 12;
    private int scrollOffset = 0;
    private VillageHallTab activeTab = VillageHallTab.TRADE;

    private static final int[] TAB_COLORS = {
        0xFF5555, 0xFFAA00, 0x55FF55, 0x5555FF, 0xFF55FF, 0x55FFFF
    };

    public VillageHallScreen(VillageHallScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, title, 256, 232);
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
        int x = this.leftPos;
        int y = this.topPos;

        graphics.text(font, this.title, this.titleLabelX, this.titleLabelY, 0x404040);
        graphics.text(font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040);

        drawTabs(graphics, font, x, y);
        drawContent(graphics, font, x, y);
    }

    private void drawTabs(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        VillageHallTab[] tabs = VillageHallTab.values();
        for (int i = 0; i < tabs.length; i++) {
            int tabX = x + 4 + i * (TAB_WIDTH + 2);
            int tabY = y + 2;
            int color = tabs[i] == activeTab ? TAB_COLORS[i] : (TAB_COLORS[i] & 0x88FFFFFF);
            String label = getTabLabel(tabs[i]);
            int textWidth = font.width(label);
            int bgX1 = tabX;
            int bgX2 = tabX + TAB_WIDTH;
            int bgY1 = tabY;
            int bgY2 = tabY + TAB_HEIGHT;

            graphics.fill(bgX1, bgY1, bgX2, bgY2, color);
            graphics.text(font, label, tabX + (TAB_WIDTH - textWidth) / 2, tabY + 3, 0xFFFFFF);
        }
    }

    private String getTabLabel(VillageHallTab tab) {
        return switch (tab) {
            case TRADE -> "\u00A7e" + Component.translatable("gui.viamercatorum.tab.trade").getString();
            case INVENTORY -> "\u00A7a" + Component.translatable("gui.viamercatorum.tab.inventory").getString();
            case WORKSHOPS -> "\u00A76" + Component.translatable("gui.viamercatorum.tab.workshops").getString();
            case STATISTICS -> "\u00A79" + Component.translatable("gui.viamercatorum.tab.statistics").getString();
            case MILITARY -> "\u00A7c" + Component.translatable("gui.viamercatorum.tab.military").getString();
            case GOALS -> "\u00A7d" + Component.translatable("gui.viamercatorum.tab.goals").getString();
        };
    }

    private void drawContent(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        int contentY = y + 20;
        int contentX = x + 4;

        switch (activeTab) {
            case TRADE -> drawTradeTab(graphics, font, contentX, contentY);
            case INVENTORY -> drawInventoryTab(graphics, font, contentX, contentY);
            case WORKSHOPS -> drawWorkshopsTab(graphics, font, contentX, contentY);
            case STATISTICS -> drawStatisticsTab(graphics, font, contentX, contentY);
            case MILITARY -> drawMilitaryTab(graphics, font, contentX, contentY);
            case GOALS -> drawGoalsTab(graphics, font, contentX, contentY);
        }
    }

    private void drawTradeTab(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        List<TradeEntry> entries = getMenu().getTradeEntries().stream()
            .filter(e -> !e.goodId().equals("emerald"))
            .toList();
        int maxVisible = 8;

        for (int i = scrollOffset; i < Math.min(entries.size(), scrollOffset + maxVisible); i++) {
            TradeEntry entry = entries.get(i);
            int ey = y + (i - scrollOffset) * ENTRY_HEIGHT;
            if (ey > y + maxVisible * ENTRY_HEIGHT) break;

            graphics.text(font, truncate(font, entry.name(), 80), x, ey, 0x404040);
            String buyStr = String.format("%.1f", entry.buyPrice());
            String sellStr = String.format("%.1f", entry.sellPrice());
            String stockStr = (int) entry.stock() + "x";
            graphics.text(font, buyStr, x + 85, ey, 0x006600);
            graphics.text(font, sellStr, x + 120, ey, 0x660000);
            graphics.text(font, stockStr, x + 155, ey, 0x404040);
        }
    }

    private void drawInventoryTab(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        List<SupplyEntry> entries = getMenu().getSupplyEntries();
        int maxVisible = 8;

        graphics.text(font, Component.translatable("gui.viamercatorum.good").getString(), x, y - 1, 0x808080);
        graphics.text(font, Component.translatable("gui.viamercatorum.stock").getString(), x + 80, y - 1, 0x808080);
        graphics.text(font, Component.translatable("gui.viamercatorum.production").getString(), x + 120, y - 1, 0x808080);
        graphics.text(font, Component.translatable("gui.viamercatorum.demand").getString(), x + 160, y - 1, 0x808080);
        y += 4;

        for (int i = scrollOffset; i < Math.min(entries.size(), scrollOffset + maxVisible); i++) {
            SupplyEntry entry = entries.get(i);
            int ey = y + (i - scrollOffset) * ENTRY_HEIGHT;

            graphics.text(font, truncate(font, entry.name(), 75), x, ey, 0x404040);
            graphics.text(font, String.format("%.0f/%.0f", entry.stock(), entry.maxCapacity()), x + 80, ey, 0x404040);

            int barWidth = 30;
            double fillRatio = entry.maxCapacity() > 0 ? entry.stock() / entry.maxCapacity() : 0;
            int fillColor = fillRatio > 0.5 ? 0xFF5555FF : fillRatio > 0.2 ? 0xFFFFFF00 : 0xFFFF0000;
            graphics.fill(x + 115, ey + 1, x + 115 + barWidth, ey + 8, 0xFF333333);
            graphics.fill(x + 115, ey + 1, x + 115 + (int) (barWidth * fillRatio), ey + 8, fillColor);

            graphics.text(font, String.format("%.1f", entry.productionRate()), x + 155, ey, 0x404040);
            graphics.text(font, String.format("%.1f", entry.demand()), x + 190, ey, 0x404040);
        }
    }

    private void drawWorkshopsTab(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        List<WorkshopEntry> entries = getMenu().getWorkshopEntries();
        int maxVisible = 6;

        for (int i = scrollOffset; i < Math.min(entries.size(), scrollOffset + maxVisible); i++) {
            WorkshopEntry entry = entries.get(i);
            int ey = y + (i - scrollOffset) * (ENTRY_HEIGHT + 4);

            String statusIcon = entry.enabled() ? "\u00A7a\u2713" : "\u00A7c\u2717";
            graphics.text(font, statusIcon, x, ey, 0x404040);
            graphics.text(font, truncate(font, entry.displayName(), 70), x + 12, ey, 0x404040);
            graphics.text(font, "Lv" + entry.level(), x + 85, ey, 0x404040);

            String autoStr = "";
            if (entry.autoBuy()) autoStr += "\u00A7aB";
            if (entry.autoSell()) autoStr += "\u00A7aS";
            if (autoStr.isEmpty()) autoStr = "\u00A78--";
            graphics.text(font, autoStr, x + 110, ey, 0x404040);

            String ownerStr = entry.isPlayerOwned() ? "\u00A7eP" : "\u00A77V";
            graphics.text(font, ownerStr, x + 135, ey, 0x404040);

            String toggleLabel = entry.enabled()
                ? Component.translatable("gui.viamercatorum.disable").getString()
                : Component.translatable("gui.viamercatorum.enable").getString();
            graphics.text(font, "\u00A77[" + toggleLabel + "]", x + 150, ey, 0xAAAAAA);
        }

        if (entries.isEmpty()) {
            graphics.text(font, Component.translatable("gui.viamercatorum.no_workshops").getString(), x, y, 0x808080);
        }
    }

    private void drawStatisticsTab(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        if (getMenu().getVillageId() == null) return;

        double population = getMenu().dataSlots[VillageHallScreenHandler.DATA_POPULATION].get() / 1000.0;
        double foodRatio = getMenu().dataSlots[VillageHallScreenHandler.DATA_FOOD_RATIO].get() / 1000.0;
        double reputation = getMenu().dataSlots[VillageHallScreenHandler.DATA_REPUTATION].get() / 100.0;
        double prodScale = getMenu().dataSlots[VillageHallScreenHandler.DATA_PRODUCTION_SCALE].get() / 1000.0;
        double maxPop = getMenu().dataSlots[VillageHallScreenHandler.DATA_MAX_POP].get() / 1000.0;
        int villageType = getMenu().dataSlots[VillageHallScreenHandler.DATA_VILLAGE_TYPE].get();
        boolean isCoastal = getMenu().dataSlots[VillageHallScreenHandler.DATA_IS_COASTAL].get() == 1;
        boolean isRaided = getMenu().dataSlots[VillageHallScreenHandler.DATA_IS_RAIDED].get() == 1;

        String[] typeNames = {"Hamlet", "Village", "Town", "City", "Fortified"};
        String typeName = villageType < typeNames.length ? typeNames[villageType] : "?";

        int line = 0;
        int lineH = 12;

        graphics.text(font, Component.translatable("gui.viamercatorum.population").getString() + ": " + String.format("%.0f/%.0f", population, maxPop), x, y + line * lineH, 0x404040); line++;
        graphics.text(font, Component.translatable("gui.viamercatorum.food_ratio").getString() + ": " + String.format("%.1f%%", foodRatio * 100), x, y + line * lineH, foodRatio < 0.3 ? 0xFF0000 : foodRatio < 0.5 ? 0xFFFF00 : 0x00AA00); line++;
        graphics.text(font, Component.translatable("gui.viamercatorum.reputation").getString() + ": " + String.format("%.1f", reputation), x, y + line * lineH, reputation > 0 ? 0x00AA00 : reputation < 0 ? 0xFF0000 : 0x404040); line++;
        graphics.text(font, Component.translatable("gui.viamercatorum.production_scale").getString() + ": " + String.format("%.2fx", prodScale), x, y + line * lineH, 0x404040); line++;
        graphics.text(font, Component.translatable("gui.viamercatorum.village_type").getString() + ": " + typeName, x, y + line * lineH, 0x404040); line++;
        graphics.text(font, Component.translatable("gui.viamercatorum.coastal").getString() + ": " + (isCoastal ? "\u00A7a\u2713" : "\u00A7c\u2717"), x, y + line * lineH, 0x404040); line++;
        graphics.text(font, Component.translatable("gui.viamercatorum.raided").getString() + ": " + (isRaided ? "\u00A7c\u2713" : "\u00A7a\u2717"), x, y + line * lineH, 0x404040); line++;

        double gdp = getMenu().dataSlots[VillageHallScreenHandler.DATA_GDP].get() / 100.0;
        double gdpPerCapita = getMenu().dataSlots[VillageHallScreenHandler.DATA_GDP_PER_CAPITA].get() / 1000.0;
        graphics.text(font, Component.translatable("gui.viamercatorum.gdp").getString() + ": " + String.format("%.1f", gdp), x, y + line * lineH, 0x404040); line++;
        graphics.text(font, Component.translatable("gui.viamercatorum.gdp_per_capita").getString() + ": " + String.format("%.2f", gdpPerCapita), x, y + line * lineH, 0x404040); line++;

        line++;
        graphics.text(font, Component.translatable("gui.viamercatorum.supplies").getString() + ": " + getMenu().dataSlots[VillageHallScreenHandler.DATA_SUPPLY_COUNT].get(), x, y + line * lineH, 0x404040); line++;
        graphics.text(font, Component.translatable("gui.viamercatorum.workshops").getString() + ": " + getMenu().dataSlots[VillageHallScreenHandler.DATA_WORKSHOP_COUNT].get(), x, y + line * lineH, 0x404040); line++;
    }

    private void drawMilitaryTab(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        if (getMenu().getVillageId() == null) return;

        double totalPower = getMenu().dataSlots[VillageHallScreenHandler.DATA_MILITARY_POWER].get() / 100.0;
        int mobilization = getMenu().dataSlots[VillageHallScreenHandler.DATA_MOBILIZATION].get();
        boolean canMobilize = getMenu().dataSlots[VillageHallScreenHandler.DATA_CAN_MOBILIZE].get() == 1;
        boolean canDemobilize = getMenu().dataSlots[VillageHallScreenHandler.DATA_CAN_DEMOBILIZE].get() == 1;

        String[] mobNames = {"Demilitarized", "Standby", "Mobilized"};
        String[] mobNamesTrKey = {"gui.viamercatorum.demilitarized", "gui.viamercatorum.standby", "gui.viamercatorum.mobilized"};
        String mobStr = mobilization < mobNamesTrKey.length ? Component.translatable(mobNamesTrKey[mobilization]).getString() : "?";

        int line = 0;
        int lineH = 12;

        graphics.text(font, Component.translatable("gui.viamercatorum.military_power").getString() + ": " + String.format("%.1f", totalPower), x, y + line * lineH, 0xFF5555); line++;
        graphics.text(font, Component.translatable("gui.viamercatorum.mobilization").getString() + ": " + mobStr, x, y + line * lineH, 0x404040); line++;
        line++;

        graphics.text(font, Component.translatable("gui.viamercatorum.breakdown").getString() + ":", x, y + line * lineH, 0x808080); line++;

        Map<String, Double> breakdown = getMenu().getMilitaryBreakdown();
        for (Map.Entry<String, Double> entry : breakdown.entrySet()) {
            String key = Component.translatable("gui.viamercatorum.mil_" + entry.getKey()).getString();
            graphics.text(font, "  " + key + ": " + String.format("%.1f", entry.getValue()), x, y + line * lineH, 0x404040);
            line++;
        }

        line += 2;
        if (canMobilize) {
            graphics.text(font, "\u00A7a[" + Component.translatable("gui.viamercatorum.mobilize").getString() + "]", x, y + line * lineH, 0x00AA00);
        }
        line++;
        if (canDemobilize) {
            graphics.text(font, "\u00A7c[" + Component.translatable("gui.viamercatorum.demobilize").getString() + "]", x, y + line * lineH, 0xFF5555);
        }
    }

    private void drawGoalsTab(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        List<GoalEntry> entries = getMenu().getGoalEntries();
        if (entries.isEmpty()) {
            graphics.text(font, Component.translatable("gui.viamercatorum.no_goals").getString(), x, y, 0x808080);
            return;
        }

        graphics.text(font, Component.translatable("gui.viamercatorum.goal").getString(), x, y, 0x808080);
        graphics.text(font, Component.translatable("gui.viamercatorum.category").getString(), x + 90, y, 0x808080);
        graphics.text(font, Component.translatable("gui.viamercatorum.urgency").getString(), x + 150, y, 0x808080);
        y += 10;

        int maxVisible = 6;
        for (int i = scrollOffset; i < Math.min(entries.size(), scrollOffset + maxVisible); i++) {
            GoalEntry entry = entries.get(i);
            int ey = y + (i - scrollOffset) * ENTRY_HEIGHT;
            int color = entry.conditionsMet() ? 0x00AA00 : 0x808080;

            graphics.text(font, truncate(font, entry.name(), 85), x, ey, color);
            graphics.text(font, entry.category(), x + 90, ey, 0x404040);
            graphics.text(font, String.format("%.1f", entry.urgencyScore()), x + 150, ey, color);
        }
    }

    private String truncate(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        while (font.width(text + "...") > maxWidth && !text.isEmpty()) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "...";
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bool) {
        if (event.button() == 0) {
            int x = this.leftPos;
            int y = this.topPos;

            VillageHallTab[] tabs = VillageHallTab.values();
            for (int i = 0; i < tabs.length; i++) {
                int tabX = x + 4 + i * (TAB_WIDTH + 2);
                int tabY = y + 2;
                if (event.x() >= tabX && event.x() <= tabX + TAB_WIDTH
                    && event.y() >= tabY && event.y() <= tabY + TAB_HEIGHT) {
                    activeTab = tabs[i];
                    scrollOffset = 0;
                    Minecraft.getInstance().gameMode.handleInventoryButtonClick(this.menu.containerId, i);
                    return true;
                }
            }

            if (activeTab == VillageHallTab.WORKSHOPS) {
                List<WorkshopEntry> entries = getMenu().getWorkshopEntries();
                int contentY = this.topPos + 20;
                int maxVisible = 6;
                for (int i = scrollOffset; i < Math.min(entries.size(), scrollOffset + maxVisible); i++) {
                    int ey = contentY + (i - scrollOffset) * (ENTRY_HEIGHT + 4);
                    int toggleX = this.leftPos + 150;
                    if (event.x() >= toggleX && event.x() <= toggleX + 40
                        && event.y() >= ey && event.y() <= ey + ENTRY_HEIGHT) {
                        Minecraft.getInstance().gameMode.handleInventoryButtonClick(this.menu.containerId, 100 + i);
                        return true;
                    }
                }
            }

            if (activeTab == VillageHallTab.MILITARY) {
                int lineH = 12;
                int baseY = this.topPos + 20;
                int line = 0;

                Map<String, Double> breakdown = getMenu().getMilitaryBreakdown();
                int totalLines = 3 + breakdown.size() + 2;
                int mobilizeY = baseY + (totalLines - 2) * lineH;
                int demobilizeY = baseY + (totalLines - 1) * lineH;

                boolean canMobilize = getMenu().dataSlots[VillageHallScreenHandler.DATA_CAN_MOBILIZE].get() == 1;
                boolean canDemobilize = getMenu().dataSlots[VillageHallScreenHandler.DATA_CAN_DEMOBILIZE].get() == 1;

                if (canMobilize && event.y() >= mobilizeY - 2 && event.y() <= mobilizeY + 12) {
                    Minecraft.getInstance().gameMode.handleInventoryButtonClick(this.menu.containerId, 200);
                    return true;
                }
                if (canDemobilize && event.y() >= demobilizeY - 2 && event.y() <= demobilizeY + 12) {
                    Minecraft.getInstance().gameMode.handleInventoryButtonClick(this.menu.containerId, 201);
                    return true;
                }
            }
        }

        return super.mouseClicked(event, bool);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int contentLines = switch (activeTab) {
            case TRADE -> getMenu().getTradeEntries().size();
            case INVENTORY -> getMenu().getSupplyEntries().size();
            case WORKSHOPS -> getMenu().getWorkshopEntries().size();
            case STATISTICS -> 0;
            case MILITARY -> 0;
            case GOALS -> getMenu().getGoalEntries().size();
        };
        int maxVisible = switch (activeTab) {
            case TRADE, INVENTORY, GOALS -> 8;
            case WORKSHOPS -> 6;
            default -> 0;
        };
        int maxScroll = Math.max(0, contentLines - maxVisible);
        scrollOffset = Math.clamp(scrollOffset - (int) Math.round(scrollY), 0, maxScroll);
        return true;
    }

    public void handleDataPayload(VillageHallDataPayload payload) {
        getMenu().receiveDataFromServer(payload);
    }
}