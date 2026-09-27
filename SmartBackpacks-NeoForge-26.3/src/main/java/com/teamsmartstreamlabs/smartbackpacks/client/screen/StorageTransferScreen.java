package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import java.util.Locale;

import com.teamsmartstreamlabs.smartbackpacks.blockentity.StorageTransferBlockEntity;
import com.teamsmartstreamlabs.smartbackpacks.client.gui.GuiGraphics;
import com.teamsmartstreamlabs.smartbackpacks.menu.StorageTransferMenu;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class StorageTransferScreen extends LegacyContainerScreen<StorageTransferMenu> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 239;
    private static final int PANEL_DARK = 0xFF3A291E;
    private static final int PANEL_MID = 0xFF9A7955;
    private static final int PANEL_LIGHT = 0xFFD9C39B;
    private static final int SLOT_FILL = 0xFFC7AE85;
    private static final int TEXT_COLOR = 0xFF3B2C20;
    private static final int ONLINE_COLOR = 0xFF2B7D43;
    private static final int ERROR_COLOR = 0xFF9B3028;
    private Button modeButton;
    private Button redstoneButton;
    private Button componentsButton;
    private Button durabilityButton;

    public StorageTransferScreen(StorageTransferMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, HEIGHT);
        this.titleLabelX = 8;
        this.titleLabelY = 8;
        this.inventoryLabelX = StorageTransferMenu.PLAYER_INVENTORY_X;
        this.inventoryLabelY = StorageTransferMenu.PLAYER_INVENTORY_Y - 13;
    }

    @Override
    protected void init() {
        super.init();
        this.modeButton = this.addRenderableWidget(this.button(0, 8, 35, 79));
        this.redstoneButton = this.addRenderableWidget(this.button(1, 89, 35, 79));
        this.componentsButton = this.addRenderableWidget(this.button(2, 8, 56, 79));
        this.durabilityButton = this.addRenderableWidget(this.button(3, 89, 56, 79));
        this.updateButtons();
    }

    private Button button(int id, int x, int y, int width) {
        return Button.builder(Component.empty(), button -> {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
            }
        }).bounds(this.leftPos + x, this.topPos + y, width, 18).build();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.fill(x, y, x + WIDTH, y + HEIGHT, PANEL_DARK);
        guiGraphics.fill(x + 3, y + 3, x + WIDTH - 3, y + HEIGHT - 3, PANEL_MID);
        guiGraphics.fill(x + 6, y + 6, x + WIDTH - 6, y + HEIGHT - 6, PANEL_LIGHT);

        for (int slot = 0; slot < StorageTransferBlockEntity.FILTER_SLOTS; slot++) {
            drawSlot(guiGraphics, x + 43 + slot % 5 * 18, y + 79 + slot / 5 * 18);
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlot(guiGraphics, x + StorageTransferMenu.PLAYER_INVENTORY_X + column * 18,
                        y + StorageTransferMenu.PLAYER_INVENTORY_Y + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawSlot(guiGraphics, x + StorageTransferMenu.PLAYER_INVENTORY_X + column * 18,
                    y + StorageTransferMenu.HOTBAR_Y);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        this.updateButtons();
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, TEXT_COLOR, false);
        guiGraphics.drawString(this.font,
                Component.translatable(this.menu.isImporter()
                        ? "screen.smartbackpacks.storage_transfer.direction.import"
                        : this.menu.transferMode() == StorageTransferBlockEntity.TransferMode.PULL_ALL.ordinal()
                        ? "screen.smartbackpacks.storage_transfer.direction.pull_all"
                        : this.menu.transferMode() == StorageTransferBlockEntity.TransferMode.PULL_FROM_EXTERNAL.ordinal()
                        ? "screen.smartbackpacks.storage_transfer.direction.pull"
                        : "screen.smartbackpacks.storage_transfer.direction.push"),
                8, 22, TEXT_COLOR, false);
        Component status = statusLabel(this.menu.status());
        int statusColor = this.menu.status() <= StorageTransferBlockEntity.Status.TRANSFERRING.ordinal()
                ? ONLINE_COLOR : ERROR_COLOR;
        guiGraphics.drawString(this.font, status, 8, 119, statusColor, false);
        guiGraphics.drawString(this.font,
                Component.translatable(this.menu.isImporter()
                                ? "screen.smartbackpacks.storage_transfer.import_rate"
                                : "screen.smartbackpacks.storage_transfer.rate",
                        this.menu.transferAmount(), this.menu.transferInterval()),
                8, 130, TEXT_COLOR, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle,
                this.inventoryLabelX, this.inventoryLabelY, TEXT_COLOR, false);
    }

    private void updateButtons() {
        if (this.modeButton == null) {
            return;
        }
        this.modeButton.active = true;
        this.modeButton.setMessage(Component.translatable(this.menu.isImporter()
                ? this.menu.isAllowlist()
                ? "screen.smartbackpacks.storage_transfer.allowlist"
                : "screen.smartbackpacks.storage_transfer.blocklist"
                : this.menu.transferMode() == StorageTransferBlockEntity.TransferMode.PULL_ALL.ordinal()
                ? "screen.smartbackpacks.storage_transfer.mode.pull_all"
                : this.menu.transferMode() == StorageTransferBlockEntity.TransferMode.PULL_FROM_EXTERNAL.ordinal()
                ? "screen.smartbackpacks.storage_transfer.mode.pull"
                : "screen.smartbackpacks.storage_transfer.mode.push"));
        this.redstoneButton.setMessage(Component.translatable(
                "screen.smartbackpacks.storage_transfer.redstone." + redstoneName(this.menu.redstoneMode())));
        this.componentsButton.setMessage(Component.translatable(
                this.menu.matchComponents()
                        ? "screen.smartbackpacks.storage_transfer.components_on"
                        : "screen.smartbackpacks.storage_transfer.components_off"));
        this.durabilityButton.setMessage(Component.translatable(
                this.menu.matchDurability()
                        ? "screen.smartbackpacks.storage_transfer.durability_on"
                        : "screen.smartbackpacks.storage_transfer.durability_off"));
    }

    private static Component statusLabel(int ordinal) {
        StorageTransferBlockEntity.Status[] statuses = StorageTransferBlockEntity.Status.values();
        int safeOrdinal = Math.max(0, Math.min(statuses.length - 1, ordinal));
        return Component.translatable("screen.smartbackpacks.storage_transfer.status."
                + statuses[safeOrdinal].name().toLowerCase(Locale.ROOT));
    }

    private static String redstoneName(int ordinal) {
        return switch (Math.floorMod(ordinal, 3)) {
            case 1 -> "signal";
            case 2 -> "no_signal";
            default -> "ignore";
        };
    }

    private static void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 18, y + 18, PANEL_DARK);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, PANEL_MID);
        guiGraphics.fill(x + 2, y + 2, x + 17, y + 17, SLOT_FILL);
    }
}
