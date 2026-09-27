package com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.alchemy;

import java.util.List;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;

public final class PotionContents {
    public static final PotionContents EMPTY = new PotionContents(List.of());

    private final List<MobEffectInstance> effects;

    public PotionContents(List<MobEffectInstance> effects) {
        this.effects = effects.stream().map(MobEffectInstance::new).toList();
    }

    public static PotionContents fromStack(ItemStack stack) {
        List<MobEffectInstance> effects = PotionUtils.getMobEffects(stack);
        if (effects.isEmpty()) {
            return EMPTY;
        }
        return new PotionContents(effects);
    }

    public List<MobEffectInstance> getAllEffects() {
        return this.effects.stream().map(MobEffectInstance::new).toList();
    }
}
