package com.teamsmartstreamlabs.smartbackpacks.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SmithingMenu;

public class PortableSmithingTableMenu extends SmithingMenu {
    public PortableSmithingTableMenu(int containerId, Inventory playerInventory) {
        super(containerId, playerInventory,
                ContainerLevelAccess.create(playerInventory.player.level(), playerInventory.player.blockPosition()));
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
