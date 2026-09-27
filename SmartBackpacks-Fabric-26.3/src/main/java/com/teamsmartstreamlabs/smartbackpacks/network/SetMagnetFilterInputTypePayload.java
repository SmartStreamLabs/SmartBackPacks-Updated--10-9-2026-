package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.MagnetUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.MagnetFilterInputType;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetMagnetFilterInputTypePayload(String inputType) implements CustomPacketPayload {
    public static final Type<SetMagnetFilterInputTypePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "set_magnet_filter_input_type"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetMagnetFilterInputTypePayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeUtf(payload.inputType),
            buffer -> new SetMagnetFilterInputTypePayload(buffer.readUtf())
    );

    @Override
    public Type<SetMagnetFilterInputTypePayload> type() {
        return TYPE;
    }

    public static void handle(SetMagnetFilterInputTypePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof MagnetUpgradeMenu menu) {
                for (MagnetFilterInputType type : MagnetFilterInputType.values()) {
                    if (type.getSerializedName().equals(payload.inputType())) {
                        menu.setSelectedInputType(type);
                        break;
                    }
                }
            }
        });
    }
}

