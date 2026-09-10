package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.stream.Stream;

import net.minecraft.world.item.ItemStack;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.component.ItemContainerContents;

public final class ItemContainerContentsHelper {
    private ItemContainerContentsHelper() {
    }

    public static Stream<ItemStack> nonEmptyStream(ItemContainerContents contents) {
        return contents.stream()
                .map(ItemStack::copy)
                .filter(stack -> !stack.isEmpty());
    }

    public static Stream<ItemStack> allItemsStream(ItemContainerContents contents) {
        return contents.stream().map(ItemStack::copy);
    }
}
