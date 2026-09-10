package com.teamsmartstreamlabs.smartbackpacks.upgrade;


import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.item.MagnetUpgradeItem;

import net.minecraft.core.NonNullList;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

public final class MagnetFilterMatcher {
    private MagnetFilterMatcher() {
    }

    public static boolean allows(MagnetUpgradeItem upgradeItem, MagnetUpgradeData data, ItemStack candidate, ItemStack backpackStack, BackpackTier tier) {
        NonNullList<ItemStack> contents = BackpackStackData.loadStorage(backpackStack, tier);
        return allows(upgradeItem, data, candidate, backpackStack, tier, contents);
    }

    public static boolean allows(MagnetUpgradeItem upgradeItem, MagnetUpgradeData data, ItemStack candidate, ItemStack backpackStack, BackpackTier tier, NonNullList<ItemStack> contents) {
        if (candidate.isEmpty()) {
            return false;
        }

        if (data.blockModdedItems()
                && !"minecraft".equals(BuiltInRegistries.ITEM.getKey(candidate.getItem()).getNamespace())) {
            return false;
        }

        if (ItemLockProtection.insertIntoStorage(backpackStack, contents, candidate, true).getCount() == candidate.getCount()) {
            return false;
        }

        return allowsPrechecked(upgradeItem, data, candidate, contents);
    }

    public static boolean allowsPrechecked(MagnetUpgradeItem upgradeItem, MagnetUpgradeData data, ItemStack candidate, NonNullList<ItemStack> contents) {
        if (candidate.isEmpty()) {
            return false;
        }

        if (data.blockModdedItems()
                && !"minecraft".equals(BuiltInRegistries.ITEM.getKey(candidate.getItem()).getNamespace())) {
            return false;
        }

        if (data.matchBackpackContentsOnly() && !existsInBackpack(contents, candidate)) {
            return false;
        }

        boolean matched = matchesAnyConfiguredFilter(upgradeItem, data, candidate);
        if (!data.hasAnyFilters()) {
            return !data.allowlist();
        }
        return data.allowlist() ? matched : !matched;
    }

    private static boolean existsInBackpack(ItemStack backpackStack, BackpackTier tier, ItemStack candidate) {
        return existsInBackpack(BackpackStackData.loadStorage(backpackStack, tier), candidate);
    }

    private static boolean existsInBackpack(NonNullList<ItemStack> contents, ItemStack candidate) {
        for (ItemStack stack : contents) {
            if (stack.isEmpty() || !ItemStack.isSameItem(stack, candidate)) {
                continue;
            }

            if (candidate.isDamageableItem() && stack.isDamageableItem() && stack.getDamageValue() != candidate.getDamageValue()) {
                continue;
            }

            return true;
        }
        return false;
    }

    private static boolean matchesAnyConfiguredFilter(MagnetUpgradeItem upgradeItem, MagnetUpgradeData data, ItemStack candidate) {
        if (matchesItemFilters(upgradeItem, data, candidate)) {
            return true;
        }

        if (upgradeItem.isAdvanced()) {
            if (data.modFilters().stream().anyMatch(modId -> modId.equals(BuiltInRegistries.ITEM.getKey(candidate.getItem()).getNamespace()))) {
                return true;
            }

            for (ResourceLocation tagId : data.tagFilters()) {
                if (candidate.is(TagKey.create(Registries.ITEM, tagId))) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean matchesItemFilters(MagnetUpgradeItem upgradeItem, MagnetUpgradeData data, ItemStack candidate) {
        return ItemContainerContentsHelper.nonEmptyStream(data.itemFilters())
                .anyMatch(filter -> matchesItemFilter(upgradeItem, data, filter, candidate));
    }

    private static boolean matchesItemFilter(MagnetUpgradeItem upgradeItem, MagnetUpgradeData data, ItemStack filter, ItemStack candidate) {
        if (!ItemStack.isSameItem(filter, candidate)) {
            return false;
        }

        if (!upgradeItem.isAdvanced()) {
            return true;
        }

        if (data.matchDamage() && filter.isDamageableItem() && filter.getDamageValue() != candidate.getDamageValue()) {
            return false;
        }

        if (data.matchNbt()) {
            ItemStack left = filter.copy();
            ItemStack right = candidate.copy();
            if (!data.matchDamage()) {
                com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.remove(left, DataComponents.DAMAGE);
                com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.remove(right, DataComponents.DAMAGE);
            }
            return com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.isSameItemSameComponents(left, right);
        }

        return true;
    }
}



