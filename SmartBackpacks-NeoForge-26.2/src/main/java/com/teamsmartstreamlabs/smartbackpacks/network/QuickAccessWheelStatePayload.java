package com.teamsmartstreamlabs.smartbackpacks.network;

import java.util.ArrayList;
import java.util.List;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.client.SmartBackpacksClient;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.QuickAccessWheelUpgradeData;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record QuickAccessWheelStatePayload(List<ItemStack> favorites, int availableMask) implements CustomPacketPayload {
    public static final Type<QuickAccessWheelStatePayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "quick_access_wheel_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, QuickAccessWheelStatePayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                for (int slot = 0; slot < QuickAccessWheelUpgradeData.FAVORITE_COUNT; slot++) {
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, payload.favorites.get(slot));
                }
                buffer.writeByte(payload.availableMask);
            },
            buffer -> {
                List<ItemStack> favorites = new ArrayList<>(QuickAccessWheelUpgradeData.FAVORITE_COUNT);
                for (int slot = 0; slot < QuickAccessWheelUpgradeData.FAVORITE_COUNT; slot++) {
                    favorites.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer));
                }
                return new QuickAccessWheelStatePayload(favorites, buffer.readUnsignedByte());
            });

    public QuickAccessWheelStatePayload {
        favorites = List.copyOf(favorites);
        if (favorites.size() != QuickAccessWheelUpgradeData.FAVORITE_COUNT) {
            throw new IllegalArgumentException("Quick Access Wheel requires exactly 8 favorites");
        }
    }

    @Override
    public Type<QuickAccessWheelStatePayload> type() {
        return TYPE;
    }

    public static void handle(QuickAccessWheelStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> SmartBackpacksClient.openQuickAccessWheel(payload.favorites, payload.availableMask));
    }
}
