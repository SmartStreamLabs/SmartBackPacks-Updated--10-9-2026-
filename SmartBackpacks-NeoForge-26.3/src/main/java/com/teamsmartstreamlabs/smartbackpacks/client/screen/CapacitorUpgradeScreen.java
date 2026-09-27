package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import java.util.Locale;

import com.teamsmartstreamlabs.smartbackpacks.menu.CapacitorUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.network.CapacitorActionPayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;

import com.teamsmartstreamlabs.smartbackpacks.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class CapacitorUpgradeScreen extends LegacyContainerScreen<CapacitorUpgradeMenu> {
    private static final int PANEL_X = 7;
    private static final int PANEL_Y = 12;
    private static final int PANEL_WIDTH = 162;
    private static final int PANEL_HEIGHT = 68;
    private static final int PANEL_BORDER = 0xFF8F989D;
    private static final int PANEL_FILL = 0xFFD9DDE0;
    private static final int PANEL_HIGHLIGHT = 0xFFF3F5F6;
    private static final int PANEL_SHADOW = 0xFFB4BBC0;
    private static final int SLOT_FILL = 0xFFC9CED1;
    private static final int SLOT_HIGHLIGHT = 0xFFE8EBED;
    private static final int SLOT_SHADOW = 0xFF8D959A;
    private static final int BAR_X = 50;
    private static final int BAR_Y = 20;
    private static final int BAR_WIDTH = 18;
    private static final int BAR_HEIGHT = 46;
    private static final int ITEM_SLOT_X = 14;
    private static final int ITEM_SLOT_Y = 38;

    public CapacitorUpgradeScreen(CapacitorUpgradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 194);
        this.titleLabelX = PANEL_X + 36;
        this.titleLabelY = PANEL_Y + 4;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 86;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.literal("Charge"), button ->
                        ClientPacketDistributor.sendToServer(new CapacitorActionPayload(CapacitorActionPayload.Action.CHARGE_ITEM)))
                .pos(this.leftPos + 76, this.topPos + 24)
                .size(48, 16)
                .tooltip(Tooltip.create(Component.literal("Charge the item from stored FE")))
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("Store"), button ->
                        ClientPacketDistributor.sendToServer(new CapacitorActionPayload(CapacitorActionPayload.Action.STORE_ITEM)))
                .pos(this.leftPos + 76, this.topPos + 45)
                .size(48, 16)
                .tooltip(Tooltip.create(Component.literal("Move FE from the item into the capacitor")))
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        this.drawPanel(guiGraphics, x + PANEL_X, y + PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT);
        this.drawSlotBox(guiGraphics, x + ITEM_SLOT_X, y + ITEM_SLOT_Y, 18, 18);
        this.drawBarBox(guiGraphics, x + BAR_X, y + BAR_Y, BAR_WIDTH, BAR_HEIGHT);
        guiGraphics.renderItem(new ItemStack(ModItems.CAPACITOR_UPGRADE.get()), x + 13, y + 14);
        this.drawInventoryBackground(guiGraphics, x + 8, y + 96);
        this.drawEnergyBar(guiGraphics, x + BAR_X, y + BAR_Y, BAR_WIDTH, BAR_HEIGHT);
    }

    @Override
    protected void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.isHovering(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT, mouseX, mouseY)) {
            guiGraphics.setTooltipForNextFrame(
                    this.font,
                    Component.literal(String.format(Locale.ROOT, "%,d / %,d FE", this.menu.getEnergyStored(), this.menu.getCapacity())),
                    mouseX,
                    mouseY
            );
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        guiGraphics.drawString(
                this.font,
                Component.literal(String.format(Locale.ROOT, "%,d / %,d FE", this.menu.getEnergyStored(), this.menu.getCapacity())),
                44,
                64,
                0x404040,
                false
        );
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    private void drawEnergyBar(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        if (this.menu.getEnergyStored() <= 0 || this.menu.getCapacity() <= 0) {
            return;
        }

        int fillHeight = Math.max(1, Math.round((this.menu.getEnergyStored() / (float) this.menu.getCapacity()) * (height - 2)));
        guiGraphics.fill(x + 1, y + height - 1 - fillHeight, x + width - 1, y + height - 1, 0xFF67C75B);
    }

    private void drawInventoryBackground(GuiGraphics guiGraphics, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.drawSlotBox(guiGraphics, x + column * 18, y + row * 18, 18, 18);
            }
        }

        for (int column = 0; column < 9; column++) {
            this.drawSlotBox(guiGraphics, x + column * 18, y + 58, 18, 18);
        }
    }

    private void drawPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, PANEL_BORDER);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_FILL);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + 2, PANEL_HIGHLIGHT);
        guiGraphics.fill(x + 1, y + 1, x + 2, y + height - 1, PANEL_HIGHLIGHT);
        guiGraphics.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, PANEL_SHADOW);
        guiGraphics.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, PANEL_SHADOW);
    }

    private void drawSlotBox(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, SLOT_SHADOW);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, SLOT_FILL);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + 2, SLOT_HIGHLIGHT);
        guiGraphics.fill(x + 1, y + 1, x + 2, y + height - 1, SLOT_HIGHLIGHT);
    }

    private void drawBarBox(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, SLOT_SHADOW);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF394045);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + 2, SLOT_HIGHLIGHT);
        guiGraphics.fill(x + 1, y + 1, x + 2, y + height - 1, SLOT_HIGHLIGHT);
    }
}
