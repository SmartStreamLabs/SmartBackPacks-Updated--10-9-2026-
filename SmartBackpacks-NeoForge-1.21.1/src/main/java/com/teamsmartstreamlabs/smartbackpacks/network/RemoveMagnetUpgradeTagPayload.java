package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.MagnetUpgradeMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RemoveMagnetUpgradeTagPayload(int tagIndex) implements CustomPacketPayload {
    public static final Type<RemoveMagnetUpgradeTagPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "remove_magnet_upgrade_tag"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveMagnetUpgradeTagPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeVarInt(payload.tagIndex),
            buffer -> new RemoveMagnetUpgradeTagPayload(buffer.readVarInt())
    );

    @Override
    public Type<RemoveMagnetUpgradeTagPayload> type() {
        return TYPE;
    }

    public static void handle(RemoveMagnetUpgradeTagPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof MagnetUpgradeMenu menu) {
                menu.removeTag(payload.tagIndex());
            }
        });
    }
}

