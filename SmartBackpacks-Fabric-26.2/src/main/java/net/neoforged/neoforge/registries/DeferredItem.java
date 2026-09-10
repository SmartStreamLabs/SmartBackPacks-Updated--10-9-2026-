package net.neoforged.neoforge.registries;

import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public final class DeferredItem<T extends Item> extends DeferredHolder<Item, T> {
   public DeferredItem(Identifier id, Supplier<? extends T> supplier) {
      super(id, supplier);
   }
}
