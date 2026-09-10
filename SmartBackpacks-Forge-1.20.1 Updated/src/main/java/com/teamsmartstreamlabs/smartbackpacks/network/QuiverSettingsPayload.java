package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.QuiverUpgradeMenu;

import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.codec.StreamCodec;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record QuiverSettingsPayload(Action action, int value) implements CustomPacketPayload {
    public enum Action {
        TOGGLE_ENABLED,
        CYCLE_SELECTION_MODE,
        CYCLE_SOURCE_PRIORITY,
        SET_PREFERRED_SLOT,
        MOVE_PREFERRED_PRIORITY,
        RESET_SETTINGS
    }

    public static final Type<QuiverSettingsPayload> TYPE =
            new Type<>(new ResourceLocation(SmartBackpacks.MOD_ID, "quiver_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, QuiverSettingsPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeEnum(payload.action);
                buffer.writeVarInt(payload.value);
            },
            buffer -> new QuiverSettingsPayload(buffer.readEnum(Action.class), buffer.readVarInt())
    );

    @Override
    public Type<QuiverSettingsPayload> type() {
        return TYPE;
    }

    public static void handle(QuiverSettingsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof QuiverUpgradeMenu menu)) {
                return;
            }

            switch (payload.action()) {
                case TOGGLE_ENABLED -> menu.setEnabled(payload.value() != 0);
                case CYCLE_SELECTION_MODE -> menu.cycleSelectionMode();
                case CYCLE_SOURCE_PRIORITY -> menu.cycleSourcePriority();
                case SET_PREFERRED_SLOT -> menu.setPreferredSlot(payload.value());
                case MOVE_PREFERRED_PRIORITY -> menu.movePreferredPriority(payload.value());
                case RESET_SETTINGS -> menu.resetSettings();
            }
        });
    }
}
