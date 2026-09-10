package com.teamsmartstreamlabs.smartbackpacks.item;

import java.util.List;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackHelper;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.QuiverUpgradeData;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class QuiverUpgradeItem extends BackpackUpgradeItem {
    public QuiverUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onInstalledRightClicked(ServerPlayer player, BackpackAccess access, int upgradeSlot, ItemStack stack) {
        BackpackHelper.openQuiverUpgrade(player, access, upgradeSlot);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        QuiverUpgradeData data = stack.getOrDefault(ModDataComponents.QUIVER_UPGRADE_DATA.get(), QuiverUpgradeData.DEFAULT);
        tooltipComponents.add(Component.translatable("tooltip.smartbackpacks.quiver_upgrade").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.smartbackpacks.quiver_slots", QuiverUpgradeData.SLOT_COUNT).withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.add(Component.translatable(data.enabled()
                ? "tooltip.smartbackpacks.upgrade_enabled"
                : "tooltip.smartbackpacks.upgrade_disabled").withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.add(Component.translatable("tooltip.smartbackpacks.quiver_source_priority",
                Component.translatable("screen.smartbackpacks.quiver.source." + data.sourcePriority().getSerializedName())).withStyle(ChatFormatting.DARK_GRAY));
    }
}
