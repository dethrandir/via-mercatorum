package com.viamercatorum.core.simulation;

import com.viamercatorum.data.DialogTree;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DialogManager {
    private final Map<UUID, ActiveDialog> activeDialogs = new HashMap<>();

    public record ActiveDialog(
        String dialogId,
        String characterId,
        String currentNodeId,
        long startedAtTick
    ) {}

    public ActiveDialog startDialog(UUID playerId, String dialogId, String characterId, String startNodeId, long tick) {
        ActiveDialog dialog = new ActiveDialog(dialogId, characterId, startNodeId, tick);
        activeDialogs.put(playerId, dialog);
        return dialog;
    }

    public ActiveDialog getActiveDialog(UUID playerId) {
        return activeDialogs.get(playerId);
    }

    public void endDialog(UUID playerId) {
        activeDialogs.remove(playerId);
    }

    public ActiveDialog chooseOption(UUID playerId, int choiceIndex, DialogTree tree) {
        ActiveDialog current = activeDialogs.get(playerId);
        if (current == null) return null;

        var node = tree.nodes().stream()
            .filter(n -> n.nodeId().equals(current.currentNodeId()))
            .findFirst()
            .orElse(null);

        if (node == null || choiceIndex < 0 || choiceIndex >= node.choices().size()) {
            return null;
        }

        var choice = node.choices().get(choiceIndex);
        if (choice.nextNodeId().isEmpty() || choice.nextNodeId().equals("end") || choice.nextNodeId().equals("exit")) {
            activeDialogs.remove(playerId);
            return null;
        }

        ActiveDialog next = new ActiveDialog(current.dialogId(), current.characterId(), choice.nextNodeId(), current.startedAtTick());
        activeDialogs.put(playerId, next);
        return next;
    }
}