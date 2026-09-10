package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.BackpackLinkUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.menu.BackpackLinkUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BackpackLinkSnapshot;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BackpackLinkMenuSyncPayload(int containerId, BackpackLinkSnapshot snapshot) implements CustomPacketPayload {
    public static final Type<BackpackLinkMenuSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "backpack_link_menu_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BackpackLinkMenuSyncPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeVarInt(payload.containerId);
                BackpackLinkSnapshot.write(buffer, payload.snapshot);
            },
            buffer -> new BackpackLinkMenuSyncPayload(buffer.readVarInt(), BackpackLinkSnapshot.read(buffer))
    );

    @Override
    public Type<BackpackLinkMenuSyncPayload> type() {
        return TYPE;
    }

    public static void handle(BackpackLinkMenuSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof BackpackLinkUpgradeMenu menu && menu.containerId == payload.containerId()) {
                menu.updateSnapshot(payload.snapshot());
            }
            if (Minecraft.getInstance().screen instanceof BackpackLinkUpgradeScreen screen) {
                screen.refreshAfterSync();
            }
        });
    }
}
