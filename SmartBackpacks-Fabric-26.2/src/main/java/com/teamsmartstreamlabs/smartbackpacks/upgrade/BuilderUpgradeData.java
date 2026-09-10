package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public record BuilderUpgradeData(
        boolean enabled,
        BuilderRefillMode refillMode,
        BuilderMatchMode matchMode,
        BuilderFilterMode filterMode,
        boolean mainHandEnabled,
        boolean offhandEnabled,
        int threshold,
        boolean scaffoldingEnabled,
        boolean moddedBlocksEnabled,
        boolean dangerousProtectionEnabled,
        boolean feedbackEnabled,
        ItemContainerContents filterItems) {
    public static final int FILTER_SLOT_COUNT = 12;
    public static final BuilderUpgradeData DEFAULT = new BuilderUpgradeData(
            true,
            BuilderRefillMode.WHEN_EMPTY,
            BuilderMatchMode.EXACT,
            BuilderFilterMode.ALLOW_ALL,
            true,
            false,
            8,
            true,
            true,
            true,
            true,
            ItemContainerContents.EMPTY);

    public static final Codec<BuilderUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(BuilderUpgradeData::enabled),
            BuilderRefillMode.CODEC.optionalFieldOf("refill_mode", BuilderRefillMode.WHEN_EMPTY).forGetter(BuilderUpgradeData::refillMode),
            BuilderMatchMode.CODEC.optionalFieldOf("match_mode", BuilderMatchMode.EXACT).forGetter(BuilderUpgradeData::matchMode),
            BuilderFilterMode.CODEC.optionalFieldOf("filter_mode", BuilderFilterMode.ALLOW_ALL).forGetter(BuilderUpgradeData::filterMode),
            Codec.BOOL.optionalFieldOf("main_hand_enabled", true).forGetter(BuilderUpgradeData::mainHandEnabled),
            Codec.BOOL.optionalFieldOf("offhand_enabled", false).forGetter(BuilderUpgradeData::offhandEnabled),
            Codec.INT.optionalFieldOf("threshold", 8).forGetter(BuilderUpgradeData::threshold),
            Codec.BOOL.optionalFieldOf("scaffolding_enabled", true).forGetter(BuilderUpgradeData::scaffoldingEnabled),
            Codec.BOOL.optionalFieldOf("modded_blocks_enabled", true).forGetter(BuilderUpgradeData::moddedBlocksEnabled),
            Codec.BOOL.optionalFieldOf("dangerous_protection_enabled", true).forGetter(BuilderUpgradeData::dangerousProtectionEnabled),
            Codec.BOOL.optionalFieldOf("feedback_enabled", true).forGetter(BuilderUpgradeData::feedbackEnabled),
            ItemContainerContents.CODEC.optionalFieldOf("filter_items", ItemContainerContents.EMPTY).forGetter(BuilderUpgradeData::filterItems)
    ).apply(instance, BuilderUpgradeData::new));

    public BuilderUpgradeData {
        threshold = Math.max(1, Math.min(64, threshold));
    }

    public NonNullList<ItemStack> loadFilterItems() {
        NonNullList<ItemStack> stacks = NonNullList.withSize(FILTER_SLOT_COUNT, ItemStack.EMPTY);
        this.filterItems.copyInto(stacks);
        return stacks;
    }

    public BuilderUpgradeData withEnabled(boolean enabled) {
        return new BuilderUpgradeData(enabled, this.refillMode, this.matchMode, this.filterMode, this.mainHandEnabled,
                this.offhandEnabled, this.threshold, this.scaffoldingEnabled, this.moddedBlocksEnabled,
                this.dangerousProtectionEnabled, this.feedbackEnabled, this.filterItems);
    }

    public BuilderUpgradeData withRefillMode(BuilderRefillMode refillMode) {
        return new BuilderUpgradeData(this.enabled, refillMode, this.matchMode, this.filterMode, this.mainHandEnabled,
                this.offhandEnabled, this.threshold, this.scaffoldingEnabled, this.moddedBlocksEnabled,
                this.dangerousProtectionEnabled, this.feedbackEnabled, this.filterItems);
    }

    public BuilderUpgradeData withMatchMode(BuilderMatchMode matchMode) {
        return new BuilderUpgradeData(this.enabled, this.refillMode, matchMode, this.filterMode, this.mainHandEnabled,
                this.offhandEnabled, this.threshold, this.scaffoldingEnabled, this.moddedBlocksEnabled,
                this.dangerousProtectionEnabled, this.feedbackEnabled, this.filterItems);
    }

    public BuilderUpgradeData withFilterMode(BuilderFilterMode filterMode) {
        return new BuilderUpgradeData(this.enabled, this.refillMode, this.matchMode, filterMode, this.mainHandEnabled,
                this.offhandEnabled, this.threshold, this.scaffoldingEnabled, this.moddedBlocksEnabled,
                this.dangerousProtectionEnabled, this.feedbackEnabled, this.filterItems);
    }

    public BuilderUpgradeData withHandToggles(boolean mainHandEnabled, boolean offhandEnabled) {
        return new BuilderUpgradeData(this.enabled, this.refillMode, this.matchMode, this.filterMode, mainHandEnabled,
                offhandEnabled, this.threshold, this.scaffoldingEnabled, this.moddedBlocksEnabled,
                this.dangerousProtectionEnabled, this.feedbackEnabled, this.filterItems);
    }

    public BuilderUpgradeData withThreshold(int threshold) {
        return new BuilderUpgradeData(this.enabled, this.refillMode, this.matchMode, this.filterMode, this.mainHandEnabled,
                this.offhandEnabled, threshold, this.scaffoldingEnabled, this.moddedBlocksEnabled,
                this.dangerousProtectionEnabled, this.feedbackEnabled, this.filterItems);
    }

    public BuilderUpgradeData withOptionToggles(boolean scaffoldingEnabled, boolean moddedBlocksEnabled,
            boolean dangerousProtectionEnabled, boolean feedbackEnabled) {
        return new BuilderUpgradeData(this.enabled, this.refillMode, this.matchMode, this.filterMode, this.mainHandEnabled,
                this.offhandEnabled, this.threshold, scaffoldingEnabled, moddedBlocksEnabled,
                dangerousProtectionEnabled, feedbackEnabled, this.filterItems);
    }

    public BuilderUpgradeData withFilterItems(NonNullList<ItemStack> filterItems) {
        return new BuilderUpgradeData(this.enabled, this.refillMode, this.matchMode, this.filterMode, this.mainHandEnabled,
                this.offhandEnabled, this.threshold, this.scaffoldingEnabled, this.moddedBlocksEnabled,
                this.dangerousProtectionEnabled, this.feedbackEnabled, ItemContainerContents.fromItems(filterItems));
    }
}
