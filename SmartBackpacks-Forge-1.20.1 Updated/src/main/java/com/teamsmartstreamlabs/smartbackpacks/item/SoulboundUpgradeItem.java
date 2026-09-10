package com.teamsmartstreamlabs.smartbackpacks.item;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class SoulboundUpgradeItem extends BackpackUpgradeItem {
    public static final int MAX_DEATHS = 5;
    private static final String USES_TAG = "SoulboundUsesRemaining";

    public SoulboundUpgradeItem(Properties properties) {
        super(properties);
    }

    public static int getRemainingUses(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(USES_TAG)) {
            tag.putInt(USES_TAG, MAX_DEATHS);
            return MAX_DEATHS;
        }
        return Math.max(0, tag.getInt(USES_TAG));
    }

    public static boolean consumeUse(ItemStack stack) {
        int remaining = getRemainingUses(stack) - 1;
        if (remaining <= 0) {
            return false;
        }
        stack.getOrCreateTag().putInt(USES_TAG, remaining);
        return true;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getRemainingUses(stack) < MAX_DEATHS;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getRemainingUses(stack) / (float) MAX_DEATHS);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float fraction = getRemainingUses(stack) / (float) MAX_DEATHS;
        return Mth.hsvToRgb(Math.max(0.0F, fraction) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("tooltip.smartbackpacks.soulbound_upgrade").withStyle(ChatFormatting.GRAY));
        int remainingDeaths = getRemainingUses(stack);
        tooltipComponents.add(Component.translatable("tooltip.smartbackpacks.soulbound_upgrade_uses", remainingDeaths).withStyle(ChatFormatting.DARK_GRAY));
    }
}
