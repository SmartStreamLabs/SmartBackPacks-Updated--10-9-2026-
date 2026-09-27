package net.neoforged.neoforge.fluids.capability;

import net.neoforged.neoforge.fluids.FluidStack;

public interface IFluidHandler {
   int getTanks();

   FluidStack getFluidInTank(int var1);

   int getTankCapacity(int var1);

   boolean isFluidValid(int var1, FluidStack var2);

   int fill(FluidStack var1, IFluidHandler.FluidAction var2);

   FluidStack drain(FluidStack var1, IFluidHandler.FluidAction var2);

   FluidStack drain(int var1, IFluidHandler.FluidAction var2);

   public static enum FluidAction {
      EXECUTE,
      SIMULATE;

      public boolean execute() {
         return this == EXECUTE;
      }

      public boolean simulate() {
         return this == SIMULATE;
      }
   }
}
