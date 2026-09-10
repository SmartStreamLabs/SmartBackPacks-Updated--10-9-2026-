package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.TorchPlacerUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class TorchPlacerUpgradeHandler {
    public static final TagKey<Item> TORCH_PLACER_ITEMS = itemTag("torch_placer_items");
    public static final TagKey<Item> TORCH_PLACER_WALL_ITEMS = itemTag("torch_placer_wall_items");
    public static final TagKey<Item> TORCH_PLACER_FLOOR_ITEMS = itemTag("torch_placer_floor_items");
    public static final TagKey<Item> TORCH_PLACER_BLACKLIST = itemTag("torch_placer_blacklist");
    public static final TagKey<Item> TORCH_PLACER_PRIORITY = itemTag("torch_placer_priority");
    public static final TagKey<Block> TORCH_PLACER_AVOID_SUPPORTS = blockTag("torch_placer_avoid_supports");
    public static final TagKey<Block> TORCH_PLACER_NEVER_REPLACE = blockTag("torch_placer_never_replace");
    public static final TagKey<Block> TORCH_PLACER_NO_WALL_ATTACH = blockTag("torch_placer_no_wall_attach");

    private static final int MOVEMENT_EPSILON_SQR = 1;
    private static final int FAILURE_MESSAGE_DELAY_TICKS = 80;
    private static final Map<UUID, PlayerMemory> MEMORIES = new HashMap<>();

    private TorchPlacerUpgradeHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }

        PlayerMemory memory = MEMORIES.computeIfAbsent(player.getUUID(), ignored -> new PlayerMemory(player.blockPosition()));
        if (player.isSpectator() || hasContainerOpen(player)) {
            memory.lastPosition = player.blockPosition();
            return;
        }

        long gameTime = player.level().getGameTime();
        if (!hasDelayPassed(gameTime, memory.lastCheckTick, SmartBackpacksConfig.torchPlacerCheckIntervalTicks())) {
            return;
        }

        memory.lastCheckTick = gameTime;
        if (!canRunForGameMode(player)) {
            memory.lastPosition = player.blockPosition();
            return;
        }

        TorchBackpack source = findTorchBackpack(player);
        if (source == null || !SmartBackpacksConfig.torchPlacerUpgradeEnabled() || !source.data().enabled()) {
            memory.lastPosition = player.blockPosition();
            return;
        }

        TorchPlacerUpgradeData data = source.data();
        if (!canPlaceForMovement(player, data, memory)) {
            memory.lastPosition = player.blockPosition();
            return;
        }

        if (isPlayerInBlockedFluid(player, data)) {
            memory.lastPosition = player.blockPosition();
            return;
        }

        ServerLevel level = player.serverLevel();
        BlockPos playerPos = player.blockPosition();
        int blockLight = level.getBrightness(LightLayer.BLOCK, playerPos);
        int skyLight = Math.max(0, level.getBrightness(LightLayer.SKY, playerPos) - level.getSkyDarken());
        if (Math.max(blockLight, skyLight) > data.lightThreshold()) {
            memory.lastPosition = playerPos;
            return;
        }

        if (memory.lastPlacementPos != null && memory.lastPlacementPos.distSqr(playerPos) < (double) data.minimumDistance() * data.minimumDistance()) {
            memory.lastPosition = playerPos;
            return;
        }

        if (!hasDelayPassed(gameTime, memory.lastPlacementTick, SmartBackpacksConfig.torchPlacerPlacementCooldownTicks())
                || !hasDelayPassed(gameTime, memory.lastFailureTick, SmartBackpacksConfig.torchPlacerFailureCooldownTicks())) {
            memory.lastPosition = playerPos;
            return;
        }

        PlacementResult result = tryPlaceTorch(player, source);
        memory.lastPosition = playerPos;
        if (result == PlacementResult.SUCCESS) {
            memory.lastPlacementTick = gameTime;
            memory.lastPlacementPos = playerPos.immutable();
            return;
        }

        sendFailureFeedback(player, memory, result);
        memory.lastFailureTick = gameTime;
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        MEMORIES.remove(event.getEntity().getUUID());
    }

    private static PlacementResult tryPlaceTorch(ServerPlayer player, TorchBackpack source) {
        NonNullList<ItemStack> storage = BackpackStackData.loadStorage(source.backpack(), source.tier());
        List<SourceSlot> sources = findTorchSources(source.backpack(), storage, source.data());
        if (sources.isEmpty()) {
            return PlacementResult.NO_TORCH;
        }

        List<PlacementCandidate> candidates = findPlacementCandidates(player, source.data());
        if (candidates.isEmpty()) {
            return PlacementResult.NO_POSITION;
        }

        for (PlacementCandidate candidate : candidates) {
            for (SourceSlot sourceSlot : sources) {
                if (!candidate.accepts(sourceSlot.stack(), source.data())) {
                    continue;
                }

                if (tryPlaceFromSlot(player, source, storage, sourceSlot.slot(), candidate)) {
                    if (SmartBackpacksConfig.torchPlacerFeedbackEnabled()) {
                        player.displayClientMessage(Component.translatable("message.smartbackpacks.torch_placer_placed", sourceSlot.stack().getHoverName()), true);
                    }
                    return PlacementResult.SUCCESS;
                }
            }
        }

        return PlacementResult.NO_POSITION;
    }

    private static boolean tryPlaceFromSlot(ServerPlayer player, TorchBackpack source, NonNullList<ItemStack> storage, int slot, PlacementCandidate candidate) {
        ServerLevel level = player.serverLevel();
        ItemStack stored = storage.get(slot);
        if (stored.isEmpty()) {
            return false;
        }
        if (!ItemLockProtection.canConsumeForUpgrade(source.backpack(), slot, stored)) {
            return false;
        }

        ItemStack placeStack = stored.copyWithCount(1);
        if (!level.isLoaded(candidate.target()) || !level.isLoaded(candidate.support())) {
            return false;
        }
        if (!level.mayInteract(player, candidate.support()) || !player.mayUseItemAt(candidate.support(), candidate.face(), placeStack)) {
            return false;
        }

        BlockState beforeTarget = level.getBlockState(candidate.target());
        if (!canReplaceTarget(beforeTarget)) {
            return false;
        }

        boolean placed = tryPlaceWithMinecraftContext(player, placeStack, candidate, beforeTarget);
        if (!placed) {
            placed = tryPlaceVanillaTorchFallback(player, placeStack, candidate);
        }
        if (!placed) {
            return false;
        }

        int consumed = 1 - placeStack.getCount();
        if (consumed <= 0) {
            consumed = 1;
        }

        stored.shrink(consumed);
        if (stored.isEmpty()) {
            storage.set(slot, ItemStack.EMPTY);
        }
        BackpackStackData.saveStorage(source.backpack(), storage);
        source.saver().accept(source.backpack());
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        player.containerMenu.broadcastChanges();
        return true;
    }

    private static boolean tryPlaceWithMinecraftContext(ServerPlayer player, ItemStack placeStack, PlacementCandidate candidate, BlockState beforeTarget) {
        if (!(placeStack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }

        ServerLevel level = player.serverLevel();
        BlockHitResult hit = new BlockHitResult(candidate.hit(), candidate.face(), candidate.support(), false);
        InteractionResult result = blockItem.place(new BlockPlaceContext(new UseOnContext(level, player, InteractionHand.MAIN_HAND, placeStack, hit)));
        BlockState afterTarget = level.getBlockState(candidate.target());
        return result.consumesAction() && !afterTarget.isAir() && !afterTarget.equals(beforeTarget);
    }

    private static boolean tryPlaceVanillaTorchFallback(ServerPlayer player, ItemStack placeStack, PlacementCandidate candidate) {
        BlockState state = vanillaTorchState(placeStack, candidate);
        if (state == null) {
            return false;
        }

        ServerLevel level = player.serverLevel();
        if (!state.canSurvive(level, candidate.target())) {
            return false;
        }

        level.setBlock(candidate.target(), state, Block.UPDATE_ALL);
        level.gameEvent(player, GameEvent.BLOCK_PLACE, candidate.target());
        level.playSound(null, candidate.target(), SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
        return true;
    }

    private static BlockState vanillaTorchState(ItemStack placeStack, PlacementCandidate candidate) {
        if (candidate.type() == CandidateType.CEILING) {
            return null;
        }
        if (placeStack.is(Items.TORCH)) {
            return candidate.type() == CandidateType.WALL
                    ? Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, candidate.face())
                    : Blocks.TORCH.defaultBlockState();
        }
        if (placeStack.is(Items.SOUL_TORCH)) {
            return candidate.type() == CandidateType.WALL
                    ? Blocks.SOUL_WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, candidate.face())
                    : Blocks.SOUL_TORCH.defaultBlockState();
        }
        return null;
    }

    private static List<SourceSlot> findTorchSources(ItemStack backpack, NonNullList<ItemStack> storage, TorchPlacerUpgradeData data) {
        List<SourceSlot> sources = new ArrayList<>();
        for (int slot = 0; slot < storage.size(); slot++) {
            ItemStack stack = storage.get(slot);
            if (!stack.isEmpty()
                    && isSupportedTorchItem(stack, data)
                    && filterAllows(data, stack)
                    && ItemLockProtection.canConsumeForUpgrade(backpack, slot, stack)) {
                sources.add(new SourceSlot(slot, stack));
            }
        }

        sources.sort(sourceComparator());
        return sources;
    }

    private static Comparator<SourceSlot> sourceComparator() {
        return Comparator.comparingInt((SourceSlot source) -> cheapestScore(source.stack())).thenComparingInt(SourceSlot::slot);
    }

    private static int cheapestScore(ItemStack stack) {
        if (stack.is(Items.TORCH)) {
            return 0;
        }
        if (stack.is(Items.SOUL_TORCH)) {
            return 1;
        }
        if (stack.is(TORCH_PLACER_PRIORITY)) {
            return 2;
        }
        return 3;
    }

    private static List<PlacementCandidate> findPlacementCandidates(ServerPlayer player, TorchPlacerUpgradeData data) {
        List<BlockPos> targets = orderedTargets(player, data.placementMode());
        List<PlacementCandidate> candidates = new ArrayList<>();
        for (BlockPos baseTarget : targets) {
            for (BlockPos target : targetHeightVariants(baseTarget)) {
                if (data.placementMode() == TorchPlacerMode.WALL_PREFERRED && data.wallPlacement()) {
                    addWallCandidates(player, target, candidates, true);
                }

                if (data.floorPlacement() && data.placementMode() != TorchPlacerMode.WALL_PREFERRED) {
                    addFloorCandidate(player, target, candidates);
                } else if (data.floorPlacement() && candidates.isEmpty()) {
                    addFloorCandidate(player, target, candidates);
                }

                if (data.wallPlacement() && data.placementMode() != TorchPlacerMode.FLOOR_ONLY) {
                    addWallCandidates(player, target, candidates, false);
                }

                if (data.ceilingPlacement()) {
                    addCeilingCandidate(player, target, candidates);
                }
            }
        }
        return candidates;
    }

    private static List<BlockPos> orderedTargets(ServerPlayer player, TorchPlacerMode mode) {
        BlockPos origin = player.blockPosition();
        Direction facing = player.getDirection();
        Direction behind = facing.getOpposite();
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();
        List<BlockPos> targets = new ArrayList<>();

        if (mode == TorchPlacerMode.NEARBY) {
            addUniqueTarget(targets, origin.relative(behind));
            addUniqueTarget(targets, origin.relative(left));
            addUniqueTarget(targets, origin.relative(right));
            addUniqueTarget(targets, origin.relative(behind, 2));
            addUniqueTarget(targets, origin.relative(left).relative(behind));
            addUniqueTarget(targets, origin.relative(right).relative(behind));
            addUniqueTarget(targets, origin.relative(facing));
        } else {
            addUniqueTarget(targets, origin.relative(behind));
            addUniqueTarget(targets, origin.relative(behind, 2));
            addUniqueTarget(targets, origin.relative(left).relative(behind));
            addUniqueTarget(targets, origin.relative(right).relative(behind));
        }

        for (int radius = 1; radius <= 2; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    addUniqueTarget(targets, origin.offset(dx, 0, dz));
                }
            }
        }

        return targets;
    }

    private static void addUniqueTarget(List<BlockPos> targets, BlockPos target) {
        if (!targets.contains(target)) {
            targets.add(target);
        }
    }

    private static List<BlockPos> targetHeightVariants(BlockPos baseTarget) {
        return List.of(baseTarget, baseTarget.above(), baseTarget.below());
    }

    private static void addFloorCandidate(ServerPlayer player, BlockPos target, List<PlacementCandidate> candidates) {
        BlockPos support = target.below();
        addCandidateIfValid(player, target, support, Direction.UP, Vec3.atCenterOf(support).add(0.0D, 0.5D, 0.0D), CandidateType.FLOOR, candidates);
    }

    private static void addCeilingCandidate(ServerPlayer player, BlockPos target, List<PlacementCandidate> candidates) {
        BlockPos support = target.above();
        addCandidateIfValid(player, target, support, Direction.DOWN, Vec3.atCenterOf(support).add(0.0D, -0.5D, 0.0D), CandidateType.CEILING, candidates);
    }

    private static void addWallCandidates(ServerPlayer player, BlockPos target, List<PlacementCandidate> candidates, boolean preferred) {
        Direction preferredFace = player.getDirection().getOpposite();
        if (preferred) {
            addWallCandidate(player, target, preferredFace, candidates);
        }
        for (Direction face : Direction.Plane.HORIZONTAL) {
            if (preferred && face == preferredFace) {
                continue;
            }
            addWallCandidate(player, target, face, candidates);
        }
    }

    private static void addWallCandidate(ServerPlayer player, BlockPos target, Direction face, List<PlacementCandidate> candidates) {
        BlockPos support = target.relative(face.getOpposite());
        Vec3 hit = Vec3.atCenterOf(support).add(face.getStepX() * 0.5D, 0.0D, face.getStepZ() * 0.5D);
        addCandidateIfValid(player, target, support, face, hit, CandidateType.WALL, candidates);
    }

    private static void addCandidateIfValid(ServerPlayer player, BlockPos target, BlockPos support, Direction face, Vec3 hit,
            CandidateType type, List<PlacementCandidate> candidates) {
        ServerLevel level = player.serverLevel();
        if (!level.isInWorldBounds(target) || !level.isInWorldBounds(support) || !level.isLoaded(target) || !level.isLoaded(support)) {
            return;
        }
        if (player.getBoundingBox().intersects(new AABB(target))) {
            return;
        }

        BlockState targetState = level.getBlockState(target);
        BlockState supportState = level.getBlockState(support);
        if (!canReplaceTarget(targetState) || !isSafeSupport(supportState) || supportState.is(TORCH_PLACER_AVOID_SUPPORTS)) {
            return;
        }
        if (type == CandidateType.WALL && supportState.is(TORCH_PLACER_NO_WALL_ATTACH)) {
            return;
        }
        if (!supportState.isFaceSturdy(level, support, face)) {
            return;
        }

        candidates.add(new PlacementCandidate(target.immutable(), support.immutable(), face, hit, type));
    }

    private static boolean canReplaceTarget(BlockState state) {
        return !state.is(TORCH_PLACER_NEVER_REPLACE)
                && state.getFluidState().isEmpty()
                && (state.isAir() || state.canBeReplaced());
    }

    private static boolean isSafeSupport(BlockState state) {
        return !state.isAir() && !state.hasBlockEntity();
    }

    private static boolean isSupportedTorchItem(ItemStack stack, TorchPlacerUpgradeData data) {
        if (stack.isEmpty()
                || stack.is(TORCH_PLACER_BLACKLIST)
                || stack.getItem() instanceof BackpackItem
                || stack.getItem() instanceof BackpackUpgradeItem
                || !(stack.getItem() instanceof BlockItem)) {
            return false;
        }

        if (stack.is(Items.TORCH) || stack.is(Items.SOUL_TORCH)) {
            return true;
        }

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!"minecraft".equals(id.getNamespace()) && (!data.moddedLightSources() || !SmartBackpacksConfig.torchPlacerAllowModdedLights())) {
            return false;
        }

        return stack.is(TORCH_PLACER_ITEMS);
    }

    private static boolean filterAllows(TorchPlacerUpgradeData data, ItemStack stack) {
        if (data.filterMode() == BuilderFilterMode.ALLOW_ALL) {
            return true;
        }

        boolean matched = false;
        for (ItemStack filter : data.loadFilterItems()) {
            if (!filter.isEmpty() && matches(data.matchMode(), stack, filter)) {
                matched = true;
                break;
            }
        }

        return data.filterMode() == BuilderFilterMode.WHITELIST ? matched : !matched;
    }

    private static boolean matches(BuilderMatchMode mode, ItemStack first, ItemStack second) {
        return mode == BuilderMatchMode.ITEM ? first.is(second.getItem()) : ItemStack.isSameItemSameComponents(first, second);
    }

    private static boolean canPlaceForMovement(ServerPlayer player, TorchPlacerUpgradeData data, PlayerMemory memory) {
        if (player.isSprinting() && !data.placeWhileSprinting()) {
            return false;
        }
        if (player.isShiftKeyDown() && !data.placeWhileSneaking()) {
            return false;
        }
        if (!data.placeWhileStandingStill()
                && memory.lastPlacementPos != null
                && memory.lastPosition.distSqr(player.blockPosition()) < MOVEMENT_EPSILON_SQR) {
            return false;
        }
        return true;
    }

    private static boolean isPlayerInBlockedFluid(Player player, TorchPlacerUpgradeData data) {
        FluidState fluid = player.level().getFluidState(player.blockPosition());
        return (!data.placeInWater() && fluid.is(Fluids.WATER)) || (!data.placeInLava() && fluid.is(Fluids.LAVA));
    }

    private static boolean canRunForGameMode(ServerPlayer player) {
        return player.gameMode.getGameModeForPlayer() != GameType.ADVENTURE || SmartBackpacksConfig.torchPlacerAllowAdventure();
    }

    private static TorchBackpack findTorchBackpack(ServerPlayer player) {
        TorchBackpack chest = createTorchBackpack(player.getItemBySlot(EquipmentSlot.CHEST), backpack -> player.setItemSlot(EquipmentSlot.CHEST, backpack));
        if (chest != null) {
            return chest;
        }

        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            int curioSlot = slot;
            TorchBackpack backpack = createTorchBackpack(CuriosCompat.getBackStack(player, curioSlot),
                    updated -> CuriosCompat.setBackStack(player, curioSlot, updated));
            if (backpack != null) {
                return backpack;
            }
        }

        for (int slot = 0; slot < 36; slot++) {
            int inventorySlot = slot;
            TorchBackpack backpack = createTorchBackpack(player.getInventory().getItem(slot), updated -> {
                player.getInventory().setItem(inventorySlot, updated);
                player.getInventory().setChanged();
            });
            if (backpack != null) {
                return backpack;
            }
        }

        return createTorchBackpack(player.getOffhandItem(), backpack -> {
            player.setItemInHand(InteractionHand.OFF_HAND, backpack);
            player.getInventory().setChanged();
        });
    }

    private static TorchBackpack createTorchBackpack(ItemStack stack, Consumer<ItemStack> saver) {
        if (!(stack.getItem() instanceof BackpackItem backpackItem)) {
            return null;
        }

        for (ItemStack upgrade : BackpackStackData.loadUpgrades(stack)) {
            if (!(upgrade.getItem() instanceof TorchPlacerUpgradeItem)) {
                continue;
            }

            TorchPlacerUpgradeData data = upgrade.getOrDefault(ModDataComponents.TORCH_PLACER_UPGRADE_DATA.get(), TorchPlacerUpgradeData.DEFAULT);
            if (data.enabled() && SmartBackpacksConfig.torchPlacerUpgradeEnabled()) {
                return new TorchBackpack(stack, backpackItem.getTier(), data, saver);
            }
        }
        return null;
    }

    private static boolean hasContainerOpen(ServerPlayer player) {
        return player.containerMenu != player.inventoryMenu;
    }

    private static boolean hasDelayPassed(long gameTime, long lastTick, int delayTicks) {
        return lastTick == Long.MIN_VALUE || gameTime - lastTick >= delayTicks;
    }

    private static void sendFailureFeedback(ServerPlayer player, PlayerMemory memory, PlacementResult result) {
        if (result == PlacementResult.SUCCESS) {
            return;
        }

        long gameTime = player.level().getGameTime();
        if (!hasDelayPassed(gameTime, memory.lastFeedbackTick, FAILURE_MESSAGE_DELAY_TICKS)) {
            return;
        }

        memory.lastFeedbackTick = gameTime;
        Component message = switch (result) {
            case NO_TORCH -> Component.translatable("message.smartbackpacks.torch_placer_no_torches");
            case NO_POSITION -> Component.translatable("message.smartbackpacks.torch_placer_no_position");
            case SUCCESS -> Component.empty();
        };
        player.displayClientMessage(message, true);
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(SmartBackpacks.MOD_ID, path));
    }

    private static TagKey<Block> blockTag(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(SmartBackpacks.MOD_ID, path));
    }

    private record TorchBackpack(ItemStack backpack, BackpackTier tier, TorchPlacerUpgradeData data, Consumer<ItemStack> saver) {
    }

    private record SourceSlot(int slot, ItemStack stack) {
    }

    private record PlacementCandidate(BlockPos target, BlockPos support, Direction face, Vec3 hit, CandidateType type) {
        private boolean accepts(ItemStack stack, TorchPlacerUpgradeData data) {
            return switch (this.type) {
                case FLOOR -> data.floorPlacement() && !stack.is(TORCH_PLACER_WALL_ITEMS);
                case WALL -> data.wallPlacement() && !stack.is(TORCH_PLACER_FLOOR_ITEMS);
                case CEILING -> data.ceilingPlacement() && !stack.is(TORCH_PLACER_WALL_ITEMS);
            };
        }
    }

    private enum CandidateType {
        FLOOR,
        WALL,
        CEILING
    }

    private enum PlacementResult {
        SUCCESS,
        NO_TORCH,
        NO_POSITION
    }

    private static final class PlayerMemory {
        private BlockPos lastPosition;
        private BlockPos lastPlacementPos;
        private long lastCheckTick = Long.MIN_VALUE;
        private long lastPlacementTick = Long.MIN_VALUE;
        private long lastFailureTick = Long.MIN_VALUE;
        private long lastFeedbackTick = Long.MIN_VALUE;

        private PlayerMemory(BlockPos lastPosition) {
            this.lastPosition = lastPosition.immutable();
        }
    }
}
