package com.teamsmartstreamlabs.smartbackpacks.mixin;

import com.teamsmartstreamlabs.smartbackpacks.protection.BackpackDropConfirmation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class BackpackDropClientMixin {
    @Inject(method = "drop", at = @At("HEAD"), cancellable = true)
    private void smartbackpacks$confirmDrop(boolean all, CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        if (BackpackDropConfirmation.allow(player)) return;
        player.connection.send(new ServerboundPlayerActionPacket(
                all ? ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS
                        : ServerboundPlayerActionPacket.Action.DROP_ITEM,
                BlockPos.ZERO, Direction.DOWN));
        cir.setReturnValue(false);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void smartbackpacks$trackSelectedSlot(CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) BackpackDropConfirmation.reconcile(player);
    }
}
