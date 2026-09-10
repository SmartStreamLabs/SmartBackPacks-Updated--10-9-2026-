package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemContainerContents;

public record FilterUpgradeData(
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
    public static final FilterUpgradeData DEFAULT = new FilterUpgradeData(
            true,
            false,
            ItemContainerContents.EMPTY,
            List.of(),
            List.of(),
            MagnetFilterInputType.ITEM,
            false,
            false,
            false);

    public static final Codec<FilterUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(FilterUpgradeData::enabled),
            Codec.BOOL.optionalFieldOf("allowlist", false).forGetter(FilterUpgradeData::allowlist),
            ItemContainerContents.CODEC.optionalFieldOf("item_filters", ItemContainerContents.EMPTY).forGetter(FilterUpgradeData::itemFilters),
            Codec.STRING.listOf().optionalFieldOf("mod_filters", List.of()).forGetter(FilterUpgradeData::modFilters),
            ResourceLocation.CODEC.listOf().optionalFieldOf("tag_filters", List.of()).forGetter(FilterUpgradeData::tagFilters),
            MagnetFilterInputType.CODEC.optionalFieldOf("selected_input_type", MagnetFilterInputType.ITEM).forGetter(FilterUpgradeData::selectedInputType),
            Codec.BOOL.optionalFieldOf("match_nbt", false).forGetter(FilterUpgradeData::matchNbt),
            Codec.BOOL.optionalFieldOf("match_damage", false).forGetter(FilterUpgradeData::matchDamage),
            Codec.BOOL.optionalFieldOf("block_modded_items", false).forGetter(FilterUpgradeData::blockModdedItems)
    ).apply(instance, FilterUpgradeData::new));

    public FilterUpgradeData {
        modFilters = List.copyOf(modFilters);
        tagFilters = List.copyOf(tagFilters);
    }

    public boolean hasAnyFilters() {
        return ItemContainerContentsHelper.nonEmptyStream(this.itemFilters).findAny().isPresent()
                || !this.modFilters.isEmpty()
                || !this.tagFilters.isEmpty();
    }
}

