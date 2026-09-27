package com.teamsmartstreamlabs.smartbackpacks.menu;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BrewingStandUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BrewingStandUpgradeLogic;

import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

public class PortableBrewingStandMenu extends BrewingStandMenu {
    private final Player owner;
    private final BackpackAccess access;
    private final int upgradeSlot;
    private final SimpleContainer container;
    private final ContainerData data;

    public PortableBrewingStandMenu(int containerId, Inventory playerInventory, BackpackAccess access, int upgradeSlot) {
        this(containerId, playerInventory, access, upgradeSlot, new SimpleContainer(5), new SimpleContainerData(2));
    }

    private PortableBrewingStandMenu(int containerId, Inventory playerInventory, BackpackAccess access, int upgradeSlot, SimpleContainer container, ContainerData data) {
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

    public void tickServer(Level level) {
        BrewingStandUpgradeData updated = BrewingStandUpgradeLogic.tick(level, this.captureData());
        this.applyData(updated);
        this.saveToUpgrade();
        this.broadcastChanges();
    }

    @Override
    public void removed(Player player) {
        this.saveToUpgrade();
        super.removed(player);
    }

    @Override
    public void slotsChanged(net.minecraft.world.Container inventory) {
        super.slotsChanged(inventory);
        this.saveToUpgrade();
    }

    private void loadFromUpgrade() {
        BrewingStandUpgradeData brewingData = this.getUpgradeStack()
                .getOrDefault(ModDataComponents.BREWING_STAND_UPGRADE_DATA.get(), BrewingStandUpgradeData.DEFAULT);
        this.applyData(brewingData);
    }

    private void applyData(BrewingStandUpgradeData brewingData) {
        NonNullList<ItemStack> items = BrewingStandUpgradeLogic.loadItems(brewingData);
        for (int slot = 0; slot < BrewingStandUpgradeData.SLOT_COUNT; slot++) {
            this.container.setItem(slot, items.get(slot));
        }
        this.data.set(0, brewingData.brewTime());
        this.data.set(1, brewingData.fuel());
    }

    private BrewingStandUpgradeData captureData() {
        NonNullList<ItemStack> items = NonNullList.withSize(BrewingStandUpgradeData.SLOT_COUNT, ItemStack.EMPTY);
        for (int slot = 0; slot < BrewingStandUpgradeData.SLOT_COUNT; slot++) {
            items.set(slot, this.container.getItem(slot).copy());
        }
        return new BrewingStandUpgradeData(
                ItemContainerContents.fromItems(items),
                this.data.get(0),
                this.data.get(1));
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

        upgrade.set(ModDataComponents.BREWING_STAND_UPGRADE_DATA.get(), this.captureData());
        upgrades.set(this.upgradeSlot, upgrade);
        BackpackStackData.saveUpgrades(backpack, upgrades);
        this.access.setBackpackStack(this.owner, backpack);
        this.owner.getInventory().setChanged();
    }
}
