package com.teamsmartstreamlabs.smartbackpacks.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.OpenBackpackTracker;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.blockentity.PlacedBackpackBlockEntity;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackInventory;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackSortMode;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackUpgradeInventory;
import com.teamsmartstreamlabs.smartbackpacks.item.AutoToolUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackLinkUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BuilderUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.CapacityWarningUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.FilterUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.HopperUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.ItemLockUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.MagnetUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.NightVisionUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.FlightUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.PickupUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.QuiverUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.RescueUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.StorageUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.TorchPlacerUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.TrashCanUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.network.ToggleItemLockPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.BackpackVisibleSlotsSyncPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.ModPayloads;
import com.teamsmartstreamlabs.smartbackpacks.pickup.PickupNotifierDestination;
import com.teamsmartstreamlabs.smartbackpacks.pickup.PickupNotifierServer;
import com.teamsmartstreamlabs.smartbackpacks.pickup.PickupNotifierSource;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.TrashCanUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.TrashCanUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.HopperUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.ItemLockData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.ItemLockProtection;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.MagnetUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.PickupUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FilterUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.AutoToolUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BuilderUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BackpackLinkData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.QuiverUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.QuiverUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.RescueUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.TorchPlacerUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacityWarningCalculationMode;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacityWarningSnapshot;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacityWarningUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacityWarningUpgradeHandler;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import com.teamsmartstreamlabs.smartbackpacks.util.PlayerMessageHelper;

public class BackpackMenu extends AbstractContainerMenu {
    private static final int MAX_VISIBLE_BACKPACK_ROWS = BackpackScreenLayout.MAX_VISIBLE_BACKPACK_ROWS;
    private static final int UPGRADE_SLOT_COUNT = BackpackUpgradeInventory.UPGRADE_SLOT_COUNT;

    private final BackpackAccess access;
    private final ItemStack openedHookBackpack;
    private final Player owner;
    private final BackpackInventory backpackInventory;
    private final BackpackUpgradeInventory backpackUpgradeInventory;
    private final BackpackTier tier;
    private final int lockedInventorySlot;
    private final int visibleBackpackRows;
    private final int visibleBackpackSlotCount;
    private BackpackSortMode sortMode = BackpackSortMode.MOD;
    private int firstVisibleRow;
    private int trashSlotIndex = -1;
    private boolean initialSlotSyncPending = true;

