package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

public enum CapacityWarningCalculationMode implements StringRepresentable {
    OCCUPIED_SLOTS("occupied_slots"),
    STACK_CAPACITY("stack_capacity");

    public static final Codec<CapacityWarningCalculationMode> CODEC =
            StringRepresentable.fromEnum(CapacityWarningCalculationMode::values);

    private final String serializedName;

    CapacityWarningCalculationMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    public CapacityWarningCalculationMode next() {
        CapacityWarningCalculationMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
