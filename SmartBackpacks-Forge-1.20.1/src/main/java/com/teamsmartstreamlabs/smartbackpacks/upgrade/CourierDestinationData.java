package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CourierDestinationData(boolean linked, String dimension, long position, String name) {
    public static final CourierDestinationData EMPTY = new CourierDestinationData(false, "", 0L, "");
    public static final Codec<CourierDestinationData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("linked", false).forGetter(CourierDestinationData::linked),
            Codec.STRING.optionalFieldOf("dimension", "").forGetter(CourierDestinationData::dimension),
            Codec.LONG.optionalFieldOf("position", 0L).forGetter(CourierDestinationData::position),
            Codec.STRING.optionalFieldOf("name", "").forGetter(CourierDestinationData::name)
    ).apply(instance, CourierDestinationData::new));
}
