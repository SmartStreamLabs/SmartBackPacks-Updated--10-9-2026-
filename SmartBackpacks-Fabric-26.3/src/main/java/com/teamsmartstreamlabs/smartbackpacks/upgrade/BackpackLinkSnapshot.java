package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record BackpackLinkSnapshot(
        boolean placed,
        boolean hasUpgrade,
        boolean configEnabled,
        boolean hasAnchor,
        boolean active,
        UUID sourceAnchorId,
        String sourceName,
        String dimension,
        BlockPos position,
        BackpackLinkVisibility visibility,
        int maxDestinations,
        String statusKey,
        List<BackpackLinkDestinationEntry> destinations) {
    public static final BackpackLinkSnapshot EMPTY = new BackpackLinkSnapshot(
            false,
            false,
            true,
            false,
            false,
            BackpackLinkData.EMPTY_UUID,
            "Backpack Anchor",
            "",
            BlockPos.ZERO,
            BackpackLinkVisibility.PRIVATE,
            0,
            "message.smartbackpacks.backpack_link.not_placed",
            List.of());

    public BackpackLinkSnapshot {
        sourceName = BackpackLinkData.sanitizeName(sourceName);
        if (sourceName.isBlank()) {
            sourceName = "Backpack Anchor";
        }
        maxDestinations = Math.max(0, maxDestinations);
        destinations = List.copyOf(destinations);
    }

    public static void write(RegistryFriendlyByteBuf buffer, BackpackLinkSnapshot snapshot) {
        buffer.writeBoolean(snapshot.placed);
        buffer.writeBoolean(snapshot.hasUpgrade);
        buffer.writeBoolean(snapshot.configEnabled);
        buffer.writeBoolean(snapshot.hasAnchor);
        buffer.writeBoolean(snapshot.active);
        buffer.writeUUID(snapshot.sourceAnchorId);
        buffer.writeUtf(snapshot.sourceName, 64);
        buffer.writeUtf(snapshot.dimension, 96);
        buffer.writeBlockPos(snapshot.position);
        buffer.writeEnum(snapshot.visibility);
        buffer.writeVarInt(snapshot.maxDestinations);
        buffer.writeUtf(snapshot.statusKey, 128);
        buffer.writeVarInt(Math.min(32, snapshot.destinations.size()));
        for (BackpackLinkDestinationEntry entry : snapshot.destinations.stream().limit(32).toList()) {
            BackpackLinkDestinationEntry.write(buffer, entry);
        }
    }

    public static BackpackLinkSnapshot read(RegistryFriendlyByteBuf buffer) {
        boolean placed = buffer.readBoolean();
        boolean hasUpgrade = buffer.readBoolean();
        boolean configEnabled = buffer.readBoolean();
        boolean hasAnchor = buffer.readBoolean();
        boolean active = buffer.readBoolean();
        UUID sourceAnchorId = buffer.readUUID();
        String sourceName = buffer.readUtf(64);
        String dimension = buffer.readUtf(96);
        BlockPos position = buffer.readBlockPos();
        BackpackLinkVisibility visibility = buffer.readEnum(BackpackLinkVisibility.class);
        int maxDestinations = buffer.readVarInt();
        String statusKey = buffer.readUtf(128);
        int destinationCount = Math.max(0, Math.min(32, buffer.readVarInt()));
        List<BackpackLinkDestinationEntry> destinations = new ArrayList<>(destinationCount);
        for (int index = 0; index < destinationCount; index++) {
            destinations.add(BackpackLinkDestinationEntry.read(buffer));
        }
        return new BackpackLinkSnapshot(placed, hasUpgrade, configEnabled, hasAnchor, active, sourceAnchorId,
                sourceName, dimension, position, visibility, maxDestinations, statusKey, destinations);
    }
}
