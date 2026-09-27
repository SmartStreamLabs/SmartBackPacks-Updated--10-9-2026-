package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

final class FurnaceFuelHelper {
    private FurnaceFuelHelper() {
    }

    static int getBurnTime(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        return AbstractFurnaceBlockEntity.getFuel().getOrDefault(stack.getItem(), 0);
    }
}
