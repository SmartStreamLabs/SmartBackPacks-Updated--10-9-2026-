package com.teamsmartstreamlabs.smartbackpacks.fabric.mixin;

import com.teamsmartstreamlabs.smartbackpacks.client.render.MobBackpackRenderStateAccess;
import com.teamsmartstreamlabs.smartbackpacks.mobbackpack.MobBackpackStore;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMobBackpackMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void smartbackpacks$extractMobBackpack(LivingEntity entity, LivingEntityRenderState renderState,
            float partialTick, CallbackInfo ci) {
        ItemStack backpack = entity instanceof Mob mob ? MobBackpackStore.get(mob).backpack() : ItemStack.EMPTY;
        ((MobBackpackRenderStateAccess) renderState).smartbackpacks$setMobBackpack(backpack);
    }
}
