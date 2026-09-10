package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.FluidStorageUpgradeMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FluidStorageActionPayload(Action action) implements CustomPacketPayload {
    public enum Action {
        FILL,
        DRAIN
    }

    public static final Type<FluidStorageActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "fluid_storage_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStorageActionPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeEnum(payload.action),
            buffer -> new FluidStorageActionPayload(buffer.readEnum(Action.class))
    );

    @Override
    public Type<FluidStorageActionPayload> type() {
        return TYPE;
    }

    public static void handle(FluidStorageActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof FluidStorageUpgradeMenu menu) {
                switch (payload.action()) {
                    case FILL -> menu.fillTankFromContainer();
                    case DRAIN -> menu.fillContainerFromTank();
                }
            }
        });
    }
}

