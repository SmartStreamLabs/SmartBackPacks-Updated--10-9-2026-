package com.teamsmartstreamlabs.smartbackpacks.network;

import java.util.ArrayList;
import java.util.List;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackHelper;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.QuickAccessWheelHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.QuickAccessWheelUpgradeData;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class QuickAccessWheelRequestPayload implements CustomPacketPayload {
    public static final QuickAccessWheelRequestPayload INSTANCE = new QuickAccessWheelRequestPayload();
    public static final Type<QuickAccessWheelRequestPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "quick_access_wheel_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, QuickAccessWheelRequestPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    private QuickAccessWheelRequestPayload() {
    }

    @Override
    public Type<QuickAccessWheelRequestPayload> type() {
        return TYPE;
    }

    public static void handle(QuickAccessWheelRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            BackpackAccess access = BackpackHelper.findWornBackpackAccess(player);
            if (access == null) {
                return;
            }
            ItemStack backpack = access.getBackpackStack(player);
            if (!(backpack.getItem() instanceof BackpackItem backpackItem)) {
                return;
            }
            QuickAccessWheelUpgradeData data = QuickAccessWheelHandler.findData(backpack);
            if (data == null) {
                return;
            }

            List<ItemStack> favorites = new ArrayList<>(QuickAccessWheelUpgradeData.FAVORITE_COUNT);
            int availableMask = 0;
            var configured = data.loadFavorites();
            for (int slot = 0; slot < QuickAccessWheelUpgradeData.FAVORITE_COUNT; slot++) {
                ItemStack favorite = configured.get(slot).copy();
                favorites.add(favorite);
                if (!favorite.isEmpty() && QuickAccessWheelHandler.containsMatchingItem(backpack, backpackItem, favorite)) {
                    availableMask |= 1 << slot;
                }
            }
            PacketDistributor.sendToPlayer(player, new QuickAccessWheelStatePayload(favorites, availableMask));
        });
    }
}
