package com.teamsmartstreamlabs.smartbackpacks.menu;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackUpgradeInventory;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.RescueUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.RescueUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.RescueUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.ItemLockProtection;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RescueUpgradeMenu extends AbstractContainerMenu {
    public static final int RESCUE_SLOT_X = 18;
    public static final int RESCUE_SLOT_Y = 60;
    public static final int PLAYER_INVENTORY_X = 8;
    public static final int PLAYER_INVENTORY_Y = 196;
    public static final int PLAYER_HOTBAR_Y = 254;
    public static final int INVENTORY_LABEL_Y = 184;

    private final BackpackAccess access;
    private final Player owner;
    private final BackpackUpgradeInventory upgradeInventory;
    private final int upgradeSlotIndex;
    private final RescueContainer rescueContainer;
    private RescueUpgradeData settings;

    public RescueUpgradeMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, BackpackAccess.fromNetwork(playerInventory.player, buffer), buffer.readVarInt());
    }

    public RescueUpgradeMenu(int containerId, Inventory playerInventory, BackpackAccess access, int upgradeSlotIndex) {
        super(ModMenuTypes.RESCUE_UPGRADE.get(), containerId);
        this.access = access;
        this.owner = playerInventory.player;
        this.upgradeInventory = new BackpackUpgradeInventory(playerInventory.player, access);
        this.upgradeSlotIndex = upgradeSlotIndex;
        this.rescueContainer = new RescueContainer();
        this.loadUpgradeData();
        this.addRescueSlots();
        this.addPlayerInventory(playerInventory);
    }

    public RescueUpgradeData getSettings() {
        return this.settings;
    }

    public void setEnabled(boolean enabled) {
        this.settings = this.settings.withEnabled(enabled);
        this.saveUpgradeData();
    }

    public void toggleTotem() {
        this.settings = this.settings.withModeToggles(!this.settings.totemEnabled(), this.settings.goldenAppleEnabled(),
                this.settings.fallEnabled(), this.settings.lavaEnabled());
        this.saveUpgradeData();
    }

    public void toggleGoldenApple() {
        this.settings = this.settings.withModeToggles(this.settings.totemEnabled(), !this.settings.goldenAppleEnabled(),
                this.settings.fallEnabled(), this.settings.lavaEnabled());
        this.saveUpgradeData();
    }

    public void toggleFall() {
        this.settings = this.settings.withModeToggles(this.settings.totemEnabled(), this.settings.goldenAppleEnabled(),
                !this.settings.fallEnabled(), this.settings.lavaEnabled());
        this.saveUpgradeData();
    }

    public void toggleLava() {
        this.settings = this.settings.withModeToggles(this.settings.totemEnabled(), this.settings.goldenAppleEnabled(),
                this.settings.fallEnabled(), !this.settings.lavaEnabled());
        this.saveUpgradeData();
    }

    public void changeHealthThreshold(int delta) {
        this.settings = this.settings.withThresholds(this.settings.healthThreshold() + delta,
                this.settings.lavaHealthThreshold(), this.settings.minimumFallDistance());
        this.saveUpgradeData();
    }

    public void changeLavaThreshold(int delta) {
        this.settings = this.settings.withThresholds(this.settings.healthThreshold(),
                this.settings.lavaHealthThreshold() + delta, this.settings.minimumFallDistance());
        this.saveUpgradeData();
    }

    public void changeFallDistance(int delta) {
        this.settings = this.settings.withThresholds(this.settings.healthThreshold(),
                this.settings.lavaHealthThreshold(), this.settings.minimumFallDistance() + delta);
        this.saveUpgradeData();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = this.slots.get(index);
        if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack sourceCopy = sourceStack.copy();
        int rescueEnd = RescueUpgradeData.SLOT_COUNT;
        int playerStart = rescueEnd;

        if (index < rescueEnd) {
            if (!this.moveItemStackTo(sourceStack, playerStart, this.slots.size(), true)
                    && !this.moveItemStackToBackpackStorage(sourceStack)) {
                return ItemStack.EMPTY;
            }
        } else if (RescueUpgradeHandler.isSupportedRescueItem(sourceStack)) {
            if (!this.moveItemStackTo(sourceStack, 0, rescueEnd, false)
                    && !this.moveItemStackToBackpackStorage(sourceStack)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackToBackpackStorage(sourceStack)) {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        return sourceCopy;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.upgradeInventory.stillValid(player) && this.getUpgradeStack().getItem() instanceof RescueUpgradeItem;
    }

    private void addRescueSlots() {
        for (int slot = 0; slot < RescueUpgradeData.SLOT_COUNT; slot++) {
            this.addSlot(new RescueSlot(this.rescueContainer, slot, RESCUE_SLOT_X + slot * 34, RESCUE_SLOT_Y));
        }
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int index = column + row * 9 + 9;
                this.addSlot(new Slot(playerInventory, index, PLAYER_INVENTORY_X + column * 18, PLAYER_INVENTORY_Y + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column, PLAYER_INVENTORY_X + column * 18, PLAYER_HOTBAR_Y));
        }
    }

    private void loadUpgradeData() {
        this.settings = this.getUpgradeStack().getOrDefault(ModDataComponents.RESCUE_UPGRADE_DATA.get(), RescueUpgradeData.DEFAULT);
        NonNullList<ItemStack> items = this.settings.loadItems();
        for (int slot = 0; slot < RescueUpgradeData.SLOT_COUNT; slot++) {
            this.rescueContainer.items.set(slot, items.get(slot));
        }
    }

    private void saveUpgradeData() {
        ItemStack upgradeStack = this.getUpgradeStack();
        if (!(upgradeStack.getItem() instanceof RescueUpgradeItem)) {
            return;
        }

        upgradeStack.set(ModDataComponents.RESCUE_UPGRADE_DATA.get(), this.settings.withItems(this.rescueContainer.items));
        this.upgradeInventory.setItem(this.upgradeSlotIndex, upgradeStack);
        this.broadcastChanges();
    }

    private boolean moveItemStackToBackpackStorage(ItemStack sourceStack) {
        if (sourceStack.isEmpty()) {
            return false;
        }

        ItemStack backpack = this.access.getBackpackStack(this.owner);
        if (!(backpack.getItem() instanceof BackpackItem backpackItem) || !BackpackStackData.isValidStorageItem(backpack, sourceStack)) {
            return false;
        }

        int originalCount = sourceStack.getCount();
        ItemStack remainder = ItemLockProtection.insertIntoStorage(backpack, backpackItem.getTier(), sourceStack, false);
        sourceStack.setCount(remainder.getCount());
        this.access.setBackpackStack(this.owner, backpack);
        this.owner.getInventory().setChanged();
        return sourceStack.getCount() != originalCount;
    }

    private ItemStack getUpgradeStack() {
        if (this.upgradeSlotIndex < 0 || this.upgradeSlotIndex >= this.upgradeInventory.getContainerSize()) {
            return ItemStack.EMPTY;
        }
        return this.upgradeInventory.getItem(this.upgradeSlotIndex);
    }

    private final class RescueContainer implements Container {
        private final NonNullList<ItemStack> items = NonNullList.withSize(RescueUpgradeData.SLOT_COUNT, ItemStack.EMPTY);

        @Override
        public int getContainerSize() {
            return RescueUpgradeData.SLOT_COUNT;
        }

        @Override
        public boolean isEmpty() {
            return this.items.stream().allMatch(ItemStack::isEmpty);
        }

        @Override
        public ItemStack getItem(int slot) {
            return this.items.get(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack removed = net.minecraft.world.ContainerHelper.removeItem(this.items, slot, amount);
            if (!removed.isEmpty()) {
                this.setChanged();
            }
            return removed;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return net.minecraft.world.ContainerHelper.takeItem(this.items, slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            this.items.set(slot, RescueUpgradeHandler.isSupportedRescueItem(stack) ? stack : ItemStack.EMPTY);
            this.items.get(slot).limitSize(this.getMaxStackSize(this.items.get(slot)));
            this.setChanged();
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return stack.getMaxStackSize();
        }

        @Override
        public void setChanged() {
            RescueUpgradeMenu.this.saveUpgradeData();
        }

        @Override
        public boolean stillValid(Player player) {
            return RescueUpgradeMenu.this.stillValid(player);
        }

        @Override
        public void clearContent() {
            for (int slot = 0; slot < this.items.size(); slot++) {
                this.items.set(slot, ItemStack.EMPTY);
            }
            this.setChanged();
        }
    }

    private static final class RescueSlot extends Slot {
        private RescueSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return RescueUpgradeHandler.isSupportedRescueItem(stack);
        }
    }
}
