package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import com.teamsmartstreamlabs.smartbackpacks.menu.XpTransferUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.network.XpTransferActionPayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;

import com.teamsmartstreamlabs.smartbackpacks.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class XpTransferUpgradeScreen extends LegacyContainerScreen<XpTransferUpgradeMenu> {
    private static final int PANEL_X = 7;
    private static final int PANEL_Y = 12;
    private static final int PANEL_WIDTH = 162;
    private static final int PANEL_HEIGHT = 82;
    private static final int PANEL_BORDER = 0xFF8F989D;
    private static final int PANEL_FILL = 0xFFD9DDE0;
    private static final int PANEL_HIGHLIGHT = 0xFFF3F5F6;
    private static final int PANEL_SHADOW = 0xFFB4BBC0;
    private static final int BAR_X = 14;
    private static final int BAR_Y = 40;
    private static final int BAR_WIDTH = 154;
    private static final int BAR_HEIGHT = 12;

    public XpTransferUpgradeScreen(XpTransferUpgradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 208);
        this.titleLabelX = 34;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 96;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.literal("+100 XP"), button ->
                        PacketDistributor.sendToServer(new XpTransferActionPayload(XpTransferActionPayload.Action.STORE_STEP)))
                .pos(this.leftPos + 14, this.topPos + 76)
                .size(52, 16)
                .tooltip(Tooltip.create(Component.literal("Store 100 XP into the tank")))
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("+All"), button ->
                        PacketDistributor.sendToServer(new XpTransferActionPayload(XpTransferActionPayload.Action.STORE_ALL)))
                .pos(this.leftPos + 70, this.topPos + 76)
                .size(38, 16)
                .tooltip(Tooltip.create(Component.literal("Store all current XP")))
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("-100 XP"), button ->
                        PacketDistributor.sendToServer(new XpTransferActionPayload(XpTransferActionPayload.Action.WITHDRAW_STEP)))
                .pos(this.leftPos + 112, this.topPos + 58)
                .size(56, 16)
                .tooltip(Tooltip.create(Component.literal("Withdraw 100 XP from the tank")))
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("-All"), button ->
                        PacketDistributor.sendToServer(new XpTransferActionPayload(XpTransferActionPayload.Action.WITHDRAW_ALL)))
                .pos(this.leftPos + 112, this.topPos + 76)
                .size(56, 16)
                .tooltip(Tooltip.create(Component.literal("Withdraw all stored XP")))
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        this.drawPanel(guiGraphics, x + PANEL_X, y + PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT);
        guiGraphics.renderItem(new ItemStack(ModItems.XP_TRANSFER_UPGRADE.get()), x + 13, y + 14);
        this.drawBar(guiGraphics, x + BAR_X, y + BAR_Y, BAR_WIDTH, BAR_HEIGHT);
        this.drawInventoryBackground(guiGraphics, x + 8, y + 106);
    }

    @Override
    protected void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        guiGraphics.drawString(this.font, Component.literal("Stored XP: " + this.menu.getStoredXpPoints()), 14, 30, 0x404040, false);
        guiGraphics.drawString(this.font, Component.literal("Stored: " + this.menu.getStoredMillibuckets() + " / " + this.menu.getCapacity() + " mB"), 14, 54, 0x404040, false);
        guiGraphics.drawString(this.font, Component.literal("Player XP: " + this.menu.getPlayerXpPoints()), 14, 67, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    private void drawBar(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, 0xFF8D959A);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF394045);
        int fillWidth = this.menu.getCapacity() <= 0 ? 0 : Math.max(1, Math.round((this.menu.getStoredMillibuckets() / (float) this.menu.getCapacity()) * (width - 2)));
        if (this.menu.getStoredMillibuckets() > 0) {
            guiGraphics.fill(x + 1, y + 1, x + 1 + fillWidth, y + height - 1, 0xFFD0D000);
        }
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
        guiGraphics.fill(x, y, x + width, y + height, 0xFF8D959A);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFFC9CED1);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + 2, 0xFFE8EBED);
        guiGraphics.fill(x + 1, y + 1, x + 2, y + height - 1, 0xFFE8EBED);
    }
}
