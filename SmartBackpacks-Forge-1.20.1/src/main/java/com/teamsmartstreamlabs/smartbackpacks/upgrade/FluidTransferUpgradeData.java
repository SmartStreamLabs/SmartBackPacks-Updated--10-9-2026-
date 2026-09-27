package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.component.ItemContainerContents;

public record FluidTransferUpgradeData(
        boolean pushMode,
        boolean collectSourceBlocks,
        ItemContainerContents fluidFilters) {
    public static final int FILTER_SLOT_COUNT = 4;
    public static final FluidTransferUpgradeData DEFAULT = new FluidTransferUpgradeData(false, true, ItemContainerContents.EMPTY);

    public static final Codec<FluidTransferUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("push_mode", false).forGetter(FluidTransferUpgradeData::pushMode),
            Codec.BOOL.optionalFieldOf("collect_source_blocks", true).forGetter(FluidTransferUpgradeData::collectSourceBlocks),
            ItemContainerContents.CODEC.optionalFieldOf("fluid_filters", ItemContainerContents.EMPTY).forGetter(FluidTransferUpgradeData::fluidFilters)
    ).apply(instance, FluidTransferUpgradeData::new));

    public boolean hasFilters() {
        return ItemContainerContentsHelper.nonEmptyStream(this.fluidFilters).findAny().isPresent();
    }
}
