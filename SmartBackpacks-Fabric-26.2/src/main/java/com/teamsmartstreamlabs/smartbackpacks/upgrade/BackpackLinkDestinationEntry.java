package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record BackpackLinkDestinationEntry(
        UUID anchorId,
        String name,
        String dimension,
        BlockPos position,
        boolean active,
        boolean placed,
        boolean sameDimension,
        int distanceBlocks,
        int costLevels,
        int cooldownSeconds,
        String statusKey) {
    public static void write(RegistryFriendlyByteBuf buffer, BackpackLinkDestinationEntry entry) {
        buffer.writeUUID(entry.anchorId);
        buffer.writeUtf(entry.name, 64);
        buffer.writeUtf(entry.dimension, 96);
        buffer.writeBlockPos(entry.position);
        buffer.writeBoolean(entry.active);
        buffer.writeBoolean(entry.placed);
        buffer.writeBoolean(entry.sameDimension);
        buffer.writeVarInt(entry.distanceBlocks);
        buffer.writeVarInt(entry.costLevels);
        buffer.writeVarInt(entry.cooldownSeconds);
        buffer.writeUtf(entry.statusKey, 128);
    }

    public static BackpackLinkDestinationEntry read(RegistryFriendlyByteBuf buffer) {
        return new BackpackLinkDestinationEntry(
                buffer.readUUID(),
                buffer.readUtf(64),
                buffer.readUtf(96),
                buffer.readBlockPos(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readUtf(128));
    }

    public boolean canTeleport() {
        return "message.smartbackpacks.backpack_link.ready".equals(this.statusKey);
    }
}
