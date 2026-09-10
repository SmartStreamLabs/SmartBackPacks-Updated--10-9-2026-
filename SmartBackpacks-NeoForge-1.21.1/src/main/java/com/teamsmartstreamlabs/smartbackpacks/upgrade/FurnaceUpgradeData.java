package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.component.ItemContainerContents;

public record FurnaceUpgradeData(
        ItemContainerContents items,
        int litTime,
        int litDuration,
        int cookingProgress,
        int cookingTotalTime) {
    public static final int SLOT_COUNT = 3;
    public static final int DEFAULT_COOK_TIME = 200;
    public static final FurnaceUpgradeData DEFAULT = new FurnaceUpgradeData(
            ItemContainerContents.EMPTY,
            0,
            0,
            0,
            DEFAULT_COOK_TIME);

    public static final Codec<FurnaceUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemContainerContents.CODEC.optionalFieldOf("items", ItemContainerContents.EMPTY).forGetter(FurnaceUpgradeData::items),
            Codec.INT.optionalFieldOf("lit_time", 0).forGetter(FurnaceUpgradeData::litTime),
            Codec.INT.optionalFieldOf("lit_duration", 0).forGetter(FurnaceUpgradeData::litDuration),
            Codec.INT.optionalFieldOf("cooking_progress", 0).forGetter(FurnaceUpgradeData::cookingProgress),
            Codec.INT.optionalFieldOf("cooking_total_time", DEFAULT_COOK_TIME).forGetter(FurnaceUpgradeData::cookingTotalTime)
    ).apply(instance, FurnaceUpgradeData::new));
}
