package com.teamsmartstreamlabs.smartbackpacks.worldgen;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.block.BackpackDisplayHookBlock;
import com.teamsmartstreamlabs.smartbackpacks.blockentity.BackpackDisplayHookBlockEntity;
import com.teamsmartstreamlabs.smartbackpacks.loot.AbandonedBackpackGenerator;
import com.teamsmartstreamlabs.smartbackpacks.loot.AbandonedBackpackProfile;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public final class BackpackerCampPiece extends StructurePiece {
    private static final ResourceLocation SUPPLY_LOOT = new ResourceLocation(SmartBackpacks.MOD_ID,
            "chests/backpacker_camp");
    private final int campState;
    private final Rotation campRotation;

    public BackpackerCampPiece(int x, int y, int z, RandomSource random) {
        super(ModCampStructures.CAMP_PIECE.get(), 0, new BoundingBox(x, y - 1, z, x + 10, y + 5, z + 10));
        int abandoned = SmartBackpacksConfig.backpackerCampAbandonedWeight();
        int backpack = SmartBackpacksConfig.backpackerCampBackpackWeight();
        int trader = SmartBackpacksConfig.backpackerCampTraderWeight();
        int total = abandoned + backpack + trader;
        int roll = total <= 0 ? 0 : random.nextInt(total);
        this.campState = total <= 0 || roll < abandoned ? 0 : roll < abandoned + backpack ? 1 : 2;
        this.campRotation = Rotation.values()[random.nextInt(4)];
    }

    public BackpackerCampPiece(CompoundTag tag) {
        super(ModCampStructures.CAMP_PIECE.get(), tag);
        this.campState = tag.getInt("CampState");
        this.campRotation = Rotation.values()[Math.floorMod(tag.getInt("CampRotation"), 4)];
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("CampState", this.campState);
        tag.putInt("CampRotation", this.campRotation.ordinal());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator,
            RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos referencePos) {
        prepareGround(level, chunkBox);

        // A-frame canvas with half-step eaves, an open entrance and a low rear wall.
        for (int z = 1; z <= 4; z++) {
            place(level, chunkBox, 3, 0, z, Blocks.GRAY_WOOL.defaultBlockState());
            place(level, chunkBox, 3, 1, z, Blocks.SPRUCE_SLAB.defaultBlockState()
                    .setValue(SlabBlock.TYPE, SlabType.BOTTOM));
            place(level, chunkBox, 7, 0, z, Blocks.GRAY_WOOL.defaultBlockState());
            place(level, chunkBox, 7, 1, z, Blocks.SPRUCE_SLAB.defaultBlockState()
                    .setValue(SlabBlock.TYPE, SlabType.BOTTOM));
            place(level, chunkBox, 4, 1, z, Blocks.LIGHT_GRAY_WOOL.defaultBlockState());
            place(level, chunkBox, 4, 2, z, Blocks.SPRUCE_SLAB.defaultBlockState()
                    .setValue(SlabBlock.TYPE, SlabType.BOTTOM));
            place(level, chunkBox, 6, 1, z, Blocks.LIGHT_GRAY_WOOL.defaultBlockState());
            place(level, chunkBox, 6, 2, z, Blocks.SPRUCE_SLAB.defaultBlockState()
                    .setValue(SlabBlock.TYPE, SlabType.BOTTOM));
            place(level, chunkBox, 5, 2, z, Blocks.GRAY_WOOL.defaultBlockState());
        }
        for (int x = 4; x <= 6; x++) place(level, chunkBox, x, 0, 1,
                Blocks.GRAY_WOOL.defaultBlockState());
        place(level, chunkBox, 4, 0, 4, Blocks.SPRUCE_FENCE.defaultBlockState());
        place(level, chunkBox, 6, 0, 4, Blocks.SPRUCE_FENCE.defaultBlockState());
        place(level, chunkBox, 5, 0, 2, Blocks.RED_CARPET.defaultBlockState());
        place(level, chunkBox, 5, 0, 3, Blocks.RED_CARPET.defaultBlockState());
        place(level, chunkBox, 5, -1, 7, Blocks.COBBLESTONE.defaultBlockState());
        place(level, chunkBox, 5, 0, 7, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        place(level, chunkBox, 3, 0, 7, Blocks.SPRUCE_LOG.defaultBlockState());
        place(level, chunkBox, 7, 0, 7, Blocks.SPRUCE_LOG.defaultBlockState());

        place(level, chunkBox, 1, 0, 7, Blocks.CRAFTING_TABLE.defaultBlockState());
        place(level, chunkBox, 2, 0, 6, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        place(level, chunkBox, 2, 1, 6, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
        BlockPos chestPos = at(1, 0, 8);
        place(level, chunkBox, 1, 0, 8, Blocks.BARREL.defaultBlockState());
        if (chunkBox.isInside(chestPos))
            RandomizableContainerBlockEntity.setLootTable(level, random, chestPos, SUPPLY_LOOT);

        // The stripped-log stand is separate from the tent and faces the open fire area.
        place(level, chunkBox, 9, 0, 6, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        place(level, chunkBox, 9, 1, 6, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        place(level, chunkBox, 9, 2, 6, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        BlockPos hangerPos = at(8, 2, 6);
        place(level, chunkBox, 8, 2, 6, ModBlocks.BACKPACK_DISPLAY_HOOK.get().defaultBlockState()
                .setValue(BackpackDisplayHookBlock.FACING, Direction.WEST));
        if (this.campState == 1 && chunkBox.isInside(hangerPos)
                && level.getBlockEntity(hangerPos) instanceof BackpackDisplayHookBlockEntity hanger) {
            LootParams params = new LootParams.Builder(level.getLevel())
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(hangerPos))
                    .create(LootContextParamSets.CHEST);
            LootContext context = new LootContext.Builder(params).withOptionalRandomSeed(random.nextLong())
                    .create(new ResourceLocation(SmartBackpacks.MOD_ID, "backpacker_camp"));
            hanger.setStoredBackpack(AbandonedBackpackGenerator.generateForCamp(AbandonedBackpackProfile.VILLAGE, context));
        }

        BlockPos traderPos = at(5, 0, 9);
        if (this.campState == 2 && chunkBox.isInside(traderPos)) {
            WanderingTrader trader = EntityType.WANDERING_TRADER.create(level.getLevel());
            if (trader != null) {
                trader.moveTo(traderPos.getX() + 0.5D, traderPos.getY(), traderPos.getZ() + 0.5D, 180.0F, 0.0F);
                trader.setDespawnDelay(0);
                trader.setPersistenceRequired();
                trader.setCustomName(Component.literal("Backpacker Merchant"));
                CampTraderOffers.apply(trader, random);
                level.addFreshEntityWithPassengers(trader);
            }
        }
    }

    private BlockPos at(int x, int y, int z) {
        int localX = switch (this.campRotation) {
            case CLOCKWISE_90 -> 10 - z;
            case CLOCKWISE_180 -> 10 - x;
            case COUNTERCLOCKWISE_90 -> z;
            default -> x;
        };
        int localZ = switch (this.campRotation) {
            case CLOCKWISE_90 -> x;
            case CLOCKWISE_180 -> 10 - z;
            case COUNTERCLOCKWISE_90 -> 10 - x;
            default -> z;
        };
        return new BlockPos(this.boundingBox.minX() + localX, this.boundingBox.minY() + 1 + y,
                this.boundingBox.minZ() + localZ);
    }

    private void place(WorldGenLevel level, BoundingBox clip, int x, int y, int z, BlockState state) {
        BlockPos pos = at(x, y, z);
        if (clip.isInside(pos)) level.setBlock(pos, state.rotate(this.campRotation), 2);
    }

    private void prepareGround(WorldGenLevel level, BoundingBox clip) {
        for (int x = 1; x <= 9; x++) {
            for (int z = 1; z <= 9; z++) {
                // Leave the outer edge natural, and only grade the walkable camp interior.
                if (x == 0 || x == 10 || z == 0 || z == 10) continue;
                BlockPos top = at(x, -1, z);
                if (!clip.isInside(top)) continue;
                for (int y = -3; y < -1; y++) {
                    BlockPos support = at(x, y, z);
                    if (level.getBlockState(support).isAir()) level.setBlock(support, Blocks.DIRT.defaultBlockState(), 2);
                }
                BlockState existing = level.getBlockState(top);
                BlockState ground = existing.isAir() || !existing.getFluidState().isEmpty()
                        ? Blocks.GRASS_BLOCK.defaultBlockState() : existing;
                if (z == 7 && x >= 3 && x <= 7 || x == 5 && z >= 4 && z <= 9) {
                    ground = Blocks.DIRT_PATH.defaultBlockState();
                } else if (z <= 4 && x >= 3 && x <= 7 || x == 5 && z == 8) {
                    ground = Blocks.COARSE_DIRT.defaultBlockState();
                }
                level.setBlock(top, ground, 2);
                // Structures generate before trees; clear existing terrain and low canopy in the actual footprint.
                for (int y = 0; y <= 6; y++) {
                    BlockPos pos = at(x, y, z);
                    if (!level.getBlockState(pos).isAir()) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    void clearLateTrees(ServerLevel level) {
        for (int x = 1; x <= 9; x++) {
            for (int z = 1; z <= 9; z++) {
                for (int y = 0; y <= 10; y++) {
                    BlockPos pos = at(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.is(BlockTags.LOGS) && !state.is(BlockTags.LEAVES)) continue;
                    boolean seat = y == 0 && z == 7 && (x == 3 || x == 7)
                            && state.is(Blocks.SPRUCE_LOG);
                    boolean supplyStand = x == 2 && z == 6 && y == 0
                            && state.is(Blocks.STRIPPED_SPRUCE_LOG);
                    boolean hangerPost = x == 9 && z == 6 && y <= 2
                            && state.is(Blocks.STRIPPED_SPRUCE_LOG);
                    if (!seat && !supplyStand && !hangerPost) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }
}
