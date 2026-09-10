package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.BackpackMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleItemLockPayload(Action action, int logicalSlot) implements CustomPacketPayload {
    public enum Action {
        TOGGLE_SLOT_LOCK,
        TOGGLE_ITEM_LOCK,
        TOGGLE_TYPE_LOCK
    }

    public static final Type<ToggleItemLockPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "toggle_item_lock"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleItemLockPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeEnum(payload.action);
                buffer.writeVarInt(payload.logicalSlot);
            },
            buffer -> new ToggleItemLockPayload(buffer.readEnum(Action.class), buffer.readVarInt())
    );

    @Override
    public Type<ToggleItemLockPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleItemLockPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof BackpackMenu backpackMenu) {
                backpackMenu.toggleItemLock(payload.action(), payload.logicalSlot(), context.player());
            }
        });
    }
}
