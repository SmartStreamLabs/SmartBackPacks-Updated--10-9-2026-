package com.teamsmartstreamlabs.smartbackpacks.menu;


import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import java.util.ArrayList;
import java.util.List;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackUpgradeInventory;
import com.teamsmartstreamlabs.smartbackpacks.item.SurvivalAssistUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.SurvivalAssistUpgradeData;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.component.ItemContainerContents;

public class SurvivalAssistUpgradeMenu extends AbstractContainerMenu {
    private final BackpackUpgradeInventory upgradeInventory;
    private final int upgradeSlotIndex;
    private final SimpleContainer filterItems = new SimpleContainer(SurvivalAssistUpgradeData.FILTER_SLOT_COUNT);
    private List<ResourceLocation> effectFilters = new ArrayList<>();

    public SurvivalAssistUpgradeMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, BackpackAccess.fromNetwork(playerInventory.player, buffer), buffer.readVarInt());
    }

    public SurvivalAssistUpgradeMenu(int containerId, Inventory playerInventory, BackpackAccess access, int upgradeSlotIndex) {
        super(ModMenuTypes.SURVIVAL_ASSIST_UPGRADE.get(), containerId);
        this.upgradeInventory = new BackpackUpgradeInventory(playerInventory.player, access);
        this.upgradeSlotIndex = upgradeSlotIndex;
        this.loadUpgradeData();
        this.addFilterSlots();
        this.addPlayerInventory(playerInventory);
    }

    public List<ResourceLocation> getEffectFilters() {
        return List.copyOf(this.effectFilters);
    }

    public void addEffectFilter(ResourceLocation effectId) {
        if (this.effectFilters.contains(effectId) || !BuiltInRegistries.MOB_EFFECT.containsKey(effectId)) {
            return;
        }

        this.effectFilters = new ArrayList<>(this.effectFilters);
        this.effectFilters.add(effectId);
        this.saveUpgradeData();
    }

    public void removeEffectFilter(int index) {
        if (index < 0 || index >= this.effectFilters.size()) {
            return;
        }

        this.effectFilters = new ArrayList<>(this.effectFilters);
        this.effectFilters.remove(index);
        this.saveUpgradeData();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < SurvivalAssistUpgradeData.FILTER_SLOT_COUNT && clickType == ClickType.PICKUP) {
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
    public boolean stillValid(Player player) {
        return this.upgradeInventory.stillValid(player) && this.getUpgradeStack().getItem() instanceof SurvivalAssistUpgradeItem;
    }

    private void addFilterSlots() {
        for (int slot = 0; slot < SurvivalAssistUpgradeData.FILTER_SLOT_COUNT; slot++) {
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
        SurvivalAssistUpgradeData data = com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.getOrDefault(this.getUpgradeStack(), ModDataComponents.SURVIVAL_ASSIST_UPGRADE_DATA.get(), SurvivalAssistUpgradeData.DEFAULT);
        NonNullList<ItemStack> filterStacks = NonNullList.withSize(SurvivalAssistUpgradeData.FILTER_SLOT_COUNT, ItemStack.EMPTY);
        data.itemFilters().copyInto(filterStacks);
        for (int slot = 0; slot < SurvivalAssistUpgradeData.FILTER_SLOT_COUNT; slot++) {
            this.filterItems.setItem(slot, filterStacks.get(slot));
        }
        this.effectFilters = new ArrayList<>(data.effectFilters());
    }

    private void saveUpgradeData() {
        ItemStack upgradeStack = this.getUpgradeStack();
        if (!(upgradeStack.getItem() instanceof SurvivalAssistUpgradeItem)) {
            return;
        }

        NonNullList<ItemStack> filterStacks = NonNullList.withSize(SurvivalAssistUpgradeData.FILTER_SLOT_COUNT, ItemStack.EMPTY);
        for (int slot = 0; slot < SurvivalAssistUpgradeData.FILTER_SLOT_COUNT; slot++) {
            filterStacks.set(slot, this.filterItems.getItem(slot));
        }

        com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.set(upgradeStack, ModDataComponents.SURVIVAL_ASSIST_UPGRADE_DATA.get(),
                new SurvivalAssistUpgradeData(ItemContainerContents.fromItems(filterStacks), List.copyOf(this.effectFilters)));
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


