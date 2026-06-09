package com.viamercatorum.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record DialogTree(
    String treeId,
    String name,
    List<String> characterTypes,
    String startingNodeId,
    List<Node> nodes
) {
    public static final Codec<DialogTree> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("tree_id").forGetter(DialogTree::treeId),
        Codec.STRING.fieldOf("name").forGetter(DialogTree::name),
        Codec.STRING.listOf().fieldOf("character_types").forGetter(DialogTree::characterTypes),
        Codec.STRING.fieldOf("starting_node_id").forGetter(DialogTree::startingNodeId),
        Node.CODEC.listOf().fieldOf("nodes").forGetter(DialogTree::nodes)
    ).apply(instance, DialogTree::new));

    public record Node(
        String nodeId,
        String text,
        Optional<String> portrait,
        List<Choice> choices,
        Optional<String> action
    ) {
        public static final Codec<Node> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("node_id").forGetter(Node::nodeId),
            Codec.STRING.fieldOf("text").forGetter(Node::text),
            Codec.STRING.optionalFieldOf("portrait").forGetter(Node::portrait),
            Choice.CODEC.listOf().fieldOf("choices").forGetter(Node::choices),
            Codec.STRING.optionalFieldOf("action").forGetter(Node::action)
        ).apply(instance, Node::new));
    }

    public record Choice(
        int index,
        String text,
        String nextNodeId,
        Optional<String> action
    ) {
        public static final Codec<Choice> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("index").forGetter(Choice::index),
            Codec.STRING.fieldOf("text").forGetter(Choice::text),
            Codec.STRING.fieldOf("next_node_id").forGetter(Choice::nextNodeId),
            Codec.STRING.optionalFieldOf("action").forGetter(Choice::action)
        ).apply(instance, Choice::new));
    }
}