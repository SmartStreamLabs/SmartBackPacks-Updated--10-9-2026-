package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemContainerContents;

public record VoidUpgradeData(
        boolean enabled,
        boolean allowlist,
        ItemContainerContents itemFilters,
        List<String> modFilters,
        List<ResourceLocation> tagFilters,
        MagnetFilterInputType selectedInputType,
        boolean matchNbt,
        boolean matchDamage,
        boolean blockModdedItems,
        boolean onlyWhenFull) {
    public static final int FILTER_SLOT_COUNT = 16;
    public static final VoidUpgradeData DEFAULT = new VoidUpgradeData(
            true,
            false,
            ItemContainerContents.EMPTY,
            List.of(),
            List.of(),
            MagnetFilterInputType.ITEM,
            false,
            false,
            false,
            false);
    public static final Codec<VoidUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(VoidUpgradeData::enabled),
            Codec.BOOL.optionalFieldOf("allowlist", false).forGetter(VoidUpgradeData::allowlist),
            ItemContainerContents.CODEC.optionalFieldOf("item_filters", ItemContainerContents.EMPTY).forGetter(VoidUpgradeData::itemFilters),
            Codec.STRING.listOf().optionalFieldOf("mod_filters", List.of()).forGetter(VoidUpgradeData::modFilters),
            ResourceLocation.CODEC.listOf().optionalFieldOf("tag_filters", List.of()).forGetter(VoidUpgradeData::tagFilters),
            MagnetFilterInputType.CODEC.optionalFieldOf("selected_input_type", MagnetFilterInputType.ITEM).forGetter(VoidUpgradeData::selectedInputType),
            Codec.BOOL.optionalFieldOf("match_nbt", false).forGetter(VoidUpgradeData::matchNbt),
            Codec.BOOL.optionalFieldOf("match_damage", false).forGetter(VoidUpgradeData::matchDamage),
            Codec.BOOL.optionalFieldOf("block_modded_items", false).forGetter(VoidUpgradeData::blockModdedItems),
            Codec.BOOL.optionalFieldOf("only_when_full", false).forGetter(VoidUpgradeData::onlyWhenFull)
    ).apply(instance, VoidUpgradeData::new));

    public VoidUpgradeData {
        modFilters = List.copyOf(modFilters);
        tagFilters = List.copyOf(tagFilters);
    }

    public boolean hasAnyFilters() {
        return ItemContainerContentsHelper.nonEmptyStream(this.itemFilters).findAny().isPresent()
                || !this.modFilters.isEmpty()
                || !this.tagFilters.isEmpty();
    }
}

