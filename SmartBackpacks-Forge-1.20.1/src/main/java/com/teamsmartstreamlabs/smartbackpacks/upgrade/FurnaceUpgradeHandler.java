package com.teamsmartstreamlabs.smartbackpacks.upgrade;


import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.FurnaceUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.menu.PortableFurnaceMenu;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class FurnaceUpgradeHandler {
    private FurnaceUpgradeHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }

        if (player.containerMenu instanceof PortableFurnaceMenu menu) {
            menu.tickServer(player.level());
            return;
        }

        for (int slot = 0; slot < 36; slot++) {
            tickFurnacesInBackpack(player.getInventory().getItem(slot), player.level(), backpack -> player.getInventory().setChanged());
        }

        tickFurnacesInBackpack(player.getOffhandItem(), player.level(), backpack -> player.getInventory().setChanged());
        tickFurnacesInBackpack(player.getItemBySlot(EquipmentSlot.CHEST), player.level(), backpack -> player.getInventory().setChanged());

        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            int curioSlot = slot;
            tickFurnacesInBackpack(CuriosCompat.getBackStack(player, curioSlot), player.level(),
                    backpack -> CuriosCompat.setBackStack(player, curioSlot, backpack));
        }
    }

    private static void tickFurnacesInBackpack(ItemStack backpack, net.minecraft.world.level.Level level, Consumer<ItemStack> saver) {
        if (!(backpack.getItem() instanceof BackpackItem)) {
            return;
        }

        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        boolean changed = false;
        for (int slot = 0; slot < upgrades.size(); slot++) {
            ItemStack upgrade = upgrades.get(slot);
            if (!(upgrade.getItem() instanceof FurnaceUpgradeItem)) {
                continue;
            }

            FurnaceUpgradeData current = com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.getOrDefault(upgrade, ModDataComponents.FURNACE_UPGRADE_DATA.get(), FurnaceUpgradeData.DEFAULT);
            FurnaceUpgradeData updated = FurnaceUpgradeLogic.tick(level, current);
            if (!updated.equals(current)) {
                com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.set(upgrade, ModDataComponents.FURNACE_UPGRADE_DATA.get(), updated);
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

