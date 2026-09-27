package com.teamsmartstreamlabs.smartbackpacks.mixin;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackQuickInsert;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class BackpackQuickInsertMixin {
    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void smartbackpacks$quickInsert(int slotId, int button, ContainerInput input, Player player, CallbackInfo ci) {
        if (BackpackQuickInsert.handle((AbstractContainerMenu) (Object) this, slotId, button, input, player)) {
            ci.cancel();
        }
    }
}
