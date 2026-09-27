package com.teamsmartstreamlabs.smartbackpacks.client;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackContentPreview;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class BackpackPreviewTooltip implements ClientTooltipComponent {
    private static final int COLUMNS = 4;
    private static final int CELL_WIDTH = 64;
    private static final int CELL_HEIGHT = 20;
    private final BackpackContentPreview preview;

    public BackpackPreviewTooltip(BackpackContentPreview preview) {
        this.preview = preview;
    }

    @Override
    public int getWidth(Font font) {
        return Math.max(COLUMNS * CELL_WIDTH, Math.max(font.width(slotsText()), font.width(moreText())));
    }

    @Override
    public int getHeight(Font font) {
        return 12 + rows() * CELL_HEIGHT + 20 + (remaining() > 0 ? 10 : 0);
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        graphics.text(font, Component.translatable("tooltip.smartbackpacks.preview_contents"), x, y, 0xFFAAAAAA);
        if (preview.items().isEmpty()) {
            graphics.text(font, Component.translatable("tooltip.smartbackpacks.preview_empty"), x, y + 12, 0xFFAAAAAA);
        } else {
            for (int index = 0; index < preview.items().size(); index++) {
                ItemStack item = preview.items().get(index);
                int iconX = x + (index % COLUMNS) * CELL_WIDTH;
                int iconY = y + 12 + (index / COLUMNS) * CELL_HEIGHT;
                graphics.item(item, iconX, iconY);
                graphics.itemDecorations(font, item, iconX, iconY, Integer.toString(item.getCount()));
            }
        }
        int footerY = y + 12 + rows() * CELL_HEIGHT;
        graphics.text(font, slotsText(), x, footerY, 0xFFAAAAAA);
        if (remaining() > 0) {
            graphics.text(font, moreText(), x, footerY + 10, 0xFFAAAAAA);
        }
    }

    private int rows() {
        return Math.max(1, (preview.items().size() + COLUMNS - 1) / COLUMNS);
    }

    private int remaining() {
        return preview.usedSlots() - preview.items().size();
    }

    private Component slotsText() {
        return Component.translatable("tooltip.smartbackpacks.preview_slots", preview.usedSlots(), preview.totalSlots());
    }

    private Component moreText() {
        return Component.translatable("tooltip.smartbackpacks.preview_more", remaining());
    }
}
