package com.teamsmartstreamlabs.smartbackpacks.fabric.mixin;

import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererBackpackMixin {
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;matches(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean smartbackpacks$treatBackpacksWithInternalStateAsSame(ItemStack oldStack, ItemStack newStack) {
        if (oldStack.getItem() instanceof BackpackItem
                && newStack.getItem() instanceof BackpackItem
                && oldStack.getItem() == newStack.getItem()
                && oldStack.getCount() == newStack.getCount()) {
            return true;
        }

        return ItemStack.matches(oldStack, newStack);
    }
}
