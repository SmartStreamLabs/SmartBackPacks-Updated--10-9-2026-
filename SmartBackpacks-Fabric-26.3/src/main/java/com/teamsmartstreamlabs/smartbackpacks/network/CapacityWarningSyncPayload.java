package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.client.CapacityWarningHud;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacityWarningState;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CapacityWarningSyncPayload(
        String backpackName,
        int percentage,
        CapacityWarningState state,
        int occupiedSlots,
        int totalSlots,
        int freeSlots,
        boolean showHud) implements CustomPacketPayload {
    public static final Type<CapacityWarningSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "capacity_warning_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CapacityWarningSyncPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeUtf(payload.backpackName, 80);
                buffer.writeVarInt(payload.percentage);
                buffer.writeEnum(payload.state);
                buffer.writeVarInt(payload.occupiedSlots);
                buffer.writeVarInt(payload.totalSlots);
                buffer.writeVarInt(payload.freeSlots);
                buffer.writeBoolean(payload.showHud);
            },
            buffer -> new CapacityWarningSyncPayload(
                    buffer.readUtf(80),
                    buffer.readVarInt(),
                    buffer.readEnum(CapacityWarningState.class),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readBoolean())
    );

    @Override
    public Type<CapacityWarningSyncPayload> type() {
        return TYPE;
    }

    public static void handle(CapacityWarningSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> CapacityWarningHud.handleSync(payload));
    }
}
