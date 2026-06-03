package com.viamercatorum.player;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.economy.PriceCalculator;
import com.viamercatorum.core.economy.Supply;
import com.viamercatorum.core.economy.TradeGood;
import com.viamercatorum.core.economy.TradeGoodRegistry;
import com.viamercatorum.core.world.Caravan;
import com.viamercatorum.core.world.TradeRoute;
import com.viamercatorum.core.world.VillageNode;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class MerchantScreenHandler extends AbstractContainerMenu {
    private String villageId;
    private String caravanId;
    private boolean isCaravan;

    public record TradeEntry(String goodId, String name, double buyPrice, double sellPrice, double stock, double maxCapacity) {}

    public MerchantScreenHandler(int syncId, Inventory inv) {
        this(syncId, inv, (String) null);
    }

    public MerchantScreenHandler(int syncId, Inventory inv, String villageId) {
        super(ModScreenHandlers.MERCHANT, syncId);
        this.villageId = villageId;
        this.isCaravan = false;
        addPlayerSlots(inv);
    }

    public MerchantScreenHandler(int syncId, Inventory inv, String caravanId, boolean isCaravan) {
        super(ModScreenHandlers.MERCHANT, syncId);
        this.caravanId = caravanId;
        this.isCaravan = isCaravan;
        addPlayerSlots(inv);
    }

    private void addPlayerSlots(Inventory inv) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(inv, j + i * 9 + 9, 108 + j * 18, 140 + i * 18));
            }
        }
        for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(inv, j, 108 + j * 18, 198));
        }
    }

    public void setVillageId(String villageId) {
        this.villageId = villageId;
    }

    public boolean isCaravan() { return isCaravan; }
    public String getCaravanId() { return caravanId; }

    public List<TradeEntry> getTradeEntries() {
        if (isCaravan && caravanId != null) {
            return getCaravanEntries();
        }
        return getVillageEntries();
    }

    private List<TradeEntry> getVillageEntries() {
        if (villageId == null) return List.of();
        VillageNode village = ViaMercatorum.getInstance().getRouteGraph().getVillage(villageId);
        if (village == null) return List.of();

        double reputation = village.getReputation();
        List<TradeEntry> entries = new ArrayList<>();

        for (Map.Entry<String, Supply> entry : village.getSupplies().entrySet()) {
            Supply supply = entry.getValue();
            TradeGood good = supply.getGood();
            String goodId = entry.getKey();
            double demand = supply.getMaxCapacity() * 0.5;
            double buyPrice = PriceCalculator.calculateBuyPrice(good, supply.getStock(), demand, reputation);
            double sellPrice = PriceCalculator.calculateSellPrice(good, supply.getStock(), demand, reputation);
            entries.add(new TradeEntry(goodId, good.name(), buyPrice, sellPrice, supply.getStock(), supply.getMaxCapacity()));
        }

        entries.sort(Comparator.comparing(e -> e.name));
        return entries;
    }

    private List<TradeEntry> getCaravanEntries() {
        Caravan caravan = ViaMercatorum.getInstance().getRouteGraph().getCaravan(caravanId);
        if (caravan == null) return List.of();

        TradeRoute route = ViaMercatorum.getInstance().getRouteGraph().getRoute(caravan.getRouteId());
        double riskPremium = route != null ? route.getRiskPremium() : 1.0;

        List<TradeEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : caravan.getCargo().entrySet()) {
            TradeGood good = TradeGoodRegistry.get(entry.getKey());
            if (good == null) continue;
            if (entry.getKey().equals("emerald")) continue;
            double stock = entry.getValue();
            double buyPrice = PriceCalculator.calculateCaravanBuyPrice(good, riskPremium);
            double sellPrice = PriceCalculator.calculateCaravanSellPrice(good, riskPremium);
            entries.add(new TradeEntry(entry.getKey(), good.name(), buyPrice, sellPrice, stock, stock));
        }
        entries.sort(Comparator.comparing(e -> e.name));
        return entries;
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!isCaravan || caravanId == null) return false;

        Caravan caravan = ViaMercatorum.getInstance().getRouteGraph().getCaravan(caravanId);
        if (caravan == null) return false;

        List<TradeEntry> entries = getCaravanEntries();
        int index;
        boolean isBuy;

        if (button < 100) {
            index = button;
            isBuy = true;
        } else {
            index = button - 100;
            isBuy = false;
        }

        if (index < 0 || index >= entries.size()) return false;
        TradeEntry entry = entries.get(index);

        if (isBuy) {
            return PlayerTradeManager.caravanBuy(player, caravan, entry.goodId(), 1);
        } else {
            return PlayerTradeManager.caravanSell(player, caravan, entry.goodId(), 1);
        }
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
