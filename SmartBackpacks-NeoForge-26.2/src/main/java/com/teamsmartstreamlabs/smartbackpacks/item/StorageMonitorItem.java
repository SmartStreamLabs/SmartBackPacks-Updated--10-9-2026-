package com.teamsmartstreamlabs.smartbackpacks.item;

import java.util.List;
import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackHelper;
import com.teamsmartstreamlabs.smartbackpacks.block.StorageControllerBlock;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.storage.StorageMonitorAccess;
import com.teamsmartstreamlabs.smartbackpacks.storage.StorageMonitorLink;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class StorageMonitorItem extends Item {
    public StorageMonitorItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockState(context.getClickedPos()).getBlock() instanceof StorageControllerBlock)) {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide()) {
            ItemStack stack = context.getItemInHand();
            StorageMonitorLink oldLink = stack.get(ModDataComponents.STORAGE_MONITOR_LINK.get());
            StorageMonitorLink newLink = new StorageMonitorLink(context.getLevel().dimension(), context.getClickedPos());
            boolean relinked = oldLink != null && !oldLink.equals(newLink);
            stack.set(ModDataComponents.STORAGE_MONITOR_LINK.get(), newLink);
            if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.translatable(relinked
                        ? "message.smartbackpacks.storage_monitor.relinked"
                        : "message.smartbackpacks.storage_monitor.linked"), true);
            }
        }
        return context.getLevel().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            StorageMonitorLink link = stack.get(ModDataComponents.STORAGE_MONITOR_LINK.get());
            StorageMonitorAccess.Result access = StorageMonitorAccess.resolve(serverPlayer, link);
            if (!access.available()) {
                serverPlayer.sendSystemMessage(Component.translatable(statusKey(access.status())), true);
            } else {
                BackpackHelper.openStorageMonitor(serverPlayer, link);
            }
        }
        return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER)
                .heldItemTransformedTo(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        StorageMonitorLink link = stack.get(ModDataComponents.STORAGE_MONITOR_LINK.get());
        if (link == null) {
            tooltip.accept(Component.translatable("tooltip.smartbackpacks.storage_monitor.not_linked").withStyle(ChatFormatting.RED));
            tooltip.accept(Component.translatable("tooltip.smartbackpacks.storage_monitor.link_help").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.accept(Component.translatable("tooltip.smartbackpacks.storage_monitor.linked").withStyle(ChatFormatting.GOLD));
            tooltip.accept(Component.translatable("tooltip.smartbackpacks.storage_monitor.dimension",
                    link.dimension().identifier().toString()).withStyle(ChatFormatting.AQUA));
            tooltip.accept(Component.translatable("tooltip.smartbackpacks.storage_monitor.position",
                    link.controllerPos().getX(), link.controllerPos().getY(), link.controllerPos().getZ())
                    .withStyle(ChatFormatting.AQUA));
            tooltip.accept(Component.translatable("tooltip.smartbackpacks.storage_monitor.summary").withStyle(ChatFormatting.GRAY));
        }
    }

    private static String statusKey(StorageMonitorAccess.Status status) {
        return "message.smartbackpacks.storage_monitor." + switch (status) {
            case DISABLED -> "disabled";
            case NOT_LINKED -> "not_linked";
            case CROSS_DIMENSION_DISABLED -> "cross_dimension_disabled";
            case TARGET_UNLOADED, NETWORK_OFFLINE -> "offline";
            case CONTROLLER_MISSING -> "controller_missing";
            case AVAILABLE -> "offline";
        };
    }
}
