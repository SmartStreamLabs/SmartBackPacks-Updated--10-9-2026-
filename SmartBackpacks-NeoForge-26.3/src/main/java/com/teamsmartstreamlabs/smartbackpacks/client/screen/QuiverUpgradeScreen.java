package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import com.teamsmartstreamlabs.smartbackpacks.menu.QuiverUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.network.QuiverSettingsPayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.QuiverUpgradeData;

import com.teamsmartstreamlabs.smartbackpacks.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class QuiverUpgradeScreen extends LegacyContainerScreen<QuiverUpgradeMenu> {
    private static final int TEXT_COLOR = 0xFF3D3127;
    private static final int PANEL_BORDER = 0xFF3D2D21;
    private static final int PANEL_FILL = 0xFFD2BC99;
    private static final int PANEL_LIGHT = 0xFFE6D3B0;
    private static final int PANEL_DARK = 0xFF9A7B59;
    private static final int SLOT_FILL = 0xFFE2CFA9;
    private static final int SLOT_SELECTED = 0xFF75D66B;
    private static final int CONTROL_LEFT_X = 8;
    private static final int CONTROL_RIGHT_X = 114;
    private static final int CONTROL_WIDTH = 98;
    private static final int ENABLED_BUTTON_X = 150;
    private static final int MODE_ROW_Y = 82;
    private static final int PREFERRED_ROW_Y = 108;
    private static final int PRIORITY_LABEL_Y = 128;
    private static final int PRIORITY_ROW_Y = 142;

    private Button enabledButton;
    private Button modeButton;
    private Button sourceButton;

    public QuiverUpgradeScreen(QuiverUpgradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 220, 264);
        this.titleLabelX = 42;
        this.titleLabelY = 10;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = QuiverUpgradeMenu.INVENTORY_LABEL_Y;
    }

    @Override
    protected void init() {
        super.init();
        this.enabledButton = this.addRenderableWidget(Button.builder(this.enabledLabel(), button -> this.toggleEnabled())
                .bounds(this.leftPos + ENABLED_BUTTON_X, this.topPos + 8, 62, 18)
                .build());
        this.modeButton = this.addRenderableWidget(Button.builder(this.modeLabel(), button -> this.cycleMode())
                .bounds(this.leftPos + CONTROL_LEFT_X, this.topPos + MODE_ROW_Y, CONTROL_WIDTH, 18)
                .build());
        this.sourceButton = this.addRenderableWidget(Button.builder(this.sourceLabel(), button -> this.cycleSource())
                .bounds(this.leftPos + CONTROL_RIGHT_X, this.topPos + MODE_ROW_Y, CONTROL_WIDTH, 18)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.smartbackpacks.quiver.reset"), button -> this.resetSettings())
                .bounds(this.leftPos + ENABLED_BUTTON_X, this.topPos + 28, 62, 16)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("<"), button -> this.changePreferred(-1))
                .bounds(this.leftPos + CONTROL_LEFT_X, this.topPos + PREFERRED_ROW_Y, 22, 16)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal(">"), button -> this.changePreferred(1))
                .bounds(this.leftPos + 32, this.topPos + PREFERRED_ROW_Y, 22, 16)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.smartbackpacks.quiver.priority_up"), button -> this.movePriority(-1))
                .bounds(this.leftPos + CONTROL_LEFT_X, this.topPos + PRIORITY_ROW_Y, CONTROL_WIDTH, 18)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.smartbackpacks.quiver.priority_down"), button -> this.movePriority(1))
                .bounds(this.leftPos + CONTROL_RIGHT_X, this.topPos + PRIORITY_ROW_Y, CONTROL_WIDTH, 18)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        this.drawPanel(guiGraphics, x, y, this.imageWidth, this.imageHeight);
        guiGraphics.renderItem(new ItemStack(ModItems.QUIVER_UPGRADE.get()), x + 10, y + 8);

        for (int slot = 0; slot < QuiverUpgradeData.SLOT_COUNT; slot++) {
            int slotX = x + QuiverUpgradeMenu.QUIVER_SLOT_X + slot * 18;
            int slotY = y + QuiverUpgradeMenu.QUIVER_SLOT_Y;
            this.drawSlotBox(guiGraphics, slotX, slotY, slot == this.menu.getPreferredSlot());
        }

        this.drawInventoryBackground(guiGraphics, x + QuiverUpgradeMenu.PLAYER_INVENTORY_X, y + QuiverUpgradeMenu.PLAYER_INVENTORY_Y);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, TEXT_COLOR, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.quiver.projectiles"), 8, 36, TEXT_COLOR, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.quiver.preferred",
                this.menu.getPreferredSlot() + 1, this.menu.getPreferredPriorityIndex() + 1), 58, PREFERRED_ROW_Y + 4, TEXT_COLOR, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.quiver.priority"), 8, PRIORITY_LABEL_Y, TEXT_COLOR, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, TEXT_COLOR, false);
    }

    private void toggleEnabled() {
        this.menu.setEnabled(!this.menu.isEnabled());
        this.enabledButton.setMessage(this.enabledLabel());
        ClientPacketDistributor.sendToServer(new QuiverSettingsPayload(QuiverSettingsPayload.Action.TOGGLE_ENABLED, this.menu.isEnabled() ? 1 : 0));
    }

    private void cycleMode() {
        this.menu.cycleSelectionMode();
        this.modeButton.setMessage(this.modeLabel());
        ClientPacketDistributor.sendToServer(new QuiverSettingsPayload(QuiverSettingsPayload.Action.CYCLE_SELECTION_MODE, 0));
    }

    private void cycleSource() {
        this.menu.cycleSourcePriority();
        this.sourceButton.setMessage(this.sourceLabel());
        ClientPacketDistributor.sendToServer(new QuiverSettingsPayload(QuiverSettingsPayload.Action.CYCLE_SOURCE_PRIORITY, 0));
    }

    private void changePreferred(int delta) {
        int next = Math.max(0, Math.min(QuiverUpgradeData.SLOT_COUNT - 1, this.menu.getPreferredSlot() + delta));
        this.menu.setPreferredSlot(next);
        ClientPacketDistributor.sendToServer(new QuiverSettingsPayload(QuiverSettingsPayload.Action.SET_PREFERRED_SLOT, next));
    }

    private void movePriority(int delta) {
        this.menu.movePreferredPriority(delta);
        ClientPacketDistributor.sendToServer(new QuiverSettingsPayload(QuiverSettingsPayload.Action.MOVE_PREFERRED_PRIORITY, delta));
    }

    private void resetSettings() {
        this.menu.resetSettings();
        this.enabledButton.setMessage(this.enabledLabel());
        this.modeButton.setMessage(this.modeLabel());
        this.sourceButton.setMessage(this.sourceLabel());
        ClientPacketDistributor.sendToServer(new QuiverSettingsPayload(QuiverSettingsPayload.Action.RESET_SETTINGS, 0));
    }

    private Component enabledLabel() {
        return Component.translatable(this.menu.isEnabled()
                ? "screen.smartbackpacks.quiver.enabled"
                : "screen.smartbackpacks.quiver.disabled");
    }

    private Component modeLabel() {
        return Component.translatable("screen.smartbackpacks.quiver.mode." + this.menu.getSelectionMode().getSerializedName());
    }

    private Component sourceLabel() {
        return Component.translatable("screen.smartbackpacks.quiver.source." + this.menu.getSourcePriority().getSerializedName());
    }

    private void drawPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, PANEL_BORDER);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_FILL);
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + 3, PANEL_LIGHT);
        guiGraphics.fill(x + 2, y + height - 3, x + width - 2, y + height - 2, PANEL_DARK);
    }

    private void drawSlotBox(GuiGraphics guiGraphics, int x, int y, boolean selected) {
        guiGraphics.fill(x, y, x + 18, y + 18, selected ? SLOT_SELECTED : PANEL_DARK);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, SLOT_FILL);
        guiGraphics.fill(x + 2, y + 2, x + 16, y + 3, PANEL_LIGHT);
        guiGraphics.fill(x + 2, y + 2, x + 3, y + 16, PANEL_LIGHT);
    }

    private void drawInventoryBackground(GuiGraphics guiGraphics, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.drawSlotBox(guiGraphics, x + column * 18, y + row * 18, false);
            }
        }

        for (int column = 0; column < 9; column++) {
            this.drawSlotBox(guiGraphics, x + column * 18, y + 58, false);
        }
    }
}
