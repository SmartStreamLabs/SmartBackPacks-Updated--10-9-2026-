package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

public enum MagnetFilterInputType implements StringRepresentable {
    ITEM("item"),
    MOD("mod"),
    TAG("tag");

    public static final Codec<MagnetFilterInputType> CODEC = StringRepresentable.fromEnum(MagnetFilterInputType::values);

    private final String serializedName;

    MagnetFilterInputType(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }
}
