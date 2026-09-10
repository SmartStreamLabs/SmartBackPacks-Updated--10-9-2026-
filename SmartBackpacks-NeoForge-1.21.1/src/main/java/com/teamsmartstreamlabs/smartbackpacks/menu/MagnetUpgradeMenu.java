package com.teamsmartstreamlabs.smartbackpacks.menu;

import java.util.ArrayList;
import java.util.List;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackUpgradeInventory;
import com.teamsmartstreamlabs.smartbackpacks.item.AutoToolUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.FilterUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.CourierUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CourierDestinationData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CourierUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.item.HopperUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.MagnetUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.PickupUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.VoidUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FilterUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.HopperUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.AutoToolUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.MagnetFilterInputType;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.MagnetUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.PickupUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.VoidUpgradeData;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class MagnetUpgradeMenu extends AbstractContainerMenu {
    private final BackpackAccess access;
    private final BackpackUpgradeInventory upgradeInventory;
    private final int upgradeSlotIndex;
    private final boolean advanced;
    private final int filterSlotCount;
    private final SimpleContainer filterItems;
    private final List<String> modFilters = new ArrayList<>();
    private final List<ResourceLocation> tagFilters = new ArrayList<>();
    private boolean allowlist;
    private MagnetFilterInputType selectedInputType = MagnetFilterInputType.ITEM;
    private boolean matchNbt;
    private boolean matchDamage;
    private boolean matchBackpackContentsOnly;
    private boolean blockModdedItems;
    private boolean onlyWhenFull;

    public MagnetUpgradeMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, BackpackAccess.fromNetwork(playerInventory.player, buffer), buffer.readVarInt());
    }

    public MagnetUpgradeMenu(int containerId, Inventory playerInventory, BackpackAccess access, int upgradeSlotIndex) {
        super(ModMenuTypes.MAGNET_UPGRADE.get(), containerId);
        this.access = access;
        this.upgradeSlotIndex = upgradeSlotIndex;
        this.upgradeInventory = new BackpackUpgradeInventory(playerInventory.player, access);
        this.advanced = this.isVoidUpgrade()
                || this.isAutoToolUpgrade()
                || this.isFilterUpgrade()
                || this.getUpgradeStack().getItem() instanceof MagnetUpgradeItem magnet && magnet.isAdvanced();
        this.filterSlotCount = this.isVoidUpgrade()
                ? VoidUpgradeData.FILTER_SLOT_COUNT
                : (this.isAutoToolUpgrade()
                ? AutoToolUpgradeData.FILTER_SLOT_COUNT
                : (this.isFilterUpgrade()
                ? FilterUpgradeData.FILTER_SLOT_COUNT
                : (this.advanced ? MagnetUpgradeData.ADVANCED_FILTER_SLOT_COUNT : MagnetUpgradeData.REGULAR_FILTER_SLOT_COUNT)));
        this.filterItems = new SimpleContainer(this.filterSlotCount);

        this.loadUpgradeData();
        this.addFilterSlots();
        this.addPlayerInventory(playerInventory);
    }

    public boolean isAdvanced() {
        return this.advanced;
    }

    public boolean isAllowlist() {
        return this.allowlist;
    }

    public MagnetFilterInputType getSelectedInputType() {
        return this.selectedInputType;
    }

    public boolean isMatchNbt() {
        return this.matchNbt;
    }

    public boolean isMatchDamage() {
        return this.matchDamage;
    }

    public boolean isMatchBackpackContentsOnly() {
        return this.matchBackpackContentsOnly;
    }

    public boolean isBlockModdedItems() {
        return this.blockModdedItems;
    }

    public boolean isVoidUpgrade() {
        return this.getUpgradeStack().getItem() instanceof VoidUpgradeItem;
    }

    public boolean isOnlyWhenFull() {
        return this.onlyWhenFull;
    }

    public boolean isFilterUpgrade() {
        return this.getUpgradeStack().getItem() instanceof FilterUpgradeItem;
    }

    public boolean isCourierUpgrade() {
        return this.getUpgradeStack().getItem() instanceof CourierUpgradeItem;
    }

    public CourierDestinationData getCourierDestination() {
        return this.getUpgradeStack().getOrDefault(ModDataComponents.COURIER_DESTINATION_DATA.get(), CourierDestinationData.EMPTY);
    }

    public boolean toggleCourierDestination(net.minecraft.server.level.ServerPlayer player) {
        ItemStack upgrade = this.getUpgradeStack();
        if (!(upgrade.getItem() instanceof CourierUpgradeItem)) {
            return false;
        }

        CourierUpgradeHandler.toggleLinking(player, this.access, this.upgradeSlotIndex, upgrade);
        if (player.containerMenu == this) {
            player.closeContainer();
        }
        return true;
    }

    public boolean isHopperUpgrade() {
        return this.getUpgradeStack().getItem() instanceof HopperUpgradeItem;
    }

    public boolean isAutoToolUpgrade() {
        return this.getUpgradeStack().getItem() instanceof AutoToolUpgradeItem;
    }

    public List<String> getModFilters() {
        return List.copyOf(this.modFilters);
    }

    public List<ResourceLocation> getTagFilters() {
        return List.copyOf(this.tagFilters);
    }

    private void loadUpgradeData() {
        if (this.isPickupUpgrade()) {
            this.loadPickupUpgradeData();
            return;
        }
        if (this.isAutoToolUpgrade()) {
            this.loadAutoToolUpgradeData();
            return;
        }
        if (this.isFilterUpgrade()) {
            this.loadFilterUpgradeData();
            return;
        }
        if (this.isHopperUpgrade()) {
            this.loadHopperUpgradeData();
            return;
        }
        if (this.isVoidUpgrade()) {
            this.loadVoidUpgradeData();
            return;
        }

        MagnetUpgradeData data = this.getUpgradeStack().getOrDefault(ModDataComponents.MAGNET_UPGRADE_DATA.get(), MagnetUpgradeData.DEFAULT);
        NonNullList<ItemStack> items = NonNullList.withSize(this.filterSlotCount, ItemStack.EMPTY);
        data.itemFilters().copyInto(items);
        for (int slot = 0; slot < this.filterSlotCount; slot++) {
            this.filterItems.setItem(slot, items.get(slot));
        }

        this.allowlist = data.allowlist();
        this.modFilters.clear();
        this.modFilters.addAll(data.modFilters());
        this.tagFilters.clear();
        this.tagFilters.addAll(data.tagFilters());
        this.selectedInputType = data.selectedInputType();
        this.matchNbt = this.advanced && data.matchNbt();
        this.matchDamage = this.advanced && data.matchDamage();
        this.matchBackpackContentsOnly = this.advanced && data.matchBackpackContentsOnly();
        this.blockModdedItems = data.blockModdedItems();
        this.onlyWhenFull = false;
    }

    private void loadHopperUpgradeData() {
        HopperUpgradeData data = this.getUpgradeStack().getOrDefault(ModDataComponents.HOPPER_UPGRADE_DATA.get(), HopperUpgradeData.DEFAULT);
        NonNullList<ItemStack> items = NonNullList.withSize(this.filterSlotCount, ItemStack.EMPTY);
        data.itemFilters().copyInto(items);
        for (int slot = 0; slot < this.filterSlotCount; slot++) {
            this.filterItems.setItem(slot, items.get(slot));
        }

        this.allowlist = data.allowlist();
        this.modFilters.clear();
        this.modFilters.addAll(data.modFilters());
        this.tagFilters.clear();
        this.tagFilters.addAll(data.tagFilters());
        this.selectedInputType = MagnetFilterInputType.ITEM;
        this.matchNbt = false;
        this.matchDamage = false;
        this.matchBackpackContentsOnly = false;
        this.blockModdedItems = data.blockModdedItems();
        this.onlyWhenFull = false;
    }

    private void loadFilterUpgradeData() {
        FilterUpgradeData data = this.getUpgradeStack().getOrDefault(ModDataComponents.FILTER_UPGRADE_DATA.get(), FilterUpgradeData.DEFAULT);
        NonNullList<ItemStack> items = NonNullList.withSize(this.filterSlotCount, ItemStack.EMPTY);
        data.itemFilters().copyInto(items);
        for (int slot = 0; slot < this.filterSlotCount; slot++) {
            this.filterItems.setItem(slot, items.get(slot));
        }

        this.allowlist = data.allowlist();
        this.modFilters.clear();
        this.modFilters.addAll(data.modFilters());
        this.tagFilters.clear();
        this.tagFilters.addAll(data.tagFilters());
        this.selectedInputType = data.selectedInputType();
        this.matchNbt = data.matchNbt();
        this.matchDamage = data.matchDamage();
        this.matchBackpackContentsOnly = false;
        this.blockModdedItems = data.blockModdedItems();
        this.onlyWhenFull = false;
    }

    private void loadPickupUpgradeData() {
        PickupUpgradeData data = this.getUpgradeStack().getOrDefault(ModDataComponents.PICKUP_UPGRADE_DATA.get(), PickupUpgradeData.DEFAULT);
        NonNullList<ItemStack> items = NonNullList.withSize(this.filterSlotCount, ItemStack.EMPTY);
        data.itemFilters().copyInto(items);
        for (int slot = 0; slot < this.filterSlotCount; slot++) {
            this.filterItems.setItem(slot, items.get(slot));
        }

        this.allowlist = data.allowlist();
        this.modFilters.clear();
        this.modFilters.addAll(data.modFilters());
        this.tagFilters.clear();
        this.tagFilters.addAll(data.tagFilters());
        this.selectedInputType = MagnetFilterInputType.ITEM;
        this.matchNbt = false;
        this.matchDamage = false;
        this.matchBackpackContentsOnly = false;
        this.blockModdedItems = data.blockModdedItems();
        this.onlyWhenFull = false;
    }

    private void loadAutoToolUpgradeData() {
        AutoToolUpgradeData data = this.getUpgradeStack().getOrDefault(ModDataComponents.AUTO_TOOL_UPGRADE_DATA.get(), AutoToolUpgradeData.DEFAULT);
        NonNullList<ItemStack> items = NonNullList.withSize(this.filterSlotCount, ItemStack.EMPTY);
        data.itemFilters().copyInto(items);
        for (int slot = 0; slot < this.filterSlotCount; slot++) {
            this.filterItems.setItem(slot, items.get(slot));
        }

        this.allowlist = data.allowlist();
        this.modFilters.clear();
        this.modFilters.addAll(data.modFilters());
        this.tagFilters.clear();
        this.tagFilters.addAll(data.tagFilters());
        this.selectedInputType = data.selectedInputType();
        this.matchNbt = data.matchNbt();
        this.matchDamage = data.matchDamage();
        this.matchBackpackContentsOnly = false;
        this.blockModdedItems = data.blockModdedItems();
        this.onlyWhenFull = false;
    }

    private void loadVoidUpgradeData() {
        VoidUpgradeData data = this.getUpgradeStack().getOrDefault(ModDataComponents.VOID_UPGRADE_DATA.get(), VoidUpgradeData.DEFAULT);
        NonNullList<ItemStack> items = NonNullList.withSize(this.filterSlotCount, ItemStack.EMPTY);
        data.itemFilters().copyInto(items);
        for (int slot = 0; slot < this.filterSlotCount; slot++) {
            this.filterItems.setItem(slot, items.get(slot));
        }

        this.allowlist = data.allowlist();
        this.modFilters.clear();
        this.modFilters.addAll(data.modFilters());
        this.tagFilters.clear();
        this.tagFilters.addAll(data.tagFilters());
        this.selectedInputType = data.selectedInputType();
        this.matchNbt = data.matchNbt();
        this.matchDamage = data.matchDamage();
        this.matchBackpackContentsOnly = false;
        this.blockModdedItems = data.blockModdedItems();
        this.onlyWhenFull = data.onlyWhenFull();
    }

    private void addFilterSlots() {
        if (this.advanced) {
            for (int slot = 0; slot < this.filterSlotCount; slot++) {
                int x = 8 + (slot % 4) * 18;
                int y = 18 + (slot / 4) * 18;
                this.addSlot(new GhostFilterSlot(this.filterItems, slot, x, y));
            }
            return;
        }

        for (int slot = 0; slot < this.filterSlotCount; slot++) {
            int x = 8 + (slot % 9) * 18;
            int y = 18 + (slot / 9) * 18;
            this.addSlot(new GhostFilterSlot(this.filterItems, slot, x, y));
        }
    }

    private void addPlayerInventory(Inventory playerInventory) {
        int playerInventoryY = this.advanced ? 170 : 86;

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

    public void setAllowlist(boolean allowlist) {
        this.allowlist = allowlist;
        this.saveUpgradeData();
    }

    public void setSelectedInputType(MagnetFilterInputType selectedInputType) {
        if (this.advanced) {
            this.selectedInputType = selectedInputType;
            this.saveUpgradeData();
        }
    }

    public void setMatchNbt(boolean matchNbt) {
        if (this.advanced) {
            this.matchNbt = matchNbt;
            this.saveUpgradeData();
        }
    }

    public void setMatchDamage(boolean matchDamage) {
        if (this.advanced) {
            this.matchDamage = matchDamage;
            this.saveUpgradeData();
        }
    }

    public void setMatchBackpackContentsOnly(boolean matchBackpackContentsOnly) {
        if (this.advanced) {
            this.matchBackpackContentsOnly = matchBackpackContentsOnly;
            this.saveUpgradeData();
        }
    }

    public void setBlockModdedItems(boolean blockModdedItems) {
        this.blockModdedItems = blockModdedItems;
        this.saveUpgradeData();
    }

    public void setOnlyWhenFull(boolean onlyWhenFull) {
        if (this.isVoidUpgrade()) {
            this.onlyWhenFull = onlyWhenFull;
            this.saveUpgradeData();
        }
    }

    public void addModFilter(String modId) {
        if (this.advanced && !modId.isBlank() && !this.modFilters.contains(modId)) {
            this.modFilters.add(modId);
            this.saveUpgradeData();
        }
    }

    public void removeModFilter(int index) {
        if (this.advanced && index >= 0 && index < this.modFilters.size()) {
            this.modFilters.remove(index);
            this.saveUpgradeData();
        }
    }

    public void addTag(ResourceLocation tagId) {
        if (this.advanced && !this.tagFilters.contains(tagId)) {
            this.tagFilters.add(tagId);
            this.saveUpgradeData();
        }
    }

    public void removeTag(int index) {
        if (this.advanced && index >= 0 && index < this.tagFilters.size()) {
            this.tagFilters.remove(index);
            this.saveUpgradeData();
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < this.filterSlotCount && clickType == ClickType.PICKUP) {
            ItemStack carried = this.getCarried();
            if (carried.isEmpty()) {
                this.filterItems.setItem(slotId, ItemStack.EMPTY);
            } else {
                this.filterItems.setItem(slotId, carried.copyWithCount(1));
            }
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
    public void removed(Player player) {
        super.removed(player);
        this.saveUpgradeData();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.upgradeInventory.stillValid(player)
                && (this.isMagnetUpgrade() || this.isPickupUpgrade() || this.isHopperUpgrade() || this.isFilterUpgrade() || this.isVoidUpgrade() || this.isAutoToolUpgrade());
    }

    private void saveUpgradeData() {
        ItemStack upgradeStack = this.getUpgradeStack();
        if (this.isPickupUpgrade()) {
            this.savePickupUpgradeData(upgradeStack);
            return;
        }
        if (this.isAutoToolUpgrade()) {
            this.saveAutoToolUpgradeData(upgradeStack);
            return;
        }
        if (this.isFilterUpgrade()) {
            this.saveFilterUpgradeData(upgradeStack);
            return;
        }
        if (this.isHopperUpgrade()) {
            this.saveHopperUpgradeData(upgradeStack);
            return;
        }
        if (this.isVoidUpgrade()) {
            this.saveVoidUpgradeData(upgradeStack);
            return;
        }

        if (!(upgradeStack.getItem() instanceof MagnetUpgradeItem)) {
            return;
        }

        NonNullList<ItemStack> filterStacks = NonNullList.withSize(this.filterSlotCount, ItemStack.EMPTY);
        for (int slot = 0; slot < this.filterSlotCount; slot++) {
            filterStacks.set(slot, this.filterItems.getItem(slot));
        }

        MagnetUpgradeData existing = upgradeStack.getOrDefault(ModDataComponents.MAGNET_UPGRADE_DATA.get(), MagnetUpgradeData.DEFAULT);
        upgradeStack.set(ModDataComponents.MAGNET_UPGRADE_DATA.get(),
                new MagnetUpgradeData(
                        existing.enabled(),
                        this.allowlist,
                        ItemContainerContents.fromItems(filterStacks),
                        this.modFilters,
                        this.tagFilters,
                        this.selectedInputType,
                        this.matchNbt,
                        this.matchDamage,
                        this.matchBackpackContentsOnly,
                        this.blockModdedItems));
        this.upgradeInventory.setItem(this.upgradeSlotIndex, upgradeStack);
        this.broadcastChanges();
    }

    private void saveVoidUpgradeData(ItemStack upgradeStack) {
        if (!(upgradeStack.getItem() instanceof VoidUpgradeItem)) {
            return;
        }

        NonNullList<ItemStack> filterStacks = NonNullList.withSize(this.filterSlotCount, ItemStack.EMPTY);
        for (int slot = 0; slot < this.filterSlotCount; slot++) {
            filterStacks.set(slot, this.filterItems.getItem(slot));
        }

        VoidUpgradeData existing = upgradeStack.getOrDefault(ModDataComponents.VOID_UPGRADE_DATA.get(), VoidUpgradeData.DEFAULT);
        upgradeStack.set(ModDataComponents.VOID_UPGRADE_DATA.get(),
                new VoidUpgradeData(
                        existing.enabled(),
                        this.allowlist,
                        ItemContainerContents.fromItems(filterStacks),
                        this.modFilters,
                        this.tagFilters,
                        this.selectedInputType,
                        this.matchNbt,
                        this.matchDamage,
                        this.blockModdedItems,
                        this.onlyWhenFull));
        this.upgradeInventory.setItem(this.upgradeSlotIndex, upgradeStack);
        this.broadcastChanges();
    }

    private void savePickupUpgradeData(ItemStack upgradeStack) {
        if (!(upgradeStack.getItem() instanceof PickupUpgradeItem)) {
            return;
        }

        NonNullList<ItemStack> filterStacks = NonNullList.withSize(this.filterSlotCount, ItemStack.EMPTY);
        for (int slot = 0; slot < this.filterSlotCount; slot++) {
            filterStacks.set(slot, this.filterItems.getItem(slot));
        }

        PickupUpgradeData existing = upgradeStack.getOrDefault(ModDataComponents.PICKUP_UPGRADE_DATA.get(), PickupUpgradeData.DEFAULT);
        upgradeStack.set(ModDataComponents.PICKUP_UPGRADE_DATA.get(),
                new PickupUpgradeData(
                        existing.enabled(),
                        this.allowlist,
                        ItemContainerContents.fromItems(filterStacks),
                        this.modFilters,
                        this.tagFilters,
                        this.blockModdedItems));
        this.upgradeInventory.setItem(this.upgradeSlotIndex, upgradeStack);
        this.broadcastChanges();
    }

    private void saveAutoToolUpgradeData(ItemStack upgradeStack) {
        if (!(upgradeStack.getItem() instanceof AutoToolUpgradeItem)) {
            return;
        }

        NonNullList<ItemStack> filterStacks = NonNullList.withSize(this.filterSlotCount, ItemStack.EMPTY);
        for (int slot = 0; slot < this.filterSlotCount; slot++) {
            filterStacks.set(slot, this.filterItems.getItem(slot));
        }

        AutoToolUpgradeData existing = upgradeStack.getOrDefault(ModDataComponents.AUTO_TOOL_UPGRADE_DATA.get(), AutoToolUpgradeData.DEFAULT);
        upgradeStack.set(ModDataComponents.AUTO_TOOL_UPGRADE_DATA.get(),
                new AutoToolUpgradeData(
                        existing.enabled(),
                        this.allowlist,
                        ItemContainerContents.fromItems(filterStacks),
                        this.modFilters,
                        this.tagFilters,
                        this.selectedInputType,
                        this.matchNbt,
                        this.matchDamage,
                        this.blockModdedItems));
        this.upgradeInventory.setItem(this.upgradeSlotIndex, upgradeStack);
        this.broadcastChanges();
    }


    private void saveHopperUpgradeData(ItemStack upgradeStack) {
        if (!(upgradeStack.getItem() instanceof HopperUpgradeItem)) {
            return;
        }

        NonNullList<ItemStack> filterStacks = NonNullList.withSize(this.filterSlotCount, ItemStack.EMPTY);
        for (int slot = 0; slot < this.filterSlotCount; slot++) {
            filterStacks.set(slot, this.filterItems.getItem(slot));
        }

        HopperUpgradeData existing = upgradeStack.getOrDefault(ModDataComponents.HOPPER_UPGRADE_DATA.get(), HopperUpgradeData.DEFAULT);
        upgradeStack.set(ModDataComponents.HOPPER_UPGRADE_DATA.get(),
                new HopperUpgradeData(
                        existing.enabled(),
                        this.allowlist,
                        ItemContainerContents.fromItems(filterStacks),
                        this.modFilters,
                        this.tagFilters,
                        this.blockModdedItems));
        this.upgradeInventory.setItem(this.upgradeSlotIndex, upgradeStack);
        this.broadcastChanges();
    }

    private void saveFilterUpgradeData(ItemStack upgradeStack) {
        if (!(upgradeStack.getItem() instanceof FilterUpgradeItem)) {
            return;
        }

        NonNullList<ItemStack> filterStacks = NonNullList.withSize(this.filterSlotCount, ItemStack.EMPTY);
        for (int slot = 0; slot < this.filterSlotCount; slot++) {
            filterStacks.set(slot, this.filterItems.getItem(slot));
        }

        FilterUpgradeData existing = upgradeStack.getOrDefault(ModDataComponents.FILTER_UPGRADE_DATA.get(), FilterUpgradeData.DEFAULT);
        upgradeStack.set(ModDataComponents.FILTER_UPGRADE_DATA.get(),
                new FilterUpgradeData(
                        existing.enabled(),
                        this.allowlist,
                        ItemContainerContents.fromItems(filterStacks),
                        this.modFilters,
                        this.tagFilters,
                        this.selectedInputType,
                        this.matchNbt,
                        this.matchDamage,
                        this.blockModdedItems));
        this.upgradeInventory.setItem(this.upgradeSlotIndex, upgradeStack);
        this.broadcastChanges();
    }

    private ItemStack getUpgradeStack() {
        return this.upgradeInventory.getItem(this.upgradeSlotIndex);
    }

    private boolean isMagnetUpgrade() {
        return this.getUpgradeStack().getItem() instanceof MagnetUpgradeItem;
    }

    private boolean isPickupUpgrade() {
        return this.getUpgradeStack().getItem() instanceof PickupUpgradeItem;
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

