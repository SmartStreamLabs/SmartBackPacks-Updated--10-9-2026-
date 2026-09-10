package net.neoforged.neoforge.capabilities;

import java.util.function.Function;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public final class Capabilities {
    public static final class ItemHandler {
        public static final CapabilityToken<IItemHandlerModifiable> BLOCK = new CapabilityToken<>();
        public static final CapabilityToken<IItemHandlerModifiable> ITEM = new CapabilityToken<>();

        private ItemHandler() {
        }
    }

    public static final class FluidHandler {
        public static final CapabilityToken<IFluidHandler> BLOCK = new CapabilityToken<>(Capabilities::getForgeBlockFluidHandler);
        public static final CapabilityToken<IFluidHandler> ITEM = new CapabilityToken<>();

        private FluidHandler() {
        }
    }

    public static final class EnergyStorage {
        public static final CapabilityToken<IEnergyStorage> BLOCK = new CapabilityToken<>();
        public static final CapabilityToken<IEnergyStorage> ITEM = new CapabilityToken<>(Capabilities::getForgeItemEnergyStorage);

        private EnergyStorage() {
        }
    }

    private Capabilities() {
    }

    private static IFluidHandler getForgeBlockFluidHandler(Object... args) {
        if (args.length < 4 || !(args[3] instanceof BlockEntity blockEntity)) {
            return null;
        }

        Direction side = args.length > 4 && args[4] instanceof Direction direction ? direction : null;
        return blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, side)
                .map(ForgeFluidHandlerBridge::new)
                .orElse(null);
    }

    private static IEnergyStorage getForgeItemEnergyStorage(Object... args) {
        if (args.length < 1 || !(args[0] instanceof ItemStack stack)) {
            return null;
        }

        Direction side = args.length > 1 && args[1] instanceof Direction direction ? direction : null;
        return stack.getCapability(ForgeCapabilities.ENERGY, side)
                .map(ForgeEnergyStorageBridge::new)
                .orElse(null);
    }

    public static final class CapabilityToken<T> {
        private final Function<Object[], T> resolver;

        public CapabilityToken() {
            this(args -> null);
        }

        public CapabilityToken(Function<Object[], T> resolver) {
            this.resolver = resolver;
        }

        public T getCapability(Object... ignored) {
            return this.resolver.apply(ignored);
        }
    }

    private static final class ForgeFluidHandlerBridge implements IFluidHandler {
        private final net.minecraftforge.fluids.capability.IFluidHandler delegate;

        private ForgeFluidHandlerBridge(net.minecraftforge.fluids.capability.IFluidHandler delegate) {
            this.delegate = delegate;
        }

        @Override
        public int getTanks() {
            return this.delegate.getTanks();
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return fromForgeFluidStack(this.delegate.getFluidInTank(tank));
        }

        @Override
        public int getTankCapacity(int tank) {
            return this.delegate.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return this.delegate.isFluidValid(tank, toForgeFluidStack(stack));
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return this.delegate.fill(toForgeFluidStack(resource), toForgeFluidAction(action));
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return fromForgeFluidStack(this.delegate.drain(toForgeFluidStack(resource), toForgeFluidAction(action)));
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return fromForgeFluidStack(this.delegate.drain(maxDrain, toForgeFluidAction(action)));
        }
    }

    private static final class ForgeEnergyStorageBridge implements IEnergyStorage {
        private final net.minecraftforge.energy.IEnergyStorage delegate;

        private ForgeEnergyStorageBridge(net.minecraftforge.energy.IEnergyStorage delegate) {
            this.delegate = delegate;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return this.delegate.receiveEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return this.delegate.extractEnergy(maxExtract, simulate);
        }

        @Override
        public int getEnergyStored() {
            return this.delegate.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return this.delegate.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return this.delegate.canExtract();
        }

        @Override
        public boolean canReceive() {
            return this.delegate.canReceive();
        }
    }

    private static FluidStack fromForgeFluidStack(net.minecraftforge.fluids.FluidStack stack) {
        if (stack == null || stack.isEmpty()) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(stack.getFluid(), stack.getAmount());
    }

    private static net.minecraftforge.fluids.FluidStack toForgeFluidStack(FluidStack stack) {
        if (stack == null || stack.isEmpty()) {
            return net.minecraftforge.fluids.FluidStack.EMPTY;
        }
        return new net.minecraftforge.fluids.FluidStack(stack.getFluid(), stack.getAmount());
    }

    private static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction toForgeFluidAction(IFluidHandler.FluidAction action) {
        return action.execute()
                ? net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE
                : net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE;
    }
}
