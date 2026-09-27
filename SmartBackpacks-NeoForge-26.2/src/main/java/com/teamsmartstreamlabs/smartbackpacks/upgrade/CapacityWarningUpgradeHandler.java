package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.CapacityWarningUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.menu.BackpackMenu;
import com.teamsmartstreamlabs.smartbackpacks.network.CapacityWarningSyncPayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModSounds;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import com.teamsmartstreamlabs.smartbackpacks.util.PlayerMessageHelper;

public final class CapacityWarningUpgradeHandler {
    private static final Map<UUID, PlayerMemory> MEMORIES = new HashMap<>();

    private CapacityWarningUpgradeHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()
                || !SmartBackpacksConfig.capacityWarningUpgradeEnabled()
                || player.tickCount % SmartBackpacksConfig.capacityWarningCheckIntervalTicks() != 0) {
            return;
        }

        PlayerMemory playerMemory = MEMORIES.computeIfAbsent(player.getUUID(), ignored -> new PlayerMemory());
        List<TrackedBackpack> backpacks = findCapacityWarningBackpacks(player);
        Set<String> seenKeys = new HashSet<>();
        List<PendingWarning> warnings = new ArrayList<>();

        for (TrackedBackpack backpack : backpacks) {
            seenKeys.add(backpack.key());
            CapacityWarningSnapshot snapshot = CapacityWarningSnapshot.calculate(backpack.stack(), backpack.tier(), allowedCalculationMode(backpack.data()));
            Tracker tracker = playerMemory.trackers.computeIfAbsent(backpack.key(), ignored -> Tracker.bootstrap(snapshot, backpack.data()));
            PendingWarning warning = tracker.update(backpack, snapshot);
            if (warning != null) {
                warnings.add(warning);
            }
        }

        playerMemory.pruneMissing(player, seenKeys);
        warnings.stream()
                .max((first, second) -> Integer.compare(first.state().severity(), second.state().severity()))
                .ifPresent(warning -> warning.send(player));
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        MEMORIES.remove(event.getEntity().getUUID());
    }

    public static void warnFailedInsertion(ServerPlayer player, ItemStack backpack, BackpackTier tier, ItemStack rejected) {
        if (!SmartBackpacksConfig.capacityWarningUpgradeEnabled()
                || backpack.isEmpty()
                || rejected.isEmpty()
                || !(backpack.getItem() instanceof BackpackItem)) {
            return;
        }

        CapacityWarningUpgradeData data = findInstalledData(backpack);
        if (data == null || !data.enabled() || !data.failedInsertionWarning()) {
            return;
        }

        PlayerMemory memory = MEMORIES.computeIfAbsent(player.getUUID(), ignored -> new PlayerMemory());
        long gameTime = player.level().getGameTime();
        if (gameTime - memory.lastFailedInsertionWarningTick < SmartBackpacksConfig.capacityWarningFailedInsertionCooldownTicks()) {
            return;
        }

        memory.lastFailedInsertionWarningTick = gameTime;
        Component name = backpack.getHoverName();
        PlayerMessageHelper.sendStatus(player, Component.translatable("message.smartbackpacks.capacity_warning.failed_insertion",
                name, rejected.getCount(), rejected.getHoverName()));
        if (data.hud() && data.sound()) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.CAPACITY_FULL.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
        }

        CapacityWarningSnapshot snapshot = CapacityWarningSnapshot.calculate(backpack, tier, allowedCalculationMode(data));
        if (data.hud()) {
            sendHudSync(player, new TrackedBackpack("manual", backpack, tier, data, stack -> {
            }), snapshot, CapacityWarningState.FULL, true, 100);
        }
    }

    private static List<TrackedBackpack> findCapacityWarningBackpacks(ServerPlayer player) {
        List<TrackedBackpack> backpacks = new ArrayList<>();
        int max = SmartBackpacksConfig.capacityWarningMaxBackpacksTrackedPerPlayer();

        addIfCapacityWarning(backpacks, "chest", player.getItemBySlot(EquipmentSlot.CHEST),
                tier -> player.setItemSlot(EquipmentSlot.CHEST, player.getItemBySlot(EquipmentSlot.CHEST)), max);

        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player) && backpacks.size() < max; slot++) {
            int curioSlot = slot;
            addIfCapacityWarning(backpacks, "curio_back:" + curioSlot, CuriosCompat.getBackStack(player, curioSlot),
                    tier -> CuriosCompat.setBackStack(player, curioSlot, CuriosCompat.getBackStack(player, curioSlot)), max);
        }

        for (int slot = 0; slot < 36 && backpacks.size() < max; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            int inventorySlot = slot;
            addIfCapacityWarning(backpacks, "inventory:" + inventorySlot, stack, tier -> {
                player.getInventory().setItem(inventorySlot, player.getInventory().getItem(inventorySlot));
                player.getInventory().setChanged();
            }, max);
        }

        if (backpacks.size() < max) {
            addIfCapacityWarning(backpacks, "offhand", player.getOffhandItem(), tier -> {
                player.setItemInHand(InteractionHand.OFF_HAND, player.getOffhandItem());
                player.getInventory().setChanged();
            }, max);
        }

        return backpacks;
    }

    private static void addIfCapacityWarning(List<TrackedBackpack> backpacks, String key, ItemStack stack,
            Consumer<BackpackTier> saver, int max) {
        if (backpacks.size() >= max || !(stack.getItem() instanceof BackpackItem backpackItem)) {
            return;
        }

        CapacityWarningUpgradeData data = findInstalledData(stack);
        if (data != null && data.enabled()) {
            backpacks.add(new TrackedBackpack(key, stack, backpackItem.getTier(), data, saver));
        }
    }

    private static CapacityWarningUpgradeData findInstalledData(ItemStack backpack) {
        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        for (ItemStack upgrade : upgrades) {
            if (!(upgrade.getItem() instanceof CapacityWarningUpgradeItem)) {
                continue;
            }

            return upgrade.getOrDefault(ModDataComponents.CAPACITY_WARNING_UPGRADE_DATA.get(), CapacityWarningUpgradeData.DEFAULT);
        }
        return null;
    }

    private static CapacityWarningCalculationMode allowedCalculationMode(CapacityWarningUpgradeData data) {
        if (data.calculationMode() == CapacityWarningCalculationMode.STACK_CAPACITY
                && !SmartBackpacksConfig.capacityWarningAllowStackCapacityMode()) {
            return CapacityWarningCalculationMode.OCCUPIED_SLOTS;
        }
        return data.calculationMode();
    }

    private static void sendHudSync(ServerPlayer player, TrackedBackpack backpack, CapacityWarningSnapshot snapshot,
            CapacityWarningState state, boolean showHud, int percentage) {
        PacketDistributor.sendToPlayer(player, new CapacityWarningSyncPayload(
                backpack.stack().getHoverName().getString(),
                percentage,
                state,
                snapshot.occupiedSlots(),
                snapshot.totalSlots(),
                snapshot.freeSlots(),
                showHud));
    }

    private record TrackedBackpack(String key, ItemStack stack, BackpackTier tier, CapacityWarningUpgradeData data, Consumer<BackpackTier> saver) {
    }

    private record PendingWarning(TrackedBackpack backpack, CapacityWarningSnapshot snapshot, CapacityWarningState state, int threshold) {
        private void send(ServerPlayer player) {
            if (this.backpack.data().actionBar()) {
                PlayerMessageHelper.sendStatus(player, this.message());
            }
            if (this.backpack.data().hud() && this.backpack.data().sound()) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        this.state == CapacityWarningState.FULL ? ModSounds.CAPACITY_FULL.get() : ModSounds.CAPACITY_WARNING.get(),
                        SoundSource.PLAYERS, this.state == CapacityWarningState.FULL ? 0.65F : 0.4F, 1.0F);
            }
            if (this.backpack.data().hud()) {
                sendHudSync(player, this.backpack, this.snapshot, this.state, true, this.threshold);
            }
        }

        private Component message() {
            return switch (this.state) {
                case FULL -> Component.translatable("message.smartbackpacks.capacity_warning.full",
                        this.backpack.stack().getHoverName(), this.snapshot.occupiedSlots(), this.snapshot.totalSlots());
                case CRITICAL -> Component.translatable("message.smartbackpacks.capacity_warning.critical",
                        this.backpack.stack().getHoverName(), this.snapshot.displayPercentage(), this.snapshot.occupiedSlots(), this.snapshot.totalSlots());
                case WARNING -> Component.translatable("message.smartbackpacks.capacity_warning.warning",
                        this.backpack.stack().getHoverName(), this.snapshot.displayPercentage(), this.snapshot.occupiedSlots(), this.snapshot.totalSlots());
                case NORMAL -> Component.empty();
            };
        }

    }

    private static final class PlayerMemory {
        private final Map<String, Tracker> trackers = new HashMap<>();
        private long lastFailedInsertionWarningTick = Long.MIN_VALUE;

        private void pruneMissing(ServerPlayer player, Set<String> seenKeys) {
            this.trackers.entrySet().removeIf(entry -> {
                boolean remove = !seenKeys.contains(entry.getKey());
                return remove;
            });
        }
    }

    private static final class Tracker {
        private int lastBucket;
        private ItemStack stack;

        private static Tracker bootstrap(CapacityWarningSnapshot snapshot, CapacityWarningUpgradeData data) {
            Tracker tracker = new Tracker();
            tracker.lastBucket = bucket(snapshot, data);
            return tracker;
        }

        private PendingWarning update(TrackedBackpack backpack, CapacityWarningSnapshot snapshot) {
            int current = bucket(snapshot, backpack.data());
            if (this.stack != backpack.stack()) {
                this.stack = backpack.stack();
                this.lastBucket = current;
                return null;
            }
            int previous = this.lastBucket;
            this.lastBucket = current;
            if (current <= previous || current == 0) {
                return null;
            }
            CapacityWarningState state = snapshot.percentage() >= 100.0D
                    ? CapacityWarningState.FULL : current * backpack.data().thresholdStep() >= 80
                    ? CapacityWarningState.CRITICAL : CapacityWarningState.WARNING;
            return new PendingWarning(backpack, snapshot, state, Math.min(100, current * backpack.data().thresholdStep()));
        }

        private static int bucket(CapacityWarningSnapshot snapshot, CapacityWarningUpgradeData data) {
            return (int) Math.floor(snapshot.percentage() / data.thresholdStep());
        }
    }
}
