package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.component.ItemContainerContents;

public record TorchPlacerUpgradeData(
        boolean enabled,
        int lightThreshold,
        int minimumDistance,
        TorchPlacerMode placementMode,
        BuilderMatchMode matchMode,
        BuilderFilterMode filterMode,
        boolean floorPlacement,
        boolean wallPlacement,
        boolean ceilingPlacement,
        boolean moddedLightSources,
        boolean placeWhileSprinting,
        boolean placeWhileSneaking,
        boolean placeWhileStandingStill,
        boolean placeInWater,
        boolean placeInLava,
        ItemContainerContents filterItems) {
    public static final int FILTER_SLOT_COUNT = 9;
    public static final int MIN_LIGHT_THRESHOLD = 0;
    public static final int MAX_LIGHT_THRESHOLD = 15;
    public static final int MIN_DISTANCE = 1;
    public static final int MAX_DISTANCE = 64;

    public static final TorchPlacerUpgradeData DEFAULT = new TorchPlacerUpgradeData(
            true,
            7,
            8,
            TorchPlacerMode.BEHIND_PLAYER,
            BuilderMatchMode.EXACT,
            BuilderFilterMode.ALLOW_ALL,
            true,
            true,
            false,
            true,
            true,
            true,
            true,
            false,
            false,
            ItemContainerContents.EMPTY);

    public static final Codec<TorchPlacerUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(TorchPlacerUpgradeData::enabled),
            Codec.INT.optionalFieldOf("light_threshold", 7).forGetter(TorchPlacerUpgradeData::lightThreshold),
            Codec.INT.optionalFieldOf("minimum_distance", 8).forGetter(TorchPlacerUpgradeData::minimumDistance),
            TorchPlacerMode.CODEC.optionalFieldOf("placement_mode", TorchPlacerMode.BEHIND_PLAYER).forGetter(TorchPlacerUpgradeData::placementMode),
            BuilderMatchMode.CODEC.optionalFieldOf("match_mode", BuilderMatchMode.EXACT).forGetter(TorchPlacerUpgradeData::matchMode),
            BuilderFilterMode.CODEC.optionalFieldOf("filter_mode", BuilderFilterMode.ALLOW_ALL).forGetter(TorchPlacerUpgradeData::filterMode),
            Codec.BOOL.optionalFieldOf("floor_placement", true).forGetter(TorchPlacerUpgradeData::floorPlacement),
            Codec.BOOL.optionalFieldOf("wall_placement", true).forGetter(TorchPlacerUpgradeData::wallPlacement),
            Codec.BOOL.optionalFieldOf("ceiling_placement", false).forGetter(TorchPlacerUpgradeData::ceilingPlacement),
            Codec.BOOL.optionalFieldOf("modded_light_sources", true).forGetter(TorchPlacerUpgradeData::moddedLightSources),
            Codec.BOOL.optionalFieldOf("place_while_sprinting", true).forGetter(TorchPlacerUpgradeData::placeWhileSprinting),
            Codec.BOOL.optionalFieldOf("place_while_sneaking", true).forGetter(TorchPlacerUpgradeData::placeWhileSneaking),
            Codec.BOOL.optionalFieldOf("place_while_standing_still", true).forGetter(TorchPlacerUpgradeData::placeWhileStandingStill),
            Codec.BOOL.optionalFieldOf("place_in_water", false).forGetter(TorchPlacerUpgradeData::placeInWater),
            Codec.BOOL.optionalFieldOf("place_in_lava", false).forGetter(TorchPlacerUpgradeData::placeInLava),
            ItemContainerContents.CODEC.optionalFieldOf("filter_items", ItemContainerContents.EMPTY).forGetter(TorchPlacerUpgradeData::filterItems)
    ).apply(instance, TorchPlacerUpgradeData::new));

    public TorchPlacerUpgradeData {
        lightThreshold = Math.max(MIN_LIGHT_THRESHOLD, Math.min(MAX_LIGHT_THRESHOLD, lightThreshold));
        minimumDistance = Math.max(MIN_DISTANCE, Math.min(MAX_DISTANCE, minimumDistance));
    }

    public NonNullList<ItemStack> loadFilterItems() {
        NonNullList<ItemStack> stacks = NonNullList.withSize(FILTER_SLOT_COUNT, ItemStack.EMPTY);
        this.filterItems.copyInto(stacks);
        return stacks;
    }

    public TorchPlacerUpgradeData withEnabled(boolean enabled) {
        return new TorchPlacerUpgradeData(enabled, this.lightThreshold, this.minimumDistance, this.placementMode,
                this.matchMode, this.filterMode, this.floorPlacement, this.wallPlacement,
                this.ceilingPlacement, this.moddedLightSources, this.placeWhileSprinting, this.placeWhileSneaking,
                this.placeWhileStandingStill, this.placeInWater, this.placeInLava, this.filterItems);
    }

    public TorchPlacerUpgradeData withLightThreshold(int lightThreshold) {
        return new TorchPlacerUpgradeData(this.enabled, lightThreshold, this.minimumDistance, this.placementMode,
                this.matchMode, this.filterMode, this.floorPlacement, this.wallPlacement,
                this.ceilingPlacement, this.moddedLightSources, this.placeWhileSprinting, this.placeWhileSneaking,
                this.placeWhileStandingStill, this.placeInWater, this.placeInLava, this.filterItems);
    }

    public TorchPlacerUpgradeData withMinimumDistance(int minimumDistance) {
        return new TorchPlacerUpgradeData(this.enabled, this.lightThreshold, minimumDistance, this.placementMode,
                this.matchMode, this.filterMode, this.floorPlacement, this.wallPlacement,
                this.ceilingPlacement, this.moddedLightSources, this.placeWhileSprinting, this.placeWhileSneaking,
                this.placeWhileStandingStill, this.placeInWater, this.placeInLava, this.filterItems);
    }

    public TorchPlacerUpgradeData withPlacementMode(TorchPlacerMode placementMode) {
        return new TorchPlacerUpgradeData(this.enabled, this.lightThreshold, this.minimumDistance, placementMode,
                this.matchMode, this.filterMode, this.floorPlacement, this.wallPlacement,
                this.ceilingPlacement, this.moddedLightSources, this.placeWhileSprinting, this.placeWhileSneaking,
                this.placeWhileStandingStill, this.placeInWater, this.placeInLava, this.filterItems);
    }

    public TorchPlacerUpgradeData withMatchMode(BuilderMatchMode matchMode) {
        return new TorchPlacerUpgradeData(this.enabled, this.lightThreshold, this.minimumDistance, this.placementMode,
                matchMode, this.filterMode, this.floorPlacement, this.wallPlacement,
                this.ceilingPlacement, this.moddedLightSources, this.placeWhileSprinting, this.placeWhileSneaking,
                this.placeWhileStandingStill, this.placeInWater, this.placeInLava, this.filterItems);
    }

    public TorchPlacerUpgradeData withFilterMode(BuilderFilterMode filterMode) {
        return new TorchPlacerUpgradeData(this.enabled, this.lightThreshold, this.minimumDistance, this.placementMode,
                this.matchMode, filterMode, this.floorPlacement, this.wallPlacement,
                this.ceilingPlacement, this.moddedLightSources, this.placeWhileSprinting, this.placeWhileSneaking,
                this.placeWhileStandingStill, this.placeInWater, this.placeInLava, this.filterItems);
    }

    public TorchPlacerUpgradeData withPlacementToggles(boolean floorPlacement, boolean wallPlacement, boolean ceilingPlacement) {
        return new TorchPlacerUpgradeData(this.enabled, this.lightThreshold, this.minimumDistance, this.placementMode,
                this.matchMode, this.filterMode, floorPlacement, wallPlacement, ceilingPlacement,
                this.moddedLightSources, this.placeWhileSprinting, this.placeWhileSneaking,
                this.placeWhileStandingStill, this.placeInWater, this.placeInLava, this.filterItems);
    }

    public TorchPlacerUpgradeData withMovementToggles(boolean moddedLightSources, boolean placeWhileSprinting,
            boolean placeWhileSneaking, boolean placeWhileStandingStill) {
        return new TorchPlacerUpgradeData(this.enabled, this.lightThreshold, this.minimumDistance, this.placementMode,
                this.matchMode, this.filterMode, this.floorPlacement, this.wallPlacement,
                this.ceilingPlacement, moddedLightSources, placeWhileSprinting, placeWhileSneaking,
                placeWhileStandingStill, this.placeInWater, this.placeInLava, this.filterItems);
    }

    public TorchPlacerUpgradeData withFluidToggles(boolean placeInWater, boolean placeInLava) {
        return new TorchPlacerUpgradeData(this.enabled, this.lightThreshold, this.minimumDistance, this.placementMode,
                this.matchMode, this.filterMode, this.floorPlacement, this.wallPlacement,
                this.ceilingPlacement, this.moddedLightSources, this.placeWhileSprinting, this.placeWhileSneaking,
                this.placeWhileStandingStill, placeInWater, placeInLava, this.filterItems);
    }

    public TorchPlacerUpgradeData withFilterItems(NonNullList<ItemStack> filterItems) {
        return new TorchPlacerUpgradeData(this.enabled, this.lightThreshold, this.minimumDistance, this.placementMode,
                this.matchMode, this.filterMode, this.floorPlacement, this.wallPlacement,
                this.ceilingPlacement, this.moddedLightSources, this.placeWhileSprinting, this.placeWhileSneaking,
                this.placeWhileStandingStill, this.placeInWater, this.placeInLava, ItemContainerContents.fromItems(filterItems));
    }
}
