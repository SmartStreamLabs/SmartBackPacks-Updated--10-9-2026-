package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.JukeboxUpgradeMenu;

import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.codec.StreamCodec;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record JukeboxUpgradeActionPayload(Action action) implements CustomPacketPayload {
    public enum Action {
        STOP,
        PLAY
    }

    public static final Type<JukeboxUpgradeActionPayload> TYPE =
            new Type<>(new ResourceLocation(SmartBackpacks.MOD_ID, "jukebox_upgrade_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, JukeboxUpgradeActionPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeEnum(payload.action),
            buffer -> new JukeboxUpgradeActionPayload(buffer.readEnum(Action.class))
    );

    @Override
    public Type<JukeboxUpgradeActionPayload> type() {
        return TYPE;
    }

    public static void handle(JukeboxUpgradeActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof JukeboxUpgradeMenu menu) {
                if (payload.action == Action.STOP) {
                    menu.stopPlayback();
                } else if (payload.action == Action.PLAY) {
                    menu.startPlayback();
                }
            }
        });
    }
}


