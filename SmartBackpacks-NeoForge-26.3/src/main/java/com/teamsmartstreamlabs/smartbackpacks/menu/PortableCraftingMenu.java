package com.teamsmartstreamlabs.smartbackpacks.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;

public class PortableCraftingMenu extends CraftingMenu {
    public PortableCraftingMenu(int containerId, Inventory playerInventory) {
        super(containerId, playerInventory,
                ContainerLevelAccess.create(playerInventory.player.level(), playerInventory.player.blockPosition()));
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        if (!player.level().isClientSide()) {
            for (int slot = 1; slot <= 9; slot++) {
                ItemStack stack = this.getSlot(slot).getItem().copy();
                if (!stack.isEmpty() && !player.getInventory().add(stack)) {
                    player.drop(stack, false, net.minecraft.util.Prediction.SERVER_ONLY);
                }
                this.getSlot(slot).set(ItemStack.EMPTY);
            }

            this.getSlot(0).set(ItemStack.EMPTY);
        }

        super.removed(player);
    }
}
