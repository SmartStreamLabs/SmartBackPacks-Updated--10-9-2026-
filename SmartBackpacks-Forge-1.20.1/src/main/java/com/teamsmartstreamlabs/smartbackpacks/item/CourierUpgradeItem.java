package com.teamsmartstreamlabs.smartbackpacks.item;

import java.util.List;

import net.minecraft.world.level.Level;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackHelper;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CourierUpgradeHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class CourierUpgradeItem extends FilterUpgradeItem {
    public CourierUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onInstalledRightClicked(ServerPlayer player, BackpackAccess access, int upgradeSlot, ItemStack stack) {
        return this.onInstalledRightClicked(player, access, upgradeSlot, stack, player.isShiftKeyDown());
    }

    @Override
    public boolean onInstalledRightClicked(ServerPlayer player, BackpackAccess access, int upgradeSlot, ItemStack stack, boolean shiftDown) {
        if (shiftDown) {
            CourierUpgradeHandler.toggleLinking(player, access, upgradeSlot, stack);
            return true;
        }
        BackpackHelper.openCourierUpgrade(player, access, upgradeSlot);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.smartbackpacks.courier_upgrade").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.smartbackpacks.courier_link").withStyle(ChatFormatting.DARK_GRAY));
    }
}
