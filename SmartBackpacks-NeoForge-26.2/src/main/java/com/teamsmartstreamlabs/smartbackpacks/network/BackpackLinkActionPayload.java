package com.teamsmartstreamlabs.smartbackpacks.network;

import java.util.UUID;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.BackpackLinkUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BackpackLinkData;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BackpackLinkActionPayload(Action action, UUID targetId, String value) implements CustomPacketPayload {
    public enum Action {
        TOGGLE_ACTIVE,
        RENAME,
        CYCLE_VISIBILITY,
        BIND_OR_COMPLETE_CRYSTAL,
        TELEPORT,
        UNLINK
    }

    public static final Type<BackpackLinkActionPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "backpack_link_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BackpackLinkActionPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeEnum(payload.action);
                buffer.writeUUID(payload.targetId);
                buffer.writeUtf(payload.value, 64);
            },
            buffer -> new BackpackLinkActionPayload(buffer.readEnum(Action.class), buffer.readUUID(), buffer.readUtf(64))
    );

    public static BackpackLinkActionPayload simple(Action action) {
        return new BackpackLinkActionPayload(action, BackpackLinkData.EMPTY_UUID, "");
    }

    @Override
    public Type<BackpackLinkActionPayload> type() {
        return TYPE;
    }

    public static void handle(BackpackLinkActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof BackpackLinkUpgradeMenu menu)) {
                return;
            }

            switch (payload.action()) {
                case TOGGLE_ACTIVE -> menu.toggleActive();
                case RENAME -> menu.rename(payload.value());
                case CYCLE_VISIBILITY -> menu.cycleVisibility();
                case BIND_OR_COMPLETE_CRYSTAL -> menu.bindOrCompleteCrystal();
                case TELEPORT -> menu.teleport(payload.targetId());
                case UNLINK -> menu.unlink(payload.targetId());
            }
        });
    }
}
