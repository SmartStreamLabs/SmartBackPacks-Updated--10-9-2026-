package net.neoforged.neoforge.capabilities;

public final class RegisterCapabilitiesEvent {
   public <B, S, C> void registerBlockEntity(Object capability, Object type, RegisterCapabilitiesEvent.BlockEntityCapabilityGetter<B, S, C> getter) {
   }

   @FunctionalInterface
   public interface BlockEntityCapabilityGetter<B, S, C> {
      C get(B var1, S var2);
   }
}
