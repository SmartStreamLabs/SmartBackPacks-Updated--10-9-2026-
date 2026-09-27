package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.DeathEmergencyKitUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class DeathEmergencyKitHandler {
    private static final Map<UUID, UUID> PENDING_DEATHS = new HashMap<>();
    private static final Set<UUID> RESPAWNED = new HashSet<>();

    private DeathEmergencyKitHandler() {
    }

    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && SmartBackpacksConfig.deathEmergencyKitEnabled()) {
            onPlayerDeath(player);
        }
    }

    public static void onPlayerDeath(ServerPlayer player) {
        if (SmartBackpacksConfig.deathEmergencyKitEnabled()) {
            PENDING_DEATHS.put(player.getUUID(), UUID.randomUUID());
        }
    }

    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && PENDING_DEATHS.containsKey(player.getUUID())) {
            RESPAWNED.add(player.getUUID());
        }
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        UUID id = player.getUUID();
        if (!RESPAWNED.remove(id) || PENDING_DEATHS.remove(id) == null
                || !SmartBackpacksConfig.deathEmergencyKitEnabled()) {
            return;
        }
        retrieve(player);
    }

    private static void retrieve(ServerPlayer player) {
        for (BackpackAccess access : carriedBackpacks(player)) {
            ItemStack backpack = access.getBackpackStack(player);
            if (!(backpack.getItem() instanceof BackpackItem backpackItem)) {
                continue;
            }
            DeathEmergencyKitData kit = null;
            for (ItemStack upgrade : BackpackStackData.loadUpgrades(backpack)) {
                if (upgrade.getItem() instanceof DeathEmergencyKitUpgradeItem) {
                    kit = upgrade.getOrDefault(ModDataComponents.DEATH_EMERGENCY_KIT_DATA.get(), DeathEmergencyKitData.DEFAULT);
                    break;
                }
            }
            if (kit == null) {
                continue;
            }
            boolean supplied = false;
            NonNullList<ItemStack> templates = kit.loadTemplates();
            NonNullList<ItemStack> storage = BackpackStackData.loadStorage(backpack, backpackItem.getTier());
            for (int entry = 0; entry < DeathEmergencyKitData.SLOT_COUNT; entry++) {
                ItemStack template = templates.get(entry);
                if (template.isEmpty()) {
                    continue;
                }
                int remaining = Math.min(template.getCount(), template.getMaxStackSize());
                for (int slot = 0; slot < storage.size() && remaining > 0; slot++) {
                    ItemStack stored = storage.get(slot);
                    if (stored.isEmpty() || !ItemStack.isSameItemSameComponents(template, stored)) {
                        continue;
                    }
                    int count = Math.min(remaining, stored.getCount());
                    ItemStack transfer = stored.copyWithCount(count);
                    stored.shrink(count);
                    if (stored.isEmpty()) {
                        storage.set(slot, ItemStack.EMPTY);
                    }
                    BackpackStackData.saveStorage(backpack, storage);
                    access.setBackpackStack(player, backpack);
                    int delivered = insertOrDrop(player, transfer, kit.destination(entry), access.getLockedInventorySlot());
                    remaining -= count;
                    supplied |= delivered > 0;
                }
            }
            if (supplied) {
                player.getInventory().setChanged();
                player.sendSystemMessage(Component.translatable("message.smartbackpacks.death_emergency_kit_retrieved"), true);
                com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.add(player, "emergency_kits_used", 1);
            }
            return;
        }
    }

    private static int insertOrDrop(ServerPlayer player, ItemStack incoming, int destination, int backpackSlot) {
        int originalCount = incoming.getCount();
        if (destination >= 0 && destination < 9 && destination != backpackSlot) {
            ItemStack target = player.getInventory().getItem(destination);
            if (!target.isEmpty() && !ItemStack.isSameItemSameComponents(target, incoming)) {
                for (int slot = 9; slot < 36; slot++) {
                    if (slot != backpackSlot && player.getInventory().getItem(slot).isEmpty()) {
                        player.getInventory().setItem(slot, target);
                        player.getInventory().setItem(destination, ItemStack.EMPTY);
                        break;
                    }
                }
            }
            insertIntoSlot(player, incoming, destination);
        }
        for (int slot = 0; slot < 36 && !incoming.isEmpty(); slot++) {
            if (slot != destination && slot != backpackSlot) {
                insertIntoSlot(player, incoming, slot);
            }
        }
        if (!incoming.isEmpty()) {
            player.drop(incoming.copy(), false, Prediction.SERVER_ONLY);
        }
        return originalCount - incoming.getCount();
    }

    private static void insertIntoSlot(ServerPlayer player, ItemStack incoming, int slot) {
        if (incoming.isEmpty()) {
            return;
        }
        ItemStack current = player.getInventory().getItem(slot);
        if (current.isEmpty()) {
            player.getInventory().setItem(slot, incoming.copyWithCount(
                    Math.min(incoming.getCount(), incoming.getMaxStackSize())));
            incoming.shrink(Math.min(incoming.getCount(), incoming.getMaxStackSize()));
        } else if (ItemStack.isSameItemSameComponents(current, incoming)) {
            int move = Math.min(incoming.getCount(), current.getMaxStackSize() - current.getCount());
            if (move > 0) {
                current.grow(move);
                incoming.shrink(move);
            }
        }
    }

    private static List<BackpackAccess> carriedBackpacks(ServerPlayer player) {
        List<BackpackAccess> result = new ArrayList<>();
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof BackpackItem item) {
                result.add(BackpackAccess.inventory(slot, item.getTier()));
            }
        }
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof BackpackItem item) {
            result.add(BackpackAccess.fromHand(player, InteractionHand.OFF_HAND, item.getTier()));
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof BackpackItem item) {
            result.add(BackpackAccess.chest(item.getTier()));
        }
        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            ItemStack stack = CuriosCompat.getBackStack(player, slot);
            if (stack.getItem() instanceof BackpackItem item) {
                result.add(BackpackAccess.curioBack(slot, item.getTier()));
            }
        }
        return result;
    }
}
