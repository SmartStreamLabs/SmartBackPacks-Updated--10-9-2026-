package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import net.minecraft.world.item.ItemStack;

public final class AutoFeedFilterMatcher {
    private AutoFeedFilterMatcher() {
    }

    public static boolean allows(AutoFeedUpgradeData data, ItemStack candidate) {
        if (candidate.isEmpty()) {
            return false;
        }

        if (!data.hasAnyFilters()) {
            return true;
        }

        return data.matchesItem(candidate);
    }
}
