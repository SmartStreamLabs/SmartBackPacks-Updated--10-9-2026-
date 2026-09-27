package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

public enum BuilderMatchMode implements StringRepresentable {
    EXACT("exact"),
    ITEM("item");

    public static final Codec<BuilderMatchMode> CODEC = StringRepresentable.fromEnum(BuilderMatchMode::values);

    private final String serializedName;

    BuilderMatchMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    public BuilderMatchMode next() {
        BuilderMatchMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
