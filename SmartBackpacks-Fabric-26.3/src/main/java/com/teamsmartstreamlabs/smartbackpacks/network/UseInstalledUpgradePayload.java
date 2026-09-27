package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.BackpackMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.MagnetUpgradeMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UseInstalledUpgradePayload(int upgradeSlot, boolean shiftDown) implements CustomPacketPayload {
    public static final Type<UseInstalledUpgradePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "use_installed_upgrade"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UseInstalledUpgradePayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeVarInt(payload.upgradeSlot);
                buffer.writeBoolean(payload.shiftDown);
            },
            buffer -> new UseInstalledUpgradePayload(buffer.readVarInt(), buffer.readBoolean())
    );

    @Override
    public Type<UseInstalledUpgradePayload> type() {
        return TYPE;
    }

    public static void handle(UseInstalledUpgradePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                    && serverPlayer.containerMenu instanceof BackpackMenu menu) {
                menu.handleInstalledUpgradeRightClick(serverPlayer, payload.upgradeSlot(), payload.shiftDown());
            } else if (context.player() instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                    && serverPlayer.containerMenu instanceof MagnetUpgradeMenu menu
                    && payload.shiftDown()) {
                menu.toggleCourierDestination(serverPlayer);
            }
        });
    }
}

