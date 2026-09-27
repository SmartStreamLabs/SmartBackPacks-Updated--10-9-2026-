package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

public enum BackpackLinkVisibility implements StringRepresentable {
    PRIVATE("private"),
    PUBLIC("public"),
    DISABLED("disabled");

    public static final Codec<BackpackLinkVisibility> CODEC = StringRepresentable.fromEnum(BackpackLinkVisibility::values);

    private final String serializedName;

    BackpackLinkVisibility(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    public BackpackLinkVisibility next(boolean allowPublic) {
        return switch (this) {
            case PRIVATE -> allowPublic ? PUBLIC : DISABLED;
            case PUBLIC -> DISABLED;
            case DISABLED -> PRIVATE;
        };
    }
}
