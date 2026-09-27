package com.teamsmartstreamlabs.smartbackpacks.item;

import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.client.StorageGuidePrompt;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

public final class StorageSystemBlockItem extends BlockItem {
    public StorageSystemBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        StorageGuidePrompt.append(tooltip);
    }
}
