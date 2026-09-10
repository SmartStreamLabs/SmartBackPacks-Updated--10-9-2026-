package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public record MagnetUpgradeData(
        boolean enabled,
        boolean allowlist,
        ItemContainerContents itemFilters,
        List<String> modFilters,
        List<ResourceLocation> tagFilters,
        MagnetFilterInputType selectedInputType,
        boolean matchNbt,
        boolean matchDamage,
        boolean matchBackpackContentsOnly,
        boolean blockModdedItems) {
    public static final int REGULAR_FILTER_SLOT_COUNT = 27;
    public static final int ADVANCED_FILTER_SLOT_COUNT = 16;
    public static final MagnetUpgradeData DEFAULT = new MagnetUpgradeData(
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
    public static final Codec<MagnetUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(MagnetUpgradeData::enabled),
            Codec.BOOL.optionalFieldOf("allowlist", false).forGetter(MagnetUpgradeData::allowlist),
            ItemContainerContents.CODEC.optionalFieldOf("item_filters", ItemContainerContents.EMPTY).forGetter(MagnetUpgradeData::itemFilters),
            Codec.STRING.listOf().optionalFieldOf("mod_filters", List.of()).forGetter(MagnetUpgradeData::modFilters),
            ResourceLocation.CODEC.listOf().optionalFieldOf("tag_filters", List.of()).forGetter(MagnetUpgradeData::tagFilters),
            MagnetFilterInputType.CODEC.optionalFieldOf("selected_input_type", MagnetFilterInputType.ITEM).forGetter(MagnetUpgradeData::selectedInputType),
            Codec.BOOL.optionalFieldOf("match_nbt", false).forGetter(MagnetUpgradeData::matchNbt),
            Codec.BOOL.optionalFieldOf("match_damage", false).forGetter(MagnetUpgradeData::matchDamage),
            Codec.BOOL.optionalFieldOf("match_backpack_contents_only", false).forGetter(MagnetUpgradeData::matchBackpackContentsOnly),
            Codec.BOOL.optionalFieldOf("block_modded_items", false).forGetter(MagnetUpgradeData::blockModdedItems)
    ).apply(instance, MagnetUpgradeData::new));

    public MagnetUpgradeData {
        modFilters = List.copyOf(modFilters);
        tagFilters = List.copyOf(tagFilters);
    }

    public boolean hasAnyFilters() {
        return ItemContainerContentsHelper.nonEmptyStream(this.itemFilters).findAny().isPresent()
                || !this.modFilters.isEmpty()
                || !this.tagFilters.isEmpty();
    }

    public boolean matchesItemId(ItemStack stack) {
        return ItemContainerContentsHelper.nonEmptyStream(this.itemFilters)
                .anyMatch(filterStack -> ItemStack.isSameItem(filterStack, stack));
    }
}

