package com.teamsmartstreamlabs.smartbackpacks.fabric.mixin;

import com.teamsmartstreamlabs.smartbackpacks.protection.BlockDropProtection;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityDropProtectionMixin {
    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("RETURN"))
    private void smartbackpacks$markDrop(EntityType<? extends ItemEntity> type, Level level, CallbackInfo ci) {
        BlockDropProtection.markNewDrop((ItemEntity) (Object) this);
    }

    @Inject(method = "tryToMerge", at = @At("HEAD"), cancellable = true)
    private void smartbackpacks$preventOwnerMerge(ItemEntity other, CallbackInfo ci) {
        if (!BlockDropProtection.canMerge((ItemEntity) (Object) this, other)) {
            ci.cancel();
        }
    }
}
