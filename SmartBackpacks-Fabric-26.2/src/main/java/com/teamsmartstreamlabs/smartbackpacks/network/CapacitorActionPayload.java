package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.CapacitorUpgradeMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CapacitorActionPayload(Action action) implements CustomPacketPayload {
    public enum Action {
        CHARGE_ITEM,
        STORE_ITEM
    }

    public static final Type<CapacitorActionPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "capacitor_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CapacitorActionPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeEnum(payload.action),
            buffer -> new CapacitorActionPayload(buffer.readEnum(Action.class))
    );

    @Override
    public Type<CapacitorActionPayload> type() {
        return TYPE;
    }

    public static void handle(CapacitorActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof CapacitorUpgradeMenu menu) {
                switch (payload.action()) {
                    case CHARGE_ITEM -> menu.chargeItemFromCapacitor();
                    case STORE_ITEM -> menu.storeItemEnergyInCapacitor();
                }
            }
        });
    }
}

