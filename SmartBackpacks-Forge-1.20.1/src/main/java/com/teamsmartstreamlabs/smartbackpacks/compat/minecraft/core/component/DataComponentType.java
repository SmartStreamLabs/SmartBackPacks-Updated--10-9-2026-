package com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.core.component;

import com.mojang.serialization.Codec;

public final class DataComponentType<T> {
    private final String id;
    private final Codec<T> codec;

    public DataComponentType(String id, Codec<T> codec) {
        this.id = id;
        this.codec = codec;
    }

    public String id() {
        return this.id;
    }

    public Codec<T> codec() {
        return this.codec;
    }
}
