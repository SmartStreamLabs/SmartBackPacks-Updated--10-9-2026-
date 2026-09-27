package com.teamsmartstreamlabs.smartbackpacks.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class PortableAnvilMenu extends AnvilMenu {
    public PortableAnvilMenu(int containerId, Inventory playerInventory) {
        super(containerId, playerInventory,
                ContainerLevelAccess.create(playerInventory.player.level(), playerInventory.player.blockPosition()));
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
