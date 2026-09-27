package com.teamsmartstreamlabs.smartbackpacks.storage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.block.BackpackBlock;
import com.teamsmartstreamlabs.smartbackpacks.block.StorageCableBlock;
import com.teamsmartstreamlabs.smartbackpacks.block.StorageControllerBlock;
import com.teamsmartstreamlabs.smartbackpacks.blockentity.PlacedBackpackBlockEntity;
import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import com.teamsmartstreamlabs.smartbackpacks.storage.StorageNetworkSnapshot.Entry;
import com.teamsmartstreamlabs.smartbackpacks.storage.StorageNetworkSnapshot.Status;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class StorageNetworkService {
    public static final int MAX_DISPLAY_ENTRIES = 2048;
    private static final Direction[] DIRECTIONS = {
            Direction.DOWN, Direction.UP, Direction.NORTH,
            Direction.SOUTH, Direction.WEST, Direction.EAST
    };

    private StorageNetworkService() {
    }

    public static StorageNetworkSnapshot createSnapshot(ServerLevel level, BlockPos controllerPos) {
        Discovery discovery = discover(level, controllerPos);
        if (discovery.status() != Status.ONLINE) {
            return new StorageNetworkSnapshot(
                    discovery.status(), List.of(), discovery.backpacks().size(), discovery.totalSlots(), 0, false);
        }

        Map<StackKey, Long> totals = new LinkedHashMap<>();
        int usedSlots = 0;
        for (PlacedBackpackBlockEntity backpack : discovery.backpacks()) {
            for (ItemStack stack : backpack.createControllerStorageSnapshot()) {
                if (stack.isEmpty() || stack.getCount() <= 0) {
                    continue;
                }
                usedSlots++;
                StackKey key = new StackKey(stack);
                totals.merge(key, (long) stack.getCount(), StorageNetworkService::saturatedAdd);
            }
        }

        List<Entry> entries = new ArrayList<>(Math.min(totals.size(), MAX_DISPLAY_ENTRIES));
        for (Map.Entry<StackKey, Long> entry : totals.entrySet()) {
            if (entries.size() == MAX_DISPLAY_ENTRIES) {
                break;
            }
            entries.add(new Entry(entry.getKey().stack(), entry.getValue()));
        }
        return new StorageNetworkSnapshot(
                Status.ONLINE,
                entries,
                discovery.backpacks().size(),
                discovery.totalSlots(),
                usedSlots,
                totals.size() > MAX_DISPLAY_ENTRIES);
    }

    public static ItemStack insert(ServerPlayer player, BlockPos controllerPos, ItemStack incoming) {
        if (incoming.isEmpty() || !(player.level() instanceof ServerLevel level)) {
            return incoming;
        }
        return insert(player, level, controllerPos, incoming);
    }

    public static ItemStack insert(ServerPlayer player, ServerLevel level, BlockPos controllerPos, ItemStack incoming) {
        if (incoming.isEmpty()) {
            return incoming;
        }

        Discovery discovery = discover(level, controllerPos);
        if (discovery.status() != Status.ONLINE) {
            return incoming.copy();
        }

        List<EndpointState> states = loadStates(discovery.backpacks());
        ItemStack remaining = incoming.copy();
        insertIntoStates(states, remaining, false);
        commit(states);
        return remaining;
    }

    public static int extract(ServerPlayer player, BlockPos controllerPos, ItemStack requestedStack, int requestedAmount) {
        if (requestedStack.isEmpty() || requestedAmount <= 0 || !(player.level() instanceof ServerLevel level)) {
            return 0;
        }
        return extract(player, level, controllerPos, requestedStack, requestedAmount);
    }

    public static int extract(ServerPlayer player, ServerLevel level, BlockPos controllerPos,
            ItemStack requestedStack, int requestedAmount) {
        if (requestedStack.isEmpty() || requestedAmount <= 0) {
            return 0;
        }

        Discovery discovery = discover(level, controllerPos);
        if (discovery.status() != Status.ONLINE) {
            return 0;
        }

        int capacity = playerInventoryCapacity(player, requestedStack, requestedAmount);
        if (capacity <= 0) {
            return 0;
        }

        List<EndpointState> states = loadStates(discovery.backpacks());
        int remaining = capacity;
        int extracted = 0;
        for (EndpointState state : states) {
            for (int slot = 0; slot < state.items().size() && remaining > 0; slot++) {
                ItemStack stored = state.items().get(slot);
                if (stored.isEmpty()
                        || !ItemStackCompat.isSameItemSameComponents(stored, requestedStack)
                        || !state.backpack().canControllerExtract(slot, stored)) {
                    continue;
                }

                int taken = Math.min(remaining, stored.getCount());
                stored.shrink(taken);
                if (stored.isEmpty()) {
                    state.items().set(slot, ItemStack.EMPTY);
                }
                state.changed = true;
                extracted += taken;
                remaining -= taken;
            }
        }

        if (extracted <= 0) {
            return 0;
        }

        commit(states);
        ItemStack transfer = requestedStack.copyWithCount(extracted);
        insertIntoPlayerInventory(player, transfer);
        if (!transfer.isEmpty()) {
            // Capacity was calculated immediately before extraction, but restore safely if another hook rejected insertion.
            ItemStack notRestored = insert(player, level, controllerPos, transfer);
            if (!notRestored.isEmpty()) {
                player.getInventory().placeItemBackInInventory(notRestored);
            }
            extracted -= transfer.getCount();
        }
        return Math.max(0, extracted);
    }

    public static Discovery discover(ServerLevel level, BlockPos controllerPos) {
        if (!SmartBackpacksConfig.storageNetworkEnabled()
                || !level.hasChunkAt(controllerPos)
                || !(level.getBlockState(controllerPos).getBlock() instanceof StorageControllerBlock)) {
            return new Discovery(Status.OFFLINE, List.of(), 0, 0);
        }

        ArrayDeque<CableNode> queue = new ArrayDeque<>();
        Set<BlockPos> visitedCables = new HashSet<>();
        Set<BlockPos> endpointPositions = new HashSet<>();
        boolean nodeLimitReached = false;

        for (Direction direction : DIRECTIONS) {
            BlockPos next = controllerPos.relative(direction);
            if (!level.hasChunkAt(next)) {
                continue;
            }
            if (level.getBlockState(next).getBlock() instanceof StorageCableBlock) {
                BlockPos immutable = next.immutable();
                if (visitedCables.add(immutable)) {
                    queue.addLast(new CableNode(immutable, 1));
                }
            }
        }

        while (!queue.isEmpty()) {
            CableNode node = queue.removeFirst();
            for (Direction direction : DIRECTIONS) {
                BlockPos next = node.pos().relative(direction);
                if (!level.hasChunkAt(next)) {
                    continue;
                }

                BlockState state = level.getBlockState(next);
                if (state.getBlock() instanceof BackpackBlock
                        && level.getBlockEntity(next) instanceof PlacedBackpackBlockEntity) {
                    endpointPositions.add(next.immutable());
                    continue;
                }
                if (state.getBlock() instanceof StorageControllerBlock) {
                    continue;
                }
                if (!(state.getBlock() instanceof StorageCableBlock)
                        || node.depth() >= SmartBackpacksConfig.storageNetworkMaxCablePath()) {
                    continue;
                }

                BlockPos immutable = next.immutable();
                if (visitedCables.add(immutable)) {
                    if (visitedCables.size() > SmartBackpacksConfig.storageNetworkMaxNodes()) {
                        nodeLimitReached = true;
                        queue.clear();
                        break;
                    }
                    queue.addLast(new CableNode(immutable, node.depth() + 1));
                }
            }
        }

        List<BlockPos> sortedPositions = new ArrayList<>(endpointPositions);
        sortedPositions.sort(Comparator.comparingLong(BlockPos::asLong));
        List<PlacedBackpackBlockEntity> backpacks = new ArrayList<>(sortedPositions.size());
        long totalSlots = 0L;
        for (BlockPos pos : sortedPositions) {
            if (level.getBlockEntity(pos) instanceof PlacedBackpackBlockEntity backpack) {
                backpacks.add(backpack);
                totalSlots += backpack.getContainerSize();
            }
        }

        Status status = nodeLimitReached
                ? Status.NETWORK_TOO_LARGE
                : backpacks.isEmpty() ? Status.NO_BACKPACKS : Status.ONLINE;
        return new Discovery(status, List.copyOf(backpacks), clampToInt(totalSlots), visitedCables.size());
    }

    public static TransferResult importFrom(ServerLevel level, BlockPos devicePos, Direction front,
            BlockPos externalPos, PlacedBackpackBlockEntity externalBackpack,
            Predicate<ItemStack> filter, int maximumAmount) {
        return importFrom(level, devicePos, front, externalPos, externalBackpack, filter, maximumAmount, false);
    }

    public static TransferResult importStacksFrom(ServerLevel level, BlockPos devicePos, Direction front,
            BlockPos externalPos, PlacedBackpackBlockEntity externalBackpack,
            Predicate<ItemStack> filter, int maximumStacks) {
        return importFrom(level, devicePos, front, externalPos, externalBackpack, filter, maximumStacks, true);
    }

    private static TransferResult importFrom(ServerLevel level, BlockPos devicePos, Direction front,
            BlockPos externalPos, PlacedBackpackBlockEntity externalBackpack,
            Predicate<ItemStack> filter, int limit, boolean fullStacks) {
        NetworkAccess access = findDeviceNetwork(level, devicePos, front);
        if (access.result() != null) {
            return access.result();
        }
        if (access.discovery().backpacks().contains(externalBackpack)) {
            return TransferResult.INVALID_TARGET;
        }

        NonNullList<ItemStack> sourceItems = externalBackpack.createControllerStorageSnapshot();
        List<EndpointState> networkStates = loadStates(access.discovery().backpacks());
        boolean foundMatching = false;
        int movedStacks = 0;
        for (int slot = 0; slot < sourceItems.size() && (!fullStacks || movedStacks < Math.max(1, limit)); slot++) {
            ItemStack stored = sourceItems.get(slot);
            if (stored.isEmpty() || !filter.test(stored) || !externalBackpack.canControllerExtract(slot, stored)) {
                continue;
            }
            foundMatching = true;
            int requested = fullStacks ? stored.getCount() : Math.min(Math.max(1, limit), stored.getCount());
            ItemStack remaining = stored.copyWithCount(requested);
            insertIntoStates(networkStates, remaining, false);
            int moved = requested - remaining.getCount();
            if (moved <= 0) {
                continue;
            }

            stored.shrink(moved);
            if (stored.isEmpty()) {
                sourceItems.set(slot, ItemStack.EMPTY);
            }
            movedStacks++;
            if (!fullStacks) {
                break;
            }
        }
        if (movedStacks > 0) {
            if (level.getBlockEntity(externalPos) != externalBackpack
                    || !externalBackpack.claimStorageTransfer(level.getGameTime())) {
                return TransferResult.BLOCKED;
            }
            commit(networkStates);
            externalBackpack.applyControllerStorageSnapshot(sourceItems);
            return TransferResult.SUCCESS;
        }
        return foundMatching ? TransferResult.NETWORK_FULL : TransferResult.NO_MATCHING_ITEMS;
    }

    public static TransferResult exportTo(ServerLevel level, BlockPos devicePos, Direction front,
            BlockPos externalPos, PlacedBackpackBlockEntity externalBackpack,
            Predicate<ItemStack> filter, int maximumAmount) {
        NetworkAccess access = findDeviceNetwork(level, devicePos, front);
        if (access.result() != null) {
            return access.result();
        }
        if (access.discovery().backpacks().contains(externalBackpack)) {
            return TransferResult.INVALID_TARGET;
        }

        List<EndpointState> networkStates = loadStates(access.discovery().backpacks());
        EndpointState targetState = new EndpointState(externalBackpack, externalBackpack.createControllerStorageSnapshot());
        boolean foundMatching = false;
        for (EndpointState state : networkStates) {
            for (int slot = 0; slot < state.items().size(); slot++) {
                ItemStack stored = state.items().get(slot);
                if (stored.isEmpty() || !filter.test(stored) || !state.backpack().canControllerExtract(slot, stored)) {
                    continue;
                }
                foundMatching = true;
                int requested = Math.min(Math.max(1, maximumAmount), stored.getCount());
                ItemStack remaining = stored.copyWithCount(requested);
                insertIntoStates(List.of(targetState), remaining, false);
                int moved = requested - remaining.getCount();
                if (moved <= 0) {
                    continue;
                }

                if (level.getBlockEntity(externalPos) != externalBackpack
                        || !externalBackpack.claimStorageTransfer(level.getGameTime())) {
                    return TransferResult.BLOCKED;
                }
                stored.shrink(moved);
                if (stored.isEmpty()) {
                    state.items().set(slot, ItemStack.EMPTY);
                }
                state.changed = true;
                commit(networkStates);
                commit(List.of(targetState));
                return TransferResult.SUCCESS;
            }
        }
        return foundMatching ? TransferResult.TARGET_FULL : TransferResult.NO_MATCHING_ITEMS;
    }

    public static TransferResult exportToExternal(ServerLevel level, BlockPos devicePos, Direction front,
            BlockPos externalPos, ExternalInventoryAccess external,
            Predicate<ItemStack> filter, int maximumAmount) {
        NetworkAccess access = findDeviceNetwork(level, devicePos, front);
        if (access.result() != null) {
            return access.result();
        }
        if (isNetworkEndpoint(access.discovery(), externalPos)) {
            return TransferResult.INVALID_TARGET;
        }

        List<EndpointState> networkStates = loadStates(access.discovery().backpacks());
        boolean foundMatching = false;
        for (EndpointState state : networkStates) {
            for (int slot = 0; slot < state.items().size(); slot++) {
                ItemStack stored = state.items().get(slot);
                if (stored.isEmpty() || !filter.test(stored) || !state.backpack().canControllerExtract(slot, stored)) {
                    continue;
                }
                foundMatching = true;
                int requested = Math.min(Math.max(1, maximumAmount), stored.getCount());
                int accepted = Math.min(requested, external.simulateInsert(stored.copyWithCount(requested)));
                if (accepted <= 0) {
                    continue;
                }
                if (!external.isStillValid()) {
                    return TransferResult.BLOCKED;
                }

                int moved = Math.min(accepted, external.insert(stored.copyWithCount(accepted)));
                if (moved <= 0) {
                    continue;
                }
                stored.shrink(moved);
                if (stored.isEmpty()) {
                    state.items().set(slot, ItemStack.EMPTY);
                }
                state.changed = true;
                commit(networkStates);
                return TransferResult.SUCCESS;
            }
        }
        return foundMatching ? TransferResult.TARGET_FULL : TransferResult.NO_MATCHING_ITEMS;
    }

    public static TransferResult importFromExternal(ServerLevel level, BlockPos devicePos, Direction front,
            BlockPos externalPos, ExternalInventoryAccess external,
            Predicate<ItemStack> filter, int maximumAmount) {
        NetworkAccess access = findDeviceNetwork(level, devicePos, front);
        if (access.result() != null) {
            return access.result();
        }
        if (isNetworkEndpoint(access.discovery(), externalPos)) {
            return TransferResult.INVALID_TARGET;
        }

        boolean foundMatching = false;
        for (ItemStack candidate : external.contents()) {
            if (candidate.isEmpty() || !filter.test(candidate)) {
                continue;
            }
            foundMatching = true;
            int requested = Math.min(Math.max(1, maximumAmount), candidate.getCount());
            ItemStack available = external.simulateExtract(candidate, requested);
            if (available.isEmpty()) {
                continue;
            }

            List<EndpointState> plannedStates = loadStates(access.discovery().backpacks());
            ItemStack plannedRemainder = available.copy();
            insertIntoStates(plannedStates, plannedRemainder, false);
            int accepted = available.getCount() - plannedRemainder.getCount();
            if (accepted <= 0) {
                continue;
            }
            if (!external.isStillValid()) {
                return TransferResult.BLOCKED;
            }

            ItemStack extracted = external.extract(candidate, accepted);
            if (extracted.isEmpty()) {
                continue;
            }
            List<EndpointState> finalStates = loadStates(access.discovery().backpacks());
            ItemStack remainder = extracted.copy();
            insertIntoStates(finalStates, remainder, false);
            int moved = extracted.getCount() - remainder.getCount();
            if (moved <= 0) {
                external.insert(extracted);
                continue;
            }
            if (!remainder.isEmpty()) {
                external.insert(remainder);
            }
            commit(finalStates);
            return TransferResult.SUCCESS;
        }
        return foundMatching ? TransferResult.NETWORK_FULL : TransferResult.NO_MATCHING_ITEMS;
    }

    private static boolean isNetworkEndpoint(Discovery discovery, BlockPos externalPos) {
        return discovery.backpacks().stream().anyMatch(backpack -> backpack.getBlockPos().equals(externalPos));
    }

    private static NetworkAccess findDeviceNetwork(ServerLevel level, BlockPos devicePos, Direction front) {
        if (!SmartBackpacksConfig.storageNetworkEnabled() || !level.hasChunkAt(devicePos)) {
            return new NetworkAccess(TransferResult.OFFLINE, null);
        }

        ArrayDeque<CableNode> queue = new ArrayDeque<>();
        Set<BlockPos> visitedCables = new HashSet<>();
        Set<BlockPos> controllers = new HashSet<>();
        for (Direction direction : DIRECTIONS) {
            if (direction == front) {
                continue;
            }
            BlockPos next = devicePos.relative(direction);
            if (level.hasChunkAt(next) && level.getBlockState(next).getBlock() instanceof StorageCableBlock) {
                BlockPos immutable = next.immutable();
                visitedCables.add(immutable);
                queue.addLast(new CableNode(immutable, 1));
            }
        }

        while (!queue.isEmpty()) {
            CableNode node = queue.removeFirst();
            for (Direction direction : DIRECTIONS) {
                BlockPos next = node.pos().relative(direction);
                if (!level.hasChunkAt(next)) {
                    continue;
                }
                BlockState state = level.getBlockState(next);
                if (state.getBlock() instanceof StorageControllerBlock) {
                    controllers.add(next.immutable());
                    continue;
                }
                if (!(state.getBlock() instanceof StorageCableBlock)
                        || node.depth() >= SmartBackpacksConfig.storageNetworkMaxCablePath()) {
                    continue;
                }
                BlockPos immutable = next.immutable();
                if (visitedCables.add(immutable)) {
                    if (visitedCables.size() > SmartBackpacksConfig.storageNetworkMaxNodes()) {
                        return new NetworkAccess(TransferResult.OFFLINE, null);
                    }
                    queue.addLast(new CableNode(immutable, node.depth() + 1));
                }
            }
        }

        List<BlockPos> sortedControllers = new ArrayList<>(controllers);
        sortedControllers.sort(Comparator.comparingLong(BlockPos::asLong));
        for (BlockPos controllerPos : sortedControllers) {
            Discovery discovery = discover(level, controllerPos);
            if (discovery.status() == Status.CONTROLLER_CONFLICT) {
                return new NetworkAccess(TransferResult.CONTROLLER_CONFLICT, null);
            }
            if (discovery.status() == Status.ONLINE) {
                return new NetworkAccess(null, discovery);
            }
        }
        return new NetworkAccess(TransferResult.OFFLINE, null);
    }

    private static List<EndpointState> loadStates(List<PlacedBackpackBlockEntity> backpacks) {
        List<EndpointState> states = new ArrayList<>(backpacks.size());
        for (PlacedBackpackBlockEntity backpack : backpacks) {
            states.add(new EndpointState(backpack, backpack.createControllerStorageSnapshot()));
        }
        return states;
    }

    private static void insertIntoStates(List<EndpointState> states, ItemStack remaining, boolean emptySlots) {
        for (EndpointState state : states) {
            for (int slot = 0; slot < state.items().size() && !remaining.isEmpty(); slot++) {
                ItemStack existing = state.items().get(slot);
                if (existing.isEmpty() != emptySlots) {
                    continue;
                }

                if (emptySlots) {
                    if (!state.backpack().canControllerInsert(slot, ItemStack.EMPTY, remaining)) {
                        continue;
                    }
                    int placed = Math.min(remaining.getCount(), state.backpack().getControllerStackLimit(remaining));
                    if (placed > 0) {
                        state.items().set(slot, remaining.copyWithCount(placed));
                        remaining.shrink(placed);
                        state.changed = true;
                    }
                    continue;
                }

                if (!ItemStackCompat.isSameItemSameComponents(existing, remaining)
                        || !state.backpack().canControllerInsert(slot, existing, remaining)) {
                    continue;
                }
                int available = state.backpack().getControllerStackLimit(existing) - existing.getCount();
                int placed = Math.min(remaining.getCount(), Math.max(0, available));
                if (placed > 0) {
                    existing.grow(placed);
                    remaining.shrink(placed);
                    state.changed = true;
                }
            }
        }
        if (!remaining.isEmpty() && !emptySlots) {
            insertIntoStates(states, remaining, true);
        }
    }

    private static void commit(List<EndpointState> states) {
        for (EndpointState state : states) {
            if (state.changed) {
                state.backpack().applyControllerStorageSnapshot(state.items());
            }
        }
    }

    private static int playerInventoryCapacity(ServerPlayer player, ItemStack stack, int requestedAmount) {
        long capacity = 0L;
        for (ItemStack existing : player.getInventory().items) {
            if (existing.isEmpty()) {
                capacity += stack.getMaxStackSize();
            } else if (ItemStackCompat.isSameItemSameComponents(existing, stack)) {
                capacity += Math.max(0, existing.getMaxStackSize() - existing.getCount());
            }
            if (capacity >= requestedAmount) {
                return requestedAmount;
            }
        }
        return (int) Math.min(capacity, requestedAmount);
    }

    private static void insertIntoPlayerInventory(ServerPlayer player, ItemStack transfer) {
        int legalStackLimit = Math.max(1, transfer.getMaxStackSize());
        while (!transfer.isEmpty()) {
            int offered = Math.min(legalStackLimit, transfer.getCount());
            ItemStack legalStack = transfer.copyWithCount(offered);
            player.getInventory().add(legalStack);
            int accepted = offered - legalStack.getCount();
            if (accepted <= 0) {
                break;
            }
            transfer.shrink(accepted);
        }
    }

    private static long saturatedAdd(long left, long right) {
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

    private static int clampToInt(long value) {
        return value >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    public record Discovery(Status status, List<PlacedBackpackBlockEntity> backpacks, int totalSlots, int cableNodes) {
    }

    public enum TransferResult {
        SUCCESS,
        NETWORK_FULL,
        TARGET_FULL,
        NO_MATCHING_ITEMS,
        OFFLINE,
        CONTROLLER_CONFLICT,
        INVALID_TARGET,
        BLOCKED
    }

    private record NetworkAccess(TransferResult result, Discovery discovery) {
    }

    private record CableNode(BlockPos pos, int depth) {
    }

    private static final class EndpointState {
        private final PlacedBackpackBlockEntity backpack;
        private final NonNullList<ItemStack> items;
        private boolean changed;

        private EndpointState(PlacedBackpackBlockEntity backpack, NonNullList<ItemStack> items) {
            this.backpack = backpack;
            this.items = items;
        }

        private PlacedBackpackBlockEntity backpack() {
            return this.backpack;
        }

        private NonNullList<ItemStack> items() {
            return this.items;
        }
    }

    private static final class StackKey {
        private final ItemStack stack;
        private final int hash;

        private StackKey(ItemStack stack) {
            this.stack = stack.copyWithCount(1);
            this.hash = 31 * BuiltInRegistries.ITEM.getId(this.stack.getItem()) + Objects.hashCode(this.stack.getTag());
        }

        private ItemStack stack() {
            return this.stack;
        }

        @Override
        public int hashCode() {
            return this.hash;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof StackKey key && ItemStackCompat.isSameItemSameComponents(this.stack, key.stack);
        }
    }
}
