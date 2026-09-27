package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.AutoToolUpgradeHandler;

import net.minecraft.core.BlockPos;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.codec.StreamCodec;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestAutoToolSwapPayload(BlockPos blockPos, boolean manual) implements CustomPacketPayload {
    public static final Type<RequestAutoToolSwapPayload> TYPE =
            new Type<>(new ResourceLocation(SmartBackpacks.MOD_ID, "request_auto_tool_swap"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestAutoToolSwapPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeBlockPos(payload.blockPos);
                buffer.writeBoolean(payload.manual);
            },
            buffer -> new RequestAutoToolSwapPayload(buffer.readBlockPos(), buffer.readBoolean())
    );

    @Override
    public Type<RequestAutoToolSwapPayload> type() {
        return TYPE;
    }

    public static void handle(RequestAutoToolSwapPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            net.minecraft.server.level.ServerPlayer serverPlayer = context.player();
            if (serverPlayer != null) {
                AutoToolUpgradeHandler.trySwapBestTool(serverPlayer, payload.blockPos(), payload.manual());
            }
        });
    }
}


