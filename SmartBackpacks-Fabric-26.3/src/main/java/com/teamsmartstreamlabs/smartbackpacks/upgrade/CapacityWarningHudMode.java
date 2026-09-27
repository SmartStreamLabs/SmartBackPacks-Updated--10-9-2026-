package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

public enum CapacityWarningHudMode implements StringRepresentable {
    THRESHOLD_ONLY("threshold_only"),
    ALWAYS("always"),
    BACKPACK_OPEN_ONLY("backpack_open_only"),
    TEMPORARY("temporary"),
    DISABLED("disabled");

    public static final Codec<CapacityWarningHudMode> CODEC =
            StringRepresentable.fromEnum(CapacityWarningHudMode::values);

    private final String serializedName;

    CapacityWarningHudMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    public CapacityWarningHudMode next() {
        CapacityWarningHudMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
