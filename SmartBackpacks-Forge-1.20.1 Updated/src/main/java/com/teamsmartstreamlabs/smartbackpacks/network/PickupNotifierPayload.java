package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.client.PickupNotifierHud;
import com.teamsmartstreamlabs.smartbackpacks.pickup.PickupNotifierDestination;
import com.teamsmartstreamlabs.smartbackpacks.pickup.PickupNotifierSource;

import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.codec.StreamCodec;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PickupNotifierPayload(
        ItemStack stack,
        int amount,
        int total,
        String backpackName,
        PickupNotifierDestination destination,
        PickupNotifierSource source) implements CustomPacketPayload {
    public static final Type<PickupNotifierPayload> TYPE =
            new Type<>(new ResourceLocation(SmartBackpacks.MOD_ID, "pickup_notifier"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PickupNotifierPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeItem(payload.stack);
                buffer.writeVarInt(payload.amount);
                buffer.writeVarInt(payload.total);
                buffer.writeUtf(payload.backpackName, 80);
                buffer.writeEnum(payload.destination);
                buffer.writeEnum(payload.source);
            },
            buffer -> new PickupNotifierPayload(
                    buffer.readItem(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readUtf(80),
                    buffer.readEnum(PickupNotifierDestination.class),
                    buffer.readEnum(PickupNotifierSource.class))
    );

    @Override
    public Type<PickupNotifierPayload> type() {
        return TYPE;
    }

    public static void handle(PickupNotifierPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> PickupNotifierHud.handle(payload));
    }
}
