package com.teamsmartstreamlabs.smartbackpacks.network;

import java.util.ArrayList;
import java.util.List;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.codec.StreamCodec;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.teamsmartstreamlabs.smartbackpacks.menu.BackpackMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BackpackVisibleSlotsSyncPayload(int containerId, int firstVisibleRow, List<ItemStack> stacks)
        implements CustomPacketPayload {
    private static final int MAX_SYNCED_SLOTS = 54;
    public static final Type<BackpackVisibleSlotsSyncPayload> TYPE =
            new Type<>(new ResourceLocation(SmartBackpacks.MOD_ID, "backpack_visible_slots_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BackpackVisibleSlotsSyncPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeVarInt(payload.containerId);
                buffer.writeVarInt(payload.firstVisibleRow);
                buffer.writeVarInt(payload.stacks.size());
                for (ItemStack stack : payload.stacks) {
                    int fullCount = stack.isEmpty() ? 0 : stack.getCount();
                    buffer.writeItem(copyForNetwork(stack));
                    buffer.writeVarInt(fullCount);
                }
            },
            buffer -> {
                int containerId = buffer.readVarInt();
                int firstVisibleRow = buffer.readVarInt();
                int count = buffer.readVarInt();
                if (count < 0 || count > MAX_SYNCED_SLOTS) {
                    throw new IllegalArgumentException("Invalid visible backpack slot count: " + count);
                }

                List<ItemStack> stacks = new ArrayList<>(count);
                for (int slot = 0; slot < count; slot++) {
                    ItemStack stack = buffer.readItem();
                    int fullCount = buffer.readVarInt();
                    if (!stack.isEmpty() && fullCount > 0) {
                        stack.setCount(fullCount);
                    }
                    stacks.add(stack);
                }
                return new BackpackVisibleSlotsSyncPayload(containerId, firstVisibleRow, stacks);
            });

    public BackpackVisibleSlotsSyncPayload {
        stacks = List.copyOf(stacks);
    }

    @Override
    public Type<BackpackVisibleSlotsSyncPayload> type() {
        return TYPE;
    }

    private static ItemStack copyForNetwork(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return stack.copyWithCount(Math.min(stack.getCount(), Math.max(1, stack.getMaxStackSize())));
    }

    public static void handle(BackpackVisibleSlotsSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null
                    && minecraft.player.containerMenu instanceof BackpackMenu menu
                    && menu.containerId == payload.containerId()) {
                menu.applyVisibleSlotSync(payload.firstVisibleRow(), payload.stacks());
            }
        });
    }
}
