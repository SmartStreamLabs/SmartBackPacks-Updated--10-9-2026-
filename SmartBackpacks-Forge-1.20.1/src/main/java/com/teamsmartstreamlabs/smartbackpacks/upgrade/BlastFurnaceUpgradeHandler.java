package com.teamsmartstreamlabs.smartbackpacks.upgrade;


import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BlastFurnaceUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.menu.PortableBlastFurnaceMenu;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class BlastFurnaceUpgradeHandler {
    private BlastFurnaceUpgradeHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }

        if (player.containerMenu instanceof PortableBlastFurnaceMenu menu) {
            menu.tickServer(player.level());
            return;
        }

        for (int slot = 0; slot < 36; slot++) {
            tickBlastFurnacesInBackpack(player.getInventory().getItem(slot), player.level(), backpack -> player.getInventory().setChanged());
        }

        tickBlastFurnacesInBackpack(player.getOffhandItem(), player.level(), backpack -> player.getInventory().setChanged());
        tickBlastFurnacesInBackpack(player.getItemBySlot(EquipmentSlot.CHEST), player.level(), backpack -> player.getInventory().setChanged());

        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            int curioSlot = slot;
            tickBlastFurnacesInBackpack(CuriosCompat.getBackStack(player, curioSlot), player.level(),
                    backpack -> CuriosCompat.setBackStack(player, curioSlot, backpack));
        }
    }

    private static void tickBlastFurnacesInBackpack(ItemStack backpack, net.minecraft.world.level.Level level, Consumer<ItemStack> saver) {
        if (!(backpack.getItem() instanceof BackpackItem)) {
            return;
        }

        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        boolean changed = false;
        for (int slot = 0; slot < upgrades.size(); slot++) {
            ItemStack upgrade = upgrades.get(slot);
            if (!(upgrade.getItem() instanceof BlastFurnaceUpgradeItem)) {
                continue;
            }

            BlastFurnaceUpgradeData current = com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.getOrDefault(upgrade, ModDataComponents.BLAST_FURNACE_UPGRADE_DATA.get(), BlastFurnaceUpgradeData.DEFAULT);
            BlastFurnaceUpgradeData updated = BlastFurnaceUpgradeLogic.tick(level, current);
            if (!updated.equals(current)) {
                com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.set(upgrade, ModDataComponents.BLAST_FURNACE_UPGRADE_DATA.get(), updated);
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

