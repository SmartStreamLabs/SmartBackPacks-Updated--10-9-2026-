package net.neoforged.neoforge.registries;

import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

public class DeferredHolder<R, T extends R> implements Supplier<T> {
    private final ResourceLocation id;
    private final RegistryObject<T> object;

    public DeferredHolder(ResourceLocation id, RegistryObject<T> object) {
        this.id = id;
        this.object = object;
    }

    @Override
    public T get() {
        return object.get();
    }

    public ResourceLocation getId() {
        return id;
    }

    public boolean isBound() {
        return object.isPresent();
    }
}
