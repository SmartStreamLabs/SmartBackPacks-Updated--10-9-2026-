package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.SurvivalAssistUpgradeMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SurvivalAssistEffectPayload(Action action, String effectId, int index) implements CustomPacketPayload {
    public enum Action {
        ADD_EFFECT,
        REMOVE_EFFECT
    }

    public static final Type<SurvivalAssistEffectPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "survival_assist_effect"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SurvivalAssistEffectPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeEnum(payload.action);
                buffer.writeUtf(payload.effectId);
                buffer.writeVarInt(payload.index);
            },
            buffer -> new SurvivalAssistEffectPayload(buffer.readEnum(Action.class), buffer.readUtf(), buffer.readVarInt())
    );

    @Override
    public Type<SurvivalAssistEffectPayload> type() {
        return TYPE;
    }

    public static void handle(SurvivalAssistEffectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof SurvivalAssistUpgradeMenu menu)) {
                return;
            }

            if (payload.action() == Action.ADD_EFFECT) {
                ResourceLocation effectId = ResourceLocation.tryParse(payload.effectId());
                if (effectId != null) {
                    menu.addEffectFilter(effectId);
                }
            } else if (payload.action() == Action.REMOVE_EFFECT) {
                menu.removeEffectFilter(payload.index());
            }
        });
    }
}

