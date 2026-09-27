package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.component.ItemContainerContents;

public record JukeboxUpgradeData(
        ItemContainerContents items,
        boolean playing) {
    public static final int SLOT_COUNT = 1;
    public static final JukeboxUpgradeData DEFAULT = new JukeboxUpgradeData(ItemContainerContents.EMPTY, false);

    public static final Codec<JukeboxUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemContainerContents.CODEC.optionalFieldOf("items", ItemContainerContents.EMPTY).forGetter(JukeboxUpgradeData::items),
            Codec.BOOL.optionalFieldOf("playing", false).forGetter(JukeboxUpgradeData::playing)
    ).apply(instance, JukeboxUpgradeData::new));
}
