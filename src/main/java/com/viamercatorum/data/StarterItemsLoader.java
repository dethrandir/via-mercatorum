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

public class StarterItemsLoader implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Identifier.fromNamespaceAndPath("viamercatorum", "starter_items");
    private static final String DATA_PATH = "starter_items";

    private volatile StarterItems starterItems;

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

        CompletableFuture<StarterItems> parse = CompletableFuture.supplyAsync(() -> {
            try {
                var resources = manager.listResources(DATA_PATH, path -> path.getPath().endsWith(".json"));
                for (var entry : resources.entrySet()) {
                    try (BufferedReader reader = entry.getValue().openAsReader()) {
                        JsonElement json = JsonParser.parseReader(reader);
                        return StarterItems.CODEC
                            .parse(JsonOps.INSTANCE, json)
                            .getOrThrow(error -> new RuntimeException(
                                "Failed to parse starter items " + entry.getKey() + ": " + error));
                    }
                }
            } catch (Exception e) {
                ViaMercatorum.LOGGER.error("Error loading starter items", e);
            }
            return new StarterItems(java.util.List.of(), java.util.Optional.empty());
        }, prepareExecutor);

        return parse
            .thenCompose(barrier::wait)
            .thenAcceptAsync(parsed -> {
                this.starterItems = parsed;
                ViaMercatorum.LOGGER.info("Loaded starter items: {} gifts", parsed.items().size());
            }, applyExecutor);
    }

    public StarterItems getItems() {
        return starterItems != null ? starterItems : new StarterItems(java.util.List.of(), java.util.Optional.empty());
    }
}