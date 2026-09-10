package com.teamsmartstreamlabs.smartbackpacks.item;

import java.util.List;



import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

public class SoulboundUpgradeItem extends BackpackUpgradeItem {
    public static final int MAX_DEATHS = 5;

    public SoulboundUpgradeItem(Properties properties) {
        super(properties.rarity(Rarity.RARE).durability(MAX_DEATHS));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("tooltip.smartbackpacks.soulbound_upgrade").withStyle(ChatFormatting.GRAY));
        int remainingDeaths = Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
        tooltipComponents.add(Component.translatable("tooltip.smartbackpacks.soulbound_upgrade_uses", remainingDeaths).withStyle(ChatFormatting.DARK_GRAY));
    }
}



