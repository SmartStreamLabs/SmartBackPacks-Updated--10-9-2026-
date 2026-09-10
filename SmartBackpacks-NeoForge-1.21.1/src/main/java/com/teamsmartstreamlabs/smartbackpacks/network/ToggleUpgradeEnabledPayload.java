package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.BackpackMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleUpgradeEnabledPayload(int upgradeSlot) implements CustomPacketPayload {
    public static final Type<ToggleUpgradeEnabledPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "toggle_upgrade_enabled"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleUpgradeEnabledPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeVarInt(payload.upgradeSlot),
            buffer -> new ToggleUpgradeEnabledPayload(buffer.readVarInt())
    );

    @Override
    public Type<ToggleUpgradeEnabledPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleUpgradeEnabledPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof BackpackMenu menu) {
                menu.toggleUpgradeEnabled(payload.upgradeSlot());
            }
        });
    }
}

