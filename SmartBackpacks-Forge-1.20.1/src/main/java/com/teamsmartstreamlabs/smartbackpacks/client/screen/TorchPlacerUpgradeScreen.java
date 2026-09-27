package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import com.teamsmartstreamlabs.smartbackpacks.menu.TorchPlacerUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.network.TorchPlacerSettingsPayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.TorchPlacerUpgradeData;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class TorchPlacerUpgradeScreen extends LegacyContainerScreen<TorchPlacerUpgradeMenu> {
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
    private Button floorButton;
    private Button wallButton;
    private Button ceilingButton;
    private Button moddedButton;
    private Button sprintButton;
    private Button sneakButton;
    private Button stillButton;
    private Button waterButton;
    private Button lavaButton;

    public TorchPlacerUpgradeScreen(TorchPlacerUpgradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 240, 260);
        this.titleLabelX = 40;
        this.titleLabelY = 11;
        this.inventoryLabelX = TorchPlacerUpgradeMenu.PLAYER_INVENTORY_X;
        this.inventoryLabelY = TorchPlacerUpgradeMenu.INVENTORY_LABEL_Y;
    }

    @Override
    protected void init() {
        super.init();
        this.enabledButton = this.addRenderableWidget(Button.builder(this.enabledLabel(), button -> this.toggleEnabled())
                .bounds(this.leftPos + 166, this.topPos + 8, 66, BUTTON_H)
                .build());
        this.modeButton = this.addRenderableWidget(Button.builder(this.modeLabel(), button -> this.cycle(TorchPlacerSettingsPayload.Action.CYCLE_PLACEMENT_MODE))
                .bounds(this.leftPos + 8, this.topPos + 32, BUTTON_W, BUTTON_H)
                .build());
        this.matchButton = this.addRenderableWidget(Button.builder(this.matchLabel(), button -> this.cycle(TorchPlacerSettingsPayload.Action.CYCLE_MATCH_MODE))
                .bounds(this.leftPos + 84, this.topPos + 32, BUTTON_W, BUTTON_H)
                .build());
        this.filterButton = this.addRenderableWidget(Button.builder(this.filterLabel(), button -> this.cycle(TorchPlacerSettingsPayload.Action.CYCLE_FILTER_MODE))
                .bounds(this.leftPos + 160, this.topPos + 32, BUTTON_W, BUTTON_H)
                .build());
        this.floorButton = this.addRenderableWidget(Button.builder(this.onOffLabel("floor", this.menu.getSettings().floorPlacement()), button -> this.toggle(TorchPlacerSettingsPayload.Action.TOGGLE_FLOOR))
                .bounds(this.leftPos + 8, this.topPos + 82, BUTTON_W, BUTTON_H)
                .build());
        this.wallButton = this.addRenderableWidget(Button.builder(this.onOffLabel("wall", this.menu.getSettings().wallPlacement()), button -> this.toggle(TorchPlacerSettingsPayload.Action.TOGGLE_WALL))
                .bounds(this.leftPos + 84, this.topPos + 82, BUTTON_W, BUTTON_H)
                .build());
        this.ceilingButton = this.addRenderableWidget(Button.builder(this.onOffLabel("ceiling", this.menu.getSettings().ceilingPlacement()), button -> this.toggle(TorchPlacerSettingsPayload.Action.TOGGLE_CEILING))
                .bounds(this.leftPos + 160, this.topPos + 82, BUTTON_W, BUTTON_H)
                .build());
        this.moddedButton = this.addRenderableWidget(Button.builder(this.onOffLabel("modded", this.menu.getSettings().moddedLightSources()), button -> this.toggle(TorchPlacerSettingsPayload.Action.TOGGLE_MODDED))
                .bounds(this.leftPos + 8, this.topPos + 104, BUTTON_W, BUTTON_H)
                .build());
        this.sprintButton = this.addRenderableWidget(Button.builder(this.onOffLabel("sprint", this.menu.getSettings().placeWhileSprinting()), button -> this.toggle(TorchPlacerSettingsPayload.Action.TOGGLE_SPRINTING))
                .bounds(this.leftPos + 84, this.topPos + 104, BUTTON_W, BUTTON_H)
                .build());
        this.sneakButton = this.addRenderableWidget(Button.builder(this.onOffLabel("sneak", this.menu.getSettings().placeWhileSneaking()), button -> this.toggle(TorchPlacerSettingsPayload.Action.TOGGLE_SNEAKING))
                .bounds(this.leftPos + 160, this.topPos + 104, BUTTON_W, BUTTON_H)
                .build());
        this.stillButton = this.addRenderableWidget(Button.builder(this.onOffLabel("still", this.menu.getSettings().placeWhileStandingStill()), button -> this.toggle(TorchPlacerSettingsPayload.Action.TOGGLE_STANDING))
                .bounds(this.leftPos + 8, this.topPos + 126, BUTTON_W, BUTTON_H)
                .build());
        this.waterButton = this.addRenderableWidget(Button.builder(this.onOffLabel("water", this.menu.getSettings().placeInWater()), button -> this.toggle(TorchPlacerSettingsPayload.Action.TOGGLE_WATER))
                .bounds(this.leftPos + 84, this.topPos + 126, BUTTON_W, BUTTON_H)
                .build());
        this.lavaButton = this.addRenderableWidget(Button.builder(this.onOffLabel("lava", this.menu.getSettings().placeInLava()), button -> this.toggle(TorchPlacerSettingsPayload.Action.TOGGLE_LAVA))
                .bounds(this.leftPos + 160, this.topPos + 126, BUTTON_W, BUTTON_H)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("-"), button -> this.change(TorchPlacerSettingsPayload.Action.CHANGE_LIGHT_THRESHOLD, -1))
                .bounds(this.leftPos + 8, this.topPos + 148, 20, BUTTON_H)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), button -> this.change(TorchPlacerSettingsPayload.Action.CHANGE_LIGHT_THRESHOLD, 1))
                .bounds(this.leftPos + 58, this.topPos + 148, 20, BUTTON_H)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("-"), button -> this.change(TorchPlacerSettingsPayload.Action.CHANGE_MIN_DISTANCE, -1))
                .bounds(this.leftPos + 88, this.topPos + 148, 20, BUTTON_H)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), button -> this.change(TorchPlacerSettingsPayload.Action.CHANGE_MIN_DISTANCE, 1))
                .bounds(this.leftPos + 138, this.topPos + 148, 20, BUTTON_H)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        this.drawPanel(guiGraphics, x, y, this.imageWidth, this.imageHeight);
        guiGraphics.renderItem(new ItemStack(ModItems.TORCH_PLACER_UPGRADE.get()), x + 10, y + 8);
        for (int slot = 0; slot < TorchPlacerUpgradeData.FILTER_SLOT_COUNT; slot++) {
            this.drawSlotBox(guiGraphics, x + TorchPlacerUpgradeMenu.FILTER_SLOT_X + (slot % TorchPlacerUpgradeMenu.FILTER_COLUMNS) * 18,
                    y + TorchPlacerUpgradeMenu.FILTER_SLOT_Y + (slot / TorchPlacerUpgradeMenu.FILTER_COLUMNS) * 18);
        }
        this.drawInventoryBackground(guiGraphics, x + TorchPlacerUpgradeMenu.PLAYER_INVENTORY_X, y + TorchPlacerUpgradeMenu.PLAYER_INVENTORY_Y);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, TEXT_COLOR, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.torch_placer.filters"), 8, 50, TEXT_COLOR, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.torch_placer.light", this.menu.getSettings().lightThreshold()), 32, 153, TEXT_COLOR, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.torch_placer.distance", this.menu.getSettings().minimumDistance()), 112, 153, TEXT_COLOR, false);
        guiGraphics.drawString(this.font, this.hint(), 8, 169, HINT_COLOR, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, TEXT_COLOR, false);
    }

    private void toggleEnabled() {
        boolean next = !this.menu.getSettings().enabled();
        this.menu.setEnabled(next);
        this.enabledButton.setMessage(this.enabledLabel());
        PacketDistributor.sendToServer(new TorchPlacerSettingsPayload(TorchPlacerSettingsPayload.Action.TOGGLE_ENABLED, next ? 1 : 0));
    }

    private void cycle(TorchPlacerSettingsPayload.Action action) {
        switch (action) {
            case CYCLE_PLACEMENT_MODE -> this.menu.cyclePlacementMode();
            case CYCLE_MATCH_MODE -> this.menu.cycleMatchMode();
            case CYCLE_FILTER_MODE -> this.menu.cycleFilterMode();
            default -> {
            }
        }
        this.updateButtons();
        PacketDistributor.sendToServer(new TorchPlacerSettingsPayload(action, 0));
    }

    private void toggle(TorchPlacerSettingsPayload.Action action) {
        switch (action) {
            case TOGGLE_FLOOR -> this.menu.toggleFloorPlacement();
            case TOGGLE_WALL -> this.menu.toggleWallPlacement();
            case TOGGLE_CEILING -> this.menu.toggleCeilingPlacement();
            case TOGGLE_MODDED -> this.menu.toggleModdedLightSources();
            case TOGGLE_SPRINTING -> this.menu.toggleSprinting();
            case TOGGLE_SNEAKING -> this.menu.toggleSneaking();
            case TOGGLE_STANDING -> this.menu.toggleStandingStill();
            case TOGGLE_WATER -> this.menu.toggleWater();
            case TOGGLE_LAVA -> this.menu.toggleLava();
            default -> {
            }
        }
        this.updateButtons();
        PacketDistributor.sendToServer(new TorchPlacerSettingsPayload(action, 0));
    }

    private void change(TorchPlacerSettingsPayload.Action action, int delta) {
        if (action == TorchPlacerSettingsPayload.Action.CHANGE_LIGHT_THRESHOLD) {
            this.menu.changeLightThreshold(delta);
        } else if (action == TorchPlacerSettingsPayload.Action.CHANGE_MIN_DISTANCE) {
            this.menu.changeMinimumDistance(delta);
        }
        PacketDistributor.sendToServer(new TorchPlacerSettingsPayload(action, delta));
    }

    private void updateButtons() {
        TorchPlacerUpgradeData data = this.menu.getSettings();
        this.enabledButton.setMessage(this.enabledLabel());
        this.modeButton.setMessage(this.modeLabel());
        this.matchButton.setMessage(this.matchLabel());
        this.filterButton.setMessage(this.filterLabel());
        this.floorButton.setMessage(this.onOffLabel("floor", data.floorPlacement()));
        this.wallButton.setMessage(this.onOffLabel("wall", data.wallPlacement()));
        this.ceilingButton.setMessage(this.onOffLabel("ceiling", data.ceilingPlacement()));
        this.moddedButton.setMessage(this.onOffLabel("modded", data.moddedLightSources()));
        this.sprintButton.setMessage(this.onOffLabel("sprint", data.placeWhileSprinting()));
        this.sneakButton.setMessage(this.onOffLabel("sneak", data.placeWhileSneaking()));
        this.stillButton.setMessage(this.onOffLabel("still", data.placeWhileStandingStill()));
        this.waterButton.setMessage(this.onOffLabel("water", data.placeInWater()));
        this.lavaButton.setMessage(this.onOffLabel("lava", data.placeInLava()));
    }

    private Component enabledLabel() {
        return Component.translatable(this.menu.getSettings().enabled()
                ? "screen.smartbackpacks.torch_placer.enabled"
                : "screen.smartbackpacks.torch_placer.disabled");
    }

    private Component modeLabel() {
        return Component.translatable("screen.smartbackpacks.torch_placer.mode." + this.menu.getSettings().placementMode().getSerializedName());
    }

    private Component matchLabel() {
        return Component.translatable("screen.smartbackpacks.torch_placer.match." + this.menu.getSettings().matchMode().getSerializedName());
    }

    private Component filterLabel() {
        return Component.translatable("screen.smartbackpacks.torch_placer.filter." + this.menu.getSettings().filterMode().getSerializedName());
    }

    private Component onOffLabel(String key, boolean enabled) {
        return Component.translatable("screen.smartbackpacks.torch_placer." + key, Component.translatable(enabled
                ? "screen.smartbackpacks.torch_placer.on"
                : "screen.smartbackpacks.torch_placer.off"));
    }

    private Component hint() {
        return Component.translatable("screen.smartbackpacks.torch_placer.hint");
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
