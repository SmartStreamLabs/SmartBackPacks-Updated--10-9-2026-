package com.teamsmartstreamlabs.smartbackpacks.backpack;

import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class BackpackCraftingHandler {
    private BackpackCraftingHandler() {
    }

    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack craftedStack = event.getCrafting();
        if (!(craftedStack.getItem() instanceof BackpackItem craftedBackpack)) {
            return;
        }

        ItemStack sourceBackpack = findBackpackIngredient(event.getInventory(), craftedBackpack.getTier());
        if (sourceBackpack.isEmpty()) {
            return;
        }

        BackpackItem.copyStorageAndAppearance(sourceBackpack, craftedStack);
    }

    public static void preserveUpgradeData(ItemStack craftedStack, Player player) {
        if (player.level().isClientSide() || !(craftedStack.getItem() instanceof BackpackItem craftedBackpack)) {
            return;
        }

        for (Slot slot : player.containerMenu.slots) {
            if (!(slot.container instanceof CraftingContainer)) {
                continue;
            }

            ItemStack sourceBackpack = slot.getItem();
            if (isUpgradeSource(sourceBackpack, craftedBackpack.getTier())) {
                BackpackItem.copyStorageAndAppearance(sourceBackpack, craftedStack);
                return;
            }
        }
    }

    private static ItemStack findBackpackIngredient(Container container, BackpackTier resultTier) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (!(stack.getItem() instanceof BackpackItem)) {
                continue;
            }

            if (isUpgradeSource(stack, resultTier)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static boolean isUpgradeSource(ItemStack stack, BackpackTier resultTier) {
        return stack.getItem() instanceof BackpackItem backpackItem
                && backpackItem.getTier() != resultTier
                && backpackItem.getTier().getSlotCount() <= resultTier.getSlotCount();
    }
}
