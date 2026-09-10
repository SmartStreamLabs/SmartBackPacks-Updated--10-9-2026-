package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.block.BackpackBlock;
import com.teamsmartstreamlabs.smartbackpacks.blockentity.PlacedBackpackBlockEntity;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class PlaceHeldBackpackPayload implements CustomPacketPayload {
    public static final PlaceHeldBackpackPayload INSTANCE = new PlaceHeldBackpackPayload();
    public static final Type<PlaceHeldBackpackPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "place_held_backpack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlaceHeldBackpackPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private PlaceHeldBackpackPayload() {
    }

    @Override
    public Type<PlaceHeldBackpackPayload> type() {
        return TYPE;
    }

    public static void handle(PlaceHeldBackpackPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !player.isShiftKeyDown()) {
                return;
            }

            ItemStack heldStack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!(heldStack.getItem() instanceof BackpackItem backpackItem)) {
                return;
            }

            BlockHitResult hitResult = player.level().clip(new ClipContext(
                    player.getEyePosition(),
                    player.getEyePosition().add(player.getViewVector(1.0F).scale(5.0D)),
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    player
            )) instanceof BlockHitResult blockHitResult ? blockHitResult : null;

            if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
                return;
            }

            BlockPos placePos = hitResult.getBlockPos().relative(hitResult.getDirection());
            if (!player.level().getBlockState(placePos).canBeReplaced()) {
                return;
            }

            BlockState state = getBlockForItem(backpackItem).defaultBlockState()
                    .setValue(BackpackBlock.FACING, player.getDirection().getOpposite());

            if (!player.level().setBlock(placePos, state, Block.UPDATE_ALL_IMMEDIATE)) {
                return;
            }

            if (player.level().getBlockEntity(placePos) instanceof PlacedBackpackBlockEntity blockEntity) {
                blockEntity.setStoredBackpack(heldStack.copyWithCount(1));
            }

            heldStack.shrink(1);
            player.level().playSound(null, placePos, SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.BLOCKS, 0.8F, 1.0F);
        });
    }

    private static Block getBlockForItem(BackpackItem backpackItem) {
        return switch (backpackItem.getTier()) {
            case LEATHER -> ModBlocks.LEATHER_BACKPACK.get();
            case COAL -> ModBlocks.COAL_BACKPACK.get();
            case LAPIS -> ModBlocks.LAPIS_BACKPACK.get();
            case REDSTONE -> ModBlocks.REDSTONE_BACKPACK.get();
            case QUARTZ -> ModBlocks.QUARTZ_BACKPACK.get();
            case COPPER -> ModBlocks.COPPER_BACKPACK.get();
            case IRON -> ModBlocks.IRON_BACKPACK.get();
            case GOLD -> ModBlocks.GOLD_BACKPACK.get();
            case EMERALD -> ModBlocks.EMERALD_BACKPACK.get();
            case DIAMOND -> ModBlocks.DIAMOND_BACKPACK.get();
            case NETHERITE -> ModBlocks.NETHERITE_BACKPACK.get();
            case ANCIENT_NETHERITE -> ModBlocks.ANCIENT_NETHERITE_BACKPACK.get();
            case ULTIMATE_DIAMOND -> ModBlocks.ULTIMATE_DIAMOND_BACKPACK.get();
            case NETHERITE_VAULT -> ModBlocks.NETHERITE_VAULT_BACKPACK.get();
        };
    }
}

