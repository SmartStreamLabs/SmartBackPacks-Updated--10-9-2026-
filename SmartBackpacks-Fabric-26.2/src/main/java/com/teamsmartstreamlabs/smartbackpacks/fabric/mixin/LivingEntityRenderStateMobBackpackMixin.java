package com.teamsmartstreamlabs.smartbackpacks.fabric.mixin;

import com.teamsmartstreamlabs.smartbackpacks.client.render.MobBackpackRenderStateAccess;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMobBackpackMixin implements MobBackpackRenderStateAccess {
    @Unique
    private ItemStack smartbackpacks$mobBackpack = ItemStack.EMPTY;

    @Override
    public ItemStack smartbackpacks$getMobBackpack() {
        return this.smartbackpacks$mobBackpack;
    }

    @Override
    public void smartbackpacks$setMobBackpack(ItemStack backpack) {
        this.smartbackpacks$mobBackpack = backpack.copy();
    }
}
