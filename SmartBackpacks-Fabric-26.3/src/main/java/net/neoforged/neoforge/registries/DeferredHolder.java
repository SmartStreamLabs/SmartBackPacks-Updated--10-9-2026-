package net.neoforged.neoforge.registries;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;

public class DeferredHolder<R, T extends R> implements Supplier<T> {
   private final Identifier id;
   private final Supplier<? extends T> supplier;
   private T value;

   public DeferredHolder(Identifier id, Supplier<? extends T> supplier) {
      this.id = id;
      this.supplier = supplier;
   }

   @Override
   public T get() {
      if (this.value == null) {
         throw new IllegalStateException("Registry object " + this.id + " has not been bound yet");
      } else {
         return this.value;
      }
   }

   public Identifier getId() {
      return this.id;
   }

   public boolean isBound() {
      return this.value != null;
   }

   T bind() {
      if (this.value == null) {
         this.value = Objects.requireNonNull((T)this.supplier.get(), "Registered object supplier returned null for " + this.id);
      }

      return this.value;
   }
}
