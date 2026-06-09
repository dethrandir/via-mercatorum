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

public class DialogTreeLoader implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Identifier.fromNamespaceAndPath("viamercatorum", "dialogs");
    private static final String DATA_PATH = "dialogs";

    private volatile Map<String, DialogTree> dialogTrees = Map.of();

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

        CompletableFuture<Map<String, DialogTree>> parse = CompletableFuture.supplyAsync(() -> {
            Map<String, DialogTree> parsed = new HashMap<>();

            manager.listResources(DATA_PATH, path -> path.getPath().endsWith(".json"))
                .forEach((location, resource) -> {
                    try (BufferedReader reader = resource.openAsReader()) {
                        JsonElement json = JsonParser.parseReader(reader);
                        DialogTree tree = DialogTree.CODEC
                            .parse(JsonOps.INSTANCE, json)
                            .getOrThrow(error -> new RuntimeException(
                                "Failed to parse dialog tree " + location + ": " + error));
                        parsed.put(tree.treeId(), tree);
                    } catch (Exception e) {
                        ViaMercatorum.LOGGER.error("Error loading dialog tree: {}", location, e);
                    }
                });

            return parsed;
        }, prepareExecutor);

        return parse
            .thenCompose(barrier::wait)
            .thenAcceptAsync(parsed -> {
                this.dialogTrees = Map.copyOf(parsed);
                ViaMercatorum.LOGGER.info("Loaded {} dialog trees", parsed.size());
            }, applyExecutor);
    }

    public Map<String, DialogTree> getDialogTrees() {
        return dialogTrees;
    }

    public DialogTree getDialogTree(String treeId) {
        return dialogTrees.get(treeId);
    }
}