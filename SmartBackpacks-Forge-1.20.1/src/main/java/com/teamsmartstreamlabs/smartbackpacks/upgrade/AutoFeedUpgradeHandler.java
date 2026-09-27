package com.teamsmartstreamlabs.smartbackpacks.upgrade;


import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.OpenBackpackTracker;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.AutoFeedUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.core.NonNullList;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class AutoFeedUpgradeHandler {
    private static final int CHECK_INTERVAL = 10;
    private static final int USE_COOLDOWN_TICKS = 20;
    private static final Map<UUID, Long> NEXT_ALLOWED_USE = new HashMap<>();

    private AutoFeedUpgradeHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || !player.isAlive()) {
            return;
        }

        long gameTime = player.level().getGameTime();
        if (player.tickCount % CHECK_INTERVAL != 0 || gameTime < NEXT_ALLOWED_USE.getOrDefault(player.getUUID(), 0L)) {
            return;
        }

        AutoFeedBackpack autoFeedBackpack = findAutoFeedBackpack(player);
        if (autoFeedBackpack == null
                || OpenBackpackTracker.isOpen(player, autoFeedBackpack.access())
                || !shouldFeed(player, autoFeedBackpack.data())) {
            return;
        }

        NonNullList<ItemStack> storage = BackpackStackData.loadStorage(autoFeedBackpack.backpack(), autoFeedBackpack.tier());
        FeedCandidate candidate = findBestCandidate(player, storage, autoFeedBackpack.data());
        if (candidate == null) {
            return;
        }

        if (consumeCandidate(player, autoFeedBackpack.backpack(), autoFeedBackpack.tier(), storage, candidate)) {
            BackpackStackData.saveStorage(autoFeedBackpack.backpack(), storage);
            autoFeedBackpack.saver().accept(autoFeedBackpack.backpack());
            NEXT_ALLOWED_USE.put(player.getUUID(), gameTime + USE_COOLDOWN_TICKS);
            com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.add(player, "auto_feed_items_consumed", 1);
        }
    }

    private static boolean shouldFeed(ServerPlayer player, AutoFeedUpgradeData data) {
        if (!data.enabled()) {
            return false;
        }

        int hunger = player.getFoodData().getFoodLevel();
        float saturation = player.getFoodData().getSaturationLevel();
        return hunger <= data.hungerThreshold() || (hunger < 20 && saturation <= data.saturationThreshold());
    }

    private static AutoFeedBackpack findAutoFeedBackpack(ServerPlayer player) {
        for (int slot = 0; slot < 36; slot++) {
            int inventorySlot = slot;
            ItemStack inventoryStack = player.getInventory().getItem(slot);
            BackpackAccess access = inventoryStack.getItem() instanceof BackpackItem backpackItem
                    ? BackpackAccess.inventory(slot, backpackItem.getTier())
                    : null;
            AutoFeedBackpack backpack = createAutoFeedBackpack(inventoryStack, updated -> {
                player.getInventory().setItem(inventorySlot, updated);
                player.getInventory().setChanged();
            }, access);
            if (backpack != null) {
                return backpack;
            }
        }

        AutoFeedBackpack offhand = createAutoFeedBackpack(
                player.getOffhandItem(),
                updated -> player.getInventory().setChanged(),
                player.getOffhandItem().getItem() instanceof BackpackItem backpackItem ? BackpackAccess.fromHand(player, net.minecraft.world.InteractionHand.OFF_HAND, backpackItem.getTier()) : null);
        if (offhand != null) {
            return offhand;
        }

        AutoFeedBackpack chest = createAutoFeedBackpack(
                player.getItemBySlot(EquipmentSlot.CHEST),
                updated -> player.setItemSlot(EquipmentSlot.CHEST, updated),
                player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof BackpackItem backpackItem ? BackpackAccess.chest(backpackItem.getTier()) : null);
        if (chest != null) {
            return chest;
        }

        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            int curioSlot = slot;
            AutoFeedBackpack backpack = createAutoFeedBackpack(CuriosCompat.getBackStack(player, curioSlot),
                    updated -> CuriosCompat.setBackStack(player, curioSlot, updated),
                    CuriosCompat.getBackStack(player, curioSlot).getItem() instanceof BackpackItem backpackItem ? BackpackAccess.curioBack(curioSlot, backpackItem.getTier()) : null);
            if (backpack != null) {
                return backpack;
            }
        }

        return null;
    }

    private static AutoFeedBackpack createAutoFeedBackpack(ItemStack stack, Consumer<ItemStack> saver, BackpackAccess access) {
        if (!(stack.getItem() instanceof BackpackItem backpackItem)) {
            return null;
        }

        for (ItemStack upgrade : BackpackStackData.loadUpgrades(stack)) {
            if (upgrade.getItem() instanceof AutoFeedUpgradeItem) {
                AutoFeedUpgradeData data = com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.getOrDefault(upgrade, ModDataComponents.AUTO_FEED_UPGRADE_DATA.get(), AutoFeedUpgradeData.DEFAULT);
                return new AutoFeedBackpack(stack, backpackItem.getTier(), data, saver, access);
            }
        }

        return null;
    }

    private static FeedCandidate findBestCandidate(ServerPlayer player, NonNullList<ItemStack> storage, AutoFeedUpgradeData data) {
        FeedCandidate bestCandidate = null;

        for (int slot = 0; slot < storage.size(); slot++) {
            ItemStack candidate = storage.get(slot);
            if (candidate.isEmpty() || !AutoFeedFilterMatcher.allows(data, candidate)) {
                continue;
            }

            FoodProperties food = com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.get(candidate, DataComponents.FOOD);
            if (food == null || !player.canEat(food.canAlwaysEat())) {
                continue;
            }

            int score = food.getNutrition() * 100 + Math.round(food.getSaturationModifier() * 10.0F);
            if (bestCandidate == null || score > bestCandidate.score()) {
                bestCandidate = new FeedCandidate(slot, score);
            }
        }

        return bestCandidate;
    }

    private static boolean consumeCandidate(ServerPlayer player, ItemStack backpack, BackpackTier tier, NonNullList<ItemStack> storage, FeedCandidate candidate) {
        ItemStack source = storage.get(candidate.slot());
        if (source.isEmpty()) {
            return false;
        }

        ItemStack singleUse = source.copyWithCount(1);
        FoodProperties food = com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.get(singleUse, DataComponents.FOOD);
        if (food == null || !player.canEat(food.canAlwaysEat())) {
            return false;
        }

        ItemStack remainder = singleUse.finishUsingItem(player.level(), player);
        source.shrink(1);
        if (source.isEmpty()) {
            storage.set(candidate.slot(), ItemStack.EMPTY);
        }

        if (!remainder.isEmpty()) {
            ItemStack leftover = BackpackStackData.insertIntoStorage(backpack, tier, remainder.copy(), false);
            if (!leftover.isEmpty() && !player.getInventory().add(leftover)) {
                player.drop(leftover, false);
            }
        }

        return true;
    }

    private record AutoFeedBackpack(ItemStack backpack, BackpackTier tier, AutoFeedUpgradeData data, Consumer<ItemStack> saver, BackpackAccess access) {
    }

    private record FeedCandidate(int slot, int score) {
    }
}

