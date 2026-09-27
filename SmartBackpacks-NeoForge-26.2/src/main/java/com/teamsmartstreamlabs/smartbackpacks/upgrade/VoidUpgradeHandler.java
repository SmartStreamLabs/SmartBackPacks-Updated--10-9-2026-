package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.VoidUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class VoidUpgradeHandler {
    private VoidUpgradeHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || player.tickCount % 10 != 0) {
            return;
        }

        for (int slot = 0; slot < 36; slot++) {
            tickVoidInBackpack(player, player.getInventory().getItem(slot), backpack -> player.getInventory().setChanged());
        }

        tickVoidInBackpack(player, player.getOffhandItem(), backpack -> player.getInventory().setChanged());
        tickVoidInBackpack(player, player.getItemBySlot(EquipmentSlot.CHEST), backpack -> player.getInventory().setChanged());

        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            int curioSlot = slot;
            tickVoidInBackpack(player, CuriosCompat.getBackStack(player, curioSlot),
                    backpack -> CuriosCompat.setBackStack(player, curioSlot, backpack));
        }
    }

    public static void tickVoidInBackpack(ServerPlayer player, ItemStack backpack, Consumer<ItemStack> saver) {
        if (!(backpack.getItem() instanceof BackpackItem backpackItem)) {
            return;
        }

        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        NonNullList<ItemStack> storage = BackpackStackData.loadStorage(backpack, backpackItem.getTier());
        boolean changed = false;
        long removed = 0;

        for (ItemStack upgrade : upgrades) {
            if (!(upgrade.getItem() instanceof VoidUpgradeItem)) {
                continue;
            }

            VoidUpgradeData data = upgrade.getOrDefault(ModDataComponents.VOID_UPGRADE_DATA.get(), VoidUpgradeData.DEFAULT);
            if (!data.enabled()) {
                continue;
            }

            if (data.onlyWhenFull() && hasAvailableSpace(backpack, storage)) {
                continue;
            }

            long count = voidMatchingItems(backpack, storage, data);
            removed += count;
            if (count > 0) {
                changed = true;
            }
        }

        if (changed) {
            BackpackStackData.saveStorage(backpack, storage);
            saver.accept(backpack);
            com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.add(player, "voided_item_count", removed);
        }
    }

    private static long voidMatchingItems(ItemStack backpack, NonNullList<ItemStack> storage, VoidUpgradeData data) {
        long removed = 0;
        for (int slot = 0; slot < storage.size(); slot++) {
            ItemStack stack = storage.get(slot);
            if (stack.isEmpty()
                    || !VoidFilterMatcher.allows(data, stack)
                    || !ItemLockProtection.canVoidStack(backpack, slot, stack)) {
                continue;
            }

            removed += stack.getCount();
            storage.set(slot, ItemStack.EMPTY);
        }
        return removed;
    }

    private static boolean hasAvailableSpace(ItemStack backpack, NonNullList<ItemStack> storage) {
        for (ItemStack stack : storage) {
            if (stack.isEmpty() || stack.getCount() < BackpackStackData.getStorageStackLimit(backpack, stack)) {
                return true;
            }
        }
        return false;
    }
}
