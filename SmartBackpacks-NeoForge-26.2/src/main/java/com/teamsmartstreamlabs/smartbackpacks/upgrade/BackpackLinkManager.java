package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.block.BackpackBlock;
import com.teamsmartstreamlabs.smartbackpacks.blockentity.PlacedBackpackBlockEntity;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackLinkUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.LinkCrystalItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import com.teamsmartstreamlabs.smartbackpacks.util.PlayerMessageHelper;

public final class BackpackLinkManager {
    private BackpackLinkManager() {
    }

    public static BackpackLinkSavedData registry(ServerLevel level) {
        ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
        ServerLevel storageLevel = overworld != null ? overworld : level;
        return storageLevel.getDataStorage().computeIfAbsent(BackpackLinkSavedData.FACTORY);
    }

    public static boolean hasLinkUpgrade(ItemStack backpack) {
        if (!(backpack.getItem() instanceof BackpackItem)) {
            return false;
        }

        for (ItemStack upgrade : BackpackStackData.loadUpgrades(backpack)) {
            if (upgrade.getItem() instanceof BackpackLinkUpgradeItem) {
                return true;
            }
        }
        return false;
    }

    public static void refreshPlacedAnchor(ServerLevel level, BlockPos pos, ItemStack backpack) {
        BackpackLinkData data = backpack.getOrDefault(ModDataComponents.BACKPACK_LINK_DATA.get(), BackpackLinkData.EMPTY);
        if (!data.hasAnchor()) {
            return;
        }

        BackpackLinkSavedData registry = registry(level);
        List<UUID> links = registry.get(data.anchorId()).map(BackpackLinkAnchor::links).orElse(data.links());
        if (!hasLinkUpgrade(backpack) || !data.active() || data.visibility() == BackpackLinkVisibility.DISABLED) {
            registry.get(data.anchorId()).ifPresent(anchor -> registry.upsert(anchor.withPlacement(level.dimension(), pos, false, true, level.getGameTime())));
            return;
        }

        registry.upsert(anchorFrom(data, level, pos, true, true, links));
    }

    public static void markOffline(ServerLevel level, ItemStack backpack) {
        BackpackLinkData data = backpack.getOrDefault(ModDataComponents.BACKPACK_LINK_DATA.get(), BackpackLinkData.EMPTY);
        if (data.hasAnchor()) {
            registry(level).markOffline(data.anchorId(), level.getGameTime());
        }
    }

    public static BackpackLinkSnapshot createSnapshot(ServerPlayer player, BackpackAccess access) {
        ItemStack backpack = access.getBackpackStack(player);
        boolean placed = access.source() == BackpackAccess.Source.BLOCK;
        boolean hasUpgrade = hasLinkUpgrade(backpack);
        if (!SmartBackpacksConfig.backpackLinkUpgradeEnabled()) {
            return baseSnapshot(player, access, placed, hasUpgrade, "message.smartbackpacks.backpack_link.disabled_config", List.of());
        }
        if (!placed) {
            return baseSnapshot(player, access, false, hasUpgrade, "message.smartbackpacks.backpack_link.not_placed", List.of());
        }
        if (!hasUpgrade) {
            return baseSnapshot(player, access, true, false, "message.smartbackpacks.backpack_link.no_upgrade", List.of());
        }

        Optional<PlacedBackpackBlockEntity> blockEntity = getPlacedBlockEntity(player, access);
        if (blockEntity.isEmpty()) {
            return baseSnapshot(player, access, true, false, "message.smartbackpacks.backpack_link.source_invalid", List.of());
        }

        BackpackLinkData data = backpack.getOrDefault(ModDataComponents.BACKPACK_LINK_DATA.get(), BackpackLinkData.EMPTY);
        if (!data.hasAnchor()) {
            return baseSnapshot(player, access, true, true, "message.smartbackpacks.backpack_link.inactive", List.of());
        }

        ServerLevel level = (ServerLevel) player.level();
        refreshPlacedAnchor(level, access.blockPos(), backpack);
        BackpackLinkSavedData registry = registry(level);
        List<BackpackLinkDestinationEntry> destinations = new ArrayList<>();
        for (UUID destinationId : registry.get(data.anchorId()).map(BackpackLinkAnchor::links).orElse(data.links())) {
            registry.get(destinationId).ifPresent(anchor -> destinations.add(toDestinationEntry(player, data.anchorId(), anchor)));
        }
        destinations.sort(Comparator.comparing((BackpackLinkDestinationEntry entry) -> !entry.canTeleport())
                .thenComparingInt(entry -> entry.sameDimension() ? entry.distanceBlocks() : Integer.MAX_VALUE)
                .thenComparing(BackpackLinkDestinationEntry::name, String.CASE_INSENSITIVE_ORDER));

        return new BackpackLinkSnapshot(true, true, true, true, data.active(),
                data.anchorId(), displayName(data, backpack), dimensionText(level.dimension()), access.blockPos(),
                data.visibility(), SmartBackpacksConfig.backpackLinkMaxDestinations(), statusForSource(player, data), destinations);
    }

