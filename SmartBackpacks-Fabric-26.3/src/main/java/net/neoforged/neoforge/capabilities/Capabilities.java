package net.neoforged.neoforge.capabilities;

import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public final class Capabilities {
   private Capabilities() {
   }

   public static final class CapabilityToken<T> {
      public T getCapability(Object... ignored) {
         return null;
      }
   }

   public static final class EnergyStorage {
      public static final Capabilities.CapabilityToken<IEnergyStorage> BLOCK = new Capabilities.CapabilityToken<>();
      public static final Capabilities.CapabilityToken<IEnergyStorage> ITEM = new Capabilities.CapabilityToken<>();

      private EnergyStorage() {
      }
   }

   public static final class FluidHandler {
      public static final Capabilities.CapabilityToken<IFluidHandler> BLOCK = new Capabilities.CapabilityToken<>();
      public static final Capabilities.CapabilityToken<IFluidHandler> ITEM = new Capabilities.CapabilityToken<>();

      private FluidHandler() {
      }
   }

   public static final class ItemHandler {
      public static final Capabilities.CapabilityToken<IItemHandlerModifiable> BLOCK = new Capabilities.CapabilityToken<>();
      public static final Capabilities.CapabilityToken<IItemHandlerModifiable> ITEM = new Capabilities.CapabilityToken<>();

      private ItemHandler() {
      }
   }
}
