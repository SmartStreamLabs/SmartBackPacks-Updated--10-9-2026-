package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.MagnetUpgradeMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RemoveMagnetUpgradeModPayload(int modIndex) implements CustomPacketPayload {
    public static final Type<RemoveMagnetUpgradeModPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "remove_magnet_upgrade_mod"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveMagnetUpgradeModPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeVarInt(payload.modIndex),
            buffer -> new RemoveMagnetUpgradeModPayload(buffer.readVarInt())
    );

    @Override
    public Type<RemoveMagnetUpgradeModPayload> type() {
        return TYPE;
    }

    public static void handle(RemoveMagnetUpgradeModPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof MagnetUpgradeMenu menu) {
                menu.removeModFilter(payload.modIndex());
            }
        });
    }
}

