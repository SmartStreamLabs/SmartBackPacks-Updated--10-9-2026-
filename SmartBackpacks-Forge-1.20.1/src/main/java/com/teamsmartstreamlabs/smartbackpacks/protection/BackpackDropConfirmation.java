package com.teamsmartstreamlabs.smartbackpacks.protection;

import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class BackpackDropConfirmation {
    private static final long WINDOW_NANOS = 3_000_000_000L;
    private static final Map<Player, Pending> CLIENT_PENDING = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Player, Pending> SERVER_PENDING = Collections.synchronizedMap(new WeakHashMap<>());

    private BackpackDropConfirmation() {
    }

    public static boolean allow(Player player) {
        ItemStack held = player.getInventory().getSelected();
        if (!(held.getItem() instanceof BackpackItem)) {
            clear(player);
            return true;
        }
        int slot = player.getInventory().selected;
        long now = System.nanoTime();
        Map<Player, Pending> pending = pendingFor(player);
        synchronized (pending) {
            Pending previous = pending.get(player);
            if (previous != null && sameBackpack(player, previous, held) && previous.slot == slot
                    && now - previous.createdAt <= WINDOW_NANOS) {
                pending.remove(player);
                return true;
            }
            pending.put(player, new Pending(held, held.copy(), slot, now));
        }
        return false;
    }

    public static void reconcile(Player player) {
        Map<Player, Pending> confirmations = pendingFor(player);
        synchronized (confirmations) {
            Pending pending = confirmations.get(player);
            if (pending != null && (pending.slot != player.getInventory().selected
                    || !sameBackpack(player, pending, player.getInventory().getSelected()))) {
                confirmations.remove(player);
            }
        }
    }

    public static void clear(Player player) {
        pendingFor(player).remove(player);
    }

    private static Map<Player, Pending> pendingFor(Player player) {
        return player.level().isClientSide() ? CLIENT_PENDING : SERVER_PENDING;
    }

    private static boolean sameBackpack(Player player, Pending pending, ItemStack held) {
        return pending.stack == held || player.level().isClientSide()
                && (ItemStack.matches(pending.snapshot, held)
                        || held.getItem() == pending.snapshot.getItem());
    }

    private record Pending(ItemStack stack, ItemStack snapshot, int slot, long createdAt) {
    }
}
