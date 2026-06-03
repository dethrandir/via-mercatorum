package com.viamercatorum.player;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.economy.GoodItemMapping;
import com.viamercatorum.core.economy.PriceCalculator;
import com.viamercatorum.core.economy.Supply;
import com.viamercatorum.core.economy.TradeGood;
import com.viamercatorum.core.economy.TradeGoodRegistry;
import com.viamercatorum.core.world.Caravan;
import com.viamercatorum.core.world.TradeRoute;
import com.viamercatorum.core.world.RouteGraph;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;

public class PlayerTradeManager {
    public static boolean attemptBuy(Player player, String villageId, String goodId, int amount) {
        var village = ViaMercatorum.getInstance().getRouteGraph().getVillage(villageId);
        if (village == null) return false;

        Supply supply = village.getSupply(goodId);
        if (supply == null) return false;

        TradeGood good = supply.getGood();
        double price = PriceCalculator.calculateBuyPrice(good, supply.getStock(),
            supply.getMaxCapacity() * 0.5, village.getReputation());
        double totalCost = price * amount;

        double stock = supply.getStock();
        if (stock < amount) return false;

        int emeraldCost = (int) Math.ceil(totalCost);
        if (!hasEmeralds(player, emeraldCost)) return false;

        supply.consume(amount);
        removeEmeralds(player, emeraldCost);

        ItemStack give = GoodItemMapping.goodToItemStack(goodId, amount);
        if (!give.isEmpty()) {
            player.getInventory().add(give);
        }

        village.addReputation(amount * 0.1);

        var bus = ViaMercatorum.getInstance().getEventBus();
        bus.emit(new com.viamercatorum.core.simulation.EconomyEvent.TradeCompleted(
            villageId, System.currentTimeMillis(), goodId, amount, totalCost
        ));

        ViaMercatorum.getInstance().getReputationSystem()
            .onTradeCompleted(player.getUUID(), villageId, totalCost);

        return true;
    }

    public static boolean attemptSell(Player player, String villageId, String goodId, int amount) {
        var village = ViaMercatorum.getInstance().getRouteGraph().getVillage(villageId);
        if (village == null) return false;

        Supply supply = village.getSupply(goodId);
        if (supply == null) return false;

        TradeGood good = supply.getGood();
        double price = PriceCalculator.calculateSellPrice(good, supply.getStock(),
            supply.getMaxCapacity() * 0.5, village.getReputation());

        ItemStack required = GoodItemMapping.goodToItemStack(goodId, amount);
        if (required.isEmpty()) return false;
        if (!playerHasAtLeast(player, required)) return false;

        supply.addStock(amount);
        removeItemsFromPlayer(player, required);

        int emeraldReward = (int) Math.floor(price * amount);
        if (emeraldReward > 0) {
            player.getInventory().add(new ItemStack(Items.EMERALD, emeraldReward));
        }

        village.addReputation(amount * 0.05);

        var bus = ViaMercatorum.getInstance().getEventBus();
        bus.emit(new com.viamercatorum.core.simulation.EconomyEvent.TradeCompleted(
            villageId, System.currentTimeMillis(), goodId, -amount, price * amount
        ));

        ViaMercatorum.getInstance().getReputationSystem()
            .onTradeCompleted(player.getUUID(), villageId, price * amount);

        return true;
    }

    public static boolean caravanBuy(Player player, Caravan caravan, String goodId, int amount) {
        Double stockObj = caravan.getCargo().get(goodId);
        if (stockObj == null) return false;
        double stock = stockObj;

        TradeGood good = TradeGoodRegistry.get(goodId);
        if (good == null) return false;

        if (stock < amount) return false;

        RouteGraph graph = ViaMercatorum.getInstance().getRouteGraph();
        TradeRoute route = graph.getRoute(caravan.getRouteId());
        double riskPremium = route != null ? route.getRiskPremium() : 1.0;

        double price = PriceCalculator.calculateCaravanBuyPrice(good, riskPremium);
        int emeraldCost = (int) Math.ceil(price * amount);
        if (!hasEmeralds(player, emeraldCost)) return false;

        double newStock = stock - amount;
        if (newStock <= 0) {
            caravan.getCargo().remove(goodId);
        } else {
            caravan.getCargo().put(goodId, newStock);
        }

        removeEmeralds(player, emeraldCost);
        caravan.addEmeralds(emeraldCost);

        ItemStack give = GoodItemMapping.goodToItemStack(goodId, amount);
        if (!give.isEmpty()) {
            player.getInventory().add(give);
        }

        return true;
    }

    public static boolean caravanSell(Player player, Caravan caravan, String goodId, int amount) {
        TradeGood good = TradeGoodRegistry.get(goodId);
        if (good == null) return false;

        ItemStack required = GoodItemMapping.goodToItemStack(goodId, amount);
        if (required.isEmpty()) return false;
        if (!playerHasAtLeast(player, required)) return false;

        RouteGraph graph = ViaMercatorum.getInstance().getRouteGraph();
        TradeRoute route = graph.getRoute(caravan.getRouteId());
        double riskPremium = route != null ? route.getRiskPremium() : 1.0;

        double price = PriceCalculator.calculateCaravanSellPrice(good, riskPremium);
        int emeraldReward = (int) Math.floor(price * amount);
        if (!caravan.spendEmeralds(emeraldReward)) return false;

        removeItemsFromPlayer(player, required);

        double currentStock = caravan.getCargo().getOrDefault(goodId, 0.0);
        caravan.getCargo().put(goodId, currentStock + amount);

        if (emeraldReward > 0) {
            player.getInventory().add(new ItemStack(Items.EMERALD, emeraldReward));
        }

        return true;
    }

    private static boolean hasEmeralds(Player player, int amount) {
        int count = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() == Items.EMERALD) {
                count += stack.getCount();
                if (count >= amount) return true;
            }
        }
        return false;
    }

    private static void removeEmeralds(Player player, int amount) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize() && amount > 0; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() == Items.EMERALD) {
                int toRemove = Math.min(stack.getCount(), amount);
                stack.shrink(toRemove);
                amount -= toRemove;
            }
        }
    }

    private static boolean playerHasAtLeast(Player player, ItemStack required) {
        int count = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, required)) {
                count += stack.getCount();
                if (count >= required.getCount()) return true;
            }
        }
        return false;
    }

    private static void removeItemsFromPlayer(Player player, ItemStack required) {
        int amount = required.getCount();
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize() && amount > 0; i++) {
            ItemStack stack = inv.getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, required)) {
                int toRemove = Math.min(stack.getCount(), amount);
                stack.shrink(toRemove);
                amount -= toRemove;
            }
        }
    }
}
