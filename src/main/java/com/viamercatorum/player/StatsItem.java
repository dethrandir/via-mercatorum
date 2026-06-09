package com.viamercatorum.player;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.economy.EconomyHelper;
import com.viamercatorum.core.simulation.Character;
import com.viamercatorum.core.simulation.CharacterRole;
import com.viamercatorum.core.simulation.CharacterTypeRegistry;
import com.viamercatorum.core.world.RouteGraph;
import com.viamercatorum.core.world.TradeRoute;
import com.viamercatorum.core.world.VillageNode;
import com.viamercatorum.network.StatsPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.*;

public class StatsItem extends Item {
    public StatsItem() {
        super(new Item.Properties());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            StatsPayload payload = buildStats();
            ServerPlayNetworking.send(sp, payload);
        }
        return InteractionResult.SUCCESS;
    }

    public static StatsPayload buildStats() {
        ViaMercatorum vm = ViaMercatorum.getInstance();
        RouteGraph graph = vm.getRouteGraph();
        var charManager = vm.getCharacterManager();

        int totalVillages = graph.getAllVillages().size();
        int totalRoutes = graph.getAllRoutes().size();
        int totalCaravans = graph.getAllCaravans().size();

        var activeChars = charManager.getAllCharacters().stream().filter(Character::isAlive).toList();
        int totalChars = activeChars.size();
        int leaders = (int) activeChars.stream().filter(Character::isLeader).count();
        int merchants = (int) activeChars.stream().filter(Character::isMerchant).count();
        int caravanLeaders = (int) activeChars.stream().filter(Character::isCaravanLeader).count();
        double totalWealth = activeChars.stream().mapToDouble(Character::getWealth).sum();

        double avgFood = graph.getAllVillages().stream().mapToDouble(VillageNode::getFoodRatio).average().orElse(0);

        List<StatsPayload.VillageStat> topVillages = graph.getAllVillages().stream()
            .sorted(Comparator.comparingDouble(EconomyHelper::gdp).reversed())
            .limit(3)
            .map(v -> new StatsPayload.VillageStat(v.getName(), v.getType().name(), v.getPopulation(), v.getMaxPopulation(),
                v.getFoodRatio(), EconomyHelper.gdp(v), v.getReputation()))
            .toList();

        CharacterTypeRegistry typeReg = charManager.getTypeRegistry();
        List<StatsPayload.CharacterStat> topCharacters = activeChars.stream()
            .sorted(Comparator.comparingDouble(Character::getWealth).reversed())
            .limit(3)
            .map(c -> {
                var type = typeReg.getType(c.getCharacterTypeId());
                return new StatsPayload.CharacterStat(c.getName(), type != null ? type.name() : "?",
                    c.getRole().name(), c.getWealth(), c.getPowerScore(), c.getReputation(),
                    c.getHomeVillageId(), c.getOwnedWorkshopIds().size());
            })
            .toList();

        double maxPiracy = 0;
        String mostPirated = "N/A";
        for (TradeRoute r : graph.getAllRoutes()) {
            if (r.getPiracyScore() > maxPiracy) {
                maxPiracy = r.getPiracyScore();
                mostPirated = r.getFrom().getName() + " -> " + r.getTo().getName();
            }
        }

        double lowestFood = Double.MAX_VALUE;
        String lowestFoodV = "N/A";
        for (VillageNode v : graph.getAllVillages()) {
            if (v.getFoodRatio() < lowestFood) {
                lowestFood = v.getFoodRatio();
                lowestFoodV = v.getName();
            }
        }

        return new StatsPayload(totalVillages, totalRoutes, totalCaravans, totalChars,
            leaders, merchants, caravanLeaders, totalWealth, avgFood,
            topVillages, topCharacters, mostPirated, maxPiracy, lowestFoodV, lowestFood);
    }
}