package com.teamsmartstreamlabs.smartbackpacks.item;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class FallProtectionUpgradeItem extends BackpackUpgradeItem {
    public FallProtectionUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        int maximum = Math.min(stack.getMaxDamage(), SmartBackpacksConfig.fallProtectionMaxCharges());
        tooltip.accept(Component.translatable("tooltip.smartbackpacks.fall_protection_upgrade").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.smartbackpacks.fall_protection_charges",
                Math.max(0, maximum - stack.getDamageValue()), maximum).withStyle(ChatFormatting.AQUA));
        long remainingMillis = stack.getOrDefault(ModDataComponents.FALL_PROTECTION_COOLDOWN_UNTIL.get(), 0L) - System.currentTimeMillis();
        if (maximum - stack.getDamageValue() <= 0) {
            tooltip.accept(Component.translatable("tooltip.smartbackpacks.fall_protection_depleted").withStyle(ChatFormatting.RED));
        } else if (remainingMillis > 0) {
            long remainingSeconds = (remainingMillis + 999L) / 1000L;
            tooltip.accept(Component.translatable("tooltip.smartbackpacks.fall_protection_cooldown",
                    remainingSeconds).withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.accept(Component.translatable("tooltip.smartbackpacks.fall_protection_ready").withStyle(ChatFormatting.GREEN));
        }
    }
}
