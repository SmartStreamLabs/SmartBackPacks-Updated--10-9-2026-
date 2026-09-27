package com.teamsmartstreamlabs.smartbackpacks.progress;

import java.util.Map;
import java.util.WeakHashMap;

import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class BackpackProgression {
    private static final Map<ServerPlayer, ResourceKey<Level>> LAST_DIMENSION = new WeakHashMap<>();

    private BackpackProgression() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) return;
        if (player.tickCount % 20 == 0 && !player.isCreative()) {
            for (int slot = 0; slot < 36; slot++) checkFound(player, player.getInventory().getItem(slot));
            checkFound(player, player.getOffhandItem());
            checkFound(player, player.getItemBySlot(EquipmentSlot.CHEST));
            for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) checkFound(player, CuriosCompat.getBackStack(player, slot));
        }
        ResourceKey<Level> previousDimension = LAST_DIMENSION.put(player, player.level().dimension());
        if (previousDimension == null || !previousDimension.equals(player.level().dimension())) return;
        double dx = player.getX() - player.xo;
        double dy = player.getY() - player.yo;
        double dz = player.getZ() - player.zo;
        double moved = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (moved <= 0 || moved > 10.0D || !hasCarriedBackpack(player)) return;
        add(player, "distance_with_backpack", Math.round(moved * 1000.0D));
    }

    private static boolean hasCarriedBackpack(ServerPlayer player) {
        for (int slot = 0; slot < 36; slot++) {
            if (player.getInventory().getItem(slot).getItem() instanceof BackpackItem) return true;
        }
        if (player.getOffhandItem().getItem() instanceof BackpackItem
                || player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof BackpackItem) return true;
        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            if (CuriosCompat.getBackStack(player, slot).getItem() instanceof BackpackItem) return true;
        }
        return false;
    }

    public static void checkFound(ServerPlayer player, ItemStack stack) {
        if (!(stack.getItem() instanceof BackpackItem) || player.isCreative()) return;
        String id = com.teamsmartstreamlabs.smartbackpacks.loot.AbandonedBackpackOrigin.naturalId(stack);
        if (id.isEmpty()) return;
        SmartBackpacksPlayerStats stats = SmartBackpacksPlayerStats.forPlayer(player);
        String key = "abandoned_seen_" + id;
        if (stats.get(player.getUUID(), key) != 0) return;
        stats.add(player.getUUID(), key, 1);
        add(player, "abandoned_backpacks_found", 1);
    }

    public static void onOpened(ServerPlayer player, ItemStack stack) {
        if (!player.isCreative() && !com.teamsmartstreamlabs.smartbackpacks.loot.AbandonedBackpackOrigin.naturalId(stack).isEmpty()
                && com.teamsmartstreamlabs.smartbackpacks.loot.AbandonedBackpackOrigin.prefilled(stack)) grant(player, "previous_owner");
    }

    public static void recordInsertion(ServerPlayer player, ItemStack backpack, BackpackTier tier, long count) {
        if (count <= 0 || !(backpack.getItem() instanceof BackpackItem)) return;
        add(player, "items_stored", count);
        var storage = BackpackStackData.loadStorage(backpack, tier);
        checkFull(player, tier, storage);
    }

    public static void checkFull(ServerPlayer player, BackpackTier tier, net.minecraft.core.NonNullList<ItemStack> storage) {
        if (storage.isEmpty() || storage.stream().anyMatch(ItemStack::isEmpty)) return;
        grant(player, "packed_tight");
        if (tier == BackpackTier.ULTIMATE_DIAMOND || tier == BackpackTier.ANCIENT_NETHERITE) {
            grant(player, "ultimate_packing");
        } else if (tier == BackpackTier.NETHERITE_VAULT) {
            grant(player, "vault_keeper");
        }
    }

    public static void add(ServerPlayer player, String counter, long amount) {
        if (amount <= 0) return;
        SmartBackpacksPlayerStats stats = SmartBackpacksPlayerStats.forPlayer(player);
        long previous = stats.get(player.getUUID(), counter);
        long total = stats.add(player.getUUID(), counter, amount);
        switch (counter) {
            case "items_stored" -> thresholds(player, previous, total, new long[] {100, 1000, 10000, 100000, 1000000},
                    new String[] {"getting_organized", "pack_rat", "bottomless_pockets", "portable_warehouse", "where_does_it_all_fit"});
            case "magnet_items_collected" -> thresholds(player, previous, total, new long[] {100, 10000},
                    new String[] {"come_to_me", "item_vacuum"});
            case "repair_durability_restored" -> thresholds(player, previous, total, new long[] {10000},
                    new String[] {"backpack_mechanic"});
            case "soulbound_deaths_survived" -> thresholds(player, previous, total, new long[] {1, 10},
                    new String[] {"bound_beyond_death", "death_cant_take_it"});
            case "fall_protection_saves" -> thresholds(player, previous, total, new long[] {1, 10},
                    new String[] {"not_today_gravity", "gravity_learns_nothing"});
            case "emergency_kits_used" -> thresholds(player, previous, total, new long[] {1, 10},
                    new String[] {"prepared_for_the_worst", "always_prepared"});
            case "auto_feed_items_consumed" -> thresholds(player, previous, total, new long[] {1, 100},
                    new String[] {"packed_lunch", "never_hungry"});
            case "auto_tool_swaps" -> thresholds(player, previous, total, new long[] {1, 100},
                    new String[] {"right_tool_for_the_job", "toolbox_on_your_back"});
            case "auto_smelts_completed" -> thresholds(player, previous, total, new long[] {1, 10000},
                    new String[] {"portable_furnace", "industrial_backpack"});
            case "compression_operations" -> thresholds(player, previous, total, new long[] {1, 1000},
                    new String[] {"pack_it_down", "space_efficient"});
            case "voided_item_count" -> thresholds(player, previous, total, new long[] {1, 10000},
                    new String[] {"didnt_need_that", "out_of_sight"});
            case "restock_operations" -> thresholds(player, previous, total, new long[] {1, 1000},
                    new String[] {"always_stocked", "restock_master"});
            case "deposit_operations" -> thresholds(player, previous, total, new long[] {1, 1000},
                    new String[] {"delivery_service", "delivery_expert"});
            case "workbench_crafts" -> thresholds(player, previous, total, new long[] {50},
                    new String[] {"production_line"});
            case "abandoned_backpacks_found" -> thresholds(player, previous, total, new long[] {1, 5},
                    new String[] {"lost_and_found", "collector_of_lost_packs"});
            case "fluid_stored_mb" -> thresholds(player, previous, total, new long[] {1}, new String[] {"tank_on_your_back"});
            case "energy_stored_fe" -> thresholds(player, previous, total, new long[] {1}, new String[] {"portable_power"});
            case "flight_distance" -> thresholds(player, previous, total, new long[] {10000000},
                    new String[] {"backpack_airlines"});
            case "distance_with_backpack" -> thresholds(player, previous, total,
                    new long[] {1000000, 10000000, 100000000, 1000000000},
                    new String[] {"on_the_road", "seasoned_traveler", "long_haul", "world_on_your_back"});
            default -> { }
        }
    }

    public static void grant(ServerPlayer player, String advancement) {
        AdvancementHolder holder = player.level().getServer().getAdvancements().get(
                Identifier.fromNamespaceAndPath("smartbackpacks", "usage/" + advancement));
        if (holder != null && !player.getAdvancements().getOrStartProgress(holder).isDone()) {
            player.getAdvancements().award(holder, "used");
        }
    }

    private static void thresholds(ServerPlayer player, long previous, long total, long[] limits, String[] advancements) {
        for (int i = 0; i < limits.length; i++) {
            if (previous < limits[i] && total >= limits[i]) grant(player, advancements[i]);
        }
    }
}
