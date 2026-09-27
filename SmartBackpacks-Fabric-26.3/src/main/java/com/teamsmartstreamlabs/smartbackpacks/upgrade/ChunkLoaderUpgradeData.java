package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ChunkLoaderUpgradeData(int radius) {
    public static final int DEFAULT_RADIUS = 0;
    public static final int MAX_RADIUS = 3;
    public static final ChunkLoaderUpgradeData DEFAULT = new ChunkLoaderUpgradeData(DEFAULT_RADIUS);

    public static final Codec<ChunkLoaderUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("radius", DEFAULT_RADIUS).forGetter(ChunkLoaderUpgradeData::radius)
    ).apply(instance, ChunkLoaderUpgradeData::new));

    public ChunkLoaderUpgradeData {
        radius = Math.max(DEFAULT_RADIUS, Math.min(MAX_RADIUS, radius));
    }

    public ChunkLoaderUpgradeData nextRadius() {
        return new ChunkLoaderUpgradeData(this.radius >= MAX_RADIUS ? DEFAULT_RADIUS : this.radius + 1);
    }

    public int loadedChunkCount() {
        int diameter = this.radius * 2 + 1;
        return diameter * diameter;
    }
}
