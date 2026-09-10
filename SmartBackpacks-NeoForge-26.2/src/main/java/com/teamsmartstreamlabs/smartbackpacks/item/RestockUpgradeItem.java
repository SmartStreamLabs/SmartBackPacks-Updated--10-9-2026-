package com.teamsmartstreamlabs.smartbackpacks.item;

import java.util.List;
import java.util.function.Consumer;



import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class RestockUpgradeItem extends BackpackUpgradeItem {
    public RestockUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.restock_upgrade").withStyle(ChatFormatting.GRAY));
    }
}



