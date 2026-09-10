package com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.component;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public final class ItemContainerContents {
    public static final ItemContainerContents EMPTY = new ItemContainerContents(List.of());
    public static final Codec<ItemContainerContents> CODEC = ItemStack.CODEC.listOf()
            .xmap(ItemContainerContents::new, ItemContainerContents::asList);

    private final List<ItemStack> stacks;

    private ItemContainerContents(List<ItemStack> stacks) {
        this.stacks = stacks.stream().map(ItemStack::copy).toList();
    }

    public static ItemContainerContents fromItems(NonNullList<ItemStack> items) {
        return new ItemContainerContents(items);
    }

    public static ItemContainerContents fromItems(List<ItemStack> items) {
        return new ItemContainerContents(items);
    }

    public void copyInto(NonNullList<ItemStack> target) {
        for (int index = 0; index < target.size(); index++) {
            ItemStack stack = index < this.stacks.size() ? this.stacks.get(index).copy() : ItemStack.EMPTY;
            target.set(index, stack);
        }
    }

    public Stream<ItemStack> stream() {
        return this.stacks.stream().map(ItemStack::copy);
    }

    public List<ItemStack> asList() {
        return this.stacks.stream().map(ItemStack::copy).toList();
    }
}
