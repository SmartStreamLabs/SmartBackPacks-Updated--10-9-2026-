package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import com.teamsmartstreamlabs.smartbackpacks.menu.JukeboxUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.network.JukeboxUpgradeActionPayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;

import com.teamsmartstreamlabs.smartbackpacks.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class JukeboxUpgradeScreen extends LegacyContainerScreen<JukeboxUpgradeMenu> {
    private static final int PANEL_X = 7;
    private static final int PANEL_Y = 12;
    private static final int PANEL_WIDTH = 150;
    private static final int PANEL_HEIGHT = 72;
    private static final int PANEL_BORDER = 0xFF8F989D;
    private static final int PANEL_FILL = 0xFFD9DDE0;
    private static final int PANEL_HIGHLIGHT = 0xFFF3F5F6;
    private static final int PANEL_SHADOW = 0xFFB4BBC0;
    private static final int SLOT_FILL = 0xFFC9CED1;
    private static final int SLOT_HIGHLIGHT = 0xFFE8EBED;
    private static final int SLOT_SHADOW = 0xFF8D959A;
    private Button stopButton;
    private Button playButton;

    public JukeboxUpgradeScreen(JukeboxUpgradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 182);
        this.titleLabelX = PANEL_X + 33;
        this.titleLabelY = PANEL_Y + 8;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 78;
    }

    @Override
    protected void init() {
        super.init();
        this.stopButton = this.addRenderableWidget(Button.builder(Component.literal("\u25A0"), button ->
                        ClientPacketDistributor.sendToServer(new JukeboxUpgradeActionPayload(JukeboxUpgradeActionPayload.Action.STOP)))
                .pos(this.leftPos + 48, this.topPos + 58)
                .size(20, 20)
                .tooltip(Tooltip.create(Component.literal("Stop")))
                .build());
        this.playButton = this.addRenderableWidget(Button.builder(Component.literal(">"), button ->
                        ClientPacketDistributor.sendToServer(new JukeboxUpgradeActionPayload(JukeboxUpgradeActionPayload.Action.PLAY)))
                .pos(this.leftPos + 72, this.topPos + 58)
                .size(20, 20)
                .tooltip(Tooltip.create(Component.literal("Play")))
                .build());
        this.updateButtons();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        this.drawPanel(guiGraphics, x + PANEL_X, y + PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT);
        this.drawSlotBox(guiGraphics, x + 12, y + 18, 18, 18);
        this.drawSlotBox(guiGraphics, x + 18, y + 40, 18, 18);

        guiGraphics.renderItem(new ItemStack(ModItems.JUKEBOX_UPGRADE.get()), x + 13, y + 19);
        this.drawInventoryBackground(guiGraphics, x + 7, y + 90);
    }

    @Override
    protected void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.updateButtons();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    private void updateButtons() {
        if (this.stopButton != null) {
            this.stopButton.active = this.menu.hasDisc() && this.menu.isPlaying();
        }
        if (this.playButton != null) {
            this.playButton.active = this.menu.hasDisc() && !this.menu.isPlaying();
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
        guiGraphics.fill(x, y, x + width, y + height, SLOT_SHADOW);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, SLOT_FILL);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + 2, SLOT_HIGHLIGHT);
        guiGraphics.fill(x + 1, y + 1, x + 2, y + height - 1, SLOT_HIGHLIGHT);
    }
}
