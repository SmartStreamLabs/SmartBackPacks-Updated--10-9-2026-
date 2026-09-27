package com.teamsmartstreamlabs.smartbackpacks.mixin;

import com.teamsmartstreamlabs.smartbackpacks.protection.BlockDropProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class BlockBreakOwnerMixin {
    @Shadow @Final protected ServerPlayer player;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void smartbackpacks$beginBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockDropProtection.beginBreak(this.player);
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void smartbackpacks$endBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockDropProtection.endBreak();
    }
}
