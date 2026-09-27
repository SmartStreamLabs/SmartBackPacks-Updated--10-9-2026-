package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

public final class AutoToolFilterMatcher {
    private AutoToolFilterMatcher() {
    }

    public static boolean allows(AutoToolUpgradeData data, ItemStack candidate) {
        if (candidate.isEmpty()) {
            return false;
        }

        if (data.blockModdedItems()
                && !"minecraft".equals(BuiltInRegistries.ITEM.getKey(candidate.getItem()).getNamespace())) {
            return false;
        }

        boolean matched = matchesAnyConfiguredFilter(data, candidate);
        if (!data.hasAnyFilters()) {
            return !data.allowlist();
        }
        return data.allowlist() ? matched : !matched;
    }

    private static boolean matchesAnyConfiguredFilter(AutoToolUpgradeData data, ItemStack candidate) {
        if (matchesItemFilters(data, candidate)) {
            return true;
        }

        if (data.modFilters().stream().anyMatch(modId -> modId.equals(BuiltInRegistries.ITEM.getKey(candidate.getItem()).getNamespace()))) {
            return true;
        }

        for (Identifier tagId : data.tagFilters()) {
            if (candidate.is(TagKey.create(Registries.ITEM, tagId))) {
                return true;
            }
        }

        return false;
    }

    private static boolean matchesItemFilters(AutoToolUpgradeData data, ItemStack candidate) {
        return ItemContainerContentsHelper.nonEmptyStream(data.itemFilters())
                .anyMatch(filter -> matchesItemFilter(data, filter, candidate));
    }

    private static boolean matchesItemFilter(AutoToolUpgradeData data, ItemStack filter, ItemStack candidate) {
        if (!ItemStack.isSameItem(filter, candidate)) {
            return false;
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

