package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.component.ItemContainerContents;

public record AutoToolUpgradeData(
        boolean enabled,
        boolean allowlist,
        ItemContainerContents itemFilters,
        List<String> modFilters,
        List<ResourceLocation> tagFilters,
        MagnetFilterInputType selectedInputType,
        boolean matchNbt,
        boolean matchDamage,
        boolean blockModdedItems) {
    public static final int FILTER_SLOT_COUNT = 16;
    public static final AutoToolUpgradeData DEFAULT = new AutoToolUpgradeData(
            true,
            false,
            ItemContainerContents.EMPTY,
            List.of(),
            List.of(),
            MagnetFilterInputType.ITEM,
            false,
            false,
            false);
    public static final Codec<AutoToolUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(AutoToolUpgradeData::enabled),
            Codec.BOOL.optionalFieldOf("allowlist", false).forGetter(AutoToolUpgradeData::allowlist),
            ItemContainerContents.CODEC.optionalFieldOf("item_filters", ItemContainerContents.EMPTY).forGetter(AutoToolUpgradeData::itemFilters),
            Codec.STRING.listOf().optionalFieldOf("mod_filters", List.of()).forGetter(AutoToolUpgradeData::modFilters),
            ResourceLocation.CODEC.listOf().optionalFieldOf("tag_filters", List.of()).forGetter(AutoToolUpgradeData::tagFilters),
            MagnetFilterInputType.CODEC.optionalFieldOf("selected_input_type", MagnetFilterInputType.ITEM).forGetter(AutoToolUpgradeData::selectedInputType),
            Codec.BOOL.optionalFieldOf("match_nbt", false).forGetter(AutoToolUpgradeData::matchNbt),
            Codec.BOOL.optionalFieldOf("match_damage", false).forGetter(AutoToolUpgradeData::matchDamage),
            Codec.BOOL.optionalFieldOf("block_modded_items", false).forGetter(AutoToolUpgradeData::blockModdedItems)
    ).apply(instance, AutoToolUpgradeData::new));

    public AutoToolUpgradeData {
        modFilters = List.copyOf(modFilters);
        tagFilters = List.copyOf(tagFilters);
    }

    public boolean hasAnyFilters() {
        return ItemContainerContentsHelper.nonEmptyStream(this.itemFilters).findAny().isPresent()
                || !this.modFilters.isEmpty()
                || !this.tagFilters.isEmpty();
    }
}

