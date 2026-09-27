package com.teamsmartstreamlabs.smartbackpacks.inventory;

import java.util.List;

import net.minecraft.world.item.ItemStack;

final class ContainerHelperCompat {
    private ContainerHelperCompat() {
    }

    static ItemStack removeItem(List<ItemStack> stacks, int slot, int amount) {
        if (slot < 0 || slot >= stacks.size() || amount <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack current = stacks.get(slot);
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return current.split(amount);
    }

    static ItemStack takeItem(List<ItemStack> stacks, int slot) {
        if (slot < 0 || slot >= stacks.size()) {
            return ItemStack.EMPTY;
        }

        ItemStack current = stacks.get(slot);
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }

        stacks.set(slot, ItemStack.EMPTY);
        return current;
    }
}
