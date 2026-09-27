package com.teamsmartstreamlabs.smartbackpacks.fabric.mixin;

import com.teamsmartstreamlabs.smartbackpacks.protection.BackpackDropConfirmation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class BackpackDropServerMixin {
    @Inject(method = "handlePlayerAction", at = @At("HEAD"), cancellable = true)
    private void smartbackpacks$confirmDrop(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        var player = ((ServerGamePacketListenerImpl) (Object) this).player;
        if (!player.getServer().isSameThread()) return;
        var action = packet.getAction();
        if (action != ServerboundPlayerActionPacket.Action.DROP_ITEM
                && action != ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS) return;
        if (BackpackDropConfirmation.allow(player)) return;
        player.displayClientMessage(
                Component.literal("Are you sure you want to drop this backpack? Press Q again to confirm."), true);
        player.inventoryMenu.broadcastFullState();
        ci.cancel();
    }

    @Inject(method = "handleSetCarriedItem", at = @At("HEAD"))
    private void smartbackpacks$clearOnSlotChange(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
        var player = ((ServerGamePacketListenerImpl) (Object) this).player;
        if (player.getServer().isSameThread()
                && packet.getSlot() != player.getInventory().selected) {
            BackpackDropConfirmation.clear(player);
        }
    }
}
