package com.teamsmartstreamlabs.smartbackpacks.menu;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackUpgradeInventory;
import com.teamsmartstreamlabs.smartbackpacks.network.PlayPortableJukeboxPayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.JukeboxUpgradeData;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.network.PacketDistributor;

public class JukeboxUpgradeMenu extends AbstractContainerMenu {
    private static final int JUKEBOX_START_EVENT = 1010;
    private static final int JUKEBOX_STOP_EVENT = 1011;

    private final Player owner;
    private final BackpackAccess access;
    private final BackpackUpgradeInventory upgradeInventory;
    private final int upgradeSlotIndex;
    private final SimpleContainer discContainer = new SimpleContainer(1);
    private final ContainerData data = new SimpleContainerData(1);
    private boolean playing;

    public JukeboxUpgradeMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, BackpackAccess.fromNetwork(playerInventory.player, buffer), buffer.readVarInt());
    }

    public JukeboxUpgradeMenu(int containerId, Inventory playerInventory, BackpackAccess access, int upgradeSlotIndex) {
        super(ModMenuTypes.JUKEBOX_UPGRADE.get(), containerId);
        this.owner = playerInventory.player;
        this.access = access;
        this.upgradeInventory = new BackpackUpgradeInventory(playerInventory.player, access);
        this.upgradeSlotIndex = upgradeSlotIndex;

        this.loadUpgradeData();
        this.data.set(0, this.playing ? 1 : 0);
        this.addDataSlots(this.data);
        this.addSlot(new JukeboxDiscSlot(this.discContainer, 0, 19, 41));
        this.addPlayerInventory(playerInventory);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.upgradeInventory.stillValid(player);
    }

    @Override
    public void slotsChanged(net.minecraft.world.Container inventory) {
        super.slotsChanged(inventory);
        this.saveUpgradeData(true);
    }

    @Override
    public void removed(Player player) {
        this.saveUpgradeData(false);
        super.removed(player);
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput ContainerInput, Player player) {
        super.clicked(slotId, button, ContainerInput, player);
        this.saveUpgradeData(true);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = this.slots.get(index);
        if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack sourceCopy = sourceStack.copy();

        if (index == 0) {
            if (!this.moveItemStackTo(sourceStack, 1, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (this.isJukeboxDisc(sourceStack)) {
            if (!this.moveItemStackTo(sourceStack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        this.saveUpgradeData(true);
        return sourceCopy;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        int playerInventoryY = 90;
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
        JukeboxUpgradeData data = this.getUpgradeStack().getOrDefault(ModDataComponents.JUKEBOX_UPGRADE_DATA.get(), JukeboxUpgradeData.DEFAULT);
        NonNullList<ItemStack> items = NonNullList.withSize(JukeboxUpgradeData.SLOT_COUNT, ItemStack.EMPTY);
        data.items().copyInto(items);
        this.discContainer.setItem(0, items.get(0));
        this.playing = data.playing();
    }

    private void saveUpgradeData(boolean allowStartPlayback) {
        ItemStack backpack = this.access.getBackpackStack(this.owner);
        if (backpack.isEmpty()) {
            return;
        }

        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        if (this.upgradeSlotIndex < 0 || this.upgradeSlotIndex >= upgrades.size()) {
            return;
        }

        ItemStack upgrade = upgrades.get(this.upgradeSlotIndex).copy();
        if (upgrade.isEmpty()) {
            return;
        }

        ItemStack disc = this.discContainer.getItem(0).copy();
        boolean shouldPlay = !disc.isEmpty() && this.playing;
        if (disc.isEmpty() && this.playing) {
            this.sendPortablePlayback(ItemStack.EMPTY, false);
        }

        upgrade.set(ModDataComponents.JUKEBOX_UPGRADE_DATA.get(),
                new JukeboxUpgradeData(ItemContainerContents.fromItems(NonNullList.of(ItemStack.EMPTY, disc)), shouldPlay));
        upgrades.set(this.upgradeSlotIndex, upgrade);
        BackpackStackData.saveUpgrades(backpack, upgrades);
        this.access.setBackpackStack(this.owner, backpack);
        this.owner.getInventory().setChanged();
        this.playing = shouldPlay;
        this.data.set(0, shouldPlay ? 1 : 0);
    }

    public boolean hasDisc() {
        return !this.discContainer.getItem(0).isEmpty();
    }

    public boolean isPlaying() {
        return this.data.get(0) != 0;
    }

    public void stopPlayback() {
        if (!this.hasDisc()) {
            return;
        }

        this.sendPortablePlayback(ItemStack.EMPTY, false);
        this.updatePlaying(false);
    }

    public void startPlayback() {
        ItemStack disc = this.discContainer.getItem(0);
        if (disc.isEmpty()) {
            return;
        }

        this.sendPortablePlayback(disc, true);
        this.updatePlaying(true);
    }

    private void sendPortablePlayback(ItemStack disc, boolean play) {
        if (this.owner instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new PlayPortableJukeboxPayload(disc.copy(), play));
        }
    }

    private void updatePlaying(boolean playing) {
        this.playing = playing;
        this.data.set(0, playing ? 1 : 0);

        ItemStack backpack = this.access.getBackpackStack(this.owner);
        if (backpack.isEmpty()) {
            return;
        }

        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        if (this.upgradeSlotIndex < 0 || this.upgradeSlotIndex >= upgrades.size()) {
            return;
        }

        ItemStack upgrade = upgrades.get(this.upgradeSlotIndex).copy();
        if (upgrade.isEmpty()) {
            return;
        }

        ItemStack disc = this.discContainer.getItem(0).copy();
        upgrade.set(ModDataComponents.JUKEBOX_UPGRADE_DATA.get(),
                new JukeboxUpgradeData(ItemContainerContents.fromItems(NonNullList.of(ItemStack.EMPTY, disc)), playing));
        upgrades.set(this.upgradeSlotIndex, upgrade);
        BackpackStackData.saveUpgrades(backpack, upgrades);
        this.access.setBackpackStack(this.owner, backpack);
        this.owner.getInventory().setChanged();
        this.broadcastChanges();
    }

    private ItemStack getUpgradeStack() {
        return this.upgradeInventory.getItem(this.upgradeSlotIndex);
    }

    private boolean isJukeboxDisc(ItemStack stack) {
        return stack.has(DataComponents.JUKEBOX_PLAYABLE);
    }

    private final class JukeboxDiscSlot extends Slot {
        private JukeboxDiscSlot(SimpleContainer container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isJukeboxDisc(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}

