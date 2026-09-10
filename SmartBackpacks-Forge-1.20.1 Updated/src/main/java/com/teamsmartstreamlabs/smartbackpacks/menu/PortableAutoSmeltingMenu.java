package com.teamsmartstreamlabs.smartbackpacks.menu;


import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.AutoSmeltingUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.AutoSmeltingUpgradeLogic;

import net.minecraft.core.NonNullList;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PortableAutoSmeltingMenu extends FurnaceMenu {
    private final Player owner;
    private final BackpackAccess access;
    private final int upgradeSlot;
    private final SimpleContainer container;
    private final SimpleContainerData data;

    public PortableAutoSmeltingMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, BackpackAccess.fromNetwork(playerInventory.player, buffer), buffer.readVarInt());
    }

    public PortableAutoSmeltingMenu(int containerId, Inventory playerInventory, BackpackAccess access, int upgradeSlot) {
        this(containerId, playerInventory, access, upgradeSlot, new SimpleContainer(3), new SimpleContainerData(4));
    }

    private PortableAutoSmeltingMenu(int containerId, Inventory playerInventory, BackpackAccess access, int upgradeSlot, SimpleContainer container, SimpleContainerData data) {
        super(containerId, playerInventory, container, data);
        this.owner = playerInventory.player;
        this.access = access;
        this.upgradeSlot = upgradeSlot;
        this.container = container;
        this.data = data;
        this.loadFromUpgrade();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void slotsChanged(net.minecraft.world.Container inventory) {
        super.slotsChanged(inventory);
        this.saveToUpgrade();
    }

    public void tickServer(Level level) {
        ItemStack backpack = this.access.getBackpackStack(this.owner);
        if (backpack.isEmpty() || !(backpack.getItem() instanceof BackpackItem backpackItem)) {
            return;
        }

        AutoSmeltingUpgradeData updated = AutoSmeltingUpgradeLogic.tick(level, backpack, backpackItem.getTier(), this.captureData());
        this.applyData(updated);
        this.access.setBackpackStack(this.owner, backpack);
        this.saveToUpgrade();
        this.broadcastChanges();
    }

    @Override
    public void removed(Player player) {
        this.saveToUpgrade();
        super.removed(player);
    }

    private void loadFromUpgrade() {
        AutoSmeltingUpgradeData upgradeData = com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.getOrDefault(this.getUpgradeStack(), ModDataComponents.AUTO_SMELTING_UPGRADE_DATA.get(), AutoSmeltingUpgradeData.DEFAULT);
        this.applyData(upgradeData);
    }

    private void applyData(AutoSmeltingUpgradeData upgradeData) {
        NonNullList<ItemStack> items = AutoSmeltingUpgradeLogic.loadItems(upgradeData);
        for (int slot = 0; slot < AutoSmeltingUpgradeData.SLOT_COUNT; slot++) {
            this.container.setItem(slot, items.get(slot));
        }
        this.data.set(0, upgradeData.litTime());
        this.data.set(1, upgradeData.litDuration());
        this.data.set(2, upgradeData.cookingProgress());
        this.data.set(3, upgradeData.cookingTotalTime());
    }

    private AutoSmeltingUpgradeData captureData() {
        NonNullList<ItemStack> items = NonNullList.withSize(AutoSmeltingUpgradeData.SLOT_COUNT, ItemStack.EMPTY);
        for (int slot = 0; slot < AutoSmeltingUpgradeData.SLOT_COUNT; slot++) {
            items.set(slot, this.container.getItem(slot).copy());
        }
        return new AutoSmeltingUpgradeData(
                com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.component.ItemContainerContents.fromItems(items),
                this.data.get(0),
                this.data.get(1),
                this.data.get(2),
                this.data.get(3));
    }

    private ItemStack getUpgradeStack() {
        ItemStack backpack = this.access.getBackpackStack(this.owner);
        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        return this.upgradeSlot >= 0 && this.upgradeSlot < upgrades.size() ? upgrades.get(this.upgradeSlot) : ItemStack.EMPTY;
    }

    private void saveToUpgrade() {
        ItemStack backpack = this.access.getBackpackStack(this.owner);
        if (backpack.isEmpty()) {
            return;
        }

        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        if (this.upgradeSlot < 0 || this.upgradeSlot >= upgrades.size()) {
            return;
        }

        ItemStack upgrade = upgrades.get(this.upgradeSlot).copy();
        if (upgrade.isEmpty()) {
            return;
        }

        com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.set(upgrade, ModDataComponents.AUTO_SMELTING_UPGRADE_DATA.get(), this.captureData());
        upgrades.set(this.upgradeSlot, upgrade);
        BackpackStackData.saveUpgrades(backpack, upgrades);
        this.access.setBackpackStack(this.owner, backpack);
        this.owner.getInventory().setChanged();
    }
}


