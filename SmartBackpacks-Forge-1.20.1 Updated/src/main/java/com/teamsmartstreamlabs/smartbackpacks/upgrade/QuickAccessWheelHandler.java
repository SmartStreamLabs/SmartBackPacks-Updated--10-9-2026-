package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackHelper;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.QuickAccessWheelUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class QuickAccessWheelHandler {
    private QuickAccessWheelHandler() {
    }

    public static void select(ServerPlayer player, int favoriteIndex) {
        if (favoriteIndex < 0 || favoriteIndex >= QuickAccessWheelUpgradeData.FAVORITE_COUNT) {
            return;
        }

        BackpackAccess access = BackpackHelper.findWornBackpackAccess(player);
        if (access == null || !(access.getBackpackStack(player).getItem() instanceof BackpackItem backpackItem)) {
            return;
        }

        ItemStack backpack = access.getBackpackStack(player);
        QuickAccessWheelUpgradeData data = findData(backpack);
        if (data == null) {
            return;
        }

        ItemStack favorite = data.loadFavorites().get(favoriteIndex);
        if (favorite.isEmpty()) {
            return;
        }

        NonNullList<ItemStack> storage = BackpackStackData.loadStorage(backpack, backpackItem.getTier());
        int sourceSlot = findFirstMatch(storage, favorite);
        if (sourceSlot < 0) {
            return;
        }

        NonNullList<ItemStack> working = deepCopy(storage);
        ItemStack source = working.get(sourceSlot);
        int moveCount = Math.min(source.getCount(), source.getMaxStackSize());
        ItemStack selected = source.copyWithCount(moveCount);
        ItemStack held = player.getInventory().getItem(player.getInventory().selected);

        if (!held.isEmpty() && ItemStackCompat.isSameItemSameComponents(held, selected)) {
            int transfer = Math.min(moveCount, held.getMaxStackSize() - held.getCount());
            if (transfer <= 0) {
                return;
            }
            removeFromSource(working, sourceSlot, transfer);
            ItemStack result = held.copy();
            result.grow(transfer);
            commit(player, access, backpack, working, result);
            return;
        }

        removeFromSource(working, sourceSlot, moveCount);
        if (!held.isEmpty() && !insertAll(backpack, working, held)) {
            return;
        }
        commit(player, access, backpack, working, selected);
    }

    public static QuickAccessWheelUpgradeData findData(ItemStack backpack) {
        for (ItemStack upgrade : BackpackStackData.loadUpgrades(backpack)) {
            if (upgrade.getItem() instanceof QuickAccessWheelUpgradeItem) {
                return ItemStackCompat.getOrDefault(upgrade, ModDataComponents.QUICK_ACCESS_WHEEL_UPGRADE_DATA.get(),
                        QuickAccessWheelUpgradeData.DEFAULT);
            }
        }
        return null;
    }

    public static boolean containsMatchingItem(ItemStack backpack, BackpackItem backpackItem, ItemStack favorite) {
        return findFirstMatch(BackpackStackData.loadStorage(backpack, backpackItem.getTier()), favorite) >= 0;
    }

    private static int findFirstMatch(NonNullList<ItemStack> storage, ItemStack favorite) {
        for (int slot = 0; slot < storage.size(); slot++) {
            if (!storage.get(slot).isEmpty() && ItemStack.isSameItem(storage.get(slot), favorite)) {
                return slot;
            }
        }
        return -1;
    }

    private static NonNullList<ItemStack> deepCopy(NonNullList<ItemStack> source) {
        NonNullList<ItemStack> copy = NonNullList.withSize(source.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < source.size(); slot++) {
            copy.set(slot, source.get(slot).copy());
        }
        return copy;
    }

    private static void removeFromSource(NonNullList<ItemStack> storage, int slot, int amount) {
        ItemStack source = storage.get(slot);
        source.shrink(amount);
        if (source.isEmpty()) {
            storage.set(slot, ItemStack.EMPTY);
        }
    }

    private static boolean insertAll(ItemStack backpack, NonNullList<ItemStack> storage, ItemStack incoming) {
        if (!BackpackStackData.isValidStorageItem(backpack, incoming)) {
            return false;
        }
        ItemStack remaining = incoming.copy();
        for (ItemStack existing : storage) {
            if (remaining.isEmpty()) {
                return true;
            }
            if (existing.isEmpty() || !ItemStackCompat.isSameItemSameComponents(existing, remaining)) {
                continue;
            }
            int transfer = Math.min(remaining.getCount(), BackpackStackData.getStorageStackLimit(backpack, existing) - existing.getCount());
            if (transfer > 0) {
                existing.grow(transfer);
                remaining.shrink(transfer);
            }
        }
        for (int slot = 0; slot < storage.size() && !remaining.isEmpty(); slot++) {
            if (!storage.get(slot).isEmpty()) {
                continue;
            }
            int transfer = Math.min(remaining.getCount(), BackpackStackData.getStorageStackLimit(backpack, remaining));
            storage.set(slot, remaining.copyWithCount(transfer));
            remaining.shrink(transfer);
        }
        return remaining.isEmpty();
    }

    private static void commit(ServerPlayer player, BackpackAccess access, ItemStack backpack,
            NonNullList<ItemStack> storage, ItemStack selectedStack) {
        BackpackStackData.saveStorage(backpack, storage);
        access.setBackpackStack(player, backpack);
        player.getInventory().setItem(player.getInventory().selected, selectedStack);
        player.getInventory().setChanged();
    }
}
