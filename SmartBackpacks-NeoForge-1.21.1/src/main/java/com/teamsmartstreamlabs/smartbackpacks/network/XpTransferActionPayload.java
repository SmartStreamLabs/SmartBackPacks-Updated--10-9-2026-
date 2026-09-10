package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.XpTransferUpgradeMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record XpTransferActionPayload(Action action) implements CustomPacketPayload {
    public enum Action {
        STORE_STEP,
        STORE_ALL,
        WITHDRAW_STEP,
        WITHDRAW_ALL
    }

    public static final Type<XpTransferActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "xp_transfer_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, XpTransferActionPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeEnum(payload.action),
            buffer -> new XpTransferActionPayload(buffer.readEnum(Action.class))
    );

    @Override
    public Type<XpTransferActionPayload> type() {
        return TYPE;
    }

    public static void handle(XpTransferActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof XpTransferUpgradeMenu menu) {
                switch (payload.action()) {
                    case STORE_STEP -> menu.storeStep();
                    case STORE_ALL -> menu.storeAll();
                    case WITHDRAW_STEP -> menu.withdrawStep();
                    case WITHDRAW_ALL -> menu.withdrawAll();
                }
            }
        });
    }
}

