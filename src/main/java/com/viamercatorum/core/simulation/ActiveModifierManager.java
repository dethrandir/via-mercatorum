package com.viamercatorum.core.simulation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ActiveModifierManager {
    private final Map<String, ActiveModifier> modifiers = new ConcurrentHashMap<>();

    public void addModifier(ActiveModifier modifier) {
        modifiers.put(modifier.modifierId(), modifier);
    }

    public void tick(long currentTick) {
        modifiers.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired(currentTick)) {
                var bus = com.viamercatorum.ViaMercatorum.getInstance().getEventBus();
                bus.emit(new EconomyEvent.ActiveModifierExpired(
                    "global", currentTick,
                    entry.getValue().modifierId(),
                    entry.getValue().targetId(),
                    entry.getValue().sourceEventId()
                ));
                return true;
            }
            return false;
        });
    }

    public List<ActiveModifier> getModifiersForVillage(String villageId) {
        return modifiers.values().stream()
            .filter(m -> "VILLAGE".equals(m.scope()) && m.targetId().equals(villageId))
            .collect(Collectors.toList());
    }

    public List<ActiveModifier> getModifiersForRegion(String regionId) {
        return modifiers.values().stream()
            .filter(m -> "REGION".equals(m.scope()) && m.targetId().equals(regionId))
            .collect(Collectors.toList());
    }

    public List<ActiveModifier> getGlobalModifiers() {
        return modifiers.values().stream()
            .filter(m -> "GLOBAL".equals(m.scope()))
            .collect(Collectors.toList());
    }

    public List<ActiveModifier> getModifiersForTarget(String targetId) {
        return modifiers.values().stream()
            .filter(m -> m.targetId().equals(targetId))
            .collect(Collectors.toList());
    }

    public Map<String, Double> getProductionModifiers(String villageId, String regionId) {
        Map<String, Double> result = new HashMap<>();
        List<ActiveModifier> applicable = new ArrayList<>(getGlobalModifiers());
        applicable.addAll(getModifiersForVillage(villageId));
        if (regionId != null) {
            applicable.addAll(getModifiersForRegion(regionId));
        }

        for (ActiveModifier mod : applicable) {
            if ("modify_production_rate".equals(mod.effectType()) || "modify_production_category".equals(mod.effectType())) {
                String key = mod.target();
                result.merge(key, 1.0 + mod.percentage(), (old, val) -> old * val);
            }
        }
        return result;
    }

    public Map<String, Double> getDemandModifiers(String villageId, String regionId) {
        Map<String, Double> result = new HashMap<>();
        List<ActiveModifier> applicable = new ArrayList<>(getGlobalModifiers());
        applicable.addAll(getModifiersForVillage(villageId));
        if (regionId != null) {
            applicable.addAll(getModifiersForRegion(regionId));
        }

        for (ActiveModifier mod : applicable) {
            if ("modify_demand".equals(mod.effectType())) {
                String key = mod.target();
                result.merge(key, 1.0 + mod.percentage(), (old, val) -> old * val);
            }
        }
        return result;
    }

    public double getPiracyModifier(String villageId, String regionId) {
        double modifier = 0;
        List<ActiveModifier> applicable = new ArrayList<>(getGlobalModifiers());
        applicable.addAll(getModifiersForVillage(villageId));
        if (regionId != null) {
            applicable.addAll(getModifiersForRegion(regionId));
        }

        for (ActiveModifier mod : applicable) {
            if ("modify_piracy".equals(mod.effectType())) {
                modifier += mod.percentage();
            }
        }
        return modifier;
    }

    public Map<String, ActiveModifier> getAllModifiers() {
        return Collections.unmodifiableMap(modifiers);
    }

    public int size() {
        return modifiers.size();
    }

    public void clear() {
        modifiers.clear();
    }
}