package com.teamsmartstreamlabs.smartbackpacks.menu;

import java.util.ArrayList;
import java.util.List;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackUpgradeInventory;
import com.teamsmartstreamlabs.smartbackpacks.item.DeathEmergencyKitUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.DeathEmergencyKitData;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class DeathEmergencyKitUpgradeMenu extends AbstractContainerMenu {
    public static final int[][] TEMPLATE_POSITIONS = {{52, 34}, {80, 34}, {108, 34}, {52, 56}, {80, 56}, {108, 56}};
    public static final int PLAYER_INVENTORY_Y = 138;
    private final BackpackUpgradeInventory upgradeInventory;
    private final int upgradeSlotIndex;
    private final SimpleContainer templates = new SimpleContainer(DeathEmergencyKitData.SLOT_COUNT);
    private final SimpleContainerData destinations = new SimpleContainerData(DeathEmergencyKitData.SLOT_COUNT);

    public DeathEmergencyKitUpgradeMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, BackpackAccess.fromNetwork(inventory.player, buffer), buffer.readVarInt());
    }

    public DeathEmergencyKitUpgradeMenu(int containerId, Inventory inventory, BackpackAccess access, int upgradeSlotIndex) {
        super(ModMenuTypes.DEATH_EMERGENCY_KIT_UPGRADE.get(), containerId);
        this.upgradeInventory = new BackpackUpgradeInventory(inventory.player, access);
        this.upgradeSlotIndex = upgradeSlotIndex;
        DeathEmergencyKitData data = this.getUpgradeStack().getOrDefault(
                ModDataComponents.DEATH_EMERGENCY_KIT_DATA.get(), DeathEmergencyKitData.DEFAULT);
        NonNullList<ItemStack> loaded = data.loadTemplates();
        for (int slot = 0; slot < DeathEmergencyKitData.SLOT_COUNT; slot++) {
            this.templates.setItem(slot, loaded.get(slot));
            this.destinations.set(slot, data.destination(slot));
            this.addSlot(new GhostSlot(this.templates, slot, TEMPLATE_POSITIONS[slot][0], TEMPLATE_POSITIONS[slot][1]));
        }
        this.addDataSlots(this.destinations);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, PLAYER_INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column, 8 + column * 18, PLAYER_INVENTORY_Y + 58));
        }
    }

    public int amount(int slot) {
        return slot >= 0 && slot < DeathEmergencyKitData.SLOT_COUNT ? this.templates.getItem(slot).getCount() : 0;
    }

    public int destination(int slot) {
        return slot >= 0 && slot < DeathEmergencyKitData.SLOT_COUNT ? this.destinations.get(slot) : -1;
    }

    @Override
    public void clicked(int slotId, int button, ClickType input, Player player) {
        if (slotId >= 0 && slotId < DeathEmergencyKitData.SLOT_COUNT && input == ClickType.PICKUP) {
            ItemStack carried = this.getCarried();
            this.templates.setItem(slotId, button == 1 || carried.isEmpty() ? ItemStack.EMPTY
                    : carried.copyWithCount(Math.min(carried.getCount(), carried.getMaxStackSize())));
            this.save(player);
            return;
        }
        super.clicked(slotId, button, input, player);
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        int slot = button / 3;
        int action = button % 3;
        if (button < 0 || slot >= DeathEmergencyKitData.SLOT_COUNT || !this.stillValid(player)) {
            return false;
        }
        ItemStack template = this.templates.getItem(slot);
        if (action == 0 && !template.isEmpty()) {
            template.setCount(Math.max(1, template.getCount() - 1));
        } else if (action == 1 && !template.isEmpty()) {
            template.setCount(Math.min(template.getMaxStackSize(), template.getCount() + 1));
        } else if (action == 2) {
            this.destinations.set(slot, this.destinations.get(slot) >= 8 ? -1 : this.destinations.get(slot) + 1);
        }
        this.save(player);
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.upgradeInventory.stillValid(player)
                && this.getUpgradeStack().getItem() instanceof DeathEmergencyKitUpgradeItem;
    }

    private void save(Player player) {
        if (player.level().isClientSide() || !this.stillValid(player)) {
            return;
        }
        NonNullList<ItemStack> items = NonNullList.withSize(DeathEmergencyKitData.SLOT_COUNT, ItemStack.EMPTY);
        List<Integer> targets = new ArrayList<>(DeathEmergencyKitData.SLOT_COUNT);
        for (int slot = 0; slot < DeathEmergencyKitData.SLOT_COUNT; slot++) {
            ItemStack template = this.templates.getItem(slot);
            items.set(slot, template.isEmpty() ? ItemStack.EMPTY
                    : template.copyWithCount(Math.min(template.getCount(), template.getMaxStackSize())));
            targets.add(Math.clamp(this.destinations.get(slot), -1, 8));
        }
        ItemStack upgrade = this.getUpgradeStack();
        upgrade.set(ModDataComponents.DEATH_EMERGENCY_KIT_DATA.get(),
                new DeathEmergencyKitData(ItemContainerContents.fromItems(items), List.copyOf(targets)));
        this.upgradeInventory.setItem(this.upgradeSlotIndex, upgrade);
        this.broadcastChanges();
    }

    private ItemStack getUpgradeStack() {
        return this.upgradeSlotIndex >= 0 && this.upgradeSlotIndex < this.upgradeInventory.getContainerSize()
                ? this.upgradeInventory.getItem(this.upgradeSlotIndex) : ItemStack.EMPTY;
    }

    private static final class GhostSlot extends Slot {
        private GhostSlot(SimpleContainer container, int slot, int x, int y) {
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
    }
}
