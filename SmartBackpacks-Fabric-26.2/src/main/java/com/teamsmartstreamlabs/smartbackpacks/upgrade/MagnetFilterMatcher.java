package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.item.MagnetUpgradeItem;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

public final class MagnetFilterMatcher {
    private MagnetFilterMatcher() {
    }

    public static boolean allows(MagnetUpgradeItem upgradeItem, MagnetUpgradeData data, ItemStack candidate, ItemStack backpackStack, BackpackTier tier) {
        if (candidate.isEmpty()) {
            return false;
        }

        if (data.blockModdedItems()
                && !"minecraft".equals(BuiltInRegistries.ITEM.getKey(candidate.getItem()).getNamespace())) {
            return false;
        }

        if (ItemLockProtection.insertIntoStorage(backpackStack, tier, candidate, true).getCount() == candidate.getCount()) {
            return false;
        }

        if (data.matchBackpackContentsOnly() && !existsInBackpack(backpackStack, tier, candidate)) {
            return false;
        }

        boolean matched = matchesAnyConfiguredFilter(upgradeItem, data, candidate);
        if (!data.hasAnyFilters()) {
            return !data.allowlist();
        }
        return data.allowlist() ? matched : !matched;
    }

    private static boolean existsInBackpack(ItemStack backpackStack, BackpackTier tier, ItemStack candidate) {
        NonNullList<ItemStack> contents = BackpackStackData.loadStorage(backpackStack, tier);
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

            for (Identifier tagId : data.tagFilters()) {
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
                left.remove(DataComponents.DAMAGE);
                right.remove(DataComponents.DAMAGE);
            }
            return ItemStack.isSameItemSameComponents(left, right);
        }

        return true;
    }
}

