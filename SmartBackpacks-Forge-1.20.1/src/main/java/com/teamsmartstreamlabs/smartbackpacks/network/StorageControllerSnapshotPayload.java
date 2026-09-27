package com.teamsmartstreamlabs.smartbackpacks.network;

import java.util.ArrayList;
import java.util.List;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.StorageControllerScreen;
import com.teamsmartstreamlabs.smartbackpacks.menu.StorageControllerMenu;
import com.teamsmartstreamlabs.smartbackpacks.storage.StorageNetworkSnapshot;
import com.teamsmartstreamlabs.smartbackpacks.storage.StorageNetworkSnapshot.Entry;

import net.minecraft.client.Minecraft;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.codec.StreamCodec;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StorageControllerSnapshotPayload(int containerId, StorageNetworkSnapshot snapshot)
        implements CustomPacketPayload {
    public static final Type<StorageControllerSnapshotPayload> TYPE =
            new Type<>(new ResourceLocation(SmartBackpacks.MOD_ID, "storage_controller_snapshot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StorageControllerSnapshotPayload> STREAM_CODEC = StreamCodec.of(
            StorageControllerSnapshotPayload::write,
            StorageControllerSnapshotPayload::read);

    private static void write(RegistryFriendlyByteBuf buffer, StorageControllerSnapshotPayload payload) {
        StorageNetworkSnapshot snapshot = payload.snapshot();
        buffer.writeVarInt(payload.containerId());
        buffer.writeEnum(snapshot.status());
        buffer.writeVarInt(snapshot.connectedBackpacks());
        buffer.writeVarInt(snapshot.totalSlots());
        buffer.writeVarInt(snapshot.usedSlots());
        buffer.writeBoolean(snapshot.truncated());
        buffer.writeVarInt(snapshot.entries().size());
        for (Entry entry : snapshot.entries()) {
            buffer.writeItem(entry.icon());
            buffer.writeVarLong(entry.count());
        }
    }

    private static StorageControllerSnapshotPayload read(RegistryFriendlyByteBuf buffer) {
        int containerId = buffer.readVarInt();
        StorageNetworkSnapshot.Status status = buffer.readEnum(StorageNetworkSnapshot.Status.class);
        int connectedBackpacks = buffer.readVarInt();
        int totalSlots = buffer.readVarInt();
        int usedSlots = buffer.readVarInt();
        boolean truncated = buffer.readBoolean();
        int size = Math.max(0, Math.min(buffer.readVarInt(), StorageNetworkServiceLimit.MAX_ENTRIES));
        List<Entry> entries = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            entries.add(new Entry(buffer.readItem(), buffer.readVarLong()));
        }
        return new StorageControllerSnapshotPayload(containerId,
                new StorageNetworkSnapshot(status, entries, connectedBackpacks, totalSlots, usedSlots, truncated));
    }

    @Override
    public Type<StorageControllerSnapshotPayload> type() {
        return TYPE;
    }

    public static void handle(StorageControllerSnapshotPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null
                    && minecraft.player.containerMenu instanceof StorageControllerMenu menu
                    && menu.containerId == payload.containerId()) {
                menu.updateSnapshot(payload.snapshot());
                if (minecraft.screen instanceof StorageControllerScreen screen) {
                    screen.refreshAfterSync();
                }
            }
        });
    }

    private static final class StorageNetworkServiceLimit {
        private static final int MAX_ENTRIES = 2048;
    }
}
