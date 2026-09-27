package com.teamsmartstreamlabs.smartbackpacks.client;

import com.teamsmartstreamlabs.smartbackpacks.network.CapacityWarningSyncPayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacityWarningState;

import net.minecraft.client.Minecraft;
import com.teamsmartstreamlabs.smartbackpacks.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public final class CapacityWarningHud {
    private static final int DISPLAY_TICKS = 50;
    private static final int PANEL_WIDTH = 156;
    private static final int PANEL_HEIGHT = 25;
    private static Display display = Display.EMPTY;

    private CapacityWarningHud() {
    }

    public static void handleSync(CapacityWarningSyncPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!payload.showHud() || minecraft.player == null) {
            display = Display.EMPTY;
            return;
        }

        display = new Display(payload.backpackName(), payload.percentage(), payload.state(),
                payload.occupiedSlots(), payload.totalSlots(), payload.freeSlots(),
                minecraft.player.tickCount + DISPLAY_TICKS);
    }

    public static void render(RenderGuiEvent.Post event) {
        render(new GuiGraphics(event.getGuiGraphics()));
    }

    public static void render(GuiGraphicsExtractor extractor) {
        render(new GuiGraphics(extractor));
    }

    public static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || display == Display.EMPTY || minecraft.player.tickCount > display.expiresAtTick()) {
            return;
        }

        int width = minecraft.getWindow().getGuiScaledWidth();
        boolean full = display.state() == CapacityWarningState.FULL;
        String fullMessage = display.name() + " is full!";
        String percentage = display.percentage() + "%";
        int desiredWidth = full
                ? minecraft.font.width(fullMessage) + 16
                : minecraft.font.width(display.name()) + minecraft.font.width(percentage) + 28;
        int panelWidth = Math.min(Math.max(80, width - 16), Math.max(PANEL_WIDTH, desiredWidth));
        int remaining = display.expiresAtTick() - minecraft.player.tickCount;
        float opacity = Math.min(1.0F, Math.min((DISPLAY_TICKS - remaining) / 8.0F, remaining / 8.0F));
        opacity = Math.max(0.0F, opacity);
        int x = Math.max(8, width - panelWidth - 8) + Math.round((1.0F - opacity) * 24);
        // Keep the temporary notice below the top-right advancement toast stack.
        int y = Math.min(76, Math.max(8, minecraft.getWindow().getGuiScaledHeight() - PANEL_HEIGHT - 8));
        int color = colorFor(display.state());
        graphics.fill(x, y, x + panelWidth, y + PANEL_HEIGHT, withAlpha(0xAA241A13, opacity));
        graphics.fill(x + 1, y + 1, x + panelWidth - 1, y + PANEL_HEIGHT - 1, withAlpha(0xCC3D2D21, opacity));

        int percentageX = x + panelWidth - 8 - minecraft.font.width(percentage);
        int nameX = x + 8;
        String name = full ? clipToWidth(minecraft, fullMessage, panelWidth - 16)
                : clipToWidth(minecraft, display.name(), Math.max(0, percentageX - nameX - 6));
        graphics.drawString(minecraft.font, Component.literal(name), nameX, y + 4, withAlpha(0xFFEADCC7, opacity), false);
        if (!full) {
            graphics.drawString(minecraft.font, Component.literal(percentage), percentageX, y + 4, withAlpha(color, opacity), false);
        }

        int barX = x + 8;
        int barRight = x + panelWidth - 8;
        graphics.fill(barX, y + 16, barRight, y + 20, withAlpha(0xFF5A4635, opacity));
        int fill = Math.round((barRight - barX) * Math.max(0, Math.min(100, display.percentage())) / 100.0F);
        graphics.fill(barX, y + 16, barX + fill, y + 20, withAlpha(color, opacity));
    }

    private static int withAlpha(int color, float opacity) {
        return (Math.round(((color >>> 24) & 0xFF) * opacity) << 24) | (color & 0xFFFFFF);
    }

    private static int colorFor(CapacityWarningState state) {
        return switch (state) {
            case FULL -> 0xFFFF4E42;
            case CRITICAL -> 0xFFFF9F2E;
            case WARNING -> 0xFFFFD84A;
            case NORMAL -> 0xFF63D97A;
        };
    }

    private static String clipToWidth(Minecraft minecraft, String value, int maxWidth) {
        if (minecraft.font.width(value) <= maxWidth) {
            return value;
        }

        int ellipsisWidth = minecraft.font.width("...");
        if (maxWidth <= ellipsisWidth) {
            return "";
        }
        return minecraft.font.plainSubstrByWidth(value, maxWidth - ellipsisWidth) + "...";
    }

    private record Display(String name, int percentage, CapacityWarningState state, int occupiedSlots, int totalSlots, int freeSlots, int expiresAtTick) {
        private static final Display EMPTY = new Display("", 0, CapacityWarningState.NORMAL, 0, 0, 0, 0);
    }
}
