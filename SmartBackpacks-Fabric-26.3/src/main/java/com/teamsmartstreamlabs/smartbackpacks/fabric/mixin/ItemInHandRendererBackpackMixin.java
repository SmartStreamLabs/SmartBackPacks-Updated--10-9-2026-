package com.teamsmartstreamlabs.smartbackpacks.fabric.mixin;

import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import java.util.function.Predicate;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({FirstPersonHandsAndItems.class})
public abstract class ItemInHandRendererBackpackMixin {
   @Redirect(
      method = {"shouldInstantlyReplaceVisibleItem"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/item/ItemStack;matchesIgnoringComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Predicate;)Z"
      )
   )
   private boolean smartbackpacks$treatBackpacksWithInternalStateAsSame(ItemStack oldStack, ItemStack newStack, Predicate<DataComponentType<?>> ignoredComponents) {
      if (oldStack.getItem() instanceof BackpackItem
         && newStack.getItem() instanceof BackpackItem
         && oldStack.getItem() == newStack.getItem()
         && oldStack.getCount() == newStack.getCount()) {
         return true;
      }
      return ItemStack.matchesIgnoringComponents(oldStack, newStack, ignoredComponents);
   }
}
