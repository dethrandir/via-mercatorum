package com.viamercatorum.core.simulation;

import com.viamercatorum.core.economy.TradeGood;
import com.viamercatorum.core.economy.TradeGoodRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Character {
    private final String id;
    private String characterTypeId;
    private CharacterRole role;
    private String name;
    private String homeVillageId;
    private double wealth;
    private double reputation;
    private double powerScore;
    private boolean alive;
    private final Map<String, Double> inventory = new ConcurrentHashMap<>();

    private String governedVillageId;
    private String leadershipTitle;
    private final List<String> questIds = new ArrayList<>();

    private String caravanId;

    private final List<String> ownedWorkshopIds = new ArrayList<>();
    private final Map<String, Integer> tradeOffers = new ConcurrentHashMap<>();

    public Character(String id, String characterTypeId, CharacterRole role, String name, String homeVillageId) {
        this.id = id;
        this.characterTypeId = characterTypeId;
        this.role = role;
        this.name = name;
        this.homeVillageId = homeVillageId;
        this.wealth = 0;
        this.reputation = 0;
        this.powerScore = 0;
        this.alive = true;
    }

    public static Character createLeader(String characterTypeId, String name, String homeVillageId) {
        String id = "leader_" + UUID.randomUUID().toString().substring(0, 8);
        Character c = new Character(id, characterTypeId, CharacterRole.LEADER, name, homeVillageId);
        c.governedVillageId = homeVillageId;
        return c;
    }

    public static Character createCaravanLeader(String characterTypeId, String name, String homeVillageId, String caravanId) {
        String id = "caravan_lead_" + UUID.randomUUID().toString().substring(0, 8);
        Character c = new Character(id, characterTypeId, CharacterRole.CARAVAN_LEADER, name, homeVillageId);
        c.caravanId = caravanId;
        return c;
    }

    public static Character createMerchant(String characterTypeId, String name, String homeVillageId) {
        String id = "merchant_" + UUID.randomUUID().toString().substring(0, 8);
        return new Character(id, characterTypeId, CharacterRole.MERCHANT, name, homeVillageId);
    }

    public void addGoods(String goodId, double amount) {
        inventory.merge(goodId, amount, Double::sum);
        double current = inventory.get(goodId);
        if (current <= 0.01) {
            inventory.remove(goodId);
        }
    }

    public double removeGoods(String goodId, double amount) {
        double available = inventory.getOrDefault(goodId, 0.0);
        double removed = Math.min(available, amount);
        if (removed > 0) {
            inventory.merge(goodId, -removed, Double::sum);
            double current = inventory.get(goodId);
            if (current <= 0.01) {
                inventory.remove(goodId);
            }
        }
        return removed;
    }

    public void addWealth(double amount) {
        this.wealth += amount;
    }

    public void addReputation(double amount) {
        this.reputation = Math.max(-100, Math.min(100, this.reputation + amount));
    }

    public void recalculatePower(CharacterTypeRegistry registry) {
        double power = wealth * 0.1;
        for (Map.Entry<String, Double> entry : inventory.entrySet()) {
            TradeGood good = TradeGoodRegistry.get(entry.getKey());
            if (good != null && (good.category() == TradeGood.Category.WEAPON || good.category() == TradeGood.Category.ARMOR)) {
                power += entry.getValue() * 2.0;
            }
        }
        CharacterTypeRegistry.CharacterType type = registry.getType(characterTypeId);
        if (type != null) {
            power *= type.powerMultiplier();
        }
        if (role == CharacterRole.LEADER) {
            power += 10.0;
        }
        this.powerScore = power;
    }

    public void addQuest(String questId) {
        if (!questIds.contains(questId)) {
            questIds.add(questId);
        }
    }

    public void removeQuest(String questId) {
        questIds.remove(questId);
    }

    public void addOwnedWorkshop(String workshopInstanceId) {
        if (!ownedWorkshopIds.contains(workshopInstanceId)) {
            ownedWorkshopIds.add(workshopInstanceId);
        }
    }

    public void removeOwnedWorkshop(String workshopInstanceId) {
        ownedWorkshopIds.remove(workshopInstanceId);
    }

    public void die() {
        this.alive = false;
    }

    public String getId() { return id; }
    public String getCharacterTypeId() { return characterTypeId; }
    public void setCharacterTypeId(String characterTypeId) { this.characterTypeId = characterTypeId; }
    public CharacterRole getRole() { return role; }
    public void setRole(CharacterRole role) { this.role = role; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getHomeVillageId() { return homeVillageId; }
    public void setHomeVillageId(String homeVillageId) { this.homeVillageId = homeVillageId; }
    public double getWealth() { return wealth; }
    public void setWealth(double wealth) { this.wealth = wealth; }
    public double getReputation() { return reputation; }
    public void setReputation(double reputation) { this.reputation = Math.max(-100, Math.min(100, reputation)); }
    public double getPowerScore() { return powerScore; }
    public void setPowerScore(double powerScore) { this.powerScore = powerScore; }
    public boolean isAlive() { return alive; }
    public Map<String, Double> getInventory() { return inventory; }

    public String getGovernedVillageId() { return governedVillageId; }
    public void setGovernedVillageId(String governedVillageId) { this.governedVillageId = governedVillageId; }
    public String getLeadershipTitle() { return leadershipTitle; }
    public void setLeadershipTitle(String leadershipTitle) { this.leadershipTitle = leadershipTitle; }
    public List<String> getQuestIds() { return questIds; }

    public String getCaravanId() { return caravanId; }
    public void setCaravanId(String caravanId) { this.caravanId = caravanId; }

    public List<String> getOwnedWorkshopIds() { return ownedWorkshopIds; }
    public Map<String, Integer> getTradeOffers() { return tradeOffers; }

    public boolean isLeader() { return role == CharacterRole.LEADER; }
    public boolean isCaravanLeader() { return role == CharacterRole.CARAVAN_LEADER; }
    public boolean isMerchant() { return role == CharacterRole.MERCHANT; }

    public boolean canOwnWorkshop() {
        return role == CharacterRole.LEADER || role == CharacterRole.MERCHANT;
    }
}