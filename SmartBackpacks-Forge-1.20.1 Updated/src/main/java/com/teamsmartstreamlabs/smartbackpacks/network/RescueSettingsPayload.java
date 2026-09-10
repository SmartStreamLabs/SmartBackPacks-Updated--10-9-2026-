package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.RescueUpgradeMenu;

import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.codec.StreamCodec;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RescueSettingsPayload(Action action, int value) implements CustomPacketPayload {
    public enum Action {
        TOGGLE_ENABLED,
        TOGGLE_TOTEM,
        TOGGLE_GOLDEN_APPLE,
        TOGGLE_FALL,
        TOGGLE_LAVA,
        CHANGE_HEALTH_THRESHOLD,
        CHANGE_LAVA_THRESHOLD,
        CHANGE_FALL_DISTANCE
    }

    public static final Type<RescueSettingsPayload> TYPE =
            new Type<>(new ResourceLocation(SmartBackpacks.MOD_ID, "rescue_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RescueSettingsPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeEnum(payload.action);
                buffer.writeVarInt(payload.value);
            },
            buffer -> new RescueSettingsPayload(buffer.readEnum(Action.class), buffer.readVarInt())
    );

    @Override
    public Type<RescueSettingsPayload> type() {
        return TYPE;
    }

    public static void handle(RescueSettingsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof RescueUpgradeMenu menu)) {
                return;
            }

            switch (payload.action()) {
                case TOGGLE_ENABLED -> menu.setEnabled(payload.value() != 0);
                case TOGGLE_TOTEM -> menu.toggleTotem();
                case TOGGLE_GOLDEN_APPLE -> menu.toggleGoldenApple();
                case TOGGLE_FALL -> menu.toggleFall();
                case TOGGLE_LAVA -> menu.toggleLava();
                case CHANGE_HEALTH_THRESHOLD -> menu.changeHealthThreshold(payload.value());
                case CHANGE_LAVA_THRESHOLD -> menu.changeLavaThreshold(payload.value());
                case CHANGE_FALL_DISTANCE -> menu.changeFallDistance(payload.value());
            }
        });
    }
}
