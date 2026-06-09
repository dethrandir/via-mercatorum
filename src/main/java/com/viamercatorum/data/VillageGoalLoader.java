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

public class VillageGoalLoader implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Identifier.fromNamespaceAndPath("viamercatorum", "village_goals");
    private static final String DATA_PATH = "village_goals";

    private volatile Map<String, VillageGoal> goals = Map.of();

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

        CompletableFuture<Map<String, VillageGoal>> parse = CompletableFuture.supplyAsync(() -> {
            Map<String, VillageGoal> parsed = new HashMap<>();

            manager.listResources(DATA_PATH, path -> path.getPath().endsWith(".json"))
                .forEach((location, resource) -> {
                    try (BufferedReader reader = resource.openAsReader()) {
                        JsonElement json = JsonParser.parseReader(reader);
                        VillageGoal goal = VillageGoal.CODEC
                            .parse(JsonOps.INSTANCE, json)
                            .getOrThrow(error -> new RuntimeException(
                                "Failed to parse village goal " + location + ": " + error));
                        parsed.put(goal.goalId(), goal);
                    } catch (Exception e) {
                        ViaMercatorum.LOGGER.error("Error loading village goal: {}", location, e);
                    }
                });

            return parsed;
        }, prepareExecutor);

        return parse
            .thenCompose(barrier::wait)
            .thenAcceptAsync(parsed -> {
                this.goals = Map.copyOf(parsed);
                ViaMercatorum.LOGGER.info("Loaded {} village goals", parsed.size());
            }, applyExecutor);
    }

    public Map<String, VillageGoal> getGoals() {
        return goals;
    }

    public VillageGoal getGoal(String goalId) {
        return goals.get(goalId);
    }
}