package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.QuiverUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.neoforged.neoforge.event.entity.living.LivingGetProjectileEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class QuiverUpgradeHandler {
    private static final TagKey<Item> QUIVER_PROJECTILES = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "quiver_projectiles"));
    private static final TagKey<Item> BOW_PROJECTILES = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "bow_projectiles"));
    private static final TagKey<Item> CROSSBOW_PROJECTILES = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "crossbow_projectiles"));
    private static final ConcurrentHashMap<UUID, List<PendingQuiverUse>> PENDING_SAVES = new ConcurrentHashMap<>();

    private QuiverUpgradeHandler() {
    }

    public static boolean isQuiverProjectile(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.is(Items.FIREWORK_ROCKET)) {
            return SmartBackpacksConfig.allowQuiverFireworkRockets();
        }
        return stack.is(ItemTags.ARROWS)
                || (SmartBackpacksConfig.allowQuiverModdedProjectileTags()
                && (stack.is(QUIVER_PROJECTILES) || stack.is(BOW_PROJECTILES) || stack.is(CROSSBOW_PROJECTILES)));
    }

    public static void onLivingGetProjectile(LivingGetProjectileEvent event) {
        if (!(event.getEntity() instanceof Player player)
                || player.hasInfiniteMaterials()
                || !(event.getProjectileWeaponItemStack().getItem() instanceof ProjectileWeaponItem projectileWeaponItem)) {
            return;
        }

        SearchResult result = findProjectile(player, event.getProjectileWeaponItemStack(), projectileWeaponItem, event.getProjectileItemStack());
        if (result == null) {
            return;
        }

        event.setProjectileItemStack(result.projectile());
        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer && result.pendingUse() != null) {
            PENDING_SAVES.computeIfAbsent(serverPlayer.getUUID(), ignored -> new ArrayList<>()).add(result.pendingUse());
        }
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }

        List<PendingQuiverUse> pending = PENDING_SAVES.remove(player.getUUID());
        if (pending == null || pending.isEmpty()) {
            return;
        }

        for (PendingQuiverUse pendingUse : pending) {
            pendingUse.save(player);
        }
    }

    public static boolean insertIntoFirstQuiver(ItemStack backpack, ItemStack incoming, boolean simulate) {
        if (!(backpack.getItem() instanceof BackpackItem) || !isQuiverProjectile(incoming)) {
            return false;
        }

        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        boolean changed = false;
        for (int upgradeSlot = 0; upgradeSlot < upgrades.size() && !incoming.isEmpty(); upgradeSlot++) {
            ItemStack upgrade = upgrades.get(upgradeSlot);
            if (!(upgrade.getItem() instanceof QuiverUpgradeItem)) {
                continue;
            }

            QuiverUpgradeData data = upgrade.getOrDefault(ModDataComponents.QUIVER_UPGRADE_DATA.get(), QuiverUpgradeData.DEFAULT);
            NonNullList<ItemStack> projectiles = data.loadProjectiles();
            int originalCount = incoming.getCount();
            insertIntoProjectileSlots(projectiles, incoming, simulate);
            if (incoming.getCount() == originalCount) {
                continue;
            }

            changed = true;
            if (!simulate) {
                upgrade.set(ModDataComponents.QUIVER_UPGRADE_DATA.get(), data.withProjectiles(projectiles));
                upgrades.set(upgradeSlot, upgrade);
            }
        }

        if (changed && !simulate) {
            BackpackStackData.saveUpgrades(backpack, upgrades);
        }
        return changed;
    }

    private static SearchResult findProjectile(Player player, ItemStack weaponStack, ProjectileWeaponItem weaponItem, ItemStack vanillaProjectile) {
        boolean sawQuiverOnly = false;
        for (CarriedBackpack carriedBackpack : findCarriedBackpacks(player)) {
            NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(carriedBackpack.stack());
            for (int upgradeSlot = 0; upgradeSlot < upgrades.size(); upgradeSlot++) {
                ItemStack upgrade = upgrades.get(upgradeSlot);
                if (!(upgrade.getItem() instanceof QuiverUpgradeItem)) {
                    continue;
                }

                QuiverUpgradeData data = upgrade.getOrDefault(ModDataComponents.QUIVER_UPGRADE_DATA.get(), QuiverUpgradeData.DEFAULT);
                if (!data.enabled()) {
                    continue;
                }

                if (!vanillaProjectile.isEmpty() && data.sourcePriority() == QuiverSourcePriority.PLAYER_INVENTORY_FIRST) {
                    return null;
                }

                sawQuiverOnly |= data.sourcePriority() == QuiverSourcePriority.QUIVER_ONLY;
                NonNullList<ItemStack> projectiles = data.loadProjectiles();
                int projectileSlot = findProjectileSlot(weaponStack, weaponItem, data, projectiles);
                if (projectileSlot < 0) {
                    continue;
                }

                ItemStack chosen = projectiles.get(projectileSlot);
                if (player.level().isClientSide()) {
                    return SearchResult.override(chosen.copy(), null);
                }

                PendingQuiverUse pendingUse = new PendingQuiverUse(carriedBackpack.access(), upgradeSlot, projectiles);
                return SearchResult.override(chosen, pendingUse);
            }
        }

        if (sawQuiverOnly) {
            return SearchResult.override(ItemStack.EMPTY, null);
        }
        return null;
    }

    private static int findProjectileSlot(ItemStack weaponStack, ProjectileWeaponItem weaponItem, QuiverUpgradeData data, NonNullList<ItemStack> projectiles) {
        Predicate<ItemStack> supported = weaponItem.getAllSupportedProjectiles().or(weaponItem.getSupportedHeldProjectiles());
        for (int slot : orderedSlots(data, projectiles)) {
            ItemStack candidate = projectiles.get(slot);
            if (isQuiverProjectile(candidate) && supported.test(candidate)) {
                return slot;
            }
        }
        return -1;
    }

    private static List<Integer> orderedSlots(QuiverUpgradeData data, NonNullList<ItemStack> projectiles) {
        if (data.selectionMode() == QuiverSelectionMode.CUSTOM_PRIORITY) {
            return data.prioritySlots();
        }

        List<Integer> slots = new ArrayList<>(QuiverUpgradeData.DEFAULT_PRIORITY);
        if (data.selectionMode() == QuiverSelectionMode.AUTO_SELECT) {
            int preferredSlot = data.preferredSlot();
            slots.sort(Comparator
                    .comparingInt((Integer slot) -> slot == preferredSlot ? -1 : autoPriority(projectiles.get(slot)))
                    .thenComparingInt(Integer::intValue));
        }
        return slots;
    }

    private static int autoPriority(ItemStack stack) {
        if (stack.is(Items.TIPPED_ARROW)) {
            return 0;
        }
        if (stack.is(Items.SPECTRAL_ARROW)) {
            return 1;
        }
        if (stack.is(Items.ARROW)) {
            return 2;
        }
        if (stack.is(Items.FIREWORK_ROCKET)) {
            return 3;
        }
        return 4;
    }

    private static List<CarriedBackpack> findCarriedBackpacks(Player player) {
        List<CarriedBackpack> backpacks = new ArrayList<>();
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof BackpackItem backpackItem) {
            backpacks.add(new CarriedBackpack(chest, BackpackAccess.chest(backpackItem.getTier())));
        }

        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            ItemStack stack = CuriosCompat.getBackStack(player, slot);
            if (stack.getItem() instanceof BackpackItem backpackItem) {
                backpacks.add(new CarriedBackpack(stack, BackpackAccess.curioBack(slot, backpackItem.getTier())));
            }
        }

        for (int slot = 0; slot < 9; slot++) {
            addInventoryBackpack(player, backpacks, slot);
        }
        for (int slot = 9; slot < 36; slot++) {
            addInventoryBackpack(player, backpacks, slot);
        }

        return backpacks;
    }

    private static void addInventoryBackpack(Player player, List<CarriedBackpack> backpacks, int slot) {
        ItemStack stack = player.getInventory().getItem(slot);
        if (stack.getItem() instanceof BackpackItem backpackItem) {
            backpacks.add(new CarriedBackpack(stack, BackpackAccess.inventory(slot, backpackItem.getTier())));
        }
    }

    private static void insertIntoProjectileSlots(NonNullList<ItemStack> projectiles, ItemStack incoming, boolean simulate) {
        for (int slot = 0; slot < projectiles.size() && !incoming.isEmpty(); slot++) {
            ItemStack existing = projectiles.get(slot);
            if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, incoming)) {
                continue;
            }

            int transfer = Math.min(incoming.getCount(), existing.getMaxStackSize() - existing.getCount());
            if (transfer <= 0) {
                continue;
            }

            if (!simulate) {
                existing.grow(transfer);
            }
            incoming.shrink(transfer);
        }

        for (int slot = 0; slot < projectiles.size() && !incoming.isEmpty(); slot++) {
            if (!projectiles.get(slot).isEmpty()) {
                continue;
            }

            int transfer = Math.min(incoming.getCount(), incoming.getMaxStackSize());
            if (!simulate) {
                projectiles.set(slot, incoming.copyWithCount(transfer));
            }
            incoming.shrink(transfer);
        }
    }

    private record SearchResult(ItemStack projectile, PendingQuiverUse pendingUse) {
        private static SearchResult override(ItemStack projectile, PendingQuiverUse pendingUse) {
            return new SearchResult(projectile, pendingUse);
        }
    }

    private record CarriedBackpack(ItemStack stack, BackpackAccess access) {
    }

    private record PendingQuiverUse(BackpackAccess access, int upgradeSlot, NonNullList<ItemStack> projectiles) {
        private void save(ServerPlayer player) {
            ItemStack backpack = this.access.getBackpackStack(player);
            if (!(backpack.getItem() instanceof BackpackItem)) {
                return;
            }

            NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
            if (this.upgradeSlot < 0 || this.upgradeSlot >= upgrades.size()) {
                return;
            }

            ItemStack upgrade = upgrades.get(this.upgradeSlot);
            if (!(upgrade.getItem() instanceof QuiverUpgradeItem)) {
                return;
            }

            QuiverUpgradeData currentData = upgrade.getOrDefault(ModDataComponents.QUIVER_UPGRADE_DATA.get(), QuiverUpgradeData.DEFAULT);
            upgrade.set(ModDataComponents.QUIVER_UPGRADE_DATA.get(), currentData.withProjectiles(this.projectiles));
            upgrades.set(this.upgradeSlot, upgrade);
            BackpackStackData.saveUpgrades(backpack, upgrades);
            this.access.setBackpackStack(player, backpack);
            player.getInventory().setChanged();
        }
    }
}