    public static void activate(ServerPlayer player, BackpackAccess access) {
        if (!SmartBackpacksConfig.backpackLinkUpgradeEnabled()) {
            message(player, "message.smartbackpacks.backpack_link.disabled_config");
            return;
        }

        Optional<PlacedBackpackBlockEntity> blockEntity = getPlacedBlockEntity(player, access);
        if (access.source() != BackpackAccess.Source.BLOCK || blockEntity.isEmpty()) {
            message(player, "message.smartbackpacks.backpack_link.not_placed");
            return;
        }

        ItemStack backpack = blockEntity.get().getStoredBackpack().copy();
        if (!hasLinkUpgrade(backpack)) {
            message(player, "message.smartbackpacks.backpack_link.no_upgrade");
            return;
        }

        BackpackLinkData data = ensureIdentity(player, backpack);
        if (!canManage(player, data)) {
            message(player, "message.smartbackpacks.backpack_link.no_permission");
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        BackpackLinkSavedData registry = registry(level);
        Optional<BackpackLinkAnchor> duplicate = registry.get(data.anchorId())
                .filter(anchor -> anchor.active() && anchor.placed())
                .filter(anchor -> !anchor.dimension().equals(level.dimension()) || !anchor.position().equals(access.blockPos()));
        if (duplicate.isPresent()) {
            SmartBackpacks.LOGGER.warn("Duplicate active Backpack Link anchor {} detected at {} and {}. New placement received a fresh disabled identity.",
                    data.anchorId(), duplicate.get().position(), access.blockPos());
            data = data.withAnchorId(UUID.randomUUID()).withActive(false);
            backpack.set(ModDataComponents.BACKPACK_LINK_DATA.get(), data);
            blockEntity.get().setStoredBackpack(backpack);
            message(player, "message.smartbackpacks.backpack_link.duplicate_identity");
            return;
        }

        data = data.withActive(true);
        if (data.visibility() == BackpackLinkVisibility.DISABLED) {
            data = data.withVisibility(BackpackLinkVisibility.PRIVATE).withActive(true);
        }
        data = withRegistryLinks(level, data);
        backpack.set(ModDataComponents.BACKPACK_LINK_DATA.get(), data);
        blockEntity.get().setStoredBackpack(backpack);
        registry.upsert(anchorFrom(data, level, access.blockPos(), true, true));
        message(player, "message.smartbackpacks.backpack_link.activated");
        level.playSound(null, access.blockPos(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 0.7F, 1.2F);
    }

    public static void deactivate(ServerPlayer player, BackpackAccess access) {
        ItemStack backpack = access.getBackpackStack(player).copy();
        BackpackLinkData data = backpack.getOrDefault(ModDataComponents.BACKPACK_LINK_DATA.get(), BackpackLinkData.EMPTY);
        if (!data.hasAnchor()) {
            message(player, "message.smartbackpacks.backpack_link.inactive");
            return;
        }
        if (!canManage(player, data)) {
            message(player, "message.smartbackpacks.backpack_link.no_permission");
            return;
        }

        if (player.level() instanceof ServerLevel level) {
            data = withRegistryLinks(level, data);
        }
        data = data.withActive(false);
        backpack.set(ModDataComponents.BACKPACK_LINK_DATA.get(), data);
        access.setBackpackStack(player, backpack);
        if (player.level() instanceof ServerLevel level) {
            BackpackLinkSavedData savedData = registry(level);
            BackpackLinkData updatedData = data;
            savedData.get(updatedData.anchorId()).ifPresent(anchor ->
                    savedData.upsert(anchor.withSettings(updatedData.name(), updatedData.visibility(), false, updatedData.revision(), anchor.links())));
        }
        message(player, "message.smartbackpacks.backpack_link.deactivated");
    }

    public static void rename(ServerPlayer player, BackpackAccess access, String rawName) {
        ItemStack backpack = access.getBackpackStack(player).copy();
        BackpackLinkData data = backpack.getOrDefault(ModDataComponents.BACKPACK_LINK_DATA.get(), BackpackLinkData.EMPTY);
        if (!data.hasAnchor()) {
            message(player, "message.smartbackpacks.backpack_link.inactive");
            return;
        }
        if (!canManage(player, data)) {
            message(player, "message.smartbackpacks.backpack_link.no_permission");
            return;
        }

        String name = BackpackLinkData.sanitizeName(rawName);
        if (name.isBlank()) {
            name = defaultAnchorName(backpack);
        }
        data = data.withName(name);
        if (player.level() instanceof ServerLevel level) {
            data = withRegistryLinks(level, data);
        }
        backpack.set(ModDataComponents.BACKPACK_LINK_DATA.get(), data);
        access.setBackpackStack(player, backpack);
        updateRegistrySettings(player, access, data);
        message(player, "message.smartbackpacks.backpack_link.renamed");
    }

    public static void cycleVisibility(ServerPlayer player, BackpackAccess access) {
        ItemStack backpack = access.getBackpackStack(player).copy();
        BackpackLinkData data = backpack.getOrDefault(ModDataComponents.BACKPACK_LINK_DATA.get(), BackpackLinkData.EMPTY);
        if (!data.hasAnchor()) {
            message(player, "message.smartbackpacks.backpack_link.inactive");
            return;
        }
        if (!canManage(player, data)) {
            message(player, "message.smartbackpacks.backpack_link.no_permission");
            return;
        }

        data = data.withVisibility(data.visibility().next(SmartBackpacksConfig.backpackLinkAllowPublicAnchors()));
        if (player.level() instanceof ServerLevel level) {
            data = withRegistryLinks(level, data);
        }
        backpack.set(ModDataComponents.BACKPACK_LINK_DATA.get(), data);
        access.setBackpackStack(player, backpack);
        updateRegistrySettings(player, access, data);
        message(player, "message.smartbackpacks.backpack_link.visibility_changed");
    }

    public static void bindOrCompleteCrystal(ServerPlayer player, BackpackAccess access) {
        ItemStack backpack = access.getBackpackStack(player);
        BackpackLinkData sourceData = backpack.getOrDefault(ModDataComponents.BACKPACK_LINK_DATA.get(), BackpackLinkData.EMPTY);
        if (access.source() != BackpackAccess.Source.BLOCK || !sourceData.hasAnchor() || !sourceData.active()) {
            message(player, "message.smartbackpacks.backpack_link.activate_first");
            return;
        }
        if (!canManage(player, sourceData)) {
            message(player, "message.smartbackpacks.backpack_link.no_permission");
            return;
        }

        ItemStack crystal = findLinkCrystal(player);
        if (crystal.isEmpty()) {
            message(player, "message.smartbackpacks.backpack_link.no_crystal");
            return;
        }

        LinkCrystalData crystalData = crystal.getOrDefault(ModDataComponents.LINK_CRYSTAL_DATA.get(), LinkCrystalData.EMPTY);
        if (!crystalData.isBound()) {
            crystal.set(ModDataComponents.LINK_CRYSTAL_DATA.get(), new LinkCrystalData(sourceData.anchorId(), displayName(sourceData, backpack)));
            message(player, "message.smartbackpacks.backpack_link.crystal_bound");
            return;
        }
        if (crystalData.anchorId().equals(sourceData.anchorId())) {
            message(player, "message.smartbackpacks.backpack_link.self_link");
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        BackpackLinkSavedData registry = registry(level);
        int maxDestinations = SmartBackpacksConfig.backpackLinkMaxDestinations();
        if (!registry.addLink(sourceData.anchorId(), crystalData.anchorId(), maxDestinations)) {
            message(player, "message.smartbackpacks.backpack_link.link_failed");
            return;
        }

        crystal.remove(ModDataComponents.LINK_CRYSTAL_DATA.get());
        syncPlacedBackpackLinks(level.getServer(), registry, sourceData.anchorId());
        syncPlacedBackpackLinks(level.getServer(), registry, crystalData.anchorId());
        message(player, "message.smartbackpacks.backpack_link.linked");
        level.playSound(null, access.blockPos(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8F, 1.0F);
    }

    public static void unlink(ServerPlayer player, BackpackAccess access, UUID destinationId) {
        ItemStack backpack = access.getBackpackStack(player).copy();
        BackpackLinkData data = backpack.getOrDefault(ModDataComponents.BACKPACK_LINK_DATA.get(), BackpackLinkData.EMPTY);
        if (!data.hasAnchor()) {
            return;
        }
        if (!canManage(player, data)) {
            message(player, "message.smartbackpacks.backpack_link.no_permission");
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        BackpackLinkSavedData registry = registry(level);
        registry.removeLink(data.anchorId(), destinationId);
        syncPlacedBackpackLinks(level.getServer(), registry, data.anchorId());
        syncPlacedBackpackLinks(level.getServer(), registry, destinationId);
        message(player, "message.smartbackpacks.backpack_link.unlinked");
    }

    public static void teleport(ServerPlayer player, BackpackAccess access, UUID destinationId) {
        if (!SmartBackpacksConfig.backpackLinkUpgradeEnabled()) {
            message(player, "message.smartbackpacks.backpack_link.disabled_config");
            return;
        }
        if (player.isPassenger()) {
            message(player, "message.smartbackpacks.backpack_link.dismount");
            return;
        }

        ItemStack sourceBackpack = access.getBackpackStack(player);
        BackpackLinkData sourceData = sourceBackpack.getOrDefault(ModDataComponents.BACKPACK_LINK_DATA.get(), BackpackLinkData.EMPTY);
        if (access.source() != BackpackAccess.Source.BLOCK || !sourceData.hasAnchor() || !sourceData.active()) {
            message(player, "message.smartbackpacks.backpack_link.source_invalid");
            return;
        }

        ServerLevel sourceLevel = (ServerLevel) player.level();
        BackpackLinkSavedData registry = registry(sourceLevel);
        Optional<BackpackLinkAnchor> sourceAnchor = registry.get(sourceData.anchorId());
        Optional<BackpackLinkAnchor> destinationAnchor = registry.get(destinationId);
        if (sourceAnchor.isEmpty() || destinationAnchor.isEmpty() || !sourceAnchor.get().links().contains(destinationId)) {
            message(player, "message.smartbackpacks.backpack_link.destination_missing");
            return;
        }

        Validation destination = validateDestination(player.level().getServer(), destinationAnchor.get(), player);
        if (!destination.valid()) {
            message(player, destination.messageKey());
            return;
        }
        if (!canTeleportTo(player, destinationAnchor.get())) {
            message(player, "message.smartbackpacks.backpack_link.no_permission");
            return;
        }

        boolean crossDimension = !destinationAnchor.get().dimension().equals(sourceLevel.dimension());
        if (crossDimension && !SmartBackpacksConfig.backpackLinkAllowCrossDimension()) {
            message(player, "message.smartbackpacks.backpack_link.cross_dimension_disabled");
            return;
        }

        int cost = calculateCost(sourceAnchor.get(), destinationAnchor.get());
        if (!player.getAbilities().instabuild && player.experienceLevel < cost) {
            PlayerMessageHelper.sendStatus(player, Component.translatable("message.smartbackpacks.backpack_link.not_enough_xp", cost));
            return;
        }

        Optional<Vec3> arrival = findSafeArrival(destination.level(), destinationAnchor.get().position(), player);
        if (arrival.isEmpty()) {
            message(player, "message.smartbackpacks.backpack_link.no_safe_arrival");
            return;
        }

        if (!player.getAbilities().instabuild && cost > 0) {
            player.giveExperienceLevels(-cost);
        }
        Vec3 oldPosition = player.position();
        ServerLevel oldLevel = sourceLevel;
        player.closeContainer();
        oldLevel.sendParticles(ParticleTypes.PORTAL, oldPosition.x, oldPosition.y + 1.0D, oldPosition.z, 28, 0.35D, 0.45D, 0.35D, 0.02D);
        oldLevel.playSound(null, oldPosition.x, oldPosition.y, oldPosition.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7F, 1.0F);

        Vec3 target = arrival.get();
        boolean teleported = player.teleportTo(destination.level(), target.x, target.y, target.z, Set.of(), player.getYRot(), player.getXRot(), true);
        if (!teleported) {
            if (!player.getAbilities().instabuild && cost > 0) {
                player.giveExperienceLevels(cost);
            }
            message(player, "message.smartbackpacks.backpack_link.teleport_failed");
            return;
        }

        destination.level().sendParticles(ParticleTypes.REVERSE_PORTAL, target.x, target.y + 1.0D, target.z, 36, 0.35D, 0.45D, 0.35D, 0.03D);
        destination.level().playSound(null, target.x, target.y, target.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.0F);
        PlayerMessageHelper.sendStatus(player, Component.translatable("message.smartbackpacks.backpack_link.teleported", destinationAnchor.get().name(), cost));
    }

    public static int calculateCost(BackpackLinkAnchor source, BackpackLinkAnchor destination) {
        int cost;
        if (!source.dimension().equals(destination.dimension())) {
            cost = SmartBackpacksConfig.backpackLinkCrossDimensionXpLevels();
        } else {
            double distance = Math.sqrt(source.position().distSqr(destination.position()));
            cost = SmartBackpacksConfig.backpackLinkSameDimensionBaseXpLevels()
                    + (int) Math.floor(distance / 1000.0D) * SmartBackpacksConfig.backpackLinkDistanceXpPer1000Blocks();
        }
        int max = SmartBackpacksConfig.backpackLinkMaxXpLevelCost();
        return max > 0 ? Math.min(cost, max) : cost;
    }

    private static BackpackLinkSnapshot baseSnapshot(ServerPlayer player, BackpackAccess access, boolean placed, boolean hasUpgrade, String statusKey,
            List<BackpackLinkDestinationEntry> destinations) {
        ItemStack backpack = access.getBackpackStack(player);
        BackpackLinkData data = backpack.getOrDefault(ModDataComponents.BACKPACK_LINK_DATA.get(), BackpackLinkData.EMPTY);
        String dimension = player.level() instanceof ServerLevel level ? dimensionText(level.dimension()) : "";
        BlockPos pos = placed ? access.blockPos() : BlockPos.ZERO;
        return new BackpackLinkSnapshot(placed, hasUpgrade, SmartBackpacksConfig.backpackLinkUpgradeEnabled(), data.hasAnchor(),
                data.active(), data.anchorId(), displayName(data, backpack), dimension, pos,
                data.visibility(), SmartBackpacksConfig.backpackLinkMaxDestinations(), statusKey, destinations);
    }

    private static BackpackLinkDestinationEntry toDestinationEntry(ServerPlayer player, UUID sourceId, BackpackLinkAnchor anchor) {
        boolean sameDimension = player.level().dimension().equals(anchor.dimension());
        int distance = sameDimension ? (int) Math.floor(Math.sqrt(player.blockPosition().distSqr(anchor.position()))) : -1;
        String status = destinationStatus(player.level().getServer(), player, sourceId, anchor);
        int cost = registry((ServerLevel) player.level()).get(sourceId).map(source -> calculateCost(source, anchor)).orElse(0);
        return new BackpackLinkDestinationEntry(anchor.anchorId(), anchor.name(), dimensionText(anchor.dimension()), anchor.position(),
                anchor.active(), anchor.placed(), sameDimension, distance, cost, 0, status);
    }

    private static String destinationStatus(MinecraftServer server, ServerPlayer player, UUID sourceId, BackpackLinkAnchor anchor) {
        if (anchor.anchorId().equals(sourceId)) {
            return "message.smartbackpacks.backpack_link.self_link";
        }
        if (!anchor.placed()) {
            return "message.smartbackpacks.backpack_link.destination_not_placed";
        }
        if (!anchor.active() || anchor.visibility() == BackpackLinkVisibility.DISABLED) {
            return "message.smartbackpacks.backpack_link.destination_offline";
        }
        if (!anchor.dimension().equals(player.level().dimension()) && !SmartBackpacksConfig.backpackLinkAllowCrossDimension()) {
            return "message.smartbackpacks.backpack_link.cross_dimension_disabled";
        }
        if (!canTeleportTo(player, anchor)) {
            return "message.smartbackpacks.backpack_link.no_permission";
        }
        Validation validation = validateDestination(server, anchor, player);
        return validation.valid() ? "message.smartbackpacks.backpack_link.ready" : validation.messageKey();
    }

    private static Validation validateDestination(MinecraftServer server, BackpackLinkAnchor anchor, ServerPlayer player) {
        if (server == null) {
            return Validation.invalid("message.smartbackpacks.backpack_link.destination_missing");
        }
        ServerLevel level = server.getLevel(anchor.dimension());
        if (level == null) {
            return Validation.invalid("message.smartbackpacks.backpack_link.destination_missing");
        }
        if (!level.isLoaded(anchor.position())) {
            return Validation.invalid("message.smartbackpacks.backpack_link.destination_chunk_unloaded");
        }
        if (!(level.getBlockEntity(anchor.position()) instanceof PlacedBackpackBlockEntity blockEntity)) {
            registry(level).markOffline(anchor.anchorId(), level.getGameTime());
            return Validation.invalid("message.smartbackpacks.backpack_link.destination_missing");
        }
        ItemStack backpack = blockEntity.getStoredBackpack();
        BackpackLinkData data = backpack.getOrDefault(ModDataComponents.BACKPACK_LINK_DATA.get(), BackpackLinkData.EMPTY);
        if (!hasLinkUpgrade(backpack) || !data.anchorId().equals(anchor.anchorId()) || !data.active()) {
            registry(level).upsert(anchor.withPlacement(level.dimension(), anchor.position(), false, true, level.getGameTime()));
            return Validation.invalid("message.smartbackpacks.backpack_link.destination_offline");
        }
        return new Validation(true, "message.smartbackpacks.backpack_link.ready", level);
    }

    private static Optional<Vec3> findSafeArrival(ServerLevel level, BlockPos anchorPos, ServerPlayer player) {
        ArrayList<BlockPos> candidates = new ArrayList<>();
        Direction facing = Direction.NORTH;
        BlockState anchorState = level.getBlockState(anchorPos);
        if (anchorState.getBlock() instanceof BackpackBlock && anchorState.hasProperty(BackpackBlock.FACING)) {
            facing = anchorState.getValue(BackpackBlock.FACING);
        }
        candidates.add(anchorPos.relative(facing));
        candidates.add(anchorPos.relative(facing.getClockWise()));
        candidates.add(anchorPos.relative(facing.getCounterClockWise()));
        candidates.add(anchorPos.relative(facing.getOpposite()));
        candidates.add(anchorPos.above().relative(facing));

        int radius = SmartBackpacksConfig.backpackLinkSafeArrivalRadius();
        for (int distance = 1; distance <= radius; distance++) {
            for (int x = -distance; x <= distance; x++) {
                for (int z = -distance; z <= distance; z++) {
                    if (Math.max(Math.abs(x), Math.abs(z)) != distance) {
                        continue;
                    }
                    candidates.add(anchorPos.offset(x, 0, z));
                    candidates.add(anchorPos.offset(x, 1, z));
                }
            }
        }

        for (BlockPos candidate : candidates) {
            if (isSafeArrival(level, candidate, player)) {
                return Optional.of(Vec3.atBottomCenterOf(candidate));
            }
        }
        return Optional.empty();
    }

    private static boolean isSafeArrival(ServerLevel level, BlockPos feetPos, ServerPlayer player) {
        if (!level.isInWorldBounds(feetPos) || !level.getWorldBorder().isWithinBounds(feetPos) || !level.isLoaded(feetPos)) {
            return false;
        }
        BlockPos floorPos = feetPos.below();
        if (!level.isLoaded(floorPos) || !level.getWorldBorder().isWithinBounds(floorPos)) {
            return false;
        }

        BlockState floor = level.getBlockState(floorPos);
        BlockState feet = level.getBlockState(feetPos);
        BlockState head = level.getBlockState(feetPos.above());
        if (isUnsafeFloor(floor) || isUnsafeBody(feet) || isUnsafeBody(head)) {
            return false;
        }
        if (!floor.entityCanStandOn(level, floorPos, player)) {
            return false;
        }

        Vec3 target = Vec3.atBottomCenterOf(feetPos);
        return level.noCollision(player, player.getDimensions(Pose.STANDING).makeBoundingBox(target));
    }

    private static boolean isUnsafeFloor(BlockState state) {
        return state.isAir()
                || state.is(Blocks.LAVA)
                || state.is(Blocks.FIRE)
                || state.is(Blocks.SOUL_FIRE)
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.MAGMA_BLOCK)
                || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.SWEET_BERRY_BUSH)
                || state.is(Blocks.NETHER_PORTAL)
                || state.is(Blocks.END_PORTAL)
                || state.getBlock() instanceof CampfireBlock;
    }

    private static boolean isUnsafeBody(BlockState state) {
        return state.is(Blocks.LAVA)
                || state.is(Blocks.FIRE)
                || state.is(Blocks.SOUL_FIRE)
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.NETHER_PORTAL)
                || state.is(Blocks.END_PORTAL);
    }

    private static BackpackLinkData ensureIdentity(ServerPlayer player, ItemStack backpack) {
        BackpackLinkData data = backpack.getOrDefault(ModDataComponents.BACKPACK_LINK_DATA.get(), BackpackLinkData.EMPTY);
        if (!data.hasAnchor()) {
            data = data.withIdentity(UUID.randomUUID(), player.getUUID(), defaultAnchorName(backpack));
        } else if (!data.hasOwner()) {
            data = data.withIdentity(data.anchorId(), player.getUUID(), displayName(data, backpack));
        }
        backpack.set(ModDataComponents.BACKPACK_LINK_DATA.get(), data);
        return data;
    }

    private static BackpackLinkAnchor anchorFrom(BackpackLinkData data, ServerLevel level, BlockPos pos, boolean active, boolean placed) {
        return anchorFrom(data, level, pos, active, placed, data.links());
    }

    private static BackpackLinkAnchor anchorFrom(BackpackLinkData data, ServerLevel level, BlockPos pos, boolean active, boolean placed, List<UUID> links) {
        return new BackpackLinkAnchor(data.anchorId(), data.ownerId(), level.dimension(), pos, data.name(), data.visibility(),
                active, placed, data.revision(), level.getGameTime(), links);
    }

    private static void updateRegistrySettings(ServerPlayer player, BackpackAccess access, BackpackLinkData data) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        BackpackLinkSavedData registry = registry(level);
        List<UUID> links = registry.get(data.anchorId()).map(BackpackLinkAnchor::links).orElse(data.links());
        registry.upsert(anchorFrom(data.withLinks(links), level, access.blockPos(), data.active(), access.source() == BackpackAccess.Source.BLOCK));
    }

    private static BackpackLinkData withRegistryLinks(ServerLevel level, BackpackLinkData data) {
        if (!data.hasAnchor()) {
            return data;
        }
        return registry(level).get(data.anchorId())
                .map(BackpackLinkAnchor::links)
                .filter(links -> !links.equals(data.links()))
                .map(data::withLinks)
                .orElse(data);
    }

    private static void syncPlacedBackpackLinks(MinecraftServer server, BackpackLinkSavedData registry, UUID anchorId) {
        if (server == null) {
            return;
        }
        registry.get(anchorId).ifPresent(anchor -> {
            ServerLevel level = server.getLevel(anchor.dimension());
            if (level == null || !level.isLoaded(anchor.position())) {
                return;
            }
            if (!(level.getBlockEntity(anchor.position()) instanceof PlacedBackpackBlockEntity blockEntity)) {
                return;
            }

            ItemStack backpack = blockEntity.getStoredBackpack().copy();
            BackpackLinkData data = backpack.getOrDefault(ModDataComponents.BACKPACK_LINK_DATA.get(), BackpackLinkData.EMPTY);
            if (data.hasAnchor() && data.anchorId().equals(anchorId) && !data.links().equals(anchor.links())) {
                backpack.set(ModDataComponents.BACKPACK_LINK_DATA.get(), data.withLinks(anchor.links()));
                blockEntity.setStoredBackpack(backpack);
            }
        });
    }

    private static Optional<PlacedBackpackBlockEntity> getPlacedBlockEntity(ServerPlayer player, BackpackAccess access) {
        if (access.source() != BackpackAccess.Source.BLOCK || !(player.level() instanceof ServerLevel)) {
            return Optional.empty();
        }
        return Optional.ofNullable(player.level().getBlockEntity(access.blockPos()))
                .filter(PlacedBackpackBlockEntity.class::isInstance)
                .map(PlacedBackpackBlockEntity.class::cast);
    }

    private static boolean canManage(ServerPlayer player, BackpackLinkData data) {
        return !data.hasOwner() || data.ownerId().equals(player.getUUID()) || player.getAbilities().instabuild;
    }

    private static boolean canTeleportTo(ServerPlayer player, BackpackLinkAnchor anchor) {
        if (anchor.visibility() == BackpackLinkVisibility.PUBLIC && SmartBackpacksConfig.backpackLinkAllowPublicAnchors()) {
            return true;
        }
        return anchor.ownerId().equals(player.getUUID()) || player.getAbilities().instabuild;
    }

    private static ItemStack findLinkCrystal(ServerPlayer player) {
        ItemStack unboundCrystal = ItemStack.EMPTY;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!(stack.getItem() instanceof LinkCrystalItem)) {
                continue;
            }
            if (stack.getOrDefault(ModDataComponents.LINK_CRYSTAL_DATA.get(), LinkCrystalData.EMPTY).isBound()) {
                return stack;
            }
            if (unboundCrystal.isEmpty()) {
                unboundCrystal = stack;
            }
        }
        return unboundCrystal;
    }

    private static String statusForSource(ServerPlayer player, BackpackLinkData data) {
        if (!data.active()) {
            return "message.smartbackpacks.backpack_link.inactive";
        }
        if (data.visibility() == BackpackLinkVisibility.DISABLED) {
            return "message.smartbackpacks.backpack_link.destination_offline";
        }
        if (!canManage(player, data)) {
            return "message.smartbackpacks.backpack_link.no_permission";
        }
        return "message.smartbackpacks.backpack_link.anchor_active";
    }

    private static String displayName(BackpackLinkData data, ItemStack backpack) {
        return data.name().isBlank() ? defaultAnchorName(backpack) : data.name();
    }

    private static String defaultAnchorName(ItemStack backpack) {
        String name = backpack.getHoverName().getString();
        return BackpackLinkData.sanitizeName(name.isBlank() ? "Backpack Anchor" : name);
    }

    private static String dimensionText(net.minecraft.resources.ResourceKey<Level> dimension) {
        return dimension.identifier().toString();
    }

    private static void message(ServerPlayer player, String key) {
        PlayerMessageHelper.sendStatus(player, Component.translatable(key));
    }

    private record Validation(boolean valid, String messageKey, ServerLevel level) {
        private static Validation invalid(String messageKey) {
            return new Validation(false, messageKey, null);
        }
    }
}
