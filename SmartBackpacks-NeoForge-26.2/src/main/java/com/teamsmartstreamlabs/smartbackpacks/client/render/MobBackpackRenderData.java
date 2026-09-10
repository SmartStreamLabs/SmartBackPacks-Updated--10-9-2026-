package com.teamsmartstreamlabs.smartbackpacks.client.render;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;

public final class MobBackpackRenderData {
    public static final ContextKey<ItemStack> BACKPACK = new ContextKey<>(
            Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "mob_backpack"));

    private MobBackpackRenderData() {
    }
}
