package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

public enum BuilderRefillMode implements StringRepresentable {
    WHEN_EMPTY("when_empty"),
    BELOW_THRESHOLD("below_threshold"),
    KEEP_FULL("keep_full"),
    MANUAL("manual");

    public static final Codec<BuilderRefillMode> CODEC = StringRepresentable.fromEnum(BuilderRefillMode::values);

    private final String serializedName;

    BuilderRefillMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    public BuilderRefillMode next() {
        return switch (this) {
            case WHEN_EMPTY -> BELOW_THRESHOLD;
            case BELOW_THRESHOLD -> KEEP_FULL;
            case KEEP_FULL, MANUAL -> WHEN_EMPTY;
        };
    }
}
