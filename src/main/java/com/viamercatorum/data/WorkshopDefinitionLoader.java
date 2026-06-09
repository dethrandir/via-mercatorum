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

public class WorkshopDefinitionLoader implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Identifier.fromNamespaceAndPath("viamercatorum", "workshop_definitions");
    private static final String DATA_PATH = "workshop_definitions";

    private volatile Map<String, WorkshopDefinition> definitions = Map.of();

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

        CompletableFuture<Map<String, WorkshopDefinition>> parse = CompletableFuture.supplyAsync(() -> {
            Map<String, WorkshopDefinition> parsed = new HashMap<>();

            manager.listResources(DATA_PATH, path -> path.getPath().endsWith(".json"))
                .forEach((location, resource) -> {
                    try (BufferedReader reader = resource.openAsReader()) {
                        JsonElement json = JsonParser.parseReader(reader);
                        WorkshopDefinition def = WorkshopDefinition.CODEC
                            .parse(JsonOps.INSTANCE, json)
                            .getOrThrow(error -> new RuntimeException(
                                "Failed to parse workshop definition " + location + ": " + error));
                        parsed.put(def.workshopId(), def);
                    } catch (Exception e) {
                        ViaMercatorum.LOGGER.error("Error loading workshop definition: {}", location, e);
                    }
                });

            return parsed;
        }, prepareExecutor);

        return parse
            .thenCompose(barrier::wait)
            .thenAcceptAsync(parsed -> {
                this.definitions = Map.copyOf(parsed);
                ViaMercatorum.LOGGER.info("Loaded {} workshop definitions", parsed.size());
            }, applyExecutor);
    }

    public Map<String, WorkshopDefinition> getDefinitions() {
        return definitions;
    }

    public WorkshopDefinition getDefinition(String workshopId) {
        return definitions.get(workshopId);
    }
}