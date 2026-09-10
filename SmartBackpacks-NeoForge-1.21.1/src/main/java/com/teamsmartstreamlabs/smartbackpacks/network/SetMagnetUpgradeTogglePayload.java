package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.MagnetUpgradeMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetMagnetUpgradeTogglePayload(String option, boolean value) implements CustomPacketPayload {
    public static final Type<SetMagnetUpgradeTogglePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "set_magnet_upgrade_toggle"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetMagnetUpgradeTogglePayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeUtf(payload.option);
                buffer.writeBoolean(payload.value);
            },
            buffer -> new SetMagnetUpgradeTogglePayload(buffer.readUtf(), buffer.readBoolean())
    );

    @Override
    public Type<SetMagnetUpgradeTogglePayload> type() {
        return TYPE;
    }

    public static void handle(SetMagnetUpgradeTogglePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof MagnetUpgradeMenu menu)) {
                return;
            }

            switch (payload.option()) {
                case "match_nbt" -> menu.setMatchNbt(payload.value());
                case "match_damage" -> menu.setMatchDamage(payload.value());
                case "match_backpack_contents_only" -> menu.setMatchBackpackContentsOnly(payload.value());
                case "block_modded_items" -> menu.setBlockModdedItems(payload.value());
                case "only_when_full" -> menu.setOnlyWhenFull(payload.value());
                default -> {
                }
            }
        });
    }
}

