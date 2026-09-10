package com.teamsmartstreamlabs.smartbackpacks.mobbackpack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

public final class MobBackpackStore {
    private static final String ROOT_KEY = "smartbackpacks:mob_backpack";
    private static final String PROCESSED_KEY = "processed";
    private static final String DROPPED_KEY = "dropped";
    private static final String STACK_KEY = "backpack";

    private MobBackpackStore() {
    }

    public static MobBackpackState get(Mob mob) {
        CompoundTag persistentData = mob.getPersistentData();
        if (!persistentData.contains(ROOT_KEY, Tag.TAG_COMPOUND)) {
            return MobBackpackState.UNPROCESSED;
        }

        CompoundTag data = persistentData.getCompound(ROOT_KEY);
        ItemStack backpack = data.contains(STACK_KEY, Tag.TAG_COMPOUND)
                ? ItemStack.of(data.getCompound(STACK_KEY))
                : ItemStack.EMPTY;
        return new MobBackpackState(data.getBoolean(PROCESSED_KEY), data.getBoolean(DROPPED_KEY), backpack);
    }

    public static void set(Mob mob, MobBackpackState state) {
        CompoundTag data = new CompoundTag();
        data.putBoolean(PROCESSED_KEY, state.processed());
        data.putBoolean(DROPPED_KEY, state.dropped());
        if (!state.backpack().isEmpty()) {
            data.put(STACK_KEY, state.backpack().save(new CompoundTag()));
        }
        mob.getPersistentData().put(ROOT_KEY, data);
    }
}
