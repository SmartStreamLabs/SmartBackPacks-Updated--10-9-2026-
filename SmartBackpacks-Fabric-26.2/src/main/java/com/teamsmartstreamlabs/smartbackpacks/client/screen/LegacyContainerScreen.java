package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import com.teamsmartstreamlabs.smartbackpacks.client.CapacityWarningHud;
import com.teamsmartstreamlabs.smartbackpacks.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

abstract class LegacyContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    protected LegacyContainerScreen(T menu, Inventory playerInventory, Component title, int imageWidth, int imageHeight) {
        super(menu, playerInventory, title, imageWidth, imageHeight);
    }

    protected abstract void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY);

    protected void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        GuiGraphics guiGraphics = new GuiGraphics(extractor);
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
        super.extractContents(extractor, mouseX, mouseY, partialTick);
        this.renderForeground(guiGraphics, mouseX, mouseY, partialTick);
        CapacityWarningHud.render(guiGraphics);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
        this.renderLabels(new GuiGraphics(extractor), mouseX, mouseY);
    }
}
