package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.menu.RescueUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.network.RescueSettingsPayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.RescueUpgradeData;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class RescueUpgradeScreen extends LegacyContainerScreen<RescueUpgradeMenu> {
    private static final int TEXT_COLOR = 0xFF3D3127;
    private static final int MUTED_TEXT_COLOR = 0xFF6F6155;
    private static final int PANEL_BORDER = 0xFF3D2D21;
    private static final int PANEL_FILL = 0xFFD2BC99;
    private static final int PANEL_LIGHT = 0xFFE6D3B0;
    private static final int PANEL_DARK = 0xFF9A7B59;
    private static final int SLOT_FILL = 0xFFE2CFA9;
    private static final int READY_COLOR = 0xFF4DAA57;
    private static final int COOLDOWN_COLOR = 0xFFC26A3A;
    private static final int LEFT_X = 8;
    private static final int RIGHT_X = 124;
    private static final int BUTTON_W = 108;
    private static final int BUTTON_H = 18;
    private static final int MODE_ROW_1_Y = 98;
    private static final int MODE_ROW_2_Y = 120;
    private static final int THRESHOLD_LABEL_Y = 148;
    private static final int THRESHOLD_BUTTON_Y = 160;

    private Button enabledButton;
    private Button totemButton;
    private Button appleButton;
    private Button fallButton;
    private Button lavaButton;

    public RescueUpgradeScreen(RescueUpgradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 240, 280);
        this.titleLabelX = 40;
        this.titleLabelY = 11;
        this.inventoryLabelX = RescueUpgradeMenu.PLAYER_INVENTORY_X;
        this.inventoryLabelY = RescueUpgradeMenu.INVENTORY_LABEL_Y;
    }

    @Override
    protected void init() {
        super.init();
        this.enabledButton = this.addRenderableWidget(Button.builder(this.enabledLabel(), button -> this.toggleEnabled())
                .bounds(this.leftPos + 166, this.topPos + 8, 66, BUTTON_H)
                .build());
        this.totemButton = this.addRenderableWidget(Button.builder(this.modeLabel("totem", this.menu.getSettings().totemEnabled()), button -> this.toggle(RescueSettingsPayload.Action.TOGGLE_TOTEM))
                .bounds(this.leftPos + LEFT_X, this.topPos + MODE_ROW_1_Y, BUTTON_W, BUTTON_H)
                .build());
        this.appleButton = this.addRenderableWidget(Button.builder(this.modeLabel("apple", this.menu.getSettings().goldenAppleEnabled()), button -> this.toggle(RescueSettingsPayload.Action.TOGGLE_GOLDEN_APPLE))
                .bounds(this.leftPos + RIGHT_X, this.topPos + MODE_ROW_1_Y, BUTTON_W, BUTTON_H)
                .build());
        this.fallButton = this.addRenderableWidget(Button.builder(this.modeLabel("fall", this.menu.getSettings().fallEnabled()), button -> this.toggle(RescueSettingsPayload.Action.TOGGLE_FALL))
                .bounds(this.leftPos + LEFT_X, this.topPos + MODE_ROW_2_Y, BUTTON_W, BUTTON_H)
                .build());
        this.lavaButton = this.addRenderableWidget(Button.builder(this.modeLabel("lava", this.menu.getSettings().lavaEnabled()), button -> this.toggle(RescueSettingsPayload.Action.TOGGLE_LAVA))
                .bounds(this.leftPos + RIGHT_X, this.topPos + MODE_ROW_2_Y, BUTTON_W, BUTTON_H)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("-"), button -> this.changeThreshold(RescueSettingsPayload.Action.CHANGE_HEALTH_THRESHOLD, -1))
                .bounds(this.leftPos + 8, this.topPos + THRESHOLD_BUTTON_Y, 20, BUTTON_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), button -> this.changeThreshold(RescueSettingsPayload.Action.CHANGE_HEALTH_THRESHOLD, 1))
                .bounds(this.leftPos + 58, this.topPos + THRESHOLD_BUTTON_Y, 20, BUTTON_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("-"), button -> this.changeThreshold(RescueSettingsPayload.Action.CHANGE_LAVA_THRESHOLD, -1))
                .bounds(this.leftPos + 86, this.topPos + THRESHOLD_BUTTON_Y, 20, BUTTON_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), button -> this.changeThreshold(RescueSettingsPayload.Action.CHANGE_LAVA_THRESHOLD, 1))
                .bounds(this.leftPos + 136, this.topPos + THRESHOLD_BUTTON_Y, 20, BUTTON_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("-"), button -> this.changeThreshold(RescueSettingsPayload.Action.CHANGE_FALL_DISTANCE, -1))
                .bounds(this.leftPos + 164, this.topPos + THRESHOLD_BUTTON_Y, 20, BUTTON_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), button -> this.changeThreshold(RescueSettingsPayload.Action.CHANGE_FALL_DISTANCE, 1))
                .bounds(this.leftPos + 214, this.topPos + THRESHOLD_BUTTON_Y, 20, BUTTON_H).build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        this.drawPanel(guiGraphics, x, y, this.imageWidth, this.imageHeight);
        guiGraphics.renderItem(new ItemStack(ModItems.RESCUE_UPGRADE.get()), x + 10, y + 8);
        for (int slot = 0; slot < RescueUpgradeData.SLOT_COUNT; slot++) {
            this.drawSlotBox(guiGraphics, x + RescueUpgradeMenu.RESCUE_SLOT_X + slot * 34, y + RescueUpgradeMenu.RESCUE_SLOT_Y);
        }
        this.drawInventoryBackground(guiGraphics, x + RescueUpgradeMenu.PLAYER_INVENTORY_X, y + RescueUpgradeMenu.PLAYER_INVENTORY_Y);
        this.drawCooldownBar(guiGraphics, x + 8, y + 42);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        RescueUpgradeData data = this.menu.getSettings();
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, TEXT_COLOR, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.rescue.items"), 8, 31, TEXT_COLOR, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.rescue.health", data.healthThreshold()), 8, THRESHOLD_LABEL_Y, TEXT_COLOR, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.rescue.lava_health", data.lavaHealthThreshold()), 86, THRESHOLD_LABEL_Y, TEXT_COLOR, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.rescue.fall_distance", data.minimumFallDistance()), 164, THRESHOLD_LABEL_Y, TEXT_COLOR, false);
        if (!SmartBackpacksConfig.rescueAllowBackpackTotem()) {
            guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.rescue.totem_server_disabled"), 112, 82, MUTED_TEXT_COLOR, false);
        }
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, TEXT_COLOR, false);
    }

    private void drawCooldownBar(GuiGraphics guiGraphics, int x, int y) {
        int cooldown = this.menu.getSettings().globalCooldownTicks();
        int max = Math.max(1, SmartBackpacksConfig.rescueGlobalCooldownTicks());
        guiGraphics.fill(x, y, x + 100, y + 7, PANEL_DARK);
        int fill = cooldown <= 0 ? 100 : Math.max(1, 100 - Math.round(cooldown / (float) max * 100));
        guiGraphics.fill(x + 1, y + 1, x + 1 + Math.min(98, fill), y + 6, cooldown <= 0 ? READY_COLOR : COOLDOWN_COLOR);
        guiGraphics.drawString(this.font, cooldown <= 0
                ? Component.translatable("screen.smartbackpacks.rescue.ready")
                : Component.translatable("screen.smartbackpacks.rescue.cooldown", (cooldown + 19) / 20), x + 106, y - 1, TEXT_COLOR, false);
    }

    private void toggleEnabled() {
        boolean next = !this.menu.getSettings().enabled();
        this.menu.setEnabled(next);
        this.enabledButton.setMessage(this.enabledLabel());
        PacketDistributor.sendToServer(new RescueSettingsPayload(RescueSettingsPayload.Action.TOGGLE_ENABLED, next ? 1 : 0));
    }

    private void toggle(RescueSettingsPayload.Action action) {
        switch (action) {
            case TOGGLE_TOTEM -> this.menu.toggleTotem();
            case TOGGLE_GOLDEN_APPLE -> this.menu.toggleGoldenApple();
            case TOGGLE_FALL -> this.menu.toggleFall();
            case TOGGLE_LAVA -> this.menu.toggleLava();
            default -> {
            }
        }
        this.updateModeButtons();
        PacketDistributor.sendToServer(new RescueSettingsPayload(action, 0));
    }

    private void changeThreshold(RescueSettingsPayload.Action action, int delta) {
        switch (action) {
            case CHANGE_HEALTH_THRESHOLD -> this.menu.changeHealthThreshold(delta);
            case CHANGE_LAVA_THRESHOLD -> this.menu.changeLavaThreshold(delta);
            case CHANGE_FALL_DISTANCE -> this.menu.changeFallDistance(delta);
            default -> {
            }
        }
        PacketDistributor.sendToServer(new RescueSettingsPayload(action, delta));
    }

    private void updateModeButtons() {
        RescueUpgradeData data = this.menu.getSettings();
        this.totemButton.setMessage(this.modeLabel("totem", data.totemEnabled()));
        this.appleButton.setMessage(this.modeLabel("apple", data.goldenAppleEnabled()));
        this.fallButton.setMessage(this.modeLabel("fall", data.fallEnabled()));
        this.lavaButton.setMessage(this.modeLabel("lava", data.lavaEnabled()));
    }

    private Component enabledLabel() {
        return Component.translatable(this.menu.getSettings().enabled()
                ? "screen.smartbackpacks.rescue.enabled"
                : "screen.smartbackpacks.rescue.disabled");
    }

    private Component modeLabel(String key, boolean enabled) {
        return Component.translatable("screen.smartbackpacks.rescue.mode." + key, Component.translatable(enabled
                ? "screen.smartbackpacks.rescue.on"
                : "screen.smartbackpacks.rescue.off"));
    }

    private void drawPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, PANEL_BORDER);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_FILL);
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + 3, PANEL_LIGHT);
        guiGraphics.fill(x + 2, y + height - 3, x + width - 2, y + height - 2, PANEL_DARK);
    }

    private void drawSlotBox(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 18, y + 18, PANEL_DARK);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, SLOT_FILL);
        guiGraphics.fill(x + 2, y + 2, x + 16, y + 3, PANEL_LIGHT);
        guiGraphics.fill(x + 2, y + 2, x + 3, y + 16, PANEL_LIGHT);
    }

    private void drawInventoryBackground(GuiGraphics guiGraphics, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.drawSlotBox(guiGraphics, x + column * 18, y + row * 18);
            }
        }

        for (int column = 0; column < 9; column++) {
            this.drawSlotBox(guiGraphics, x + column * 18, y + 58);
        }
    }
}
