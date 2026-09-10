package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.component.ItemContainerContents;

public record BrewingStandUpgradeData(
        ItemContainerContents items,
        int brewTime,
        int fuel) {
    public static final int SLOT_COUNT = 5;
    public static final BrewingStandUpgradeData DEFAULT = new BrewingStandUpgradeData(
            ItemContainerContents.EMPTY,
            0,
            0);

    public static final Codec<BrewingStandUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemContainerContents.CODEC.optionalFieldOf("items", ItemContainerContents.EMPTY).forGetter(BrewingStandUpgradeData::items),
            Codec.INT.optionalFieldOf("brew_time", 0).forGetter(BrewingStandUpgradeData::brewTime),
            Codec.INT.optionalFieldOf("fuel", 0).forGetter(BrewingStandUpgradeData::fuel)
    ).apply(instance, BrewingStandUpgradeData::new));
}
