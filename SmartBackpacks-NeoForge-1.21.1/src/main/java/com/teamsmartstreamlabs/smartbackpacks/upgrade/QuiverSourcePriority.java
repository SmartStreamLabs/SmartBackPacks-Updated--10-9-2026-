package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

public enum QuiverSourcePriority implements StringRepresentable {
    PLAYER_INVENTORY_FIRST("player_inventory_first"),
    QUIVER_FIRST("quiver_first"),
    QUIVER_ONLY("quiver_only");

    public static final Codec<QuiverSourcePriority> CODEC = StringRepresentable.fromEnum(QuiverSourcePriority::values);

    private final String serializedName;

    QuiverSourcePriority(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    public QuiverSourcePriority next() {
        QuiverSourcePriority[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
