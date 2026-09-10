package com.teamsmartstreamlabs.smartbackpacks.upgrade;


import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.SmokerUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.menu.PortableSmokerMenu;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class SmokerUpgradeHandler {
    private SmokerUpgradeHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }

        if (player.containerMenu instanceof PortableSmokerMenu menu) {
            menu.tickServer(player.level());
            return;
        }

        for (int slot = 0; slot < 36; slot++) {
            tickSmokersInBackpack(player.getInventory().getItem(slot), player.level(), backpack -> player.getInventory().setChanged());
        }

        tickSmokersInBackpack(player.getOffhandItem(), player.level(), backpack -> player.getInventory().setChanged());
        tickSmokersInBackpack(player.getItemBySlot(EquipmentSlot.CHEST), player.level(), backpack -> player.getInventory().setChanged());

        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            int curioSlot = slot;
            tickSmokersInBackpack(CuriosCompat.getBackStack(player, curioSlot), player.level(),
                    backpack -> CuriosCompat.setBackStack(player, curioSlot, backpack));
        }
    }

    private static void tickSmokersInBackpack(ItemStack backpack, net.minecraft.world.level.Level level, Consumer<ItemStack> saver) {
        if (!(backpack.getItem() instanceof BackpackItem)) {
            return;
        }

        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        boolean changed = false;
        for (int slot = 0; slot < upgrades.size(); slot++) {
            ItemStack upgrade = upgrades.get(slot);
            if (!(upgrade.getItem() instanceof SmokerUpgradeItem)) {
                continue;
            }

            SmokerUpgradeData current = com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.getOrDefault(upgrade, ModDataComponents.SMOKER_UPGRADE_DATA.get(), SmokerUpgradeData.DEFAULT);
            SmokerUpgradeData updated = SmokerUpgradeLogic.tick(level, current);
            if (!updated.equals(current)) {
                com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.set(upgrade, ModDataComponents.SMOKER_UPGRADE_DATA.get(), updated);
                upgrades.set(slot, upgrade);
                changed = true;
            }
        }

        if (changed) {
            BackpackStackData.saveUpgrades(backpack, upgrades);
            saver.accept(backpack);
        }
    }
}

