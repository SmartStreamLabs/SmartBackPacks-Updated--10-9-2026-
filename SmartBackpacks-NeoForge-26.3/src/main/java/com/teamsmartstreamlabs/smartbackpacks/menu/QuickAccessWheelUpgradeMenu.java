package com.teamsmartstreamlabs.smartbackpacks.menu;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackUpgradeInventory;
import com.teamsmartstreamlabs.smartbackpacks.item.QuickAccessWheelUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.QuickAccessWheelUpgradeData;

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

public class QuickAccessWheelUpgradeMenu extends AbstractContainerMenu {
    public static final int[][] FAVORITE_POSITIONS = {
            {79, 18}, {109, 28}, {119, 50}, {109, 72},
            {79, 82}, {49, 72}, {39, 50}, {49, 28}
    };
    public static final int PLAYER_INVENTORY_Y = 108;

    private final BackpackUpgradeInventory upgradeInventory;
    private final int upgradeSlotIndex;
    private final SimpleContainer favorites = new SimpleContainer(QuickAccessWheelUpgradeData.FAVORITE_COUNT);

    public QuickAccessWheelUpgradeMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, BackpackAccess.fromNetwork(playerInventory.player, buffer), buffer.readVarInt());
    }

    public QuickAccessWheelUpgradeMenu(int containerId, Inventory playerInventory, BackpackAccess access, int upgradeSlotIndex) {
        super(ModMenuTypes.QUICK_ACCESS_WHEEL_UPGRADE.get(), containerId);
        this.upgradeInventory = new BackpackUpgradeInventory(playerInventory.player, access);
        this.upgradeSlotIndex = upgradeSlotIndex;
        this.loadFavorites();
        this.addFavoriteSlots();
        this.addPlayerInventory(playerInventory);
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput input, Player player) {
        if (slotId >= 0 && slotId < QuickAccessWheelUpgradeData.FAVORITE_COUNT && input == ContainerInput.PICKUP) {
            ItemStack carried = this.getCarried();
            this.favorites.setItem(slotId, button == 1 || carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1));
            this.saveFavorites();
            return;
        }
        super.clicked(slotId, button, input, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.upgradeInventory.stillValid(player)
                && this.getUpgradeStack().getItem() instanceof QuickAccessWheelUpgradeItem;
    }

    private void addFavoriteSlots() {
        for (int slot = 0; slot < QuickAccessWheelUpgradeData.FAVORITE_COUNT; slot++) {
            this.addSlot(new GhostFavoriteSlot(this.favorites, slot, FAVORITE_POSITIONS[slot][0], FAVORITE_POSITIONS[slot][1]));
        }
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, PLAYER_INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column, 8 + column * 18, PLAYER_INVENTORY_Y + 58));
        }
    }

    private void loadFavorites() {
        QuickAccessWheelUpgradeData data = this.getUpgradeStack().getOrDefault(
                ModDataComponents.QUICK_ACCESS_WHEEL_UPGRADE_DATA.get(), QuickAccessWheelUpgradeData.DEFAULT);
        NonNullList<ItemStack> stacks = data.loadFavorites();
        for (int slot = 0; slot < stacks.size(); slot++) {
            this.favorites.setItem(slot, stacks.get(slot));
        }
    }

    private void saveFavorites() {
        ItemStack upgrade = this.getUpgradeStack();
        if (!(upgrade.getItem() instanceof QuickAccessWheelUpgradeItem)) {
            return;
        }
        NonNullList<ItemStack> stacks = NonNullList.withSize(QuickAccessWheelUpgradeData.FAVORITE_COUNT, ItemStack.EMPTY);
        for (int slot = 0; slot < stacks.size(); slot++) {
            stacks.set(slot, this.favorites.getItem(slot));
        }
        upgrade.set(ModDataComponents.QUICK_ACCESS_WHEEL_UPGRADE_DATA.get(),
                new QuickAccessWheelUpgradeData(ItemContainerContents.fromItems(stacks)));
        this.upgradeInventory.setItem(this.upgradeSlotIndex, upgrade);
        this.broadcastChanges();
    }

    private ItemStack getUpgradeStack() {
        return this.upgradeSlotIndex >= 0 && this.upgradeSlotIndex < this.upgradeInventory.getContainerSize()
                ? this.upgradeInventory.getItem(this.upgradeSlotIndex) : ItemStack.EMPTY;
    }

    private static final class GhostFavoriteSlot extends Slot {
        private GhostFavoriteSlot(SimpleContainer container, int slot, int x, int y) {
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
