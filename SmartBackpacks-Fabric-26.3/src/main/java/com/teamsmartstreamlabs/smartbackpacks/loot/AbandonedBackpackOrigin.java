package com.teamsmartstreamlabs.smartbackpacks.loot;

import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class AbandonedBackpackOrigin {
    private static final String ORIGIN = "smartbackpacks_abandoned_origin";
    private static final String ID = "smartbackpacks_abandoned_id";
    private static final String DEBUG = "smartbackpacks_abandoned_debug";
    private static final String PREFILLED = "smartbackpacks_abandoned_prefilled";
    private AbandonedBackpackOrigin() {}
    public static void mark(ItemStack stack, boolean debug, boolean prefilled) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putBoolean(ORIGIN, true);
            tag.putString(ID, UUID.randomUUID().toString());
            tag.putBoolean(DEBUG, debug);
            tag.putBoolean(PREFILLED, prefilled);
        });
    }
    private static CompoundTag tag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }
    public static String naturalId(ItemStack stack) {
        CompoundTag tag = tag(stack);
        if (!tag.getBooleanOr(ORIGIN, false) || tag.getBooleanOr(DEBUG, false)) return "";
        String id = tag.getStringOr(ID, "");
        try { return UUID.fromString(id).toString(); } catch (IllegalArgumentException ignored) { return ""; }
    }
    public static boolean prefilled(ItemStack stack) { return tag(stack).getBooleanOr(PREFILLED, false); }
}
