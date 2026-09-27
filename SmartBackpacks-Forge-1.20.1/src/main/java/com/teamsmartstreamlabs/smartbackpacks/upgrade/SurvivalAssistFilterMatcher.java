package com.teamsmartstreamlabs.smartbackpacks.upgrade;


import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class SurvivalAssistFilterMatcher {
    private SurvivalAssistFilterMatcher() {
    }

    public static boolean allows(SurvivalAssistUpgradeData data, ItemStack candidate, Set<ResourceLocation> candidateEffects) {
        if (candidate.isEmpty()) {
            return false;
        }

        boolean itemMatched = ItemContainerContentsHelper.nonEmptyStream(data.itemFilters())
                .anyMatch(filter -> com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.isSameItemSameComponents(filter, candidate));
        boolean effectMatched = candidateEffects.stream().anyMatch(data.effectFilters()::contains);
        if (!data.hasAnyFilters()) {
            return true;
        }
        return itemMatched || effectMatched;
    }
}


