package com.teamsmartstreamlabs.smartbackpacks.upgrade;


import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.backpack.OpenBackpackTracker;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.PickupUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.pickup.PickupNotifierDestination;
import com.teamsmartstreamlabs.smartbackpacks.pickup.PickupNotifierServer;
import com.teamsmartstreamlabs.smartbackpacks.pickup.PickupNotifierSource;
import com.teamsmartstreamlabs.smartbackpacks.protection.BlockDropProtection;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class PickupUpgradeHandler {
    public static final double PICKUP_RANGE = 1.75D;
    private static final int FRESH_DROP_PROTECTION_TICKS = 20;

    private PickupUpgradeHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || player.tickCount % 2 != 0) {
            return;
        }

        PickupBackpack pickupBackpack = findPickupBackpack(player);
        if (pickupBackpack == null) {
            return;
        }

        Level level = player.level();
        AABB range = player.getBoundingBox().inflate(PICKUP_RANGE);
        Set<Integer> processedEntities = new HashSet<>();
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, range, entity ->
                entity != null
                        && entity.isAlive()
                        && !entity.getItem().isEmpty()
                        && !entity.hasPickUpDelay()
                        && entity.tickCount > FRESH_DROP_PROTECTION_TICKS
                        && BlockDropProtection.canMagnetCollect(player, entity))) {
            if (!processedEntities.add(itemEntity.getId())) {
                continue;
            }

            ItemStack original = itemEntity.getItem();
            if (!PickupFilterMatcher.allows(pickupBackpack.data(), original, pickupBackpack.stack(), pickupBackpack.tier())) {
                continue;
            }

            int beforeCount = original.getCount();
            ItemStack remainder = BackpackStackData.insertIntoStorage(pickupBackpack.stack(), pickupBackpack.tier(), original, false);
            int insertedCount = beforeCount - remainder.getCount();
            if (insertedCount <= 0) {
                continue;
            }

            pickupBackpack.saver().accept(pickupBackpack.stack());
            PickupNotifierServer.reportInserted(player, pickupBackpack.stack(), pickupBackpack.tier(),
                    original.copyWithCount(insertedCount), PickupNotifierDestination.MAIN_STORAGE, PickupNotifierSource.WORLD_PICKUP);
            if (remainder.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(remainder);
            }
        }
    }

    public static void onItemPickup(ItemEntityPickupEvent.Post event) {
        if (!(event.getPlayer() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }
        if (!BlockDropProtection.canMagnetCollect(player, event.getItemEntity())) {
            return;
        }

        int pickedUpCount = event.getOriginalStack().getCount() - event.getCurrentStack().getCount();
        if (pickedUpCount <= 0) {
            return;
        }

        ItemStack pickedUpStack = event.getOriginalStack().copyWithCount(pickedUpCount);
        ItemStack extracted = extractMatchingFromInventory(player.getInventory(), pickedUpStack);
        if (extracted.isEmpty()) {
            return;
        }

        ItemStack remaining = moveIntoPickupBackpacks(player, extracted);
        if (!remaining.isEmpty()) {
            player.getInventory().add(remaining);
        }
    }

    private static PickupBackpack findPickupBackpack(ServerPlayer player) {
        return findPickupBackpacks(player).stream().findFirst().orElse(null);
    }

    private static java.util.List<PickupBackpack> findPickupBackpacks(ServerPlayer player) {
        java.util.List<PickupBackpack> backpacks = new java.util.ArrayList<>();

        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (isOpenBackpack(player, BackpackAccess.inventory(slot, getTier(stack)))) {
                continue;
            }
            int inventorySlot = slot;
            PickupBackpack pickup = createPickupBackpack(stack, backpack -> {
                player.getInventory().setItem(inventorySlot, backpack);
                player.getInventory().setChanged();
            });
            if (pickup != null) {
                backpacks.add(pickup);
            }
        }

        PickupBackpack offhand = isOpenBackpack(player, BackpackAccess.fromHand(player, net.minecraft.world.InteractionHand.OFF_HAND, getTier(player.getOffhandItem())))
                ? null
                : createPickupBackpack(player.getOffhandItem(), backpack -> player.getInventory().setChanged());
        if (offhand != null) {
            backpacks.add(offhand);
        }

        PickupBackpack chest = isOpenBackpack(player, BackpackAccess.chest(getTier(player.getItemBySlot(EquipmentSlot.CHEST))))
                ? null
                : createPickupBackpack(player.getItemBySlot(EquipmentSlot.CHEST), backpack -> player.setItemSlot(EquipmentSlot.CHEST, backpack));
        if (chest != null) {
            backpacks.add(chest);
        }

        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            int curioSlot = slot;
            ItemStack stack = CuriosCompat.getBackStack(player, curioSlot);
            if (isOpenBackpack(player, BackpackAccess.curioBack(curioSlot, getTier(stack)))) {
                continue;
            }
            PickupBackpack pickup = createPickupBackpack(stack, backpack -> CuriosCompat.setBackStack(player, curioSlot, backpack));
            if (pickup != null) {
                backpacks.add(pickup);
            }
        }

        return backpacks;
    }

    private static PickupBackpack createPickupBackpack(ItemStack stack, Consumer<ItemStack> saver) {
        if (!(stack.getItem() instanceof BackpackItem backpackItem)) {
            return null;
        }

        for (ItemStack upgradeStack : BackpackStackData.loadUpgrades(stack)) {
            if (!(upgradeStack.getItem() instanceof PickupUpgradeItem)) {
                continue;
            }

            PickupUpgradeData data = com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.getOrDefault(upgradeStack, ModDataComponents.PICKUP_UPGRADE_DATA.get(), PickupUpgradeData.DEFAULT);
            if (data.enabled()) {
                return new PickupBackpack(stack, backpackItem.getTier(), data, saver);
            }
        }

        return null;
    }

    private static BackpackTier getTier(ItemStack stack) {
        return stack.getItem() instanceof BackpackItem backpackItem ? backpackItem.getTier() : BackpackTier.LEATHER;
    }

    private static boolean isOpenBackpack(ServerPlayer player, BackpackAccess access) {
        return access != null
                && access.tier() != null
                && OpenBackpackTracker.isOpen(player, access);
    }

    public static ItemStack moveIntoPickupBackpacks(ServerPlayer player, ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (PickupBackpack backpack : findPickupBackpacks(player)) {
            if (!PickupFilterMatcher.allows(backpack.data(), remaining, backpack.stack(), backpack.tier())) {
                continue;
            }

            ItemStack attempted = remaining.copy();
            int beforeCount = remaining.getCount();
            remaining = BackpackStackData.insertIntoStorage(backpack.stack(), backpack.tier(), remaining, false);
            int insertedCount = beforeCount - remaining.getCount();
            backpack.saver().accept(backpack.stack());
            if (insertedCount > 0) {
                PickupNotifierServer.reportInserted(player, backpack.stack(), backpack.tier(),
                        attempted.copyWithCount(insertedCount), PickupNotifierDestination.MAIN_STORAGE, PickupNotifierSource.WORLD_PICKUP);
            }
            if (remaining.isEmpty()) {
                break;
            }
        }
        return remaining;
    }

    private static ItemStack extractMatchingFromInventory(Inventory inventory, ItemStack target) {
        ItemStack extracted = ItemStack.EMPTY;
        int remaining = target.getCount();

        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.isSameItemSameComponents(stack, target)) {
                continue;
            }

            int taken = Math.min(remaining, stack.getCount());
            ItemStack split = stack.split(taken);
            if (extracted.isEmpty()) {
                extracted = split;
            } else {
                extracted.grow(split.getCount());
            }
            remaining -= taken;
        }

        if (!extracted.isEmpty()) {
            inventory.setChanged();
        }

        return extracted;
    }

    private record PickupBackpack(ItemStack stack, BackpackTier tier, PickupUpgradeData data, Consumer<ItemStack> saver) {
    }
}


