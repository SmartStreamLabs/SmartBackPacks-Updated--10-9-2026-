package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.BackpackMenu;

import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.codec.StreamCodec;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetBackpackScrollOffsetPayload(int firstVisibleRow) implements CustomPacketPayload {
    public static final Type<SetBackpackScrollOffsetPayload> TYPE =
            new Type<>(new ResourceLocation(SmartBackpacks.MOD_ID, "set_backpack_scroll_offset"));
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


