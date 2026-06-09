package com.viamercatorum.player;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.economy.BankAccount;
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

import java.text.DecimalFormat;
import java.util.function.Consumer;

public class DebtNoteItem extends Item {
    private static final DecimalFormat DF = new DecimalFormat("#.##");

    public DebtNoteItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            var bankManager = ViaMercatorum.getInstance().getBankManager();
            var loans = bankManager.getPlayerLoans(player.getStringUUID());

            player.sendSystemMessage(Component.literal("§c§l═══ Debt Ledger ═══")
                .withStyle(ChatFormatting.RED));

            if (loans.isEmpty()) {
                player.sendSystemMessage(Component.literal("  §7No outstanding debts.").withStyle(ChatFormatting.GRAY));
            } else {
                double totalDebt = 0;
                for (BankAccount loan : loans) {
                    String statusStr = switch (loan.getStatus()) {
                        case ACTIVE -> "§aActive";
                        case OVERDUE -> "§c§lOVERDUE";
                        case PAID -> "§7Paid";
                    };
                    totalDebt += loan.getTotalDebt();

                    player.sendSystemMessage(Component.literal(
                        String.format("  §7Village: §f%s §7| Principal: §e%s §7| Interest: §c%s §7| Total: §6%s §7| Status: %s",
                            loan.getVillageId(),
                            DF.format(loan.getPrincipal()),
                            DF.format(loan.getInterestAccrued()),
                            DF.format(loan.getTotalDebt()),
                            statusStr
                        )
                    ));
                }
                player.sendSystemMessage(Component.literal(""));
                player.sendSystemMessage(Component.literal("  §c§lTotal Debt: " + DF.format(totalDebt) + " emeralds")
                    .withStyle(ChatFormatting.RED));

                if (bankManager.hasOverdueLoan(player.getStringUUID())) {
                    player.sendSystemMessage(Component.literal("  §c§l\u26a0 You have overdue debts! Armed collectors may be sent.")
                        .withStyle(ChatFormatting.RED));
                }
            }

            double rep = ViaMercatorum.getInstance().getReputationSystem().getGlobalReputation(player.getUUID());
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("  §7Reputation: §f" + DF.format(rep))
                .withStyle(ChatFormatting.GRAY));
        }
        return level.isClientSide() ? InteractionResult.PASS : InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> consumer, TooltipFlag flag) {
        consumer.accept(Component.literal("Right-click to view your debts").withStyle(ChatFormatting.GRAY));
    }
}