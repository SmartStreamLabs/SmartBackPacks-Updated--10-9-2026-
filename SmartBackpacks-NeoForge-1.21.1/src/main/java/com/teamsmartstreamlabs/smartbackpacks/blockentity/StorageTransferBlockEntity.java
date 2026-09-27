package com.teamsmartstreamlabs.smartbackpacks.blockentity;

import java.util.List;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.block.StorageTransferBlock;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModBlockEntities;
import com.teamsmartstreamlabs.smartbackpacks.storage.ExternalInventoryAccess;
import com.teamsmartstreamlabs.smartbackpacks.storage.StorageNetworkService;
import com.teamsmartstreamlabs.smartbackpacks.storage.StorageNetworkService.TransferResult;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FilterUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FilterUpgradeMatcher;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.MagnetFilterInputType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class StorageTransferBlockEntity extends BlockEntity implements Container {
    public static final int FILTER_SLOTS = 10;
    public static final int DATA_COUNT = 9;
    private final NonNullList<ItemStack> filters = NonNullList.withSize(FILTER_SLOTS, ItemStack.EMPTY);
    private boolean allowlist;
    private boolean matchComponents;
    private boolean matchDurability;
    private RedstoneMode redstoneMode = RedstoneMode.IGNORE;
    private TransferMode transferMode = TransferMode.PUSH_TO_EXTERNAL;
    private Status status = Status.OFFLINE;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> isImporter() ? 1 : 0;
                case 1 -> allowlist ? 1 : 0;
                case 2 -> redstoneMode.ordinal();
                case 3 -> status.ordinal();
                case 4 -> transferInterval();
                case 5 -> transferAmount();
                case 6 -> matchComponents ? 1 : 0;
                case 7 -> matchDurability ? 1 : 0;
                case 8 -> transferMode.ordinal();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 1 && isImporter()) {
                allowlist = value != 0;
            } else if (index == 2) {
                redstoneMode = RedstoneMode.byOrdinal(value);
            } else if (index == 6) {
                matchComponents = value != 0;
            } else if (index == 7) {
                matchDurability = value != 0;
            } else if (index == 8 && !isImporter()) {
                transferMode = TransferMode.byOrdinal(value);
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public StorageTransferBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STORAGE_TRANSFER.get(), pos, state);
        this.allowlist = !this.isImporter();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StorageTransferBlockEntity blockEntity) {
        if (level.isClientSide() || !(state.getBlock() instanceof StorageTransferBlock)) {
            return;
        }
        int interval = Math.max(1, blockEntity.transferInterval());
        if (Math.floorMod(level.getGameTime() + pos.asLong(), interval) != 0L) {
            return;
        }
        blockEntity.runTransfer(level, state);
    }

    public ContainerData getDataAccess() {
        return this.dataAccess;
    }

    public boolean isImporter() {
        return this.getBlockState().getBlock() instanceof StorageTransferBlock block && block.isImporter();
    }

    public void handleButton(int button) {
        switch (button) {
            case 0 -> {
                if (this.isImporter()) {
                    this.allowlist = !this.allowlist;
                } else {
                    this.transferMode = this.transferMode.next();
                }
            }
            case 1 -> this.redstoneMode = this.redstoneMode.next();
            case 2 -> this.matchComponents = !this.matchComponents;
            case 3 -> this.matchDurability = !this.matchDurability;
            default -> {
                return;
            }
        }
        this.markConfigurationChanged();
    }

    public void setFilter(int slot, ItemStack stack) {
        if (slot < 0 || slot >= FILTER_SLOTS) {
            return;
        }
        this.filters.set(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        this.markConfigurationChanged();
    }

    private void runTransfer(Level level, BlockState state) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }
        if (this.isImporter() ? !SmartBackpacksConfig.storageImporterEnabled() : !SmartBackpacksConfig.storageExporterEnabled()) {
            this.setStatus(Status.OFFLINE);
            return;
        }
        if (SmartBackpacksConfig.storageTransferRedstoneControlEnabled() && !this.redstoneMode.allows(level.hasNeighborSignal(this.worldPosition))) {
            this.setStatus(Status.REDSTONE_DISABLED);
            return;
        }

        net.minecraft.core.Direction facing = state.getValue(StorageTransferBlock.FACING);
        BlockPos targetPos = this.worldPosition.relative(facing);
        if (!level.hasChunkAt(targetPos)) {
            this.setStatus(this.isImporter() ? Status.NO_BACKPACK : Status.NO_EXTERNAL_INVENTORY);
            return;
        }

        if (this.isImporter()) {
            if (!(level.getBlockEntity(targetPos) instanceof PlacedBackpackBlockEntity externalBackpack)) {
                this.setStatus(Status.NO_BACKPACK);
                return;
            }
            TransferResult result = StorageNetworkService.importStacksFrom(serverLevel, this.worldPosition, facing,
                    targetPos, externalBackpack, this::allows, this.transferAmount());
            this.setStatus(Status.from(result, true, false));
            return;
        }

        if (level.getBlockEntity(targetPos) instanceof PlacedBackpackBlockEntity externalBackpack) {
            boolean pulling = this.transferMode.isPulling();
            TransferResult result = pulling
                    ? StorageNetworkService.importFrom(serverLevel, this.worldPosition, facing, targetPos,
                    externalBackpack, this::allows, this.transferAmount())
                    : StorageNetworkService.exportTo(serverLevel, this.worldPosition, facing, targetPos,
                    externalBackpack, this::allows, this.transferAmount());
            this.setStatus(Status.from(result, pulling, false));
            return;
        }

        ExternalInventoryAccess external = ExternalInventoryAccess.find(serverLevel, targetPos, facing.getOpposite());
        if (external == null) {
            this.setStatus(level.getBlockState(targetPos).isAir()
                    ? Status.NO_EXTERNAL_INVENTORY
                    : Status.UNSUPPORTED_INVENTORY);
            return;
        }
        boolean pulling = this.transferMode.isPulling();
        TransferResult result = pulling
                ? StorageNetworkService.importFromExternal(serverLevel, this.worldPosition, facing, targetPos,
                external, this::allows, this.transferAmount())
                : StorageNetworkService.exportToExternal(serverLevel, this.worldPosition, facing, targetPos,
                external, this::allows, this.transferAmount());
        this.setStatus(Status.from(result, pulling, true));
    }

    private boolean allows(ItemStack candidate) {
        if (!this.isImporter() && this.transferMode == TransferMode.PULL_ALL) {
            return true;
        }
        if (!this.isImporter() && this.filters.stream().allMatch(ItemStack::isEmpty)) {
            return false;
        }
        FilterUpgradeData data = new FilterUpgradeData(
                true,
                this.isImporter() ? this.allowlist : true,
                ItemContainerContents.fromItems(this.filters),
                List.of(),
                List.of(),
                MagnetFilterInputType.ITEM,
                this.matchComponents,
                this.matchDurability,
                false);
        return FilterUpgradeMatcher.allows(data, candidate);
    }

    private int transferInterval() {
        return this.isImporter()
                ? SmartBackpacksConfig.storageImporterTransferInterval()
                : SmartBackpacksConfig.storageExporterTransferInterval();
    }

    private int transferAmount() {
        return this.isImporter()
                ? SmartBackpacksConfig.storageImporterTransferAmount()
                : SmartBackpacksConfig.storageExporterTransferAmount();
    }

    private void setStatus(Status status) {
        this.status = status;
    }

    private void markConfigurationChanged() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("Allowlist", this.allowlist);
        tag.putBoolean("MatchComponents", this.matchComponents);
        tag.putBoolean("MatchDurability", this.matchDurability);
        tag.putInt("RedstoneMode", this.redstoneMode.ordinal());
        tag.putInt("TransferMode", this.transferMode.ordinal());
        for (int slot = 0; slot < FILTER_SLOTS; slot++) {
            ItemStack stack = this.filters.get(slot);
            if (!stack.isEmpty()) {
                tag.put("Filter" + slot, stack.copyWithCount(1).save(registries));
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.allowlist = tag.contains("Allowlist") ? tag.getBoolean("Allowlist") : !this.isImporter();
        this.matchComponents = tag.getBoolean("MatchComponents");
        this.matchDurability = tag.getBoolean("MatchDurability");
        this.redstoneMode = RedstoneMode.byOrdinal(tag.getInt("RedstoneMode"));
        this.transferMode = tag.contains("TransferMode", Tag.TAG_INT)
                ? TransferMode.byOrdinal(tag.getInt("TransferMode"))
                : TransferMode.PUSH_TO_EXTERNAL;
        for (int slot = 0; slot < FILTER_SLOTS; slot++) {
            this.filters.set(slot, tag.contains("Filter" + slot, Tag.TAG_COMPOUND)
                    ? ItemStack.parseOptional(registries, tag.getCompound("Filter" + slot))
                    : ItemStack.EMPTY);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public int getContainerSize() {
        return FILTER_SLOTS;
    }

    @Override
    public boolean isEmpty() {
        return this.filters.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < FILTER_SLOTS ? this.filters.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(this.filters, slot, amount);
        if (!removed.isEmpty()) {
            this.markConfigurationChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.filters, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.setFilter(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.level != null
                && this.level.getBlockEntity(this.worldPosition) == this
                && player.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D,
                this.worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < FILTER_SLOTS; slot++) {
            this.filters.set(slot, ItemStack.EMPTY);
        }
        this.markConfigurationChanged();
    }

    public enum RedstoneMode {
        IGNORE,
        REQUIRE_SIGNAL,
        REQUIRE_NO_SIGNAL;

        private static RedstoneMode byOrdinal(int ordinal) {
            return values()[Math.floorMod(ordinal, values().length)];
        }

        private RedstoneMode next() {
            return byOrdinal(this.ordinal() + 1);
        }

        private boolean allows(boolean powered) {
            return switch (this) {
                case IGNORE -> true;
                case REQUIRE_SIGNAL -> powered;
                case REQUIRE_NO_SIGNAL -> !powered;
            };
        }
    }

    public enum TransferMode {
        PUSH_TO_EXTERNAL,
        PULL_FROM_EXTERNAL,
        PULL_ALL;

        public static TransferMode byOrdinal(int ordinal) {
            return values()[Math.floorMod(ordinal, values().length)];
        }

        private TransferMode next() {
            return byOrdinal(this.ordinal() + 1);
        }

        public boolean isPulling() {
            return this != PUSH_TO_EXTERNAL;
        }
    }

    public enum Status {
        ONLINE,
        IMPORTING,
        EXPORTING,
        TRANSFERRING,
        NO_BACKPACK,
        NO_EXTERNAL_INVENTORY,
        UNSUPPORTED_INVENTORY,
        NETWORK_FULL,
        TARGET_FULL,
        NO_MATCHING_ITEMS,
        OFFLINE,
        CONTROLLER_CONFLICT,
        REDSTONE_DISABLED,
        INVALID_TARGET,
        BLOCKED;

        private static Status from(TransferResult result, boolean importer, boolean genericInventory) {
            return switch (result) {
                case SUCCESS -> genericInventory ? TRANSFERRING : importer ? IMPORTING : EXPORTING;
                case NETWORK_FULL -> NETWORK_FULL;
                case TARGET_FULL -> TARGET_FULL;
                case NO_MATCHING_ITEMS -> NO_MATCHING_ITEMS;
                case CONTROLLER_CONFLICT -> CONTROLLER_CONFLICT;
                case INVALID_TARGET -> INVALID_TARGET;
                case BLOCKED -> BLOCKED;
                case OFFLINE -> OFFLINE;
            };
        }
    }
}
