package com.teamsmartstreamlabs.smartbackpacks.compat.legacy;

import java.util.Optional;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class FluidUtil {
    private FluidUtil() {
    }

    public static Optional<ResourceHandler<FluidResource>> getFluidHandler(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(ItemAccess.forStack(stack).getCapability(Capabilities.Fluid.ITEM));
    }

    public static Optional<FluidStack> getFluidContained(ItemStack stack) {
        FluidStack fluid = net.neoforged.neoforge.transfer.fluid.FluidUtil.getFirstStackContained(stack);
        return fluid.isEmpty() ? Optional.empty() : Optional.of(fluid);
    }

    public static Result tryEmptyContainer(ItemStack stack, IFluidHandler tank, int limit, Object player, boolean doTransfer) {
        if (stack.isEmpty() || limit <= 0) {
            return Result.failure(stack);
        }
        SimpleContainer container = new SimpleContainer(stack.copy());
        ResourceHandler<FluidResource> source = ItemAccess.forHandlerIndex(VanillaContainerWrapper.of(container), 0)
                .getCapability(Capabilities.Fluid.ITEM);
        if (source == null) {
            return Result.failure(stack);
        }
        for (int slot = 0; slot < source.size(); slot++) {
            FluidResource resource = source.getResource(slot);
            if (resource.isEmpty()) {
                continue;
            }
            int available = Math.min(source.getAmountAsInt(slot), limit);
            int accepted = tank.fill(resource.toStack(available), IFluidHandler.FluidAction.SIMULATE);
            if (accepted <= 0) {
                continue;
            }
            if (!doTransfer) {
                return Result.success(stack);
            }
            try (Transaction tx = Transaction.openRoot()) {
                int extracted = source.extract(slot, resource, accepted, tx);
                if (extracted != accepted) {
                    continue;
                }
                if (tank.fill(resource.toStack(extracted), IFluidHandler.FluidAction.EXECUTE) != extracted) {
                    continue;
                }
                tx.commit();
                return Result.success(container.getItem(0));
            }
        }
        return Result.failure(stack);
    }

    public static Result tryFillContainer(ItemStack stack, IFluidHandler tank, int limit, Object player, boolean doTransfer) {
        if (stack.isEmpty() || limit <= 0) {
            return Result.failure(stack);
        }
        SimpleContainer container = new SimpleContainer(stack.copy());
        ResourceHandler<FluidResource> destination = ItemAccess.forHandlerIndex(VanillaContainerWrapper.of(container), 0)
                .getCapability(Capabilities.Fluid.ITEM);
        if (destination == null) {
            return Result.failure(stack);
        }
        for (int tankSlot = 0; tankSlot < tank.getTanks(); tankSlot++) {
            FluidStack stored = tank.getFluidInTank(tankSlot);
            if (stored.isEmpty()) {
                continue;
            }
            FluidResource resource = FluidResource.of(stored);
            int available = Math.min(stored.getAmount(), limit);
            if (!doTransfer) {
                return Result.success(stack);
            }
            try (Transaction tx = Transaction.openRoot()) {
                int inserted = destination.insert(resource, available, tx);
                if (inserted <= 0) {
                    continue;
                }
                FluidStack drained = tank.drain(resource.toStack(inserted), IFluidHandler.FluidAction.EXECUTE);
                if (drained.getAmount() != inserted) {
                    continue;
                }
                tx.commit();
                return Result.success(container.getItem(0));
            }
        }
        return Result.failure(stack);
    }

    public record Result(boolean isSuccess, ItemStack result) {
        public static Result success(ItemStack result) {
            return new Result(true, result);
        }

        public static Result failure(ItemStack result) {
            return new Result(false, result);
        }

        public ItemStack getResult() {
            return result;
        }
    }
}
