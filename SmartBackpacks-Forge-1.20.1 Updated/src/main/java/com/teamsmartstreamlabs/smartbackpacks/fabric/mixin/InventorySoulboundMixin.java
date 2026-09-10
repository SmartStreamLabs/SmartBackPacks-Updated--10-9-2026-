package com.teamsmartstreamlabs.smartbackpacks.fabric.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.teamsmartstreamlabs.smartbackpacks.upgrade.SoulboundUpgradeHandler;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(Inventory.class)
public abstract class InventorySoulboundMixin {
    @Shadow
    @Final
    public NonNullList<ItemStack> items;

    @Shadow
    @Final
    public NonNullList<ItemStack> armor;

    @Shadow
    @Final
    public NonNullList<ItemStack> offhand;

    @Shadow
    @Final
    public Player player;

    @Inject(method = "dropAll", at = @At("HEAD"))
    private void smartbackpacks$captureSoulboundBackpacks(CallbackInfo ci) {
        SoulboundUpgradeHandler.captureInventoryDrops(this.player, List.of(this.items, this.armor, this.offhand));
    }
}
