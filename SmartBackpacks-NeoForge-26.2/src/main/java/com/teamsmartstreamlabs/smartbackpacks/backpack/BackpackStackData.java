package com.teamsmartstreamlabs.smartbackpacks.backpack;

import java.util.ArrayList;
import java.util.List;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackUpgradeInventory;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.NestedStorageUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.SoulboundUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.StorageUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.VoidboundUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public final class BackpackStackData {
    public static final int STORAGE_UPGRADE_I_MULTIPLIER_NUMERATOR = 3;
    public static final int STORAGE_UPGRADE_I_MULTIPLIER_DENOMINATOR = 2;
    public static final int STORAGE_UPGRADE_II_MULTIPLIER = 4;
    public static final int STORAGE_UPGRADE_III_STACK_LIMIT = 356;
    public static final int STORAGE_UPGRADE_IV_STACK_LIMIT = 612;
    public static final int STORAGE_UPGRADE_V_STACK_LIMIT = 2024;
    public static final int ULTIMATE_STORAGE_STACK_LIMIT = Integer.MAX_VALUE;

    private BackpackStackData() {
    }

    public static NonNullList<ItemStack> loadStorage(ItemStack backpack, BackpackTier tier) {
        NonNullList<ItemStack> items = loadContents(backpack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY), tier.getSlotCount());
        List<Integer> overflow = backpack.getOrDefault(ModDataComponents.BACKPACK_STORAGE_OVERFLOW.get(), List.of());
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (stack.isEmpty()) {
                continue;
            }

            int extra = slot < overflow.size() ? overflow.get(slot) : 0;
            if (extra <= 0) {
                continue;
            }

            int stackLimit = getStorageStackLimit(backpack, stack);
            stack.setCount(restoreStorageCount(stack.getCount(), extra, stackLimit));
        }
        return items;
    }

    public static void saveStorage(ItemStack backpack, NonNullList<ItemStack> items) {
        NonNullList<ItemStack> serializedItems = NonNullList.withSize(items.size(), ItemStack.EMPTY);
        List<Integer> overflow = new ArrayList<>(items.size());
        boolean hasOverflow = false;

        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (stack.isEmpty()) {
                serializedItems.set(slot, ItemStack.EMPTY);
                overflow.add(0);
                continue;
            }

            int stackLimit = getStorageStackLimit(backpack, stack);
            StoredCounts counts = splitStorageCount(stack.getCount(), stackLimit, stack.getMaxStackSize());
            ItemStack serialized = stack.copyWithCount(counts.serialized());

            serializedItems.set(slot, serialized);
            overflow.add(counts.overflow());
            hasOverflow |= counts.overflow() > 0;
        }

        backpack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(serializedItems));
        if (hasOverflow) {
            backpack.set(ModDataComponents.BACKPACK_STORAGE_OVERFLOW.get(), overflow);
        } else {
            backpack.remove(ModDataComponents.BACKPACK_STORAGE_OVERFLOW.get());
        }
    }

    public static NonNullList<ItemStack> loadUpgrades(ItemStack backpack) {
        return loadContents(backpack.getOrDefault(ModDataComponents.BACKPACK_UPGRADES.get(), ItemContainerContents.EMPTY), BackpackUpgradeInventory.UPGRADE_SLOT_COUNT);
    }

    public static void saveUpgrades(ItemStack backpack, NonNullList<ItemStack> items) {
        backpack.set(ModDataComponents.BACKPACK_UPGRADES.get(), ItemContainerContents.fromItems(items));
    }

    public static ItemStack insertIntoStorage(ItemStack backpack, BackpackTier tier, ItemStack incoming, boolean simulate) {
        if (incoming.isEmpty() || !isValidStorageItem(backpack, incoming)) {
            return incoming;
        }

        NonNullList<ItemStack> items = loadStorage(backpack, tier);
        ItemStack remaining = incoming.copy();

        for (int slot = 0; slot < items.size() && !remaining.isEmpty(); slot++) {
            ItemStack existing = items.get(slot);
            if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, remaining)) {
                continue;
            }

            int stackLimit = getStorageStackLimit(backpack, existing);
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
            if (!items.get(slot).isEmpty()) {
                continue;
            }

            int placed = Math.min(remaining.getCount(), getStorageStackLimit(backpack, remaining));
            if (!simulate) {
                items.set(slot, remaining.copyWithCount(placed));
            }
            remaining.shrink(placed);
        }

        if (!simulate && remaining.getCount() != incoming.getCount()) {
            saveStorage(backpack, items);
        }

        return remaining;
    }

    public static boolean allowsNestedBackpacks(ItemStack backpack) {
        if (SmartBackpacksConfig.allowBackpackInBackpack()) {
            return true;
        }

        for (ItemStack upgrade : loadUpgrades(backpack)) {
            if (upgrade.getItem() instanceof NestedStorageUpgradeItem) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasVoidboundUpgrade(ItemStack backpack) {
        if (!(backpack.getItem() instanceof BackpackItem)) {
            return false;
        }

        for (ItemStack upgrade : loadUpgrades(backpack)) {
            if (upgrade.getItem() instanceof VoidboundUpgradeItem) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasSoulboundUpgrade(ItemStack backpack) {
        if (!(backpack.getItem() instanceof BackpackItem)) {
            return false;
        }

        for (ItemStack upgrade : loadUpgrades(backpack)) {
            if (upgrade.getItem() instanceof SoulboundUpgradeItem) {
                return true;
            }
        }

        return false;
    }

    public static boolean consumeSoulboundUpgradeUse(ItemStack backpack) {
        if (!(backpack.getItem() instanceof BackpackItem)) {
            return false;
        }

        NonNullList<ItemStack> upgrades = loadUpgrades(backpack);
        for (int slot = 0; slot < upgrades.size(); slot++) {
            ItemStack upgrade = upgrades.get(slot);
            if (!(upgrade.getItem() instanceof SoulboundUpgradeItem)) {
                continue;
            }

            if (!upgrade.isDamageableItem()) {
                upgrades.set(slot, ItemStack.EMPTY);
                saveUpgrades(backpack, upgrades);
                return true;
            }

            int nextDamage = upgrade.getDamageValue() + 1;
            if (nextDamage >= upgrade.getMaxDamage()) {
                upgrades.set(slot, ItemStack.EMPTY);
            } else {
                ItemStack damagedUpgrade = upgrade.copy();
                damagedUpgrade.setDamageValue(nextDamage);
                upgrades.set(slot, damagedUpgrade);
            }

            saveUpgrades(backpack, upgrades);
            return true;
        }

        return false;
    }

    public static boolean isValidStorageItem(ItemStack backpack, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (BackpackItem.isBackpack(stack) && !allowsNestedBackpacks(backpack)) {
            return false;
        }

        return true;
    }

    public static boolean isValidStorageItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (!SmartBackpacksConfig.allowBackpackInBackpack() && BackpackItem.isBackpack(stack)) {
            return false;
        }

        return true;
    }

    public static int getStorageStackLimit(ItemStack backpack, ItemStack stack) {
        if (stack.isEmpty()) {
            return getBackpackMaxStackSize(backpack);
        }

        int multiplierTier = getStorageUpgradeTier(backpack);
        return getStorageStackLimit(stack.getMaxStackSize(), multiplierTier);
    }

    static int getStorageStackLimit(int baseLimit, int multiplierTier) {
        if (multiplierTier <= 0) {
            return baseLimit;
        }

        if (baseLimit <= 1) {
            return switch (multiplierTier) {
                case 6 -> ULTIMATE_STORAGE_STACK_LIMIT;
                case 5 -> STORAGE_UPGRADE_V_STACK_LIMIT;
                case 4 -> STORAGE_UPGRADE_IV_STACK_LIMIT;
                case 3 -> STORAGE_UPGRADE_III_STACK_LIMIT;
                case 2 -> 256;
                default -> 96;
            };
        }

        if (multiplierTier >= 2) {
            if (multiplierTier >= 6) {
                return Math.max(baseLimit, ULTIMATE_STORAGE_STACK_LIMIT);
            }
            if (multiplierTier >= 5) {
                return Math.max(baseLimit, STORAGE_UPGRADE_V_STACK_LIMIT);
            }
            if (multiplierTier >= 4) {
                return Math.max(baseLimit, STORAGE_UPGRADE_IV_STACK_LIMIT);
            }
            if (multiplierTier >= 3) {
                return Math.max(baseLimit, STORAGE_UPGRADE_III_STACK_LIMIT);
            }
            return Math.max(baseLimit, baseLimit * STORAGE_UPGRADE_II_MULTIPLIER);
        }

        return Math.max(baseLimit, baseLimit * STORAGE_UPGRADE_I_MULTIPLIER_NUMERATOR / STORAGE_UPGRADE_I_MULTIPLIER_DENOMINATOR);
    }

    public static int getBackpackMaxStackSize(ItemStack backpack) {
        return switch (getStorageUpgradeTier(backpack)) {
            case 6 -> ULTIMATE_STORAGE_STACK_LIMIT;
            case 5 -> STORAGE_UPGRADE_V_STACK_LIMIT;
            case 4 -> STORAGE_UPGRADE_IV_STACK_LIMIT;
            case 3 -> STORAGE_UPGRADE_III_STACK_LIMIT;
            case 2 -> 256;
            case 1 -> 96;
            default -> 64;
        };
    }

    static StoredCounts splitStorageCount(int logicalCount, int storageLimit, int legalSerializedLimit) {
        int safeCount = Math.min(Math.max(0, logicalCount), Math.max(0, storageLimit));
        int serializedCount = Math.min(safeCount, Math.max(1, legalSerializedLimit));
        return new StoredCounts(serializedCount, safeCount - serializedCount);
    }

    static int restoreStorageCount(int serializedCount, int overflowCount, int storageLimit) {
        long restored = Math.max(0L, (long) serializedCount) + Math.max(0L, (long) overflowCount);
        return (int) Math.min(Math.max(0L, (long) storageLimit), restored);
    }

    record StoredCounts(int serialized, int overflow) {
    }

    public static int getStorageUpgradeTier(ItemStack backpack) {
        for (ItemStack upgrade : loadUpgrades(backpack)) {
            if (!(upgrade.getItem() instanceof StorageUpgradeItem)) {
                continue;
            }

            if (upgrade.is(ModItems.ULTIMATE_STORAGE_UPGRADE.get())) {
                return 6;
            }

            if (upgrade.is(ModItems.STORAGE_UPGRADE_V.get())) {
                return 5;
            }

            if (upgrade.is(ModItems.STORAGE_UPGRADE_IV.get())) {
                return 4;
            }

            if (upgrade.is(ModItems.STORAGE_UPGRADE_III.get())) {
                return 3;
            }

            if (upgrade.is(ModItems.STORAGE_UPGRADE_II.get())) {
                return 2;
            }

            if (upgrade.is(ModItems.STORAGE_UPGRADE_I.get())) {
                return 1;
            }
        }
        return 0;
    }

    private static NonNullList<ItemStack> loadContents(ItemContainerContents contents, int size) {
        NonNullList<ItemStack> items = NonNullList.withSize(size, ItemStack.EMPTY);
        contents.copyInto(items);
        return items;
    }
}
