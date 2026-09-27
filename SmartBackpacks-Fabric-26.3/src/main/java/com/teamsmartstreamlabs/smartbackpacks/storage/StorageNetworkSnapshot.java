package com.teamsmartstreamlabs.smartbackpacks.storage;

import java.util.List;

import net.minecraft.world.item.ItemStack;

public record StorageNetworkSnapshot(
        Status status,
        List<Entry> entries,
        int connectedBackpacks,
        int totalSlots,
        int usedSlots,
        boolean truncated) {
    public static final StorageNetworkSnapshot OFFLINE =
            new StorageNetworkSnapshot(Status.OFFLINE, List.of(), 0, 0, 0, false);

    public StorageNetworkSnapshot {
        entries = List.copyOf(entries);
        totalSlots = Math.max(0, totalSlots);
        usedSlots = Math.max(0, Math.min(usedSlots, totalSlots));
    }

    public enum Status {
        ONLINE,
        NO_BACKPACKS,
        CONTROLLER_CONFLICT,
        NETWORK_TOO_LARGE,
        OFFLINE
    }

    public record Entry(ItemStack icon, long count) {
        public Entry {
            icon = icon.isEmpty() ? ItemStack.EMPTY : icon.copyWithCount(1);
            count = Math.max(0L, count);
        }
    }
}
