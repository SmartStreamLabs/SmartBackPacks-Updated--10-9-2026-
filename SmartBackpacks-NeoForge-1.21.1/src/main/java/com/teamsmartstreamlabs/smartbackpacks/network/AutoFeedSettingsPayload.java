package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.AutoFeedUpgradeMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AutoFeedSettingsPayload(Action action, int value) implements CustomPacketPayload {
    public enum Action {
        TOGGLE_ENABLED,
        SET_HUNGER_THRESHOLD,
        SET_SATURATION_THRESHOLD
    }

    public static final Type<AutoFeedSettingsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "auto_feed_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AutoFeedSettingsPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeEnum(payload.action);
                buffer.writeVarInt(payload.value);
            },
            buffer -> new AutoFeedSettingsPayload(buffer.readEnum(Action.class), buffer.readVarInt())
    );

    @Override
    public Type<AutoFeedSettingsPayload> type() {
        return TYPE;
    }

    public static void handle(AutoFeedSettingsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof AutoFeedUpgradeMenu menu)) {
                return;
            }

            switch (payload.action()) {
                case TOGGLE_ENABLED -> menu.setEnabled(payload.value() != 0);
                case SET_HUNGER_THRESHOLD -> menu.setHungerThreshold(payload.value());
                case SET_SATURATION_THRESHOLD -> menu.setSaturationThreshold(payload.value());
            }
        });
    }
}

