package net.neoforged.neoforge.registries;

import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

public class DeferredRegister<T> {
    protected final net.minecraftforge.registries.DeferredRegister<T> delegate;
    protected final String modId;

    protected DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String modId) {
        this(net.minecraftforge.registries.DeferredRegister.create(registryKey, modId), modId);
    }

    protected DeferredRegister(net.minecraftforge.registries.DeferredRegister<T> delegate, String modId) {
        this.delegate = delegate;
        this.modId = modId;
    }

    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryKey, String modId) {
        return new DeferredRegister<>(registryKey, modId);
    }

    public static Items createItems(String modId) {
        return new Items(modId);
    }

    public static Blocks createBlocks(String modId) {
        return new Blocks(modId);
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> supplier) {
        ResourceLocation id = new ResourceLocation(modId, name);
        RegistryObject<I> object = delegate.register(name, supplier);
        return new DeferredHolder<>(id, object);
    }

    public void register(IEventBus eventBus) {
        delegate.register(eventBus);
    }

    public static final class Items extends DeferredRegister<Item> {
        private Items(String modId) {
            super(Registries.ITEM, modId);
        }

        public <I extends Item> DeferredItem<I> register(String name, Supplier<? extends I> supplier) {
            ResourceLocation id = new ResourceLocation(super.modId, name);
            RegistryObject<I> object = super.delegate.register(name, supplier);
            return new DeferredItem<>(id, object);
        }
    }

    public static final class Blocks extends DeferredRegister<Block> {
        private Blocks(String modId) {
            super(Registries.BLOCK, modId);
        }

        public <I extends Block> DeferredBlock<I> register(String name, Supplier<? extends I> supplier) {
            ResourceLocation id = new ResourceLocation(super.modId, name);
            RegistryObject<I> object = super.delegate.register(name, supplier);
            return new DeferredBlock<>(id, object);
        }
    }
}
