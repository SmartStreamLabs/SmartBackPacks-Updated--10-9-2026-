package com.teamsmartstreamlabs.smartbackpacks.client.render;

import net.minecraft.world.item.ItemStack;

public interface MobBackpackRenderStateAccess {
    ItemStack smartbackpacks$getMobBackpack();
    void smartbackpacks$setMobBackpack(ItemStack backpack);
}
