package com.teamsmartstreamlabs.smartbackpacks.block;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackHelper;
import com.teamsmartstreamlabs.smartbackpacks.blockentity.BackpackDisplayHookBlockEntity;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

public final class BackpackDisplayHookBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape[] SHAPES = {
            box(6.2, 5, 5.35, 9.8, 10.8, 16), box(6.2, 5, 0, 9.8, 10.8, 10.65),
            box(0, 5, 6.2, 10.65, 10.8, 9.8), box(5.35, 5, 6.2, 16, 10.8, 9.8)
    };
    private static final VoxelShape[] FILLED_SHAPES = {
            Shapes.or(SHAPES[0], box(3, 1, 5, 13, 11, 16)),
            Shapes.or(SHAPES[1], box(3, 1, 0, 13, 11, 11)),
            Shapes.or(SHAPES[2], box(0, 1, 3, 11, 11, 13)),
            Shapes.or(SHAPES[3], box(5, 1, 3, 16, 11, 13))
    };

    public BackpackDisplayHookBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(BackpackDisplayHookBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        if (!facing.getAxis().isHorizontal()) return null;
        BlockState state = this.defaultBlockState().setValue(FACING, facing);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        return Block.canSupportCenter(level, pos.relative(facing.getOpposite()), facing);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor,
                                BlockPos neighborPos, boolean moved) {
        if (!level.isClientSide() && !state.canSurvive(level, pos)) level.destroyBlock(pos, true);
        super.neighborChanged(state, level, pos, neighbor, neighborPos, moved);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape[] shapes = level.getBlockEntity(pos) instanceof BackpackDisplayHookBlockEntity hook
                && !hook.getStoredBackpack().isEmpty() ? FILLED_SHAPES : SHAPES;
        return switch (state.getValue(FACING)) {
            case SOUTH -> shapes[1];
            case EAST -> shapes[2];
            case WEST -> shapes[3];
            default -> shapes[0];
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPES[1];
            case EAST -> SHAPES[2];
            case WEST -> SHAPES[3];
            default -> SHAPES[0];
        };
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity().isShiftKeyDown()
                && event.getLevel().getBlockEntity(event.getPos()) instanceof BackpackDisplayHookBlockEntity hook
                && !hook.getStoredBackpack().isEmpty()) {
            event.setUseBlock(TriState.TRUE);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BackpackDisplayHookBlockEntity hook)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!hook.getStoredBackpack().isEmpty()) {
            return this.interact(level, pos, player, hook) == InteractionResult.PASS
                    ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION : ItemInteractionResult.CONSUME;
        }
        if (!(held.getItem() instanceof BackpackItem)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide()) return ItemInteractionResult.SUCCESS;
        hook.setStoredBackpack(held.copyWithCount(1));
        held.shrink(1);
        if (player instanceof ServerPlayer serverPlayer) {
            com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.grant(serverPlayer, "hang_it_up");
        }
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        return level.getBlockEntity(pos) instanceof BackpackDisplayHookBlockEntity hook
                ? this.interact(level, pos, player, hook) : InteractionResult.PASS;
    }

    private InteractionResult interact(Level level, BlockPos pos, Player player, BackpackDisplayHookBlockEntity hook) {
        ItemStack stored = hook.getStoredBackpack();
        if (!(stored.getItem() instanceof BackpackItem backpackItem)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (player.isShiftKeyDown()) {
            ItemStack removed = hook.takeBackpack();
            if (!player.getInventory().add(removed)) {
                hook.setStoredBackpack(removed);
                player.displayClientMessage(Component.literal("Inventory full."), true);
            }
            return InteractionResult.CONSUME;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            BackpackHelper.openBackpack(serverPlayer, BackpackAccess.displayHook(pos, backpackItem.getTier()));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BackpackDisplayHookBlockEntity(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()
                && level.getBlockEntity(pos) instanceof BackpackDisplayHookBlockEntity hook) {
            ItemStack stored = hook.takeBackpack();
            if (!stored.isEmpty()) net.minecraft.world.Containers.dropItemStack(
                    level, pos.getX(), pos.getY(), pos.getZ(), stored);
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
