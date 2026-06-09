package com.viamercatorum.core.simulation;

import java.util.UUID;

public record ActiveModifier(
    String modifierId,
    String sourceEventId,
    String scope,
    String targetId,
    String effectType,
    String target,
    double percentage,
    double amount,
    long startTick,
    long durationTicks
) {
    public boolean isExpired(long currentTick) {
        return currentTick >= startTick + durationTicks;
    }

    public double getRemainingFraction(long currentTick) {
        if (durationTicks <= 0) return 0;
        long remaining = (startTick + durationTicks) - currentTick;
        return Math.max(0, (double) remaining / durationTicks);
    }

    public static ActiveModifier create(String sourceEventId, String scope, String targetId,
                                         String effectType, String target, double percentage,
                                         double amount, long startTick, long durationTicks) {
        return new ActiveModifier(
            UUID.randomUUID().toString().substring(0, 8),
            sourceEventId, scope, targetId,
            effectType, target, percentage, amount,
            startTick, durationTicks
        );
    }
}