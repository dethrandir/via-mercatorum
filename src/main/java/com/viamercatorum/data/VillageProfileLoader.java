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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class VillageProfileLoader implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Identifier.fromNamespaceAndPath("viamercatorum", "village_profiles");
    private static final String DATA_PATH = "village_profiles";

    private volatile Map<Identifier, VillageProfile> profiles = Map.of();

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

        CompletableFuture<Map<Identifier, VillageProfile>> parse = CompletableFuture.supplyAsync(() -> {
            Map<Identifier, VillageProfile> parsed = new HashMap<>();

            manager.listResources(DATA_PATH, path -> path.getPath().endsWith(".json"))
                .forEach((location, resource) -> {
                    try (BufferedReader reader = resource.openAsReader()) {
                        JsonElement json = JsonParser.parseReader(reader);
                        VillageProfile profile = VillageProfile.CODEC
                            .parse(JsonOps.INSTANCE, json)
                            .getOrThrow(error -> new RuntimeException(
                                "Failed to parse village profile " + location + ": " + error));
                        parsed.put(location, profile);
                    } catch (Exception e) {
                        ViaMercatorum.LOGGER.error("Error loading village profile: {}", location, e);
                    }
                });

            return parsed;
        }, prepareExecutor);

        return parse
            .thenCompose(barrier::wait)
            .thenAcceptAsync(parsed -> {
                this.profiles = Map.copyOf(parsed);
                ViaMercatorum.LOGGER.info("Loaded {} village profiles", parsed.size());
            }, applyExecutor);
    }

    public Map<Identifier, VillageProfile> getProfiles() {
        return profiles;
    }

    public VillageProfile getProfile(Identifier id) {
        return profiles.get(id);
    }

    public VillageProfile getProfileForBiome(String biomeId) {
        return profiles.values().stream()
            .filter(p -> p.biome().equals(biomeId))
            .findFirst()
            .orElse(null);
    }
}
