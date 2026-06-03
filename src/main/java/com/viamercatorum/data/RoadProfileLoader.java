package com.viamercatorum.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.viamercatorum.ViaMercatorum;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class RoadProfileLoader implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Identifier.fromNamespaceAndPath("viamercatorum", "road_profiles");
    private static final String DATA_PATH = "road_profiles";

    private volatile Map<String, RoadProfile> profiles = Map.of();

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public CompletableFuture<Void> reload(
            PreparableReloadListener.SharedState sharedState,
            Executor prepareExecutor,
            PreparableReloadListener.PreparationBarrier barrier,
            Executor applyExecutor) {

        ResourceManager manager = sharedState.resourceManager();

        CompletableFuture<Map<String, RoadProfile>> parse = CompletableFuture.supplyAsync(() -> {
            Map<String, RoadProfile> parsed = new LinkedHashMap<>();

            manager.listResources(DATA_PATH, path -> path.getPath().endsWith(".json"))
                .forEach((location, resource) -> {
                    try (BufferedReader reader = resource.openAsReader()) {
                        JsonElement json = JsonParser.parseReader(reader);
                        RoadProfile profile = RoadProfile.CODEC
                            .parse(JsonOps.INSTANCE, json)
                            .getOrThrow(error -> new RuntimeException(
                                "Failed to parse road profile " + location + ": " + error));
                        parsed.put(profile.quality(), profile);
                    } catch (Exception e) {
                        ViaMercatorum.LOGGER.error("Error loading road profile: {}", location, e);
                    }
                });

            return parsed;
        }, prepareExecutor);

        return parse
            .thenCompose(barrier::wait)
            .thenAcceptAsync(parsed -> {
                this.profiles = Map.copyOf(parsed);
                ViaMercatorum.LOGGER.info("Loaded {} road profiles", parsed.size());
            }, applyExecutor);
    }

    public Map<String, RoadProfile> getProfiles() {
        return profiles;
    }

    public RoadProfile getProfile(String quality) {
        return profiles.get(quality);
    }

    public String getNextQuality(String currentQuality) {
        RoadProfile profile = profiles.get(currentQuality);
        if (profile == null) return currentQuality;
        return profile.nextQuality().orElse(currentQuality);
    }
}
