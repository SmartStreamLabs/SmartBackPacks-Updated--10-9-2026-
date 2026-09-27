package com.teamsmartstreamlabs.smartbackpacks.item;

import java.util.List;



import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class DepositUpgradeItem extends BackpackUpgradeItem {
    public DepositUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("tooltip.smartbackpacks.deposit_upgrade").withStyle(ChatFormatting.GRAY));
    }
}




