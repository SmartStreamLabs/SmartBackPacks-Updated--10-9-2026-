package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

public enum QuiverSelectionMode implements StringRepresentable {
    SLOT_ORDER("slot_order"),
    CUSTOM_PRIORITY("custom_priority"),
    AUTO_SELECT("auto_select");

    public static final Codec<QuiverSelectionMode> CODEC = StringRepresentable.fromEnum(QuiverSelectionMode::values);

    private final String serializedName;

    QuiverSelectionMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    public QuiverSelectionMode next() {
        QuiverSelectionMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
