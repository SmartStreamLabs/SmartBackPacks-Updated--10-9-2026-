package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.block.BackpackBlock;
import com.teamsmartstreamlabs.smartbackpacks.blockentity.PlacedBackpackBlockEntity;

import net.minecraft.core.BlockPos;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.codec.StreamCodec;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PickupPlacedBackpackPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<PickupPlacedBackpackPayload> TYPE =
            new Type<>(new ResourceLocation(SmartBackpacks.MOD_ID, "pickup_placed_backpack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PickupPlacedBackpackPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeBlockPos(payload.pos),
            buffer -> new PickupPlacedBackpackPayload(buffer.readBlockPos())
    );

    @Override
    public Type<PickupPlacedBackpackPayload> type() {
        return TYPE;
    }

    public static void handle(PickupPlacedBackpackPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.player();
            if (player == null || player.isCreative() || !player.isShiftKeyDown()) {
                return;
            }

            BlockState state = player.level().getBlockState(payload.pos());
            if (!(state.getBlock() instanceof BackpackBlock) || !(player.level().getBlockEntity(payload.pos()) instanceof PlacedBackpackBlockEntity blockEntity)) {
                return;
            }

            ItemStack backpack = blockEntity.extractStoredBackpack();
            if (backpack.isEmpty()) {
                return;
            }

            player.level().setBlock(payload.pos(), Blocks.AIR.defaultBlockState(), 3);
            if (!player.getInventory().add(backpack)) {
                player.drop(backpack, false);
            }
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
            player.level().playSound(null, payload.pos(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.BLOCKS, 0.8F, 0.95F);
        });
    }
}
