package com.teamsmartstreamlabs.smartbackpacks.menu;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackUpgradeInventory;
import com.teamsmartstreamlabs.smartbackpacks.item.TorchPlacerUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BuilderFilterMode;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BuilderMatchMode;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.TorchPlacerUpgradeData;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TorchPlacerUpgradeMenu extends AbstractContainerMenu {
    public static final int FILTER_SLOT_X = 8;
    public static final int FILTER_SLOT_Y = 58;
    public static final int FILTER_COLUMNS = 9;
    public static final int PLAYER_INVENTORY_X = 39;
    public static final int PLAYER_INVENTORY_Y = 184;
    public static final int PLAYER_HOTBAR_Y = 242;
    public static final int INVENTORY_LABEL_Y = 172;

    private final BackpackUpgradeInventory upgradeInventory;
    private final int upgradeSlotIndex;
    private final SimpleContainer filterItems = new SimpleContainer(TorchPlacerUpgradeData.FILTER_SLOT_COUNT);
    private TorchPlacerUpgradeData settings;

    public TorchPlacerUpgradeMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, BackpackAccess.fromNetwork(playerInventory.player, buffer), buffer.readVarInt());
    }

    public TorchPlacerUpgradeMenu(int containerId, Inventory playerInventory, BackpackAccess access, int upgradeSlotIndex) {
        super(ModMenuTypes.TORCH_PLACER_UPGRADE.get(), containerId);
        this.upgradeInventory = new BackpackUpgradeInventory(playerInventory.player, access);
        this.upgradeSlotIndex = upgradeSlotIndex;
        this.loadUpgradeData();
        this.addFilterSlots();
        this.addPlayerInventory(playerInventory);
    }

    public TorchPlacerUpgradeData getSettings() {
        return this.settings;
    }

    public void setEnabled(boolean enabled) {
        this.settings = this.settings.withEnabled(enabled);
        this.saveUpgradeData();
    }

    public void cyclePlacementMode() {
        this.settings = this.settings.withPlacementMode(this.settings.placementMode().next());
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

    public void toggleFloorPlacement() {
        this.settings = this.settings.withPlacementToggles(!this.settings.floorPlacement(), this.settings.wallPlacement(), this.settings.ceilingPlacement());
        this.saveUpgradeData();
    }

    public void toggleWallPlacement() {
        this.settings = this.settings.withPlacementToggles(this.settings.floorPlacement(), !this.settings.wallPlacement(), this.settings.ceilingPlacement());
        this.saveUpgradeData();
    }

    public void toggleCeilingPlacement() {
        this.settings = this.settings.withPlacementToggles(this.settings.floorPlacement(), this.settings.wallPlacement(), !this.settings.ceilingPlacement());
        this.saveUpgradeData();
    }

    public void toggleModdedLightSources() {
        this.settings = this.settings.withMovementToggles(!this.settings.moddedLightSources(), this.settings.placeWhileSprinting(),
                this.settings.placeWhileSneaking(), this.settings.placeWhileStandingStill());
        this.saveUpgradeData();
    }

    public void toggleSprinting() {
        this.settings = this.settings.withMovementToggles(this.settings.moddedLightSources(), !this.settings.placeWhileSprinting(),
                this.settings.placeWhileSneaking(), this.settings.placeWhileStandingStill());
        this.saveUpgradeData();
    }

    public void toggleSneaking() {
        this.settings = this.settings.withMovementToggles(this.settings.moddedLightSources(), this.settings.placeWhileSprinting(),
                !this.settings.placeWhileSneaking(), this.settings.placeWhileStandingStill());
        this.saveUpgradeData();
    }

    public void toggleStandingStill() {
        this.settings = this.settings.withMovementToggles(this.settings.moddedLightSources(), this.settings.placeWhileSprinting(),
                this.settings.placeWhileSneaking(), !this.settings.placeWhileStandingStill());
        this.saveUpgradeData();
    }

    public void toggleWater() {
        this.settings = this.settings.withFluidToggles(!this.settings.placeInWater(), this.settings.placeInLava());
        this.saveUpgradeData();
    }

    public void toggleLava() {
        this.settings = this.settings.withFluidToggles(this.settings.placeInWater(), !this.settings.placeInLava());
        this.saveUpgradeData();
    }

    public void changeLightThreshold(int delta) {
        this.settings = this.settings.withLightThreshold(this.settings.lightThreshold() + delta);
        this.saveUpgradeData();
    }

    public void changeMinimumDistance(int delta) {
        this.settings = this.settings.withMinimumDistance(this.settings.minimumDistance() + delta);
        this.saveUpgradeData();
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput ContainerInput, Player player) {
        if (slotId >= 0 && slotId < TorchPlacerUpgradeData.FILTER_SLOT_COUNT && ContainerInput == ContainerInput.PICKUP) {
            ItemStack carried = this.getCarried();
            this.filterItems.setItem(slotId, carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1));
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
        return this.upgradeInventory.stillValid(player) && this.getUpgradeStack().getItem() instanceof TorchPlacerUpgradeItem;
    }

    private void addFilterSlots() {
        for (int slot = 0; slot < TorchPlacerUpgradeData.FILTER_SLOT_COUNT; slot++) {
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
        this.settings = this.getUpgradeStack().getOrDefault(ModDataComponents.TORCH_PLACER_UPGRADE_DATA.get(), TorchPlacerUpgradeData.DEFAULT);
        if (this.settings.matchMode() != BuilderMatchMode.EXACT && this.settings.matchMode() != BuilderMatchMode.ITEM) {
            this.settings = this.settings.withMatchMode(BuilderMatchMode.EXACT);
            this.saveUpgradeData();
        }
        if (this.settings.filterMode() == null) {
            this.settings = this.settings.withFilterMode(BuilderFilterMode.ALLOW_ALL);
            this.saveUpgradeData();
        }
        NonNullList<ItemStack> filters = this.settings.loadFilterItems();
        for (int slot = 0; slot < TorchPlacerUpgradeData.FILTER_SLOT_COUNT; slot++) {
            this.filterItems.setItem(slot, filters.get(slot));
        }
    }

    private void saveUpgradeData() {
        ItemStack upgradeStack = this.getUpgradeStack();
        if (!(upgradeStack.getItem() instanceof TorchPlacerUpgradeItem)) {
            return;
        }

        NonNullList<ItemStack> filters = NonNullList.withSize(TorchPlacerUpgradeData.FILTER_SLOT_COUNT, ItemStack.EMPTY);
        for (int slot = 0; slot < TorchPlacerUpgradeData.FILTER_SLOT_COUNT; slot++) {
            filters.set(slot, this.filterItems.getItem(slot));
        }
        upgradeStack.set(ModDataComponents.TORCH_PLACER_UPGRADE_DATA.get(), this.settings.withFilterItems(filters));
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
