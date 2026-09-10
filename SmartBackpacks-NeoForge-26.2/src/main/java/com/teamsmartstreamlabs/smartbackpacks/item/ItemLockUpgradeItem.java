package com.teamsmartstreamlabs.smartbackpacks.item;

import java.util.List;
import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import com.teamsmartstreamlabs.smartbackpacks.util.PlayerMessageHelper;
import net.minecraft.world.item.component.TooltipDisplay;

public class ItemLockUpgradeItem extends BackpackUpgradeItem {
    public ItemLockUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onInstalledRightClicked(ServerPlayer player, BackpackAccess access, int upgradeSlot, ItemStack stack) {
        PlayerMessageHelper.sendStatus(player, Component.translatable("message.smartbackpacks.item_lock_open_hint"));
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.item_lock_upgrade").withStyle(ChatFormatting.GRAY));
        tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.item_lock_usage").withStyle(ChatFormatting.DARK_GRAY));
    }
}
