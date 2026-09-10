package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.MagnetUpgradeMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetMagnetUpgradeModePayload(boolean allowlist) implements CustomPacketPayload {
    public static final Type<SetMagnetUpgradeModePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "set_magnet_upgrade_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetMagnetUpgradeModePayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeBoolean(payload.allowlist),
            buffer -> new SetMagnetUpgradeModePayload(buffer.readBoolean())
    );

    @Override
    public Type<SetMagnetUpgradeModePayload> type() {
        return TYPE;
    }

    public static void handle(SetMagnetUpgradeModePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof MagnetUpgradeMenu menu) {
                menu.setAllowlist(payload.allowlist());
            }
        });
    }
}

