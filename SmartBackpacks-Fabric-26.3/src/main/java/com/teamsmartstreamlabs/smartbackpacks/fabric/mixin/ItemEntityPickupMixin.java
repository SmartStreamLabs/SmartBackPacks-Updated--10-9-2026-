package com.teamsmartstreamlabs.smartbackpacks.fabric.mixin;

import com.teamsmartstreamlabs.smartbackpacks.upgrade.MagnetUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.PickupUpgradeHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemEntity.class})
public abstract class ItemEntityPickupMixin {
   @Inject(
      method = {"playerTouch"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void smartbackpacks$routePickupIntoBackpackFirst(Player player, CallbackInfo ci) {
      if (player instanceof ServerPlayer serverPlayer && !serverPlayer.level().isClientSide()) {
         ItemEntity itemEntity = (ItemEntity)(Object)this;
         boolean handled = MagnetUpgradeHandler.tryDirectPickup(serverPlayer, itemEntity);
         handled = PickupUpgradeHandler.tryDirectPickup(serverPlayer, itemEntity) || handled;
         if (handled) {
            ci.cancel();
         }
      }
   }
}
