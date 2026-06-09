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

public class ConsumptionProfileLoader implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Identifier.fromNamespaceAndPath("viamercatorum", "consumption_profiles");
    private static final String DATA_PATH = "consumption_profiles";

    private volatile Map<String, ConsumptionProfile> profiles = Map.of();

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

        CompletableFuture<Map<String, ConsumptionProfile>> parse = CompletableFuture.supplyAsync(() -> {
            Map<String, ConsumptionProfile> parsed = new HashMap<>();

            manager.listResources(DATA_PATH, path -> path.getPath().endsWith(".json"))
                .forEach((location, resource) -> {
                    try (BufferedReader reader = resource.openAsReader()) {
                        JsonElement json = JsonParser.parseReader(reader);
                        ConsumptionProfile profile = ConsumptionProfile.CODEC
                            .parse(JsonOps.INSTANCE, json)
                            .getOrThrow(error -> new RuntimeException(
                                "Failed to parse consumption profile " + location + ": " + error));
                        parsed.put(profile.villageType(), profile);
                    } catch (Exception e) {
                        ViaMercatorum.LOGGER.error("Error loading consumption profile: {}", location, e);
                    }
                });

            return parsed;
        }, prepareExecutor);

        return parse
            .thenCompose(barrier::wait)
            .thenAcceptAsync(parsed -> {
                this.profiles = Map.copyOf(parsed);
                ViaMercatorum.LOGGER.info("Loaded {} consumption profiles", parsed.size());
            }, applyExecutor);
    }

    public Map<String, ConsumptionProfile> getProfiles() {
        return profiles;
    }

    public ConsumptionProfile getProfile(String villageType) {
        return profiles.get(villageType);
    }
}