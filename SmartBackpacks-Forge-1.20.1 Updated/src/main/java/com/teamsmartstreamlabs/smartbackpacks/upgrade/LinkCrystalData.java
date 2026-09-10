package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.UUIDUtil;

public record LinkCrystalData(UUID anchorId, String anchorName) {
    public static final LinkCrystalData EMPTY = new LinkCrystalData(BackpackLinkData.EMPTY_UUID, "");
    public static final Codec<LinkCrystalData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("anchor_id", BackpackLinkData.EMPTY_UUID).forGetter(LinkCrystalData::anchorId),
            Codec.STRING.optionalFieldOf("anchor_name", "").forGetter(LinkCrystalData::anchorName)
    ).apply(instance, LinkCrystalData::new));

    public boolean isBound() {
        return !BackpackLinkData.EMPTY_UUID.equals(this.anchorId);
    }
}
