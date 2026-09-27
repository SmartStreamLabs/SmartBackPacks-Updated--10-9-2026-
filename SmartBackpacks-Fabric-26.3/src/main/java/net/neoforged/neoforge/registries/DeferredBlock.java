package net.neoforged.neoforge.registries;

import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class DeferredBlock<T extends Block> extends DeferredHolder<Block, T> {
   public DeferredBlock(Identifier id, Supplier<? extends T> supplier) {
      super(id, supplier);
   }

   public Item asItem() {
      return this.get().asItem();
   }
}
