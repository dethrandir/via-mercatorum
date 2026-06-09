package com.viamercatorum.core.simulation;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.config.ViaMercatorumConfig;
import com.viamercatorum.core.world.RouteGraph;
import com.viamercatorum.core.world.VillageNode;
import com.viamercatorum.data.WorldEventProfile;
import com.viamercatorum.data.WorldEventProfileLoader;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventEngine {
    private final EventTriggerResolver triggerResolver;
    private final EventEffectResolver effectResolver;
    private final ActiveModifierManager modifierManager;
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();
    private final Set<String> firedOnceEvents = ConcurrentHashMap.newKeySet();
    private final Random random = new Random();

    public EventEngine(ActiveModifierManager modifierManager) {
        this.triggerResolver = new EventTriggerResolver();
        this.effectResolver = new EventEffectResolver();
        this.modifierManager = modifierManager;
    }

    public void tick(RouteGraph routeGraph, SeasonManager seasonManager, long currentTick) {
        WorldEventProfileLoader loader = ViaMercatorum.getInstance().getWorldEventProfileLoader();
        if (loader == null) return;

        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        Collection<WorldEventProfile> profiles = loader.getProfiles().values();

        for (WorldEventProfile profile : profiles) {
            if (!profile.repeatable() && firedOnceEvents.contains(profile.eventId())) continue;

            Long lastFired = cooldowns.get(profile.eventId());
            if (lastFired != null && (currentTick - lastFired) < profile.cooldownTicks()) continue;

            if (profile.prerequisiteEvent().isPresent()) {
                String prereq = profile.prerequisiteEvent().get();
                if (!firedOnceEvents.contains(prereq)) continue;
            }

            double probabilityPerTick = 1.0 - Math.pow(1.0 - 1.0 / profile.meanTimeToHappen(), config.event.mtthModifier * profile.weight());

            switch (profile.scope()) {
                case "GLOBAL" -> evaluateGlobal(profile, probabilityPerTick, routeGraph, seasonManager, currentTick);
                case "REGION" -> evaluateRegional(profile, probabilityPerTick, routeGraph, seasonManager, currentTick);
                case "VILLAGE" -> evaluatePerVillage(profile, probabilityPerTick, routeGraph, seasonManager, currentTick);
                default -> ViaMercatorum.LOGGER.warn("Unknown event scope: {}", profile.scope());
            }
        }
    }

    private void evaluateGlobal(WorldEventProfile profile, double probability, RouteGraph routeGraph,
                                 SeasonManager seasonManager, long currentTick) {
        if (random.nextDouble() >= probability) return;

        if (!triggerResolver.evaluateTriggers(profile.triggers(), null, routeGraph, seasonManager)) return;

        effectResolver.applyGlobalEffects(profile.effects(), profile, routeGraph, modifierManager, currentTick);
        cooldowns.put(profile.eventId(), currentTick);
        if (!profile.repeatable()) firedOnceEvents.add(profile.eventId());

        var bus = ViaMercatorum.getInstance().getEventBus();
        bus.emit(new EconomyEvent.WorldEventFired("global", currentTick, profile.eventId(), "GLOBAL", "global"));
    }

    private void evaluateRegional(WorldEventProfile profile, double probability, RouteGraph routeGraph,
                                   SeasonManager seasonManager, long currentTick) {
        if (random.nextDouble() >= probability) return;

        var regionManager = ViaMercatorum.getInstance().getMarketRegionManager();
        for (var region : regionManager.getAllRegions()) {
            VillageNode center = routeGraph.getVillage(region.getMarketCenterId());
            if (center == null) center = routeGraph.getAllVillages().stream().findFirst().orElse(null);
            if (center == null) continue;

            if (!triggerResolver.evaluateTriggers(profile.triggers(), center, routeGraph, seasonManager)) continue;

            for (String villageId : region.getMemberVillageIds()) {
                VillageNode village = routeGraph.getVillage(villageId);
                if (village == null) continue;
                effectResolver.applyEffects(profile.effects(), profile, village, routeGraph, modifierManager, currentTick);
            }

            var bus = ViaMercatorum.getInstance().getEventBus();
            bus.emit(new EconomyEvent.WorldEventFired(region.getMarketCenterId(), currentTick, profile.eventId(), "REGION", region.getId()));
        }

        cooldowns.put(profile.eventId(), currentTick);
        if (!profile.repeatable()) firedOnceEvents.add(profile.eventId());
    }

    private void evaluatePerVillage(WorldEventProfile profile, double probability, RouteGraph routeGraph,
                                     SeasonManager seasonManager, long currentTick) {
        for (VillageNode village : routeGraph.getAllVillages()) {
            if (random.nextDouble() >= probability) continue;

            if (!triggerResolver.evaluateTriggers(profile.triggers(), village, routeGraph, seasonManager)) continue;

            effectResolver.applyEffects(profile.effects(), profile, village, routeGraph, modifierManager, currentTick);

            var bus = ViaMercatorum.getInstance().getEventBus();
            bus.emit(new EconomyEvent.WorldEventFired(village.getId(), currentTick, profile.eventId(), "VILLAGE", village.getId()));
        }

        cooldowns.put(profile.eventId(), currentTick);
        if (!profile.repeatable()) firedOnceEvents.add(profile.eventId());
    }

    public ActiveModifierManager getModifierManager() {
        return modifierManager;
    }
}