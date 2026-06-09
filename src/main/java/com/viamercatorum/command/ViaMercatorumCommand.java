package com.viamercatorum.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.economy.EconomyHelper;
import com.viamercatorum.core.simulation.SeasonManager;
import com.viamercatorum.core.world.RouteGraph;
import com.viamercatorum.core.world.VillageNode;
import com.viamercatorum.core.world.TradeRoute;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.text.DecimalFormat;

public class ViaMercatorumCommand {
    private static final DecimalFormat DF = new DecimalFormat("#.##");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("viamercatorum")
            .then(Commands.literal("status")
                .executes(ctx -> executeStatus(ctx.getSource())))
            .then(Commands.literal("villages")
                .executes(ctx -> executeVillages(ctx.getSource())))
            .then(Commands.literal("reload")
                .executes(ctx -> executeReload(ctx.getSource())))
            .then(Commands.literal("season")
                .executes(ctx -> executeSeasonInfo(ctx.getSource()))
                .then(Commands.argument("season", StringArgumentType.string())
                    .executes(ctx -> executeSeasonSet(ctx.getSource(),
                        StringArgumentType.getString(ctx, "season")))))
        );
    }

    private static int executeStatus(CommandSourceStack source) {
        var instance = ViaMercatorum.getInstance();
        var tick = instance.getSimulationTick();
        var routeGraph = instance.getRouteGraph();
        var seasonManager = tick.getSeasonManager();
        var regionManager = instance.getMarketRegionManager();

        source.sendSuccess(() -> Component.literal("§6§l═══ Via Mercatorum Status ═══"), false);
        source.sendSuccess(() -> Component.literal("  §7Tick: §f" + tick.getTickCounter()), false);
        source.sendSuccess(() -> Component.literal("  §7Season: §f" + seasonManager.getCurrentSeason().name()), false);
        source.sendSuccess(() -> Component.literal("  §7Season Progress: §f" + DF.format(seasonManager.getSeasonProgress() * 100) + "%"), false);
        source.sendSuccess(() -> Component.literal("  §7Villages: §f" + routeGraph.getAllVillages().size()), false);
        source.sendSuccess(() -> Component.literal("  §7Routes: §f" + routeGraph.getAllRoutes().size()
            + " §7(Sea: " + routeGraph.getAllRoutes().stream().filter(TradeRoute::isSeaRoute).count() + ")"), false);
        source.sendSuccess(() -> Component.literal("  §7Active Caravans: §f" + routeGraph.getAllCaravans().size()), false);
        source.sendSuccess(() -> Component.literal("  §7Market Regions: §f" + regionManager.getAllRegions().size()), false);
        source.sendSuccess(() -> Component.literal("  §7Bank Loans: §f" + instance.getBankManager().getAllLoans().size()), false);
        return 1;
    }

    private static int executeVillages(CommandSourceStack source) {
        var instance = ViaMercatorum.getInstance();
        var routeGraph = instance.getRouteGraph();
        var seasonManager = instance.getSimulationTick().getSeasonManager();

        source.sendSuccess(() -> Component.literal("§6§l═══ Village List ═══"), false);

        for (VillageNode village : routeGraph.getAllVillages()) {
            double gdp = EconomyHelper.gdp(village);
            double prosperity = EconomyHelper.prosperityIndex(village);
            double harvestMod = seasonManager.getHarvestModifier(village.getBiome());
            String coastal = village.isCoastal() ? " §9⚓" : "";
            String region = village.getRegionId() != null ? " §7[R:" + village.getRegionId() + "]" : "";

            source.sendSuccess(() -> Component.literal(
                String.format("  §f%s §7(%s) §7pos:(%d,%d) GDP:§a%s Pros:§e%s Season:§a%s%s%s",
                    village.getName(), village.getType().name(),
                    village.getChunkX(), village.getChunkZ(),
                    DF.format(gdp), DF.format(prosperity),
                    DF.format(harvestMod), coastal, region
                )
            ), false);
        }
        return routeGraph.getAllVillages().size();
    }

    private static int executeReload(CommandSourceStack source) {
        var instance = ViaMercatorum.getInstance();
        instance.reloadConfig();
        source.sendSuccess(() -> Component.literal("§aVia Mercatorum config reloaded."), false);
        return 1;
    }

    private static int executeSeasonInfo(CommandSourceStack source) {
        var seasonManager = ViaMercatorum.getInstance().getSimulationTick().getSeasonManager();
        source.sendSuccess(() -> Component.literal("§7Current season: §f" + seasonManager.getCurrentSeason().name()
            + " §7(Progress: " + DF.format(seasonManager.getSeasonProgress() * 100) + "%)"), false);
        return 1;
    }

    private static int executeSeasonSet(CommandSourceStack source, String seasonName) {
        try {
            SeasonManager.Season season = SeasonManager.Season.valueOf(seasonName.toUpperCase());
            ViaMercatorum.getInstance().getSimulationTick().getSeasonManager().forceSeason(season);
            source.sendSuccess(() -> Component.literal("§aSeason set to §f" + season.name()), false);
            return 1;
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("§cInvalid season. Use: SPRING, SUMMER, AUTUMN, WINTER"));
            return 0;
        }
    }
}