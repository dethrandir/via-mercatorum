package com.viamercatorum.core.simulation;

import com.viamercatorum.core.economy.TradeGood;
import com.viamercatorum.core.economy.TradeGoodRegistry;

import java.util.*;

public class CharacterWealthTest {
    public static void main(String[] args) {
        System.out.println("=== Character Wealth Test (Standalone) ===");
        System.out.println();

        TradeGoodRegistry.getAll();

        CharacterTypeRegistry typeRegistry = new CharacterTypeRegistry();
        typeRegistry.registerDefaults();

        List<Character> characters = new ArrayList<>();
        Random rng = new Random(42);

        String[] villageTypes = {"HAMLET", "VILLAGE", "TOWN", "CITY", "FORTIFIED"};
        String[] villageNames = {"Yesilkoy", "Karakoy", "Aksehir", "Madenkent", "Hisar", "Demirkapi", "Bogazkale", "Cukurkoy", "Yaylakent", "Tekkekoy", "Sahilkent", "Kizilkale", "Golbasi", "Ortakoy", "Yukarikoy", "Asagisehir", "Yenicekoy", "Eskikale", "Boztepe", "Derebucak", "Kucuksehir", "Buyuksehir", "Tepesehir", "Vadikent", "Ormanli", "Caykoy", "Dagsehir", "Ovasehir", "Ucurumkoy", "Barajkoy"};

        for (int i = 0; i < 30; i++) {
            String villageId = "village_" + i;
            String villageName = villageNames[i % villageNames.length];
            String villageType = villageTypes[rng.nextInt(villageTypes.length)];

            Character leader = Character.createLeader(
                typeRegistry.getType("village_elder") != null && (villageType.equals("CITY") || villageType.equals("TOWN"))
                    ? "merchant_lord" : (villageType.equals("FORTIFIED") ? "warlord" : "village_elder"),
                "NPC_Leader_" + i,
                villageId
            );
            leader.setLeadershipTitle(getLeaderTitle(villageType));
            leader.setGovernedVillageId(villageId);

            double wealthBase = getWealthBase(villageType);
            leader.setWealth(wealthBase * (1.0 + rng.nextDouble() * 2.0));
            leader.setReputation(getRepBase(villageType) * (1.0 + rng.nextDouble() * 0.5));
            leader.recalculatePower(typeRegistry);
            characters.add(leader);

            int merchantCount = getMerchantCount(villageType);
            String[] merchantTypes = {"workshop_owner", "banker", "guild_master"};
            for (int m = 0; m < merchantCount; m++) {
                String mType = merchantTypes[rng.nextInt(merchantTypes.length)];
                Character merchant = Character.createMerchant(mType, "NPC_Merchant_" + i + "_" + m, villageId);
                merchant.setWealth(20.0 + rng.nextDouble() * 150.0);

                String[] goodIds = {"wheat", "iron_ingot", "coal", "fish", "bread", "leather", "wood"};
                for (int g = 0; g < 2 + rng.nextInt(3); g++) {
                    String goodId = goodIds[rng.nextInt(goodIds.length)];
                    double amount = 5.0 + rng.nextDouble() * 30.0;
                    merchant.addGoods(goodId, amount);
                }
                merchant.recalculatePower(typeRegistry);
                characters.add(merchant);
            }
        }

        // Add some caravan leaders
        for (int i = 0; i < 5; i++) {
            Character caravanLeader = Character.createCaravanLeader(
                i % 2 == 0 ? "ship_captain" : "captain",
                "NPC_CaravanLead_" + i,
                "village_" + rng.nextInt(30),
                "caravan_" + i
            );
            caravanLeader.setWealth(50.0 + rng.nextDouble() * 100.0);
            caravanLeader.recalculatePower(typeRegistry);
            characters.add(caravanLeader);
        }

        System.out.println();
        System.out.println("=== Top 3 Richest Characters ===");
        System.out.println();

        characters.sort((a, b) -> Double.compare(b.getWealth(), a.getWealth()));

        for (int i = 0; i < Math.min(3, characters.size()); i++) {
            Character c = characters.get(i);
            CharacterTypeRegistry.CharacterType type = typeRegistry.getType(c.getCharacterTypeId());
            String typeName = type != null ? type.name() : "?";

            System.out.println("  #" + (i + 1) + ": " + c.getName() + " [" + typeName + "]");
            System.out.println("       Role: " + c.getRole());
            if (c.getLeadershipTitle() != null) System.out.println("       Title: " + c.getLeadershipTitle());
            System.out.println("       Wealth: " + String.format("%.1f", c.getWealth()) + " emeralds");
            System.out.println("       Power Score: " + String.format("%.1f", c.getPowerScore()));
            System.out.println("       Reputation: " + String.format("%.1f", c.getReputation()));
            System.out.println("       Home: " + c.getHomeVillageId());
            if (!c.getInventory().isEmpty()) {
                System.out.println("       Inventory:");
                for (var entry : c.getInventory().entrySet()) {
                    System.out.println("         - " + entry.getKey() + ": " + String.format("%.1f", entry.getValue()));
                }
            } else {
                System.out.println("       Inventory: (empty)");
            }
            System.out.println();
        }

        System.out.println("=== Summary ===");
        System.out.println("  Total Characters: " + characters.size());
        System.out.println("  Leaders: " + characters.stream().filter(Character::isLeader).count());
        System.out.println("  Merchants: " + characters.stream().filter(Character::isMerchant).count());
        System.out.println("  Caravan Leaders: " + characters.stream().filter(Character::isCaravanLeader).count());
        System.out.println("  Avg Wealth: " + String.format("%.1f", characters.stream().mapToDouble(Character::getWealth).average().orElse(0)));
        System.out.println("  Total Wealth: " + String.format("%.1f", characters.stream().mapToDouble(Character::getWealth).sum()) + " emeralds");
    }

    private static String getLeaderTitle(String villageType) {
        return switch (villageType) {
            case "HAMLET" -> "Muhtar";
            case "VILLAGE" -> "Muhtar";
            case "TOWN" -> "Baskan";
            case "CITY" -> "Lord";
            case "FORTIFIED" -> "Komutan";
            default -> "Muhtar";
        };
    }

    private static double getWealthBase(String villageType) {
        return switch (villageType) {
            case "HAMLET" -> 50.0;
            case "VILLAGE" -> 200.0;
            case "TOWN" -> 500.0;
            case "CITY" -> 1000.0;
            case "FORTIFIED" -> 300.0;
            default -> 100.0;
        };
    }

    private static double getRepBase(String villageType) {
        return switch (villageType) {
            case "HAMLET" -> 10.0;
            case "VILLAGE" -> 25.0;
            case "TOWN" -> 40.0;
            case "CITY" -> 50.0;
            case "FORTIFIED" -> 30.0;
            default -> 10.0;
        };
    }

    private static int getMerchantCount(String villageType) {
        return switch (villageType) {
            case "HAMLET" -> 1;
            case "VILLAGE" -> 1;
            case "TOWN" -> 2;
            case "CITY" -> 3;
            case "FORTIFIED" -> 2;
            default -> 1;
        };
    }
}