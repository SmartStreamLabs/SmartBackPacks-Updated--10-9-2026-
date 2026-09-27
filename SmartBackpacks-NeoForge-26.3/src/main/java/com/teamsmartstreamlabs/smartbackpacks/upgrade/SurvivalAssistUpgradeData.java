package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.ItemContainerContents;

public record SurvivalAssistUpgradeData(
        ItemContainerContents itemFilters,
        List<Identifier> effectFilters) {
    public static final int FILTER_SLOT_COUNT = 8;
    public static final SurvivalAssistUpgradeData DEFAULT = new SurvivalAssistUpgradeData(ItemContainerContents.EMPTY, List.of());
    public static final Codec<SurvivalAssistUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemContainerContents.CODEC.optionalFieldOf("item_filters", ItemContainerContents.EMPTY).forGetter(SurvivalAssistUpgradeData::itemFilters),
            Identifier.CODEC.listOf().optionalFieldOf("effect_filters", List.of()).forGetter(SurvivalAssistUpgradeData::effectFilters)
    ).apply(instance, SurvivalAssistUpgradeData::new));

    public SurvivalAssistUpgradeData {
        effectFilters = List.copyOf(effectFilters);
    }

    public boolean hasAnyFilters() {
        return ItemContainerContentsHelper.nonEmptyStream(this.itemFilters).findAny().isPresent()
                || !this.effectFilters.isEmpty();
    }
}

