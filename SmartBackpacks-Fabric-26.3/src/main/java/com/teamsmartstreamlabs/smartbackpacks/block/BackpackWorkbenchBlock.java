package com.teamsmartstreamlabs.smartbackpacks.block;

import com.teamsmartstreamlabs.smartbackpacks.menu.BackpackWorkbenchMenu;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModBlocks;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BackpackWorkbenchBlock extends Block {
    public enum Part implements StringRepresentable {
        LEFT("left"), RIGHT("right");

        private final String name;

        Part(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
    private static final VoxelShape SHAPE = Shapes.or(
            box(0, 10, 0, 16, 13, 16),
            box(3, 2, 2, 13, 11, 14),
            box(1, 0, 1, 4, 10, 4),
            box(12, 0, 1, 15, 10, 4),
            box(1, 0, 12, 4, 10, 15),
            box(12, 0, 12, 15, 10, 15));

    public BackpackWorkbenchBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, Part.LEFT));
    }

    public static BlockPos controllerPos(BlockPos pos, BlockState state) {
        return state.getValue(PART) == Part.LEFT
                ? pos
                : pos.relative(state.getValue(FACING).getCounterClockWise());
    }

    private static BlockPos otherPos(BlockPos pos, BlockState state) {
        return pos.relative(state.getValue(PART) == Part.LEFT
                ? state.getValue(FACING).getClockWise()
                : state.getValue(FACING).getCounterClockWise());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos other = context.getClickedPos().relative(facing.getClockWise());
        if (!context.getLevel().getWorldBorder().isWithinBounds(other)
                || !context.getLevel().getBlockState(other).canBeReplaced(context)) {
            return null;
        }
        return this.defaultBlockState().setValue(FACING, facing).setValue(PART, Part.LEFT);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        level.setBlock(otherPos(pos, state), state.setValue(PART, Part.RIGHT), 3);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        BlockPos other = otherPos(pos, state);
        BlockState otherState = level.getBlockState(other);
        if (otherState.is(this) && otherState.getValue(FACING) == state.getValue(FACING)
                && otherState.getValue(PART) != state.getValue(PART)) {
            level.setBlock(other, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }

    private InteractionResult open(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            BlockPos controller = controllerPos(pos, state);
            BlockState controllerState = level.getBlockState(controller);
            if (!controllerState.is(ModBlocks.BACKPACK_WORKBENCH.get())
                    || controllerState.getValue(PART) != Part.LEFT) {
                return InteractionResult.FAIL;
            }
            serverPlayer.openMenu(new ExtendedMenuProvider<byte[]>() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("block.smartbackpacks.backpack_workbench");
                }

                @Override
                public BackpackWorkbenchMenu createMenu(int id, net.minecraft.world.entity.player.Inventory inventory, Player menuPlayer) {
                    return new BackpackWorkbenchMenu(id, inventory, controller);
                }

                @Override
                public byte[] getScreenOpeningData(ServerPlayer player) {
                    RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
                    BackpackWorkbenchMenu.writeContext(buffer, player, controller);
                    byte[] data = new byte[buffer.readableBytes()];
                    buffer.getBytes(0, data);
                    return data;
                }
            });
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        return this.open(level, pos, state, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        return this.open(level, pos, state, player);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }
}
