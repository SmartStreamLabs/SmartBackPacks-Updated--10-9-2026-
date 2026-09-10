package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public record PickupUpgradeData(
        boolean enabled,
        boolean allowlist,
        ItemContainerContents itemFilters,
        List<String> modFilters,
        List<Identifier> tagFilters,
        boolean blockModdedItems) {
    public static final PickupUpgradeData DEFAULT = new PickupUpgradeData(
            true,
            false,
            ItemContainerContents.EMPTY,
            List.of(),
            List.of(),
            false);
    public static final Codec<PickupUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(PickupUpgradeData::enabled),
            Codec.BOOL.optionalFieldOf("allowlist", false).forGetter(PickupUpgradeData::allowlist),
            ItemContainerContents.CODEC.optionalFieldOf("item_filters", ItemContainerContents.EMPTY).forGetter(PickupUpgradeData::itemFilters),
            Codec.STRING.listOf().optionalFieldOf("mod_filters", List.of()).forGetter(PickupUpgradeData::modFilters),
            Identifier.CODEC.listOf().optionalFieldOf("tag_filters", List.of()).forGetter(PickupUpgradeData::tagFilters),
            Codec.BOOL.optionalFieldOf("block_modded_items", false).forGetter(PickupUpgradeData::blockModdedItems)
    ).apply(instance, PickupUpgradeData::new));

    public PickupUpgradeData {
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

