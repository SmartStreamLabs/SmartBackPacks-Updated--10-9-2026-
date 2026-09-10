package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.CompressionUpgradeItem;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class CompressionUpgradeHandler {
    private CompressionUpgradeHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || player.tickCount % 10 != 0) {
            return;
        }

        for (int slot = 0; slot < 36; slot++) {
            tickCompressionInBackpack(player.getInventory().getItem(slot), player.level(), backpack -> player.getInventory().setChanged());
        }

        tickCompressionInBackpack(player.getOffhandItem(), player.level(), backpack -> player.getInventory().setChanged());
        tickCompressionInBackpack(player.getItemBySlot(EquipmentSlot.CHEST), player.level(), backpack -> player.getInventory().setChanged());

        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            int curioSlot = slot;
            tickCompressionInBackpack(CuriosCompat.getBackStack(player, curioSlot), player.level(),
                    backpack -> CuriosCompat.setBackStack(player, curioSlot, backpack));
        }
    }

    public static void tickCompressionInBackpack(ItemStack backpack, net.minecraft.world.level.Level level, Consumer<ItemStack> saver) {
        if (!(backpack.getItem() instanceof BackpackItem backpackItem)) {
            return;
        }

        boolean hasUpgrade = false;
        for (ItemStack upgrade : com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData.loadUpgrades(backpack)) {
            if (upgrade.getItem() instanceof CompressionUpgradeItem) {
                hasUpgrade = true;
                break;
            }
        }

        if (hasUpgrade && CompressionUpgradeLogic.tick(level, backpack, backpackItem.getTier())) {
            saver.accept(backpack);
        }
    }
}
