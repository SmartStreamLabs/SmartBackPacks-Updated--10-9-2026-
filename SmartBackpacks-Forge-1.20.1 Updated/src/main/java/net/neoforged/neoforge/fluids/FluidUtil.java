package net.neoforged.neoforge.fluids;

import java.util.Optional;
import java.lang.reflect.Field;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public final class FluidUtil {
    private FluidUtil() {
    }

    public static Optional<FluidStack> getFluidContained(ItemStack stack) {
        if (stack.getItem() instanceof BucketItem bucketItem) {
            var fluid = getBucketFluid(bucketItem);
            if (fluid != Fluids.EMPTY) {
                return Optional.of(new FluidStack(fluid, FluidType.BUCKET_VOLUME));
            }
        }
        return Optional.empty();
    }

    public static Optional<IFluidHandler> getFluidHandler(ItemStack stack) {
        if (getFluidContained(stack).isPresent() || stack.is(Items.BUCKET)) {
            return Optional.of(new BucketFluidHandler(stack));
        }
        return Optional.empty();
    }

    public static FluidActionResult tryEmptyContainer(ItemStack container, IFluidHandler destination, int maxAmount, Object player, boolean execute) {
        Optional<FluidStack> contained = getFluidContained(container);
        if (contained.isEmpty()) {
            return FluidActionResult.failure(container);
        }

        FluidStack fluid = contained.get();
        int transferable = Math.min(fluid.getAmount(), maxAmount);
        FluidStack toTransfer = fluid.copyWithAmount(transferable);
        int filled = destination.fill(toTransfer, execute ? IFluidHandler.FluidAction.EXECUTE : IFluidHandler.FluidAction.SIMULATE);
        if (filled <= 0) {
            return FluidActionResult.failure(container);
        }

        ItemStack result = container.copy();
        if (execute) {
            result = new ItemStack(Items.BUCKET);
        }
        return FluidActionResult.success(result);
    }

    public static FluidActionResult tryFillContainer(ItemStack container, IFluidHandler source, int maxAmount, Object player, boolean execute) {
        if (!container.is(Items.BUCKET)) {
            return FluidActionResult.failure(container);
        }

        FluidStack drained = source.drain(maxAmount, execute ? IFluidHandler.FluidAction.EXECUTE : IFluidHandler.FluidAction.SIMULATE);
        if (drained.isEmpty() || drained.getAmount() < FluidType.BUCKET_VOLUME) {
            return FluidActionResult.failure(container);
        }

        Item bucketItem = bucketForFluid(drained);
        if (bucketItem == null) {
            return FluidActionResult.failure(container);
        }

        ItemStack result = execute ? new ItemStack(bucketItem) : container.copy();
        return FluidActionResult.success(result);
    }

    private static Item bucketForFluid(FluidStack fluid) {
        if (fluid.getFluid() == Fluids.WATER || fluid.getFluid() == Fluids.FLOWING_WATER) {
            return Items.WATER_BUCKET;
        }
        if (fluid.getFluid() == Fluids.LAVA || fluid.getFluid() == Fluids.FLOWING_LAVA) {
            return Items.LAVA_BUCKET;
        }
        if (fluid.getFluid() instanceof FlowingFluid flowingFluid) {
            Item bucket = flowingFluid.getBucket();
            return bucket == Items.AIR ? null : bucket;
        }
        return null;
    }

    private static net.minecraft.world.level.material.Fluid getBucketFluid(BucketItem bucketItem) {
        try {
            Field field = BucketItem.class.getDeclaredField("content");
            field.setAccessible(true);
            Object value = field.get(bucketItem);
            return value instanceof net.minecraft.world.level.material.Fluid fluid ? fluid : Fluids.EMPTY;
        } catch (ReflectiveOperationException ignored) {
            return Fluids.EMPTY;
        }
    }

    public record FluidActionResult(boolean success, ItemStack result) {
        public static FluidActionResult success(ItemStack result) {
            return new FluidActionResult(true, result);
        }

        public static FluidActionResult failure(ItemStack result) {
            return new FluidActionResult(false, result);
        }

        public boolean isSuccess() {
            return this.success;
        }

        public ItemStack getResult() {
            return this.result;
        }
    }

    private static final class BucketFluidHandler implements IFluidHandler {
        private final ItemStack stack;

        private BucketFluidHandler(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? getFluidContained(this.stack).orElse(FluidStack.EMPTY) : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return FluidType.BUCKET_VOLUME;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && !stack.isEmpty();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack contained = this.getFluidInTank(0);
            if (contained.isEmpty() || !contained.isFluidEqual(resource)) {
                return FluidStack.EMPTY;
            }
            return this.drain(resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            FluidStack contained = this.getFluidInTank(0);
            if (contained.isEmpty()) {
                return FluidStack.EMPTY;
            }

            int drained = Math.min(maxDrain, contained.getAmount());
            return contained.copyWithAmount(drained);
        }
    }
}
