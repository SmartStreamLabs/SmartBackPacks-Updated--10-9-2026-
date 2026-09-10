package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.BackpackMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TrashCanActionPayload(Action action, int slotIndex) implements CustomPacketPayload {
    public enum Action {
        TRASH_SLOT,
        DELETE_NOW,
        CONFIRM_DELETE,
        RESTORE_LAST
    }

    public static final Type<TrashCanActionPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "trash_can_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TrashCanActionPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeEnum(payload.action);
                buffer.writeVarInt(payload.slotIndex);
            },
            buffer -> new TrashCanActionPayload(buffer.readEnum(Action.class), buffer.readVarInt())
    );

    @Override
    public Type<TrashCanActionPayload> type() {
        return TYPE;
    }

    public static void handle(TrashCanActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof BackpackMenu menu)) {
                return;
            }

            switch (payload.action()) {
                case TRASH_SLOT -> menu.moveMenuSlotToTrash(payload.slotIndex());
                case DELETE_NOW -> menu.deleteTrashPending(false);
                case CONFIRM_DELETE -> menu.deleteTrashPending(true);
                case RESTORE_LAST -> menu.restoreLastTrashedItem();
            }
        });
    }
}
