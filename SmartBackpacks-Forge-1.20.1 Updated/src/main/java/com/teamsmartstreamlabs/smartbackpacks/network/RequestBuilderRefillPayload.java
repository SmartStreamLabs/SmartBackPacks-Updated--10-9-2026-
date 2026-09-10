package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BuilderUpgradeHandler;

import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.codec.StreamCodec;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public enum RequestBuilderRefillPayload implements CustomPacketPayload {
    INSTANCE;

    public static final Type<RequestBuilderRefillPayload> TYPE =
            new Type<>(new ResourceLocation(SmartBackpacks.MOD_ID, "request_builder_refill"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestBuilderRefillPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<RequestBuilderRefillPayload> type() {
        return TYPE;
    }

    public static void handle(RequestBuilderRefillPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.player();
            if (player != null) {
                BuilderUpgradeHandler.requestManualRefill(player);
            }
        });
    }
}
