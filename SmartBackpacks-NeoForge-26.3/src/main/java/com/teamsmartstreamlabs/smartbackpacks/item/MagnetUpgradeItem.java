package com.teamsmartstreamlabs.smartbackpacks.item;

import java.util.List;
import java.util.function.Consumer;


import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackHelper;


import net.minecraft.ChatFormatting;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class MagnetUpgradeItem extends BackpackUpgradeItem {
    private final boolean advanced;

    public MagnetUpgradeItem(boolean advanced, Properties properties) {
        super(properties);
        this.advanced = advanced;
    }

    public boolean isAdvanced() {
        return this.advanced;
    }

    public int getRange() {
        return this.advanced ? SmartBackpacksConfig.advancedMagnetRadius() : SmartBackpacksConfig.magnetRadius();
    }

    public int getTickInterval() {
        return this.advanced ? 2 : 5;
    }

    public double getPullStrength() {
        return this.advanced ? 0.55D : 0.25D;
    }

    public double getPickupDistanceSqr() {
        return this.advanced ? 6.25D : 2.25D;
    }

    @Override
    public boolean onInstalledRightClicked(ServerPlayer player, BackpackAccess access, int upgradeSlot, ItemStack stack) {
        BackpackHelper.openMagnetUpgrade(player, access, upgradeSlot);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.magnet_open_filter").withStyle(ChatFormatting.GRAY));
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null
                && (InputConstants.isKeyDown(InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(InputConstants.KEY_RSHIFT))) {
            tooltipComponents.accept(Component.translatable(this.advanced
                    ? "tooltip.smartbackpacks.advanced_magnet_range_value"
                    : "tooltip.smartbackpacks.magnet_range_value", this.getRange()).withStyle(ChatFormatting.GRAY));
        } else {
            tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}



