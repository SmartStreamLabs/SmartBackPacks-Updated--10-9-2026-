package com.teamsmartstreamlabs.smartbackpacks.block;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackHelper;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.blockentity.PlacedBackpackBlockEntity;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BackpackBlock extends BaseEntityBlock implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            Block.box(3.5D, 0.0D, 5.0D, 12.5D, 12.0D, 11.0D),
            Block.box(4.25D, 0.25D, 3.75D, 11.75D, 5.25D, 5.25D),
            Block.box(2.0D, 0.75D, 5.75D, 3.5D, 6.75D, 10.25D),
            Block.box(12.5D, 0.75D, 5.75D, 14.0D, 6.75D, 10.25D)
    );
    private static final VoxelShape SOUTH_SHAPE = Shapes.or(
            Block.box(3.5D, 0.0D, 5.0D, 12.5D, 12.0D, 11.0D),
            Block.box(4.25D, 0.25D, 10.75D, 11.75D, 5.25D, 12.25D),
            Block.box(2.0D, 0.75D, 5.75D, 3.5D, 6.75D, 10.25D),
            Block.box(12.5D, 0.75D, 5.75D, 14.0D, 6.75D, 10.25D)
    );
    private static final VoxelShape EAST_SHAPE = Shapes.or(
            Block.box(5.0D, 0.0D, 3.5D, 11.0D, 12.0D, 12.5D),
            Block.box(10.75D, 0.25D, 4.25D, 12.25D, 5.25D, 11.75D),
            Block.box(5.75D, 0.75D, 2.0D, 10.25D, 6.75D, 3.5D),
            Block.box(5.75D, 0.75D, 12.5D, 10.25D, 6.75D, 14.0D)
    );
    private static final VoxelShape WEST_SHAPE = Shapes.or(
            Block.box(5.0D, 0.0D, 3.5D, 11.0D, 12.0D, 12.5D),
            Block.box(3.75D, 0.25D, 4.25D, 5.25D, 5.25D, 11.75D),
            Block.box(5.75D, 0.75D, 2.0D, 10.25D, 6.75D, 3.5D),
            Block.box(5.75D, 0.75D, 12.5D, 10.25D, 6.75D, 14.0D)
    );

    private final BackpackTier tier;

    public BackpackBlock(BackpackTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public BackpackTier getTier() {
        return this.tier;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(properties -> new BackpackBlock(this.tier, properties));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.getDirectionalShape(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.getDirectionalShape(state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(heldStack.getItem() instanceof DyeItem)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        DyeColor dyeColor = heldStack.get(DataComponents.DYE);
        if (dyeColor == null) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof PlacedBackpackBlockEntity blockEntity)) {
            return InteractionResult.PASS;
        }

        ItemStack backpack = blockEntity.getStoredBackpack();
        int rgb = dyeColor.getTextureDiffuseColor();
        DyedItemColor currentColor = backpack.get(DataComponents.DYED_COLOR);
        if (currentColor == null || currentColor.rgb() != rgb) {
            backpack.set(DataComponents.DYED_COLOR, new DyedItemColor(rgb));
            blockEntity.setStoredBackpack(backpack);
            if (!player.hasInfiniteMaterials()) {
                heldStack.shrink(1);
            }
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.grant(serverPlayer, "make_it_yours");
            }
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof PlacedBackpackBlockEntity blockEntity) {
            ItemStack stack = blockEntity.getStoredBackpack();
            if (stack.getItem() instanceof com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem backpackItem) {
                BackpackHelper.openBackpack(serverPlayer, BackpackAccess.block(pos, backpackItem.getTier()));
                level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.BLOCKS, 0.7F, 1.0F);
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PlacedBackpackBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide()
                ? createTickerHelper(blockEntityType, com.teamsmartstreamlabs.smartbackpacks.registry.ModBlockEntities.PLACED_BACKPACK.get(),
                PlacedBackpackBlockEntity::clientTick)
                : createTickerHelper(blockEntityType, com.teamsmartstreamlabs.smartbackpacks.registry.ModBlockEntities.PLACED_BACKPACK.get(),
                PlacedBackpackBlockEntity::serverTick);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (!level.isClientSide() && blockEntity instanceof PlacedBackpackBlockEntity backpackBlockEntity) {
            backpackBlockEntity.releaseChunkForce();
            ItemStack backpack = backpackBlockEntity.extractStoredBackpack();
            giveBackpackToPlayerOrDrop(player, backpack);
        }

        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof PlacedBackpackBlockEntity backpackBlockEntity) {
            backpackBlockEntity.releaseChunkForce();
            ItemStack backpack = backpackBlockEntity.extractStoredBackpack();
            giveBackpackToPlayerOrDrop(player, backpack);
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    private static void giveBackpackToPlayerOrDrop(Player player, ItemStack backpack) {
        if (backpack.isEmpty()) {
            return;
        }

        if (!player.getInventory().add(backpack)) {
            player.drop(backpack, false);
        }
    }

    private VoxelShape getDirectionalShape(BlockState state) {
        return switch (state.getValue(FACING)) {
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }
}
