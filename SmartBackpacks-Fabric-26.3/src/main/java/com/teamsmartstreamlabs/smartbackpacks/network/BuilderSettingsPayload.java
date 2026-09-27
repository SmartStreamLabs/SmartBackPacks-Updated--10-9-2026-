package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.BuilderUpgradeMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BuilderSettingsPayload(Action action, int value) implements CustomPacketPayload {
    public enum Action {
        TOGGLE_ENABLED,
        CYCLE_REFILL_MODE,
        CYCLE_MATCH_MODE,
        CYCLE_FILTER_MODE,
        TOGGLE_MAIN_HAND,
        TOGGLE_OFFHAND,
        TOGGLE_SCAFFOLDING,
        TOGGLE_MODDED_BLOCKS,
        TOGGLE_DANGEROUS_PROTECTION,
        TOGGLE_FEEDBACK,
        CHANGE_THRESHOLD
    }

    public static final Type<BuilderSettingsPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "builder_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BuilderSettingsPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeEnum(payload.action);
                buffer.writeVarInt(payload.value);
            },
            buffer -> new BuilderSettingsPayload(buffer.readEnum(Action.class), buffer.readVarInt())
    );

    @Override
    public Type<BuilderSettingsPayload> type() {
        return TYPE;
    }

    public static void handle(BuilderSettingsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof BuilderUpgradeMenu menu)) {
                return;
            }

            switch (payload.action()) {
                case TOGGLE_ENABLED -> menu.setEnabled(payload.value() != 0);
                case CYCLE_REFILL_MODE -> menu.cycleRefillMode();
                case CYCLE_MATCH_MODE -> menu.cycleMatchMode();
                case CYCLE_FILTER_MODE -> menu.cycleFilterMode();
                case TOGGLE_MAIN_HAND -> menu.toggleMainHand();
                case TOGGLE_OFFHAND -> menu.toggleOffhand();
                case TOGGLE_SCAFFOLDING -> menu.toggleScaffolding();
                case TOGGLE_MODDED_BLOCKS -> menu.toggleModdedBlocks();
                case TOGGLE_DANGEROUS_PROTECTION -> menu.toggleDangerousProtection();
                case TOGGLE_FEEDBACK -> menu.toggleFeedback();
                case CHANGE_THRESHOLD -> menu.changeThreshold(payload.value());
            }
        });
    }
}
