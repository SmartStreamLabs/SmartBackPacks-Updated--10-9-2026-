package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.UUIDUtil;

public record BackpackLinkData(
        UUID anchorId,
        UUID ownerId,
        String name,
        boolean active,
        BackpackLinkVisibility visibility,
        int revision,
        List<UUID> links) {
    public static final UUID EMPTY_UUID = new UUID(0L, 0L);
    public static final int MAX_NAME_LENGTH = 32;
    public static final BackpackLinkData EMPTY = new BackpackLinkData(
            EMPTY_UUID,
            EMPTY_UUID,
            "",
            false,
            BackpackLinkVisibility.PRIVATE,
            0,
            List.of());

    public static final Codec<BackpackLinkData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("anchor_id", EMPTY_UUID).forGetter(BackpackLinkData::anchorId),
            UUIDUtil.CODEC.optionalFieldOf("owner_id", EMPTY_UUID).forGetter(BackpackLinkData::ownerId),
            Codec.STRING.optionalFieldOf("name", "").forGetter(BackpackLinkData::name),
            Codec.BOOL.optionalFieldOf("active", false).forGetter(BackpackLinkData::active),
            BackpackLinkVisibility.CODEC.optionalFieldOf("visibility", BackpackLinkVisibility.PRIVATE).forGetter(BackpackLinkData::visibility),
            Codec.INT.optionalFieldOf("revision", 0).forGetter(BackpackLinkData::revision),
            UUIDUtil.CODEC.listOf().optionalFieldOf("links", List.of()).forGetter(BackpackLinkData::links)
    ).apply(instance, BackpackLinkData::new));

    public BackpackLinkData {
        name = sanitizeName(name);
        revision = Math.max(0, revision);
        links = List.copyOf(links.stream()
                .filter(id -> !EMPTY_UUID.equals(id))
                .distinct()
                .limit(64)
                .toList());
    }

    public boolean hasAnchor() {
        return !EMPTY_UUID.equals(this.anchorId);
    }

    public boolean hasOwner() {
        return !EMPTY_UUID.equals(this.ownerId);
    }

    public BackpackLinkData withIdentity(UUID anchorId, UUID ownerId, String name) {
        return new BackpackLinkData(anchorId, ownerId, name, this.active, this.visibility, this.revision + 1, this.links);
    }

    public BackpackLinkData withAnchorId(UUID anchorId) {
        return new BackpackLinkData(anchorId, this.ownerId, this.name, false, this.visibility, this.revision + 1, this.links);
    }

    public BackpackLinkData withActive(boolean active) {
        return new BackpackLinkData(this.anchorId, this.ownerId, this.name, active, this.visibility, this.revision + 1, this.links);
    }

    public BackpackLinkData withName(String name) {
        return new BackpackLinkData(this.anchorId, this.ownerId, name, this.active, this.visibility, this.revision + 1, this.links);
    }

    public BackpackLinkData withVisibility(BackpackLinkVisibility visibility) {
        return new BackpackLinkData(this.anchorId, this.ownerId, this.name,
                visibility != BackpackLinkVisibility.DISABLED && this.active, visibility, this.revision + 1, this.links);
    }

    public BackpackLinkData withLinks(List<UUID> links) {
        return new BackpackLinkData(this.anchorId, this.ownerId, this.name, this.active, this.visibility, this.revision + 1, links);
    }

    public static String sanitizeName(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        value.codePoints()
                .filter(codePoint -> !Character.isISOControl(codePoint))
                .limit(MAX_NAME_LENGTH)
                .forEach(result::appendCodePoint);
        return result.toString().trim();
    }
}
