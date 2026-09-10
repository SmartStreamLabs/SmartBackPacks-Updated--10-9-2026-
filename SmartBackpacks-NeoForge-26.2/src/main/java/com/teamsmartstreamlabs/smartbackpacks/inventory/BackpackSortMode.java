package com.teamsmartstreamlabs.smartbackpacks.inventory;

import java.util.Comparator;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public enum BackpackSortMode {
    MOD("screen.smartbackpacks.sort_mode.mod", "M"),
    TAGS("screen.smartbackpacks.sort_mode.tags", "T"),
    COUNT("screen.smartbackpacks.sort_mode.count", "#");

    private final String translationKey;
    private final String shortLabel;

    BackpackSortMode(String translationKey, String shortLabel) {
        this.translationKey = translationKey;
        this.shortLabel = shortLabel;
    }

    public String getTranslationKey() {
        return this.translationKey;
    }

    public String getShortLabel() {
        return this.shortLabel;
    }

    public BackpackSortMode next() {
        BackpackSortMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

    public Comparator<ItemStack> comparator() {
        Comparator<ItemStack> fallback = Comparator
                .comparing((ItemStack stack) -> BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath())
                .thenComparing(stack -> stack.getHoverName().getString())
                .thenComparing(Comparator.comparingInt(ItemStack::getCount).reversed());

        return switch (this) {
            case TAGS -> Comparator
                    .comparing(BackpackSortMode::getTagSignature)
                    .thenComparing((ItemStack stack) -> BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace())
                    .thenComparing(fallback);
            case COUNT -> Comparator
                    .comparingInt(ItemStack::getCount)
                    .reversed()
                    .thenComparing((ItemStack stack) -> BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace())
                    .thenComparing(fallback);
            case MOD -> Comparator
                    .comparing((ItemStack stack) -> BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace())
                    .thenComparing(fallback);
        };
    }

    private static String getTagSignature(ItemStack stack) {
        return BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem())
                .tags()
                .map(tagKey -> tagKey.location().toString())
                .sorted()
                .collect(Collectors.joining("|"));
    }
}
