package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.StorageControllerMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StorageControllerActionPayload(int containerId, Action action, ItemStack stack, int amount)
        implements CustomPacketPayload {
    public enum Action {
        REFRESH,
        EXTRACT
    }

    public static final Type<StorageControllerActionPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "storage_controller_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StorageControllerActionPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeVarInt(payload.containerId());
                buffer.writeEnum(payload.action());
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, payload.stack());
                buffer.writeVarInt(payload.amount());
            },
            buffer -> new StorageControllerActionPayload(
                    buffer.readVarInt(),
                    buffer.readEnum(Action.class),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer),
                    buffer.readVarInt()));

    public static StorageControllerActionPayload refresh(int containerId) {
        return new StorageControllerActionPayload(containerId, Action.REFRESH, ItemStack.EMPTY, 0);
    }

    public static StorageControllerActionPayload extract(int containerId, ItemStack stack, int amount) {
        return new StorageControllerActionPayload(containerId, Action.EXTRACT, stack.copyWithCount(1), amount);
    }

    @Override
    public Type<StorageControllerActionPayload> type() {
        return TYPE;
    }

    public static void handle(StorageControllerActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !(player.containerMenu instanceof StorageControllerMenu menu)
                    || menu.containerId != payload.containerId()) {
                return;
            }
            if (payload.action() == Action.REFRESH) {
                menu.refreshAndSync(player);
            } else if (payload.action() == Action.EXTRACT) {
                menu.extract(player, payload.stack(), Math.max(1, payload.amount()));
            }
        });
    }
}
