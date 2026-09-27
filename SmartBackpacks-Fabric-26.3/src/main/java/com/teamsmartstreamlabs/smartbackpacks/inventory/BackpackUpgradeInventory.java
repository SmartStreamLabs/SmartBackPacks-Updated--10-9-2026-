package com.teamsmartstreamlabs.smartbackpacks.inventory;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackUpgradeItem;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BackpackUpgradeInventory implements Container {
    public static final int UPGRADE_SLOT_COUNT = 10;

    private final Player owner;
    private final BackpackAccess access;
    private final NonNullList<ItemStack> items;

    public BackpackUpgradeInventory(Player owner, BackpackAccess access) {
        this.owner = owner;
        this.access = access;
        this.items = BackpackStackData.loadUpgrades(this.getBackpackStack());
    }

    public ItemStack getBackpackStack() {
        return this.access.getBackpackStack(this.owner);
    }

    public void saveToStack() {
        if (this.owner.level().isClientSide()) {
            return;
        }

        ItemStack backpack = this.getBackpackStack();
        if (backpack.isEmpty()) {
            return;
        }

        BackpackStackData.saveUpgrades(backpack, this.items);
        this.access.setBackpackStack(this.owner, backpack);
        this.owner.getInventory().setChanged();
    }

    @Override
    public int getContainerSize() {
        return UPGRADE_SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelperCompat.removeItem(this.items, slot, amount);
        if (!stack.isEmpty()) {
            this.setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = ContainerHelperCompat.takeItem(this.items, slot);
        if (!stack.isEmpty()) {
            this.setChanged();
        }
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!stack.isEmpty() && !(stack.getItem() instanceof BackpackUpgradeItem)) {
            return;
        }

        this.items.set(slot, stack.copyWithCount(Math.min(1, stack.getCount())));
        this.setChanged();
    }

    @Override
    public void setChanged() {
        this.saveToStack();
    }

    @Override
    public boolean stillValid(Player player) {
        return player == this.owner && this.access.isStillValid(player);
    }

    @Override
    public void clearContent() {
        for (int index = 0; index < this.items.size(); index++) {
            this.items.set(index, ItemStack.EMPTY);
        }
        this.setChanged();
    }
}
