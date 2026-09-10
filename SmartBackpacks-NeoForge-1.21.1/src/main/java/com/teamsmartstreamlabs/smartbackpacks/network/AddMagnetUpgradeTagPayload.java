package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.MagnetUpgradeMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AddMagnetUpgradeTagPayload(String tagId) implements CustomPacketPayload {
    public static final Type<AddMagnetUpgradeTagPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "add_magnet_upgrade_tag"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AddMagnetUpgradeTagPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeUtf(payload.tagId),
            buffer -> new AddMagnetUpgradeTagPayload(buffer.readUtf())
    );

    @Override
    public Type<AddMagnetUpgradeTagPayload> type() {
        return TYPE;
    }

    public static void handle(AddMagnetUpgradeTagPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof MagnetUpgradeMenu menu) {
                ResourceLocation tagId = ResourceLocation.tryParse(payload.tagId());
                if (tagId != null) {
                    menu.addTag(tagId);
                }
            }
        });
    }
}

