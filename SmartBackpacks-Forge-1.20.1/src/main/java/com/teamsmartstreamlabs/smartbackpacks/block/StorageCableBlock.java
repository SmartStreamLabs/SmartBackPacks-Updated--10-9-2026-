package com.teamsmartstreamlabs.smartbackpacks.block;

import java.util.List;

import com.teamsmartstreamlabs.smartbackpacks.client.StorageGuidePrompt;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StorageCableBlock extends Block {
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty BACKPACK_NORTH = BooleanProperty.create("backpack_north");
    public static final BooleanProperty BACKPACK_SOUTH = BooleanProperty.create("backpack_south");
    public static final BooleanProperty BACKPACK_EAST = BooleanProperty.create("backpack_east");
    public static final BooleanProperty BACKPACK_WEST = BooleanProperty.create("backpack_west");
    public static final BooleanProperty BACKPACK_UP = BooleanProperty.create("backpack_up");
    public static final BooleanProperty BACKPACK_DOWN = BooleanProperty.create("backpack_down");
    private static final VoxelShape CORE = Block.box(6.0D, 6.0D, 6.0D, 10.0D, 10.0D, 10.0D);
    private static final VoxelShape NORTH_ARM = Block.box(6.0D, 6.0D, 0.0D, 10.0D, 10.0D, 6.0D);
    private static final VoxelShape SOUTH_ARM = Block.box(6.0D, 6.0D, 10.0D, 10.0D, 10.0D, 16.0D);
    private static final VoxelShape EAST_ARM = Block.box(10.0D, 6.0D, 6.0D, 16.0D, 10.0D, 10.0D);
    private static final VoxelShape WEST_ARM = Block.box(0.0D, 6.0D, 6.0D, 6.0D, 10.0D, 10.0D);
    private static final VoxelShape UP_ARM = Block.box(6.0D, 10.0D, 6.0D, 10.0D, 16.0D, 10.0D);
    private static final VoxelShape DOWN_ARM = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 6.0D, 10.0D);

    public StorageCableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false)
                .setValue(BACKPACK_NORTH, false)
                .setValue(BACKPACK_SOUTH, false)
                .setValue(BACKPACK_EAST, false)
                .setValue(BACKPACK_WEST, false)
                .setValue(BACKPACK_UP, false)
                .setValue(BACKPACK_DOWN, false));
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        StorageGuidePrompt.append(tooltip);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN,
                BACKPACK_NORTH, BACKPACK_SOUTH, BACKPACK_EAST,
                BACKPACK_WEST, BACKPACK_UP, BACKPACK_DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = context.getClickedPos().relative(direction);
            state = updateConnection(state, direction, context.getLevel().getBlockState(neighborPos));
        }
        return state;
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos) {
        return updateConnection(state, direction, neighborState);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    private static VoxelShape shapeFor(BlockState state) {
        VoxelShape shape = CORE;
        if (state.getValue(NORTH)) shape = Shapes.or(shape, NORTH_ARM);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, SOUTH_ARM);
        if (state.getValue(EAST)) shape = Shapes.or(shape, EAST_ARM);
        if (state.getValue(WEST)) shape = Shapes.or(shape, WEST_ARM);
        if (state.getValue(UP)) shape = Shapes.or(shape, UP_ARM);
        if (state.getValue(DOWN)) shape = Shapes.or(shape, DOWN_ARM);
        return shape;
    }

    private static boolean connectsTo(BlockState state, Direction direction) {
        Block block = state.getBlock();
        if (block instanceof StorageTransferBlock) {
            return state.getValue(StorageTransferBlock.FACING) != direction.getOpposite();
        }
        return block instanceof StorageCableBlock
                || block instanceof StorageControllerBlock
                || block instanceof BackpackBlock;
    }

    private static BlockState updateConnection(BlockState state, Direction direction, BlockState neighborState) {
        return state.setValue(property(direction), connectsTo(neighborState, direction))
                .setValue(backpackProperty(direction), neighborState.getBlock() instanceof BackpackBlock);
    }

    private static BooleanProperty property(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    private static BooleanProperty backpackProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> BACKPACK_NORTH;
            case SOUTH -> BACKPACK_SOUTH;
            case EAST -> BACKPACK_EAST;
            case WEST -> BACKPACK_WEST;
            case UP -> BACKPACK_UP;
            case DOWN -> BACKPACK_DOWN;
        };
    }
}
