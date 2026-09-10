package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.block.BackpackBlock;
import com.teamsmartstreamlabs.smartbackpacks.blockentity.PlacedBackpackBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class PickupPlacedBackpackPayload implements CustomPacketPayload {
    public static final PickupPlacedBackpackPayload INSTANCE = new PickupPlacedBackpackPayload();
    public static final Type<PickupPlacedBackpackPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "pickup_placed_backpack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PickupPlacedBackpackPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private PickupPlacedBackpackPayload() {
    }

    @Override
    public Type<PickupPlacedBackpackPayload> type() {
        return TYPE;
    }

    public static void handle(PickupPlacedBackpackPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || player.isCreative() || !player.isShiftKeyDown()) {
                return;
            }

            BlockHitResult hitResult = player.level().clip(new ClipContext(
                    player.getEyePosition(),
                    player.getEyePosition().add(player.getViewVector(1.0F).scale(5.0D)),
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    player
            )) instanceof BlockHitResult blockHitResult ? blockHitResult : null;

            if (hitResult == null) {
                return;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = player.level().getBlockState(pos);
            if (!(state.getBlock() instanceof BackpackBlock) || !(player.level().getBlockEntity(pos) instanceof PlacedBackpackBlockEntity blockEntity)) {
                return;
            }

            ItemStack backpack = blockEntity.extractStoredBackpack();
            if (backpack.isEmpty()) {
                return;
            }

            player.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            if (!player.getInventory().add(backpack)) {
                player.drop(backpack, false);
            }
            player.level().playSound(null, pos, SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.BLOCKS, 0.8F, 0.95F);
        });
    }
}

