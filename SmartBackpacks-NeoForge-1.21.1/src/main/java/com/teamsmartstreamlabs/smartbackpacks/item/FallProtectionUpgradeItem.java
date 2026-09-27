package com.teamsmartstreamlabs.smartbackpacks.item;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class FallProtectionUpgradeItem extends BackpackUpgradeItem {
    public FallProtectionUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        int maximum = Math.min(stack.getMaxDamage(), SmartBackpacksConfig.fallProtectionMaxCharges());
        tooltip.add(Component.translatable("tooltip.smartbackpacks.fall_protection_upgrade").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.smartbackpacks.fall_protection_charges",
                Math.max(0, maximum - stack.getDamageValue()), maximum).withStyle(ChatFormatting.AQUA));
        long remainingMillis = stack.getOrDefault(ModDataComponents.FALL_PROTECTION_COOLDOWN_UNTIL.get(), 0L) - System.currentTimeMillis();
        if (maximum - stack.getDamageValue() <= 0) {
            tooltip.add(Component.translatable("tooltip.smartbackpacks.fall_protection_depleted").withStyle(ChatFormatting.RED));
        } else if (remainingMillis > 0) {
            long remainingSeconds = (remainingMillis + 999L) / 1000L;
            tooltip.add(Component.translatable("tooltip.smartbackpacks.fall_protection_cooldown",
                    remainingSeconds).withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(Component.translatable("tooltip.smartbackpacks.fall_protection_ready").withStyle(ChatFormatting.GREEN));
        }
    }
}
