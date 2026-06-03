package com.viamercatorum.player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SkillSystem {
    private final Map<UUID, PlayerSkills> playerSkills = new ConcurrentHashMap<>();

    public record PlayerSkills(int trading, int sailing, int negotiation, int exploration) {
        public static PlayerSkills empty() { return new PlayerSkills(0, 0, 0, 0); }
    }

    public PlayerSkills getSkills(UUID player) {
        return playerSkills.getOrDefault(player, PlayerSkills.empty());
    }

    public void addExperience(UUID player, SkillType skill, int amount) {
        playerSkills.compute(player, (k, v) -> {
            if (v == null) v = PlayerSkills.empty();
            return switch (skill) {
                case TRADING -> new PlayerSkills(v.trading + amount, v.sailing, v.negotiation, v.exploration);
                case SAILING -> new PlayerSkills(v.trading, v.sailing + amount, v.negotiation, v.exploration);
                case NEGOTIATION -> new PlayerSkills(v.trading, v.sailing, v.negotiation + amount, v.exploration);
                case EXPLORATION -> new PlayerSkills(v.trading, v.sailing, v.negotiation, v.exploration + amount);
            };
        });
    }

    public enum SkillType {
        TRADING, SAILING, NEGOTIATION, EXPLORATION
    }
}
