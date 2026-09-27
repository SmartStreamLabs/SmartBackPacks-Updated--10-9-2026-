package com.teamsmartstreamlabs.smartbackpacks.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BackpackInventory implements Container {
    private final Player owner;
    private final BackpackAccess access;
    private final BackpackTier tier;
    private final NonNullList<ItemStack> items;
    private boolean storageWritesAllowed;
    private boolean blockedWriteLogged;

    public BackpackInventory(Player owner, BackpackAccess access, BackpackTier tier) {
        this.owner = owner;
        this.access = access;
        this.tier = tier;
        this.items = NonNullList.withSize(tier.getSlotCount(), ItemStack.EMPTY);
        this.loadFromStack();
    }

    private void loadFromStack() {
        NonNullList<ItemStack> loadedItems = BackpackStackData.loadStorage(this.getBackpackStack(), this.tier);
        for (int slot = 0; slot < this.items.size(); slot++) {
            this.items.set(slot, slot < loadedItems.size() ? loadedItems.get(slot) : ItemStack.EMPTY);
        }
    }

    public void refreshFromStack() {
        this.loadFromStack();
    }

    public void setItemFromNetwork(int slot, ItemStack stack) {
        if (slot >= 0 && slot < this.items.size()) {
            this.items.set(slot, stack);
        }
    }

    public ItemStack getBackpackStack() {
        return this.access.getBackpackStack(this.owner);
    }

    public void allowStorageWrites() {
        this.storageWritesAllowed = true;
    }

    public void saveToStack() {
        if (this.owner.level().isClientSide()) {
            return;
        }

        if (!this.storageWritesAllowed) {
            if (!this.blockedWriteLogged) {
                this.blockedWriteLogged = true;
                SmartBackpacks.LOGGER.error("Blocked a backpack storage write before any server-side menu mutation", new IllegalStateException("Unexpected backpack storage write"));
            }
            return;
        }

        ItemStack backpack = this.getBackpackStack();
        if (!(backpack.getItem() instanceof BackpackItem backpackItem) || backpackItem.getTier() != this.tier) {
            return;
        }

        // Persist after every meaningful inventory mutation so drops, relogs, and menu closes keep data intact.
        BackpackStackData.saveStorage(backpack, this.items);
        if (this.owner instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.checkFull(serverPlayer, this.tier, this.items);
        }
        this.access.setBackpackStack(this.owner, backpack);
        this.owner.getInventory().setChanged();
    }

    public void sortContents(BackpackSortMode sortMode) {
        this.sortContents(sortMode, slot -> true);
    }

    public void sortContents(BackpackSortMode sortMode, IntPredicate canSortSlot) {
        List<ItemStack> mergedStacks = new ArrayList<>();
        for (int slot = 0; slot < this.items.size(); slot++) {
            ItemStack stack = this.items.get(slot);
            if (!stack.isEmpty()) {
                if (canSortSlot.test(slot)) {
                    this.mergeIntoList(mergedStacks, stack);
                }
            }
        }

        mergedStacks.sort(sortMode.comparator());

        NonNullList<ItemStack> sortedItems = NonNullList.withSize(this.tier.getSlotCount(), ItemStack.EMPTY);
        for (int slot = 0; slot < this.tier.getSlotCount(); slot++) {
            if (!canSortSlot.test(slot)) {
                sortedItems.set(slot, this.items.get(slot));
            }
        }

        int mergedIndex = 0;
        for (int slot = 0; slot < this.tier.getSlotCount(); slot++) {
            if (!canSortSlot.test(slot)) {
                continue;
            }
            sortedItems.set(slot, mergedIndex < mergedStacks.size() ? mergedStacks.get(mergedIndex++) : ItemStack.EMPTY);
        }

        for (int i = 0; i < this.tier.getSlotCount(); i++) {
            this.items.set(i, sortedItems.get(i));
        }
        this.setChanged();
    }

    private void mergeIntoList(List<ItemStack> mergedStacks, ItemStack sourceStack) {
        ItemStack remaining = sourceStack.copy();
        ItemStack backpack = this.getBackpackStack();

        for (ItemStack mergedStack : mergedStacks) {
            if (!ItemStackCompat.isSameItemSameComponents(mergedStack, remaining)) {
                continue;
            }

            int transferAmount = Math.min(remaining.getCount(), BackpackStackData.getStorageStackLimit(backpack, mergedStack) - mergedStack.getCount());
            if (transferAmount <= 0) {
                continue;
            }

            mergedStack.grow(transferAmount);
            remaining.shrink(transferAmount);
            if (remaining.isEmpty()) {
                return;
            }
        }

        while (!remaining.isEmpty()) {
            ItemStack splitStack = remaining.copyWithCount(Math.min(remaining.getCount(), this.getMaxStackSize(remaining)));
            mergedStacks.add(splitStack);
            remaining.shrink(splitStack.getCount());
        }
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelperCompat.removeItem(this.items, slot, amount);
        if (!result.isEmpty()) {
            this.setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = ContainerHelperCompat.takeItem(this.items, slot);
        if (!result.isEmpty()) {
            this.setChanged();
        }
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        ItemStackCompat.limitSize(stack, this.getMaxStackSize(stack));
        this.setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return BackpackStackData.getBackpackMaxStackSize(this.getBackpackStack());
    }

    public int getMaxStackSize(ItemStack stack) {
        return BackpackStackData.getStorageStackLimit(this.getBackpackStack(), stack);
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
        this.items.clear();
        for (int index = 0; index < this.tier.getSlotCount(); index++) {
            this.items.add(ItemStack.EMPTY);
        }
        this.setChanged();
    }
}
