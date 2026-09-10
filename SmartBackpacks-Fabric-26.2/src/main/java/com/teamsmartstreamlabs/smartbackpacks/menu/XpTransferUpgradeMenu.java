package com.teamsmartstreamlabs.smartbackpacks.menu;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackUpgradeInventory;
import com.teamsmartstreamlabs.smartbackpacks.item.XpTransferUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FluidStorageUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.XpTransferUpgradeData;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class XpTransferUpgradeMenu extends AbstractContainerMenu {
    public static final int MB_PER_XP = 20;
    public static final int STEP_XP = 100;

    private final Player owner;
    private final BackpackAccess access;
    private final BackpackUpgradeInventory upgradeInventory;
    private final int upgradeSlotIndex;
    private final ContainerData data = new SimpleContainerData(4);

    public XpTransferUpgradeMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, BackpackAccess.fromNetwork(playerInventory.player, buffer), buffer.readVarInt());
    }

    public XpTransferUpgradeMenu(int containerId, Inventory playerInventory, BackpackAccess access, int upgradeSlotIndex) {
        super(ModMenuTypes.XP_TRANSFER_UPGRADE.get(), containerId);
        this.owner = playerInventory.player;
        this.access = access;
        this.upgradeInventory = new BackpackUpgradeInventory(playerInventory.player, access);
        this.upgradeSlotIndex = upgradeSlotIndex;
        this.addDataSlots(this.data);
        this.addPlayerInventory(playerInventory);
        this.refreshData();
    }

    public boolean hasTankUpgrade() {
        ItemStack backpack = this.access.getBackpackStack(this.owner);
        if (backpack.isEmpty()) {
            return false;
        }
        return FluidStorageUpgradeHandler.findFirstUpgradeSlot(BackpackStackData.loadUpgrades(backpack)) >= 0;
    }

    public int getStoredMillibuckets() {
        return this.data.get(0);
    }

    public int getCapacity() {
        return this.data.get(1);
    }

    public int getStoredXpPoints() {
        return this.data.get(2);
    }

    public int getPlayerXpPoints() {
        return this.data.get(3);
    }

    public void storeStep() {
        this.transferPlayerXpToTank(STEP_XP);
    }

    public void storeAll() {
        this.transferPlayerXpToTank(this.getPlayerXpPoints());
    }

    public void withdrawStep() {
        this.transferTankXpToPlayer(STEP_XP);
    }

    public void withdrawAll() {
        this.transferTankXpToPlayer(this.getStoredXpPoints());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.upgradeInventory.stillValid(player) && this.getUpgradeStack().getItem() instanceof XpTransferUpgradeItem;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        int playerInventoryY = 86;
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

    private void transferPlayerXpToTank(int requestedXp) {
        if (!this.hasTankUpgrade() || requestedXp <= 0) {
            return;
        }

        ItemStack upgradeStack = this.getUpgradeStack();
        if (!(upgradeStack.getItem() instanceof XpTransferUpgradeItem)) {
            return;
        }

        int availableXp = this.getTotalExperience(this.owner);
        int availableCapacityXp = (FluidStorageUpgradeHandler.CAPACITY - this.getStoredMillibuckets(upgradeStack)) / MB_PER_XP;
        int movedXp = Math.min(requestedXp, Math.min(availableXp, availableCapacityXp));
        if (movedXp <= 0) {
            return;
        }

        this.owner.giveExperiencePoints(-movedXp);
        this.saveStoredMillibuckets(upgradeStack, this.getStoredMillibuckets(upgradeStack) + movedXp * MB_PER_XP);
    }

    private void transferTankXpToPlayer(int requestedXp) {
        if (!this.hasTankUpgrade() || requestedXp <= 0) {
            return;
        }

        ItemStack upgradeStack = this.getUpgradeStack();
        if (!(upgradeStack.getItem() instanceof XpTransferUpgradeItem)) {
            return;
        }

        int storedXp = this.getStoredMillibuckets(upgradeStack) / MB_PER_XP;
        int movedXp = Math.min(requestedXp, storedXp);
        if (movedXp <= 0) {
            return;
        }

        this.owner.giveExperiencePoints(movedXp);
        this.saveStoredMillibuckets(upgradeStack, this.getStoredMillibuckets(upgradeStack) - movedXp * MB_PER_XP);
    }

    private ItemStack getUpgradeStack() {
        return this.upgradeInventory.getItem(this.upgradeSlotIndex);
    }

    private int getStoredMillibuckets(ItemStack upgradeStack) {
        return upgradeStack.getOrDefault(ModDataComponents.XP_TRANSFER_UPGRADE_DATA.get(), XpTransferUpgradeData.DEFAULT).storedMillibuckets();
    }

    private void saveStoredMillibuckets(ItemStack upgradeStack, int amount) {
        int clamped = Math.max(0, Math.min(FluidStorageUpgradeHandler.CAPACITY, amount));
        upgradeStack.set(ModDataComponents.XP_TRANSFER_UPGRADE_DATA.get(), new XpTransferUpgradeData(clamped));
        this.upgradeInventory.setItem(this.upgradeSlotIndex, upgradeStack);
        this.owner.getInventory().setChanged();
        this.refreshData();
        this.broadcastChanges();
    }

    private void refreshData() {
        ItemStack upgradeStack = this.getUpgradeStack();
        int storedMillibuckets = this.getStoredMillibuckets(upgradeStack);
        this.data.set(0, storedMillibuckets);
        this.data.set(1, FluidStorageUpgradeHandler.CAPACITY);
        this.data.set(2, storedMillibuckets / MB_PER_XP);
        this.data.set(3, this.getTotalExperience(this.owner));
    }

    private int getTotalExperience(Player player) {
        return Math.max(0, player.totalExperience);
    }
}
