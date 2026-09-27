package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.component.ItemContainerContents;

public record RescueUpgradeData(
        boolean enabled,
        boolean totemEnabled,
        boolean goldenAppleEnabled,
        boolean fallEnabled,
        boolean lavaEnabled,
        int healthThreshold,
        int lavaHealthThreshold,
        int minimumFallDistance,
        ItemContainerContents rescueItems,
        int globalCooldownTicks,
        int totemCooldownTicks,
        int goldenAppleCooldownTicks,
        int fallCooldownTicks,
        int lavaCooldownTicks) {
    public static final int SLOT_COUNT = 3;
    public static final int TOTEM_SLOT = 0;
    public static final int APPLE_SLOT = 1;
    public static final int WATER_SLOT = 2;
    public static final RescueUpgradeData DEFAULT = new RescueUpgradeData(
            true,
            true,
            true,
            true,
            true,
            8,
            12,
            12,
            ItemContainerContents.EMPTY,
            0,
            0,
            0,
            0,
            0);

    public static final Codec<RescueUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(RescueUpgradeData::enabled),
            Codec.BOOL.optionalFieldOf("totem_enabled", true).forGetter(RescueUpgradeData::totemEnabled),
            Codec.BOOL.optionalFieldOf("golden_apple_enabled", true).forGetter(RescueUpgradeData::goldenAppleEnabled),
            Codec.BOOL.optionalFieldOf("fall_enabled", true).forGetter(RescueUpgradeData::fallEnabled),
            Codec.BOOL.optionalFieldOf("lava_enabled", true).forGetter(RescueUpgradeData::lavaEnabled),
            Codec.INT.optionalFieldOf("health_threshold", 8).forGetter(RescueUpgradeData::healthThreshold),
            Codec.INT.optionalFieldOf("lava_health_threshold", 12).forGetter(RescueUpgradeData::lavaHealthThreshold),
            Codec.INT.optionalFieldOf("minimum_fall_distance", 12).forGetter(RescueUpgradeData::minimumFallDistance),
            ItemContainerContents.CODEC.optionalFieldOf("rescue_items", ItemContainerContents.EMPTY).forGetter(RescueUpgradeData::rescueItems),
            Codec.INT.optionalFieldOf("global_cooldown_ticks", 0).forGetter(RescueUpgradeData::globalCooldownTicks),
            Codec.INT.optionalFieldOf("totem_cooldown_ticks", 0).forGetter(RescueUpgradeData::totemCooldownTicks),
            Codec.INT.optionalFieldOf("golden_apple_cooldown_ticks", 0).forGetter(RescueUpgradeData::goldenAppleCooldownTicks),
            Codec.INT.optionalFieldOf("fall_cooldown_ticks", 0).forGetter(RescueUpgradeData::fallCooldownTicks),
            Codec.INT.optionalFieldOf("lava_cooldown_ticks", 0).forGetter(RescueUpgradeData::lavaCooldownTicks)
    ).apply(instance, RescueUpgradeData::new));

    public RescueUpgradeData {
        healthThreshold = clampHearts(healthThreshold);
        lavaHealthThreshold = clampHearts(lavaHealthThreshold);
        minimumFallDistance = Math.max(4, Math.min(64, minimumFallDistance));
        globalCooldownTicks = Math.max(0, globalCooldownTicks);
        totemCooldownTicks = Math.max(0, totemCooldownTicks);
        goldenAppleCooldownTicks = Math.max(0, goldenAppleCooldownTicks);
        fallCooldownTicks = Math.max(0, fallCooldownTicks);
        lavaCooldownTicks = Math.max(0, lavaCooldownTicks);
    }

    public NonNullList<ItemStack> loadItems() {
        NonNullList<ItemStack> stacks = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        this.rescueItems.copyInto(stacks);
        return stacks;
    }

    public boolean hasAnyItems() {
        return ItemContainerContentsHelper.nonEmptyStream(this.rescueItems).findAny().isPresent();
    }

    public RescueUpgradeData withItems(NonNullList<ItemStack> stacks) {
        return new RescueUpgradeData(this.enabled, this.totemEnabled, this.goldenAppleEnabled, this.fallEnabled, this.lavaEnabled,
                this.healthThreshold, this.lavaHealthThreshold, this.minimumFallDistance, ItemContainerContents.fromItems(stacks),
                this.globalCooldownTicks, this.totemCooldownTicks, this.goldenAppleCooldownTicks, this.fallCooldownTicks, this.lavaCooldownTicks);
    }

    public RescueUpgradeData withEnabled(boolean enabled) {
        return new RescueUpgradeData(enabled, this.totemEnabled, this.goldenAppleEnabled, this.fallEnabled, this.lavaEnabled,
                this.healthThreshold, this.lavaHealthThreshold, this.minimumFallDistance, this.rescueItems,
                this.globalCooldownTicks, this.totemCooldownTicks, this.goldenAppleCooldownTicks, this.fallCooldownTicks, this.lavaCooldownTicks);
    }

    public RescueUpgradeData withModeToggles(boolean totemEnabled, boolean goldenAppleEnabled, boolean fallEnabled, boolean lavaEnabled) {
        return new RescueUpgradeData(this.enabled, totemEnabled, goldenAppleEnabled, fallEnabled, lavaEnabled,
                this.healthThreshold, this.lavaHealthThreshold, this.minimumFallDistance, this.rescueItems,
                this.globalCooldownTicks, this.totemCooldownTicks, this.goldenAppleCooldownTicks, this.fallCooldownTicks, this.lavaCooldownTicks);
    }

    public RescueUpgradeData withThresholds(int healthThreshold, int lavaHealthThreshold, int minimumFallDistance) {
        return new RescueUpgradeData(this.enabled, this.totemEnabled, this.goldenAppleEnabled, this.fallEnabled, this.lavaEnabled,
                healthThreshold, lavaHealthThreshold, minimumFallDistance, this.rescueItems,
                this.globalCooldownTicks, this.totemCooldownTicks, this.goldenAppleCooldownTicks, this.fallCooldownTicks, this.lavaCooldownTicks);
    }

    public RescueUpgradeData tickCooldowns() {
        return new RescueUpgradeData(this.enabled, this.totemEnabled, this.goldenAppleEnabled, this.fallEnabled, this.lavaEnabled,
                this.healthThreshold, this.lavaHealthThreshold, this.minimumFallDistance, this.rescueItems,
                decrement(this.globalCooldownTicks), decrement(this.totemCooldownTicks), decrement(this.goldenAppleCooldownTicks),
                decrement(this.fallCooldownTicks), decrement(this.lavaCooldownTicks));
    }

    public RescueUpgradeData withTotemCooldowns(int globalCooldownTicks, int totemCooldownTicks) {
        return new RescueUpgradeData(this.enabled, this.totemEnabled, this.goldenAppleEnabled, this.fallEnabled, this.lavaEnabled,
                this.healthThreshold, this.lavaHealthThreshold, this.minimumFallDistance, this.rescueItems,
                globalCooldownTicks, totemCooldownTicks, this.goldenAppleCooldownTicks, this.fallCooldownTicks, this.lavaCooldownTicks);
    }

    public RescueUpgradeData withGoldenAppleCooldowns(int globalCooldownTicks, int goldenAppleCooldownTicks) {
        return new RescueUpgradeData(this.enabled, this.totemEnabled, this.goldenAppleEnabled, this.fallEnabled, this.lavaEnabled,
                this.healthThreshold, this.lavaHealthThreshold, this.minimumFallDistance, this.rescueItems,
                globalCooldownTicks, this.totemCooldownTicks, goldenAppleCooldownTicks, this.fallCooldownTicks, this.lavaCooldownTicks);
    }

    public RescueUpgradeData withFallCooldowns(int globalCooldownTicks, int fallCooldownTicks) {
        return new RescueUpgradeData(this.enabled, this.totemEnabled, this.goldenAppleEnabled, this.fallEnabled, this.lavaEnabled,
                this.healthThreshold, this.lavaHealthThreshold, this.minimumFallDistance, this.rescueItems,
                globalCooldownTicks, this.totemCooldownTicks, this.goldenAppleCooldownTicks, fallCooldownTicks, this.lavaCooldownTicks);
    }

    public RescueUpgradeData withLavaCooldowns(int globalCooldownTicks, int lavaCooldownTicks) {
        return new RescueUpgradeData(this.enabled, this.totemEnabled, this.goldenAppleEnabled, this.fallEnabled, this.lavaEnabled,
                this.healthThreshold, this.lavaHealthThreshold, this.minimumFallDistance, this.rescueItems,
                globalCooldownTicks, this.totemCooldownTicks, this.goldenAppleCooldownTicks, this.fallCooldownTicks, lavaCooldownTicks);
    }

    public boolean globalReady() {
        return this.globalCooldownTicks <= 0;
    }

    private static int decrement(int value) {
        return Math.max(0, value - 1);
    }

    private static int clampHearts(int value) {
        return Math.max(1, Math.min(20, value));
    }
}
