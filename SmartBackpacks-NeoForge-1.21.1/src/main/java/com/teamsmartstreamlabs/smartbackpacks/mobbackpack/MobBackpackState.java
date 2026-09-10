package com.teamsmartstreamlabs.smartbackpacks.mobbackpack;

import net.minecraft.world.item.ItemStack;

public record MobBackpackState(boolean processed, boolean dropped, ItemStack backpack) {
    public static final MobBackpackState UNPROCESSED = new MobBackpackState(false, false, ItemStack.EMPTY);

    public MobBackpackState {
        backpack = backpack.copy();
    }

    public MobBackpackState withDropped() {
        return new MobBackpackState(true, true, ItemStack.EMPTY);
    }
}
