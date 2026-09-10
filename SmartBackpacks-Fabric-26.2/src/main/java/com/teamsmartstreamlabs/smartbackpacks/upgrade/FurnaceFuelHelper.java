package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

final class FurnaceFuelHelper {
   private FurnaceFuelHelper() {
   }

   static int getBurnTime(Level level, ItemStack stack) {
      if (stack.isEmpty()) {
         return 0;
      }
      return level.fuelValues().burnDuration(stack);
   }
}
