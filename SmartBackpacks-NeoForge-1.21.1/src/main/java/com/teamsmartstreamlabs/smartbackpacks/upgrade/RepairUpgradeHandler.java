package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.RepairUpgradeItem;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class RepairUpgradeHandler {
    private static final Map<UUID, Map<String, RepairState>> STATES = new HashMap<>();

    private RepairUpgradeHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()
                || player.tickCount % SmartBackpacksConfig.repairUpgradeTicksPerRepair() != 0) {
            return;
        }
        if (!SmartBackpacksConfig.repairUpgradeEnabled()) {
            STATES.remove(player.getUUID());
            return;
        }

        Map<String, RepairState> states = STATES.computeIfAbsent(player.getUUID(), id -> new HashMap<>());
        Set<String> active = new HashSet<>();
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof BackpackItem item) {
                process(player, "inventory:" + slot, BackpackAccess.inventory(slot, item.getTier()), states, active);
            }
        }
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof BackpackItem item) {
            process(player, "offhand", BackpackAccess.fromHand(player, InteractionHand.OFF_HAND, item.getTier()), states, active);
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof BackpackItem item) {
            process(player, "chest", BackpackAccess.chest(item.getTier()), states, active);
        }
        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            ItemStack stack = CuriosCompat.getBackStack(player, slot);
            if (stack.getItem() instanceof BackpackItem item) {
                process(player, "back:" + slot, BackpackAccess.curioBack(slot, item.getTier()), states, active);
            }
        }
        states.keySet().retainAll(active);
        if (states.isEmpty()) {
            STATES.remove(player.getUUID());
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            STATES.remove(player.getUUID());
        }
    }

    private static void process(ServerPlayer player, String key, BackpackAccess access,
            Map<String, RepairState> states, Set<String> active) {
        ItemStack backpack = access.getBackpackStack(player);
        if (BackpackStackData.loadUpgrades(backpack).stream()
                .noneMatch(upgrade -> upgrade.getItem() instanceof RepairUpgradeItem)) {
            return;
        }
        active.add(key);
        RepairState state = states.get(key);
        if (state == null || state.backpack != backpack) {
            state = new RepairState(backpack);
            states.put(key, state);
        }
        if (player.totalExperience <= 0) {
            return;
        }

        BackpackItem item = (BackpackItem) backpack.getItem();
        NonNullList<ItemStack> contents = BackpackStackData.loadStorage(backpack, item.getTier());
        long totalDamage = 0;
        for (ItemStack stack : contents) {
            if (stack.isDamageableItem() && stack.getDamageValue() > 0) {
                totalDamage += (long) stack.getDamageValue() * stack.getCount();
            }
        }
        if (totalDamage == 0) {
            state.pending = 0;
            return;
        }

        int perXp = SmartBackpacksConfig.repairUpgradeDurabilityPerXp();
        state.pending = (int) Math.min(Integer.MAX_VALUE,
                (long) state.pending + SmartBackpacksConfig.repairUpgradeDurabilityPerCycle());
        long affordable = (long) player.totalExperience * perXp;
        int budget = (int) Math.min(Math.min((long) state.pending, affordable), totalDamage);
        if (budget < Math.min(perXp, totalDamage)) {
            return;
        }

        int repaired = 0;
        boolean fullyRepaired = false;
        int startSlot = state.nextSlot;
        for (int offset = 0; offset < contents.size() && repaired < budget; offset++) {
            int slot = (startSlot + offset) % contents.size();
            ItemStack stack = contents.get(slot);
            if (!stack.isDamageableItem() || stack.getDamageValue() <= 0) {
                continue;
            }
            int amount = Math.min(stack.getDamageValue(), (budget - repaired) / stack.getCount());
            if (amount == 0) {
                continue;
            }
            stack.setDamageValue(stack.getDamageValue() - amount);
            if (stack.getDamageValue() == 0) fullyRepaired = true;
            repaired += amount * stack.getCount();
            state.nextSlot = (slot + 1) % contents.size();
        }
        if (repaired == 0) {
            return;
        }

        BackpackStackData.saveStorage(backpack, contents);
        access.setBackpackStack(player, backpack);
        player.giveExperiencePoints(-(int) (((long) repaired + perXp - 1) / perXp));
        state.pending -= repaired;
        com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.add(player, "repair_durability_restored", repaired);
        if (fullyRepaired) com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.grant(player, "good_as_new");
    }

    private static final class RepairState {
        private final ItemStack backpack;
        private int nextSlot;
        private int pending;

        private RepairState(ItemStack backpack) {
            this.backpack = backpack;
        }
    }
}
