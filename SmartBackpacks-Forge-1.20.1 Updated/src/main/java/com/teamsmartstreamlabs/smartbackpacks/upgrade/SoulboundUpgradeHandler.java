package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class SoulboundUpgradeHandler {
    private static final Map<UUID, List<ItemStack>> PENDING_SOULBOUND_BACKPACKS = new HashMap<>();

    private SoulboundUpgradeHandler() {
    }

    public static void captureInventoryDrops(Player player, Iterable<? extends List<ItemStack>> compartments) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (serverLevel.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            return;
        }

        List<ItemStack> pendingBackpacks = new ArrayList<>();
        for (List<ItemStack> compartment : compartments) {
            for (int slot = 0; slot < compartment.size(); slot++) {
                ItemStack stack = compartment.get(slot);
                if (!isSoulboundBackpack(stack)) {
                    continue;
                }

                pendingBackpacks.add(createPreservedBackpack(stack));
                compartment.set(slot, ItemStack.EMPTY);
            }
        }

        if (!pendingBackpacks.isEmpty()) {
            PENDING_SOULBOUND_BACKPACKS.put(player.getUUID(), pendingBackpacks);
        }
    }

    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (serverLevel.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            return;
        }

        List<ItemStack> pendingBackpacks = new ArrayList<>();
        event.getDrops().removeIf(drop -> {
            if (!isSoulboundBackpack(drop.getItem())) {
                return false;
            }

            pendingBackpacks.add(createPreservedBackpack(drop.getItem()));
            return true;
        });

        if (!pendingBackpacks.isEmpty()) {
            PENDING_SOULBOUND_BACKPACKS.put(event.getEntity().getUUID(), pendingBackpacks);
        }
    }

    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player == null) {
            return;
        }

        restoreSoulboundBackpacks(player);
    }

    public static void restoreSoulboundBackpacks(Player player) {
        List<ItemStack> soulboundBackpacks = PENDING_SOULBOUND_BACKPACKS.remove(player.getUUID());
        if (soulboundBackpacks == null || soulboundBackpacks.isEmpty()) {
            return;
        }

        for (ItemStack backpack : soulboundBackpacks) {
            if (!player.getInventory().add(backpack.copy())) {
                placeInFirstEmptySlot(player, backpack.copy());
            }
        }

        player.getInventory().setChanged();
    }

    private static void placeInFirstEmptySlot(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static boolean isSoulboundBackpack(ItemStack stack) {
        return stack.getItem() instanceof BackpackItem && BackpackStackData.hasSoulboundUpgrade(stack);
    }

    private static ItemStack createPreservedBackpack(ItemStack backpack) {
        ItemStack preserved = backpack.copy();
        BackpackStackData.consumeSoulboundUpgradeUse(preserved);
        return preserved;
    }
}
