package com.viamercatorum.client.player;

import com.viamercatorum.network.StatsPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class StatsScreen extends Screen {
    private StatsPayload stats;

    public StatsScreen() {
        super(Component.literal("Via Mercatorum Stats"));
    }

    public void setStats(StatsPayload stats) {
        this.stats = stats;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int x = (width - 300) / 2;
        int y = (height - 380) / 2;
        graphics.fill(x, y, x + 300, y + 380, 0xC0101010);
        graphics.fill(x, y, x + 300, y + 1, 0xFF5588AA);
        graphics.fill(x, y, x + 1, y + 380, 0xFF5588AA);
        graphics.fill(x + 299, y, x + 300, y + 380, 0xFF5588AA);
        graphics.fill(x, y + 379, x + 300, y + 380, 0xFF5588AA);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        Font font = Minecraft.getInstance().font;
        if (stats == null) return;

        int x = (width - 300) / 2 + 8;
        int y = (height - 380) / 2 + 8;
        int line = 0;
        int lh = 12;

        draw(graphics, font, x, y, lh, line++, "§6§lVia Mercatorum — Economy Stats", 0xFFD700);
        line++;

        draw(graphics, font, x, y, lh, line++, "§7Villages: §f" + stats.totalVillages() + "  Routes: §f" + stats.totalRoutes() + "  Caravans: §f" + stats.totalCaravans(), 0xAAAAAA);
        draw(graphics, font, x, y, lh, line++, "§7Characters: §f" + stats.totalCharacters() + "  Leaders: §f" + stats.leaderCount() + "  Merchants: §f" + stats.merchantCount() + "  CaravanL: §f" + stats.caravanLeaderCount(), 0xAAAAAA);
        draw(graphics, font, x, y, lh, line++, "§7Total Wealth: §a" + fmt(stats.totalCharacterWealth()) + " §7Avg Food: §a" + fmt(stats.avgFoodRatio()), 0xAAAAAA);
        line++;

        draw(graphics, font, x, y, lh, line++, "§6§lTop 3 Villages (GDP)", 0xFFAA00);
        for (int i = 0; i < stats.topVillages().size(); i++) {
            var v = stats.topVillages().get(i);
            draw(graphics, font, x, y, lh, line++, " §f#" + (i+1) + " " + v.name() + " §7(" + v.type() + ") §aGDP:" + fmt(v.gdp()) + " §7Pop:" + fmt(v.population()) + " Food:" + fmt(v.foodRatio()) + " Rep:" + fmt(v.reputation()), 0xE0E0E0);
        }
        line++;

        draw(graphics, font, x, y, lh, line++, "§6§lTop 3 Characters (Wealth)", 0xFFAA00);
        for (int i = 0; i < stats.topCharacters().size(); i++) {
            var c = stats.topCharacters().get(i);
            draw(graphics, font, x, y, lh, line++, " §f#" + (i+1) + " " + c.name() + " §7[" + c.type() + "] §6" + fmt(c.wealth()) + "z §cPow:" + fmt(c.power()) + " §7WS:" + c.workshopCount(), 0xE0E0E0);
        }
        line++;

        draw(graphics, font, x, y, lh, line++, "§c§lDanger", 0xFF5555);
        draw(graphics, font, x, y, lh, line++, " §7Most Pirated: §c" + stats.mostPiratedRoute() + " §7(" + fmt(stats.maxPiracy()) + ")", 0xAAAAAA);
        draw(graphics, font, x, y, lh, line++, " §7Lowest Food: §c" + stats.lowestFoodVillage() + " §7(" + fmt(stats.lowestFoodRatio()) + ")", 0xAAAAAA);
    }

    private void draw(GuiGraphicsExtractor g, Font f, int x, int y, int lh, int line, String text, int color) {
        g.text(f, text, x, y + line * lh, color);
    }

    private String fmt(double d) {
        return String.format("%.1f", d);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bool) {
        if (event.button() == 0) {
            this.onClose();
            return true;
        }
        return super.mouseClicked(event, bool);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}