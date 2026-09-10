package com.teamsmartstreamlabs.smartbackpacks.menu;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackUpgradeInventory;
import com.teamsmartstreamlabs.smartbackpacks.item.CapacitorUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacitorUpgradeHandler;

import net.minecraft.core.NonNullList;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class CapacitorUpgradeMenu extends AbstractContainerMenu {
    private final Player owner;
    private final BackpackAccess access;
    private final BackpackUpgradeInventory upgradeInventory;
    private final int upgradeSlotIndex;
    private final SimpleContainer energyItemSlot = new SimpleContainer(1);
    private final ContainerData data = new SimpleContainerData(2);

    public CapacitorUpgradeMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, BackpackAccess.fromNetwork(playerInventory.player, buffer), buffer.readVarInt());
    }

    public CapacitorUpgradeMenu(int containerId, Inventory playerInventory, BackpackAccess access, int upgradeSlotIndex) {
        super(ModMenuTypes.CAPACITOR_UPGRADE.get(), containerId);
        this.owner = playerInventory.player;
        this.access = access;
        this.upgradeInventory = new BackpackUpgradeInventory(playerInventory.player, access);
        this.upgradeSlotIndex = upgradeSlotIndex;

        this.addDataSlots(this.data);
        this.refreshData();
        this.addSlot(new EnergyItemSlot(this.energyItemSlot, 0, 14, 38));
        this.addPlayerInventory(playerInventory);
    }

    public int getEnergyStored() {
        return this.data.get(0);
    }

    public int getCapacity() {
        return this.data.get(1);
    }

    public void chargeItemFromCapacitor() {
        ItemStack container = this.energyItemSlot.getItem(0);
        if (container.isEmpty()) {
            return;
        }

        IEnergyStorage itemStorage = Capabilities.EnergyStorage.ITEM.getCapability(container, null);
        if (itemStorage == null) {
            return;
        }

        ItemStack upgradeStack = this.getUpgradeStack().copy();
        if (!(upgradeStack.getItem() instanceof CapacitorUpgradeItem)) {
            return;
        }

        int available = CapacitorUpgradeHandler.extractEnergy(upgradeStack, CapacitorUpgradeHandler.TRANSFER_RATE, true);
        if (available <= 0) {
            return;
        }

        int received = itemStorage.receiveEnergy(available, false);
        if (received <= 0) {
            return;
        }

        CapacitorUpgradeHandler.extractEnergy(upgradeStack, received, false);
        this.energyItemSlot.setChanged();
        this.saveUpgradeStack(upgradeStack);
    }

    public void storeItemEnergyInCapacitor() {
        ItemStack container = this.energyItemSlot.getItem(0);
        if (container.isEmpty()) {
            return;
        }

        IEnergyStorage itemStorage = Capabilities.EnergyStorage.ITEM.getCapability(container, null);
        if (itemStorage == null) {
            return;
        }

        ItemStack upgradeStack = this.getUpgradeStack().copy();
        if (!(upgradeStack.getItem() instanceof CapacitorUpgradeItem)) {
            return;
        }

        int space = CapacitorUpgradeHandler.receiveEnergy(upgradeStack, CapacitorUpgradeHandler.TRANSFER_RATE, true);
        if (space <= 0) {
            return;
        }

        int extracted = itemStorage.extractEnergy(space, false);
        if (extracted <= 0) {
            return;
        }

        CapacitorUpgradeHandler.receiveEnergy(upgradeStack, extracted, false);
        this.energyItemSlot.setChanged();
        this.saveUpgradeStack(upgradeStack);
    }

    @Override
    public void removed(Player player) {
        ItemStack container = this.energyItemSlot.removeItemNoUpdate(0);
        if (!container.isEmpty()) {
            player.getInventory().placeItemBackInInventory(container);
        }
        super.removed(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = this.slots.get(index);
        if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack sourceCopy = sourceStack.copy();

        if (index == 0) {
            if (!this.moveItemStackTo(sourceStack, 1, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (this.isEnergyItem(sourceStack)) {
            if (!this.moveItemStackTo(sourceStack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        return sourceCopy;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.upgradeInventory.stillValid(player) && this.getUpgradeStack().getItem() instanceof CapacitorUpgradeItem;
    }

    @Override
    public void broadcastChanges() {
        this.refreshData();
        super.broadcastChanges();
    }

    private void addPlayerInventory(Inventory playerInventory) {
        int playerInventoryY = 96;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int index = column + row * 9 + 9;
                this.addSlot(new Slot(playerInventory, index, 8 + column * 18, playerInventoryY + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column, 8 + column * 18, playerInventoryY + 58));
        }
    }

    private boolean isEnergyItem(ItemStack stack) {
        return Capabilities.EnergyStorage.ITEM.getCapability(stack.copyWithCount(1), null) != null;
    }

    private ItemStack getUpgradeStack() {
        ItemStack backpack = this.access.getBackpackStack(this.owner);
        if (backpack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        if (this.upgradeSlotIndex < 0 || this.upgradeSlotIndex >= upgrades.size()) {
            return ItemStack.EMPTY;
        }

        return upgrades.get(this.upgradeSlotIndex);
    }

    private void saveUpgradeStack(ItemStack upgradeStack) {
        ItemStack backpack = this.access.getBackpackStack(this.owner);
        if (backpack.isEmpty()) {
            return;
        }

        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        if (this.upgradeSlotIndex < 0 || this.upgradeSlotIndex >= upgrades.size()) {
            return;
        }

        upgrades.set(this.upgradeSlotIndex, upgradeStack);
        BackpackStackData.saveUpgrades(backpack, upgrades);
        this.access.setBackpackStack(this.owner, backpack);
        this.owner.getInventory().setChanged();
        this.refreshData();
        this.broadcastChanges();
    }

    private void refreshData() {
        ItemStack upgradeStack = this.getUpgradeStack();
        this.data.set(0, CapacitorUpgradeHandler.getEnergyStored(upgradeStack));
        this.data.set(1, CapacitorUpgradeHandler.CAPACITY);
    }

    private static final class EnergyItemSlot extends Slot {
        private EnergyItemSlot(SimpleContainer container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return Capabilities.EnergyStorage.ITEM.getCapability(stack.copyWithCount(1), null) != null;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
