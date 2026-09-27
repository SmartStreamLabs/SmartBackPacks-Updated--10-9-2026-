package com.teamsmartstreamlabs.smartbackpacks.menu;

import com.teamsmartstreamlabs.smartbackpacks.network.StorageControllerSnapshotPayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModBlocks;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.storage.StorageNetworkService;
import com.teamsmartstreamlabs.smartbackpacks.storage.StorageNetworkSnapshot;
import com.teamsmartstreamlabs.smartbackpacks.storage.StorageMonitorAccess;
import com.teamsmartstreamlabs.smartbackpacks.storage.StorageMonitorLink;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class StorageControllerMenu extends AbstractContainerMenu {
    public static final int PLAYER_INVENTORY_X = 79;
    public static final int PLAYER_INVENTORY_Y = 168;
    public static final int HOTBAR_Y = 226;
    private static final int PLAYER_SLOT_COUNT = 36;

    private final BlockPos accessPos;
    private final StorageMonitorLink target;
    private final boolean remote;
    private StorageNetworkSnapshot snapshot = StorageNetworkSnapshot.OFFLINE;

    public StorageControllerMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, readContext(buffer));
    }

    public StorageControllerMenu(int containerId, Inventory playerInventory, BlockPos controllerPos) {
        this(containerId, playerInventory, new MenuContext(false, controllerPos,
                new StorageMonitorLink(playerInventory.player.level().dimension(), controllerPos)));
    }

    public StorageControllerMenu(int containerId, Inventory playerInventory, BlockPos accessPos, StorageMonitorLink link) {
        this(containerId, playerInventory, new MenuContext(true, accessPos, link));
    }

    private StorageControllerMenu(int containerId, Inventory playerInventory, MenuContext context) {
        super(ModMenuTypes.STORAGE_CONTROLLER.get(), containerId);
        this.remote = context.remote();
        this.accessPos = context.accessPos().immutable();
        this.target = context.target();
        this.addPlayerInventory(playerInventory);
        if (playerInventory.player instanceof ServerPlayer serverPlayer) {
            this.snapshot = this.createSnapshot(serverPlayer);
        }
    }

    public BlockPos getControllerPos() {
        return this.target.controllerPos();
    }

    public StorageNetworkSnapshot getSnapshot() {
        return this.snapshot;
    }

    public void updateSnapshot(StorageNetworkSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public void refreshAndSync(ServerPlayer player) {
        if (player.containerMenu != this) {
            return;
        }
        this.snapshot = this.createSnapshot(player);
        PacketDistributor.sendToPlayer(player, new StorageControllerSnapshotPayload(this.containerId, this.snapshot));
        this.broadcastChanges();
    }

    public void extract(ServerPlayer player, ItemStack stack, int amount) {
        ServerLevel targetLevel = this.resolveTargetLevel(player);
        if (targetLevel != null) {
            StorageNetworkService.extract(player, targetLevel, this.target.controllerPos(), stack, amount);
        }
        this.refreshAndSync(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!(player instanceof ServerPlayer serverPlayer) || index < 0 || index >= this.slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack original = slot.getItem().copy();
        ServerLevel targetLevel = this.resolveTargetLevel(serverPlayer);
        if (targetLevel == null) {
            this.refreshAndSync(serverPlayer);
            return ItemStack.EMPTY;
        }
        ItemStack remainder = StorageNetworkService.insert(serverPlayer, targetLevel, this.target.controllerPos(), slot.getItem());
        if (remainder.getCount() == original.getCount()) {
            this.refreshAndSync(serverPlayer);
            return ItemStack.EMPTY;
        }

        slot.set(remainder.isEmpty() ? ItemStack.EMPTY : remainder);
        slot.setChanged();
        this.refreshAndSync(serverPlayer);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        if (!this.remote) {
            if (player.distanceToSqr(
                    this.accessPos.getX() + 0.5D, this.accessPos.getY() + 0.5D, this.accessPos.getZ() + 0.5D) > 64.0D) {
                return false;
            }
            return player.level().dimension().equals(this.target.dimension())
                    && player.level().getBlockState(this.target.controllerPos()).is(ModBlocks.STORAGE_CONTROLLER.get());
        }
        if (player instanceof ServerPlayer serverPlayer) {
            return StorageMonitorAccess.resolve(serverPlayer, this.target).available();
        }
        return true;
    }

    public static void writeContext(RegistryFriendlyByteBuf buffer, boolean remote, BlockPos accessPos, StorageMonitorLink target) {
        buffer.writeBoolean(remote);
        buffer.writeBlockPos(accessPos);
        buffer.writeUtf(target.dimension().identifier().toString());
        buffer.writeBlockPos(target.controllerPos());
    }

    private static MenuContext readContext(RegistryFriendlyByteBuf buffer) {
        boolean remote = buffer.readBoolean();
        BlockPos accessPos = buffer.readBlockPos();
        ResourceKey<net.minecraft.world.level.Level> dimension = ResourceKey.create(
                Registries.DIMENSION, Identifier.parse(buffer.readUtf()));
        return new MenuContext(remote, accessPos, new StorageMonitorLink(dimension, buffer.readBlockPos()));
    }

    private StorageNetworkSnapshot createSnapshot(ServerPlayer player) {
        ServerLevel targetLevel = this.resolveTargetLevel(player);
        return targetLevel == null ? StorageNetworkSnapshot.OFFLINE
                : StorageNetworkService.createSnapshot(targetLevel, this.target.controllerPos());
    }

    private ServerLevel resolveTargetLevel(ServerPlayer player) {
        if (this.remote) {
            StorageMonitorAccess.Result access = StorageMonitorAccess.resolve(player, this.target);
            return access.available() ? access.targetLevel() : null;
        }
        if (!(player.level() instanceof ServerLevel level)
                || !level.dimension().equals(this.target.dimension())
                || !level.getBlockState(this.target.controllerPos()).is(ModBlocks.STORAGE_CONTROLLER.get())) {
            return null;
        }
        return level;
    }

    private record MenuContext(boolean remote, BlockPos accessPos, StorageMonitorLink target) {
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9,
                        PLAYER_INVENTORY_X + column * 18, PLAYER_INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column, PLAYER_INVENTORY_X + column * 18, HOTBAR_Y));
        }
    }
}
