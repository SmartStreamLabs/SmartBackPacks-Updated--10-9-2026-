package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.CapacityWarningUpgradeMenu;

import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.codec.StreamCodec;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CapacityWarningSettingsPayload(Action action, int value) implements CustomPacketPayload {
    public enum Action {
        TOGGLE_ENABLED,
        CYCLE_CALCULATION_MODE,
        TOGGLE_THRESHOLD_1,
        TOGGLE_THRESHOLD_2,
        TOGGLE_THRESHOLD_3,
        CHANGE_THRESHOLD_1,
        CHANGE_THRESHOLD_2,
        CHANGE_THRESHOLD_3,
        CHANGE_RESET_MARGIN,
        TOGGLE_ACTION_BAR,
        TOGGLE_SOUND,
        TOGGLE_HUD,
        CYCLE_HUD_MODE,
        TOGGLE_PERCENTAGE,
        TOGGLE_SLOT_COUNT,
        TOGGLE_FAILED_INSERTION,
        PREVIEW
    }

    public static final Type<CapacityWarningSettingsPayload> TYPE =
            new Type<>(new ResourceLocation(SmartBackpacks.MOD_ID, "capacity_warning_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CapacityWarningSettingsPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeEnum(payload.action);
                buffer.writeVarInt(payload.value);
            },
            buffer -> new CapacityWarningSettingsPayload(buffer.readEnum(Action.class), buffer.readVarInt())
    );

    @Override
    public Type<CapacityWarningSettingsPayload> type() {
        return TYPE;
    }

    public static void handle(CapacityWarningSettingsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof CapacityWarningUpgradeMenu menu)) {
                return;
            }

            switch (payload.action()) {
                case TOGGLE_ENABLED -> menu.setEnabled(payload.value() != 0);
                case CYCLE_CALCULATION_MODE -> menu.cycleCalculationMode();
                case TOGGLE_THRESHOLD_1 -> menu.toggleThreshold(0);
                case TOGGLE_THRESHOLD_2 -> menu.toggleThreshold(1);
                case TOGGLE_THRESHOLD_3 -> menu.toggleThreshold(2);
                case CHANGE_THRESHOLD_1 -> menu.changeThreshold(0, payload.value());
                case CHANGE_THRESHOLD_2 -> menu.changeThreshold(1, payload.value());
                case CHANGE_THRESHOLD_3 -> menu.changeThreshold(2, payload.value());
                case CHANGE_RESET_MARGIN -> menu.changeResetMargin(payload.value());
                case TOGGLE_ACTION_BAR -> menu.toggleActionBar();
                case TOGGLE_SOUND -> menu.toggleSound();
                case TOGGLE_HUD -> menu.toggleHud();
                case CYCLE_HUD_MODE -> menu.cycleHudMode();
                case TOGGLE_PERCENTAGE -> menu.toggleShowPercentage();
                case TOGGLE_SLOT_COUNT -> menu.toggleShowSlotCount();
                case TOGGLE_FAILED_INSERTION -> menu.toggleFailedInsertionWarning();
                case PREVIEW -> menu.previewFeedback();
            }
        });
    }
}
