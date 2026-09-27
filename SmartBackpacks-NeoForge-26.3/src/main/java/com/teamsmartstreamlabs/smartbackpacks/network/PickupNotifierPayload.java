package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.client.PickupNotifierHud;
import com.teamsmartstreamlabs.smartbackpacks.pickup.PickupNotifierDestination;
import com.teamsmartstreamlabs.smartbackpacks.pickup.PickupNotifierSource;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
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
            new Type<>(Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "pickup_notifier"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PickupNotifierPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, payload.stack);
                buffer.writeVarInt(payload.amount);
                buffer.writeVarInt(payload.total);
                buffer.writeUtf(payload.backpackName, 80);
                buffer.writeEnum(payload.destination);
                buffer.writeEnum(payload.source);
            },
            buffer -> new PickupNotifierPayload(
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer),
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
