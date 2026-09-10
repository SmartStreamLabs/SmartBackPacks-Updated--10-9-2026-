package com.teamsmartstreamlabs.smartbackpacks.fabric.data;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public final class ItemStackDataComponents {
   private ItemStackDataComponents() {
   }

   public static <T> T getOrDefault(ItemStack stack, DataComponentType<T> key, T defaultValue) {
      return (T)(!stack.isEmpty() && key != null ? stack.getOrDefault(key, defaultValue) : defaultValue);
   }

   public static <T> T get(ItemStack stack, DataComponentType<T> key) {
      return (T)(!stack.isEmpty() && key != null ? stack.get(key) : null);
   }

   public static <T> void set(ItemStack stack, DataComponentType<T> key, T value) {
      if (!stack.isEmpty() && key != null) {
         if (value == null) {
            stack.remove(key);
         } else {
            stack.set(key, value);
         }
      }
   }

   public static void remove(ItemStack stack, DataComponentType<?> key) {
      if (!stack.isEmpty() && key != null) {
         stack.remove(key);
      }
   }

   public static boolean has(ItemStack stack, DataComponentType<?> key) {
      return !stack.isEmpty() && key != null && stack.has(key);
   }
}