    public BackpackMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, BackpackAccess.fromNetwork(playerInventory.player, buffer));
    }

    public BackpackMenu(int containerId, Inventory playerInventory, BackpackAccess access) {
        super(ModMenuTypes.BACKPACK.get(), containerId);
        this.access = access;
        this.openedHookBackpack = access.source() == BackpackAccess.Source.DISPLAY_HOOK
                ? access.getBackpackStack(playerInventory.player) : ItemStack.EMPTY;
        this.owner = playerInventory.player;
        this.tier = access.tier();
        this.lockedInventorySlot = access.getLockedInventorySlot();
        this.visibleBackpackRows = Math.min(this.tier.getRowCount(), MAX_VISIBLE_BACKPACK_ROWS);
        this.visibleBackpackSlotCount = this.visibleBackpackRows * 9;
        this.backpackInventory = new BackpackInventory(playerInventory.player, access, access.tier());
        this.backpackUpgradeInventory = new BackpackUpgradeInventory(playerInventory.player, access);

        this.addBackpackSlots();
        this.addUpgradeSlots();
        this.addPlayerInventory(playerInventory);
        this.addTrashSlot();

        if (!playerInventory.player.level().isClientSide()) {
            OpenBackpackTracker.markOpen(playerInventory.player, access);
            if (playerInventory.player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
                com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.onOpened(serverPlayer, this.backpackInventory.getBackpackStack());
        }
        this.getPlacedBackpackBlockEntity(playerInventory.player).ifPresent(PlacedBackpackBlockEntity::startOpen);
    }

    private void addBackpackSlots() {
        for (int slot = 0; slot < this.visibleBackpackSlotCount; slot++) {
            int visibleRow = slot / 9;
            int column = slot % 9;
            this.addSlot(new BackpackSlot(this.backpackInventory, slot, visibleRow, column));
        }
    }

    private void addPlayerInventory(Inventory playerInventory) {
        int playerInventoryY = BackpackScreenLayout.playerInventoryY(this.visibleBackpackRows);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int index = column + row * 9 + 9;
                this.addSlot(new PlayerInventorySlot(playerInventory, index,
                        BackpackScreenLayout.PLAYER_INVENTORY_X + column * BackpackScreenLayout.SLOT_SIZE,
                        playerInventoryY + row * BackpackScreenLayout.SLOT_SIZE,
                        this.lockedInventorySlot));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new PlayerInventorySlot(playerInventory, column,
                    BackpackScreenLayout.PLAYER_INVENTORY_X + column * BackpackScreenLayout.SLOT_SIZE,
                    playerInventoryY + BackpackScreenLayout.PLAYER_HOTBAR_OFFSET_Y,
                    this.lockedInventorySlot));
        }
    }

    public BackpackTier getTier() {
        return this.tier;
    }

    public BackpackAccess getAccess() {
        return this.access;
    }

    public BackpackSortMode getSortMode() {
        return this.sortMode;
    }

    public int getVisibleBackpackRows() {
        return this.visibleBackpackRows;
    }

    public int getPageCount() {
        return Math.max(1, this.getMaxScrollRowOffset() + 1);
    }

    public int getCurrentPage() {
        return this.firstVisibleRow;
    }

    public int getRowsOnCurrentPage() {
        return this.visibleBackpackRows;
    }

    public int getColumnsInLastRowOnCurrentPage() {
        return 9;
    }

    public void scrollPage(int delta) {
        this.scrollRows(delta);
    }

    public int getTotalBackpackRows() {
        return this.tier.getRowCount();
    }

    public int getFirstVisibleRow() {
        return this.firstVisibleRow;
    }

    public int getMaxScrollRowOffset() {
        return Math.max(0, this.tier.getRowCount() - this.visibleBackpackRows);
    }

    public boolean canScroll() {
        return this.getMaxScrollRowOffset() > 0;
    }

    public void scrollRows(int delta) {
        this.setFirstVisibleRow(this.firstVisibleRow + delta);
    }

    public void setFirstVisibleRow(int rowOffset) {
        int clamped = Mth.clamp(rowOffset, 0, this.getMaxScrollRowOffset());
        if (clamped == this.firstVisibleRow) {
            return;
        }

        this.firstVisibleRow = clamped;
        if (!this.owner.level().isClientSide()) {
            this.syncVisibleBackpackSlotsToClient();
        }
    }

    public void applyVisibleSlotSync(int rowOffset, List<ItemStack> stacks) {
        this.firstVisibleRow = Mth.clamp(rowOffset, 0, this.getMaxScrollRowOffset());
        int count = Math.min(stacks.size(), this.visibleBackpackSlotCount);
        for (int menuSlot = 0; menuSlot < count; menuSlot++) {
            int logicalSlot = this.getLogicalBackpackSlot(menuSlot);
            if (logicalSlot >= 0) {
                this.backpackInventory.setItemFromNetwork(logicalSlot, stacks.get(menuSlot).copy());
            }
        }
    }

    private void syncVisibleBackpackSlotsToClient() {
        if (!(this.owner instanceof ServerPlayer serverPlayer)) {
            return;
        }

        List<ItemStack> visibleStacks = new ArrayList<>(this.visibleBackpackSlotCount);
        for (int menuSlot = 0; menuSlot < this.visibleBackpackSlotCount; menuSlot++) {
            int logicalSlot = this.getLogicalBackpackSlot(menuSlot);
            visibleStacks.add(logicalSlot >= 0 ? this.backpackInventory.getItem(logicalSlot).copy() : ItemStack.EMPTY);
        }

        ModPayloads.sendToPlayer(serverPlayer, new BackpackVisibleSlotsSyncPayload(this.containerId, this.firstVisibleRow, visibleStacks));
        for (int menuSlot = 0; menuSlot < visibleStacks.size(); menuSlot++) {
            this.setRemoteSlot(menuSlot, visibleStacks.get(menuSlot));
        }
    }

    public float getScrollProgress() {
        int maxOffset = this.getMaxScrollRowOffset();
        if (maxOffset <= 0) {
            return 0.0F;
        }
        return this.firstVisibleRow / (float) maxOffset;
    }

    public int getBackpackSlotCount() {
        return this.visibleBackpackSlotCount;
    }

    public int getUpgradeSlotCount() {
        return UPGRADE_SLOT_COUNT;
    }

    public ItemStack getUpgradeStack(int upgradeSlot) {
        return upgradeSlot >= 0 && upgradeSlot < UPGRADE_SLOT_COUNT ? this.backpackUpgradeInventory.getItem(upgradeSlot) : ItemStack.EMPTY;
    }

    public boolean isUpgradeEnabled(int upgradeSlot) {
        ItemStack stack = this.getUpgradeStack(upgradeSlot);
        if (stack.getItem() instanceof FlightUpgradeItem) {
            return stack.getOrDefault(ModDataComponents.FLIGHT_UPGRADE_ENABLED.get(), true);
        }
        if (stack.getItem() instanceof NightVisionUpgradeItem) {
            return stack.getOrDefault(ModDataComponents.NIGHT_VISION_UPGRADE_ENABLED.get(), true);
        }
        if (stack.getItem() instanceof MagnetUpgradeItem) {
            return stack.getOrDefault(ModDataComponents.MAGNET_UPGRADE_DATA.get(), MagnetUpgradeData.DEFAULT).enabled();
        }
        if (stack.getItem() instanceof PickupUpgradeItem) {
            return stack.getOrDefault(ModDataComponents.PICKUP_UPGRADE_DATA.get(), PickupUpgradeData.DEFAULT).enabled();
        }
        if (stack.getItem() instanceof HopperUpgradeItem) {
            return stack.getOrDefault(ModDataComponents.HOPPER_UPGRADE_DATA.get(), HopperUpgradeData.DEFAULT).enabled();
        }
        if (stack.getItem() instanceof FilterUpgradeItem) {
            return stack.getOrDefault(ModDataComponents.FILTER_UPGRADE_DATA.get(), FilterUpgradeData.DEFAULT).enabled();
        }
        if (stack.getItem() instanceof AutoToolUpgradeItem) {
            return stack.getOrDefault(ModDataComponents.AUTO_TOOL_UPGRADE_DATA.get(), AutoToolUpgradeData.DEFAULT).enabled();
        }
        if (stack.getItem() instanceof QuiverUpgradeItem) {
            return stack.getOrDefault(ModDataComponents.QUIVER_UPGRADE_DATA.get(), QuiverUpgradeData.DEFAULT).enabled();
        }
        if (stack.getItem() instanceof TrashCanUpgradeItem) {
            return stack.getOrDefault(ModDataComponents.TRASH_CAN_UPGRADE_DATA.get(), TrashCanUpgradeData.DEFAULT).enabled();
        }
        if (stack.getItem() instanceof RescueUpgradeItem) {
            return stack.getOrDefault(ModDataComponents.RESCUE_UPGRADE_DATA.get(), RescueUpgradeData.DEFAULT).enabled();
        }
        if (stack.getItem() instanceof BuilderUpgradeItem) {
            return stack.getOrDefault(ModDataComponents.BUILDER_UPGRADE_DATA.get(), BuilderUpgradeData.DEFAULT).enabled();
        }
        if (stack.getItem() instanceof TorchPlacerUpgradeItem) {
            return stack.getOrDefault(ModDataComponents.TORCH_PLACER_UPGRADE_DATA.get(), TorchPlacerUpgradeData.DEFAULT).enabled();
        }
        if (stack.getItem() instanceof CapacityWarningUpgradeItem) {
            return stack.getOrDefault(ModDataComponents.CAPACITY_WARNING_UPGRADE_DATA.get(), CapacityWarningUpgradeData.DEFAULT).enabled();
        }
        if (stack.getItem() instanceof BackpackLinkUpgradeItem) {
            return this.backpackInventory.getBackpackStack()
                    .getOrDefault(ModDataComponents.BACKPACK_LINK_DATA.get(), BackpackLinkData.EMPTY)
                    .active();
        }
        return true;
    }

    public void toggleUpgradeEnabled(int upgradeSlot) {
        ItemStack stack = this.getUpgradeStack(upgradeSlot);
        if (stack.getItem() instanceof FlightUpgradeItem) {
            boolean enabled = stack.getOrDefault(ModDataComponents.FLIGHT_UPGRADE_ENABLED.get(), true);
            stack.set(ModDataComponents.FLIGHT_UPGRADE_ENABLED.get(), !enabled);
            this.backpackUpgradeInventory.setItem(upgradeSlot, stack);
            this.broadcastChanges();
            return;
        }
        if (stack.getItem() instanceof NightVisionUpgradeItem) {
            boolean enabled = stack.getOrDefault(ModDataComponents.NIGHT_VISION_UPGRADE_ENABLED.get(), true);
            stack.set(ModDataComponents.NIGHT_VISION_UPGRADE_ENABLED.get(), !enabled);
            this.backpackUpgradeInventory.setItem(upgradeSlot, stack);
            this.broadcastChanges();
            return;
        }
        if (stack.getItem() instanceof MagnetUpgradeItem) {
            MagnetUpgradeData data = stack.getOrDefault(ModDataComponents.MAGNET_UPGRADE_DATA.get(), MagnetUpgradeData.DEFAULT);
            stack.set(ModDataComponents.MAGNET_UPGRADE_DATA.get(),
                    new MagnetUpgradeData(!data.enabled(), data.allowlist(), data.itemFilters(), data.modFilters(), data.tagFilters(),
                            data.selectedInputType(), data.matchNbt(), data.matchDamage(), data.matchBackpackContentsOnly(), data.blockModdedItems()));
            this.backpackUpgradeInventory.setItem(upgradeSlot, stack);
            this.broadcastChanges();
            return;
        }

        if (stack.getItem() instanceof PickupUpgradeItem) {
            PickupUpgradeData data = stack.getOrDefault(ModDataComponents.PICKUP_UPGRADE_DATA.get(), PickupUpgradeData.DEFAULT);
            stack.set(ModDataComponents.PICKUP_UPGRADE_DATA.get(),
                    new PickupUpgradeData(!data.enabled(), data.allowlist(), data.itemFilters(), data.modFilters(), data.tagFilters(), data.blockModdedItems()));
            this.backpackUpgradeInventory.setItem(upgradeSlot, stack);
            this.broadcastChanges();
            return;
        }

        if (stack.getItem() instanceof HopperUpgradeItem) {
            HopperUpgradeData data = stack.getOrDefault(ModDataComponents.HOPPER_UPGRADE_DATA.get(), HopperUpgradeData.DEFAULT);
            stack.set(ModDataComponents.HOPPER_UPGRADE_DATA.get(),
                    new HopperUpgradeData(!data.enabled(), data.allowlist(), data.itemFilters(), data.modFilters(), data.tagFilters(), data.blockModdedItems()));
            this.backpackUpgradeInventory.setItem(upgradeSlot, stack);
            this.broadcastChanges();
            return;
        }

        if (stack.getItem() instanceof FilterUpgradeItem) {
            FilterUpgradeData data = stack.getOrDefault(ModDataComponents.FILTER_UPGRADE_DATA.get(), FilterUpgradeData.DEFAULT);
            stack.set(ModDataComponents.FILTER_UPGRADE_DATA.get(),
                    new FilterUpgradeData(!data.enabled(), data.allowlist(), data.itemFilters(), data.modFilters(), data.tagFilters(),
                            data.selectedInputType(), data.matchNbt(), data.matchDamage(), data.blockModdedItems()));
            this.backpackUpgradeInventory.setItem(upgradeSlot, stack);
            this.broadcastChanges();
            return;
        }

        if (stack.getItem() instanceof AutoToolUpgradeItem) {
            AutoToolUpgradeData data = stack.getOrDefault(ModDataComponents.AUTO_TOOL_UPGRADE_DATA.get(), AutoToolUpgradeData.DEFAULT);
            stack.set(ModDataComponents.AUTO_TOOL_UPGRADE_DATA.get(),
                    new AutoToolUpgradeData(!data.enabled(), data.allowlist(), data.itemFilters(), data.modFilters(), data.tagFilters(),
                            data.selectedInputType(), data.matchNbt(), data.matchDamage(), data.blockModdedItems()));
            this.backpackUpgradeInventory.setItem(upgradeSlot, stack);
            this.broadcastChanges();
            return;
        }

        if (stack.getItem() instanceof QuiverUpgradeItem) {
            QuiverUpgradeData data = stack.getOrDefault(ModDataComponents.QUIVER_UPGRADE_DATA.get(), QuiverUpgradeData.DEFAULT);
            stack.set(ModDataComponents.QUIVER_UPGRADE_DATA.get(), data.withEnabled(!data.enabled()));
            this.backpackUpgradeInventory.setItem(upgradeSlot, stack);
            this.broadcastChanges();
            return;
        }

        if (stack.getItem() instanceof TrashCanUpgradeItem) {
            TrashCanUpgradeData data = stack.getOrDefault(ModDataComponents.TRASH_CAN_UPGRADE_DATA.get(), TrashCanUpgradeData.DEFAULT);
            stack.set(ModDataComponents.TRASH_CAN_UPGRADE_DATA.get(), data.withEnabled(!data.enabled()));
            this.backpackUpgradeInventory.setItem(upgradeSlot, stack);
            this.broadcastChanges();
            return;
        }

        if (stack.getItem() instanceof RescueUpgradeItem) {
            RescueUpgradeData data = stack.getOrDefault(ModDataComponents.RESCUE_UPGRADE_DATA.get(), RescueUpgradeData.DEFAULT);
            stack.set(ModDataComponents.RESCUE_UPGRADE_DATA.get(), data.withEnabled(!data.enabled()));
            this.backpackUpgradeInventory.setItem(upgradeSlot, stack);
            this.broadcastChanges();
            return;
        }

        if (stack.getItem() instanceof BuilderUpgradeItem) {
            BuilderUpgradeData data = stack.getOrDefault(ModDataComponents.BUILDER_UPGRADE_DATA.get(), BuilderUpgradeData.DEFAULT);
            stack.set(ModDataComponents.BUILDER_UPGRADE_DATA.get(), data.withEnabled(!data.enabled()));
            this.backpackUpgradeInventory.setItem(upgradeSlot, stack);
            this.broadcastChanges();
            return;
        }

        if (stack.getItem() instanceof TorchPlacerUpgradeItem) {
            TorchPlacerUpgradeData data = stack.getOrDefault(ModDataComponents.TORCH_PLACER_UPGRADE_DATA.get(), TorchPlacerUpgradeData.DEFAULT);
            stack.set(ModDataComponents.TORCH_PLACER_UPGRADE_DATA.get(), data.withEnabled(!data.enabled()));
            this.backpackUpgradeInventory.setItem(upgradeSlot, stack);
            this.broadcastChanges();
            return;
        }

        if (stack.getItem() instanceof CapacityWarningUpgradeItem) {
            CapacityWarningUpgradeData data = stack.getOrDefault(ModDataComponents.CAPACITY_WARNING_UPGRADE_DATA.get(), CapacityWarningUpgradeData.DEFAULT);
            stack.set(ModDataComponents.CAPACITY_WARNING_UPGRADE_DATA.get(), data.withEnabled(!data.enabled()));
            this.backpackUpgradeInventory.setItem(upgradeSlot, stack);
            this.broadcastChanges();
        }
    }

    public boolean handleInstalledUpgradeRightClick(net.minecraft.server.level.ServerPlayer player, int upgradeSlot) {
        return this.handleInstalledUpgradeRightClick(player, upgradeSlot, false);
    }

    public boolean handleInstalledUpgradeRightClick(net.minecraft.server.level.ServerPlayer player, int upgradeSlot, boolean shiftDown) {
        if (upgradeSlot < 0 || upgradeSlot >= UPGRADE_SLOT_COUNT) {
            return false;
        }

        ItemStack stack = this.getUpgradeStack(upgradeSlot);
        boolean handled = stack.getItem() instanceof BackpackUpgradeItem upgradeItem
                && upgradeItem.onInstalledRightClicked(player, this.access, upgradeSlot, stack, shiftDown);
        if (handled) {
            this.backpackUpgradeInventory.setItem(upgradeSlot, stack);
            this.broadcastChanges();
        }
        return handled;
    }

    public void sortContents(BackpackSortMode sortMode) {
        if (!this.owner.level().isClientSide()) {
            this.backpackInventory.allowStorageWrites();
        }
        this.sortMode = sortMode;
        ItemStack backpack = this.backpackInventory.getBackpackStack();
        this.backpackInventory.sortContents(sortMode,
                slot -> ItemLockProtection.canSortSlot(backpack, slot, this.backpackInventory.getItem(slot)));
        this.broadcastChanges();
    }

    public void applyClientSearchFilter(Predicate<ItemStack> filter) {
        for (int index = 0; index < this.getBackpackSlotCount(); index++) {
            if (this.slots.get(index) instanceof BackpackSlot backpackSlot) {
                backpackSlot.setVisible(filter.test(backpackSlot.getItem()));
            }
        }
    }

    public int getImageHeight() {
        return BackpackScreenLayout.imageHeight(this.visibleBackpackRows, UPGRADE_SLOT_COUNT);
    }

    public boolean hasTrashCanUpgrade() {
        return this.findTrashCanUpgradeSlot() >= 0;
    }

    public boolean isTrashSlotIndex(int menuSlot) {
        return menuSlot == this.trashSlotIndex;
    }

    public int getTrashSlotIndex() {
        return this.trashSlotIndex;
    }

    public TrashCanUpgradeData getTrashCanData() {
        ItemStack upgrade = this.getTrashCanUpgradeStack();
        return upgrade.isEmpty() ? TrashCanUpgradeData.DEFAULT : upgrade.getOrDefault(ModDataComponents.TRASH_CAN_UPGRADE_DATA.get(), TrashCanUpgradeData.DEFAULT);
    }

    public boolean hasTrashPendingItem() {
        return this.hasTrashCanUpgrade() && this.getTrashCanData().hasPendingItem();
    }

    public boolean hasTrashRecoveryItem() {
        return this.hasTrashCanUpgrade() && this.getTrashCanData().hasLastDeletedItem();
    }

    public boolean isTrashPendingProtected() {
        return this.hasTrashPendingItem() && TrashCanUpgradeHandler.requiresConfirmation(this.getTrashCanData().loadPendingItem());
    }

    public int getTrashRemainingTicks() {
        return this.getTrashCanData().remainingTicks();
    }

    public boolean hasCapacityWarningUpgrade() {
        return this.findCapacityWarningUpgradeSlot() >= 0;
    }

    public boolean hasItemLockUpgrade() {
        return this.findItemLockUpgradeSlot() >= 0 && ItemLockProtection.isActive(this.backpackInventory.getBackpackStack());
    }

    public boolean isLogicalSlotLocked(int logicalSlot) {
        return ItemLockProtection.isSlotLocked(this.backpackInventory.getBackpackStack(), logicalSlot);
    }

    public boolean isItemLocked(ItemStack stack) {
        return ItemLockProtection.isStackLocked(this.backpackInventory.getBackpackStack(), stack);
    }

    public boolean isBackpackMenuSlotLocked(int menuSlot) {
        int logicalSlot = this.getLogicalBackpackSlot(menuSlot);
        return logicalSlot >= 0 && ItemLockProtection.isSlotLocked(this.backpackInventory.getBackpackStack(), logicalSlot);
    }

    public boolean isBackpackMenuSlotProtected(int menuSlot) {
        int logicalSlot = this.getLogicalBackpackSlot(menuSlot);
        return logicalSlot >= 0
                && ItemLockProtection.isProtected(this.backpackInventory.getBackpackStack(), logicalSlot, this.backpackInventory.getItem(logicalSlot));
    }

    public int getLogicalBackpackSlot(int menuSlot) {
        if (menuSlot < 0 || menuSlot >= this.getBackpackSlotCount() || !(this.slots.get(menuSlot) instanceof BackpackSlot backpackSlot)) {
            return -1;
        }

        int logicalSlot = backpackSlot.getMappedSlot();
        return logicalSlot >= 0 && logicalSlot < this.tier.getSlotCount() ? logicalSlot : -1;
    }

    public void toggleItemLock(ToggleItemLockPayload.Action action, int logicalSlot, Player player) {
        if (player.level().isClientSide()) {
            return;
        }
        if (!this.hasItemLockUpgrade()) {
            PlayerMessageHelper.sendStatus(player, Component.translatable("message.smartbackpacks.item_lock_no_upgrade"));
            return;
        }
        if (logicalSlot < 0 || logicalSlot >= this.backpackInventory.getContainerSize()) {
            return;
        }

        ItemStack backpack = this.backpackInventory.getBackpackStack();
        ItemLockData data = ItemLockProtection.getData(backpack);
        switch (action) {
            case TOGGLE_SLOT_LOCK -> {
                if (!SmartBackpacksConfig.itemLockAllowSlotLocks() || !data.slotLocking()) {
                    return;
                }
                boolean wasLocked = data.hasLockedSlot(logicalSlot);
                ItemLockProtection.setData(backpack, data.toggleSlot(logicalSlot));
                this.access.setBackpackStack(this.owner, backpack);
                PlayerMessageHelper.sendStatus(player, Component.translatable(wasLocked
                        ? "message.smartbackpacks.item_lock_slot_unlocked"
                        : "message.smartbackpacks.item_lock_slot_locked"));
            }
            case TOGGLE_ITEM_LOCK -> {
                if (!SmartBackpacksConfig.itemLockAllowItemLocks() || !data.exactItemLocking()) {
                    return;
                }
                ItemStack stack = this.backpackInventory.getItem(logicalSlot);
                if (stack.isEmpty()) {
                    PlayerMessageHelper.sendStatus(player, Component.translatable("message.smartbackpacks.item_lock_empty_slot"));
                    return;
                }
                String fingerprint = ItemLockProtection.fingerprint(stack);
                boolean wasLocked = data.hasItemFingerprint(fingerprint);
                ItemLockProtection.setData(backpack, data.toggleItemFingerprint(fingerprint));
                this.access.setBackpackStack(this.owner, backpack);
                PlayerMessageHelper.sendStatus(player, Component.translatable(wasLocked
                        ? "message.smartbackpacks.item_lock_item_unlocked"
                        : "message.smartbackpacks.item_lock_item_locked"));
            }
            case TOGGLE_TYPE_LOCK -> {
                if (!SmartBackpacksConfig.itemLockAllowTypeLocks() || !data.typeLocking()) {
                    PlayerMessageHelper.sendStatus(player, Component.translatable("message.smartbackpacks.item_lock_type_disabled"));
                    return;
                }
                ItemStack stack = this.backpackInventory.getItem(logicalSlot);
                if (stack.isEmpty()) {
                    PlayerMessageHelper.sendStatus(player, Component.translatable("message.smartbackpacks.item_lock_empty_slot"));
                    return;
                }
                String itemType = ItemLockProtection.itemType(stack);
                boolean wasLocked = data.hasItemType(itemType);
                ItemLockProtection.setData(backpack, data.toggleItemType(itemType));
                this.access.setBackpackStack(this.owner, backpack);
                PlayerMessageHelper.sendStatus(player, Component.translatable(wasLocked
                        ? "message.smartbackpacks.item_lock_item_unlocked"
                        : "message.smartbackpacks.item_lock_item_locked"));
            }
        }
        this.broadcastChanges();
    }

    public CapacityWarningUpgradeData getCapacityWarningData() {
        ItemStack upgrade = this.getCapacityWarningUpgradeStack();
        return upgrade.isEmpty() ? CapacityWarningUpgradeData.DEFAULT : upgrade.getOrDefault(ModDataComponents.CAPACITY_WARNING_UPGRADE_DATA.get(), CapacityWarningUpgradeData.DEFAULT);
    }

    public CapacityWarningSnapshot getCapacityWarningSnapshot() {
        CapacityWarningUpgradeData data = this.getCapacityWarningData();
        CapacityWarningCalculationMode mode = data.calculationMode();
        int totalSlots = this.backpackInventory.getContainerSize();
        if (totalSlots <= 0) {
            return CapacityWarningSnapshot.empty(mode);
        }

        int occupiedSlots = 0;
        double filledUnits = 0.0D;
        for (int slot = 0; slot < totalSlots; slot++) {
            ItemStack stack = this.backpackInventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            occupiedSlots++;
            if (mode == CapacityWarningCalculationMode.STACK_CAPACITY) {
                int limit = Math.max(1, this.backpackInventory.getMaxStackSize(stack));
                filledUnits += limit <= 1 ? 1.0D : Math.max(0.0D, Math.min(1.0D, stack.getCount() / (double) limit));
            }
        }

        double percentage = mode == CapacityWarningCalculationMode.STACK_CAPACITY
                ? filledUnits * 100.0D / totalSlots
                : occupiedSlots * 100.0D / totalSlots;
        percentage = Math.max(0.0D, Math.min(100.0D, percentage));
        int displayPercentage = percentage <= 0.0D
                ? 0
                : Math.max(1, Math.min(100, (int) Math.ceil(percentage - 0.000001D)));
        return new CapacityWarningSnapshot(percentage, displayPercentage, occupiedSlots, totalSlots,
                Math.max(0, totalSlots - occupiedSlots), mode);
    }

    @Override
    public boolean stillValid(Player player) {
        if (!player.level().isClientSide() && this.access.source() == BackpackAccess.Source.DISPLAY_HOOK
                && this.access.getBackpackStack(player) != this.openedHookBackpack) return false;
        return this.backpackInventory.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            if (this.hasTrashPendingItem()) {
                this.backpackInventory.allowStorageWrites();
            }
            this.handleTrashOnClose(player);
        }
        if (!player.level().isClientSide()) {
            OpenBackpackTracker.markClosed(player, this.access);
        }
        this.getPlacedBackpackBlockEntity(player).ifPresent(PlacedBackpackBlockEntity::stopOpen);
        if (!player.level().isClientSide()) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 0.7F, 0.9F);
        }
    }

    private java.util.Optional<PlacedBackpackBlockEntity> getPlacedBackpackBlockEntity(Player player) {
        if (this.access.source() != BackpackAccess.Source.BLOCK) {
            return java.util.Optional.empty();
        }

        return java.util.Optional.ofNullable(player.level().getBlockEntity(this.access.blockPos()))
                .filter(PlacedBackpackBlockEntity.class::isInstance)
                .map(PlacedBackpackBlockEntity.class::cast);
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput ContainerInput, Player player) {
        if (!player.level().isClientSide()) {
            this.backpackInventory.allowStorageWrites();
        }
        if (slotId >= this.getBackpackSlotCount() && slotId < this.getBackpackSlotCount() + UPGRADE_SLOT_COUNT
                && ContainerInput == ContainerInput.PICKUP
                && button == 1) {
            Slot slot = this.slots.get(slotId);
            if (slot.hasItem()
                    && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                    && this.handleInstalledUpgradeRightClick(serverPlayer, slotId - this.getBackpackSlotCount())) {
                return;
            }
        }

        // The bound carrier slot stays locked while the menu is open to avoid move/drop dupes.
        if (ContainerInput == ContainerInput.SWAP && button == this.lockedInventorySlot) {
            return;
        }

        if (slotId >= 0 && slotId < this.slots.size() && this.slots.get(slotId) instanceof PlayerInventorySlot playerSlot && playerSlot.isLocked()) {
            return;
        }

        if (this.blocksLockedBackpackInteraction(slotId, player)) {
            return;
        }

        int progressSlot = ContainerInput == ContainerInput.PICKUP || ContainerInput == ContainerInput.SWAP
                ? this.getLogicalBackpackSlot(slotId) : -1;
        ItemStack beforeClick = progressSlot >= 0 ? this.backpackInventory.getItem(progressSlot).copy() : null;
        ItemStack[] beforeDrag = this.snapshotDragStorage(player, ContainerInput == ContainerInput.QUICK_CRAFT);
        if (this.handleLogicalStackPickup(slotId, button, ContainerInput)) {
            this.recordManualInsertion(player, progressSlot, beforeClick);
            return;
        }

        super.clicked(slotId, button, ContainerInput, player);
        this.recordManualInsertion(player, progressSlot, beforeClick);
        this.recordDragInsertion(player, beforeDrag);
    }

    private ItemStack[] snapshotDragStorage(Player player, boolean dragging) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer) || !dragging) return null;
        ItemStack[] before = new ItemStack[this.backpackInventory.getContainerSize()];
        for (int slot = 0; slot < before.length; slot++) before[slot] = this.backpackInventory.getItem(slot).copy();
        return before;
    }

    private void recordDragInsertion(Player player, ItemStack[] before) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) || before == null) return;
        long inserted = 0;
        for (int slot = 0; slot < before.length; slot++) {
            ItemStack after = this.backpackInventory.getItem(slot);
            if (after.isEmpty()) continue;
            inserted += ItemStack.isSameItemSameComponents(before[slot], after)
                    ? Math.max(0, after.getCount() - before[slot].getCount()) : after.getCount();
        }
        if (inserted > 0) com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.recordInsertion(
                serverPlayer, this.backpackInventory.getBackpackStack(), this.tier, inserted);
    }

    private void recordManualInsertion(Player player, int logicalSlot, ItemStack before) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) || logicalSlot < 0 || before == null) return;
        ItemStack after = this.backpackInventory.getItem(logicalSlot);
        if (after.isEmpty()) return;
        long inserted = ItemStack.isSameItemSameComponents(before, after)
                ? Math.max(0, after.getCount() - before.getCount()) : after.getCount();
        if (inserted > 0) com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.recordInsertion(
                serverPlayer, this.backpackInventory.getBackpackStack(), this.tier, inserted);
    }

    private boolean handleLogicalStackPickup(int slotId, int button, ContainerInput input) {
        if (slotId < 0 || slotId >= this.getBackpackSlotCount()) {
            return false;
        }

        int logicalSlot = this.getLogicalBackpackSlot(slotId);
        if (logicalSlot < 0) {
            return false;
        }

        ItemStack existing = this.backpackInventory.getItem(logicalSlot);
        if (existing.isEmpty()) {
            return false;
        }

        int vanillaLimit = Math.max(1, existing.getMaxStackSize());
        int logicalLimit = this.backpackInventory.getMaxStackSize(existing);
        if (logicalLimit <= vanillaLimit) {
            return false;
        }

        if ((input == ContainerInput.SWAP || input == ContainerInput.THROW)
                && existing.getCount() > vanillaLimit) {
            return true;
        }
        if (input != ContainerInput.PICKUP) {
            return false;
        }

        ItemStack carried = this.getCarried();
        if (carried.isEmpty()) {
            if (existing.getCount() <= vanillaLimit) {
                return false;
            }
            int amount = button == 1 ? 1 : Math.min(vanillaLimit, existing.getCount());
            this.setCarried(this.backpackInventory.removeItem(logicalSlot, amount));
            return true;
        }

        if (ItemStack.isSameItemSameComponents(existing, carried)) {
            int requested = button == 1 ? 1 : carried.getCount();
            int transfer = Math.min(requested, logicalLimit - existing.getCount());
            if (transfer > 0) {
                existing.grow(transfer);
                carried.shrink(transfer);
                this.backpackInventory.setChanged();
            }
            return true;
        }

        // Swapping would place an illegal oversized stack on the vanilla cursor.
        return existing.getCount() > vanillaLimit;
    }

    private boolean blocksLockedBackpackInteraction(int slotId, Player player) {
        if (slotId < 0 || slotId >= this.getBackpackSlotCount()) {
            return false;
        }

        int logicalSlot = this.getLogicalBackpackSlot(slotId);
        if (logicalSlot < 0) {
            return false;
        }

        ItemStack backpack = this.backpackInventory.getBackpackStack();
        ItemStack existing = this.backpackInventory.getItem(logicalSlot);
        ItemStack carried = this.getCarried();
        if (!carried.isEmpty() && !ItemLockProtection.canPlayerInsertIntoSlot(backpack, logicalSlot, existing, carried)) {
            PlayerMessageHelper.sendStatus(player, Component.translatable("message.smartbackpacks.item_lock_locked"));
            return true;
        }

        if (!existing.isEmpty() && !ItemLockProtection.canPlayerTakeFromSlot(backpack, logicalSlot, existing)) {
            PlayerMessageHelper.sendStatus(player, Component.translatable("message.smartbackpacks.item_lock_locked"));
            return true;
        }

        if (carried.isEmpty() && ItemLockProtection.isSlotLocked(backpack, logicalSlot)) {
            PlayerMessageHelper.sendStatus(player, Component.translatable("message.smartbackpacks.item_lock_locked"));
            return true;
        }

        return false;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        int menuSlot = this.slots.indexOf(slot);
        if (menuSlot >= 0 && menuSlot < this.getBackpackSlotCount()) {
            int logicalSlot = this.getLogicalBackpackSlot(menuSlot);
            if (logicalSlot >= 0
                    && !ItemLockProtection.canPlayerTakeFromSlot(this.backpackInventory.getBackpackStack(),
                    logicalSlot, this.backpackInventory.getItem(logicalSlot))) {
                return false;
            }
        }
        return super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public void broadcastChanges() {
        if (!this.owner.level().isClientSide()) {
            this.tickTrashCan();
        }
        super.broadcastChanges();
        if (!this.owner.level().isClientSide() && this.initialSlotSyncPending) {
            this.initialSlotSyncPending = false;
            this.syncVisibleBackpackSlotsToClient();
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = this.slots.get(index);
        if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }
        if (!sourceSlot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack sourceCopy = sourceStack.copy();
        int backpackSlots = this.getBackpackSlotCount();
        int upgradeStart = backpackSlots;
        int playerStart = backpackSlots + UPGRADE_SLOT_COUNT;
        int playerEnd = playerStart + 36;

        if (index < backpackSlots) {
            int logicalSlot = this.getLogicalBackpackSlot(index);
            if (logicalSlot >= 0
                    && !ItemLockProtection.canPlayerTakeFromSlot(this.backpackInventory.getBackpackStack(), logicalSlot, sourceStack)) {
                PlayerMessageHelper.sendStatus(player, Component.translatable("message.smartbackpacks.item_lock_locked"));
                return ItemStack.EMPTY;
            }
            if (this.moveItemStackToQuiverStorage(sourceStack, null)) {
                // Projectile items can be promoted from normal storage into installed Quiver Upgrades.
            } else if (!this.moveBackpackStackToPlayerInventory(sourceStack, playerStart, playerEnd)) {
                return ItemStack.EMPTY;
            }
        } else if (index < playerStart) {
            if (!this.moveItemStackTo(sourceStack, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (index < playerEnd) {
            if (sourceStack.getItem() instanceof BackpackUpgradeItem && this.moveItemStackTo(sourceStack, upgradeStart, playerStart, false)) {
                // Prefer dedicated upgrade slots first.
            } else if (this.moveItemStackToQuiverStorage(sourceStack, PickupNotifierSource.MANUAL_TRANSFER)) {
                // Projectile items route into installed Quiver Upgrades before normal backpack storage.
            } else if (!this.moveItemStackToBackpackStorage(sourceStack, true, PickupNotifierSource.MANUAL_TRANSFER)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.moveItemStackTo(sourceStack, playerStart, playerEnd, true)
                    && !this.moveItemStackToBackpackStorage(sourceStack, true, null)) {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        return sourceCopy;
    }

    private boolean moveBackpackStackToPlayerInventory(ItemStack sourceStack, int playerStart, int playerEnd) {
        boolean moved = false;
        while (!sourceStack.isEmpty()) {
            int previousCount = sourceStack.getCount();
            if (!this.moveItemStackTo(sourceStack, playerStart, playerEnd, true)) {
                break;
            }
            moved = true;
            if (sourceStack.getCount() >= previousCount) {
                break;
            }
        }
        return moved;
    }

    private boolean moveItemStackToBackpackStorage(ItemStack sourceStack) {
        return this.moveItemStackToBackpackStorage(sourceStack, false, null);
    }

    private boolean moveItemStackToBackpackStorage(ItemStack sourceStack, boolean warnIfRejected) {
        return this.moveItemStackToBackpackStorage(sourceStack, warnIfRejected, null);
    }

    private boolean moveItemStackToBackpackStorage(ItemStack sourceStack, boolean warnIfRejected, PickupNotifierSource notifySource) {
        if (sourceStack.isEmpty() || !BackpackStackData.isValidStorageItem(this.backpackInventory.getBackpackStack(), sourceStack)) {
            return false;
        }

        int originalCount = sourceStack.getCount();
        ItemStack notificationStack = sourceStack.copy();

        for (int slot = 0; slot < this.backpackInventory.getContainerSize() && !sourceStack.isEmpty(); slot++) {
            ItemStack existing = this.backpackInventory.getItem(slot);
            if (existing.isEmpty()
                    || !ItemStack.isSameItemSameComponents(existing, sourceStack)
                    || !ItemLockProtection.canPlayerInsertIntoSlot(this.backpackInventory.getBackpackStack(), slot, existing, sourceStack)) {
                continue;
            }

            int transfer = Math.min(sourceStack.getCount(), this.backpackInventory.getMaxStackSize(existing) - existing.getCount());
            if (transfer <= 0) {
                continue;
            }

            existing.grow(transfer);
            sourceStack.shrink(transfer);
            this.backpackInventory.setChanged();
        }

        for (int slot = 0; slot < this.backpackInventory.getContainerSize() && !sourceStack.isEmpty(); slot++) {
            ItemStack existing = this.backpackInventory.getItem(slot);
            if (!existing.isEmpty()
                    || !ItemLockProtection.canPlayerInsertIntoSlot(this.backpackInventory.getBackpackStack(), slot, existing, sourceStack)) {
                continue;
            }

            int placed = Math.min(sourceStack.getCount(), this.backpackInventory.getMaxStackSize(sourceStack));
            this.backpackInventory.setItem(slot, sourceStack.copyWithCount(placed));
            sourceStack.shrink(placed);
        }

        boolean moved = sourceStack.getCount() != originalCount;
        if (moved && notifySource != null && this.owner instanceof ServerPlayer serverPlayer) {
            PickupNotifierServer.reportInserted(serverPlayer, this.backpackInventory.getBackpackStack(), this.tier,
                    notificationStack.copyWithCount(originalCount - sourceStack.getCount()),
                    PickupNotifierDestination.MAIN_STORAGE, notifySource);
        }
        if (warnIfRejected && !sourceStack.isEmpty() && this.owner instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            CapacityWarningUpgradeHandler.warnFailedInsertion(serverPlayer, this.backpackInventory.getBackpackStack(), this.tier, sourceStack);
        }
        return moved;
    }

    private boolean moveItemStackToQuiverStorage(ItemStack sourceStack, PickupNotifierSource notifySource) {
        if (sourceStack.isEmpty() || !QuiverUpgradeHandler.isQuiverProjectile(sourceStack)) {
            return false;
        }
        if (ItemLockProtection.isStackLocked(this.backpackInventory.getBackpackStack(), sourceStack)) {
            return false;
        }

        int originalCount = sourceStack.getCount();
        ItemStack notificationStack = sourceStack.copy();
        for (int upgradeSlot = 0; upgradeSlot < this.backpackUpgradeInventory.getContainerSize() && !sourceStack.isEmpty(); upgradeSlot++) {
            ItemStack upgrade = this.backpackUpgradeInventory.getItem(upgradeSlot);
            if (!(upgrade.getItem() instanceof QuiverUpgradeItem)) {
                continue;
            }

            QuiverUpgradeData data = upgrade.getOrDefault(ModDataComponents.QUIVER_UPGRADE_DATA.get(), QuiverUpgradeData.DEFAULT);
            net.minecraft.core.NonNullList<ItemStack> projectiles = data.loadProjectiles();
            this.insertIntoQuiverSlots(projectiles, sourceStack);
            if (sourceStack.getCount() == originalCount) {
                continue;
            }

            upgrade.set(ModDataComponents.QUIVER_UPGRADE_DATA.get(), data.withProjectiles(projectiles));
            this.backpackUpgradeInventory.setItem(upgradeSlot, upgrade);
        }

        boolean moved = sourceStack.getCount() != originalCount;
        if (moved && notifySource != null && this.owner instanceof ServerPlayer serverPlayer) {
            PickupNotifierServer.reportInserted(serverPlayer, this.backpackInventory.getBackpackStack(), this.tier,
                    notificationStack.copyWithCount(originalCount - sourceStack.getCount()),
                    PickupNotifierDestination.QUIVER_STORAGE, notifySource);
        }
        return moved;
    }

    private void insertIntoQuiverSlots(net.minecraft.core.NonNullList<ItemStack> projectiles, ItemStack sourceStack) {
        for (int slot = 0; slot < projectiles.size() && !sourceStack.isEmpty(); slot++) {
            ItemStack existing = projectiles.get(slot);
            if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, sourceStack)) {
                continue;
            }

            int transfer = Math.min(sourceStack.getCount(), existing.getMaxStackSize() - existing.getCount());
            if (transfer <= 0) {
                continue;
            }

            existing.grow(transfer);
            sourceStack.shrink(transfer);
        }

        for (int slot = 0; slot < projectiles.size() && !sourceStack.isEmpty(); slot++) {
            if (!projectiles.get(slot).isEmpty()) {
                continue;
            }

            int transfer = Math.min(sourceStack.getCount(), sourceStack.getMaxStackSize());
            projectiles.set(slot, sourceStack.copyWithCount(transfer));
            sourceStack.shrink(transfer);
        }
    }

    private void addUpgradeSlots() {
        for (int slot = 0; slot < UPGRADE_SLOT_COUNT; slot++) {
            this.addSlot(new UpgradeSlot(this.backpackUpgradeInventory, slot,
                    BackpackScreenLayout.UPGRADE_SLOT_X,
                    BackpackScreenLayout.UPGRADE_SLOT_Y + slot * BackpackScreenLayout.UPGRADE_SLOT_STEP_Y));
        }
    }

    private void addTrashSlot() {
        this.trashSlotIndex = this.slots.size();
        this.addSlot(new TrashSlot(BackpackScreenLayout.TRASH_SLOT_X, BackpackScreenLayout.TRASH_SLOT_Y));
    }

    public void moveMenuSlotToTrash(int slotIndex) {
        if (!this.owner.level().isClientSide()) {
            this.backpackInventory.allowStorageWrites();
        }
        if (slotIndex < 0 || slotIndex >= this.slots.size() || slotIndex == this.trashSlotIndex) {
            return;
        }
        if (!this.hasTrashCanUpgrade()) {
            return;
        }

        TrashCanUpgradeData data = this.getTrashCanData();
        if (!data.enabled()) {
            PlayerMessageHelper.sendStatus(this.owner, Component.translatable("message.smartbackpacks.trash_can_disabled"));
            return;
        }
        if (data.hasPendingItem()) {
            PlayerMessageHelper.sendStatus(this.owner, Component.translatable("message.smartbackpacks.trash_can_slot_occupied"));
            return;
        }

        Slot sourceSlot = this.slots.get(slotIndex);
        if (!sourceSlot.hasItem() || !sourceSlot.mayPickup(this.owner)) {
            return;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack moved = sourceStack.copy();
        int logicalSlot = slotIndex < this.getBackpackSlotCount() ? this.getLogicalBackpackSlot(slotIndex) : -1;
        if (!ItemLockProtection.canTrashStack(this.backpackInventory.getBackpackStack(), logicalSlot, moved)) {
            PlayerMessageHelper.sendStatus(this.owner, Component.translatable("message.smartbackpacks.item_lock_trash_blocked"));
            return;
        }
        if (!TrashCanUpgradeHandler.canEnterTrash(moved, this.backpackInventory.getBackpackStack())) {
            PlayerMessageHelper.sendStatus(this.owner, Component.translatable("message.smartbackpacks.trash_can_never_allowed"));
            return;
        }

        this.setTrashCanData(data.withPendingItem(moved, TrashCanUpgradeHandler.deletionDelayTicks()));
        sourceSlot.set(ItemStack.EMPTY);
        sourceSlot.setChanged();
        this.broadcastChanges();
    }

    public boolean deleteTrashPending(boolean confirmed) {
        if (!this.hasTrashCanUpgrade()) {
            return false;
        }

        TrashCanUpgradeData data = this.getTrashCanData();
        ItemStack pending = data.loadPendingItem();
        if (pending.isEmpty()) {
            return false;
        }
        if (TrashCanUpgradeHandler.isNeverAllowed(pending, this.backpackInventory.getBackpackStack())) {
            PlayerMessageHelper.sendStatus(this.owner, Component.translatable("message.smartbackpacks.trash_can_never_allowed"));
            return false;
        }
        if (!ItemLockProtection.canTrashStack(this.backpackInventory.getBackpackStack(), -1, pending)) {
            PlayerMessageHelper.sendStatus(this.owner, Component.translatable("message.smartbackpacks.item_lock_trash_blocked"));
            return false;
        }
        if (TrashCanUpgradeHandler.requiresConfirmation(pending) && !confirmed) {
            PlayerMessageHelper.sendStatus(this.owner, Component.translatable("message.smartbackpacks.trash_can_confirmation_required"));
            return false;
        }

        TrashCanUpgradeData next = data.withLastDeletedItem(pending).clearPendingItem();
        this.setTrashCanData(next);
        PlayerMessageHelper.sendStatus(this.owner, Component.translatable("message.smartbackpacks.trash_can_deleted"));
        this.broadcastChanges();
        return true;
    }

    public boolean restoreLastTrashedItem() {
        if (!this.owner.level().isClientSide()) {
            this.backpackInventory.allowStorageWrites();
        }
        if (!this.hasTrashCanUpgrade()) {
            return false;
        }

        TrashCanUpgradeData data = this.getTrashCanData();
        ItemStack recovery = data.loadLastDeletedItem();
        if (recovery.isEmpty()) {
            return false;
        }

        if (this.canInsertIntoPlayerInventory(recovery)) {
            this.insertIntoPlayerInventory(recovery.copy());
            this.setTrashCanData(data.clearLastDeletedItem());
            PlayerMessageHelper.sendStatus(this.owner, Component.translatable("message.smartbackpacks.trash_can_restored"));
            this.broadcastChanges();
            return true;
        }

        if (this.canInsertIntoBackpackStorage(recovery)) {
            this.insertIntoBackpackStorage(recovery.copy());
            this.setTrashCanData(data.clearLastDeletedItem());
            PlayerMessageHelper.sendStatus(this.owner, Component.translatable("message.smartbackpacks.trash_can_restored"));
            this.broadcastChanges();
            return true;
        }

        PlayerMessageHelper.sendStatus(this.owner, Component.translatable("message.smartbackpacks.trash_can_restore_no_space"));
        return false;
    }

    private void tickTrashCan() {
        if (!this.hasTrashCanUpgrade()) {
            return;
        }

        TrashCanUpgradeData data = this.getTrashCanData();
        ItemStack pending = data.loadPendingItem();
        if (pending.isEmpty() || !data.enabled() || TrashCanUpgradeHandler.requiresConfirmation(pending)) {
            return;
        }

        int remaining = Math.max(0, data.remainingTicks() - 1);
        if (remaining <= 0) {
            this.deleteTrashPending(false);
        } else if (remaining != data.remainingTicks()) {
            this.setTrashCanData(data.withRemainingTicks(remaining));
        }
    }

    private void handleTrashOnClose(Player player) {
        if (!this.hasTrashCanUpgrade()) {
            return;
        }

        TrashCanUpgradeData data = this.getTrashCanData();
        ItemStack pending = data.loadPendingItem();
        if (pending.isEmpty()) {
            return;
        }

        if (SmartBackpacksConfig.trashCanDeletePendingItemOnClose()
                && data.enabled()
                && !TrashCanUpgradeHandler.requiresConfirmation(pending)
                && !TrashCanUpgradeHandler.isNeverAllowed(pending, this.backpackInventory.getBackpackStack())
                && ItemLockProtection.canTrashStack(this.backpackInventory.getBackpackStack(), -1, pending)) {
            this.deleteTrashPending(false);
            return;
        }

        if (this.returnPendingTrashSafely(pending)) {
            this.setTrashCanData(data.clearPendingItem());
            return;
        }

        PlayerMessageHelper.sendStatus(player, Component.translatable("message.smartbackpacks.trash_can_pending_kept"));
    }

    private boolean returnPendingTrashSafely(ItemStack stack) {
        if (this.canInsertIntoPlayerInventory(stack)) {
            this.insertIntoPlayerInventory(stack.copy());
            return true;
        }
        if (this.canInsertIntoBackpackStorage(stack)) {
            this.insertIntoBackpackStorage(stack.copy());
            return true;
        }
        return false;
    }

    private int findTrashCanUpgradeSlot() {
        for (int slot = 0; slot < this.backpackUpgradeInventory.getContainerSize(); slot++) {
            if (this.backpackUpgradeInventory.getItem(slot).getItem() instanceof TrashCanUpgradeItem) {
                return slot;
            }
        }
        return -1;
    }

    private ItemStack getTrashCanUpgradeStack() {
        int slot = this.findTrashCanUpgradeSlot();
        return slot >= 0 ? this.backpackUpgradeInventory.getItem(slot) : ItemStack.EMPTY;
    }

    private int findCapacityWarningUpgradeSlot() {
        for (int slot = 0; slot < this.backpackUpgradeInventory.getContainerSize(); slot++) {
            if (this.backpackUpgradeInventory.getItem(slot).getItem() instanceof CapacityWarningUpgradeItem) {
                return slot;
            }
        }
        return -1;
    }

    private int findItemLockUpgradeSlot() {
        for (int slot = 0; slot < this.backpackUpgradeInventory.getContainerSize(); slot++) {
            if (this.backpackUpgradeInventory.getItem(slot).getItem() instanceof ItemLockUpgradeItem) {
                return slot;
            }
        }
        return -1;
    }

    private ItemStack getCapacityWarningUpgradeStack() {
        int slot = this.findCapacityWarningUpgradeSlot();
        return slot >= 0 ? this.backpackUpgradeInventory.getItem(slot) : ItemStack.EMPTY;
    }

    private void setTrashCanData(TrashCanUpgradeData data) {
        int slot = this.findTrashCanUpgradeSlot();
        if (slot < 0) {
            return;
        }

        ItemStack upgrade = this.backpackUpgradeInventory.getItem(slot);
        upgrade.set(ModDataComponents.TRASH_CAN_UPGRADE_DATA.get(), data);
        this.backpackUpgradeInventory.setItem(slot, upgrade);
    }

    private boolean canInsertIntoPlayerInventory(ItemStack stack) {
        ItemStack remaining = stack.copy();
        Inventory inventory = this.owner.getInventory();
        for (int slot = 0; slot < 36 && !remaining.isEmpty(); slot++) {
            if (slot == this.lockedInventorySlot) {
                continue;
            }

            ItemStack existing = inventory.getItem(slot);
            if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, remaining)) {
                continue;
            }

            int transfer = Math.min(remaining.getCount(), Math.min(existing.getMaxStackSize(), inventory.getMaxStackSize()) - existing.getCount());
            if (transfer > 0) {
                remaining.shrink(transfer);
            }
        }

        for (int slot = 0; slot < 36 && !remaining.isEmpty(); slot++) {
            if (slot != this.lockedInventorySlot && inventory.getItem(slot).isEmpty()) {
                remaining.shrink(Math.min(remaining.getCount(), Math.min(remaining.getMaxStackSize(), inventory.getMaxStackSize())));
            }
        }
        return remaining.isEmpty();
    }

    private void insertIntoPlayerInventory(ItemStack stack) {
        Inventory inventory = this.owner.getInventory();
        for (int slot = 0; slot < 36 && !stack.isEmpty(); slot++) {
            if (slot == this.lockedInventorySlot) {
                continue;
            }

            ItemStack existing = inventory.getItem(slot);
            if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, stack)) {
                continue;
            }

            int transfer = Math.min(stack.getCount(), Math.min(existing.getMaxStackSize(), inventory.getMaxStackSize()) - existing.getCount());
            if (transfer > 0) {
                existing.grow(transfer);
                stack.shrink(transfer);
            }
        }

        for (int slot = 0; slot < 36 && !stack.isEmpty(); slot++) {
            if (slot == this.lockedInventorySlot || !inventory.getItem(slot).isEmpty()) {
                continue;
            }

            int placed = Math.min(stack.getCount(), Math.min(stack.getMaxStackSize(), inventory.getMaxStackSize()));
            inventory.setItem(slot, stack.copyWithCount(placed));
            stack.shrink(placed);
        }
        inventory.setChanged();
    }

    private boolean canInsertIntoBackpackStorage(ItemStack stack) {
        if (!BackpackStackData.isValidStorageItem(this.backpackInventory.getBackpackStack(), stack)) {
            return false;
        }

        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < this.backpackInventory.getContainerSize() && !remaining.isEmpty(); slot++) {
            ItemStack existing = this.backpackInventory.getItem(slot);
            if (existing.isEmpty()
                    || !ItemStack.isSameItemSameComponents(existing, remaining)
                    || !ItemLockProtection.canPlayerInsertIntoSlot(this.backpackInventory.getBackpackStack(), slot, existing, remaining)) {
                continue;
            }

            int transfer = Math.min(remaining.getCount(), this.backpackInventory.getMaxStackSize(existing) - existing.getCount());
            if (transfer > 0) {
                remaining.shrink(transfer);
            }
        }

        for (int slot = 0; slot < this.backpackInventory.getContainerSize() && !remaining.isEmpty(); slot++) {
            ItemStack existing = this.backpackInventory.getItem(slot);
            if (existing.isEmpty()
                    && ItemLockProtection.canPlayerInsertIntoSlot(this.backpackInventory.getBackpackStack(), slot, existing, remaining)) {
                remaining.shrink(Math.min(remaining.getCount(), this.backpackInventory.getMaxStackSize(remaining)));
            }
        }
        return remaining.isEmpty();
    }

    private void insertIntoBackpackStorage(ItemStack stack) {
        this.moveItemStackToBackpackStorage(stack);
    }

    private boolean wouldRemovingStorageUpgradeLoseItems(int removingUpgradeSlot) {
        ItemStack backpack = this.backpackInventory.getBackpackStack();
        if (!(backpack.getItem() instanceof BackpackItem)) {
            return false;
        }

        ItemStack simulatedBackpack = backpack.copy();
        net.minecraft.core.NonNullList<ItemStack> simulatedUpgrades = BackpackStackData.loadUpgrades(simulatedBackpack);
        if (removingUpgradeSlot < 0 || removingUpgradeSlot >= simulatedUpgrades.size()) {
            return false;
        }

        simulatedUpgrades.set(removingUpgradeSlot, ItemStack.EMPTY);
        BackpackStackData.saveUpgrades(simulatedBackpack, simulatedUpgrades);
        for (int slot = 0; slot < this.backpackInventory.getContainerSize(); slot++) {
            ItemStack stored = this.backpackInventory.getItem(slot);
            if (!stored.isEmpty() && stored.getCount() > BackpackStackData.getStorageStackLimit(simulatedBackpack, stored)) {
                return true;
            }
        }
        return false;
    }

    private class BackpackSlot extends Slot {
        private final int visibleRow;
        private final int column;
        private boolean visible = true;

        BackpackSlot(BackpackInventory inventory, int slot, int visibleRow, int column) {
            super(inventory, slot,
                    BackpackScreenLayout.BACKPACK_SLOT_X + column * BackpackScreenLayout.SLOT_SIZE,
                    BackpackScreenLayout.BACKPACK_SLOT_Y + visibleRow * BackpackScreenLayout.SLOT_SIZE);
            this.visibleRow = visibleRow;
            this.column = column;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return this.hasMappedSlot()
                    && this.mayStore(stack)
                    && ItemLockProtection.canPlayerInsertIntoSlot(BackpackMenu.this.backpackInventory.getBackpackStack(),
                    this.getMappedSlot(), this.getItem(), stack);
        }

        @Override
        public boolean isActive() {
            return this.visible && this.hasMappedSlot();
        }

        void setVisible(boolean visible) {
            this.visible = visible;
        }

        @Override
        public int getContainerSlot() {
            return this.getMappedSlot();
        }

        @Override
        public ItemStack getItem() {
            return this.hasMappedSlot() ? BackpackMenu.this.backpackInventory.getItem(this.getMappedSlot()) : ItemStack.EMPTY;
        }

        @Override
        public boolean hasItem() {
            return !this.getItem().isEmpty();
        }

        @Override
        public void set(ItemStack stack) {
            if (!this.hasMappedSlot()) {
                return;
            }

            int mappedSlot = this.getMappedSlot();
            if (BackpackMenu.this.owner.level().isClientSide()) {
                BackpackMenu.this.backpackInventory.setItemFromNetwork(mappedSlot, stack);
                return;
            }
            if (ItemLockProtection.canPlayerInsertIntoSlot(BackpackMenu.this.backpackInventory.getBackpackStack(),
                    mappedSlot, BackpackMenu.this.backpackInventory.getItem(mappedSlot), stack)) {
                BackpackMenu.this.backpackInventory.setItem(mappedSlot, stack);
                this.setChanged();
            }
        }

        @Override
        public ItemStack remove(int amount) {
            if (!this.hasMappedSlot()) {
                return ItemStack.EMPTY;
            }

            int mappedSlot = this.getMappedSlot();
            ItemStack stack = BackpackMenu.this.backpackInventory.getItem(mappedSlot);
            return ItemLockProtection.canPlayerTakeFromSlot(BackpackMenu.this.backpackInventory.getBackpackStack(), mappedSlot, stack)
                    ? BackpackMenu.this.backpackInventory.removeItem(mappedSlot, amount)
                    : ItemStack.EMPTY;
        }

        @Override
        public boolean mayPickup(Player player) {
            if (!this.hasMappedSlot() || !super.mayPickup(player)) {
                return false;
            }

            int mappedSlot = this.getMappedSlot();
            return ItemLockProtection.canPlayerTakeFromSlot(BackpackMenu.this.backpackInventory.getBackpackStack(),
                    mappedSlot, BackpackMenu.this.backpackInventory.getItem(mappedSlot));
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return BackpackMenu.this.backpackInventory.getMaxStackSize(stack);
        }

        @Override
        public int getMaxStackSize() {
            return BackpackMenu.this.backpackInventory.getMaxStackSize();
        }

        private boolean mayStore(ItemStack stack) {
            return BackpackStackData.isValidStorageItem(BackpackMenu.this.backpackInventory.getBackpackStack(), stack);
        }

        private boolean hasMappedSlot() {
            return this.getMappedSlot() < BackpackMenu.this.tier.getSlotCount();
        }

        private int getMappedSlot() {
            return (BackpackMenu.this.firstVisibleRow + this.visibleRow) * 9 + this.column;
        }
    }

    private class UpgradeSlot extends Slot {
        UpgradeSlot(BackpackUpgradeInventory inventory, int slot, int x, int y) {
            super(inventory, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (stack.getItem() instanceof TrashCanUpgradeItem && !SmartBackpacksConfig.allowMultipleTrashCanUpgrades()) {
                for (int slot = 0; slot < BackpackMenu.this.backpackUpgradeInventory.getContainerSize(); slot++) {
                    if (slot != this.getContainerSlot()
                            && BackpackMenu.this.backpackUpgradeInventory.getItem(slot).getItem() instanceof TrashCanUpgradeItem) {
                        return false;
                    }
                }
            }
            if (stack.getItem() instanceof RescueUpgradeItem && !SmartBackpacksConfig.allowMultipleRescueUpgrades()) {
                for (int slot = 0; slot < BackpackMenu.this.backpackUpgradeInventory.getContainerSize(); slot++) {
                    if (slot != this.getContainerSlot()
                            && BackpackMenu.this.backpackUpgradeInventory.getItem(slot).getItem() instanceof RescueUpgradeItem) {
                        return false;
                    }
                }
            }
            if (stack.getItem() instanceof BuilderUpgradeItem && !SmartBackpacksConfig.builderAllowMultipleUpgrades()) {
                for (int slot = 0; slot < BackpackMenu.this.backpackUpgradeInventory.getContainerSize(); slot++) {
                    if (slot != this.getContainerSlot()
                            && BackpackMenu.this.backpackUpgradeInventory.getItem(slot).getItem() instanceof BuilderUpgradeItem) {
                        return false;
                    }
                }
            }
            if (stack.getItem() instanceof QuiverUpgradeItem && !SmartBackpacksConfig.allowMultipleQuiverUpgrades()) {
                for (int slot = 0; slot < BackpackMenu.this.backpackUpgradeInventory.getContainerSize(); slot++) {
                    if (slot != this.getContainerSlot()
                            && BackpackMenu.this.backpackUpgradeInventory.getItem(slot).getItem() instanceof QuiverUpgradeItem) {
                        return false;
                    }
                }
            }
            if (stack.getItem() instanceof TorchPlacerUpgradeItem && !SmartBackpacksConfig.torchPlacerAllowMultipleUpgrades()) {
                for (int slot = 0; slot < BackpackMenu.this.backpackUpgradeInventory.getContainerSize(); slot++) {
                    if (slot != this.getContainerSlot()
                            && BackpackMenu.this.backpackUpgradeInventory.getItem(slot).getItem() instanceof TorchPlacerUpgradeItem) {
                        return false;
                    }
                }
            }
            if (stack.getItem() instanceof CapacityWarningUpgradeItem && !SmartBackpacksConfig.capacityWarningAllowMultipleUpgrades()) {
                for (int slot = 0; slot < BackpackMenu.this.backpackUpgradeInventory.getContainerSize(); slot++) {
                    if (slot != this.getContainerSlot()
                            && BackpackMenu.this.backpackUpgradeInventory.getItem(slot).getItem() instanceof CapacityWarningUpgradeItem) {
                        if (!BackpackMenu.this.owner.level().isClientSide()) {
                            PlayerMessageHelper.sendStatus(BackpackMenu.this.owner, Component.translatable("message.smartbackpacks.capacity_warning_duplicate"));
                        }
                        return false;
                    }
                }
            }
            if (stack.getItem() instanceof ItemLockUpgradeItem && !SmartBackpacksConfig.itemLockAllowMultipleUpgrades()) {
                for (int slot = 0; slot < BackpackMenu.this.backpackUpgradeInventory.getContainerSize(); slot++) {
                    if (slot != this.getContainerSlot()
                            && BackpackMenu.this.backpackUpgradeInventory.getItem(slot).getItem() instanceof ItemLockUpgradeItem) {
                        if (!BackpackMenu.this.owner.level().isClientSide()) {
                            PlayerMessageHelper.sendStatus(BackpackMenu.this.owner, Component.translatable("message.smartbackpacks.item_lock_duplicate"));
                        }
                        return false;
                    }
                }
            }
            if (stack.getItem() instanceof BackpackLinkUpgradeItem && !SmartBackpacksConfig.backpackLinkAllowMultipleUpgrades()) {
                for (int slot = 0; slot < BackpackMenu.this.backpackUpgradeInventory.getContainerSize(); slot++) {
                    if (slot != this.getContainerSlot()
                            && BackpackMenu.this.backpackUpgradeInventory.getItem(slot).getItem() instanceof BackpackLinkUpgradeItem) {
                        if (!BackpackMenu.this.owner.level().isClientSide()) {
                            PlayerMessageHelper.sendStatus(BackpackMenu.this.owner, Component.translatable("message.smartbackpacks.backpack_link_duplicate"));
                        }
                        return false;
                    }
                }
            }
            return stack.getItem() instanceof BackpackUpgradeItem;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean mayPickup(Player player) {
            ItemStack stack = this.getItem();
            if (stack.getItem() instanceof QuiverUpgradeItem
                    && stack.getOrDefault(ModDataComponents.QUIVER_UPGRADE_DATA.get(), QuiverUpgradeData.DEFAULT).hasAnyProjectiles()) {
                if (!player.level().isClientSide()) {
                    PlayerMessageHelper.sendStatus(player, net.minecraft.network.chat.Component.translatable("message.smartbackpacks.quiver_remove_blocked"));
                }
                return false;
            }
            if (stack.getItem() instanceof TrashCanUpgradeItem
                    && (stack.getOrDefault(ModDataComponents.TRASH_CAN_UPGRADE_DATA.get(), TrashCanUpgradeData.DEFAULT).hasPendingItem()
                    || stack.getOrDefault(ModDataComponents.TRASH_CAN_UPGRADE_DATA.get(), TrashCanUpgradeData.DEFAULT).hasLastDeletedItem())) {
                if (!player.level().isClientSide()) {
                    PlayerMessageHelper.sendStatus(player, Component.translatable("message.smartbackpacks.trash_can_remove_blocked"));
                }
                return false;
            }
            if (stack.getItem() instanceof RescueUpgradeItem
                    && stack.getOrDefault(ModDataComponents.RESCUE_UPGRADE_DATA.get(), RescueUpgradeData.DEFAULT).hasAnyItems()) {
                if (!player.level().isClientSide()) {
                    PlayerMessageHelper.sendStatus(player, Component.translatable("message.smartbackpacks.rescue_remove_blocked"));
                }
                return false;
            }
            if (stack.getItem() instanceof StorageUpgradeItem
                    && BackpackMenu.this.wouldRemovingStorageUpgradeLoseItems(this.getContainerSlot())) {
                if (!player.level().isClientSide()) {
                    PlayerMessageHelper.sendStatus(player, Component.translatable("message.smartbackpacks.storage_upgrade_remove_blocked"));
                }
                return false;
            }
            if (stack.getItem() instanceof BackpackLinkUpgradeItem) {
                BackpackLinkData data = BackpackMenu.this.backpackInventory.getBackpackStack()
                        .getOrDefault(ModDataComponents.BACKPACK_LINK_DATA.get(), BackpackLinkData.EMPTY);
                if (data.active() || !data.links().isEmpty()) {
                    if (!player.level().isClientSide()) {
                        PlayerMessageHelper.sendStatus(player, Component.translatable("message.smartbackpacks.backpack_link_remove_blocked"));
                    }
                    return false;
                }
            }
            return super.mayPickup(player);
        }
    }

    private class TrashSlot extends Slot {
        TrashSlot(int x, int y) {
            super(new SimpleContainer(1), 0, x, y);
        }

        @Override
        public boolean isActive() {
            return BackpackMenu.this.hasTrashCanUpgrade();
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return this.isActive()
                    && BackpackMenu.this.getTrashCanData().enabled()
                    && !BackpackMenu.this.getTrashCanData().hasPendingItem()
                    && ItemLockProtection.canTrashStack(BackpackMenu.this.backpackInventory.getBackpackStack(), -1, stack)
                    && TrashCanUpgradeHandler.canEnterTrash(stack, BackpackMenu.this.backpackInventory.getBackpackStack());
        }

        @Override
        public ItemStack getItem() {
            return this.isActive() ? BackpackMenu.this.getTrashCanData().loadPendingItem() : ItemStack.EMPTY;
        }

        @Override
        public boolean hasItem() {
            return !this.getItem().isEmpty();
        }

        @Override
        public void set(ItemStack stack) {
            if (!this.isActive()) {
                return;
            }

            TrashCanUpgradeData data = BackpackMenu.this.getTrashCanData();
            if (stack.isEmpty()) {
                BackpackMenu.this.setTrashCanData(data.clearPendingItem());
                return;
            }

            BackpackMenu.this.setTrashCanData(data.withPendingItem(stack.copy(), TrashCanUpgradeHandler.deletionDelayTicks()));
        }

        @Override
        public ItemStack remove(int amount) {
            ItemStack current = this.getItem();
            if (current.isEmpty()) {
                return ItemStack.EMPTY;
            }

            ItemStack removed = current.split(amount);
            TrashCanUpgradeData data = BackpackMenu.this.getTrashCanData();
            if (current.isEmpty()) {
                BackpackMenu.this.setTrashCanData(data.clearPendingItem());
            } else {
                BackpackMenu.this.setTrashCanData(data.withPendingItem(current, TrashCanUpgradeHandler.deletionDelayTicks()));
            }
            return removed;
        }

        @Override
        public int getMaxStackSize() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return Integer.MAX_VALUE;
        }
    }

    private static class PlayerInventorySlot extends Slot {
        private final int lockedInventorySlot;

        PlayerInventorySlot(Inventory inventory, int slot, int x, int y, int lockedInventorySlot) {
            super(inventory, slot, x, y);
            this.lockedInventorySlot = lockedInventorySlot;
        }

        boolean isLocked() {
            return this.getContainerSlot() == this.lockedInventorySlot;
        }

        @Override
        public boolean mayPickup(Player player) {
            return !this.isLocked() && super.mayPickup(player);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !this.isLocked() && super.mayPlace(stack);
        }
    }
}

