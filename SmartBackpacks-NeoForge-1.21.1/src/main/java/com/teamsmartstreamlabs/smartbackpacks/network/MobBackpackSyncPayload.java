package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.mobbackpack.MobBackpackClientData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MobBackpackSyncPayload(int entityId, ItemStack backpack) implements CustomPacketPayload {
    public static final Type<MobBackpackSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "mob_backpack_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MobBackpackSyncPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeVarInt(payload.entityId());
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, payload.backpack());
            },
            buffer -> new MobBackpackSyncPayload(buffer.readVarInt(), ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer)));

    public MobBackpackSyncPayload {
        backpack = backpack.copy();
    }

    @Override
    public Type<MobBackpackSyncPayload> type() {
        return TYPE;
    }

    public static void handle(MobBackpackSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> MobBackpackClientData.update(payload.entityId(), payload.backpack()));
    }
}
