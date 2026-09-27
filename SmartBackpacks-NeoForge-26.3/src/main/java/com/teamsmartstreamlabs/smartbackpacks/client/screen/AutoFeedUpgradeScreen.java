package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import com.teamsmartstreamlabs.smartbackpacks.menu.AutoFeedUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.network.AutoFeedSettingsPayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;

import com.teamsmartstreamlabs.smartbackpacks.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class AutoFeedUpgradeScreen extends LegacyContainerScreen<AutoFeedUpgradeMenu> {
    private static final int PANEL_X = 7;
    private static final int PANEL_Y = 12;
    private static final int PANEL_WIDTH = 162;
    private static final int PANEL_HEIGHT = 112;
    private static final int PANEL_BORDER = 0xFF8F989D;
    private static final int PANEL_FILL = 0xFFD9DDE0;
    private static final int PANEL_HIGHLIGHT = 0xFFF3F5F6;
    private static final int PANEL_SHADOW = 0xFFB4BBC0;
    private static final int SLOT_FILL = 0xFFC9CED1;
    private static final int SLOT_HIGHLIGHT = 0xFFE8EBED;
    private static final int SLOT_SHADOW = 0xFF8D959A;
    private static final int TEXT_COLOR = 0xFF404040;
    private static final int BAR_BORDER = 0xFF61686D;
    private static final int BAR_FILL = 0xFF2E3336;
    private static final int HUNGER_BAR_COLOR = 0xFFD37B34;
    private static final int SATURATION_BAR_COLOR = 0xFF4D9FD1;
    private static final int BAR_WIDTH = 54;
    private static final int BAR_HEIGHT = 10;

    private boolean enabled;
    private int hungerThreshold;
    private int saturationThreshold;
    private Button enabledButton;

    public AutoFeedUpgradeScreen(AutoFeedUpgradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 226);
        this.titleLabelX = PANEL_X + 36;
        this.titleLabelY = PANEL_Y + 4;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 118;
        this.enabled = menu.isEnabled();
        this.hungerThreshold = menu.getHungerThreshold();
        this.saturationThreshold = menu.getSaturationThreshold();
    }

    @Override
    protected void init() {
        super.init();
        this.enabledButton = this.addRenderableWidget(Button.builder(this.getEnabledLabel(), button -> this.toggleEnabled())
                .pos(this.leftPos + 14, this.topPos + 32)
                .size(54, 16)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("-"), button -> this.changeHungerThreshold(-1))
                .pos(this.leftPos + 14, this.topPos + 66)
                .size(16, 16)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), button -> this.changeHungerThreshold(1))
                .pos(this.leftPos + 52, this.topPos + 66)
                .size(16, 16)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("-"), button -> this.changeSaturationThreshold(-1))
                .pos(this.leftPos + 14, this.topPos + 98)
                .size(16, 16)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), button -> this.changeSaturationThreshold(1))
                .pos(this.leftPos + 52, this.topPos + 98)
                .size(16, 16)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        this.drawPanel(guiGraphics, x + PANEL_X, y + PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT);
        guiGraphics.renderItem(new ItemStack(ModItems.AUTO_FEED_UPGRADE.get()), x + 13, y + 14);
        guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.auto_feed.hunger_short"), x + 14, y + 50, TEXT_COLOR, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.auto_feed.saturation_short"), x + 14, y + 82, TEXT_COLOR, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, x + this.inventoryLabelX, y + this.inventoryLabelY, TEXT_COLOR, false);
        this.drawThresholdBar(guiGraphics, x + 14, y + 56, this.hungerThreshold, HUNGER_BAR_COLOR);
        this.drawThresholdBar(guiGraphics, x + 14, y + 88, this.saturationThreshold, SATURATION_BAR_COLOR);
        for (int slot = 0; slot < 16; slot++) {
            int slotX = x + 76 + (slot % 4) * 18;
            int slotY = y + 22 + (slot / 4) * 18;
            this.drawSlotBox(guiGraphics, slotX, slotY, 18, 18);
        }
        this.drawInventoryBackground(guiGraphics, x + 8, y + 128);
    }

    private void toggleEnabled() {
        this.enabled = !this.enabled;
        this.enabledButton.setMessage(this.getEnabledLabel());
        ClientPacketDistributor.sendToServer(new AutoFeedSettingsPayload(AutoFeedSettingsPayload.Action.TOGGLE_ENABLED, this.enabled ? 1 : 0));
    }

    private void changeHungerThreshold(int delta) {
        this.hungerThreshold = Mth.clamp(this.hungerThreshold + delta, 0, 20);
        ClientPacketDistributor.sendToServer(new AutoFeedSettingsPayload(AutoFeedSettingsPayload.Action.SET_HUNGER_THRESHOLD, this.hungerThreshold));
    }

    private void changeSaturationThreshold(int delta) {
        this.saturationThreshold = Mth.clamp(this.saturationThreshold + delta, 0, 20);
        ClientPacketDistributor.sendToServer(new AutoFeedSettingsPayload(AutoFeedSettingsPayload.Action.SET_SATURATION_THRESHOLD, this.saturationThreshold));
    }

    private Component getEnabledLabel() {
        return Component.translatable(this.enabled
                ? "screen.smartbackpacks.auto_feed.enabled"
                : "screen.smartbackpacks.auto_feed.disabled");
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

    private void drawThresholdBar(GuiGraphics guiGraphics, int x, int y, int value, int fillColor) {
        int innerWidth = BAR_WIDTH - 2;
        int clampedValue = Mth.clamp(value, 0, 20);
        int filledWidth = Math.max(0, Math.round(innerWidth * (clampedValue / 20.0F)));
        String label = clampedValue + " / 20";

        guiGraphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, BAR_BORDER);
        guiGraphics.fill(x + 1, y + 1, x + BAR_WIDTH - 1, y + BAR_HEIGHT - 1, BAR_FILL);
        if (filledWidth > 0) {
            guiGraphics.fill(x + 1, y + 1, x + 1 + filledWidth, y + BAR_HEIGHT - 1, fillColor);
        }

        int textX = x + (BAR_WIDTH - this.font.width(label)) / 2;
        guiGraphics.drawString(this.font, label, textX, y + 1, 0xFFFFFFFF, false);
    }
}
