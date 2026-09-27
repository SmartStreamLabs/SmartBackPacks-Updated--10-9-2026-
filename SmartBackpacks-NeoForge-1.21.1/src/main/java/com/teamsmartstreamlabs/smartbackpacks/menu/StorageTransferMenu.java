package com.teamsmartstreamlabs.smartbackpacks.menu;

import com.teamsmartstreamlabs.smartbackpacks.blockentity.StorageTransferBlockEntity;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class StorageTransferMenu extends AbstractContainerMenu {
    public static final int PLAYER_INVENTORY_X = 8;
    public static final int PLAYER_INVENTORY_Y = 157;
    public static final int HOTBAR_Y = 215;
    private final BlockPos blockPos;
    private final Container filters;
    private final ContainerData data;
    private final StorageTransferBlockEntity blockEntity;

    public StorageTransferMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, buffer.readBlockPos(), null,
                new SimpleContainer(StorageTransferBlockEntity.FILTER_SLOTS),
                new SimpleContainerData(StorageTransferBlockEntity.DATA_COUNT));
    }

    public StorageTransferMenu(int containerId, Inventory playerInventory, StorageTransferBlockEntity blockEntity) {
        this(containerId, playerInventory, blockEntity.getBlockPos(), blockEntity,
                blockEntity, blockEntity.getDataAccess());
    }

    private StorageTransferMenu(int containerId, Inventory playerInventory, BlockPos blockPos,
            StorageTransferBlockEntity blockEntity, Container filters, ContainerData data) {
        super(ModMenuTypes.STORAGE_TRANSFER.get(), containerId);
        this.blockPos = blockPos;
        this.blockEntity = blockEntity;
        this.filters = filters;
        this.data = data;
        this.checkContainerDataCount(data, StorageTransferBlockEntity.DATA_COUNT);
        this.addDataSlots(data);

        for (int slot = 0; slot < StorageTransferBlockEntity.FILTER_SLOTS; slot++) {
            this.addSlot(new GhostFilterSlot(filters, slot, 43 + slot % 5 * 18, 79 + slot / 5 * 18));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9,
                        PLAYER_INVENTORY_X + column * 18, PLAYER_INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column, PLAYER_INVENTORY_X + column * 18, HOTBAR_Y));
        }
    }

    public boolean isImporter() {
        return this.data.get(0) != 0;
    }

    public boolean isAllowlist() {
        return this.data.get(1) != 0;
    }

    public int redstoneMode() {
        return this.data.get(2);
    }

    public int status() {
        return this.data.get(3);
    }

    public int transferInterval() {
        return this.data.get(4);
    }

    public int transferAmount() {
        return this.data.get(5);
    }

    public boolean matchComponents() {
        return this.data.get(6) != 0;
    }

    public boolean matchDurability() {
        return this.data.get(7) != 0;
    }

    public int transferMode() {
        return this.data.get(8);
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button < 0 || button > 3) {
            return false;
        }
        if (this.blockEntity != null) {
            this.blockEntity.handleButton(button);
        }
        return true;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < StorageTransferBlockEntity.FILTER_SLOTS && clickType == ClickType.PICKUP) {
            ItemStack carried = this.getCarried();
            ItemStack ghost = carried.isEmpty() || button == 1 ? ItemStack.EMPTY : carried.copyWithCount(1);
            if (this.blockEntity != null) {
                this.blockEntity.setFilter(slotId, ghost);
            } else {
                this.filters.setItem(slotId, ghost);
            }
            this.broadcastChanges();
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.blockEntity == null || this.blockEntity.stillValid(player);
    }

    private static final class GhostFilterSlot extends Slot {
        private GhostFilterSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
