package com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.core.component;

import com.mojang.serialization.Codec;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.food.FoodProperties;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.alchemy.PotionContents;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.component.DyedItemColor;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.component.ItemContainerContents;

public final class DataComponents {
    public static final DataComponentType<ItemContainerContents> CONTAINER =
            new DataComponentType<>("minecraft:container", ItemContainerContents.CODEC);
    public static final DataComponentType<DyedItemColor> DYED_COLOR =
            new DataComponentType<>("minecraft:dyed_color", DyedItemColor.CODEC);
    public static final DataComponentType<Integer> DAMAGE =
            new DataComponentType<>("minecraft:damage", Codec.INT);
    public static final DataComponentType<FoodProperties> FOOD =
            new DataComponentType<>("minecraft:food", null);
    public static final DataComponentType<CompoundTag> BUCKET_ENTITY_DATA =
            new DataComponentType<>("minecraft:bucket_entity_data", CompoundTag.CODEC);
    public static final DataComponentType<PotionContents> POTION_CONTENTS =
            new DataComponentType<>("minecraft:potion_contents", null);
    public static final DataComponentType<Object> JUKEBOX_PLAYABLE =
            new DataComponentType<>("minecraft:jukebox_playable", null);
    public static final DataComponentType<Object> CONSUMABLE =
            new DataComponentType<>("minecraft:consumable", null);

    private DataComponents() {
    }
}
