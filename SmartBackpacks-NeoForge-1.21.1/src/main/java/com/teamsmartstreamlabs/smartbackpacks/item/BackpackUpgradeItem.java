package com.teamsmartstreamlabs.smartbackpacks.item;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BackpackUpgradeItem extends Item {
    public BackpackUpgradeItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public boolean onInstalledRightClicked(ServerPlayer player, BackpackAccess access, int upgradeSlot, ItemStack stack) {
        return false;
    }

    public boolean onInstalledRightClicked(ServerPlayer player, BackpackAccess access, int upgradeSlot, ItemStack stack, boolean shiftDown) {
        return this.onInstalledRightClicked(player, access, upgradeSlot, stack);
    }
}

