package com.viamercatorum.player;

import com.viamercatorum.ViaMercatorum;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class NewspaperItem extends Item {
    public NewspaperItem() {
        super(new Item.Properties().stacksTo(16));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            var newspaperSystem = ViaMercatorum.getInstance().getNewspaperSystem();
            var newspapers = newspaperSystem.getAllNewspapers();
            for (var newspaper : newspapers) {
                player.sendSystemMessage(Component.literal("§l" + newspaper.title() + " §r§7- " + newspaper.date())
                    .withStyle(ChatFormatting.GOLD));
                for (var article : newspaper.articles()) {
                    player.sendSystemMessage(Component.literal("  §l" + article.headline())
                        .withStyle(ChatFormatting.YELLOW));
                    player.sendSystemMessage(Component.literal("  §7" + article.body())
                        .withStyle(ChatFormatting.GRAY));
                }
            }
            if (newspapers.isEmpty()) {
                player.sendSystemMessage(Component.literal("No news available yet.")
                    .withStyle(ChatFormatting.ITALIC));
            }
        }
        return level.isClientSide() ? InteractionResult.PASS : InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> consumer, TooltipFlag flag) {
        consumer.accept(Component.literal("Right-click to read the news").withStyle(ChatFormatting.GRAY));
    }
}