package com.teamsmartstreamlabs.smartbackpacks.item;


import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import java.util.List;
import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackHelper;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacityWarningUpgradeData;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class CapacityWarningUpgradeItem extends BackpackUpgradeItem {
    public CapacityWarningUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onInstalledRightClicked(ServerPlayer player, BackpackAccess access, int upgradeSlot, ItemStack stack) {
        BackpackHelper.openCapacityWarningUpgrade(player, access, upgradeSlot);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        CapacityWarningUpgradeData data = ItemStackCompat.getOrDefault(stack, ModDataComponents.CAPACITY_WARNING_UPGRADE_DATA.get(), CapacityWarningUpgradeData.DEFAULT);
        tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.capacity_warning_upgrade").withStyle(ChatFormatting.GRAY));
        tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.capacity_warning_thresholds",
                data.threshold1(), data.threshold2(), data.threshold3()).withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.capacity_warning_feedback").withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.accept(Component.translatable(data.enabled()
                ? "tooltip.smartbackpacks.upgrade_enabled"
                : "tooltip.smartbackpacks.upgrade_disabled").withStyle(ChatFormatting.DARK_GRAY));
    }
}
