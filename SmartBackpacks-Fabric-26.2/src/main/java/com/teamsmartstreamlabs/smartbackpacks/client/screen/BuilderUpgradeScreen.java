package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import com.teamsmartstreamlabs.smartbackpacks.menu.BuilderUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.network.BuilderSettingsPayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BuilderUpgradeData;

import com.teamsmartstreamlabs.smartbackpacks.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class BuilderUpgradeScreen extends LegacyContainerScreen<BuilderUpgradeMenu> {
    private static final int TEXT_COLOR = 0xFF3D3127;
    private static final int PANEL_BORDER = 0xFF3D2D21;
    private static final int PANEL_FILL = 0xFFD2BC99;
    private static final int PANEL_LIGHT = 0xFFE6D3B0;
    private static final int PANEL_DARK = 0xFF9A7B59;
    private static final int SLOT_FILL = 0xFFE2CFA9;
    private static final int HINT_COLOR = 0xFF6E5740;
    private static final int BUTTON_H = 18;
    private static final int BUTTON_W = 72;

    private Button enabledButton;
    private Button modeButton;
    private Button matchButton;
    private Button filterButton;
    private Button mainHandButton;
    private Button offhandButton;
    private Button scaffoldingButton;
    private Button moddedButton;
    private Button dangerButton;
    private Button feedbackButton;

    public BuilderUpgradeScreen(BuilderUpgradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 240, 260);
        this.titleLabelX = 40;
        this.titleLabelY = 11;
        this.inventoryLabelX = BuilderUpgradeMenu.PLAYER_INVENTORY_X;
        this.inventoryLabelY = BuilderUpgradeMenu.INVENTORY_LABEL_Y;
    }

    @Override
    protected void init() {
        super.init();
        this.enabledButton = this.addRenderableWidget(Button.builder(this.enabledLabel(), button -> this.toggleEnabled())
                .bounds(this.leftPos + 166, this.topPos + 8, 66, BUTTON_H)
                .build());
        this.modeButton = this.addRenderableWidget(Button.builder(this.modeLabel(), button -> this.cycle(BuilderSettingsPayload.Action.CYCLE_REFILL_MODE))
                .bounds(this.leftPos + 8, this.topPos + 32, BUTTON_W, BUTTON_H)
                .build());
        this.matchButton = this.addRenderableWidget(Button.builder(this.matchLabel(), button -> this.cycle(BuilderSettingsPayload.Action.CYCLE_MATCH_MODE))
                .bounds(this.leftPos + 84, this.topPos + 32, BUTTON_W, BUTTON_H)
                .build());
        this.filterButton = this.addRenderableWidget(Button.builder(this.filterLabel(), button -> this.cycle(BuilderSettingsPayload.Action.CYCLE_FILTER_MODE))
                .bounds(this.leftPos + 160, this.topPos + 32, BUTTON_W, BUTTON_H)
                .build());
        this.mainHandButton = this.addRenderableWidget(Button.builder(this.onOffLabel("main", this.menu.getSettings().mainHandEnabled()), button -> this.toggle(BuilderSettingsPayload.Action.TOGGLE_MAIN_HAND))
                .bounds(this.leftPos + 8, this.topPos + 108, BUTTON_W, BUTTON_H)
                .build());
        this.offhandButton = this.addRenderableWidget(Button.builder(this.onOffLabel("offhand", this.menu.getSettings().offhandEnabled()), button -> this.toggle(BuilderSettingsPayload.Action.TOGGLE_OFFHAND))
                .bounds(this.leftPos + 84, this.topPos + 108, BUTTON_W, BUTTON_H)
                .build());
        this.scaffoldingButton = this.addRenderableWidget(Button.builder(this.onOffLabel("scaffolding", this.menu.getSettings().scaffoldingEnabled()), button -> this.toggle(BuilderSettingsPayload.Action.TOGGLE_SCAFFOLDING))
                .bounds(this.leftPos + 160, this.topPos + 108, BUTTON_W, BUTTON_H)
                .build());
        this.moddedButton = this.addRenderableWidget(Button.builder(this.onOffLabel("modded", this.menu.getSettings().moddedBlocksEnabled()), button -> this.toggle(BuilderSettingsPayload.Action.TOGGLE_MODDED_BLOCKS))
                .bounds(this.leftPos + 8, this.topPos + 130, BUTTON_W, BUTTON_H)
                .build());
        this.dangerButton = this.addRenderableWidget(Button.builder(this.onOffLabel("safe", this.menu.getSettings().dangerousProtectionEnabled()), button -> this.toggle(BuilderSettingsPayload.Action.TOGGLE_DANGEROUS_PROTECTION))
                .bounds(this.leftPos + 84, this.topPos + 130, BUTTON_W, BUTTON_H)
                .build());
        this.feedbackButton = this.addRenderableWidget(Button.builder(this.onOffLabel("feedback", this.menu.getSettings().feedbackEnabled()), button -> this.toggle(BuilderSettingsPayload.Action.TOGGLE_FEEDBACK))
                .bounds(this.leftPos + 160, this.topPos + 130, BUTTON_W, BUTTON_H)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("-"), button -> this.changeThreshold(-1))
                .bounds(this.leftPos + 8, this.topPos + 152, 20, BUTTON_H)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), button -> this.changeThreshold(1))
                .bounds(this.leftPos + 72, this.topPos + 152, 20, BUTTON_H)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        this.drawPanel(guiGraphics, x, y, this.imageWidth, this.imageHeight);
        guiGraphics.renderItem(new ItemStack(ModItems.BUILDER_UPGRADE.get()), x + 10, y + 8);
        for (int slot = 0; slot < BuilderUpgradeData.FILTER_SLOT_COUNT; slot++) {
            this.drawSlotBox(guiGraphics, x + BuilderUpgradeMenu.FILTER_SLOT_X + (slot % BuilderUpgradeMenu.FILTER_COLUMNS) * 18,
                    y + BuilderUpgradeMenu.FILTER_SLOT_Y + (slot / BuilderUpgradeMenu.FILTER_COLUMNS) * 18);
        }
        this.drawInventoryBackground(guiGraphics, x + BuilderUpgradeMenu.PLAYER_INVENTORY_X, y + BuilderUpgradeMenu.PLAYER_INVENTORY_Y);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, TEXT_COLOR, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.builder.filters"), 8, 54, TEXT_COLOR, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.builder.threshold", this.menu.getSettings().threshold()), 32, 157, TEXT_COLOR, false);
        guiGraphics.drawString(this.font, this.statusHint(), 100, 157, HINT_COLOR, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, TEXT_COLOR, false);
    }

    private void toggleEnabled() {
        boolean next = !this.menu.getSettings().enabled();
        this.menu.setEnabled(next);
        this.enabledButton.setMessage(this.enabledLabel());
        PacketDistributor.sendToServer(new BuilderSettingsPayload(BuilderSettingsPayload.Action.TOGGLE_ENABLED, next ? 1 : 0));
    }

    private void cycle(BuilderSettingsPayload.Action action) {
        switch (action) {
            case CYCLE_REFILL_MODE -> this.menu.cycleRefillMode();
            case CYCLE_MATCH_MODE -> this.menu.cycleMatchMode();
            case CYCLE_FILTER_MODE -> this.menu.cycleFilterMode();
            default -> {
            }
        }
        this.updateButtons();
        PacketDistributor.sendToServer(new BuilderSettingsPayload(action, 0));
    }

    private void toggle(BuilderSettingsPayload.Action action) {
        switch (action) {
            case TOGGLE_MAIN_HAND -> this.menu.toggleMainHand();
            case TOGGLE_OFFHAND -> this.menu.toggleOffhand();
            case TOGGLE_SCAFFOLDING -> this.menu.toggleScaffolding();
            case TOGGLE_MODDED_BLOCKS -> this.menu.toggleModdedBlocks();
            case TOGGLE_DANGEROUS_PROTECTION -> this.menu.toggleDangerousProtection();
            case TOGGLE_FEEDBACK -> this.menu.toggleFeedback();
            default -> {
            }
        }
        this.updateButtons();
        PacketDistributor.sendToServer(new BuilderSettingsPayload(action, 0));
    }

    private void changeThreshold(int delta) {
        this.menu.changeThreshold(delta);
        PacketDistributor.sendToServer(new BuilderSettingsPayload(BuilderSettingsPayload.Action.CHANGE_THRESHOLD, delta));
    }

    private void updateButtons() {
        BuilderUpgradeData data = this.menu.getSettings();
        this.enabledButton.setMessage(this.enabledLabel());
        this.modeButton.setMessage(this.modeLabel());
        this.matchButton.setMessage(this.matchLabel());
        this.filterButton.setMessage(this.filterLabel());
        this.mainHandButton.setMessage(this.onOffLabel("main", data.mainHandEnabled()));
        this.offhandButton.setMessage(this.onOffLabel("offhand", data.offhandEnabled()));
        this.scaffoldingButton.setMessage(this.onOffLabel("scaffolding", data.scaffoldingEnabled()));
        this.moddedButton.setMessage(this.onOffLabel("modded", data.moddedBlocksEnabled()));
        this.dangerButton.setMessage(this.onOffLabel("safe", data.dangerousProtectionEnabled()));
        this.feedbackButton.setMessage(this.onOffLabel("feedback", data.feedbackEnabled()));
    }

    private Component enabledLabel() {
        return Component.translatable(this.menu.getSettings().enabled()
                ? "screen.smartbackpacks.builder.enabled"
                : "screen.smartbackpacks.builder.disabled");
    }

    private Component modeLabel() {
        return Component.translatable("screen.smartbackpacks.builder.mode." + this.menu.getSettings().refillMode().getSerializedName());
    }

    private Component matchLabel() {
        return Component.translatable("screen.smartbackpacks.builder.match." + this.menu.getSettings().matchMode().getSerializedName());
    }

    private Component filterLabel() {
        return Component.translatable("screen.smartbackpacks.builder.filter." + this.menu.getSettings().filterMode().getSerializedName());
    }

    private Component onOffLabel(String key, boolean enabled) {
        return Component.translatable("screen.smartbackpacks.builder." + key, Component.translatable(enabled
                ? "screen.smartbackpacks.builder.on"
                : "screen.smartbackpacks.builder.off"));
    }

    private Component statusHint() {
        return Component.translatable("screen.smartbackpacks.builder.hint." + this.menu.getSettings().refillMode().getSerializedName());
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
