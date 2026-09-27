package com.teamsmartstreamlabs.smartbackpacks.menu;


import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackUpgradeInventory;
import com.teamsmartstreamlabs.smartbackpacks.item.BuilderUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BuilderFilterMode;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BuilderMatchMode;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BuilderRefillMode;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BuilderUpgradeData;

import net.minecraft.core.NonNullList;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BuilderUpgradeMenu extends AbstractContainerMenu {
    public static final int FILTER_SLOT_X = 8;
    public static final int FILTER_SLOT_Y = 64;
    public static final int FILTER_COLUMNS = 6;
    public static final int PLAYER_INVENTORY_X = 39;
    public static final int PLAYER_INVENTORY_Y = 182;
    public static final int PLAYER_HOTBAR_Y = 240;
    public static final int INVENTORY_LABEL_Y = 170;

    private final BackpackUpgradeInventory upgradeInventory;
    private final int upgradeSlotIndex;
    private final SimpleContainer filterItems = new SimpleContainer(BuilderUpgradeData.FILTER_SLOT_COUNT);
    private BuilderUpgradeData settings;

    public BuilderUpgradeMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, BackpackAccess.fromNetwork(playerInventory.player, buffer), buffer.readVarInt());
    }

    public BuilderUpgradeMenu(int containerId, Inventory playerInventory, BackpackAccess access, int upgradeSlotIndex) {
        super(ModMenuTypes.BUILDER_UPGRADE.get(), containerId);
        this.upgradeInventory = new BackpackUpgradeInventory(playerInventory.player, access);
        this.upgradeSlotIndex = upgradeSlotIndex;
        this.loadUpgradeData();
        this.addFilterSlots();
        this.addPlayerInventory(playerInventory);
    }

    public BuilderUpgradeData getSettings() {
        return this.settings;
    }

    public void setEnabled(boolean enabled) {
        this.settings = this.settings.withEnabled(enabled);
        this.saveUpgradeData();
    }

    public void cycleRefillMode() {
        this.settings = this.settings.withRefillMode(this.settings.refillMode().next());
        this.saveUpgradeData();
    }

    public void cycleMatchMode() {
        this.settings = this.settings.withMatchMode(this.settings.matchMode().next());
        this.saveUpgradeData();
    }

    public void cycleFilterMode() {
        this.settings = this.settings.withFilterMode(this.settings.filterMode().next());
        this.saveUpgradeData();
    }

    public void toggleMainHand() {
        this.settings = this.settings.withHandToggles(!this.settings.mainHandEnabled(), this.settings.offhandEnabled());
        this.saveUpgradeData();
    }

    public void toggleOffhand() {
        this.settings = this.settings.withHandToggles(this.settings.mainHandEnabled(), !this.settings.offhandEnabled());
        this.saveUpgradeData();
    }

    public void toggleScaffolding() {
        this.settings = this.settings.withOptionToggles(!this.settings.scaffoldingEnabled(), this.settings.moddedBlocksEnabled(),
                this.settings.dangerousProtectionEnabled(), this.settings.feedbackEnabled());
        this.saveUpgradeData();
    }

    public void toggleModdedBlocks() {
        this.settings = this.settings.withOptionToggles(this.settings.scaffoldingEnabled(), !this.settings.moddedBlocksEnabled(),
                this.settings.dangerousProtectionEnabled(), this.settings.feedbackEnabled());
        this.saveUpgradeData();
    }

    public void toggleDangerousProtection() {
        this.settings = this.settings.withOptionToggles(this.settings.scaffoldingEnabled(), this.settings.moddedBlocksEnabled(),
                !this.settings.dangerousProtectionEnabled(), this.settings.feedbackEnabled());
        this.saveUpgradeData();
    }

    public void toggleFeedback() {
        this.settings = this.settings.withOptionToggles(this.settings.scaffoldingEnabled(), this.settings.moddedBlocksEnabled(),
                this.settings.dangerousProtectionEnabled(), !this.settings.feedbackEnabled());
        this.saveUpgradeData();
    }

    public void changeThreshold(int delta) {
        this.settings = this.settings.withThreshold(this.settings.threshold() + delta);
        this.saveUpgradeData();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < BuilderUpgradeData.FILTER_SLOT_COUNT && clickType == ClickType.PICKUP) {
            ItemStack carried = this.getCarried();
            this.filterItems.setItem(slotId, carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1));
            this.saveUpgradeData();
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
        return this.upgradeInventory.stillValid(player) && this.getUpgradeStack().getItem() instanceof BuilderUpgradeItem;
    }

    private void addFilterSlots() {
        for (int slot = 0; slot < BuilderUpgradeData.FILTER_SLOT_COUNT; slot++) {
            int x = FILTER_SLOT_X + (slot % FILTER_COLUMNS) * 18;
            int y = FILTER_SLOT_Y + (slot / FILTER_COLUMNS) * 18;
            this.addSlot(new GhostFilterSlot(this.filterItems, slot, x, y));
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
        this.settings = ItemStackCompat.getOrDefault(this.getUpgradeStack(), ModDataComponents.BUILDER_UPGRADE_DATA.get(), BuilderUpgradeData.DEFAULT);
        if (this.settings.refillMode() == BuilderRefillMode.MANUAL) {
            this.settings = this.settings.withRefillMode(BuilderRefillMode.WHEN_EMPTY);
            this.saveUpgradeData();
        }
        NonNullList<ItemStack> filters = this.settings.loadFilterItems();
        for (int slot = 0; slot < BuilderUpgradeData.FILTER_SLOT_COUNT; slot++) {
            this.filterItems.setItem(slot, filters.get(slot));
        }
    }

    private void saveUpgradeData() {
        ItemStack upgradeStack = this.getUpgradeStack();
        if (!(upgradeStack.getItem() instanceof BuilderUpgradeItem)) {
            return;
        }

        NonNullList<ItemStack> filters = NonNullList.withSize(BuilderUpgradeData.FILTER_SLOT_COUNT, ItemStack.EMPTY);
        for (int slot = 0; slot < BuilderUpgradeData.FILTER_SLOT_COUNT; slot++) {
            filters.set(slot, this.filterItems.getItem(slot));
        }
        ItemStackCompat.set(upgradeStack, ModDataComponents.BUILDER_UPGRADE_DATA.get(), this.settings.withFilterItems(filters));
        this.upgradeInventory.setItem(this.upgradeSlotIndex, upgradeStack);
        this.broadcastChanges();
    }

    private ItemStack getUpgradeStack() {
        if (this.upgradeSlotIndex < 0 || this.upgradeSlotIndex >= this.upgradeInventory.getContainerSize()) {
            return ItemStack.EMPTY;
        }
        return this.upgradeInventory.getItem(this.upgradeSlotIndex);
    }

    private static final class GhostFilterSlot extends Slot {
        private GhostFilterSlot(SimpleContainer container, int slot, int x, int y) {
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
