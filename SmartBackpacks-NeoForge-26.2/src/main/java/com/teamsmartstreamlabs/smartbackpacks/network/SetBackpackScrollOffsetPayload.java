package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.BackpackMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetBackpackScrollOffsetPayload(int firstVisibleRow) implements CustomPacketPayload {
    public static final Type<SetBackpackScrollOffsetPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "set_backpack_scroll_offset"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetBackpackScrollOffsetPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeVarInt(payload.firstVisibleRow),
            buffer -> new SetBackpackScrollOffsetPayload(buffer.readVarInt()));

    @Override
    public Type<SetBackpackScrollOffsetPayload> type() {
        return TYPE;
    }

    public static void handle(SetBackpackScrollOffsetPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof BackpackMenu backpackMenu) {
                backpackMenu.setFirstVisibleRow(payload.firstVisibleRow());
            }
        });
    }
}

