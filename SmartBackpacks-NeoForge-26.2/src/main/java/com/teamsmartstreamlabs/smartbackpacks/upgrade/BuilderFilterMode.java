package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

public enum BuilderFilterMode implements StringRepresentable {
    ALLOW_ALL("allow_all"),
    WHITELIST("whitelist"),
    BLACKLIST("blacklist");

    public static final Codec<BuilderFilterMode> CODEC = StringRepresentable.fromEnum(BuilderFilterMode::values);

    private final String serializedName;

    BuilderFilterMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    public BuilderFilterMode next() {
        BuilderFilterMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
