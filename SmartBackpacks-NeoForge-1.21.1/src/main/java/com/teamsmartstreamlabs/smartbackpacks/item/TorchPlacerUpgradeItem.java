package com.teamsmartstreamlabs.smartbackpacks.item;

import java.util.List;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackHelper;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.TorchPlacerUpgradeData;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class TorchPlacerUpgradeItem extends BackpackUpgradeItem {
    public TorchPlacerUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onInstalledRightClicked(ServerPlayer player, BackpackAccess access, int upgradeSlot, ItemStack stack) {
        BackpackHelper.openTorchPlacerUpgrade(player, access, upgradeSlot);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        TorchPlacerUpgradeData data = stack.getOrDefault(ModDataComponents.TORCH_PLACER_UPGRADE_DATA.get(), TorchPlacerUpgradeData.DEFAULT);
        tooltipComponents.add(Component.translatable("tooltip.smartbackpacks.torch_placer_upgrade").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.smartbackpacks.torch_placer_threshold", data.lightThreshold(), data.minimumDistance()).withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.add(Component.translatable(data.enabled()
                ? "tooltip.smartbackpacks.upgrade_enabled"
                : "tooltip.smartbackpacks.upgrade_disabled").withStyle(ChatFormatting.DARK_GRAY));
    }
}
