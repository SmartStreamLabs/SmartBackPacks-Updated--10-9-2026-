package com.teamsmartstreamlabs.smartbackpacks.upgrade;


import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.ItemLockUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class ItemLockProtection {
    private ItemLockProtection() {
    }

    public static boolean isActive(ItemStack backpack) {
        return SmartBackpacksConfig.itemLockUpgradeEnabled()
                && backpack.getItem() instanceof BackpackItem
                && hasUpgradeInstalled(backpack);
    }

    public static boolean hasUpgradeInstalled(ItemStack backpack) {
        if (!(backpack.getItem() instanceof BackpackItem)) {
            return false;
        }

        for (ItemStack upgrade : BackpackStackData.loadUpgrades(backpack)) {
            if (upgrade.getItem() instanceof ItemLockUpgradeItem) {
                return true;
            }
        }
        return false;
    }

    public static ItemLockData getData(ItemStack backpack) {
        return ItemStackCompat.getOrDefault(backpack, ModDataComponents.ITEM_LOCK_DATA.get(), ItemLockData.DEFAULT);
    }

    public static void setData(ItemStack backpack, ItemLockData data) {
        if (!backpack.isEmpty()) {
            ItemStackCompat.set(backpack, ModDataComponents.ITEM_LOCK_DATA.get(), data);
        }
    }

    public static boolean isSlotLocked(ItemStack backpack, int logicalSlot) {
        return isSlotLocked(backpack, logicalSlot, isActive(backpack), getData(backpack));
    }

    private static boolean isSlotLocked(ItemStack backpack, int logicalSlot, boolean active, ItemLockData data) {
        if (!active || logicalSlot < 0 || !SmartBackpacksConfig.itemLockAllowSlotLocks()) {
            return false;
        }

        return data.slotLocking() && data.hasLockedSlot(logicalSlot);
    }

    public static boolean isStackLocked(ItemStack backpack, ItemStack stack) {
        return isStackLocked(backpack, stack, isActive(backpack), getData(backpack));
    }

    private static boolean isStackLocked(ItemStack backpack, ItemStack stack, boolean active, ItemLockData data) {
        if (!active || stack.isEmpty()) {
            return false;
        }

        if (SmartBackpacksConfig.itemLockAllowItemLocks()
                && data.exactItemLocking()
                && data.hasItemFingerprint(fingerprint(stack))) {
            return true;
        }

        return SmartBackpacksConfig.itemLockAllowTypeLocks()
                && data.typeLocking()
                && data.hasItemType(itemType(stack));
    }

    public static boolean isProtected(ItemStack backpack, int logicalSlot, ItemStack stack) {
        return isSlotLocked(backpack, logicalSlot) || isStackLocked(backpack, stack);
    }

    public static boolean canPlayerTakeFromSlot(ItemStack backpack, int logicalSlot, ItemStack stack) {
        if (!isProtected(backpack, logicalSlot, stack)) {
            return true;
        }

        ItemLockData data = getData(backpack);
        return SmartBackpacksConfig.itemLockAllowManualAccess() && data.manualAccessAllowed();
    }

    public static boolean canPlayerInsertIntoSlot(ItemStack backpack, int logicalSlot, ItemStack existing, ItemStack incoming) {
        if (!isActive(backpack)) {
            return true;
        }

        ItemLockData data = getData(backpack);
        if (SmartBackpacksConfig.itemLockAllowManualAccess() && data.manualAccessAllowed()) {
            return true;
        }

        return !isSlotLocked(backpack, logicalSlot)
                && !isStackLocked(backpack, existing)
                && !isStackLocked(backpack, incoming);
    }

    public static boolean canAutomationExtract(ItemStack backpack, int logicalSlot, ItemStack stack) {
        if (!isProtected(backpack, logicalSlot, stack)) {
            return true;
        }

        ItemLockData data = getData(backpack);
        return SmartBackpacksConfig.itemLockAllowAutomationAccess() && data.automationAccessAllowed();
    }

    public static boolean canAutomationInsert(ItemStack backpack, int logicalSlot, ItemStack existing, ItemStack incoming) {
        return canAutomationInsert(backpack, logicalSlot, existing, incoming, isActive(backpack), getData(backpack));
    }

    private static boolean canAutomationInsert(ItemStack backpack, int logicalSlot, ItemStack existing, ItemStack incoming, boolean active, ItemLockData data) {
        if (!active) {
            return true;
        }

        if (SmartBackpacksConfig.itemLockAllowAutomationAccess() && data.automationAccessAllowed()) {
            return true;
        }

        return !isSlotLocked(backpack, logicalSlot, active, data)
                && !isStackLocked(backpack, existing, active, data)
                && !isStackLocked(backpack, incoming, active, data);
    }

    public static boolean canSortSlot(ItemStack backpack, int logicalSlot, ItemStack stack) {
        return !isProtected(backpack, logicalSlot, stack);
    }

    public static boolean canTrashStack(ItemStack backpack, int logicalSlot, ItemStack stack) {
        return !isProtected(backpack, logicalSlot, stack);
    }

    public static boolean canVoidStack(ItemStack backpack, int logicalSlot, ItemStack stack) {
        return !isProtected(backpack, logicalSlot, stack);
    }

    public static boolean canDepositStack(ItemStack backpack, int logicalSlot, ItemStack stack) {
        return !isProtected(backpack, logicalSlot, stack);
    }

    public static boolean canRestockInsert(ItemStack backpack, ItemStack stack) {
        return !isStackLocked(backpack, stack);
    }

    public static boolean canConsumeForUpgrade(ItemStack backpack, int logicalSlot, ItemStack stack) {
        return !isProtected(backpack, logicalSlot, stack);
    }

    public static ItemStack insertIntoStorage(ItemStack backpack, BackpackTier tier, ItemStack incoming, boolean simulate) {
        NonNullList<ItemStack> items = BackpackStackData.loadStorage(backpack, tier);
        ItemStack remaining = insertIntoStorage(backpack, items, incoming, simulate);

        if (!simulate && remaining.getCount() != incoming.getCount()) {
            BackpackStackData.saveStorage(backpack, items);
        }

        return remaining;
    }

    public static ItemStack insertIntoStorage(ItemStack backpack, NonNullList<ItemStack> items, ItemStack incoming, boolean simulate) {
        return insertIntoStorage(backpack, items, incoming, simulate, BackpackStackData.getStorageUpgradeTier(backpack));
    }

    public static ItemStack insertIntoStorage(ItemStack backpack, NonNullList<ItemStack> items, ItemStack incoming, boolean simulate, int storageUpgradeTier) {
        if (incoming.isEmpty() || !BackpackStackData.isValidStorageItem(backpack, incoming)) {
            return incoming;
        }

        boolean lockActive = isActive(backpack);
        ItemLockData lockData = lockActive ? getData(backpack) : ItemLockData.DEFAULT;
        if (isStackLocked(backpack, incoming, lockActive, lockData)) {
            return incoming;
        }

        ItemStack remaining = incoming.copy();

        for (int slot = 0; slot < items.size() && !remaining.isEmpty(); slot++) {
            ItemStack existing = items.get(slot);
            if (existing.isEmpty()
                    || !ItemStackCompat.isSameItemSameComponents(existing, remaining)
                    || !canAutomationInsert(backpack, slot, existing, remaining, lockActive, lockData)) {
                continue;
            }

            int stackLimit = BackpackStackData.getStorageStackLimit(backpack, existing, storageUpgradeTier);
            int transfer = Math.min(remaining.getCount(), stackLimit - existing.getCount());
            if (transfer <= 0) {
                continue;
            }

            if (!simulate) {
                existing.grow(transfer);
            }
            remaining.shrink(transfer);
        }

        for (int slot = 0; slot < items.size() && !remaining.isEmpty(); slot++) {
            ItemStack existing = items.get(slot);
            if (!existing.isEmpty() || !canAutomationInsert(backpack, slot, existing, remaining, lockActive, lockData)) {
                continue;
            }

            int placed = Math.min(remaining.getCount(), BackpackStackData.getStorageStackLimit(backpack, remaining, storageUpgradeTier));
            if (!simulate) {
                items.set(slot, remaining.copyWithCount(placed));
            }
            remaining.shrink(placed);
        }

        return remaining;
    }

    public static String fingerprint(ItemStack stack) {
        if (stack.isEmpty()) {
            return "";
        }

        return itemType(stack) + "|" + (stack.hasTag() ? stack.getTag().toString() : "");
    }

    public static String itemType(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id == null ? "minecraft:air" : id.toString();
    }
}
