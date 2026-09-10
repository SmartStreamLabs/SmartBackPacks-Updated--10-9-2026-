package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.component.ItemContainerContents;

public record QuickAccessWheelUpgradeData(ItemContainerContents favorites) {
    public static final int FAVORITE_COUNT = 8;
    public static final QuickAccessWheelUpgradeData DEFAULT = new QuickAccessWheelUpgradeData(ItemContainerContents.EMPTY);
    public static final Codec<QuickAccessWheelUpgradeData> CODEC = ItemContainerContents.CODEC
            .optionalFieldOf("favorites", ItemContainerContents.EMPTY)
            .xmap(QuickAccessWheelUpgradeData::new, QuickAccessWheelUpgradeData::favorites)
            .codec();

    public NonNullList<ItemStack> loadFavorites() {
        NonNullList<ItemStack> stacks = NonNullList.withSize(FAVORITE_COUNT, ItemStack.EMPTY);
        this.favorites.copyInto(stacks);
        return stacks;
    }
}
