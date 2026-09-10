package com.teamsmartstreamlabs.smartbackpacks.menu;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackUpgradeInventory;
import com.teamsmartstreamlabs.smartbackpacks.item.ChunkLoaderUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.ChunkLoaderUpgradeData;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class ChunkLoaderUpgradeMenu extends AbstractContainerMenu {
    private final BackpackUpgradeInventory upgradeInventory;
    private final int upgradeSlotIndex;
    private int radius;

    public ChunkLoaderUpgradeMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, BackpackAccess.fromNetwork(playerInventory.player, buffer), buffer.readVarInt());
    }

    public ChunkLoaderUpgradeMenu(int containerId, Inventory playerInventory, BackpackAccess access, int upgradeSlotIndex) {
        super(ModMenuTypes.CHUNK_LOADER_UPGRADE.get(), containerId);
        this.upgradeInventory = new BackpackUpgradeInventory(playerInventory.player, access);
        this.upgradeSlotIndex = upgradeSlotIndex;
        this.loadUpgradeData();
    }

    public int getRadius() {
        return this.radius;
    }

    public int getLoadedChunkCount() {
        int diameter = this.radius * 2 + 1;
        return diameter * diameter;
    }

    public void setRadius(int radius) {
        this.radius = Mth.clamp(radius, ChunkLoaderUpgradeData.DEFAULT_RADIUS, ChunkLoaderUpgradeData.MAX_RADIUS);
        this.saveUpgradeData();
    }

    public void adjustRadius(int delta) {
        this.setRadius(this.radius + delta);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.upgradeInventory.stillValid(player) && this.getUpgradeStack().getItem() instanceof ChunkLoaderUpgradeItem;
    }

    private void loadUpgradeData() {
        this.radius = this.getUpgradeStack()
                .getOrDefault(ModDataComponents.CHUNK_LOADER_UPGRADE_DATA.get(), ChunkLoaderUpgradeData.DEFAULT)
                .radius();
    }

    private void saveUpgradeData() {
        ItemStack upgradeStack = this.getUpgradeStack();
        if (!(upgradeStack.getItem() instanceof ChunkLoaderUpgradeItem)) {
            return;
        }

        upgradeStack.set(ModDataComponents.CHUNK_LOADER_UPGRADE_DATA.get(), new ChunkLoaderUpgradeData(this.radius));
        this.upgradeInventory.setItem(this.upgradeSlotIndex, upgradeStack);
        this.broadcastChanges();
    }

    private ItemStack getUpgradeStack() {
        if (this.upgradeSlotIndex < 0 || this.upgradeSlotIndex >= this.upgradeInventory.getContainerSize()) {
            return ItemStack.EMPTY;
        }
        return this.upgradeInventory.getItem(this.upgradeSlotIndex);
    }
}
