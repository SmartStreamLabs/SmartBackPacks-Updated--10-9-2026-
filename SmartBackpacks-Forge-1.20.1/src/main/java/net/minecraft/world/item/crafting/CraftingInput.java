package com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;

public class CraftingInput extends TransientCraftingContainer {
    private CraftingInput(int width, int height, NonNullList<ItemStack> items) {
        super(DummyMenu.INSTANCE, width, height, items);
    }

    public static CraftingInput of(int width, int height, NonNullList<ItemStack> items) {
        return new CraftingInput(width, height, items);
    }

    private static final class DummyMenu extends AbstractContainerMenu {
        private static final DummyMenu INSTANCE = new DummyMenu();

        private DummyMenu() {
            super(null, -1);
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            return ItemStack.EMPTY;
        }
    }
}
