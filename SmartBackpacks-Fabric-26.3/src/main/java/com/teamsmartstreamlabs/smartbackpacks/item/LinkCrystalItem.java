package com.teamsmartstreamlabs.smartbackpacks.item;

import java.util.List;
import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.LinkCrystalData;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class LinkCrystalItem extends Item {
    public LinkCrystalItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        LinkCrystalData data = stack.getOrDefault(ModDataComponents.LINK_CRYSTAL_DATA.get(), LinkCrystalData.EMPTY);
        if (data.isBound()) {
            tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.link_crystal_bound", data.anchorName()).withStyle(ChatFormatting.AQUA));
            tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.link_crystal_complete").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.link_crystal_empty").withStyle(ChatFormatting.GRAY));
        }
    }
}
