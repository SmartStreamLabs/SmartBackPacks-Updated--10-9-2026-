package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

public final class PickupFilterMatcher {
    private PickupFilterMatcher() {
    }

    public static boolean allows(PickupUpgradeData data, ItemStack candidate, ItemStack backpackStack, BackpackTier tier) {
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

        boolean matched = data.matchesItemId(candidate)
                || data.modFilters().stream().anyMatch(modId -> modId.equals(BuiltInRegistries.ITEM.getKey(candidate.getItem()).getNamespace()));

        if (!matched) {
            for (ResourceLocation tagId : data.tagFilters()) {
                if (candidate.is(TagKey.create(Registries.ITEM, tagId))) {
                    matched = true;
                    break;
                }
            }
        }

        if (!data.hasAnyFilters()) {
            return !data.allowlist();
        }

        return data.allowlist() ? matched : !matched;
    }
}

