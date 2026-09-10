package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackHelper;

import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.codec.StreamCodec;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public final class OpenWornBackpackPayload implements CustomPacketPayload {
    public static final OpenWornBackpackPayload INSTANCE = new OpenWornBackpackPayload();
    public static final Type<OpenWornBackpackPayload> TYPE = new Type<>(new ResourceLocation(SmartBackpacks.MOD_ID, "open_worn_backpack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenWornBackpackPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private OpenWornBackpackPayload() {
    }

    @Override
    public Type<OpenWornBackpackPayload> type() {
        return TYPE;
    }

    public static void handle(OpenWornBackpackPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.player();
            if (player != null) {
                BackpackAccess access = BackpackHelper.findWornBackpackAccess(player);
                if (access == null) {
                    access = BackpackHelper.findWirelessBackpackAccess(player);
                }
                if (access != null) {
                    BackpackHelper.openBackpack(player, access);
                }
            }
        });
    }
}


