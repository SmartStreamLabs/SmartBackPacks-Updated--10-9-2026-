package com.teamsmartstreamlabs.smartbackpacks.upgrade;


import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.DepositUpgradeItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class DepositUpgradeHandler {
    private DepositUpgradeHandler() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || !player.isShiftKeyDown()) {
            return;
        }

        Container container = getContainer(player.level(), event.getPos());
        if (container == null) {
            return;
        }

        DepositTarget target = findDepositTarget(player);
        if (target == null) {
            return;
        }

        int movedItems = moveMatchingItems(target.backpack(), target.tier(), container);
        if (movedItems <= 0) {
            return;
        }

        container.setChanged();
        target.save().accept(target.backpack());
        player.getInventory().setChanged();
        com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.add(player, "deposit_operations", 1);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static Container getContainer(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock chestBlock) {
            return ChestBlock.getContainer(chestBlock, state, level, pos, true);
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof Container container ? container : null;
    }

    private static DepositTarget findDepositTarget(ServerPlayer player) {
        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        DepositTarget chestTarget = createTarget(chestStack, backpack -> player.getInventory().setChanged());
        if (chestTarget != null) {
            return chestTarget;
        }

        DepositTarget offhandTarget = createTarget(player.getOffhandItem(), backpack -> player.getInventory().setChanged());
        if (offhandTarget != null) {
            return offhandTarget;
        }

        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            int curioSlot = slot;
            DepositTarget target = createTarget(CuriosCompat.getBackStack(player, curioSlot),
                    backpack -> CuriosCompat.setBackStack(player, curioSlot, backpack));
            if (target != null) {
                return target;
            }
        }

        for (int slot = 0; slot < 36; slot++) {
            DepositTarget target = createTarget(player.getInventory().getItem(slot), backpack -> player.getInventory().setChanged());
            if (target != null) {
                return target;
            }
        }

        return null;
    }

    private static DepositTarget createTarget(ItemStack stack, Consumer<ItemStack> saver) {
        if (!(stack.getItem() instanceof BackpackItem backpackItem)) {
            return null;
        }

        for (ItemStack upgrade : BackpackStackData.loadUpgrades(stack)) {
            if (upgrade.getItem() instanceof DepositUpgradeItem) {
                return new DepositTarget(stack, backpackItem.getTier(), saver);
            }
        }

        return null;
    }

    private static int moveMatchingItems(ItemStack backpack, BackpackTier tier, Container container) {
        NonNullList<ItemStack> storage = BackpackStackData.loadStorage(backpack, tier);
        boolean changed = false;
        int movedItems = 0;

        for (int slot = 0; slot < storage.size(); slot++) {
            ItemStack stack = storage.get(slot);
            if (stack.isEmpty() || !containerHasMatch(container, stack)) {
                continue;
            }

            ItemStack remaining = insertIntoContainer(container, stack);
            int moved = stack.getCount() - remaining.getCount();
            if (moved <= 0) {
                continue;
            }

            storage.set(slot, remaining);
            movedItems += moved;
            changed = true;
        }

        if (changed) {
            BackpackStackData.saveStorage(backpack, storage);
        }

        return movedItems;
    }

    private static boolean containerHasMatch(Container container, ItemStack stack) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack existing = container.getItem(slot);
            if (!existing.isEmpty() && com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.isSameItemSameComponents(existing, stack)) {
                return true;
            }
        }
        return false;
    }

    private static ItemStack insertIntoContainer(Container container, ItemStack source) {
        ItemStack remaining = source.copy();

        for (int slot = 0; slot < container.getContainerSize() && !remaining.isEmpty(); slot++) {
            ItemStack existing = container.getItem(slot);
            if (existing.isEmpty() || !com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.isSameItemSameComponents(existing, remaining) || !container.canPlaceItem(slot, remaining)) {
                continue;
            }

            int transfer = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
            if (transfer <= 0) {
                continue;
            }

            existing.grow(transfer);
            remaining.shrink(transfer);
            container.setItem(slot, existing);
        }

        for (int slot = 0; slot < container.getContainerSize() && !remaining.isEmpty(); slot++) {
            ItemStack existing = container.getItem(slot);
            if (!existing.isEmpty() || !container.canPlaceItem(slot, remaining)) {
                continue;
            }

            int placed = Math.min(remaining.getCount(), remaining.getMaxStackSize());
            container.setItem(slot, remaining.copyWithCount(placed));
            remaining.shrink(placed);
        }

        return remaining;
    }

    private record DepositTarget(ItemStack backpack, BackpackTier tier, Consumer<ItemStack> save) {
    }
}

