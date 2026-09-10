package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.SurvivalAssistUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class SurvivalAssistUpgradeHandler {
    private static final int CHECK_INTERVAL = 5;
    private static final int USE_COOLDOWN_TICKS = 40;
    private static final Map<UUID, Long> NEXT_ALLOWED_USE = new java.util.HashMap<>();
    private static final ResourceLocation FIRE_RESISTANCE_ID = effectId("fire_resistance");
    private static final ResourceLocation WATER_BREATHING_ID = effectId("water_breathing");
    private static final ResourceLocation SLOW_FALLING_ID = effectId("slow_falling");
    private static final ResourceLocation INSTANT_HEALTH_ID = effectId("instant_health");
    private static final ResourceLocation REGENERATION_ID = effectId("regeneration");
    private static final ResourceLocation ABSORPTION_ID = effectId("absorption");
    private static final ResourceLocation RESISTANCE_ID = effectId("resistance");
    private static final ResourceLocation SATURATION_ID = effectId("saturation");
    private static final DataComponentType<?> CONSUMABLE_COMPONENT = resolveConsumableComponent();

    private SurvivalAssistUpgradeHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || !player.isAlive()) {
            return;
        }

        long gameTime = player.level().getGameTime();
        if (gameTime < NEXT_ALLOWED_USE.getOrDefault(player.getUUID(), 0L) || player.tickCount % CHECK_INTERVAL != 0) {
            return;
        }

        SurvivalBackpack assistBackpack = findAssistBackpack(player);
        if (assistBackpack == null) {
            return;
        }

        NonNullList<ItemStack> storage = BackpackStackData.loadStorage(assistBackpack.backpack(), assistBackpack.tier());
        UseCandidate candidate = findBestCandidate(player, storage, assistBackpack.data());
        if (candidate == null) {
            return;
        }

        if (consumeCandidate(player, assistBackpack.backpack(), assistBackpack.tier(), storage, candidate)) {
            BackpackStackData.saveStorage(assistBackpack.backpack(), storage);
            assistBackpack.saver().accept(assistBackpack.backpack());
            NEXT_ALLOWED_USE.put(player.getUUID(), gameTime + USE_COOLDOWN_TICKS);
        }
    }

    private static SurvivalBackpack findAssistBackpack(ServerPlayer player) {
        for (int slot = 0; slot < 36; slot++) {
            int inventorySlot = slot;
            SurvivalBackpack backpack = createSurvivalBackpack(player.getInventory().getItem(slot), updated -> {
                player.getInventory().setItem(inventorySlot, updated);
                player.getInventory().setChanged();
            });
            if (backpack != null) {
                return backpack;
            }
        }

        SurvivalBackpack offhand = createSurvivalBackpack(player.getOffhandItem(), updated -> player.getInventory().setChanged());
        if (offhand != null) {
            return offhand;
        }

        SurvivalBackpack chest = createSurvivalBackpack(player.getItemBySlot(EquipmentSlot.CHEST), updated -> player.setItemSlot(EquipmentSlot.CHEST, updated));
        if (chest != null) {
            return chest;
        }

        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            int curioSlot = slot;
            SurvivalBackpack backpack = createSurvivalBackpack(CuriosCompat.getBackStack(player, curioSlot),
                    updated -> CuriosCompat.setBackStack(player, curioSlot, updated));
            if (backpack != null) {
                return backpack;
            }
        }

        return null;
    }

    private static SurvivalBackpack createSurvivalBackpack(ItemStack stack, Consumer<ItemStack> saver) {
        if (!(stack.getItem() instanceof BackpackItem backpackItem)) {
            return null;
        }

        for (ItemStack upgrade : BackpackStackData.loadUpgrades(stack)) {
            if (upgrade.getItem() instanceof SurvivalAssistUpgradeItem) {
                SurvivalAssistUpgradeData data = upgrade.getOrDefault(ModDataComponents.SURVIVAL_ASSIST_UPGRADE_DATA.get(), SurvivalAssistUpgradeData.DEFAULT);
                return new SurvivalBackpack(stack, backpackItem.getTier(), data, saver);
            }
        }

        return null;
    }

    private static UseCandidate findBestCandidate(ServerPlayer player, NonNullList<ItemStack> storage, SurvivalAssistUpgradeData data) {
        UseCandidate best = null;
        for (int slot = 0; slot < storage.size(); slot++) {
            ItemStack candidate = storage.get(slot);
            if (candidate.isEmpty()) {
                continue;
            }

            CandidateProfile profile = analyzeCandidate(candidate);
            if (!profile.supported() || !SurvivalAssistFilterMatcher.allows(data, candidate, profile.effectIds())) {
                continue;
            }

            int score = getUseScore(player, profile);
            if (score <= 0) {
                continue;
            }

            if (best == null || score > best.score()) {
                best = new UseCandidate(slot, score);
            }
        }
        return best;
    }

    private static CandidateProfile analyzeCandidate(ItemStack stack) {
        Set<ResourceLocation> effectIds = new HashSet<>();
        Set<ResourceLocation> appliedEffects = new HashSet<>();
        Set<ResourceLocation> removedEffects = new HashSet<>();
        boolean clearsEffects = stack.is(Items.MILK_BUCKET);

        PotionContents potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        for (MobEffectInstance instance : potionContents.getAllEffects()) {
            ResourceLocation effectId = BuiltInRegistries.MOB_EFFECT.getKey(instance.getEffect().value());
            if (effectId != null) {
                effectIds.add(effectId);
                appliedEffects.add(effectId);
            }
        }

        Object consumable = getConsumableComponent(stack);
        if (consumable != null) {
            for (Object consumeEffect : getConsumeEffects(consumable)) {
                String effectType = consumeEffect.getClass().getSimpleName();
                if ("ApplyStatusEffectsConsumeEffect".equals(effectType)) {
                    for (Object effectObject : getEffectValues(consumeEffect)) {
                        if (effectObject instanceof MobEffectInstance instance) {
                            ResourceLocation effectId = BuiltInRegistries.MOB_EFFECT.getKey(instance.getEffect().value());
                            if (effectId != null) {
                                effectIds.add(effectId);
                                appliedEffects.add(effectId);
                            }
                        }
                    }
                } else if ("RemoveStatusEffectsConsumeEffect".equals(effectType)) {
                    for (Object effectObject : getEffectValues(consumeEffect)) {
                        ResourceLocation effectId = extractEffectId(effectObject);
                        if (effectId != null) {
                            effectIds.add(effectId);
                            removedEffects.add(effectId);
                        }
                    }
                } else if ("ClearAllStatusEffectsConsumeEffect".equals(effectType)) {
                    clearsEffects = true;
                }
            }
        }

        boolean supported = clearsEffects || !appliedEffects.isEmpty() || !removedEffects.isEmpty();
        return new CandidateProfile(supported, clearsEffects, effectIds, appliedEffects, removedEffects);
    }

    private static int getUseScore(ServerPlayer player, CandidateProfile profile) {
        int bestScore = 0;
        if (profile.clearsEffects() && hasHarmfulEffect(player)) {
            bestScore = 250;
        }
        if (!profile.removedEffects().isEmpty() && hasMatchingNegativeEffect(player, profile.removedEffects())) {
            bestScore = Math.max(bestScore, 225);
        }
        for (ResourceLocation effect : profile.appliedEffects()) {
            bestScore = Math.max(bestScore, getEffectNeedScore(player, effect));
        }
        return bestScore;
    }

    private static int getEffectNeedScore(ServerPlayer player, ResourceLocation effectId) {
        if (FIRE_RESISTANCE_ID.equals(effectId)) {
            return (player.isOnFire() || player.isInLava()) && !hasActiveEffect(player, effectId, 60) ? 240 : 0;
        }
        if (WATER_BREATHING_ID.equals(effectId)) {
            return (player.isUnderWater() || player.getAirSupply() < player.getMaxAirSupply() / 2) && !hasActiveEffect(player, effectId, 60) ? 235 : 0;
        }
        if (SLOW_FALLING_ID.equals(effectId)) {
            return player.fallDistance > 6.0F && !hasActiveEffect(player, effectId, 40) ? 230 : 0;
        }
        if (INSTANT_HEALTH_ID.equals(effectId)) {
            return player.getHealth() <= player.getMaxHealth() * 0.5F ? 220 : 0;
        }
        if (REGENERATION_ID.equals(effectId)) {
            return player.getHealth() <= player.getMaxHealth() * 0.65F && !hasActiveEffect(player, effectId, 40) ? 210 : 0;
        }
        if (ABSORPTION_ID.equals(effectId)) {
            return player.getHealth() <= player.getMaxHealth() * 0.6F && !hasActiveEffect(player, effectId, 80) ? 205 : 0;
        }
        if (RESISTANCE_ID.equals(effectId)) {
            return player.getHealth() <= player.getMaxHealth() * 0.55F && !hasActiveEffect(player, effectId, 60) ? 200 : 0;
        }
        if (SATURATION_ID.equals(effectId)) {
            return player.getFoodData().getFoodLevel() <= 10 ? 190 : 0;
        }
        return 0;
    }

    private static boolean consumeCandidate(ServerPlayer player, ItemStack backpack, BackpackTier tier, NonNullList<ItemStack> storage, UseCandidate candidate) {
        ItemStack source = storage.get(candidate.slot());
        if (source.isEmpty()) {
            return false;
        }
        if (!ItemLockProtection.canConsumeForUpgrade(backpack, candidate.slot(), source)) {
            return false;
        }

        ItemStack singleUse = source.copyWithCount(1);
        Object consumable = getConsumableComponent(singleUse);
        if (consumable != null && !canConsume(consumable, player, singleUse)) {
            return false;
        }

        ItemStack remainder = singleUse.finishUsingItem(player.level(), player);
        source.shrink(1);
        if (source.isEmpty()) {
            storage.set(candidate.slot(), ItemStack.EMPTY);
        }

        if (!remainder.isEmpty()) {
            ItemStack leftover = ItemLockProtection.insertIntoStorage(backpack, tier, remainder.copy(), false);
            if (!leftover.isEmpty()) {
                if (!player.getInventory().add(leftover)) {
                    player.drop(leftover, false);
                }
            }
        }

        return true;
    }

    private static boolean hasActiveEffect(ServerPlayer player, ResourceLocation effectId, int minimumDuration) {
        return player.getActiveEffects().stream()
                .anyMatch(instance -> effectId.equals(BuiltInRegistries.MOB_EFFECT.getKey(instance.getEffect().value()))
                        && instance.getDuration() > minimumDuration);
    }

    private static boolean hasHarmfulEffect(ServerPlayer player) {
        return player.getActiveEffects().stream().anyMatch(instance -> instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL);
    }

    private static boolean hasMatchingNegativeEffect(ServerPlayer player, Set<ResourceLocation> removedEffects) {
        return player.getActiveEffects().stream()
                .anyMatch(instance -> instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL
                        && removedEffects.contains(BuiltInRegistries.MOB_EFFECT.getKey(instance.getEffect().value())));
    }

    private static ResourceLocation effectId(String path) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", path);
    }

    private static DataComponentType<?> resolveConsumableComponent() {
        try {
            Object fieldValue = DataComponents.class.getField("CONSUMABLE").get(null);
            return fieldValue instanceof DataComponentType<?> componentType ? componentType : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object getConsumableComponent(ItemStack stack) {
        if (CONSUMABLE_COMPONENT == null) {
            return null;
        }
        return stack.get((DataComponentType) CONSUMABLE_COMPONENT);
    }

    private static boolean canConsume(Object consumable, ServerPlayer player, ItemStack stack) {
        try {
            Object result = consumable.getClass().getMethod("canConsume", net.minecraft.world.entity.LivingEntity.class, ItemStack.class)
                    .invoke(consumable, player, stack);
            return result instanceof Boolean bool ? bool : true;
        } catch (ReflectiveOperationException ignored) {
            return true;
        }
    }

    private static List<?> getConsumeEffects(Object consumable) {
        try {
            Object result = consumable.getClass().getMethod("onConsumeEffects").invoke(consumable);
            return result instanceof List<?> list ? list : List.of();
        } catch (ReflectiveOperationException ignored) {
            return List.of();
        }
    }

    private static List<?> getEffectValues(Object consumeEffect) {
        try {
            Object result = consumeEffect.getClass().getMethod("effects").invoke(consumeEffect);
            if (result instanceof Iterable<?> iterable) {
                List<Object> values = new java.util.ArrayList<>();
                iterable.forEach(values::add);
                return values;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return List.of();
    }

    private static ResourceLocation extractEffectId(Object value) {
        if (value instanceof MobEffectInstance instance) {
            return BuiltInRegistries.MOB_EFFECT.getKey(instance.getEffect().value());
        }
        if (value instanceof Holder<?> holder && holder.value() instanceof MobEffect effect) {
            return BuiltInRegistries.MOB_EFFECT.getKey(effect);
        }
        return null;
    }

    private record SurvivalBackpack(ItemStack backpack, BackpackTier tier, SurvivalAssistUpgradeData data, Consumer<ItemStack> saver) {
    }

    private record UseCandidate(int slot, int score) {
    }

    private record CandidateProfile(
            boolean supported,
            boolean clearsEffects,
            Set<ResourceLocation> effectIds,
            Set<ResourceLocation> appliedEffects,
            Set<ResourceLocation> removedEffects) {
    }
}

