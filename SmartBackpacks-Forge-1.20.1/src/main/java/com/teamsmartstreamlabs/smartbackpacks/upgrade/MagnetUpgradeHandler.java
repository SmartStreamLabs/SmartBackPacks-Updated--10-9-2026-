package com.teamsmartstreamlabs.smartbackpacks.upgrade;


import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
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
            MagnetPickupTarget target = findPickupTarget(backpacks, player, itemEntity);
            if (target == null) {
                continue;
            }

            pullTowardPlayer(player, itemEntity, target.magnet().item().getPullStrength());
            if (itemEntity.distanceToSqr(player) > target.magnet().item().getPickupDistanceSqr()) {
                continue;
            }

            int beforeCount = entityStack.getCount();
            ItemStack remainder = target.backpack().insert(entityStack);
            int insertedCount = beforeCount - remainder.getCount();
            if (insertedCount <= 0) {
                continue;
            }

            com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.add(player, "magnet_items_collected", insertedCount);
            PickupNotifierServer.reportInserted(player, target.backpack().stack(), target.backpack().tier(),
                    entityStack.copyWithCount(insertedCount), PickupNotifierDestination.MAIN_STORAGE, PickupNotifierSource.WORLD_PICKUP);
            if (remainder.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(remainder);
            }
        }

        backpacks.forEach(MagnetizedBackpack::saveIfDirty);
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
                MagnetUpgradeData data = com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.getOrDefault(upgradeStack, ModDataComponents.MAGNET_UPGRADE_DATA.get(), MagnetUpgradeData.DEFAULT);
                if (data.enabled()) {
                    magnets.add(new InstalledMagnet(magnetItem, data));
                }
            }
        }

        if (!magnets.isEmpty()) {
            BackpackTier tier = backpackItem.getTier();
            backpacks.add(new MagnetizedBackpack(stack, tier, magnets, BackpackStackData.loadStorage(stack, tier), saver));
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

    public static ItemStack moveIntoMagnetBackpacks(ServerPlayer player, ItemStack stack) {
        ItemStack remaining = stack.copy();
        List<MagnetizedBackpack> backpacks = findMagnetBackpacks(player);
        for (MagnetizedBackpack backpack : backpacks) {
            if (!backpack.canCollectPickedUpStack(remaining)) {
                continue;
            }

            ItemStack attempted = remaining.copy();
            int beforeCount = remaining.getCount();
            remaining = backpack.insert(remaining);
            int insertedCount = beforeCount - remaining.getCount();
            if (insertedCount > 0) {
                com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.add(player, "magnet_items_collected", insertedCount);
                PickupNotifierServer.reportInserted(player, backpack.stack(), backpack.tier(),
                        attempted.copyWithCount(insertedCount), PickupNotifierDestination.MAIN_STORAGE, PickupNotifierSource.WORLD_PICKUP);
            }
            if (remaining.isEmpty()) {
                break;
            }
        }
        backpacks.forEach(MagnetizedBackpack::saveIfDirty);
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

    private static void pullTowardPlayer(ServerPlayer player, ItemEntity itemEntity, double pullStrength) {
        Vec3 target = player.position().add(0.0D, 0.75D, 0.0D);
        Vec3 direction = target.subtract(itemEntity.position());
        if (direction.lengthSqr() < 0.001D) {
            return;
        }

        Vec3 motion = direction.normalize().scale(pullStrength);
        itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().scale(0.6D).add(motion));
        itemEntity.hurtMarked = true;
    }

    private static MagnetPickupTarget findPickupTarget(List<MagnetizedBackpack> backpacks, ServerPlayer player, ItemEntity itemEntity) {
        for (MagnetizedBackpack backpack : backpacks) {
            InstalledMagnet magnet = backpack.findMatchingMagnet(player, itemEntity);
            if (magnet != null) {
                return new MagnetPickupTarget(backpack, magnet);
            }
        }
        return null;
    }

    private static final class MagnetizedBackpack {
        private final ItemStack stack;
        private final BackpackTier tier;
        private final List<InstalledMagnet> magnets;
        private final NonNullList<ItemStack> contents;
        private final Consumer<ItemStack> saver;
        private final int storageUpgradeTier;
        private final int range;
        private final int tickInterval;
        private boolean dirty;

        private MagnetizedBackpack(ItemStack stack, BackpackTier tier, List<InstalledMagnet> magnets, NonNullList<ItemStack> contents, Consumer<ItemStack> saver) {
            this.stack = stack;
            this.tier = tier;
            this.magnets = magnets;
            this.contents = contents;
            this.saver = saver;
            this.storageUpgradeTier = BackpackStackData.getStorageUpgradeTier(stack);
            this.range = magnets.stream().mapToInt(magnet -> magnet.item().getRange()).max().orElse(10);
            this.tickInterval = magnets.stream().mapToInt(magnet -> magnet.item().getTickInterval()).min().orElse(5);
        }

        private ItemStack stack() {
            return this.stack;
        }

        private BackpackTier tier() {
            return this.tier;
        }

        private boolean canCollectPickedUpStack(ItemStack stack) {
            if (!this.canFit(stack)) {
                return false;
            }

            for (InstalledMagnet magnet : this.magnets) {
                if (MagnetFilterMatcher.allowsPrechecked(magnet.item(), magnet.data(), stack, this.contents)) {
                    return true;
                }
            }
            return false;
        }

        private ItemStack insert(ItemStack stack) {
            ItemStack remaining = ItemLockProtection.insertIntoStorage(this.stack, this.contents, stack, false, this.storageUpgradeTier);
            this.dirty |= remaining.getCount() != stack.getCount();
            return remaining;
        }

        private void saveIfDirty() {
            if (!this.dirty) {
                return;
            }

            BackpackStackData.saveStorage(this.stack, this.contents);
            this.saver.accept(this.stack);
            this.dirty = false;
        }

        private boolean canFit(ItemStack stack) {
            return ItemLockProtection.insertIntoStorage(this.stack, this.contents, stack, true, this.storageUpgradeTier).getCount() < stack.getCount();
        }

        private int getRange() {
            return this.range;
        }

        private int getTickInterval() {
            return this.tickInterval;
        }

        private InstalledMagnet findMatchingMagnet(ServerPlayer player, ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            for (InstalledMagnet magnet : this.magnets) {
                int range = magnet.item().getRange();
                if (itemEntity.distanceToSqr(player) > range * range) {
                    continue;
                }
                if (MagnetFilterMatcher.allowsPrechecked(magnet.item(), magnet.data(), stack, this.contents)) {
                    return magnet;
                }
            }
            return null;
        }
    }

    private record MagnetPickupTarget(MagnetizedBackpack backpack, InstalledMagnet magnet) {
    }

    private record InstalledMagnet(MagnetUpgradeItem item, MagnetUpgradeData data) {
    }
}


