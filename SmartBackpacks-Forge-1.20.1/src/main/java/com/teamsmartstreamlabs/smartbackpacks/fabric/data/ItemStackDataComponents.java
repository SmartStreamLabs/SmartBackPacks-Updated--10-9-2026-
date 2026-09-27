package com.teamsmartstreamlabs.smartbackpacks.fabric.data;

import com.mojang.serialization.Codec;

import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.core.component.DataComponentType;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.alchemy.PotionContents;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.component.DyedItemColor;

public final class ItemStackDataComponents {
    private static final String ROOT_TAG = "smartbackpacks_components";

    private ItemStackDataComponents() {
    }

    public static <T> T getOrDefault(ItemStack stack, DataComponentType<T> key, T defaultValue) {
        T value = get(stack, key);
        return value != null ? value : defaultValue;
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(ItemStack stack, DataComponentType<T> key) {
        if (key == null || stack.isEmpty()) {
            return null;
        }

        if (key == DataComponents.DAMAGE) {
            return (T) Integer.valueOf(stack.getDamageValue());
        }
        if (key == DataComponents.DYED_COLOR) {
            CompoundTag display = stack.getTagElement("display");
            if (display != null && display.contains("color", Tag.TAG_INT)) {
                return (T) new DyedItemColor(display.getInt("color"), true);
            }
            return null;
        }
        if (key == DataComponents.FOOD) {
            return stack.isEdible() ? (T) stack.getItem().getFoodProperties() : null;
        }
        if (key == DataComponents.BUCKET_ENTITY_DATA) {
            CompoundTag blockEntityTag = stack.getTagElement("BlockEntityTag");
            return blockEntityTag != null ? (T) blockEntityTag.copy() : null;
        }
        if (key == DataComponents.JUKEBOX_PLAYABLE) {
            return stack.getItem() instanceof RecordItem ? (T) Boolean.TRUE : null;
        }
        if (key == DataComponents.POTION_CONTENTS) {
            return (T) PotionContents.fromStack(stack);
        }
        if (key == DataComponents.CONSUMABLE) {
            return stack.isEdible() || !PotionContents.fromStack(stack).getAllEffects().isEmpty() ? (T) Boolean.TRUE : null;
        }

        CompoundTag root = stack.getTagElement(ROOT_TAG);
        if (root == null) {
            return null;
        }

        String tagKey = tagKey(key);
        if (!root.contains(tagKey)) {
            return null;
        }

        Codec<T> codec = key.codec();
        if (codec == null) {
            return null;
        }

        Tag encoded = root.get(tagKey);
        return codec.parse(NbtOps.INSTANCE, encoded).result().orElse(null);
    }

    public static <T> void set(ItemStack stack, DataComponentType<T> key, T value) {
        if (stack.isEmpty() || key == null) {
            return;
        }

        if (key == DataComponents.DAMAGE) {
            stack.setDamageValue(value instanceof Integer integer ? Math.max(0, integer) : 0);
            return;
        }
        if (key == DataComponents.DYED_COLOR) {
            if (value instanceof DyedItemColor dyed) {
                stack.getOrCreateTagElement("display").putInt("color", dyed.rgb());
            } else {
                remove(stack, key);
            }
            return;
        }

        Codec<T> codec = key.codec();
        if (codec == null || value == null) {
            remove(stack, key);
            return;
        }

        Tag encoded = codec.encodeStart(NbtOps.INSTANCE, value).result().orElse(null);
        if (encoded == null) {
            remove(stack, key);
            return;
        }

        stack.getOrCreateTagElement(ROOT_TAG).put(tagKey(key), encoded);
    }

    public static void remove(ItemStack stack, DataComponentType<?> key) {
        if (stack.isEmpty() || key == null) {
            return;
        }

        if (key == DataComponents.DAMAGE) {
            stack.setDamageValue(0);
            return;
        }
        if (key == DataComponents.DYED_COLOR) {
            CompoundTag display = stack.getTagElement("display");
            if (display != null) {
                display.remove("color");
                if (display.isEmpty()) {
                    stack.removeTagKey("display");
                }
            }
            return;
        }

        CompoundTag root = stack.getTagElement(ROOT_TAG);
        if (root == null) {
            return;
        }

        root.remove(tagKey(key));
        if (root.isEmpty()) {
            stack.removeTagKey(ROOT_TAG);
        }
    }

    public static boolean has(ItemStack stack, DataComponentType<?> key) {
        if (stack.isEmpty() || key == null) {
            return false;
        }

        if (key == DataComponents.DAMAGE) {
            return stack.isDamaged();
        }
        if (key == DataComponents.DYED_COLOR) {
            CompoundTag display = stack.getTagElement("display");
            return display != null && display.contains("color", Tag.TAG_INT);
        }
        if (key == DataComponents.FOOD) {
            return stack.isEdible() && stack.getItem().getFoodProperties() != null;
        }
        if (key == DataComponents.BUCKET_ENTITY_DATA) {
            return stack.getTagElement("BlockEntityTag") != null;
        }
        if (key == DataComponents.JUKEBOX_PLAYABLE) {
            return stack.getItem() instanceof RecordItem;
        }
        if (key == DataComponents.POTION_CONTENTS) {
            return !PotionContents.fromStack(stack).getAllEffects().isEmpty();
        }
        if (key == DataComponents.CONSUMABLE) {
            return stack.isEdible() || has(stack, DataComponents.POTION_CONTENTS);
        }

        CompoundTag root = stack.getTagElement(ROOT_TAG);
        return root != null && root.contains(tagKey(key));
    }

    private static String tagKey(DataComponentType<?> key) {
        return key.id().replace(':', '_');
    }
}
