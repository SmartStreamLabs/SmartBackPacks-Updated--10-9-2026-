package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import com.teamsmartstreamlabs.smartbackpacks.menu.CapacityWarningUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.network.CapacityWarningSettingsPayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacityWarningSnapshot;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacityWarningState;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacityWarningUpgradeData;

import com.teamsmartstreamlabs.smartbackpacks.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class CapacityWarningUpgradeScreen extends LegacyContainerScreen<CapacityWarningUpgradeMenu> {
    private static final int TEXT_COLOR = 0xFF3D3127;
    private static final int MUTED_TEXT = 0xFF6E5740;
    private static final int PANEL_BORDER = 0xFF3D2D21;
    private static final int PANEL_FILL = 0xFFD2BC99;
    private static final int PANEL_LIGHT = 0xFFE6D3B0;
    private static final int PANEL_DARK = 0xFF9A7B59;
    private static final int BAR_BG = 0xFF6B5440;
    private static final int BUTTON_H = 18;

    private Button enabledButton;
    private Button modeButton;
    private Button hudModeButton;
    private Button actionButton;
    private Button soundButton;
    private Button hudButton;
    private Button stepButton;
    private Button failedButton;
    private final Button[] thresholdButtons = new Button[3];

    public CapacityWarningUpgradeScreen(CapacityWarningUpgradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 240, 178);
        this.titleLabelX = 40;
        this.titleLabelY = 11;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        this.enabledButton = this.addRenderableWidget(Button.builder(this.enabledLabel(), button -> this.toggleEnabled())
                .bounds(this.leftPos + 166, this.topPos + 8, 66, BUTTON_H)
                .build());
        this.modeButton = this.addRenderableWidget(Button.builder(this.modeLabel(), button -> this.cycle(CapacityWarningSettingsPayload.Action.CYCLE_CALCULATION_MODE))
                .bounds(this.leftPos + 8, this.topPos + 34, 108, BUTTON_H)
                .build());
        this.actionButton = this.addRenderableWidget(Button.builder(this.toggleLabel("actionbar", this.menu.getSettings().actionBar()), button -> this.toggle(CapacityWarningSettingsPayload.Action.TOGGLE_ACTION_BAR))
                .bounds(this.leftPos + 8, this.topPos + 56, 70, BUTTON_H)
                .build());
        this.soundButton = this.addRenderableWidget(Button.builder(this.toggleLabel("sound", this.menu.getSettings().sound()), button -> this.toggle(CapacityWarningSettingsPayload.Action.TOGGLE_SOUND))
                .bounds(this.leftPos + 85, this.topPos + 56, 70, BUTTON_H)
                .build());
        this.hudButton = this.addRenderableWidget(Button.builder(this.toggleLabel("hud", this.menu.getSettings().hud()), button -> this.toggle(CapacityWarningSettingsPayload.Action.TOGGLE_HUD))
                .bounds(this.leftPos + 162, this.topPos + 56, 70, BUTTON_H)
                .build());
        this.stepButton = this.addRenderableWidget(Button.builder(this.stepLabel(), button -> {
            this.change(CapacityWarningSettingsPayload.Action.CHANGE_THRESHOLD_1, 1);
            this.updateButtons();
        }).bounds(this.leftPos + 8, this.topPos + 80, 224, BUTTON_H).build());
        this.failedButton = this.addRenderableWidget(Button.builder(this.toggleLabel("failed", this.menu.getSettings().failedInsertionWarning()), button -> this.toggle(CapacityWarningSettingsPayload.Action.TOGGLE_FAILED_INSERTION))
                .bounds(this.leftPos + 96, this.topPos + 146, 68, BUTTON_H)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.smartbackpacks.capacity_warning.preview"), button ->
                ClientPacketDistributor.sendToServer(new CapacityWarningSettingsPayload(CapacityWarningSettingsPayload.Action.PREVIEW, 0)))
                .bounds(this.leftPos + 170, this.topPos + 146, 62, BUTTON_H)
                .build());
    }

    private void addThresholdRow(int index, int y, CapacityWarningSettingsPayload.Action toggleAction, CapacityWarningSettingsPayload.Action changeAction) {
        this.thresholdButtons[index] = this.addRenderableWidget(Button.builder(this.thresholdToggleLabel(index), button -> this.toggle(toggleAction))
                .bounds(this.leftPos + 8, this.topPos + y, 54, BUTTON_H)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("-"), button -> this.change(changeAction, -1))
                .bounds(this.leftPos + 68, this.topPos + y, 22, BUTTON_H)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), button -> this.change(changeAction, 1))
                .bounds(this.leftPos + 94, this.topPos + y, 22, BUTTON_H)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        this.drawPanel(guiGraphics, x, y, this.imageWidth, this.imageHeight);
        guiGraphics.renderItem(new ItemStack(ModItems.CAPACITY_WARNING_UPGRADE.get()), x + 10, y + 8);

        CapacityWarningSnapshot snapshot = this.menu.getCurrentSnapshot();
        CapacityWarningState state = snapshot.stateFor(this.menu.getSettings());
        int barX = x + 8;
        int barY = y + 28;
        int barW = 224;
        guiGraphics.fill(barX, barY, barX + barW, barY + 5, PANEL_DARK);
        guiGraphics.fill(barX + 1, barY + 1, barX + barW - 1, barY + 4, BAR_BG);
        guiGraphics.fill(barX + 1, barY + 1, barX + 1 + Math.round((barW - 2) * snapshot.displayPercentage() / 100.0F), barY + 4, this.stateColor(state));
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        CapacityWarningUpgradeData data = this.menu.getSettings();
        CapacityWarningSnapshot snapshot = this.menu.getCurrentSnapshot();
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, TEXT_COLOR, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.capacity_warning.capacity",
                snapshot.displayPercentage(), snapshot.occupiedSlots(), snapshot.totalSlots()), 8, 166, MUTED_TEXT, false);
    }

    private void toggleEnabled() {
        boolean next = !this.menu.getSettings().enabled();
        this.menu.setEnabled(next);
        this.enabledButton.setMessage(this.enabledLabel());
        ClientPacketDistributor.sendToServer(new CapacityWarningSettingsPayload(CapacityWarningSettingsPayload.Action.TOGGLE_ENABLED, next ? 1 : 0));
    }

    private void cycle(CapacityWarningSettingsPayload.Action action) {
        if (action == CapacityWarningSettingsPayload.Action.CYCLE_CALCULATION_MODE) {
            this.menu.cycleCalculationMode();
        } else if (action == CapacityWarningSettingsPayload.Action.CYCLE_HUD_MODE) {
            this.menu.cycleHudMode();
        }
        this.updateButtons();
        ClientPacketDistributor.sendToServer(new CapacityWarningSettingsPayload(action, 0));
    }

    private void toggle(CapacityWarningSettingsPayload.Action action) {
        switch (action) {
            case TOGGLE_THRESHOLD_1 -> this.menu.toggleThreshold(0);
            case TOGGLE_THRESHOLD_2 -> this.menu.toggleThreshold(1);
            case TOGGLE_THRESHOLD_3 -> this.menu.toggleThreshold(2);
            case TOGGLE_ACTION_BAR -> this.menu.toggleActionBar();
            case TOGGLE_SOUND -> this.menu.toggleSound();
            case TOGGLE_HUD -> this.menu.toggleHud();
            case TOGGLE_PERCENTAGE -> this.menu.toggleShowPercentage();
            case TOGGLE_SLOT_COUNT -> this.menu.toggleShowSlotCount();
            case TOGGLE_FAILED_INSERTION -> this.menu.toggleFailedInsertionWarning();
            default -> {
            }
        }
        this.updateButtons();
        ClientPacketDistributor.sendToServer(new CapacityWarningSettingsPayload(action, 0));
    }

    private void change(CapacityWarningSettingsPayload.Action action, int delta) {
        switch (action) {
            case CHANGE_THRESHOLD_1 -> this.menu.changeThreshold(0, delta);
            case CHANGE_THRESHOLD_2 -> this.menu.changeThreshold(1, delta);
            case CHANGE_THRESHOLD_3 -> this.menu.changeThreshold(2, delta);
            case CHANGE_RESET_MARGIN -> this.menu.changeResetMargin(delta);
            default -> {
            }
        }
        ClientPacketDistributor.sendToServer(new CapacityWarningSettingsPayload(action, delta));
    }

    private void updateButtons() {
        this.enabledButton.setMessage(this.enabledLabel());
        this.modeButton.setMessage(this.modeLabel());
        if (this.hudModeButton != null) {
            this.hudModeButton.setMessage(this.hudModeLabel());
        }
        this.actionButton.setMessage(this.toggleLabel("actionbar", this.menu.getSettings().actionBar()));
        this.soundButton.setMessage(this.toggleLabel("sound", this.menu.getSettings().sound()));
        this.hudButton.setMessage(this.toggleLabel("hud", this.menu.getSettings().hud()));
        this.stepButton.setMessage(this.stepLabel());
        this.failedButton.setMessage(this.toggleLabel("failed", this.menu.getSettings().failedInsertionWarning()));
        for (int index = 0; index < this.thresholdButtons.length; index++) {
            if (this.thresholdButtons[index] != null) {
                this.thresholdButtons[index].setMessage(this.thresholdToggleLabel(index));
            }
        }
    }

    private Component enabledLabel() {
        return Component.translatable(this.menu.getSettings().enabled()
                ? "screen.smartbackpacks.capacity_warning.enabled"
                : "screen.smartbackpacks.capacity_warning.disabled");
    }

    private Component modeLabel() {
        return Component.translatable("screen.smartbackpacks.capacity_warning.mode." + this.menu.getSettings().calculationMode().getSerializedName());
    }

    private Component stepLabel() {
        return Component.translatable("screen.smartbackpacks.capacity_warning.step", this.menu.getSettings().thresholdStep());
    }

    private Component hudModeLabel() {
        return Component.translatable("screen.smartbackpacks.capacity_warning.hud_mode." + this.menu.getSettings().hudMode().getSerializedName());
    }

    private Component thresholdToggleLabel(int index) {
        return Component.translatable(this.menu.getSettings().thresholdEnabled(index)
                ? "screen.smartbackpacks.capacity_warning.threshold_on"
                : "screen.smartbackpacks.capacity_warning.threshold_off", index + 1);
    }

    private Component toggleLabel(String key, boolean enabled) {
        return Component.translatable("screen.smartbackpacks.capacity_warning." + (key.equals("hud") ? "notifications" : key),
                Component.translatable(enabled ? "screen.smartbackpacks.capacity_warning.on" : "screen.smartbackpacks.capacity_warning.off"));
    }

    private int stateColor(CapacityWarningState state) {
        return switch (state) {
            case FULL -> 0xFFE34A38;
            case CRITICAL -> 0xFFE8892F;
            case WARNING -> 0xFFE2C44D;
            case NORMAL -> 0xFF62B96A;
        };
    }

    private void drawPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, PANEL_BORDER);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_FILL);
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + 3, PANEL_LIGHT);
        guiGraphics.fill(x + 2, y + height - 3, x + width - 2, y + height - 2, PANEL_DARK);
    }
}
