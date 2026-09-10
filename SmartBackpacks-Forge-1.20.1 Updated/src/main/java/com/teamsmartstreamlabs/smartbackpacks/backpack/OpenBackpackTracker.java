package com.teamsmartstreamlabs.smartbackpacks.backpack;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public final class OpenBackpackTracker {
    private static final Set<OpenedBackpack> OPEN_BACKPACKS = ConcurrentHashMap.newKeySet();

    private OpenBackpackTracker() {
    }

    public static void markOpen(Player player, BackpackAccess access) {
        OPEN_BACKPACKS.add(new OpenedBackpack(player.getUUID(), canonicalize(player, access)));
    }

    public static void markClosed(Player player, BackpackAccess access) {
        OPEN_BACKPACKS.remove(new OpenedBackpack(player.getUUID(), canonicalize(player, access)));
    }

    public static boolean isOpen(Player player, BackpackAccess access) {
        return OPEN_BACKPACKS.contains(new OpenedBackpack(player.getUUID(), canonicalize(player, access)));
    }

    private static BackpackAccess canonicalize(Player player, BackpackAccess access) {
        return switch (access.source()) {
            case MAIN_HAND -> BackpackAccess.inventory(player.getInventory().selected, access.tier());
            case INVENTORY -> BackpackAccess.inventory(access.slotIndex(), access.tier());
            case OFF_HAND -> new BackpackAccess(BackpackAccess.Source.OFF_HAND, BackpackAccess.OFFHAND_SLOT, BlockPos.ZERO, access.tier());
            case CHEST -> BackpackAccess.chest(access.tier());
            case CURIO_BACK -> BackpackAccess.curioBack(access.slotIndex(), access.tier());
            case BLOCK -> BackpackAccess.block(access.blockPos(), access.tier());
        };
    }

    private record OpenedBackpack(UUID playerId, BackpackAccess access) {
    }
}
