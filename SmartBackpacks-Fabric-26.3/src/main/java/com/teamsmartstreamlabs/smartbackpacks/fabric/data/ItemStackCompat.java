package com.teamsmartstreamlabs.smartbackpacks.fabric.data;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public final class ItemStackCompat {
   private ItemStackCompat() {
   }

   public static <T> T getOrDefault(ItemStack stack, DataComponentType<T> key, T defaultValue) {
      return ItemStackDataComponents.getOrDefault(stack, key, defaultValue);
   }

   public static <T> T get(ItemStack stack, DataComponentType<T> key) {
      return ItemStackDataComponents.get(stack, key);
   }

   public static <T> void set(ItemStack stack, DataComponentType<T> key, T value) {
      ItemStackDataComponents.set(stack, key, value);
   }

   public static void remove(ItemStack stack, DataComponentType<?> key) {
      ItemStackDataComponents.remove(stack, key);
   }

   public static boolean has(ItemStack stack, DataComponentType<?> key) {
      return ItemStackDataComponents.has(stack, key);
   }

   public static boolean isSameItemSameComponents(ItemStack left, ItemStack right) {
      if (left.isEmpty() && right.isEmpty()) {
         return true;
      } else {
         return !left.isEmpty() && !right.isEmpty() ? ItemStack.isSameItemSameComponents(left, right) : false;
      }
   }

   public static void limitSize(ItemStack stack, int maxSize) {
      if (!stack.isEmpty() && stack.getCount() > maxSize) {
         stack.setCount(maxSize);
      }
   }
}
