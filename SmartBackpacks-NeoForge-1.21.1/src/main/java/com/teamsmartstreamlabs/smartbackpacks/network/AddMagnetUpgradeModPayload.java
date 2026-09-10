package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.MagnetUpgradeMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AddMagnetUpgradeModPayload(String modId) implements CustomPacketPayload {
    public static final Type<AddMagnetUpgradeModPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "add_magnet_upgrade_mod"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AddMagnetUpgradeModPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeUtf(payload.modId),
            buffer -> new AddMagnetUpgradeModPayload(buffer.readUtf())
    );

    @Override
    public Type<AddMagnetUpgradeModPayload> type() {
        return TYPE;
    }

    public static void handle(AddMagnetUpgradeModPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof MagnetUpgradeMenu menu) {
                menu.addModFilter(payload.modId().trim());
            }
        });
    }
}

