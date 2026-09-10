package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record BackpackLinkAnchor(
        UUID anchorId,
        UUID ownerId,
        ResourceKey<Level> dimension,
        BlockPos position,
        String name,
        BackpackLinkVisibility visibility,
        boolean active,
        boolean placed,
        int revision,
        long lastValidatedGameTime,
        List<UUID> links) {
    public static final Codec<BackpackLinkAnchor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("anchor_id").forGetter(BackpackLinkAnchor::anchorId),
            UUIDUtil.CODEC.optionalFieldOf("owner_id", BackpackLinkData.EMPTY_UUID).forGetter(BackpackLinkAnchor::ownerId),
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(BackpackLinkAnchor::dimension),
            BlockPos.CODEC.fieldOf("position").forGetter(BackpackLinkAnchor::position),
            Codec.STRING.optionalFieldOf("name", "Backpack Anchor").forGetter(BackpackLinkAnchor::name),
            BackpackLinkVisibility.CODEC.optionalFieldOf("visibility", BackpackLinkVisibility.PRIVATE).forGetter(BackpackLinkAnchor::visibility),
            Codec.BOOL.optionalFieldOf("active", false).forGetter(BackpackLinkAnchor::active),
            Codec.BOOL.optionalFieldOf("placed", false).forGetter(BackpackLinkAnchor::placed),
            Codec.INT.optionalFieldOf("revision", 0).forGetter(BackpackLinkAnchor::revision),
            Codec.LONG.optionalFieldOf("last_validated_game_time", 0L).forGetter(BackpackLinkAnchor::lastValidatedGameTime),
            UUIDUtil.CODEC.listOf().optionalFieldOf("links", List.of()).forGetter(BackpackLinkAnchor::links)
    ).apply(instance, BackpackLinkAnchor::new));

    public BackpackLinkAnchor {
        name = BackpackLinkData.sanitizeName(name);
        if (name.isBlank()) {
            name = "Backpack Anchor";
        }
        revision = Math.max(0, revision);
        lastValidatedGameTime = Math.max(0L, lastValidatedGameTime);
        links = List.copyOf(links.stream()
                .filter(id -> !BackpackLinkData.EMPTY_UUID.equals(id))
                .filter(id -> !id.equals(anchorId))
                .distinct()
                .limit(64)
                .toList());
    }

    public BackpackLinkAnchor withPlacement(ResourceKey<Level> dimension, BlockPos position, boolean active, boolean placed, long gameTime) {
        return new BackpackLinkAnchor(this.anchorId, this.ownerId, dimension, position, this.name, this.visibility,
                active && this.visibility != BackpackLinkVisibility.DISABLED, placed, this.revision, gameTime, this.links);
    }

    public BackpackLinkAnchor withSettings(String name, BackpackLinkVisibility visibility, boolean active, int revision, List<UUID> links) {
        return new BackpackLinkAnchor(this.anchorId, this.ownerId, this.dimension, this.position, name, visibility,
                active && visibility != BackpackLinkVisibility.DISABLED, this.placed, revision, this.lastValidatedGameTime, links);
    }

    public BackpackLinkAnchor withLinks(List<UUID> links) {
        return new BackpackLinkAnchor(this.anchorId, this.ownerId, this.dimension, this.position, this.name,
                this.visibility, this.active, this.placed, this.revision, this.lastValidatedGameTime, links);
    }

    public BackpackLinkAnchor offline(long gameTime) {
        return new BackpackLinkAnchor(this.anchorId, this.ownerId, this.dimension, this.position, this.name,
                this.visibility, false, false, this.revision, gameTime, this.links);
    }
}
