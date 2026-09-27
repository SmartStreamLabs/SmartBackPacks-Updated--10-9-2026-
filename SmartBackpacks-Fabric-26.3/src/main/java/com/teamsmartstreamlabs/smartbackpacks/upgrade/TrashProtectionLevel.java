package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import net.minecraft.util.StringRepresentable;

public enum TrashProtectionLevel implements StringRepresentable {
    OFF("off"),
    VALUABLE_ITEMS("valuable_items"),
    STRICT("strict"),
    ALWAYS_CONFIRM("always_confirm");

    private final String serializedName;

    TrashProtectionLevel(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }
}
