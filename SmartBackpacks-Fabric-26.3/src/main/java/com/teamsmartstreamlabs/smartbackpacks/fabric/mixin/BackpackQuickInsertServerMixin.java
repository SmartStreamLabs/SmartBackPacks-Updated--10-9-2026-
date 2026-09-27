package com.teamsmartstreamlabs.smartbackpacks.fabric.mixin;

import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class BackpackQuickInsertServerMixin {
    @Inject(method = "handleContainerClick", at = @At("HEAD"), cancellable = true)
    private void smartbackpacks$rejectStaleQuickInsert(ServerboundContainerClickPacket packet, CallbackInfo ci) {
        var player = ((ServerGamePacketListenerImpl) (Object) this).player;
        if (!player.level().getServer().isSameThread()) {
            return;
        }
        AbstractContainerMenu menu = player.containerMenu;
        int slotId = packet.slotNum();
        if (menu != player.inventoryMenu || packet.containerId() != menu.containerId
                || packet.containerInput() != ContainerInput.PICKUP || packet.buttonNum() != 1
                || slotId < 0 || slotId >= menu.slots.size() || menu.getCarried().isEmpty()
                || !(menu.slots.get(slotId).getItem().getItem() instanceof BackpackItem)) {
            return;
        }
        if (packet.stateId() != menu.getStateId()) {
            menu.broadcastFullState();
            ci.cancel();
        }
    }
}
