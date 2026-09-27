package com.teamsmartstreamlabs.smartbackpacks.upgrade;


import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

public final class VoidFilterMatcher {
    private VoidFilterMatcher() {
    }

    public static boolean allows(VoidUpgradeData data, ItemStack candidate) {
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

    private static boolean matchesAnyConfiguredFilter(VoidUpgradeData data, ItemStack candidate) {
        if (matchesItemFilters(data, candidate)) {
            return true;
        }

        if (data.modFilters().stream().anyMatch(modId -> modId.equals(BuiltInRegistries.ITEM.getKey(candidate.getItem()).getNamespace()))) {
            return true;
        }

        for (ResourceLocation tagId : data.tagFilters()) {
            if (candidate.is(TagKey.create(Registries.ITEM, tagId))) {
                return true;
            }
        }

        return false;
    }

    private static boolean matchesItemFilters(VoidUpgradeData data, ItemStack candidate) {
        return ItemContainerContentsHelper.nonEmptyStream(data.itemFilters())
                .anyMatch(filter -> matchesItemFilter(data, filter, candidate));
    }

    private static boolean matchesItemFilter(VoidUpgradeData data, ItemStack filter, ItemStack candidate) {
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
                com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.remove(left, DataComponents.DAMAGE);
                com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.remove(right, DataComponents.DAMAGE);
            }
            return com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.isSameItemSameComponents(left, right);
        }

        return true;
    }
}



