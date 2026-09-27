package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.FluidTransferUpgradeMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FluidTransferActionPayload(Action action) implements CustomPacketPayload {
    public enum Action {
        TOGGLE_MODE,
        TOGGLE_SOURCE,
        FILL,
        DRAIN
    }

    public static final Type<FluidTransferActionPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "fluid_transfer_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidTransferActionPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeEnum(payload.action),
            buffer -> new FluidTransferActionPayload(buffer.readEnum(Action.class))
    );

    @Override
    public Type<FluidTransferActionPayload> type() {
        return TYPE;
    }

    public static void handle(FluidTransferActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof FluidTransferUpgradeMenu menu) {
                switch (payload.action()) {
                    case TOGGLE_MODE -> menu.toggleMode();
                    case TOGGLE_SOURCE -> menu.toggleCollectSourceBlocks();
                    case FILL -> menu.fillTankFromContainer();
                    case DRAIN -> menu.fillContainerFromTank();
                }
            }
        });
    }
}

