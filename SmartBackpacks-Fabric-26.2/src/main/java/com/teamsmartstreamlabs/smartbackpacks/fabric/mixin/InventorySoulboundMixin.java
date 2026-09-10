package com.teamsmartstreamlabs.smartbackpacks.fabric.mixin;

import com.teamsmartstreamlabs.smartbackpacks.upgrade.SoulboundUpgradeHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Inventory.class})
public abstract class InventorySoulboundMixin {
   @Shadow
   @Final
   private NonNullList<ItemStack> items;
   @Shadow
   @Final
   private EntityEquipment equipment;
   @Shadow
   @Final
   private Player player;

   @Inject(
      method = {"dropAll"},
      at = {@At("HEAD")}
   )
   private void smartbackpacks$captureSoulboundBackpacks(CallbackInfo ci) {
      SoulboundUpgradeHandler.captureInventoryDrops(this.player, this.items, this.equipment);
   }
}
