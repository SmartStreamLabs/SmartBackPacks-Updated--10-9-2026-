package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.neoforged.neoforge.fluids.FluidStack;

public record FluidStorageUpgradeData(FluidStack fluid) {
    public static final FluidStorageUpgradeData DEFAULT = new FluidStorageUpgradeData(FluidStack.EMPTY);
    public static final Codec<FluidStorageUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FluidStack.OPTIONAL_CODEC.optionalFieldOf("fluid", FluidStack.EMPTY).forGetter(FluidStorageUpgradeData::fluid)
    ).apply(instance, FluidStorageUpgradeData::new));
}
