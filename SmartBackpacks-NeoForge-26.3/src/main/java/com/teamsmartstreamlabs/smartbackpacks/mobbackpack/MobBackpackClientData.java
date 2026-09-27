package com.teamsmartstreamlabs.smartbackpacks.mobbackpack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.item.ItemStack;

public final class MobBackpackClientData {
    private static final Map<Integer, ItemStack> BACKPACKS = new ConcurrentHashMap<>();

    private MobBackpackClientData() {
    }

    public static void update(int entityId, ItemStack backpack) {
        if (backpack.isEmpty()) BACKPACKS.remove(entityId);
        else BACKPACKS.put(entityId, backpack.copy());
    }

    public static ItemStack get(int entityId) {
        return BACKPACKS.getOrDefault(entityId, ItemStack.EMPTY);
    }

    public static void clear() {
        BACKPACKS.clear();
    }
}
