package com.teamsmartstreamlabs.smartbackpacks.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.network.PickupNotifierPayload;
import com.teamsmartstreamlabs.smartbackpacks.pickup.PickupNotifierDestination;

import net.minecraft.client.Minecraft;
import com.teamsmartstreamlabs.smartbackpacks.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public final class PickupNotifierHud {
    private static final int PANEL_WIDTH = 168;
    private static final int PANEL_HEIGHT = 32;
    private static final int SPACING = 4;
    private static final int FADE_TICKS = 8;
    private static final List<Entry> ENTRIES = new ArrayList<>();

    private PickupNotifierHud() {
    }

    public static void handle(PickupNotifierPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !SmartBackpacksConfig.pickupNotifierEnabled() || payload.amount() <= 0 || payload.stack().isEmpty()) {
            return;
        }

        int now = minecraft.player.tickCount;
        int groupingWindow = SmartBackpacksConfig.pickupNotifierGroupingWindowTicks();
        for (Entry entry : ENTRIES) {
            if (entry.matches(payload) && now - entry.lastUpdatedTick <= groupingWindow) {
                entry.amount += payload.amount();
                entry.total = payload.total();
                entry.lastUpdatedTick = now;
                entry.expiresAtTick = now + SmartBackpacksConfig.pickupNotifierDisplayTicks();
                return;
            }
        }

        ENTRIES.add(0, new Entry(payload.stack().copyWithCount(1), payload.amount(), payload.total(),
                payload.backpackName(), payload.destination(), now, now + SmartBackpacksConfig.pickupNotifierDisplayTicks()));
        trimQueue();
    }

    public static void render(RenderGuiEvent.Post event) {
        render(new GuiGraphics(event.getGuiGraphics()));
    }

    public static void render(GuiGraphicsExtractor extractor) {
        render(new GuiGraphics(extractor));
    }

    public static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || ENTRIES.isEmpty()) {
            return;
        }

        int now = minecraft.player.tickCount;
        Iterator<Entry> iterator = ENTRIES.iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            if (now > entry.expiresAtTick + FADE_TICKS) {
                iterator.remove();
            }
        }

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int x = Math.max(6, screenWidth - PANEL_WIDTH - 8);
        int baseY = screenHeight - 54;
        int rendered = 0;
        int maxVisible = SmartBackpacksConfig.pickupNotifierMaxVisible();
        for (Entry entry : ENTRIES) {
            if (rendered >= maxVisible) {
                break;
            }

            int y = baseY - rendered * (PANEL_HEIGHT + SPACING);
            renderEntry(graphics, minecraft, entry, x, y, alphaFor(now, entry));
            rendered++;
        }
    }

    public static void preview() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        int now = minecraft.player.tickCount;
        ENTRIES.add(0, new Entry(new ItemStack(Items.COBBLESTONE), 64, 1248,
                "Netherite Backpack", PickupNotifierDestination.MAIN_STORAGE,
                now, now + SmartBackpacksConfig.pickupNotifierDisplayTicks()));
        trimQueue();
    }

    private static void renderEntry(GuiGraphics graphics, Minecraft minecraft, Entry entry, int x, int y, int alpha) {
        if (alpha <= 0) {
            return;
        }

        int background = argb(alpha * 180 / 255, 0x2E2118);
        int inner = argb(alpha * 210 / 255, 0x4A3627);
        int border = argb(alpha, 0xD7B98F);
        graphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, background);
        graphics.fill(x + 1, y + 1, x + PANEL_WIDTH - 1, y + PANEL_HEIGHT - 1, inner);
        graphics.fill(x, y, x + PANEL_WIDTH, y + 1, border);
        graphics.fill(x, y + PANEL_HEIGHT - 1, x + PANEL_WIDTH, y + PANEL_HEIGHT, border);
        graphics.fill(x, y, x + 1, y + PANEL_HEIGHT, border);
        graphics.fill(x + PANEL_WIDTH - 1, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, border);

        graphics.renderFakeItem(entry.stack, x + 6, y + 8);

        String amount = "+" + entry.amount;
        int amountWidth = minecraft.font.width(amount);
        int textX = x + 28;
        int amountX = x + PANEL_WIDTH - 8 - amountWidth;
        int textMaxWidth = Math.max(0, amountX - textX - 5);
        String itemName = clipToWidth(minecraft, entry.stack.getHoverName().getString(), textMaxWidth);
        graphics.drawString(minecraft.font, Component.literal(itemName), textX, y + 6, argb(alpha, 0xF9E9CD), true);
        graphics.drawString(minecraft.font, Component.literal(amount), amountX, y + 6, argb(alpha, 0xFFE070), true);

        String detail = detailLine(entry);
        graphics.drawString(minecraft.font, Component.literal(clipToWidth(minecraft, detail, PANEL_WIDTH - 34)),
                textX, y + 19, argb(alpha, 0xD7C1A6), false);
    }

    private static String detailLine(Entry entry) {
        String destination = SmartBackpacksConfig.pickupNotifierShowDestination()
                ? Component.translatable(entry.destination.translationKey()).getString()
                : "";
        String total = SmartBackpacksConfig.pickupNotifierShowTotalStoredAmount() && entry.total >= 0
                ? "Total: " + entry.total
                : "";
        String backpack = SmartBackpacksConfig.pickupNotifierShowBackpackName() ? entry.backpackName : "";

        if (!destination.isEmpty() && !total.isEmpty()) {
            return destination + " \u2022 " + total;
        }
        if (!destination.isEmpty() && !backpack.isEmpty()) {
            return destination + " \u2022 " + backpack;
        }
        if (!destination.isEmpty()) {
            return destination;
        }
        if (!total.isEmpty()) {
            return total;
        }
        return backpack;
    }

    private static int alphaFor(int now, Entry entry) {
        if (now <= entry.expiresAtTick) {
            return 255;
        }

        int fadeAge = now - entry.expiresAtTick;
        return Mth.clamp(255 - Math.round(255.0F * fadeAge / FADE_TICKS), 0, 255);
    }

    private static int argb(int alpha, int rgb) {
        return (Mth.clamp(alpha, 0, 255) << 24) | (rgb & 0x00FFFFFF);
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

    private static void trimQueue() {
        int limit = SmartBackpacksConfig.pickupNotifierQueueLimit();
        while (ENTRIES.size() > limit) {
            ENTRIES.remove(ENTRIES.size() - 1);
        }
    }

    private static final class Entry {
        private final ItemStack stack;
        private int amount;
        private int total;
        private final String backpackName;
        private final PickupNotifierDestination destination;
        private int lastUpdatedTick;
        private int expiresAtTick;

        private Entry(ItemStack stack, int amount, int total, String backpackName, PickupNotifierDestination destination,
                int lastUpdatedTick, int expiresAtTick) {
            this.stack = stack;
            this.amount = amount;
            this.total = total;
            this.backpackName = backpackName;
            this.destination = destination;
            this.lastUpdatedTick = lastUpdatedTick;
            this.expiresAtTick = expiresAtTick;
        }

        private boolean matches(PickupNotifierPayload payload) {
            return this.destination == payload.destination()
                    && this.backpackName.equals(payload.backpackName())
                    && ItemStack.isSameItemSameComponents(this.stack, payload.stack());
        }
    }
}
