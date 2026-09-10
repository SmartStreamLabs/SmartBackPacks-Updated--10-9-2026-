package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

public final class HopperFilterMatcher {
    private HopperFilterMatcher() {
    }

    public static boolean allows(HopperUpgradeData data, ItemStack candidate) {
        if (candidate.isEmpty()) {
            return false;
        }

        if (data.blockModdedItems()
                && !"minecraft".equals(BuiltInRegistries.ITEM.getKey(candidate.getItem()).getNamespace())) {
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

