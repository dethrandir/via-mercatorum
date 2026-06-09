package com.viamercatorum.entity;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.simulation.Character;
import com.viamercatorum.core.simulation.CharacterRole;
import com.viamercatorum.core.simulation.CharacterTypeRegistry;
import com.viamercatorum.core.simulation.DialogManager;
import com.viamercatorum.core.world.VillageNode;
import com.viamercatorum.data.DialogTree;
import com.viamercatorum.network.DialogPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

public class CharacterEntity extends PathfinderMob {
    private String characterId;

    public CharacterEntity(EntityType<? extends CharacterEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            .add(Attributes.ATTACK_DAMAGE, 1.0)
            .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    public void setCharacterId(String characterId) {
        this.characterId = characterId;
        updateNametag();
    }

    public String getCharacterId() { return characterId; }

    private void updateNametag() {
        if (characterId == null) return;
        Character character = ViaMercatorum.getInstance().getCharacterManager().getCharacter(characterId);
        if (character != null) {
            CharacterTypeRegistry.CharacterType type = ViaMercatorum.getInstance().getCharacterManager()
                .getTypeRegistry().getType(character.getCharacterTypeId());
            String typeName = type != null ? type.name() : "Character";
            StringBuilder label = new StringBuilder(character.getName());
            if (character.getLeadershipTitle() != null && !character.getLeadershipTitle().isEmpty()) {
                label.insert(0, character.getLeadershipTitle() + " ");
            }
            label.append(" [").append(typeName).append("]");
            setCustomName(Component.literal(label.toString()));
            setCustomNameVisible(true);
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        Character character = getCharacter();
        if (character != null) {
            switch (character.getRole()) {
                case LEADER -> {
                    this.goalSelector.addGoal(1, new RandomStrollGoal(this, 0.2));
                    this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 10));
                }
                case CARAVAN_LEADER -> {
                    this.goalSelector.addGoal(1, new RandomStrollGoal(this, 0.5));
                    this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8));
                }
                case MERCHANT -> {
                    this.goalSelector.addGoal(1, new RandomStrollGoal(this, 0.3));
                    this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8));
                }
            }
        } else {
            this.goalSelector.addGoal(1, new RandomStrollGoal(this, 0.4));
            this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8));
        }
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
    }

    private Character getCharacter() {
        if (characterId == null) return null;
        return ViaMercatorum.getInstance().getCharacterManager().getCharacter(characterId);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide()) return InteractionResult.SUCCESS;
        if (characterId == null) return InteractionResult.PASS;

        Character character = ViaMercatorum.getInstance().getCharacterManager().getCharacter(characterId);
        if (character == null || !character.isAlive()) return InteractionResult.PASS;

        DialogTree tree = findDialogTree(character);
        if (tree == null) {
            String fallback = character.isLeader() ? "They seem busy with matters of governance." :
                character.isCaravanLeader() ? "They're focused on the road ahead." :
                "They have nothing to say right now.";
            player.sendSystemMessage(Component.literal(character.getName() + ": " + fallback));
            return InteractionResult.SUCCESS;
        }

        DialogManager dialogManager = ViaMercatorum.getInstance().getDialogManager();
        DialogManager.ActiveDialog dialog = dialogManager.startDialog(
            player.getUUID(), tree.treeId(), characterId, tree.startingNodeId(),
            ViaMercatorum.getInstance().getSimulationTick().getTickCounter()
        );

        sendDialogNode((ServerPlayer) player, tree, dialog, character);
        return InteractionResult.SUCCESS;
    }

    private DialogTree findDialogTree(Character character) {
        var allTrees = ViaMercatorum.getInstance().getDialogTreeLoader().getDialogTrees();
        for (DialogTree tree : allTrees.values()) {
            if (tree.characterTypes().contains(character.getCharacterTypeId())) {
                return tree;
            }
        }
        for (DialogTree tree : allTrees.values()) {
            if (tree.characterTypes().contains(character.getRole().name().toLowerCase())) {
                return tree;
            }
        }
        return null;
    }

    private void sendDialogNode(ServerPlayer player, DialogTree tree, DialogManager.ActiveDialog dialog, Character character) {
        var nodeOpt = tree.nodes().stream()
            .filter(n -> n.nodeId().equals(dialog.currentNodeId()))
            .findFirst();
        if (nodeOpt.isEmpty()) {
            ViaMercatorum.getInstance().getDialogManager().endDialog(player.getUUID());
            return;
        }

        var node = nodeOpt.get();
        String characterType = character.getCharacterTypeId();

        List<DialogPayload.DialogChoice> choices = node.choices().stream()
            .map(c -> new DialogPayload.DialogChoice(c.index(), c.text(), c.nextNodeId(), c.action().orElse("")))
            .toList();

        DialogPayload payload = new DialogPayload(
            tree.treeId(), characterId, character.getName(), characterType,
            node.text(), choices
        );
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (tickCount % 200 == 0) {
                updateNametag();
            }
            Character character = getCharacter();
            if (character != null && !character.isAlive()) {
                this.discard();
            }
        }
    }

    @Override
    public boolean isPersistenceRequired() {
        Character character = getCharacter();
        return character != null && (character.isLeader() || character.isMerchant());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (characterId != null) {
            output.putString("characterId", characterId);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.characterId = input.getStringOr("characterId", "");
    }
}