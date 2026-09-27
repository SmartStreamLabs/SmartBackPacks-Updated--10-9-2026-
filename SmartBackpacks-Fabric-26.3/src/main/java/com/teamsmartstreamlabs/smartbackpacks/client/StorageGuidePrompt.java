package com.teamsmartstreamlabs.smartbackpacks.client;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class StorageGuidePrompt {
    private static final long HOVER_GRACE_NANOS = 250_000_000L;
    private static volatile long lastHoverNanos;

    private StorageGuidePrompt() {
    }

    public static void append(List<Component> tooltip) {
        append(tooltip::add);
    }

    public static void append(Consumer<Component> tooltip) {
        lastHoverNanos = System.nanoTime();
        tooltip.accept(Component.translatable("tooltip.smartbackpacks.storage_system.summary").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.smartbackpacks.storage_system.guide").withStyle(ChatFormatting.AQUA));
    }

    public static boolean wasHoveredRecently() {
        return System.nanoTime() - lastHoverNanos <= HOVER_GRACE_NANOS;
    }
}
