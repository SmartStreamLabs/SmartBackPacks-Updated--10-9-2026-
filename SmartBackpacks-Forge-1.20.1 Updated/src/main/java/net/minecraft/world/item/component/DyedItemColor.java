package com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class DyedItemColor {
    public static final DyedItemColor DEFAULT = new DyedItemColor(0xA06540, true);
    public static final Codec<DyedItemColor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("rgb").forGetter(DyedItemColor::rgb),
            Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter(DyedItemColor::showInTooltip)
    ).apply(instance, DyedItemColor::new));

    private final int rgb;
    private final boolean showInTooltip;

    public DyedItemColor(int rgb, boolean showInTooltip) {
        this.rgb = rgb;
        this.showInTooltip = showInTooltip;
    }

    public int rgb() {
        return this.rgb;
    }

    public boolean showInTooltip() {
        return this.showInTooltip;
    }
}
