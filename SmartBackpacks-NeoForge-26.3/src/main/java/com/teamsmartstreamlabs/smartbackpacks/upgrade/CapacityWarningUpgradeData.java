package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Mth;

public record CapacityWarningUpgradeData(
        boolean enabled,
        CapacityWarningCalculationMode calculationMode,
        int threshold1,
        int threshold2,
        int threshold3,
        boolean threshold1Enabled,
        boolean threshold2Enabled,
        boolean threshold3Enabled,
        int resetMargin,
        boolean actionBar,
        boolean sound,
        boolean hud,
        CapacityWarningHudMode hudMode,
        boolean showPercentage,
        boolean showSlotCount,
        boolean failedInsertionWarning) {
    public static final int MIN_THRESHOLD = 1;
    public static final int MAX_THRESHOLD = 100;
    public static final int MIN_RESET_MARGIN = 0;
    public static final int MAX_RESET_MARGIN = 10;

    public static final CapacityWarningUpgradeData DEFAULT = new CapacityWarningUpgradeData(
            true,
            CapacityWarningCalculationMode.OCCUPIED_SLOTS,
            10,
            90,
            100,
            true,
            true,
            true,
            3,
            true,
            true,
            true,
            CapacityWarningHudMode.ALWAYS,
            true,
            true,
            true);

    public static final Codec<CapacityWarningUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(CapacityWarningUpgradeData::enabled),
            CapacityWarningCalculationMode.CODEC.optionalFieldOf("calculation_mode", CapacityWarningCalculationMode.OCCUPIED_SLOTS).forGetter(CapacityWarningUpgradeData::calculationMode),
            Codec.INT.optionalFieldOf("threshold_1", 10).forGetter(CapacityWarningUpgradeData::threshold1),
            Codec.INT.optionalFieldOf("threshold_2", 90).forGetter(CapacityWarningUpgradeData::threshold2),
            Codec.INT.optionalFieldOf("threshold_3", 100).forGetter(CapacityWarningUpgradeData::threshold3),
            Codec.BOOL.optionalFieldOf("threshold_1_enabled", true).forGetter(CapacityWarningUpgradeData::threshold1Enabled),
            Codec.BOOL.optionalFieldOf("threshold_2_enabled", true).forGetter(CapacityWarningUpgradeData::threshold2Enabled),
            Codec.BOOL.optionalFieldOf("threshold_3_enabled", true).forGetter(CapacityWarningUpgradeData::threshold3Enabled),
            Codec.INT.optionalFieldOf("reset_margin", 3).forGetter(CapacityWarningUpgradeData::resetMargin),
            Codec.BOOL.optionalFieldOf("action_bar", true).forGetter(CapacityWarningUpgradeData::actionBar),
            Codec.BOOL.optionalFieldOf("sound", true).forGetter(CapacityWarningUpgradeData::sound),
            Codec.BOOL.optionalFieldOf("hud", true).forGetter(CapacityWarningUpgradeData::hud),
            CapacityWarningHudMode.CODEC.optionalFieldOf("hud_mode", CapacityWarningHudMode.ALWAYS).forGetter(CapacityWarningUpgradeData::hudMode),
            Codec.BOOL.optionalFieldOf("show_percentage", true).forGetter(CapacityWarningUpgradeData::showPercentage),
            Codec.BOOL.optionalFieldOf("show_slot_count", true).forGetter(CapacityWarningUpgradeData::showSlotCount),
            Codec.BOOL.optionalFieldOf("failed_insertion_warning", true).forGetter(CapacityWarningUpgradeData::failedInsertionWarning)
    ).apply(instance, CapacityWarningUpgradeData::new));

    public CapacityWarningUpgradeData {
        calculationMode = calculationMode == null ? CapacityWarningCalculationMode.OCCUPIED_SLOTS : calculationMode;
        hudMode = hudMode == null ? CapacityWarningHudMode.THRESHOLD_ONLY : hudMode;
        threshold1 = clampThreshold(threshold1);
        threshold2 = Math.max(threshold1, clampThreshold(threshold2));
        threshold3 = Math.max(threshold2, clampThreshold(threshold3));
        resetMargin = Mth.clamp(resetMargin, MIN_RESET_MARGIN, MAX_RESET_MARGIN);
    }

    public int threshold(int index) {
        return switch (index) {
            case 0 -> this.threshold1;
            case 1 -> this.threshold2;
            case 2 -> this.threshold3;
            default -> this.threshold3;
        };
    }

    public int thresholdStep() {
        return this.threshold1 == 25 ? 25 : 10;
    }

    public boolean thresholdEnabled(int index) {
        return switch (index) {
            case 0 -> this.threshold1Enabled;
            case 1 -> this.threshold2Enabled;
            case 2 -> this.threshold3Enabled;
            default -> false;
        };
    }

    public CapacityWarningUpgradeData withEnabled(boolean enabled) {
        return new CapacityWarningUpgradeData(enabled, this.calculationMode, this.threshold1, this.threshold2, this.threshold3,
                this.threshold1Enabled, this.threshold2Enabled, this.threshold3Enabled, this.resetMargin, this.actionBar,
                this.sound, this.hud, this.hudMode, this.showPercentage, this.showSlotCount, this.failedInsertionWarning);
    }

    public CapacityWarningUpgradeData withCalculationMode(CapacityWarningCalculationMode calculationMode) {
        return new CapacityWarningUpgradeData(this.enabled, calculationMode, this.threshold1, this.threshold2, this.threshold3,
                this.threshold1Enabled, this.threshold2Enabled, this.threshold3Enabled, this.resetMargin, this.actionBar,
                this.sound, this.hud, this.hudMode, this.showPercentage, this.showSlotCount, this.failedInsertionWarning);
    }

    public CapacityWarningUpgradeData withThreshold(int index, int value) {
        int next1 = index == 0 ? value : this.threshold1;
        int next2 = index == 1 ? value : this.threshold2;
        int next3 = index == 2 ? value : this.threshold3;
        return new CapacityWarningUpgradeData(this.enabled, this.calculationMode, next1, next2, next3,
                this.threshold1Enabled, this.threshold2Enabled, this.threshold3Enabled, this.resetMargin, this.actionBar,
                this.sound, this.hud, this.hudMode, this.showPercentage, this.showSlotCount, this.failedInsertionWarning);
    }

    public CapacityWarningUpgradeData withThresholdEnabled(int index, boolean enabled) {
        return new CapacityWarningUpgradeData(this.enabled, this.calculationMode, this.threshold1, this.threshold2, this.threshold3,
                index == 0 ? enabled : this.threshold1Enabled,
                index == 1 ? enabled : this.threshold2Enabled,
                index == 2 ? enabled : this.threshold3Enabled,
                this.resetMargin, this.actionBar, this.sound, this.hud, this.hudMode,
                this.showPercentage, this.showSlotCount, this.failedInsertionWarning);
    }

    public CapacityWarningUpgradeData withResetMargin(int resetMargin) {
        return new CapacityWarningUpgradeData(this.enabled, this.calculationMode, this.threshold1, this.threshold2, this.threshold3,
                this.threshold1Enabled, this.threshold2Enabled, this.threshold3Enabled, resetMargin, this.actionBar,
                this.sound, this.hud, this.hudMode, this.showPercentage, this.showSlotCount, this.failedInsertionWarning);
    }

    public CapacityWarningUpgradeData withActionBar(boolean actionBar) {
        return new CapacityWarningUpgradeData(this.enabled, this.calculationMode, this.threshold1, this.threshold2, this.threshold3,
                this.threshold1Enabled, this.threshold2Enabled, this.threshold3Enabled, this.resetMargin, actionBar,
                this.sound, this.hud, this.hudMode, this.showPercentage, this.showSlotCount, this.failedInsertionWarning);
    }

    public CapacityWarningUpgradeData withSound(boolean sound) {
        return new CapacityWarningUpgradeData(this.enabled, this.calculationMode, this.threshold1, this.threshold2, this.threshold3,
                this.threshold1Enabled, this.threshold2Enabled, this.threshold3Enabled, this.resetMargin, this.actionBar,
                sound, this.hud, this.hudMode, this.showPercentage, this.showSlotCount, this.failedInsertionWarning);
    }

    public CapacityWarningUpgradeData withHud(boolean hud) {
        return new CapacityWarningUpgradeData(this.enabled, this.calculationMode, this.threshold1, this.threshold2, this.threshold3,
                this.threshold1Enabled, this.threshold2Enabled, this.threshold3Enabled, this.resetMargin, this.actionBar,
                this.sound, hud, this.hudMode, this.showPercentage, this.showSlotCount, this.failedInsertionWarning);
    }

    public CapacityWarningUpgradeData withHudMode(CapacityWarningHudMode hudMode) {
        return new CapacityWarningUpgradeData(this.enabled, this.calculationMode, this.threshold1, this.threshold2, this.threshold3,
                this.threshold1Enabled, this.threshold2Enabled, this.threshold3Enabled, this.resetMargin, this.actionBar,
                this.sound, this.hud, hudMode, this.showPercentage, this.showSlotCount, this.failedInsertionWarning);
    }

    public CapacityWarningUpgradeData withShowPercentage(boolean showPercentage) {
        return new CapacityWarningUpgradeData(this.enabled, this.calculationMode, this.threshold1, this.threshold2, this.threshold3,
                this.threshold1Enabled, this.threshold2Enabled, this.threshold3Enabled, this.resetMargin, this.actionBar,
                this.sound, this.hud, this.hudMode, showPercentage, this.showSlotCount, this.failedInsertionWarning);
    }

    public CapacityWarningUpgradeData withShowSlotCount(boolean showSlotCount) {
        return new CapacityWarningUpgradeData(this.enabled, this.calculationMode, this.threshold1, this.threshold2, this.threshold3,
                this.threshold1Enabled, this.threshold2Enabled, this.threshold3Enabled, this.resetMargin, this.actionBar,
                this.sound, this.hud, this.hudMode, this.showPercentage, showSlotCount, this.failedInsertionWarning);
    }

    public CapacityWarningUpgradeData withFailedInsertionWarning(boolean failedInsertionWarning) {
        return new CapacityWarningUpgradeData(this.enabled, this.calculationMode, this.threshold1, this.threshold2, this.threshold3,
                this.threshold1Enabled, this.threshold2Enabled, this.threshold3Enabled, this.resetMargin, this.actionBar,
                this.sound, this.hud, this.hudMode, this.showPercentage, this.showSlotCount, failedInsertionWarning);
    }

    private static int clampThreshold(int value) {
        return Mth.clamp(value, MIN_THRESHOLD, MAX_THRESHOLD);
    }
}
