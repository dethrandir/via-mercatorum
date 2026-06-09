package com.viamercatorum.core.simulation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CharacterTypeRegistry {
    private final Map<String, CharacterType> types = new ConcurrentHashMap<>();

    public record CharacterType(
        String id,
        String name,
        double powerMultiplier,
        double wealthRate,
        boolean canTrade,
        boolean canOwnWorkshop,
        boolean canLead,
        boolean canFight,
        String defaultDialogue,
        CharacterRole role
    ) {}

    public void registerType(CharacterType type) {
        types.put(type.id(), type);
    }

    public CharacterType getType(String id) {
        return types.get(id);
    }

    public Map<String, CharacterType> getAllTypes() {
        return types;
    }

    public void registerDefaults() {
        registerType(new CharacterType("village_elder", "Village Elder", 1.5, 0.5, true, true, true, false, "viamercatorum.dialogue.village_elder", CharacterRole.LEADER));
        registerType(new CharacterType("workshop_owner", "Workshop Owner", 1.0, 1.0, true, true, false, false, "viamercatorum.dialogue.workshop_owner", CharacterRole.MERCHANT));
        registerType(new CharacterType("banker", "Banker", 1.2, 2.0, true, false, true, false, "viamercatorum.dialogue.banker", CharacterRole.MERCHANT));
        registerType(new CharacterType("ship_captain", "Ship Captain", 1.3, 1.5, true, false, true, true, "viamercatorum.dialogue.ship_captain", CharacterRole.CARAVAN_LEADER));
        registerType(new CharacterType("merchant_lord", "Merchant Lord", 2.0, 3.0, true, true, true, false, "viamercatorum.dialogue.merchant_lord", CharacterRole.LEADER));
        registerType(new CharacterType("warlord", "Warlord", 2.5, 0.5, true, false, true, true, "viamercatorum.dialogue.warlord", CharacterRole.LEADER));
        registerType(new CharacterType("raider_commander", "Raider Commander", 2.0, 0.3, false, false, true, true, "viamercatorum.dialogue.raider_commander", CharacterRole.MERCHANT));
        registerType(new CharacterType("illager_lord", "Illager Lord", 3.0, 0.2, false, false, true, true, "viamercatorum.dialogue.illager_lord", CharacterRole.LEADER));
        registerType(new CharacterType("captain", "Captain", 1.5, 0.8, true, false, true, true, "viamercatorum.dialogue.captain", CharacterRole.CARAVAN_LEADER));
        registerType(new CharacterType("guild_master", "Guild Master", 2.0, 2.5, true, true, true, false, "viamercatorum.dialogue.guild_master", CharacterRole.MERCHANT));
        registerType(new CharacterType("smuggler", "Smuggler", 1.0, 2.0, true, false, false, true, "viamercatorum.dialogue.smuggler", CharacterRole.CARAVAN_LEADER));
        registerType(new CharacterType("diplomat", "Diplomat", 1.0, 1.0, true, false, true, false, "viamercatorum.dialogue.diplomat", CharacterRole.LEADER));
    }
}