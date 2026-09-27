package com.teamsmartstreamlabs.smartbackpacks.backpack;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * Persistent backpack storage without vanilla's 256-entry container limit.
 */
public final class BackpackStorageContents {
    private static final int MAX_SUPPORTED_SLOTS = BackpackTier.NETHERITE_VAULT.getSlotCount();

    public static final Codec<BackpackStorageContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("items").forGetter(BackpackStorageContents::items),
            Codec.INT.listOf().fieldOf("overflow").forGetter(BackpackStorageContents::overflow)
    ).apply(instance, BackpackStorageContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BackpackStorageContents> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public BackpackStorageContents decode(RegistryFriendlyByteBuf buffer) {
            int itemCount = readListSize(buffer);
            List<ItemStack> items = new ArrayList<>(itemCount);
            for (int index = 0; index < itemCount; index++) {
                items.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer));
            }

            int overflowCount = readListSize(buffer);
            List<Integer> overflow = new ArrayList<>(overflowCount);
            for (int index = 0; index < overflowCount; index++) {
                overflow.add(buffer.readVarInt());
            }
            return new BackpackStorageContents(items, overflow);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, BackpackStorageContents contents) {
            writeListSize(buffer, contents.items.size());
            for (ItemStack item : contents.items) {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, item);
            }

            writeListSize(buffer, contents.overflow.size());
            for (int count : contents.overflow) {
                buffer.writeVarInt(count);
            }
        }
    };

    private final List<ItemStack> items;
    private final List<Integer> overflow;

    public BackpackStorageContents(List<ItemStack> items, List<Integer> overflow) {
        this.items = copyItems(items);
        this.overflow = List.copyOf(overflow);
    }

    public List<ItemStack> items() {
        return copyItems(this.items);
    }

    public List<Integer> overflow() {
        return this.overflow;
    }

    int storedSlotCount() {
        return this.items.size();
    }

    boolean occupiedAt(int slot) {
        return !this.items.get(slot).isEmpty();
    }

    ItemStack copyStoredItemAt(int slot) {
        return this.items.get(slot).copy();
    }

    int overflowAt(int slot) {
        return slot < this.overflow.size() ? this.overflow.get(slot) : 0;
    }

    public NonNullList<ItemStack> copyInto(int size) {
        NonNullList<ItemStack> copied = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int slot = 0; slot < Math.min(size, this.items.size()); slot++) {
            ItemStack item = this.items.get(slot);
            copied.set(slot, item.isEmpty() ? ItemStack.EMPTY : item.copy());
        }
        return copied;
    }

    private static List<ItemStack> copyItems(List<ItemStack> items) {
        return items.stream().map(item -> item.isEmpty() ? ItemStack.EMPTY : item.copy()).toList();
    }

    private static int readListSize(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        if (size < 0 || size > MAX_SUPPORTED_SLOTS) {
            throw new IllegalArgumentException("Invalid backpack storage size: " + size);
        }
        return size;
    }

    private static void writeListSize(RegistryFriendlyByteBuf buffer, int size) {
        if (size < 0 || size > MAX_SUPPORTED_SLOTS) {
            throw new IllegalArgumentException("Invalid backpack storage size: " + size);
        }
        buffer.writeVarInt(size);
    }
}
