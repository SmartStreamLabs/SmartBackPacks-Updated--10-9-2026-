package com.teamsmartstreamlabs.smartbackpacks.loot;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class AbandonedBackpackOrigin {
    private static final String ORIGIN = "smartbackpacks_abandoned_origin";
    private static final String ID = "smartbackpacks_abandoned_id";
    private static final String DEBUG = "smartbackpacks_abandoned_debug";
    private static final String PREFILLED = "smartbackpacks_abandoned_prefilled";
    private AbandonedBackpackOrigin() {}
    public static void mark(ItemStack stack, boolean debug, boolean prefilled) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(ORIGIN, true);
        tag.putString(ID, UUID.randomUUID().toString());
        tag.putBoolean(DEBUG, debug);
        tag.putBoolean(PREFILLED, prefilled);
    }
    private static CompoundTag tag(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? new CompoundTag() : tag;
    }
    public static String naturalId(ItemStack stack) {
        CompoundTag tag = tag(stack);
        if (!tag.getBoolean(ORIGIN) || tag.getBoolean(DEBUG)) return "";
        String id = tag.getString(ID);
        try { return UUID.fromString(id).toString(); } catch (IllegalArgumentException ignored) { return ""; }
    }
    public static boolean prefilled(ItemStack stack) { return tag(stack).getBoolean(PREFILLED); }
}
