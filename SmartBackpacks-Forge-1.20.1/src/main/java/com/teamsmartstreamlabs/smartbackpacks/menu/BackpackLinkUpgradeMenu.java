package com.teamsmartstreamlabs.smartbackpacks.menu;

import java.util.UUID;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackUpgradeInventory;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackLinkUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.network.BackpackLinkMenuSyncPayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BackpackLinkManager;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BackpackLinkSnapshot;

import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class BackpackLinkUpgradeMenu extends AbstractContainerMenu {
    private final BackpackAccess access;
    private final Player owner;
    private final BackpackUpgradeInventory upgradeInventory;
    private final int upgradeSlotIndex;
    private BackpackLinkSnapshot snapshot;

    public BackpackLinkUpgradeMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, BackpackAccess.fromNetwork(playerInventory.player, buffer), buffer.readVarInt(),
                BackpackLinkSnapshot.read(buffer));
    }

    public BackpackLinkUpgradeMenu(int containerId, Inventory playerInventory, BackpackAccess access, int upgradeSlotIndex) {
        this(containerId, playerInventory, access, upgradeSlotIndex,
                playerInventory.player instanceof ServerPlayer serverPlayer
                        ? BackpackLinkManager.createSnapshot(serverPlayer, access)
                        : BackpackLinkSnapshot.EMPTY);
    }

    private BackpackLinkUpgradeMenu(int containerId, Inventory playerInventory, BackpackAccess access, int upgradeSlotIndex,
            BackpackLinkSnapshot snapshot) {
        super(ModMenuTypes.BACKPACK_LINK_UPGRADE.get(), containerId);
        this.access = access;
        this.owner = playerInventory.player;
        this.upgradeInventory = new BackpackUpgradeInventory(playerInventory.player, access);
        this.upgradeSlotIndex = upgradeSlotIndex;
        this.snapshot = snapshot;
    }

    public static void writeInitialData(ServerPlayer player, BackpackAccess access, RegistryFriendlyByteBuf buffer) {
        BackpackLinkSnapshot.write(buffer, BackpackLinkManager.createSnapshot(player, access));
    }

    public BackpackLinkSnapshot getSnapshot() {
        return this.snapshot;
    }

    public void updateSnapshot(BackpackLinkSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public void toggleActive() {
        if (!(this.owner instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (this.snapshot.active()) {
            BackpackLinkManager.deactivate(serverPlayer, this.access);
        } else {
            BackpackLinkManager.activate(serverPlayer, this.access);
        }
        this.refreshAndSync();
    }

    public void rename(String name) {
        if (this.owner instanceof ServerPlayer serverPlayer) {
            BackpackLinkManager.rename(serverPlayer, this.access, name);
            this.refreshAndSync();
        }
    }

    public void cycleVisibility() {
        if (this.owner instanceof ServerPlayer serverPlayer) {
            BackpackLinkManager.cycleVisibility(serverPlayer, this.access);
            this.refreshAndSync();
        }
    }

    public void bindOrCompleteCrystal() {
        if (this.owner instanceof ServerPlayer serverPlayer) {
            BackpackLinkManager.bindOrCompleteCrystal(serverPlayer, this.access);
            this.refreshAndSync();
        }
    }

    public void unlink(UUID destinationId) {
        if (this.owner instanceof ServerPlayer serverPlayer) {
            BackpackLinkManager.unlink(serverPlayer, this.access, destinationId);
            this.refreshAndSync();
        }
    }

    public void teleport(UUID destinationId) {
        if (this.owner instanceof ServerPlayer serverPlayer) {
            BackpackLinkManager.teleport(serverPlayer, this.access, destinationId);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.upgradeInventory.stillValid(player) && this.getUpgradeStack().getItem() instanceof BackpackLinkUpgradeItem;
    }

    private void refreshAndSync() {
        if (!(this.owner instanceof ServerPlayer serverPlayer)) {
            return;
        }
        this.snapshot = BackpackLinkManager.createSnapshot(serverPlayer, this.access);
        PacketDistributor.sendToPlayer(serverPlayer, new BackpackLinkMenuSyncPayload(this.containerId, this.snapshot));
        this.broadcastChanges();
    }

    private ItemStack getUpgradeStack() {
        if (this.upgradeSlotIndex < 0 || this.upgradeSlotIndex >= this.upgradeInventory.getContainerSize()) {
            return ItemStack.EMPTY;
        }
        return this.upgradeInventory.getItem(this.upgradeSlotIndex);
    }
}
