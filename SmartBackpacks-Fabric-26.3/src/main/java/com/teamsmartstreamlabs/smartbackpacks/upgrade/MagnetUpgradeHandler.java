package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.backpack.OpenBackpackTracker;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.MagnetUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.pickup.PickupNotifierDestination;
import com.teamsmartstreamlabs.smartbackpacks.pickup.PickupNotifierServer;
import com.teamsmartstreamlabs.smartbackpacks.pickup.PickupNotifierSource;
import com.teamsmartstreamlabs.smartbackpacks.protection.BlockDropProtection;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class MagnetUpgradeHandler {
    private static final int FRESH_DROP_PROTECTION_TICKS = 20;

    private MagnetUpgradeHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }

        List<MagnetizedBackpack> backpacks = findMagnetBackpacks(player);
        if (backpacks.isEmpty()) {
            return;
        }

        int tickInterval = backpacks.stream()
                .mapToInt(MagnetizedBackpack::getTickInterval)
                .min()
                .orElse(5);
        if (player.tickCount % tickInterval != 0) {
            return;
        }

        Level level = player.level();
        int maxRange = backpacks.stream()
                .mapToInt(MagnetizedBackpack::getRange)
                .max()
                .orElse(10);
        AABB range = player.getBoundingBox().inflate(maxRange);
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, range, entity ->
                entity != null
                        && entity.isAlive()
                        && !entity.getItem().isEmpty()
                        && entity.tickCount > FRESH_DROP_PROTECTION_TICKS
                        && BlockDropProtection.canMagnetCollect(player, entity))) {
            ItemStack entityStack = itemEntity.getItem();
            MagnetizedBackpack target = backpacks.stream()
                    .filter(backpack -> backpack.canCollect(player, itemEntity))
                    .findFirst()
                    .orElse(null);
            if (target == null) {
                continue;
            }

            pullTowardPlayer(player, itemEntity, target.getPullStrength(player, itemEntity));
            if (itemEntity.distanceToSqr(player) > target.getPickupDistanceSqr(player, itemEntity)) {
                continue;
            }

            ItemStack remainder = ItemLockProtection.insertIntoStorage(target.stack(), target.tier(), entityStack, false);
            if (remainder.getCount() == entityStack.getCount()) {
                continue;
            }

            target.saver().accept(target.stack());
            int inserted = entityStack.getCount() - remainder.getCount();
            com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.add(player, "magnet_items_collected", inserted);
            PickupNotifierServer.reportInserted(player, target.stack(), target.tier(), entityStack.copyWithCount(inserted),
                    PickupNotifierDestination.MAIN_STORAGE, PickupNotifierSource.WORLD_PICKUP);
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

        ItemStack remaining = moveIntoMagnetBackpacks(player, extracted);
        if (!remaining.isEmpty()) {
            player.getInventory().add(remaining);
        }
    }

    public static boolean tryDirectPickup(ServerPlayer player, ItemEntity itemEntity) {
        if (player.level().isClientSide()
                || itemEntity == null
                || !BlockDropProtection.canMagnetCollect(player, itemEntity)
                || !itemEntity.isAlive()
                || itemEntity.getItem().isEmpty()
                || itemEntity.hasPickUpDelay()
                || itemEntity.tickCount <= FRESH_DROP_PROTECTION_TICKS) {
            return false;
        }

        ItemStack original = itemEntity.getItem();
        ItemStack remaining = moveIntoMagnetBackpacks(player, original);
        if (remaining.getCount() == original.getCount()) {
            return false;
        }

        if (remaining.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(remaining);
        }
        return true;
    }

    private static List<MagnetizedBackpack> findMagnetBackpacks(ServerPlayer player) {
        List<MagnetizedBackpack> backpacks = new ArrayList<>();

        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (isOpenBackpack(player, BackpackAccess.inventory(slot, getTier(stack)))) {
                continue;
            }
            int inventorySlot = slot;
            addIfMagnetized(backpacks, stack, backpack -> {
                player.getInventory().setItem(inventorySlot, backpack);
                player.getInventory().setChanged();
            });
        }

        if (!isOpenBackpack(player, BackpackAccess.fromHand(player, net.minecraft.world.InteractionHand.OFF_HAND, getTier(player.getOffhandItem())))) {
            addIfMagnetized(backpacks, player.getOffhandItem(), backpack -> player.getInventory().setChanged());
        }
        if (!isOpenBackpack(player, BackpackAccess.chest(getTier(player.getItemBySlot(EquipmentSlot.CHEST))))) {
            addIfMagnetized(backpacks, player.getItemBySlot(EquipmentSlot.CHEST), backpack -> player.setItemSlot(EquipmentSlot.CHEST, backpack));
        }

        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            int curioSlot = slot;
            ItemStack stack = CuriosCompat.getBackStack(player, curioSlot);
            if (isOpenBackpack(player, BackpackAccess.curioBack(curioSlot, getTier(stack)))) {
                continue;
            }
            addIfMagnetized(backpacks, stack, backpack -> CuriosCompat.setBackStack(player, curioSlot, backpack));
        }

        return backpacks;
    }

    private static void addIfMagnetized(List<MagnetizedBackpack> backpacks, ItemStack stack, Consumer<ItemStack> saver) {
        if (!(stack.getItem() instanceof BackpackItem backpackItem)) {
            return;
        }

        List<InstalledMagnet> magnets = new ArrayList<>();
        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(stack);
        for (ItemStack upgradeStack : upgrades) {
            if (upgradeStack.getItem() instanceof MagnetUpgradeItem magnetItem) {
                MagnetUpgradeData data = upgradeStack.getOrDefault(ModDataComponents.MAGNET_UPGRADE_DATA.get(), MagnetUpgradeData.DEFAULT);
                if (data.enabled()) {
                    magnets.add(new InstalledMagnet(magnetItem, data));
                }
            }
        }

        if (!magnets.isEmpty()) {
            backpacks.add(new MagnetizedBackpack(stack, backpackItem.getTier(), magnets, saver));
        }
    }

    private static BackpackTier getTier(ItemStack stack) {
        return stack.getItem() instanceof BackpackItem backpackItem ? backpackItem.getTier() : BackpackTier.LEATHER;
    }

    private static boolean isOpenBackpack(ServerPlayer player, BackpackAccess access) {
        return access != null
                && access.tier() != null
                && OpenBackpackTracker.isOpen(player, access);
    }

    private static ItemStack moveIntoMagnetBackpacks(ServerPlayer player, ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (MagnetizedBackpack backpack : findMagnetBackpacks(player)) {
            if (!backpack.canCollectPickedUpStack(remaining)) {
                continue;
            }

            ItemStack attempted = remaining.copy();
            remaining = ItemLockProtection.insertIntoStorage(backpack.stack(), backpack.tier(), remaining, false);
            backpack.saver().accept(backpack.stack());
            int inserted = attempted.getCount() - remaining.getCount();
            if (inserted > 0) {
                com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.add(player, "magnet_items_collected", inserted);
                PickupNotifierServer.reportInserted(player, backpack.stack(), backpack.tier(), attempted.copyWithCount(inserted),
                        PickupNotifierDestination.MAIN_STORAGE, PickupNotifierSource.WORLD_PICKUP);
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
            if (!ItemStack.isSameItemSameComponents(stack, target)) {
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

    private static void pullTowardPlayer(ServerPlayer player, ItemEntity itemEntity, double pullStrength) {
        Vec3 target = player.position().add(0.0D, 0.75D, 0.0D);
        Vec3 direction = target.subtract(itemEntity.position());
        if (direction.lengthSqr() < 0.001D) {
            return;
        }

        Vec3 motion = direction.normalize().scale(pullStrength);
        itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().scale(0.6D).add(motion));
    }

    private record MagnetizedBackpack(ItemStack stack, BackpackTier tier, List<InstalledMagnet> magnets, Consumer<ItemStack> saver) {
        private boolean canCollect(ServerPlayer player, ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            if (ItemLockProtection.insertIntoStorage(this.stack, this.tier, stack, true).getCount() == stack.getCount()) {
                return false;
            }

            return this.findMatchingMagnet(player, itemEntity) != null;
        }

        private boolean canCollectPickedUpStack(ItemStack stack) {
            if (ItemLockProtection.insertIntoStorage(this.stack, this.tier, stack, true).getCount() == stack.getCount()) {
                return false;
            }

            for (InstalledMagnet magnet : this.magnets) {
                if (MagnetFilterMatcher.allows(magnet.item(), magnet.data(), stack, this.stack, this.tier)) {
                    return true;
                }
            }
            return false;
        }

        private int getRange() {
            return this.magnets.stream().mapToInt(magnet -> magnet.item().getRange()).max().orElse(10);
        }

        private int getTickInterval() {
            return this.magnets.stream().mapToInt(magnet -> magnet.item().getTickInterval()).min().orElse(5);
        }

        private double getPullStrength(ServerPlayer player, ItemEntity itemEntity) {
            InstalledMagnet magnet = this.findMatchingMagnet(player, itemEntity);
            return magnet != null ? magnet.item().getPullStrength() : 0.25D;
        }

        private double getPickupDistanceSqr(ServerPlayer player, ItemEntity itemEntity) {
            InstalledMagnet magnet = this.findMatchingMagnet(player, itemEntity);
            return magnet != null ? magnet.item().getPickupDistanceSqr() : 2.25D;
        }

        private InstalledMagnet findMatchingMagnet(ServerPlayer player, ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            for (InstalledMagnet magnet : this.magnets) {
                int range = magnet.item().getRange();
                if (itemEntity.distanceToSqr(player) > range * range) {
                    continue;
                }
                if (MagnetFilterMatcher.allows(magnet.item(), magnet.data(), stack, this.stack, this.tier)) {
                    return magnet;
                }
            }
            return null;
        }
    }

    private record InstalledMagnet(MagnetUpgradeItem item, MagnetUpgradeData data) {
    }
}
