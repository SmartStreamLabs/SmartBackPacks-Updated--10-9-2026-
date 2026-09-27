package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

public enum TorchPlacerPriorityMode implements StringRepresentable {
    SLOT_ORDER("slot_order"),
    CHEAPEST_FIRST("cheapest_first"),
    BRIGHTEST_FIRST("brightest_first");

    public static final Codec<TorchPlacerPriorityMode> CODEC = StringRepresentable.fromEnum(TorchPlacerPriorityMode::values);

    private final String serializedName;

    TorchPlacerPriorityMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    public TorchPlacerPriorityMode next() {
        TorchPlacerPriorityMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
