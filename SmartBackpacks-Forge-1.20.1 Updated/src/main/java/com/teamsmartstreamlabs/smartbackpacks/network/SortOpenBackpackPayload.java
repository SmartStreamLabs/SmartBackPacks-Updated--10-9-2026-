package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackSortMode;
import com.teamsmartstreamlabs.smartbackpacks.menu.BackpackMenu;

import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.codec.StreamCodec;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SortOpenBackpackPayload(BackpackSortMode sortMode) implements CustomPacketPayload {
    public static final Type<SortOpenBackpackPayload> TYPE = new Type<>(new ResourceLocation(SmartBackpacks.MOD_ID, "sort_open_backpack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SortOpenBackpackPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeEnum(payload.sortMode),
            buffer -> new SortOpenBackpackPayload(buffer.readEnum(BackpackSortMode.class))
    );

    @Override
    public Type<SortOpenBackpackPayload> type() {
        return TYPE;
    }

    public static void handle(SortOpenBackpackPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof BackpackMenu backpackMenu) {
                backpackMenu.sortContents(payload.sortMode());
            }
        });
    }
}


