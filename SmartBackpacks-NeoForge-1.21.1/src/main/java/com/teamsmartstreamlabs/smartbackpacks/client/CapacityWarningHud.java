package com.teamsmartstreamlabs.smartbackpacks.client;

import com.teamsmartstreamlabs.smartbackpacks.network.CapacityWarningSyncPayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacityWarningState;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public final class CapacityWarningHud {
    private static final int DISPLAY_TICKS = 160;
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
        render(event.getGuiGraphics());
    }

    public static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || display == Display.EMPTY || minecraft.player.tickCount > display.expiresAtTick()) {
            return;
        }

        int width = minecraft.getWindow().getGuiScaledWidth();
        int x = Math.max(6, width - PANEL_WIDTH - 8);
        int y = 8;
        int color = colorFor(display.state());
        graphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xAA241A13);
        graphics.fill(x + 1, y + 1, x + PANEL_WIDTH - 1, y + PANEL_HEIGHT - 1, 0xCC3D2D21);
        graphics.renderFakeItem(new ItemStack(ModItems.CAPACITY_WARNING_UPGRADE.get()), x + 4, y + 5);

        String percentage = display.percentage() + "%";
        int percentageX = x + PANEL_WIDTH - 8 - minecraft.font.width(percentage);
        int nameX = x + 24;
        String name = clipToWidth(minecraft, display.name(), Math.max(0, percentageX - nameX - 6));
        graphics.drawString(minecraft.font, Component.literal(name), nameX, y + 4, 0xFFEADCC7, false);
        graphics.drawString(minecraft.font, Component.literal(percentage), percentageX, y + 4, color, false);

        int barX = x + 24;
        int barRight = x + PANEL_WIDTH - 8;
        graphics.fill(barX, y + 16, barRight, y + 20, 0xFF5A4635);
        int fill = Math.round((barRight - barX) * Math.max(0, Math.min(100, display.percentage())) / 100.0F);
        graphics.fill(barX, y + 16, barX + fill, y + 20, color);
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
