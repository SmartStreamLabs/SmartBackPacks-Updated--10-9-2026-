package com.teamsmartstreamlabs.smartbackpacks.mobbackpack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
        CompoundTag data = mob.getPersistentData().getCompoundOrEmpty(ROOT_KEY);
        if (data.isEmpty()) {
            return MobBackpackState.UNPROCESSED;
        }
        ItemStack backpack = data.read(STACK_KEY, ItemStack.CODEC,
                mob.registryAccess().createSerializationContext(NbtOps.INSTANCE)).orElse(ItemStack.EMPTY);
        return new MobBackpackState(data.getBooleanOr(PROCESSED_KEY, false),
                data.getBooleanOr(DROPPED_KEY, false), backpack);
    }

    public static void set(Mob mob, MobBackpackState state) {
        CompoundTag data = new CompoundTag();
        data.putBoolean(PROCESSED_KEY, state.processed());
        data.putBoolean(DROPPED_KEY, state.dropped());
        if (!state.backpack().isEmpty()) {
            data.store(STACK_KEY, ItemStack.CODEC,
                    mob.registryAccess().createSerializationContext(NbtOps.INSTANCE), state.backpack());
        }
        mob.getPersistentData().put(ROOT_KEY, data);
    }
}
