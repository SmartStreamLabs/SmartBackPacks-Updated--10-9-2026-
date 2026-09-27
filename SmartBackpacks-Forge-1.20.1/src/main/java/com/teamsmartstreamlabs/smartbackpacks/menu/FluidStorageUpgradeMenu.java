package com.teamsmartstreamlabs.smartbackpacks.menu;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackUpgradeInventory;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FluidStorageUpgradeHandler;

import net.minecraft.core.NonNullList;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

public class FluidStorageUpgradeMenu extends AbstractContainerMenu {
    private final Player owner;
    private final BackpackAccess access;
    private final BackpackUpgradeInventory upgradeInventory;
    private final int upgradeSlotIndex;
    private final SimpleContainer containerSlot = new SimpleContainer(1);
    private final ContainerData data = new SimpleContainerData(3);

    public FluidStorageUpgradeMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, BackpackAccess.fromNetwork(playerInventory.player, buffer), buffer.readVarInt());
    }

    public FluidStorageUpgradeMenu(int containerId, Inventory playerInventory, BackpackAccess access, int upgradeSlotIndex) {
        super(ModMenuTypes.FLUID_STORAGE_UPGRADE.get(), containerId);
        this.owner = playerInventory.player;
        this.access = access;
        this.upgradeInventory = new BackpackUpgradeInventory(playerInventory.player, access);
        this.upgradeSlotIndex = upgradeSlotIndex;

        this.addDataSlots(this.data);
        this.refreshData();
        this.addSlot(new FluidContainerSlot(this.containerSlot, 0, 14, 34));
        this.addPlayerInventory(playerInventory);
    }

    public int getFluidAmount() {
        return this.data.get(0);
    }

    public int getCapacity() {
        return this.data.get(1);
    }

    public int getFluidId() {
        return this.data.get(2);
    }

    public FluidStack getDisplayedFluid() {
        if (this.getFluidAmount() <= 0 || this.getFluidId() < 0) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(BuiltInRegistries.FLUID.byId(this.getFluidId()), this.getFluidAmount());
    }

    public void fillTankFromContainer() {
        int beforeFluid = this.getFluidAmount();
        ItemStack container = this.containerSlot.getItem(0);
        if (container.isEmpty()) {
            return;
        }

        ItemStack upgrade = this.getUpgradeStack().copy();
        var result = FluidUtil.tryEmptyContainer(container, new SingleTankHandler(upgrade), Integer.MAX_VALUE, null, true);
        if (!result.isSuccess()) {
            return;
        }

        this.containerSlot.setItem(0, result.getResult());
        this.saveUpgradeStack(upgrade);
        if (this.owner instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
            com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.add(serverPlayer, "fluid_stored_mb", Math.max(0L, (long) this.getFluidAmount() - beforeFluid));
    }

    public void fillContainerFromTank() {
        ItemStack container = this.containerSlot.getItem(0);
        if (container.isEmpty()) {
            return;
        }

        ItemStack upgrade = this.getUpgradeStack().copy();
        var result = FluidUtil.tryFillContainer(container, new SingleTankHandler(upgrade), Integer.MAX_VALUE, null, true);
        if (!result.isSuccess()) {
            return;
        }

        this.containerSlot.setItem(0, result.getResult());
        this.saveUpgradeStack(upgrade);
    }

    @Override
    public void removed(Player player) {
        ItemStack container = this.containerSlot.removeItemNoUpdate(0);
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
        } else if (this.isFluidContainer(sourceStack)) {
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
        return this.upgradeInventory.stillValid(player);
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

    private boolean isFluidContainer(ItemStack stack) {
        return FluidUtil.getFluidHandler(stack.copyWithCount(1)).isPresent()
                || ItemStackCompat.has(stack, DataComponents.BUCKET_ENTITY_DATA)
                || ItemStackCompat.has(stack, DataComponents.CONTAINER);
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
        FluidStack fluid = FluidStorageUpgradeHandler.getFluid(this.getUpgradeStack());
        this.data.set(0, fluid.getAmount());
        this.data.set(1, FluidStorageUpgradeHandler.CAPACITY);
        this.data.set(2, fluid.isEmpty() ? -1 : BuiltInRegistries.FLUID.getId(fluid.getFluid()));
    }

    private static final class FluidContainerSlot extends Slot {
        private FluidContainerSlot(SimpleContainer container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return FluidUtil.getFluidHandler(stack.copyWithCount(1)).isPresent();
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private static final class SingleTankHandler implements net.neoforged.neoforge.fluids.capability.IFluidHandler {
        private final ItemStack upgradeStack;

        private SingleTankHandler(ItemStack upgradeStack) {
            this.upgradeStack = upgradeStack;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? FluidStorageUpgradeHandler.getFluid(this.upgradeStack) : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? FluidStorageUpgradeHandler.CAPACITY : 0;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && !stack.isEmpty() && !stack.is(Fluids.EMPTY);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return FluidStorageUpgradeHandler.fill(this.upgradeStack, resource, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStorageUpgradeHandler.drain(this.upgradeStack, resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStorageUpgradeHandler.drain(this.upgradeStack, maxDrain, action);
        }
    }
}
