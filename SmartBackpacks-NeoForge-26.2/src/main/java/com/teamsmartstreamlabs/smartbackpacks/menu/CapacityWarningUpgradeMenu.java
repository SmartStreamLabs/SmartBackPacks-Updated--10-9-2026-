package com.teamsmartstreamlabs.smartbackpacks.menu;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackUpgradeInventory;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.CapacityWarningUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.network.CapacityWarningSyncPayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModSounds;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacityWarningCalculationMode;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacityWarningHudMode;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacityWarningSnapshot;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacityWarningState;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacityWarningUpgradeData;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import com.teamsmartstreamlabs.smartbackpacks.util.PlayerMessageHelper;

public class CapacityWarningUpgradeMenu extends AbstractContainerMenu {
    private final BackpackAccess access;
    private final Player owner;
    private final BackpackUpgradeInventory upgradeInventory;
    private final int upgradeSlotIndex;
    private CapacityWarningUpgradeData settings;

    public CapacityWarningUpgradeMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, BackpackAccess.fromNetwork(playerInventory.player, buffer), buffer.readVarInt());
    }

    public CapacityWarningUpgradeMenu(int containerId, Inventory playerInventory, BackpackAccess access, int upgradeSlotIndex) {
        super(ModMenuTypes.CAPACITY_WARNING_UPGRADE.get(), containerId);
        this.access = access;
        this.owner = playerInventory.player;
        this.upgradeInventory = new BackpackUpgradeInventory(playerInventory.player, access);
        this.upgradeSlotIndex = upgradeSlotIndex;
        this.loadUpgradeData();
    }

    public CapacityWarningUpgradeData getSettings() {
        return this.settings;
    }

    public CapacityWarningSnapshot getCurrentSnapshot() {
        ItemStack backpack = this.access.getBackpackStack(this.owner);
        if (!(backpack.getItem() instanceof BackpackItem backpackItem)) {
            return CapacityWarningSnapshot.empty(this.settings.calculationMode());
        }
        return CapacityWarningSnapshot.calculate(backpack, backpackItem.getTier(), this.getAllowedCalculationMode());
    }

    public void setEnabled(boolean enabled) {
        this.settings = this.settings.withEnabled(enabled);
        this.saveUpgradeData();
    }

    public void cycleCalculationMode() {
        CapacityWarningCalculationMode next = this.settings.calculationMode().next();
        if (next == CapacityWarningCalculationMode.STACK_CAPACITY && !SmartBackpacksConfig.capacityWarningAllowStackCapacityMode()) {
            next = CapacityWarningCalculationMode.OCCUPIED_SLOTS;
        }
        this.settings = this.settings.withCalculationMode(next);
        this.saveUpgradeData();
    }

    public void toggleThreshold(int index) {
        this.settings = this.settings.withThresholdEnabled(index, !this.settings.thresholdEnabled(index));
        this.saveUpgradeData();
    }

    public void changeThreshold(int index, int delta) {
        if (index != 0 || delta == 0) {
            return;
        }
        this.settings = this.settings.withThreshold(0, this.settings.thresholdStep() == 10 ? 25 : 10);
        this.saveUpgradeData();
    }

    public void changeResetMargin(int delta) {
        this.settings = this.settings.withResetMargin(this.settings.resetMargin() + delta);
        this.saveUpgradeData();
    }

    public void toggleActionBar() {
        this.settings = this.settings.withActionBar(!this.settings.actionBar());
        this.saveUpgradeData();
    }

    public void toggleSound() {
        this.settings = this.settings.withSound(!this.settings.sound());
        this.saveUpgradeData();
    }

    public void toggleHud() {
        this.settings = this.settings.withHud(!this.settings.hud());
        this.saveUpgradeData();
    }

    public void cycleHudMode() {
        this.settings = this.settings.withHudMode(this.settings.hudMode().next());
        this.saveUpgradeData();
    }

    public void toggleShowPercentage() {
        this.settings = this.settings.withShowPercentage(!this.settings.showPercentage());
        this.saveUpgradeData();
    }

    public void toggleShowSlotCount() {
        this.settings = this.settings.withShowSlotCount(!this.settings.showSlotCount());
        this.saveUpgradeData();
    }

    public void toggleFailedInsertionWarning() {
        this.settings = this.settings.withFailedInsertionWarning(!this.settings.failedInsertionWarning());
        this.saveUpgradeData();
    }

    public void previewFeedback() {
        if (!(this.owner instanceof ServerPlayer serverPlayer)) {
            return;
        }

        CapacityWarningSnapshot snapshot = this.getCurrentSnapshot();
        CapacityWarningState state = snapshot.stateFor(this.settings);
        PlayerMessageHelper.sendStatus(serverPlayer, Component.translatable("message.smartbackpacks.capacity_warning.preview",
                this.access.getBackpackStack(this.owner).getHoverName(), Math.max(snapshot.displayPercentage(), this.settings.threshold1())));
        if (this.settings.hud() && this.settings.sound()) {
            serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    ModSounds.CAPACITY_WARNING.get(), SoundSource.PLAYERS, 0.4F, 1.0F);
        }
        PacketDistributor.sendToPlayer(serverPlayer, new CapacityWarningSyncPayload(
                this.access.getBackpackStack(this.owner).getHoverName().getString(),
                Math.max(snapshot.displayPercentage(), this.settings.thresholdStep()),
                state == CapacityWarningState.NORMAL ? CapacityWarningState.WARNING : state,
                snapshot.occupiedSlots(),
                snapshot.totalSlots(),
                snapshot.freeSlots(),
                this.settings.hud()));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.upgradeInventory.stillValid(player) && this.getUpgradeStack().getItem() instanceof CapacityWarningUpgradeItem;
    }

    private void loadUpgradeData() {
        this.settings = this.getUpgradeStack().getOrDefault(ModDataComponents.CAPACITY_WARNING_UPGRADE_DATA.get(), CapacityWarningUpgradeData.DEFAULT);
        if (this.settings.calculationMode() == CapacityWarningCalculationMode.STACK_CAPACITY
                && !SmartBackpacksConfig.capacityWarningAllowStackCapacityMode()) {
            this.settings = this.settings.withCalculationMode(CapacityWarningCalculationMode.OCCUPIED_SLOTS);
            this.saveUpgradeData();
        }
    }

    private CapacityWarningCalculationMode getAllowedCalculationMode() {
        if (this.settings.calculationMode() == CapacityWarningCalculationMode.STACK_CAPACITY
                && !SmartBackpacksConfig.capacityWarningAllowStackCapacityMode()) {
            return CapacityWarningCalculationMode.OCCUPIED_SLOTS;
        }
        return this.settings.calculationMode();
    }

    private void saveUpgradeData() {
        ItemStack upgradeStack = this.getUpgradeStack();
        if (!(upgradeStack.getItem() instanceof CapacityWarningUpgradeItem)) {
            return;
        }

        upgradeStack.set(ModDataComponents.CAPACITY_WARNING_UPGRADE_DATA.get(), this.settings);
        this.upgradeInventory.setItem(this.upgradeSlotIndex, upgradeStack);
        this.broadcastChanges();
    }

    private ItemStack getUpgradeStack() {
        if (this.upgradeSlotIndex < 0 || this.upgradeSlotIndex >= this.upgradeInventory.getContainerSize()) {
            return ItemStack.EMPTY;
        }
        return this.upgradeInventory.getItem(this.upgradeSlotIndex);
    }
}
