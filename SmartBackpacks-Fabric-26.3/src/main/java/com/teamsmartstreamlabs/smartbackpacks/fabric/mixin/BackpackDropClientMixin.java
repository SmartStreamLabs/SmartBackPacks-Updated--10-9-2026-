package com.teamsmartstreamlabs.smartbackpacks.fabric.mixin;

import com.teamsmartstreamlabs.smartbackpacks.protection.BackpackDropConfirmation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class BackpackDropClientMixin {
    @Shadow
    private void ensureHasSentCarriedItem() {
        throw new AssertionError();
    }

    @Inject(method = "dropItem", at = @At("HEAD"), cancellable = true)
    private void smartbackpacks$confirmDrop(LocalPlayer player, boolean all, CallbackInfo ci) {
        if (BackpackDropConfirmation.allow(player)) return;
        this.ensureHasSentCarriedItem();
        player.connection.send(new ServerboundPlayerActionPacket(
                all ? ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS
                        : ServerboundPlayerActionPacket.Action.DROP_ITEM,
                BlockPos.ZERO, Direction.DOWN));
        ci.cancel();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void smartbackpacks$trackSelectedSlot(CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) BackpackDropConfirmation.reconcile(player);
    }
}
