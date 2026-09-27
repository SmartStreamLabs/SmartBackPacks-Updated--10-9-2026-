package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CapacitorUpgradeData(int energyStored) {
    public static final CapacitorUpgradeData DEFAULT = new CapacitorUpgradeData(0);
    public static final Codec<CapacitorUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("energy_stored", 0).forGetter(CapacitorUpgradeData::energyStored)
    ).apply(instance, CapacitorUpgradeData::new));
}
