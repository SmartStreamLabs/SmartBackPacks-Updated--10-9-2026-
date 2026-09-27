package net.neoforged.neoforge.fluids;

import java.lang.reflect.Field;
import java.util.Optional;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public final class FluidUtil {
   private FluidUtil() {
   }

   public static Optional<FluidStack> getFluidContained(ItemStack stack) {
      if (stack.getItem() instanceof BucketItem bucketItem) {
         Fluid fluid = getBucketFluid(bucketItem);
         if (fluid != Fluids.EMPTY) {
            return Optional.of(new FluidStack(fluid, 1000));
         }
      }

      return Optional.empty();
   }

   public static Optional<IFluidHandler> getFluidHandler(ItemStack stack) {
      return !getFluidContained(stack).isPresent() && !stack.is(Items.BUCKET) ? Optional.empty() : Optional.of(new FluidUtil.BucketFluidHandler(stack));
   }

   public static FluidUtil.FluidActionResult tryEmptyContainer(ItemStack container, IFluidHandler destination, int maxAmount, Object player, boolean execute) {
      Optional<FluidStack> contained = getFluidContained(container);
      if (contained.isEmpty()) {
         return FluidUtil.FluidActionResult.failure(container);
      } else {
         FluidStack fluid = contained.get();
         int transferable = Math.min(fluid.getAmount(), maxAmount);
         FluidStack toTransfer = fluid.copyWithAmount(transferable);
         int filled = destination.fill(toTransfer, execute ? IFluidHandler.FluidAction.EXECUTE : IFluidHandler.FluidAction.SIMULATE);
         if (filled <= 0) {
            return FluidUtil.FluidActionResult.failure(container);
         } else {
            ItemStack result = container.copy();
            if (execute) {
               result = new ItemStack(Items.BUCKET);
            }

            return FluidUtil.FluidActionResult.success(result);
         }
      }
   }

   public static FluidUtil.FluidActionResult tryFillContainer(ItemStack container, IFluidHandler source, int maxAmount, Object player, boolean execute) {
      if (!container.is(Items.BUCKET)) {
         return FluidUtil.FluidActionResult.failure(container);
      } else {
         FluidStack drained = source.drain(maxAmount, execute ? IFluidHandler.FluidAction.EXECUTE : IFluidHandler.FluidAction.SIMULATE);
         if (!drained.isEmpty() && drained.getAmount() >= 1000) {
            Item bucketItem = bucketForFluid(drained);
            if (bucketItem == null) {
               return FluidUtil.FluidActionResult.failure(container);
            } else {
               ItemStack result = execute ? new ItemStack(bucketItem) : container.copy();
               return FluidUtil.FluidActionResult.success(result);
            }
         } else {
            return FluidUtil.FluidActionResult.failure(container);
         }
      }
   }

   private static Item bucketForFluid(FluidStack fluid) {
      if (fluid.getFluid() == Fluids.WATER || fluid.getFluid() == Fluids.FLOWING_WATER) {
         return Items.WATER_BUCKET;
      } else if (fluid.getFluid() != Fluids.LAVA && fluid.getFluid() != Fluids.FLOWING_LAVA) {
         if (fluid.getFluid() instanceof FlowingFluid flowingFluid) {
            Item bucket = flowingFluid.getBucket();
            return bucket == Items.AIR ? null : bucket;
         } else {
            return null;
         }
      } else {
         return Items.LAVA_BUCKET;
      }
   }

   private static Fluid getBucketFluid(BucketItem bucketItem) {
      try {
         Field field = BucketItem.class.getDeclaredField("content");
         field.setAccessible(true);
         return field.get(bucketItem) instanceof Fluid fluid ? fluid : Fluids.EMPTY;
      } catch (ReflectiveOperationException var4) {
         return Fluids.EMPTY;
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
         return tank == 0 ? FluidUtil.getFluidContained(this.stack).orElse(FluidStack.EMPTY) : FluidStack.EMPTY;
      }

      @Override
      public int getTankCapacity(int tank) {
         return 1000;
      }

      @Override
      public boolean isFluidValid(int tank, FluidStack stack) {
         return tank == 0 && !stack.isEmpty();
      }

      @Override
      public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
         return 0;
      }

      @Override
      public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
         FluidStack contained = this.getFluidInTank(0);
         return !contained.isEmpty() && contained.isFluidEqual(resource) ? this.drain(resource.getAmount(), action) : FluidStack.EMPTY;
      }

      @Override
      public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
         FluidStack contained = this.getFluidInTank(0);
         if (contained.isEmpty()) {
            return FluidStack.EMPTY;
         } else {
            int drained = Math.min(maxDrain, contained.getAmount());
            return contained.copyWithAmount(drained);
         }
      }
   }

   public record FluidActionResult(boolean success, ItemStack result) {
      public static FluidUtil.FluidActionResult success(ItemStack result) {
         return new FluidUtil.FluidActionResult(true, result);
      }

      public static FluidUtil.FluidActionResult failure(ItemStack result) {
         return new FluidUtil.FluidActionResult(false, result);
      }

      public boolean isSuccess() {
         return this.success;
      }

      public ItemStack getResult() {
         return this.result;
      }
   }
}
