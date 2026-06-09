package com.viamercatorum.player;

import com.viamercatorum.ViaMercatorum;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.text.DecimalFormat;
import java.util.function.Consumer;

public class MerchantMapItem extends Item {
    private static final DecimalFormat DF = new DecimalFormat("#.##");

    public MerchantMapItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            var map = ViaMercatorum.getInstance().getMerchantMap();
            var seasonManager = ViaMercatorum.getInstance().getSimulationTick().getSeasonManager();
            var regionManager = ViaMercatorum.getInstance().getMarketRegionManager();
            var routeGraph = ViaMercatorum.getInstance().getRouteGraph();
            var bankManager = ViaMercatorum.getInstance().getBankManager();

            player.sendSystemMessage(Component.literal("§6§l═══ Merchant's World Map ═══")
                .withStyle(ChatFormatting.GOLD));

            String seasonStr = switch (seasonManager.getCurrentSeason()) {
                case SPRING -> "§a§lSpring §r§7\u2601";
                case SUMMER -> "§e§lSummer §r§7\u2600";
                case AUTUMN -> "§6§lAutumn §r§7\ud83c\udf42";
                case WINTER -> "§b§lWinter §r§7\u2744";
            };
            player.sendSystemMessage(Component.literal("  Season: " + seasonStr));

            double rep = ViaMercatorum.getInstance().getReputationSystem().getGlobalReputation(player.getUUID());
            player.sendSystemMessage(Component.literal("  §7Reputation: §f" + DF.format(rep)));

            var playerLoans = bankManager.getPlayerLoans(player.getStringUUID());
            if (!playerLoans.isEmpty()) {
                double totalDebt = playerLoans.stream().mapToDouble(a -> a.getTotalDebt()).sum();
                player.sendSystemMessage(Component.literal("  §cDebt: " + DF.format(totalDebt) + " emeralds"));
            }

            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("§6§l── Discovered Villages ──")
                .withStyle(ChatFormatting.GOLD));

            for (var village : map.getDiscoveredVillages()) {
                String typeIcon = switch (village.type()) {
                    case HAMLET -> "§7\u2618";
                    case VILLAGE -> "§f\u2618";
                    case TOWN -> "§e\u2609";
                    case CITY -> "§6\u2609";
                    case FORTIFIED -> "§c\u2694";
                };
                String coastalIcon = village.isCoastal() ? " §9\u2693" : "";
                String regionLabel = village.regionId() != null ? " §7[R:" + village.regionId() + "]" : "";

                player.sendSystemMessage(Component.literal(
                    String.format("  %s §f%s§7(%d,%d)%s GDP:§a%.0f Pros:§e%.2f%s",
                        typeIcon, village.name(),
                        village.chunkX(), village.chunkZ(), regionLabel,
                        village.gdp(), village.prosperity(), coastalIcon
                    )
                ));

                var region = regionManager.getRegionForVillage(village.id());
                if (region != null) {
                    player.sendSystemMessage(Component.literal("    §7Market: §f" + region.getName()));
                }
            }

            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("§6§l── Trade Routes ──")
                .withStyle(ChatFormatting.GOLD));

            for (var route : routeGraph.getAllRoutes()) {
                String typeIcon = route.isSeaRoute() ? "§9\u2693" : "§6\u2693";
                String qualityStr = switch (route.getQuality()) {
                    case DIRT -> "§7Dirt";
                    case GRAVEL -> "§fGravel";
                    case STONE -> "§eStone";
                    case PAVED -> "§6Paved";
                };
                String maintainStr = String.format("%.0f%%", route.getMaintenanceLevel() * 100);
                String piracyStr = route.getPiracyScore() > 0.3 ? " §c\u2694" : "";

                player.sendSystemMessage(Component.literal(
                    String.format("  %s §f%s\u2192%s %s §7[M:%s]%s",
                        typeIcon,
                        route.getFrom().getName(), route.getTo().getName(),
                        qualityStr, maintainStr, piracyStr
                    )
                ));
            }
        }
        return level.isClientSide() ? InteractionResult.PASS : InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> consumer, TooltipFlag flag) {
        consumer.accept(Component.literal("Right-click to view the world map").withStyle(ChatFormatting.GRAY));
        consumer.accept(Component.literal("Shows villages, routes, GDP, and more").withStyle(ChatFormatting.GRAY));
    }
}