package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.ChunkLoaderUpgradeMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ChunkLoaderRadiusPayload(int delta) implements CustomPacketPayload {
    public static final Type<ChunkLoaderRadiusPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "chunk_loader_radius"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChunkLoaderRadiusPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeVarInt(payload.delta),
            buffer -> new ChunkLoaderRadiusPayload(buffer.readVarInt())
    );

    @Override
    public Type<ChunkLoaderRadiusPayload> type() {
        return TYPE;
    }

    public static void handle(ChunkLoaderRadiusPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof ChunkLoaderUpgradeMenu menu) {
                menu.adjustRadius(payload.delta());
            }
        });
    }
}
