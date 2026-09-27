package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import com.teamsmartstreamlabs.smartbackpacks.menu.ChunkLoaderUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.network.ChunkLoaderRadiusPayload;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.ChunkLoaderUpgradeData;

import com.teamsmartstreamlabs.smartbackpacks.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class ChunkLoaderUpgradeScreen extends LegacyContainerScreen<ChunkLoaderUpgradeMenu> {
    private static final int TEXT_COLOR = 0xFF3D3127;
    private static final int PANEL_BORDER = 0xFF3D2D21;
    private static final int PANEL_FILL = 0xFFD2BC99;
    private static final int PANEL_LIGHT = 0xFFE6D3B0;
    private static final int PANEL_DARK = 0xFF9A7B59;

    public ChunkLoaderUpgradeScreen(ChunkLoaderUpgradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 90);
        this.titleLabelX = 12;
        this.titleLabelY = 10;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.literal("-"), button -> this.changeRadius(-1))
                .bounds(this.leftPos + 44, this.topPos + 48, 32, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), button -> this.changeRadius(1))
                .bounds(this.leftPos + 100, this.topPos + 48, 32, 20)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, PANEL_BORDER);
        guiGraphics.fill(x + 1, y + 1, x + this.imageWidth - 1, y + this.imageHeight - 1, PANEL_FILL);
        guiGraphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + 3, PANEL_LIGHT);
        guiGraphics.fill(x + 2, y + this.imageHeight - 3, x + this.imageWidth - 2, y + this.imageHeight - 2, PANEL_DARK);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, TEXT_COLOR, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.chunk_loader.radius",
                this.menu.getRadius()), 44, 28, TEXT_COLOR, false);
        guiGraphics.drawCenteredString(this.font,
                Component.translatable("screen.smartbackpacks.chunk_loader.chunks", this.menu.getLoadedChunkCount()),
                this.imageWidth / 2, 74, TEXT_COLOR);
    }

    private void changeRadius(int delta) {
        int nextRadius = Math.max(ChunkLoaderUpgradeData.DEFAULT_RADIUS,
                Math.min(ChunkLoaderUpgradeData.MAX_RADIUS, this.menu.getRadius() + delta));
        if (nextRadius == this.menu.getRadius()) {
            return;
        }

        this.menu.setRadius(nextRadius);
        PacketDistributor.sendToServer(new ChunkLoaderRadiusPayload(delta));
    }
}
