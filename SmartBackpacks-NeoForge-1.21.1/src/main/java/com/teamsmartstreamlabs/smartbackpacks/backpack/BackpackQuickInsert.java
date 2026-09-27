package com.teamsmartstreamlabs.smartbackpacks.backpack;

import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.FilterUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FilterUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FilterUpgradeMatcher;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.ItemLockProtection;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class BackpackQuickInsert {
    private BackpackQuickInsert() {
    }

    public static boolean handle(AbstractContainerMenu menu, int slotId, int button, ClickType input, Player player) {
        if (input != ClickType.PICKUP || button != 1 || menu != player.inventoryMenu
                || menu != player.containerMenu || slotId < 0 || slotId >= menu.slots.size()
                || menu.getCarried().isEmpty()) {
            return false;
        }

        Slot slot = menu.slots.get(slotId);
        if (slot.container != player.getInventory() || !slot.isActive() || !slot.mayPickup(player)) {
            return false;
        }
        ItemStack backpack = slot.getItem();
        if (!(backpack.getItem() instanceof BackpackItem item)) {
            return false;
        }

        ItemStack carried = menu.getCarried();
        for (ItemStack upgrade : BackpackStackData.loadUpgrades(backpack)) {
            if (upgrade.getItem() instanceof FilterUpgradeItem) {
                FilterUpgradeData filter = upgrade.getOrDefault(ModDataComponents.FILTER_UPGRADE_DATA.get(), FilterUpgradeData.DEFAULT);
                if (filter.enabled() && !FilterUpgradeMatcher.allows(filter, carried)) {
                    return true;
                }
            }
        }

        ItemStack remainder = ItemLockProtection.insertIntoStorage(backpack, item.getTier(), carried, false);
        if (remainder.getCount() != carried.getCount()) {
            int inserted = carried.getCount() - remainder.getCount();
            menu.setCarried(remainder);
            slot.setChanged();
            if (!player.level().isClientSide()) {
                menu.broadcastChanges();
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.recordInsertion(
                            serverPlayer, backpack, item.getTier(), inserted);
                    com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.grant(serverPlayer, "pack_it_up");
                }
            }
        }
        return true;
    }
}
