package com.teamsmartstreamlabs.smartbackpacks.registry;

import java.util.ArrayList;
import java.util.List;

import com.teamsmartstreamlabs.smartbackpacks.blockentity.PlacedBackpackBlockEntity;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import com.teamsmartstreamlabs.smartbackpacks.compat.legacy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import com.teamsmartstreamlabs.smartbackpacks.compat.legacy.IFluidHandler;
import com.teamsmartstreamlabs.smartbackpacks.compat.legacy.IItemHandlerModifiable;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class ModCapabilities {
    private ModCapabilities() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.PLACED_BACKPACK.get(),
                (PlacedBackpackBlockEntity blockEntity, Direction side) -> wrapItemHandler(side == null
                        ? blockEntity.getItemHandler()
                        : blockEntity.getSidedHandler(side))
        );
        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.PLACED_BACKPACK.get(),
                (PlacedBackpackBlockEntity blockEntity, Direction side) -> wrapFluidHandler(side == null
                        ? blockEntity.getFluidHandler()
                        : blockEntity.getSidedFluidHandler(side))
        );
        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                ModBlockEntities.PLACED_BACKPACK.get(),
                (PlacedBackpackBlockEntity blockEntity, Direction side) -> wrapEnergyStorage(side == null
                        ? blockEntity.getEnergyStorage()
                        : blockEntity.getSidedEnergyStorage(side))
        );
    }

    private static ResourceHandler<ItemResource> wrapItemHandler(IItemHandlerModifiable handler) {
        return handler == null ? null : new ItemHandlerCapabilityAdapter(handler);
    }

    private static ResourceHandler<FluidResource> wrapFluidHandler(IFluidHandler handler) {
        return handler == null ? null : new FluidHandlerCapabilityAdapter(handler);
    }

    private static EnergyHandler wrapEnergyStorage(IEnergyStorage storage) {
        return storage == null ? null : new EnergyCapabilityAdapter(storage);
    }

    private static final class ItemHandlerCapabilityAdapter extends SnapshotJournal<List<ItemStack>> implements ResourceHandler<ItemResource> {
        private final IItemHandlerModifiable handler;

        private ItemHandlerCapabilityAdapter(IItemHandlerModifiable handler) {
            this.handler = handler;
        }

        @Override
        protected List<ItemStack> createSnapshot() {
            List<ItemStack> snapshot = new ArrayList<>(this.handler.getSlots());
            for (int slot = 0; slot < this.handler.getSlots(); slot++) {
                snapshot.add(this.handler.getStackInSlot(slot).copy());
            }
            return snapshot;
        }

        @Override
        protected void revertToSnapshot(List<ItemStack> snapshot) {
            int slots = Math.min(this.handler.getSlots(), snapshot.size());
            for (int slot = 0; slot < slots; slot++) {
                this.handler.setStackInSlot(slot, snapshot.get(slot).copy());
            }
        }

        @Override
        public int size() {
            return this.handler.getSlots();
        }

        @Override
        public ItemResource getResource(int slot) {
            return ItemResource.of(this.handler.getStackInSlot(slot));
        }

        @Override
        public long getAmountAsLong(int slot) {
            return this.handler.getStackInSlot(slot).getCount();
        }

        @Override
        public long getCapacityAsLong(int slot, ItemResource resource) {
            return this.handler.getSlotLimit(slot);
        }

        @Override
        public boolean isValid(int slot, ItemResource resource) {
            return resource.isEmpty() || this.handler.isItemValid(slot, resource.toStack(1));
        }

        @Override
        public int insert(int slot, ItemResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0) {
                return 0;
            }
            this.updateSnapshots(transaction);
            ItemStack remainder = this.handler.insertItem(slot, resource.toStack(amount), false);
            return amount - remainder.getCount();
        }

        @Override
        public int extract(int slot, ItemResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0 || !resource.matches(this.handler.getStackInSlot(slot))) {
                return 0;
            }
            this.updateSnapshots(transaction);
            return this.handler.extractItem(slot, amount, false).getCount();
        }
    }

    private static final class FluidHandlerCapabilityAdapter extends SnapshotJournal<FluidStack> implements ResourceHandler<FluidResource> {
        private final IFluidHandler handler;

        private FluidHandlerCapabilityAdapter(IFluidHandler handler) {
            this.handler = handler;
        }

        @Override
        protected FluidStack createSnapshot() {
            return this.handler.getTanks() > 0 ? this.handler.getFluidInTank(0).copy() : FluidStack.EMPTY;
        }

        @Override
        protected void revertToSnapshot(FluidStack snapshot) {
            if (this.handler.getTanks() <= 0) {
                return;
            }
            FluidStack current = this.handler.getFluidInTank(0);
            if (!current.isEmpty()) {
                this.handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
            }
            if (!snapshot.isEmpty()) {
                this.handler.fill(snapshot.copy(), IFluidHandler.FluidAction.EXECUTE);
            }
        }

        @Override
        public int size() {
            return this.handler.getTanks();
        }

        @Override
        public FluidResource getResource(int slot) {
            return slot >= 0 && slot < this.handler.getTanks() ? FluidResource.of(this.handler.getFluidInTank(slot)) : FluidResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int slot) {
            return slot >= 0 && slot < this.handler.getTanks() ? this.handler.getFluidInTank(slot).getAmount() : 0L;
        }

        @Override
        public long getCapacityAsLong(int slot, FluidResource resource) {
            return slot >= 0 && slot < this.handler.getTanks() ? this.handler.getTankCapacity(slot) : 0L;
        }

        @Override
        public boolean isValid(int slot, FluidResource resource) {
            return slot >= 0 && slot < this.handler.getTanks() && (resource.isEmpty() || this.handler.isFluidValid(slot, resource.toStack(1)));
        }

        @Override
        public int insert(int slot, FluidResource resource, int amount, TransactionContext transaction) {
            if (slot != 0 || resource.isEmpty() || amount <= 0) {
                return 0;
            }
            this.updateSnapshots(transaction);
            return this.handler.fill(resource.toStack(amount), IFluidHandler.FluidAction.EXECUTE);
        }

        @Override
        public int extract(int slot, FluidResource resource, int amount, TransactionContext transaction) {
            if (slot != 0 || resource.isEmpty() || amount <= 0 || !resource.matches(this.handler.getFluidInTank(slot))) {
                return 0;
            }
            this.updateSnapshots(transaction);
            return this.handler.drain(resource.toStack(amount), IFluidHandler.FluidAction.EXECUTE).getAmount();
        }
    }

    private static final class EnergyCapabilityAdapter extends SnapshotJournal<Integer> implements EnergyHandler {
        private final IEnergyStorage storage;

        private EnergyCapabilityAdapter(IEnergyStorage storage) {
            this.storage = storage;
        }

        @Override
        protected Integer createSnapshot() {
            return this.storage.getEnergyStored();
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            int current = this.storage.getEnergyStored();
            if (current > snapshot) {
                this.storage.extractEnergy(current - snapshot, false);
            } else if (current < snapshot) {
                this.storage.receiveEnergy(snapshot - current, false);
            }
        }

        @Override
        public long getAmountAsLong() {
            return this.storage.getEnergyStored();
        }

        @Override
        public long getCapacityAsLong() {
            return this.storage.getMaxEnergyStored();
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            this.updateSnapshots(transaction);
            return this.storage.receiveEnergy(amount, false);
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            this.updateSnapshots(transaction);
            return this.storage.extractEnergy(amount, false);
        }
    }
}
