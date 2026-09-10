package com.teamsmartstreamlabs.smartbackpacks.compat;

import eu.pb4.trinkets.api.DefaultTrinketSlots;
import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketInventory;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import java.util.Map;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

final class TrinketsCompat {
    private TrinketsCompat() {
    }

    static int getBackSlotCount(Player player) {
        TrinketInventory inventory = getBackInventory(player);
        return inventory == null ? 0 : inventory.getContainerSize();
    }

    static ItemStack getBackStack(Player player, int slot) {
        TrinketInventory inventory = getBackInventory(player);
        return inventory != null && slot >= 0 && slot < inventory.getContainerSize()
                ? inventory.getItem(slot)
                : ItemStack.EMPTY;
    }

    static void setBackStack(Player player, int slot, ItemStack stack) {
        TrinketInventory inventory = getBackInventory(player);
        if (inventory != null && slot >= 0 && slot < inventory.getContainerSize()) {
            inventory.getSlotAccess(slot).set(stack);
            inventory.setChanged();
        }
    }

    static boolean insertIntoFirstEmptyBackSlot(Player player, ItemStack stack) {
        TrinketInventory inventory = getBackInventory(player);
        if (inventory == null || stack.isEmpty()) {
            return false;
        }

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            TrinketSlotAccess access = inventory.getSlotAccess(slot);
            if (!access.isValid() || !access.get().isEmpty()
                    || !access.slotType().validatorCheck(stack, access, player)) {
                continue;
            }
            if (access.set(stack.copy())) {
                inventory.setChanged();
                return true;
            }
        }
        return false;
    }

    private static TrinketInventory getBackInventory(Player player) {
        TrinketAttachment attachment = TrinketsApi.getAttachment(player);
        if (attachment == null) {
            return null;
        }

        TrinketInventory inventory = attachment.getInventory(DefaultTrinketSlots.CHEST_BACK);
        if (inventory != null) {
            return inventory;
        }

        Map<String, TrinketInventory> inventories = attachment.getInventories();
        inventory = inventories.get(DefaultTrinketSlots.CHEST_BACK);
        if (inventory != null) {
            return inventory;
        }

        return inventories.values().stream()
                .filter(candidate -> DefaultTrinketSlots.CHEST_BACK.equals(candidate.slotType().getId()))
                .findFirst()
                .orElse(null);
    }
}
