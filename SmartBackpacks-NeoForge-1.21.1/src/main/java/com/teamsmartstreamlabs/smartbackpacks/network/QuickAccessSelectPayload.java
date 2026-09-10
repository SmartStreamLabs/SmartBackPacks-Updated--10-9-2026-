package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.QuickAccessWheelHandler;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record QuickAccessSelectPayload(int favoriteIndex) implements CustomPacketPayload {
    public static final Type<QuickAccessSelectPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "quick_access_select"));
    public static final StreamCodec<RegistryFriendlyByteBuf, QuickAccessSelectPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeVarInt(payload.favoriteIndex),
            buffer -> new QuickAccessSelectPayload(buffer.readVarInt()));

    @Override
    public Type<QuickAccessSelectPayload> type() {
        return TYPE;
    }

    public static void handle(QuickAccessSelectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                QuickAccessWheelHandler.select(player, payload.favoriteIndex());
            }
        });
    }
}
