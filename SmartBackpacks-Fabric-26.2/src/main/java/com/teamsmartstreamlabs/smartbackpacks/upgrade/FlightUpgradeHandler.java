package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.FlightUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class FlightUpgradeHandler {
    private static final Map<UUID, ServerPlayer> OWNED_FLIGHT = new HashMap<>();

    private FlightUpgradeHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        tick(event.getEntity());
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        logout(event.getEntity());
    }

    public static void tick(Player entity) {
        if (!(entity instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }

        UUID id = player.getUUID();
        if (player.isCreative() || player.isSpectator()) {
            OWNED_FLIGHT.remove(id);
            return;
        }

        if (!SmartBackpacksConfig.flightUpgradeEnabled() || !hasEnabledUpgrade(player)) {
            revoke(player, true);
            return;
        }

        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            OWNED_FLIGHT.put(id, player);
            player.onUpdateAbilities();
        } else if (OWNED_FLIGHT.containsKey(id)) {
            // Dimension transfers can replace the ServerPlayer while retaining its abilities.
            OWNED_FLIGHT.put(id, player);
        }
        if (OWNED_FLIGHT.get(id) == player && player.getAbilities().flying) {
            com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.grant(player, "why_walk");
            double dx = player.getX() - player.xo;
            double dy = player.getY() - player.yo;
            double dz = player.getZ() - player.zo;
            double moved = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (moved > 0 && moved <= 10.0D) {
                com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.add(player, "flight_distance", Math.round(moved * 1000.0D));
            }
        }
    }

    public static void logout(Player entity) {
        if (entity instanceof ServerPlayer player) {
            revoke(player, false);
        }
    }

    private static void revoke(ServerPlayer player, boolean sync) {
        if (OWNED_FLIGHT.remove(player.getUUID()) == null || player.isCreative() || player.isSpectator()) {
            return;
        }
        if (player.getAbilities().mayfly) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            if (sync) {
                player.onUpdateAbilities();
            }
        }
    }

    private static boolean hasEnabledUpgrade(ServerPlayer player) {
        for (int slot = 0; slot < 36; slot++) {
            if (hasEnabledUpgrade(player.getInventory().getItem(slot))) {
                return true;
            }
        }
        if (hasEnabledUpgrade(player.getOffhandItem())
                || hasEnabledUpgrade(player.getItemBySlot(EquipmentSlot.CHEST))) {
            return true;
        }
        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            if (hasEnabledUpgrade(CuriosCompat.getBackStack(player, slot))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasEnabledUpgrade(ItemStack backpack) {
        if (!(backpack.getItem() instanceof BackpackItem)) {
            return false;
        }
        return BackpackStackData.loadUpgrades(backpack).stream()
                .anyMatch(upgrade -> upgrade.getItem() instanceof FlightUpgradeItem
                        && ItemStackCompat.getOrDefault(upgrade, ModDataComponents.FLIGHT_UPGRADE_ENABLED.get(), true));
    }
}
