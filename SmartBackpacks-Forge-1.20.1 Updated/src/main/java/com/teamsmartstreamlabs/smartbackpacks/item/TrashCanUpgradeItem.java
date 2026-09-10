package com.teamsmartstreamlabs.smartbackpacks.item;


import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import java.util.List;
import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.TrashCanUpgradeData;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class TrashCanUpgradeItem extends BackpackUpgradeItem {
    public TrashCanUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onInstalledRightClicked(ServerPlayer player, BackpackAccess access, int upgradeSlot, ItemStack stack) {
        player.displayClientMessage(Component.translatable("message.smartbackpacks.trash_can_panel_hint"), true);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        TrashCanUpgradeData data = ItemStackCompat.getOrDefault(stack, ModDataComponents.TRASH_CAN_UPGRADE_DATA.get(), TrashCanUpgradeData.DEFAULT);
        tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.trash_can_upgrade").withStyle(ChatFormatting.GRAY));
        tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.trash_can_delay", SmartBackpacksConfig.trashCanDeletionDelaySeconds()).withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.trash_can_restore").withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.trash_can_shortcut").withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.trash_can_warning").withStyle(ChatFormatting.RED));
        tooltipComponents.accept(Component.translatable(data.enabled()
                ? "tooltip.smartbackpacks.upgrade_enabled"
                : "tooltip.smartbackpacks.upgrade_disabled").withStyle(ChatFormatting.DARK_GRAY));
    }
}
