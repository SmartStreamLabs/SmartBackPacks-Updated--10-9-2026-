package com.teamsmartstreamlabs.smartbackpacks.pickup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.network.PickupNotifierPayload;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class PickupNotifierServer {
    private static final int BATCH_INTERVAL_TICKS = 4;
    private static final Map<UUID, List<PendingNotification>> PENDING = new HashMap<>();

    private PickupNotifierServer() {
    }

    public static void reportInserted(ServerPlayer player, ItemStack backpack, BackpackTier tier, ItemStack inserted,
            PickupNotifierDestination destination, PickupNotifierSource source) {
        if (player != null && !player.level().isClientSide() && !inserted.isEmpty()
                && destination == PickupNotifierDestination.MAIN_STORAGE) {
            com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.recordInsertion(
                    player, backpack, tier, inserted.getCount());
        }
        if (player == null
                || player.level().isClientSide()
                || !SmartBackpacksConfig.pickupNotifierEnabled()
                || !source.isEnabledByConfig()
                || !(backpack.getItem() instanceof BackpackItem)
                || inserted.isEmpty()
                || inserted.getCount() <= 0) {
            return;
        }

        String backpackName = safeBackpackName(backpack);
        List<PendingNotification> notifications = PENDING.computeIfAbsent(player.getUUID(), ignored -> new ArrayList<>());
        for (PendingNotification notification : notifications) {
            if (notification.matches(inserted, backpackName, destination)) {
                notification.add(inserted.getCount(), backpack, tier);
                return;
            }
        }

        // The client cannot display entries beyond its queue limit, so do not create
        // unbounded packet and storage-scan work during very large pickup bursts.
        int usefulEntries = Math.min(SmartBackpacksConfig.pickupNotifierQueueLimit(),
                SmartBackpacksConfig.pickupNotifierMaxVisible());
        if (notifications.size() < Math.max(1, usefulEntries)) {
            notifications.add(new PendingNotification(inserted.copyWithCount(1), inserted.getCount(), backpack,
                    tier, backpackName, destination, source));
        }
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide()) {
            flush(player);
        }
    }

    public static void flush(ServerPlayer player) {
        if (player.tickCount % BATCH_INTERVAL_TICKS != 0) {
            return;
        }

        List<PendingNotification> notifications = PENDING.remove(player.getUUID());
        if (notifications == null) {
            return;
        }

        for (PendingNotification notification : notifications) {
            int total = SmartBackpacksConfig.pickupNotifierShowTotalStoredAmount()
                    && notification.destination == PickupNotifierDestination.MAIN_STORAGE
                    ? countMatching(notification.backpack, notification.tier, notification.displayStack)
                    : -1;
            PacketDistributor.sendToPlayer(player, new PickupNotifierPayload(
                    notification.displayStack, notification.amount, total, notification.backpackName,
                    notification.destination, notification.source));
        }
    }

    private static int countMatching(ItemStack backpack, BackpackTier tier, ItemStack match) {
        int total = 0;
        NonNullList<ItemStack> storage = BackpackStackData.loadStorage(backpack, tier);
        for (ItemStack stack : storage) {
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, match)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static String safeBackpackName(ItemStack backpack) {
        String name = backpack.getHoverName().getString();
        if (name.isBlank()) {
            return "Backpack";
        }
        return name.length() <= 80 ? name : name.substring(0, 80);
    }

    private static final class PendingNotification {
        private final ItemStack displayStack;
        private int amount;
        private ItemStack backpack;
        private BackpackTier tier;
        private final String backpackName;
        private final PickupNotifierDestination destination;
        private final PickupNotifierSource source;

        private PendingNotification(ItemStack displayStack, int amount, ItemStack backpack, BackpackTier tier,
                String backpackName, PickupNotifierDestination destination, PickupNotifierSource source) {
            this.displayStack = displayStack;
            this.amount = amount;
            this.backpack = backpack;
            this.tier = tier;
            this.backpackName = backpackName;
            this.destination = destination;
            this.source = source;
        }

        private boolean matches(ItemStack inserted, String name, PickupNotifierDestination target) {
            return this.destination == target
                    && this.backpackName.equals(name)
                    && ItemStack.isSameItemSameComponents(this.displayStack, inserted);
        }

        private void add(int added, ItemStack latestBackpack, BackpackTier latestTier) {
            this.amount = (int)Math.min(Integer.MAX_VALUE, (long)this.amount + added);
            this.backpack = latestBackpack;
            this.tier = latestTier;
        }
    }
}
