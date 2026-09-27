package net.neoforged.neoforge.capabilities;

public final class RegisterCapabilitiesEvent {
    @FunctionalInterface
    public interface BlockEntityCapabilityGetter<B, S, C> {
        C get(B blockEntity, S side);
    }

    public <B, S, C> void registerBlockEntity(Object capability, Object type, BlockEntityCapabilityGetter<B, S, C> getter) {
        // Fabric variant uses different attachment APIs; this is a compile-time bridge.
    }
}
