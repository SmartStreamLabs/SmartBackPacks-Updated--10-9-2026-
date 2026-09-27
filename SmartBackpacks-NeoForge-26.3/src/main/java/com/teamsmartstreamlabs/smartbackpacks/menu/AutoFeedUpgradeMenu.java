package com.teamsmartstreamlabs.smartbackpacks.menu;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackUpgradeInventory;
import com.teamsmartstreamlabs.smartbackpacks.item.AutoFeedUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.AutoFeedUpgradeData;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.util.Mth;

public class AutoFeedUpgradeMenu extends AbstractContainerMenu {
    private final BackpackUpgradeInventory upgradeInventory;
    private final int upgradeSlotIndex;
    private final SimpleContainer filterItems = new SimpleContainer(AutoFeedUpgradeData.FILTER_SLOT_COUNT);
    private boolean enabled;
    private int hungerThreshold;
    private int saturationThreshold;

    public AutoFeedUpgradeMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, BackpackAccess.fromNetwork(playerInventory.player, buffer), buffer.readVarInt());
    }

    public AutoFeedUpgradeMenu(int containerId, Inventory playerInventory, BackpackAccess access, int upgradeSlotIndex) {
        super(ModMenuTypes.AUTO_FEED_UPGRADE.get(), containerId);
        this.upgradeInventory = new BackpackUpgradeInventory(playerInventory.player, access);
        this.upgradeSlotIndex = upgradeSlotIndex;
        this.loadUpgradeData();
        this.addFilterSlots();
        this.addPlayerInventory(playerInventory);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public int getHungerThreshold() {
        return this.hungerThreshold;
    }

    public int getSaturationThreshold() {
        return this.saturationThreshold;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.saveUpgradeData();
    }

    public void setHungerThreshold(int hungerThreshold) {
        this.hungerThreshold = Mth.clamp(hungerThreshold, 0, 20);
        this.saveUpgradeData();
    }

    public void setSaturationThreshold(int saturationThreshold) {
        this.saturationThreshold = Mth.clamp(saturationThreshold, 0, 20);
        this.saveUpgradeData();
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput ContainerInput, Player player) {
        if (slotId >= 0 && slotId < AutoFeedUpgradeData.FILTER_SLOT_COUNT && ContainerInput == ContainerInput.PICKUP) {
            ItemStack carried = this.getCarried();
            if (carried.isEmpty()) {
                this.filterItems.setItem(slotId, ItemStack.EMPTY);
            } else {
                this.filterItems.setItem(slotId, carried.copyWithCount(1));
            }
            this.saveUpgradeData();
            return;
        }

        super.clicked(slotId, button, ContainerInput, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.upgradeInventory.stillValid(player) && this.getUpgradeStack().getItem() instanceof AutoFeedUpgradeItem;
    }

    private void addFilterSlots() {
        for (int slot = 0; slot < AutoFeedUpgradeData.FILTER_SLOT_COUNT; slot++) {
            int x = 76 + (slot % 4) * 18;
            int y = 22 + (slot / 4) * 18;
            this.addSlot(new GhostFilterSlot(this.filterItems, slot, x, y));
        }
    }

    private void addPlayerInventory(Inventory playerInventory) {
        int playerInventoryY = 128;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int index = column + row * 9 + 9;
                this.addSlot(new Slot(playerInventory, index, 8 + column * 18, playerInventoryY + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column, 8 + column * 18, playerInventoryY + 58));
        }
    }

    private void loadUpgradeData() {
        AutoFeedUpgradeData data = this.getUpgradeStack().getOrDefault(ModDataComponents.AUTO_FEED_UPGRADE_DATA.get(), AutoFeedUpgradeData.DEFAULT);
        NonNullList<ItemStack> filterStacks = NonNullList.withSize(AutoFeedUpgradeData.FILTER_SLOT_COUNT, ItemStack.EMPTY);
        data.itemFilters().copyInto(filterStacks);
        for (int slot = 0; slot < AutoFeedUpgradeData.FILTER_SLOT_COUNT; slot++) {
            this.filterItems.setItem(slot, filterStacks.get(slot));
        }
        this.enabled = data.enabled();
        this.hungerThreshold = data.hungerThreshold();
        this.saturationThreshold = data.saturationThreshold();
    }

    private void saveUpgradeData() {
        ItemStack upgradeStack = this.getUpgradeStack();
        if (!(upgradeStack.getItem() instanceof AutoFeedUpgradeItem)) {
            return;
        }

        NonNullList<ItemStack> filterStacks = NonNullList.withSize(AutoFeedUpgradeData.FILTER_SLOT_COUNT, ItemStack.EMPTY);
        for (int slot = 0; slot < AutoFeedUpgradeData.FILTER_SLOT_COUNT; slot++) {
            filterStacks.set(slot, this.filterItems.getItem(slot));
        }

        upgradeStack.set(ModDataComponents.AUTO_FEED_UPGRADE_DATA.get(),
                new AutoFeedUpgradeData(
                        this.enabled,
                        ItemContainerContents.fromItems(filterStacks),
                        this.hungerThreshold,
                        this.saturationThreshold));
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

