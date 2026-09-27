package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.NightVisionUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class NightVisionUpgradeHandler {
    private static final int EFFECT_DURATION = 260;
    private static final int REFRESH_AT = 220;
    private static final Map<UUID, MobEffectInstance> OWNED_EFFECTS = new HashMap<>();

    private NightVisionUpgradeHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }

        UUID id = player.getUUID();
        MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
        MobEffectInstance owned = OWNED_EFFECTS.get(id);
        if (!hasEnabledUpgrade(player)) {
            clearOwnedEffect(player, current, owned);
            OWNED_EFFECTS.remove(id);
            return;
        }

        if (current != null && current != owned) {
            OWNED_EFFECTS.remove(id);
            return;
        }
        if (current != null && !isOurEffect(current)) {
            OWNED_EFFECTS.remove(id);
            return;
        }
        if (current == null || current.getDuration() <= REFRESH_AT) {
            MobEffectInstance refreshed = new MobEffectInstance(MobEffects.NIGHT_VISION, EFFECT_DURATION, 0, true, false, false);
            player.addEffect(refreshed);
            OWNED_EFFECTS.put(id, player.getEffect(MobEffects.NIGHT_VISION));
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MobEffectInstance owned = OWNED_EFFECTS.remove(player.getUUID());
            clearOwnedEffect(player, player.getEffect(MobEffects.NIGHT_VISION), owned);
        }
    }

    private static void clearOwnedEffect(ServerPlayer player, MobEffectInstance current, MobEffectInstance owned) {
        if (current != null && current == owned && isOurEffect(current)) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    private static boolean isOurEffect(MobEffectInstance effect) {
        return effect.getAmplifier() == 0 && effect.getDuration() <= EFFECT_DURATION
                && effect.isAmbient() && !effect.isVisible() && !effect.showIcon();
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
                .anyMatch(upgrade -> upgrade.getItem() instanceof NightVisionUpgradeItem
                        && upgrade.getOrDefault(ModDataComponents.NIGHT_VISION_UPGRADE_ENABLED.get(), true));
    }
}
