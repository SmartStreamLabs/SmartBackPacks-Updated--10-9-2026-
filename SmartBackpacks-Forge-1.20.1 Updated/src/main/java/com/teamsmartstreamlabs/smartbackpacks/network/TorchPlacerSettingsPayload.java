package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.TorchPlacerUpgradeMenu;

import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.codec.StreamCodec;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TorchPlacerSettingsPayload(Action action, int value) implements CustomPacketPayload {
    public enum Action {
        TOGGLE_ENABLED,
        CYCLE_PLACEMENT_MODE,
        CYCLE_MATCH_MODE,
        CYCLE_FILTER_MODE,
        TOGGLE_FLOOR,
        TOGGLE_WALL,
        TOGGLE_CEILING,
        TOGGLE_MODDED,
        TOGGLE_SPRINTING,
        TOGGLE_SNEAKING,
        TOGGLE_STANDING,
        TOGGLE_WATER,
        TOGGLE_LAVA,
        CHANGE_LIGHT_THRESHOLD,
        CHANGE_MIN_DISTANCE
    }

    public static final Type<TorchPlacerSettingsPayload> TYPE =
            new Type<>(new ResourceLocation(SmartBackpacks.MOD_ID, "torch_placer_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TorchPlacerSettingsPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeEnum(payload.action);
                buffer.writeVarInt(payload.value);
            },
            buffer -> new TorchPlacerSettingsPayload(buffer.readEnum(Action.class), buffer.readVarInt())
    );

    @Override
    public Type<TorchPlacerSettingsPayload> type() {
        return TYPE;
    }

    public static void handle(TorchPlacerSettingsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof TorchPlacerUpgradeMenu menu)) {
                return;
            }

            switch (payload.action()) {
                case TOGGLE_ENABLED -> menu.setEnabled(payload.value() != 0);
                case CYCLE_PLACEMENT_MODE -> menu.cyclePlacementMode();
                case CYCLE_MATCH_MODE -> menu.cycleMatchMode();
                case CYCLE_FILTER_MODE -> menu.cycleFilterMode();
                case TOGGLE_FLOOR -> menu.toggleFloorPlacement();
                case TOGGLE_WALL -> menu.toggleWallPlacement();
                case TOGGLE_CEILING -> menu.toggleCeilingPlacement();
                case TOGGLE_MODDED -> menu.toggleModdedLightSources();
                case TOGGLE_SPRINTING -> menu.toggleSprinting();
                case TOGGLE_SNEAKING -> menu.toggleSneaking();
                case TOGGLE_STANDING -> menu.toggleStandingStill();
                case TOGGLE_WATER -> menu.toggleWater();
                case TOGGLE_LAVA -> menu.toggleLava();
                case CHANGE_LIGHT_THRESHOLD -> menu.changeLightThreshold(payload.value());
                case CHANGE_MIN_DISTANCE -> menu.changeMinimumDistance(payload.value());
            }
        });
    }
}
