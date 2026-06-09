package com.viamercatorum.core.simulation;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.world.RouteGraph;
import com.viamercatorum.core.world.VillageNode;
import com.viamercatorum.data.LeaderProfile;
import com.viamercatorum.data.NamePool;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CharacterManager {
    private final Map<String, Character> characters = new ConcurrentHashMap<>();
    private final CharacterTypeRegistry typeRegistry = new CharacterTypeRegistry();
    private long nameCounter = 0;

    public CharacterManager() {
        typeRegistry.registerDefaults();
    }

    public Character createCharacter(String characterTypeId, String name, String homeVillageId) {
        String id = "char_" + UUID.randomUUID().toString().substring(0, 8);
        CharacterTypeRegistry.CharacterType type = typeRegistry.getType(characterTypeId);
        CharacterRole role = type != null ? type.role() : CharacterRole.MERCHANT;
        Character character = new Character(id, characterTypeId, role, name, homeVillageId);
        characters.put(id, character);
        return character;
    }

    public Character createLeaderForVillage(VillageNode village) {
        LeaderProfile profile = ViaMercatorum.getInstance().getLeaderProfileLoader().getProfile(village.getType().name());
        String typeId;
        String title;
        double powerBase = 10.0;
        double wealthBase = 100.0;
        double repBase = 10.0;

        if (profile != null) {
            typeId = profile.leaderTypeId();
            title = profile.leadershipTitle();
            powerBase = profile.powerBase();
            wealthBase = profile.wealthBase();
            repBase = profile.reputationBase();

            String existingLeaderId = getLeaderOfVillage(village.getId());
            if (existingLeaderId != null) {
                Character existing = characters.get(existingLeaderId);
                if (existing != null && existing.isAlive()) return existing;
            }
        } else {
            typeId = "village_elder";
            title = "Muhtar";
        }

        String name = pickNameFromPool("leader_names");
        Character leader = Character.createLeader(typeId, name, village.getId());
        leader.setLeadershipTitle(title);
        leader.setGovernedVillageId(village.getId());
        leader.setWealth(wealthBase);
        leader.setReputation(repBase);
        leader.setPowerScore(powerBase);
        leader.addWealth(wealthBase);

        characters.put(leader.getId(), leader);
        return leader;
    }

    public Character createCaravanLeaderForCaravan(String caravanId, VillageNode origin) {
        String name = pickRandomName("caravan_leader_names");
        String typeId = origin.isCoastal() ? "ship_captain" : "captain";
        Character leader = Character.createCaravanLeader(typeId, name, origin.getId(), caravanId);
        characters.put(leader.getId(), leader);
        return leader;
    }

    public Character createMerchantForVillage(VillageNode village) {
        String name = pickRandomName("merchant_names");
        String[] merchantTypes = {"workshop_owner", "banker", "guild_master"};
        String typeId = merchantTypes[(int) (Math.random() * merchantTypes.length)];
        Character merchant = Character.createMerchant(typeId, name, village.getId());
        merchant.setWealth(20.0 + Math.random() * 80.0);
        characters.put(merchant.getId(), merchant);
        return merchant;
    }

    public Character getCharacter(String id) {
        return characters.get(id);
    }

    public Collection<Character> getAllCharacters() {
        return characters.values();
    }

    public Collection<Character> getCharactersByVillage(String villageId) {
        return characters.values().stream()
            .filter(c -> villageId.equals(c.getHomeVillageId()))
            .filter(Character::isAlive)
            .collect(Collectors.toList());
    }

    public Collection<Character> getCharactersByType(String characterTypeId) {
        return characters.values().stream()
            .filter(c -> characterTypeId.equals(c.getCharacterTypeId()))
            .filter(Character::isAlive)
            .collect(Collectors.toList());
    }

    public Collection<Character> getCharactersByRole(CharacterRole role) {
        return characters.values().stream()
            .filter(c -> c.getRole() == role)
            .filter(Character::isAlive)
            .collect(Collectors.toList());
    }

    public String getLeaderOfVillage(String villageId) {
        return characters.values().stream()
            .filter(c -> c.isLeader() && villageId.equals(c.getGovernedVillageId()) && c.isAlive())
            .map(Character::getId)
            .findFirst()
            .orElse(null);
    }

    public Collection<Character> getMerchantsOfVillage(String villageId) {
        return characters.values().stream()
            .filter(c -> c.isMerchant() && villageId.equals(c.getHomeVillageId()) && c.isAlive())
            .collect(Collectors.toList());
    }

    public Character getCaravanLeader(String caravanId) {
        return characters.values().stream()
            .filter(c -> c.isCaravanLeader() && caravanId.equals(c.getCaravanId()) && c.isAlive())
            .findFirst()
            .orElse(null);
    }

    public Collection<Character> getCaravanLeaders() {
        return characters.values().stream()
            .filter(c -> c.isCaravanLeader() && c.isAlive())
            .collect(Collectors.toList());
    }

    public void removeCharacter(String id) {
        characters.remove(id);
    }

    public void removeCaravanLeaders(String caravanId) {
        characters.values().stream()
            .filter(c -> caravanId.equals(c.getCaravanId()))
            .map(Character::getId)
            .toList()
            .forEach(characters::remove);
    }

    public void handleCharacterDeath(String characterId) {
        Character character = characters.get(characterId);
        if (character == null) return;

        character.die();

        if (character.isLeader()) {
            String governedVillageId = character.getGovernedVillageId();
            if (governedVillageId != null) {
                VillageNode village = ViaMercatorum.getInstance().getRouteGraph().getVillage(governedVillageId);
                if (village != null) {
                    createLeaderForVillage(village);
                }
            }
        }
    }

    public int getDesiredMerchantCount(VillageNode village) {
        return switch (village.getType()) {
            case HAMLET -> 1;
            case VILLAGE -> 1;
            case TOWN -> 2;
            case CITY -> 3;
            case FORTIFIED -> 2;
        };
    }

    public void ensureVillageCharacters(VillageNode village) {
        String villageId = village.getId();

        String leaderId = getLeaderOfVillage(villageId);
        if (leaderId == null) {
            createLeaderForVillage(village);
        }

        long currentMerchants = getMerchantsOfVillage(villageId).size();
        int desired = getDesiredMerchantCount(village);
        for (int i = (int) currentMerchants; i < desired; i++) {
            createMerchantForVillage(village);
        }
    }

    public void tick(RouteGraph routeGraph) {
        for (Character character : characters.values()) {
            if (!character.isAlive()) continue;

            CharacterTypeRegistry.CharacterType type = typeRegistry.getType(character.getCharacterTypeId());
            if (type == null) continue;

            if (type.canTrade()) {
                character.addWealth(type.wealthRate() * 0.01);
            }

            character.recalculatePower(typeRegistry);

            VillageNode village = routeGraph.getVillage(character.getHomeVillageId());
            if (village != null && Math.random() < 0.001) {
                character.addReputation(Math.random() > 0.5 ? 0.1 : -0.05);
            }
        }
    }

    public CharacterTypeRegistry getTypeRegistry() {
        return typeRegistry;
    }

    private String pickNameFromPool(String poolId) {
        NamePool pool = ViaMercatorum.getInstance().getNamePoolLoader().getPool(poolId);
        if (pool != null && !pool.names().isEmpty()) {
            return pool.names().get((int) (nameCounter % pool.names().size()));
        }
        nameCounter++;
        return "NPC_" + nameCounter;
    }

    private String pickRandomName(String poolId) {
        NamePool pool = ViaMercatorum.getInstance().getNamePoolLoader().getPool(poolId);
        if (pool != null && !pool.names().isEmpty()) {
            return pool.names().get((int) (Math.random() * pool.names().size()));
        }
        nameCounter++;
        return "NPC_" + nameCounter;
    }
}