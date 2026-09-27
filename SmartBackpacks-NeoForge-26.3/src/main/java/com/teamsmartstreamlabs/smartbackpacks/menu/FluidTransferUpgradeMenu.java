package com.teamsmartstreamlabs.smartbackpacks.menu;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackUpgradeInventory;
import com.teamsmartstreamlabs.smartbackpacks.item.FluidTransferUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FluidStorageUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FluidTransferUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FluidTransferUpgradeHandler;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import com.teamsmartstreamlabs.smartbackpacks.compat.legacy.FluidUtil;

public class FluidTransferUpgradeMenu extends AbstractContainerMenu {
    private final Player owner;
    private final BackpackAccess access;
    private final BackpackUpgradeInventory upgradeInventory;
    private final int upgradeSlotIndex;
    private final SimpleContainer containerSlot = new SimpleContainer(1);
    private final SimpleContainer filterItems = new SimpleContainer(FluidTransferUpgradeData.FILTER_SLOT_COUNT);
    private final ContainerData data = new SimpleContainerData(5);
    private boolean pushMode;
    private boolean collectSourceBlocks;

    public FluidTransferUpgradeMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, BackpackAccess.fromNetwork(playerInventory.player, buffer), buffer.readVarInt());
    }

    public FluidTransferUpgradeMenu(int containerId, Inventory playerInventory, BackpackAccess access, int upgradeSlotIndex) {
        super(ModMenuTypes.FLUID_TRANSFER_UPGRADE.get(), containerId);
        this.owner = playerInventory.player;
        this.access = access;
        this.upgradeInventory = new BackpackUpgradeInventory(playerInventory.player, access);
        this.upgradeSlotIndex = upgradeSlotIndex;

        this.addDataSlots(this.data);
        this.loadUpgradeData();
        this.refreshRuntimeData();
        this.addSlot(new FluidContainerSlot(this.containerSlot, 0, 14, 34));
        this.addFilterSlots();
        this.addPlayerInventory(playerInventory);
    }

    public boolean isPushMode() {
        return this.pushMode;
    }

    public boolean isCollectSourceBlocks() {
        return this.collectSourceBlocks;
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

    public void toggleMode() {
        this.pushMode = !this.pushMode;
        this.saveUpgradeData();
    }

    public void toggleCollectSourceBlocks() {
        this.collectSourceBlocks = !this.collectSourceBlocks;
        this.saveUpgradeData();
    }

    public void fillTankFromContainer() {
        int beforeFluid = this.getFluidAmount();
        ItemStack container = this.containerSlot.getItem(0);
        if (container.isEmpty()) {
            return;
        }

        ItemStack tankUpgrade = this.getTankUpgradeStack().copy();
        if (tankUpgrade.isEmpty()) {
            return;
        }

        var result = FluidUtil.tryEmptyContainer(container, new SingleTankHandler(tankUpgrade), Integer.MAX_VALUE, null, true);
        if (!result.isSuccess()) {
            return;
        }

        this.containerSlot.setItem(0, result.getResult());
        this.saveTankUpgradeStack(tankUpgrade);
        if (this.owner instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
            com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.add(serverPlayer, "fluid_stored_mb", Math.max(0L, (long) this.getFluidAmount() - beforeFluid));
    }

    public void fillContainerFromTank() {
        ItemStack container = this.containerSlot.getItem(0);
        if (container.isEmpty()) {
            return;
        }

        ItemStack tankUpgrade = this.getTankUpgradeStack().copy();
        if (tankUpgrade.isEmpty()) {
            return;
        }

        var result = FluidUtil.tryFillContainer(container, new SingleTankHandler(tankUpgrade), Integer.MAX_VALUE, null, true);
        if (!result.isSuccess()) {
            return;
        }

        this.containerSlot.setItem(0, result.getResult());
        this.saveTankUpgradeStack(tankUpgrade);
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput ContainerInput, Player player) {
        if (slotId >= 1 && slotId < 1 + FluidTransferUpgradeData.FILTER_SLOT_COUNT && ContainerInput == ContainerInput.PICKUP) {
            ItemStack carried = this.getCarried();
            int filterSlot = slotId - 1;
            if (carried.isEmpty()) {
                this.filterItems.setItem(filterSlot, ItemStack.EMPTY);
            } else if (FluidUtil.getFluidContained(carried.copyWithCount(1)).isPresent()) {
                this.filterItems.setItem(filterSlot, carried.copyWithCount(1));
            }
            this.saveUpgradeData();
            return;
        }

        super.clicked(slotId, button, ContainerInput, player);
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
            if (!this.moveItemStackTo(sourceStack, 1 + FluidTransferUpgradeData.FILTER_SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (index > FluidTransferUpgradeData.FILTER_SLOT_COUNT && this.isFluidContainer(sourceStack)) {
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
    public void removed(Player player) {
        ItemStack container = this.containerSlot.removeItemNoUpdate(0);
        if (!container.isEmpty()) {
            player.getInventory().placeItemBackInInventory(container, net.minecraft.util.Prediction.SERVER_ONLY);
        }
        super.removed(player);
        this.saveUpgradeData();
    }

    @Override
    public void broadcastChanges() {
        this.refreshRuntimeData();
        super.broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.upgradeInventory.stillValid(player) && this.getUpgradeStack().getItem() instanceof FluidTransferUpgradeItem;
    }

    private void addFilterSlots() {
        for (int slot = 0; slot < FluidTransferUpgradeData.FILTER_SLOT_COUNT; slot++) {
            int x = 118 + (slot % 2) * 18;
            int y = 20 + (slot / 2) * 18;
            this.addSlot(new GhostFluidFilterSlot(this.filterItems, slot, x, y));
        }
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

    private void loadUpgradeData() {
        FluidTransferUpgradeData data = FluidTransferUpgradeHandler.getData(this.getUpgradeStack());
        NonNullList<ItemStack> filterStacks = NonNullList.withSize(FluidTransferUpgradeData.FILTER_SLOT_COUNT, ItemStack.EMPTY);
        data.fluidFilters().copyInto(filterStacks);
        for (int slot = 0; slot < FluidTransferUpgradeData.FILTER_SLOT_COUNT; slot++) {
            this.filterItems.setItem(slot, filterStacks.get(slot));
        }
        this.pushMode = data.pushMode();
        this.collectSourceBlocks = data.collectSourceBlocks();
    }

    private void saveUpgradeData() {
        ItemStack upgradeStack = this.getUpgradeStack();
        if (!(upgradeStack.getItem() instanceof FluidTransferUpgradeItem)) {
            return;
        }

        NonNullList<ItemStack> filterStacks = NonNullList.withSize(FluidTransferUpgradeData.FILTER_SLOT_COUNT, ItemStack.EMPTY);
        for (int slot = 0; slot < FluidTransferUpgradeData.FILTER_SLOT_COUNT; slot++) {
            filterStacks.set(slot, this.filterItems.getItem(slot));
        }

        upgradeStack.set(ModDataComponents.FLUID_TRANSFER_UPGRADE_DATA.get(),
                new FluidTransferUpgradeData(this.pushMode, this.collectSourceBlocks, ItemContainerContents.fromItems(filterStacks)));
        this.upgradeInventory.setItem(this.upgradeSlotIndex, upgradeStack);
        this.refreshRuntimeData();
        this.broadcastChanges();
    }

    private boolean isFluidContainer(ItemStack stack) {
        return FluidUtil.getFluidHandler(stack.copyWithCount(1)).isPresent()
                || stack.has(DataComponents.BUCKET_ENTITY_DATA)
                || stack.has(DataComponents.CONTAINER);
    }

    private void refreshRuntimeData() {
        FluidStack fluid = FluidStorageUpgradeHandler.getFluid(this.getTankUpgradeStack());
        this.data.set(0, fluid.getAmount());
        this.data.set(1, FluidStorageUpgradeHandler.CAPACITY);
        this.data.set(2, fluid.isEmpty() ? -1 : BuiltInRegistries.FLUID.getId(fluid.getFluid()));
        this.data.set(3, this.pushMode ? 1 : 0);
        this.data.set(4, this.collectSourceBlocks ? 1 : 0);
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

    private ItemStack getTankUpgradeStack() {
        ItemStack backpack = this.access.getBackpackStack(this.owner);
        if (backpack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        int tankSlot = FluidStorageUpgradeHandler.findFirstUpgradeSlot(upgrades);
        return tankSlot >= 0 && tankSlot < upgrades.size() ? upgrades.get(tankSlot) : ItemStack.EMPTY;
    }

    private void saveTankUpgradeStack(ItemStack tankUpgradeStack) {
        ItemStack backpack = this.access.getBackpackStack(this.owner);
        if (backpack.isEmpty()) {
            return;
        }

        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        int tankSlot = FluidStorageUpgradeHandler.findFirstUpgradeSlot(upgrades);
        if (tankSlot < 0 || tankSlot >= upgrades.size()) {
            return;
        }

        upgrades.set(tankSlot, tankUpgradeStack);
        BackpackStackData.saveUpgrades(backpack, upgrades);
        this.access.setBackpackStack(this.owner, backpack);
        this.owner.getInventory().setChanged();
        this.refreshRuntimeData();
        this.broadcastChanges();
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

    private static final class GhostFluidFilterSlot extends Slot {
        private GhostFluidFilterSlot(SimpleContainer container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private static final class SingleTankHandler implements com.teamsmartstreamlabs.smartbackpacks.compat.legacy.IFluidHandler {
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

