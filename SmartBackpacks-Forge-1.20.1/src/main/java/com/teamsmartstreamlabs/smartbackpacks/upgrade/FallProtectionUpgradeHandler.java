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
import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.FallProtectionUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class FallProtectionUpgradeHandler {
    private static final Map<UUID, Long> PLAYER_COOLDOWNS = new HashMap<>();
    private static final Set<UUID> PROTECTED_FALLS = new HashSet<>();

    private FallProtectionUpgradeHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }
        UUID id = player.getUUID();
        if (PROTECTED_FALLS.contains(id)) {
            if (player.onGround() || player.isInWater() || player.isInLava()) {
                PROTECTED_FALLS.remove(id);
            } else {
                player.fallDistance = 0;
                MobEffectInstance current = player.getEffect(MobEffects.SLOW_FALLING);
                if (current == null || current.getDuration() < 20) {
                    player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,
                            SmartBackpacksConfig.fallProtectionSlowFallingSeconds() * 20, 0, true, false, false));
                }
            }
            return;
        }
        if (!SmartBackpacksConfig.fallProtectionUpgradeEnabled() || player.isCreative() || player.isSpectator()
                || player.getAbilities().flying || player.isFallFlying() || player.onGround()
                || player.isInWater() || player.isInLava() || player.isPassenger()
                || player.hasEffect(MobEffects.SLOW_FALLING) || player.fallDistance < 4.0F
                || player.level().getFluidState(player.blockPosition().below()).is(FluidTags.WATER)) {
            return;
        }

        double expectedDamage = Math.max(0.0D, player.fallDistance - 3.0D);
        double remainingHealth = player.getHealth() + player.getAbsorptionAmount() - expectedDamage;
        if (expectedDamage < 1.0F || remainingHealth > SmartBackpacksConfig.fallProtectionTriggerHealthMargin()) {
            return;
        }

        long now = System.currentTimeMillis();
        List<BackpackAccess> accesses = carriedBackpacks(player);
        Candidate selected = null;
        long latestCooldown = PLAYER_COOLDOWNS.getOrDefault(id, 0L);
        for (BackpackAccess access : accesses) {
            NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(access.getBackpackStack(player));
            for (int slot = 0; slot < upgrades.size(); slot++) {
                ItemStack upgrade = upgrades.get(slot);
                if (!(upgrade.getItem() instanceof FallProtectionUpgradeItem)) {
                    continue;
                }
                latestCooldown = Math.max(latestCooldown, ItemStackCompat.getOrDefault(upgrade, ModDataComponents.FALL_PROTECTION_COOLDOWN_UNTIL.get(), 0L));
                if (selected == null && upgrade.getDamageValue() < Math.min(upgrade.getMaxDamage(),
                        SmartBackpacksConfig.fallProtectionMaxCharges())) {
                    selected = new Candidate(access, slot);
                }
            }
        }
        if (selected == null || now < latestCooldown) {
            return;
        }

        MobEffectInstance effect = new MobEffectInstance(MobEffects.SLOW_FALLING,
                SmartBackpacksConfig.fallProtectionSlowFallingSeconds() * 20, 0, true, false, false);
        if (!player.addEffect(effect)) {
            return;
        }
        player.fallDistance = 0;
        PROTECTED_FALLS.add(id);
        long until = now + SmartBackpacksConfig.fallProtectionCooldownSeconds() * 1000L;
        PLAYER_COOLDOWNS.put(id, until);

        for (BackpackAccess access : accesses) {
            ItemStack backpack = access.getBackpackStack(player);
            NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
            boolean changed = false;
            for (int slot = 0; slot < upgrades.size(); slot++) {
                ItemStack upgrade = upgrades.get(slot);
                if (!(upgrade.getItem() instanceof FallProtectionUpgradeItem)) {
                    continue;
                }
                ItemStackCompat.set(upgrade, ModDataComponents.FALL_PROTECTION_COOLDOWN_UNTIL.get(), until);
                if (access.equals(selected.access()) && slot == selected.slot()) {
                    upgrade.setDamageValue(upgrade.getDamageValue() + 1);
                }
                changed = true;
            }
            if (changed) {
                BackpackStackData.saveUpgrades(backpack, upgrades);
                access.setBackpackStack(player, backpack);
            }
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.25F, 1.25F);
        player.sendSystemMessage(Component.translatable("message.smartbackpacks.fall_protection_activated"), true);
        com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.add(player, "fall_protection_saves", 1);
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PROTECTED_FALLS.remove(player.getUUID());
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

    private record Candidate(BackpackAccess access, int slot) {
    }
}
