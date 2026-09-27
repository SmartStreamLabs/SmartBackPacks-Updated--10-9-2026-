package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.ItemStack;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.component.ItemContainerContents;

public record AutoFeedUpgradeData(
        boolean enabled,
        ItemContainerContents itemFilters,
        int hungerThreshold,
        int saturationThreshold) {
    public static final int FILTER_SLOT_COUNT = 16;
    public static final int DEFAULT_HUNGER_THRESHOLD = 16;
    public static final int DEFAULT_SATURATION_THRESHOLD = 2;
    public static final AutoFeedUpgradeData DEFAULT = new AutoFeedUpgradeData(
            true,
            ItemContainerContents.EMPTY,
            DEFAULT_HUNGER_THRESHOLD,
            DEFAULT_SATURATION_THRESHOLD);
    public static final Codec<AutoFeedUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(AutoFeedUpgradeData::enabled),
            ItemContainerContents.CODEC.optionalFieldOf("item_filters", ItemContainerContents.EMPTY).forGetter(AutoFeedUpgradeData::itemFilters),
            Codec.intRange(0, 20).optionalFieldOf("hunger_threshold", DEFAULT_HUNGER_THRESHOLD).forGetter(AutoFeedUpgradeData::hungerThreshold),
            Codec.intRange(0, 20).optionalFieldOf("saturation_threshold", DEFAULT_SATURATION_THRESHOLD).forGetter(AutoFeedUpgradeData::saturationThreshold)
    ).apply(instance, AutoFeedUpgradeData::new));

    public boolean hasAnyFilters() {
        return ItemContainerContentsHelper.nonEmptyStream(this.itemFilters).findAny().isPresent();
    }

    public boolean matchesItem(ItemStack stack) {
        return ItemContainerContentsHelper.nonEmptyStream(this.itemFilters)
                .anyMatch(filter -> ItemStack.isSameItem(filter, stack));
    }
}
