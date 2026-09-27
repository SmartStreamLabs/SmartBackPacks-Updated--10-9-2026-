package net.neoforged.neoforge.registries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

public final class DeferredBlock<T extends Block> extends DeferredHolder<Block, T> {
    public DeferredBlock(ResourceLocation id, RegistryObject<T> object) {
        super(id, object);
    }

    public Item asItem() {
        return get().asItem();
    }
}
