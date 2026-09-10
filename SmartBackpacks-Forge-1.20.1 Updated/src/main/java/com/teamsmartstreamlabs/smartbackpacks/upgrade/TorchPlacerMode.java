package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

public enum TorchPlacerMode implements StringRepresentable {
    BEHIND_PLAYER("behind_player"),
    NEARBY("nearby"),
    FLOOR_ONLY("floor_only"),
    WALL_PREFERRED("wall_preferred");

    public static final Codec<TorchPlacerMode> CODEC = StringRepresentable.fromEnum(TorchPlacerMode::values);

    private final String serializedName;

    TorchPlacerMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    public TorchPlacerMode next() {
        TorchPlacerMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
