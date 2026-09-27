package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackHelper;
import com.teamsmartstreamlabs.smartbackpacks.item.StorageMonitorItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.storage.StorageMonitorAccess;
import com.teamsmartstreamlabs.smartbackpacks.storage.StorageMonitorLink;
import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;

import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.codec.StreamCodec;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class OpenStorageMonitorPayload implements CustomPacketPayload {
    public static final OpenStorageMonitorPayload INSTANCE = new OpenStorageMonitorPayload();
    public static final Type<OpenStorageMonitorPayload> TYPE = new Type<>(new ResourceLocation(SmartBackpacks.MOD_ID, "open_storage_monitor"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenStorageMonitorPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private OpenStorageMonitorPayload() {
    }

    @Override
    public Type<OpenStorageMonitorPayload> type() {
        return TYPE;
    }

    public static void handle(OpenStorageMonitorPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.player();
            if (player != null) {
                ItemStack monitor = findMonitor(player);
                if (monitor.isEmpty()) {
                    return;
                }
                StorageMonitorLink link = ItemStackCompat.get(monitor, ModDataComponents.STORAGE_MONITOR_LINK.get());
                StorageMonitorAccess.Result access = StorageMonitorAccess.resolve(player, link);
                if (access.available()) {
                    BackpackHelper.openStorageMonitor(player, link);
                } else {
                    player.displayClientMessage(Component.translatable(statusKey(access.status())), true);
                }
            }
        });
    }

    private static ItemStack findMonitor(ServerPlayer player) {
        if (player.getMainHandItem().getItem() instanceof StorageMonitorItem) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().getItem() instanceof StorageMonitorItem) {
            return player.getOffhandItem();
        }
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof StorageMonitorItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static String statusKey(StorageMonitorAccess.Status status) {
        return "message.smartbackpacks.storage_monitor." + switch (status) {
            case DISABLED -> "disabled";
            case NOT_LINKED -> "not_linked";
            case CROSS_DIMENSION_DISABLED -> "cross_dimension_disabled";
            case TARGET_UNLOADED, NETWORK_OFFLINE -> "offline";
            case CONTROLLER_MISSING -> "controller_missing";
            case AVAILABLE -> "offline";
        };
    }
}
