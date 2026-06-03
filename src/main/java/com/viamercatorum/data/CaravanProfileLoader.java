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

public class CaravanProfileLoader implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Identifier.fromNamespaceAndPath("viamercatorum", "caravan_profiles");
    private static final String DATA_PATH = "caravan_profiles";

    private volatile Map<String, CaravanProfile> profiles = Map.of();

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

        CompletableFuture<Map<String, CaravanProfile>> parse = CompletableFuture.supplyAsync(() -> {
            Map<String, CaravanProfile> parsed = new HashMap<>();

            manager.listResources(DATA_PATH, path -> path.getPath().endsWith(".json"))
                .forEach((location, resource) -> {
                    try (BufferedReader reader = resource.openAsReader()) {
                        JsonElement json = JsonParser.parseReader(reader);
                        CaravanProfile profile = CaravanProfile.CODEC
                            .parse(JsonOps.INSTANCE, json)
                            .getOrThrow(error -> new RuntimeException(
                                "Failed to parse caravan profile " + location + ": " + error));
                        parsed.put(profile.biome(), profile);
                    } catch (Exception e) {
                        ViaMercatorum.LOGGER.error("Error loading caravan profile: {}", location, e);
                    }
                });

            return parsed;
        }, prepareExecutor);

        return parse
            .thenCompose(barrier::wait)
            .thenAcceptAsync(parsed -> {
                this.profiles = Map.copyOf(parsed);
                ViaMercatorum.LOGGER.info("Loaded {} caravan profiles", parsed.size());
            }, applyExecutor);
    }

    public CaravanProfile getProfile(String biome) {
        return profiles.get(biome);
    }
}
