package com.teamsmartstreamlabs.smartbackpacks.item;

import java.util.List;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class QuickAccessWheelUpgradeItem extends BackpackUpgradeItem {
    public QuickAccessWheelUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onInstalledRightClicked(ServerPlayer player, BackpackAccess access, int upgradeSlot, ItemStack stack) {
        BackpackHelper.openQuickAccessWheelUpgrade(player, access, upgradeSlot);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("tooltip.smartbackpacks.quick_access_wheel_upgrade").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.smartbackpacks.quick_access_wheel_open").withStyle(ChatFormatting.DARK_GRAY));
    }
}
