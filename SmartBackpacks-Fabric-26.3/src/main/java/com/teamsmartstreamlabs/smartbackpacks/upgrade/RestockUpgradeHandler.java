package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.RestockUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.pickup.PickupNotifierDestination;
import com.teamsmartstreamlabs.smartbackpacks.pickup.PickupNotifierServer;
import com.teamsmartstreamlabs.smartbackpacks.pickup.PickupNotifierSource;

import net.minecraft.core.BlockPos;
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

public final class RestockUpgradeHandler {
    private RestockUpgradeHandler() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || !player.isShiftKeyDown()) {
            return;
        }

        Container container = getContainer(player.level(), event.getPos());
        if (container == null) {
            return;
        }

        RestockTarget target = findRestockTarget(player);
        if (target == null) {
            return;
        }

        int movedItems = moveMatchingItems(player, container, target.backpack(), target.tier());
        if (movedItems <= 0) {
            return;
        }

        container.setChanged();
        target.save().accept(target.backpack());
        player.getInventory().setChanged();
        com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.add(player, "restock_operations", 1);
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

    private static RestockTarget findRestockTarget(ServerPlayer player) {
        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        RestockTarget chestTarget = createTarget(chestStack, backpack -> player.getInventory().setChanged());
        if (chestTarget != null) {
            return chestTarget;
        }

        RestockTarget offhandTarget = createTarget(player.getOffhandItem(), backpack -> player.getInventory().setChanged());
        if (offhandTarget != null) {
            return offhandTarget;
        }

        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            int curioSlot = slot;
            RestockTarget target = createTarget(CuriosCompat.getBackStack(player, curioSlot),
                    backpack -> CuriosCompat.setBackStack(player, curioSlot, backpack));
            if (target != null) {
                return target;
            }
        }

        for (int slot = 0; slot < 36; slot++) {
            RestockTarget target = createTarget(player.getInventory().getItem(slot), backpack -> player.getInventory().setChanged());
            if (target != null) {
                return target;
            }
        }

        return null;
    }

    private static RestockTarget createTarget(ItemStack stack, Consumer<ItemStack> saver) {
        if (!(stack.getItem() instanceof BackpackItem backpackItem)) {
            return null;
        }

        for (ItemStack upgrade : BackpackStackData.loadUpgrades(stack)) {
            if (upgrade.getItem() instanceof RestockUpgradeItem) {
                return new RestockTarget(stack, backpackItem.getTier(), saver);
            }
        }

        return null;
    }

    private static int moveMatchingItems(ServerPlayer player, Container container, ItemStack backpack, BackpackTier tier) {
        int movedItems = 0;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty() || !BackpackStackData.isValidStorageItem(backpack, stack) || !backpackHasMatch(backpack, tier, stack)) {
                continue;
            }

            if (!ItemLockProtection.canRestockInsert(backpack, stack)) {
                continue;
            }

            ItemStack remainder = ItemLockProtection.insertIntoStorage(backpack, tier, stack, false);
            int moved = stack.getCount() - remainder.getCount();
            if (moved <= 0) {
                continue;
            }

            movedItems += moved;
            PickupNotifierServer.reportInserted(player, backpack, tier, stack.copyWithCount(moved),
                    PickupNotifierDestination.MAIN_STORAGE, PickupNotifierSource.CONTAINER_TRANSFER);
            container.setItem(slot, remainder);
        }
        return movedItems;
    }

    private static boolean backpackHasMatch(ItemStack backpack, BackpackTier tier, ItemStack stack) {
        for (ItemStack stored : BackpackStackData.loadStorage(backpack, tier)) {
            if (!stored.isEmpty() && ItemStack.isSameItemSameComponents(stored, stack)) {
                return true;
            }
        }
        return false;
    }

    private record RestockTarget(ItemStack backpack, BackpackTier tier, Consumer<ItemStack> save) {
    }
}
