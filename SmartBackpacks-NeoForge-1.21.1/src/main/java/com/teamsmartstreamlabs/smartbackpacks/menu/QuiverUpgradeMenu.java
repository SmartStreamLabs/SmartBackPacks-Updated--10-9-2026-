package com.teamsmartstreamlabs.smartbackpacks.menu;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackUpgradeInventory;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.QuiverUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.QuiverSelectionMode;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.QuiverSourcePriority;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.QuiverUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.QuiverUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.ItemLockProtection;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class QuiverUpgradeMenu extends AbstractContainerMenu {
    public static final int QUIVER_SLOT_X = 8;
    public static final int QUIVER_SLOT_Y = 48;
    public static final int PLAYER_INVENTORY_X = 8;
    public static final int PLAYER_INVENTORY_Y = 178;
    public static final int PLAYER_HOTBAR_Y = 236;
    public static final int INVENTORY_LABEL_Y = 166;

    private final BackpackAccess access;
    private final Player owner;
    private final BackpackUpgradeInventory upgradeInventory;
    private final int upgradeSlotIndex;
    private final QuiverContainer quiverContainer;
    private boolean enabled;
    private QuiverSelectionMode selectionMode;
    private QuiverSourcePriority sourcePriority;
    private int preferredSlot;
    private java.util.List<Integer> prioritySlots;

    public QuiverUpgradeMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, BackpackAccess.fromNetwork(playerInventory.player, buffer), buffer.readVarInt());
    }

    public QuiverUpgradeMenu(int containerId, Inventory playerInventory, BackpackAccess access, int upgradeSlotIndex) {
        super(ModMenuTypes.QUIVER_UPGRADE.get(), containerId);
        this.access = access;
        this.owner = playerInventory.player;
        this.upgradeInventory = new BackpackUpgradeInventory(playerInventory.player, access);
        this.upgradeSlotIndex = upgradeSlotIndex;
        this.quiverContainer = new QuiverContainer();
        this.loadUpgradeData();
        this.addQuiverSlots();
        this.addPlayerInventory(playerInventory);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public QuiverSelectionMode getSelectionMode() {
        return this.selectionMode;
    }

    public QuiverSourcePriority getSourcePriority() {
        return this.sourcePriority;
    }

    public int getPreferredSlot() {
        return this.preferredSlot;
    }

    public int getPreferredPriorityIndex() {
        int index = this.prioritySlots.indexOf(this.preferredSlot);
        return index < 0 ? 0 : index;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.saveUpgradeData();
    }

    public void cycleSelectionMode() {
        this.selectionMode = this.selectionMode.next();
        this.saveUpgradeData();
    }

    public void cycleSourcePriority() {
        this.sourcePriority = this.sourcePriority.next();
        this.saveUpgradeData();
    }

    public void setPreferredSlot(int preferredSlot) {
        QuiverUpgradeData next = this.toData().withPreferredSlot(preferredSlot);
        this.applySettings(next);
        this.saveUpgradeData();
    }

    public void movePreferredPriority(int delta) {
        QuiverUpgradeData next = this.toData().movePreferredPriority(delta);
        this.applySettings(next);
        this.saveUpgradeData();
    }

    public void resetSettings() {
        QuiverUpgradeData next = this.toData().resetSettings();
        this.applySettings(next);
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
        int quiverEnd = QuiverUpgradeData.SLOT_COUNT;
        int playerStart = quiverEnd;

        if (index < quiverEnd) {
            if (!this.moveItemStackTo(sourceStack, playerStart, this.slots.size(), true)
                    && !this.moveItemStackToBackpackStorage(sourceStack)) {
                return ItemStack.EMPTY;
            }
        } else if (QuiverUpgradeHandler.isQuiverProjectile(sourceStack)) {
            if (!this.moveItemStackTo(sourceStack, 0, quiverEnd, false)
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
        return this.upgradeInventory.stillValid(player) && this.getUpgradeStack().getItem() instanceof QuiverUpgradeItem;
    }

    private void addQuiverSlots() {
        for (int slot = 0; slot < QuiverUpgradeData.SLOT_COUNT; slot++) {
            this.addSlot(new QuiverSlot(this.quiverContainer, slot, QUIVER_SLOT_X + slot * 18, QUIVER_SLOT_Y));
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
        QuiverUpgradeData data = this.getUpgradeStack().getOrDefault(ModDataComponents.QUIVER_UPGRADE_DATA.get(), QuiverUpgradeData.DEFAULT);
        this.applySettings(data);
        NonNullList<ItemStack> projectiles = data.loadProjectiles();
        for (int slot = 0; slot < QuiverUpgradeData.SLOT_COUNT; slot++) {
            this.quiverContainer.items.set(slot, projectiles.get(slot));
        }
    }

    private void applySettings(QuiverUpgradeData data) {
        this.enabled = data.enabled();
        this.selectionMode = data.selectionMode();
        this.sourcePriority = data.sourcePriority();
        this.preferredSlot = data.preferredSlot();
        this.prioritySlots = data.prioritySlots();
    }

    private QuiverUpgradeData toData() {
        return new QuiverUpgradeData(
                this.enabled,
                ItemContainerContents.fromItems(this.quiverContainer.items),
                this.selectionMode,
                this.sourcePriority,
                this.preferredSlot,
                this.prioritySlots);
    }

    private void saveUpgradeData() {
        ItemStack upgradeStack = this.getUpgradeStack();
        if (!(upgradeStack.getItem() instanceof QuiverUpgradeItem)) {
            return;
        }

        upgradeStack.set(ModDataComponents.QUIVER_UPGRADE_DATA.get(), this.toData());
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

    private final class QuiverContainer implements Container {
        private final NonNullList<ItemStack> items = NonNullList.withSize(QuiverUpgradeData.SLOT_COUNT, ItemStack.EMPTY);

        @Override
        public int getContainerSize() {
            return QuiverUpgradeData.SLOT_COUNT;
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
            this.items.set(slot, QuiverUpgradeHandler.isQuiverProjectile(stack) ? stack : ItemStack.EMPTY);
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
            QuiverUpgradeMenu.this.saveUpgradeData();
        }

        @Override
        public boolean stillValid(Player player) {
            return QuiverUpgradeMenu.this.stillValid(player);
        }

        @Override
        public void clearContent() {
            for (int slot = 0; slot < this.items.size(); slot++) {
                this.items.set(slot, ItemStack.EMPTY);
            }
            this.setChanged();
        }
    }

    private static final class QuiverSlot extends Slot {
        private QuiverSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return QuiverUpgradeHandler.isQuiverProjectile(stack);
        }
    }
}
