package net.neoforged.neoforge.registries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public final class DeferredItem<T extends Item> extends DeferredHolder<Item, T> {
    public DeferredItem(ResourceLocation id, RegistryObject<T> object) {
        super(id, object);
    }
}
