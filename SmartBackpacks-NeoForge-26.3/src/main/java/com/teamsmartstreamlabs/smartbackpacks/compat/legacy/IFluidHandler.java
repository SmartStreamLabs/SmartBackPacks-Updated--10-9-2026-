package com.teamsmartstreamlabs.smartbackpacks.compat.legacy;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public interface IFluidHandler {
    int getTanks();

    FluidStack getFluidInTank(int tank);

    int getTankCapacity(int tank);

    boolean isFluidValid(int tank, FluidStack stack);

    int fill(FluidStack resource, FluidAction action);

    FluidStack drain(FluidStack resource, FluidAction action);

    FluidStack drain(int maxDrain, FluidAction action);

    static IFluidHandler of(ResourceHandler<FluidResource> handler) {
        return new ResourceAdapter(handler);
    }

    enum FluidAction {
        EXECUTE,
        SIMULATE;

        public boolean execute() {
            return this == EXECUTE;
        }
    }

    final class ResourceAdapter implements IFluidHandler {
        private final ResourceHandler<FluidResource> handler;

        private ResourceAdapter(ResourceHandler<FluidResource> handler) {
            this.handler = handler;
        }

        @Override
        public int getTanks() {
            return handler.size();
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            FluidResource resource = handler.getResource(tank);
            return resource.isEmpty() ? FluidStack.EMPTY : resource.toStack(handler.getAmountAsInt(tank));
        }

        @Override
        public int getTankCapacity(int tank) {
            return handler.getCapacityAsInt(tank, handler.getResource(tank));
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return handler.isValid(tank, FluidResource.of(stack));
        }

        @Override
        public int fill(FluidStack stack, FluidAction action) {
            if (stack.isEmpty()) {
                return 0;
            }
            try (Transaction tx = Transaction.openRoot()) {
                int inserted = handler.insert(FluidResource.of(stack), stack.getAmount(), tx);
                if (action.execute()) {
                    tx.commit();
                }
                return inserted;
            }
        }

        @Override
        public FluidStack drain(FluidStack stack, FluidAction action) {
            if (stack.isEmpty()) {
                return FluidStack.EMPTY;
            }
            FluidResource resource = FluidResource.of(stack);
            try (Transaction tx = Transaction.openRoot()) {
                int extracted = handler.extract(resource, stack.getAmount(), tx);
                if (action.execute()) {
                    tx.commit();
                }
                return extracted > 0 ? resource.toStack(extracted) : FluidStack.EMPTY;
            }
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain <= 0) {
                return FluidStack.EMPTY;
            }
            for (int tank = 0; tank < handler.size(); tank++) {
                FluidResource resource = handler.getResource(tank);
                if (!resource.isEmpty()) {
                    return drain(resource.toStack(maxDrain), action);
                }
            }
            return FluidStack.EMPTY;
        }
    }
}
