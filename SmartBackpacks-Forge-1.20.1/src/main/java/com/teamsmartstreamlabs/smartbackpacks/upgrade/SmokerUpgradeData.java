package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.component.ItemContainerContents;

public record SmokerUpgradeData(
        ItemContainerContents items,
        int litTime,
        int litDuration,
        int cookingProgress,
        int cookingTotalTime) {
    public static final int SLOT_COUNT = 3;
    public static final int DEFAULT_COOK_TIME = 100;
    public static final SmokerUpgradeData DEFAULT = new SmokerUpgradeData(
            ItemContainerContents.EMPTY,
            0,
            0,
            0,
            DEFAULT_COOK_TIME);

    public static final Codec<SmokerUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemContainerContents.CODEC.optionalFieldOf("items", ItemContainerContents.EMPTY).forGetter(SmokerUpgradeData::items),
            Codec.INT.optionalFieldOf("lit_time", 0).forGetter(SmokerUpgradeData::litTime),
            Codec.INT.optionalFieldOf("lit_duration", 0).forGetter(SmokerUpgradeData::litDuration),
            Codec.INT.optionalFieldOf("cooking_progress", 0).forGetter(SmokerUpgradeData::cookingProgress),
            Codec.INT.optionalFieldOf("cooking_total_time", DEFAULT_COOK_TIME).forGetter(SmokerUpgradeData::cookingTotalTime)
    ).apply(instance, SmokerUpgradeData::new));
}
