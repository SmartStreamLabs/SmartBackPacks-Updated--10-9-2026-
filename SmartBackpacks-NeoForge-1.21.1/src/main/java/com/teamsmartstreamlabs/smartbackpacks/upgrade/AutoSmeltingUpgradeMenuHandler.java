package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.AutoSmeltingUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.menu.PortableAutoSmeltingMenu;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class AutoSmeltingUpgradeMenuHandler {
    private AutoSmeltingUpgradeMenuHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }

        if (player.containerMenu instanceof PortableAutoSmeltingMenu menu) {
            menu.tickServer(player.level());
            return;
        }

        for (int slot = 0; slot < 36; slot++) {
            tickAutoSmeltingInBackpack(player, player.getInventory().getItem(slot), player.level(), backpack -> player.getInventory().setChanged());
        }

        tickAutoSmeltingInBackpack(player, player.getOffhandItem(), player.level(), backpack -> player.getInventory().setChanged());
        tickAutoSmeltingInBackpack(player, player.getItemBySlot(EquipmentSlot.CHEST), player.level(), backpack -> player.getInventory().setChanged());

        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            int curioSlot = slot;
            tickAutoSmeltingInBackpack(player, CuriosCompat.getBackStack(player, curioSlot), player.level(),
                    backpack -> CuriosCompat.setBackStack(player, curioSlot, backpack));
        }
    }

    private static void tickAutoSmeltingInBackpack(ServerPlayer player, ItemStack backpack, net.minecraft.world.level.Level level, Consumer<ItemStack> saver) {
        if (!(backpack.getItem() instanceof BackpackItem backpackItem)) {
            return;
        }

        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        boolean changed = false;
        for (int slot = 0; slot < upgrades.size(); slot++) {
            ItemStack upgrade = upgrades.get(slot);
            if (!(upgrade.getItem() instanceof AutoSmeltingUpgradeItem)) {
                continue;
            }

            AutoSmeltingUpgradeData current = upgrade.getOrDefault(ModDataComponents.AUTO_SMELTING_UPGRADE_DATA.get(), AutoSmeltingUpgradeData.DEFAULT);
            AutoSmeltingUpgradeData updated = AutoSmeltingUpgradeLogic.tick(level, backpack, backpackItem.getTier(), current, count ->
                    com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.add(player, "auto_smelts_completed", count));
            if (!updated.equals(current)) {
                upgrade.set(ModDataComponents.AUTO_SMELTING_UPGRADE_DATA.get(), updated);
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
