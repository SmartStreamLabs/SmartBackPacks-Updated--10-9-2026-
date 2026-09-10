package net.neoforged.neoforge.registries;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;

public class DeferredRegister<T> {
   private final ResourceKey<? extends Registry<T>> registryKey;
   private final String modId;
   private final Map<String, DeferredHolder<T, ? extends T>> entries = new LinkedHashMap<>();
   private boolean registered;

   protected DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String modId) {
      this.registryKey = registryKey;
      this.modId = modId;
   }

   public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryKey, String modId) {
      return new DeferredRegister<>(registryKey, modId);
   }

   public static DeferredRegister.Items createItems(String modId) {
      return new DeferredRegister.Items(modId);
   }

   public static DeferredRegister.Blocks createBlocks(String modId) {
      return new DeferredRegister.Blocks(modId);
   }

   public static DeferredRegister.DataComponents createDataComponents(ResourceKey<? extends Registry<DataComponentType<?>>> registryKey, String modId) {
      return new DeferredRegister.DataComponents(modId);
   }

   public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> supplier) {
      Identifier id = Identifier.fromNamespaceAndPath(this.modId, name);
      DeferredHolder<T, I> holder = new DeferredHolder<>(id, supplier);
      this.entries.put(name, holder);
      return holder;
   }

   public void register(IEventBus eventBus) {
      if (!this.registered) {
         Registry<T> registry = this.resolveRegistry();

         for (DeferredHolder<T, ? extends T> holder : this.entries.values()) {
            Registry.register(registry, holder.getId(), holder.bind());
         }

         this.registered = true;
      }
   }

   @SuppressWarnings("unchecked")
   private Registry<T> resolveRegistry() {
      if (this.registryKey.equals(Registries.ITEM)) {
         return (Registry<T>)BuiltInRegistries.ITEM;
      } else if (this.registryKey.equals(Registries.BLOCK)) {
         return (Registry<T>)BuiltInRegistries.BLOCK;
      } else if (this.registryKey.equals(Registries.BLOCK_ENTITY_TYPE)) {
         return (Registry<T>)BuiltInRegistries.BLOCK_ENTITY_TYPE;
      } else if (this.registryKey.equals(Registries.CREATIVE_MODE_TAB)) {
         return (Registry<T>)BuiltInRegistries.CREATIVE_MODE_TAB;
      } else if (this.registryKey.equals(Registries.MENU)) {
         return (Registry<T>)BuiltInRegistries.MENU;
      } else if (this.registryKey.equals(Registries.DATA_COMPONENT_TYPE)) {
         return (Registry<T>)BuiltInRegistries.DATA_COMPONENT_TYPE;
      } else if (this.registryKey.equals(Registries.RECIPE_SERIALIZER)) {
         return (Registry<T>)BuiltInRegistries.RECIPE_SERIALIZER;
      } else {
         throw new IllegalStateException("Unsupported Fabric deferred registry key: " + this.registryKey.registry());
      }
   }

   public static final class Blocks extends DeferredRegister<Block> {
      private Blocks(String modId) {
         super(Registries.BLOCK, modId);
      }

      public <I extends Block> DeferredBlock<I> register(String name, Supplier<? extends I> supplier) {
         Identifier id = Identifier.fromNamespaceAndPath(super.modId, name);
         DeferredBlock<I> holder = new DeferredBlock<>(id, supplier);
         super.entries.put(name, holder);
         return holder;
      }
   }

   public static final class Items extends DeferredRegister<Item> {
      private Items(String modId) {
         super(Registries.ITEM, modId);
      }

      public <I extends Item> DeferredItem<I> register(String name, Supplier<? extends I> supplier) {
         Identifier id = Identifier.fromNamespaceAndPath(super.modId, name);
         DeferredItem<I> holder = new DeferredItem<>(id, supplier);
         super.entries.put(name, holder);
         return holder;
      }
   }

   public static final class DataComponents extends DeferredRegister<DataComponentType<?>> {
      private DataComponents(String modId) {
         super(Registries.DATA_COMPONENT_TYPE, modId);
      }

      public <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> registerComponentType(
         String name, Function<DataComponentType.Builder<T>, DataComponentType.Builder<T>> builderFactory
      ) {
         Identifier id = Identifier.fromNamespaceAndPath(super.modId, name);
         DeferredHolder<DataComponentType<?>, DataComponentType<T>> holder = new DeferredHolder<>(id, () -> builderFactory.apply(DataComponentType.builder()).build());
         super.entries.put(name, holder);
         return holder;
      }
   }
}
