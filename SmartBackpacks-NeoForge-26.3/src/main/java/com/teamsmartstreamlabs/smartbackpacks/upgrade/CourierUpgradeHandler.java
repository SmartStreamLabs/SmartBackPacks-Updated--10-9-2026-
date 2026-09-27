package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackUpgradeInventory;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.CourierUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.util.PlayerMessageHelper;


import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class CourierUpgradeHandler {
    private static final int INTERVAL = 20;
    private static final Map<UUID, PendingLink> PENDING_LINKS = new HashMap<>();
    private static final Map<String, Integer> CURSORS = new HashMap<>();

    private CourierUpgradeHandler() {
    }

    public static void toggleLinking(ServerPlayer player, BackpackAccess access, int upgradeSlot, ItemStack upgrade) {
        CourierDestinationData current = upgrade.getOrDefault(ModDataComponents.COURIER_DESTINATION_DATA.get(), CourierDestinationData.EMPTY);
        if (current.linked()) {
            upgrade.set(ModDataComponents.COURIER_DESTINATION_DATA.get(), CourierDestinationData.EMPTY);
            new BackpackUpgradeInventory(player, access).setItem(upgradeSlot, upgrade);
            PENDING_LINKS.remove(player.getUUID());
            PlayerMessageHelper.sendStatus(player, Component.translatable("message.smartbackpacks.courier.cleared"));
            return;
        }
        PENDING_LINKS.put(player.getUUID(), new PendingLink(access, upgradeSlot));
        player.closeContainer();
        PlayerMessageHelper.sendStatus(player, Component.translatable("message.smartbackpacks.courier.select_destination"));
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) return;
        PendingLink pending = PENDING_LINKS.remove(player.getUUID());
        if (pending == null) return;
        BlockPos pos = event.getPos();
        BlockEntity destination = player.level().hasChunkAt(pos) ? player.level().getBlockEntity(pos) : null;
        if (destination == null || !isInventory((ServerLevel) player.level(), pos, destination)) {
            PlayerMessageHelper.sendStatus(player, Component.translatable("message.smartbackpacks.courier.invalid_destination"));
            return;
        }
        BackpackUpgradeInventory upgrades = new BackpackUpgradeInventory(player, pending.access());
        if (pending.slot() < 0 || pending.slot() >= upgrades.getContainerSize()) return;
        ItemStack upgrade = upgrades.getItem(pending.slot());
        if (!(upgrade.getItem() instanceof CourierUpgradeItem)) return;
        String name = player.level().getBlockState(pos).getBlock().getName().getString();
        CourierDestinationData data = new CourierDestinationData(true, player.level().dimension().toString(), pos.asLong(), name);
        upgrade.set(ModDataComponents.COURIER_DESTINATION_DATA.get(), data);
        upgrades.setItem(pending.slot(), upgrade);
        PlayerMessageHelper.sendStatus(player, Component.translatable("message.smartbackpacks.courier.linked", name));
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
    }

    private static boolean isInventory(ServerLevel level, BlockPos pos, BlockEntity blockEntity) {
        return blockEntity instanceof Container || Capabilities.Item.BLOCK.getCapability(level, pos, level.getBlockState(pos), blockEntity, null) != null;
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || player.tickCount % INTERVAL != 0) return;
        process(player, "chest", player.getItemBySlot(EquipmentSlot.CHEST), stack -> player.setItemSlot(EquipmentSlot.CHEST, stack));
        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            int index = slot;
            process(player, "curio:" + slot, CuriosCompat.getBackStack(player, slot), stack -> CuriosCompat.setBackStack(player, index, stack));
        }
        for (int slot = 0; slot < 36; slot++) {
            int index = slot;
            process(player, "inventory:" + slot, player.getInventory().getItem(slot), stack -> { player.getInventory().setItem(index, stack); player.getInventory().setChanged(); });
        }
        process(player, "offhand", player.getOffhandItem(), stack -> player.setItemInHand(InteractionHand.OFF_HAND, stack));
    }

    private static void process(ServerPlayer player, String key, ItemStack backpack, Consumer<ItemStack> saver) {
        process((ServerLevel) player.level(), player.getUUID() + ":" + key, backpack, updated -> {
            saver.accept(updated);
            player.getInventory().setChanged();
            player.inventoryMenu.broadcastChanges();
            if (player.containerMenu != player.inventoryMenu) {
                player.containerMenu.broadcastChanges();
            }
        });
    }

    public static void processPlaced(ServerLevel level, BlockPos sourcePos, ItemStack backpack, Consumer<ItemStack> saver) {
        if (level.getGameTime() % INTERVAL == 0L) process(level, "placed:" + level.dimension() + ":" + sourcePos.asLong(), backpack, saver);
    }

    private static void process(ServerLevel level, String key, ItemStack backpack, Consumer<ItemStack> saver) {
        if (!(backpack.getItem() instanceof BackpackItem backpackItem)) return;

        // Always replace the carrier stack after a transfer. Mutating the same ItemStack
        // reference can be missed by inventory/equipment sync and restore the old contents.
        ItemStack updatedBackpack = backpack.copy();
        ItemStack courier = findCourier(updatedBackpack);
        if (courier.isEmpty()) return;
        CourierDestinationData destination = courier.getOrDefault(ModDataComponents.COURIER_DESTINATION_DATA.get(), CourierDestinationData.EMPTY);
        FilterUpgradeData filter = courier.getOrDefault(ModDataComponents.FILTER_UPGRADE_DATA.get(), FilterUpgradeData.DEFAULT);
        if (!filter.enabled() || !destination.linked() || !destination.dimension().equals(level.dimension().toString())) return;
        BlockPos pos = BlockPos.of(destination.position());
        if (!level.hasChunkAt(pos)) return;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return;
        BackpackTier tier = backpackItem.getTier();
        NonNullList<ItemStack> storage = BackpackStackData.loadStorage(updatedBackpack, tier);
        int start = CURSORS.getOrDefault(key, 0) % Math.max(1, storage.size());
        for (int checked = 0; checked < storage.size(); checked++) {
            int slot = (start + checked) % storage.size();
            ItemStack source = storage.get(slot);
            if (source.isEmpty() || !FilterUpgradeMatcher.allows(filter, source)) continue;
            int transferLimit = Math.min(64, source.getCount());
            int moved = Math.min(transferLimit, Math.max(0,
                    insert(level, pos, blockEntity, source.copyWithCount(transferLimit))));
            CURSORS.put(key, (slot + 1) % storage.size());
            if (moved > 0) {
                source.shrink(moved);
                if (source.isEmpty()) storage.set(slot, ItemStack.EMPTY);
                BackpackStackData.saveStorage(updatedBackpack, storage);
                saver.accept(updatedBackpack);
            }
            return;
        }
    }

    private static ItemStack findCourier(ItemStack backpack) {
        for (ItemStack upgrade : BackpackStackData.loadUpgrades(backpack)) if (upgrade.getItem() instanceof CourierUpgradeItem) return upgrade;
        return ItemStack.EMPTY;
    }

    private static int insert(ServerLevel level, BlockPos pos, BlockEntity blockEntity, ItemStack incoming) {
        if (blockEntity instanceof Container target) return insertContainer(target, incoming);
        var handler = Capabilities.Item.BLOCK.getCapability(level, pos, level.getBlockState(pos), blockEntity, null);
        if (handler == null) return 0;
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = handler.insert(ItemResource.of(incoming), incoming.getCount(), transaction);
            transaction.commit();
            return inserted;
        }
    }

    private static int insertContainer(Container target, ItemStack incoming) {
        int original = incoming.getCount();
        for (int slot = 0; slot < target.getContainerSize() && !incoming.isEmpty(); slot++) {
            if (!target.canPlaceItem(slot, incoming)) continue;
            ItemStack existing = target.getItem(slot);
            int limit = Math.min(target.getMaxStackSize(), incoming.getMaxStackSize());
            if (existing.isEmpty()) {
                int amount = Math.min(limit, incoming.getCount());
                target.setItem(slot, incoming.copyWithCount(amount));
                incoming.shrink(amount);
            } else if (ItemStack.isSameItemSameComponents(existing, incoming)) {
                int amount = Math.min(limit - existing.getCount(), incoming.getCount());
                if (amount > 0) { existing.grow(amount); incoming.shrink(amount); }
            }
        }
        int moved = original - incoming.getCount();
        if (moved > 0) target.setChanged();
        return moved;
    }

    private record PendingLink(BackpackAccess access, int slot) {}
}
