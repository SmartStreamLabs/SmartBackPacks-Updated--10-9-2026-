package com.teamsmartstreamlabs.smartbackpacks.client.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

/**
 * Compatibility wrapper for the 26.1 GuiGraphicsExtractor pipeline.
 */
public final class GuiGraphics {
    private final GuiGraphicsExtractor delegate;

    public GuiGraphics(GuiGraphicsExtractor delegate) {
        this.delegate = delegate;
    }

    public GuiGraphicsExtractor unwrap() {
        return this.delegate;
    }

    public Matrix3x2fStack pose() {
        return this.delegate.pose();
    }

    public void fill(int x1, int y1, int x2, int y2, int color) {
        this.delegate.fill(x1, y1, x2, y2, normalizeColor(color));
    }

    public int drawString(Font font, Component text, int x, int y, int color, boolean shadow) {
        this.delegate.text(font, text, x, y, normalizeColor(color), shadow);
        return x + font.width(text);
    }

    public int drawString(Font font, String text, int x, int y, int color, boolean shadow) {
        this.delegate.text(font, text, x, y, normalizeColor(color), shadow);
        return x + font.width(text);
    }

    public int drawString(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        this.delegate.text(font, text, x, y, normalizeColor(color), shadow);
        return x + font.width(text);
    }

    public void drawCenteredString(Font font, Component text, int x, int y, int color) {
        this.delegate.centeredText(font, text, x, y, normalizeColor(color));
    }

    public void drawCenteredString(Font font, String text, int x, int y, int color) {
        this.delegate.centeredText(font, text, x, y, normalizeColor(color));
    }

    public void drawCenteredString(Font font, FormattedCharSequence text, int x, int y, int color) {
        this.delegate.centeredText(font, text, x, y, normalizeColor(color));
    }

    public void blit(Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        this.delegate.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    public void blit(Identifier texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        this.blit(texture, x, y, (float) u, (float) v, width, height, textureWidth, textureHeight);
    }

    public void blit(
            RenderPipeline pipeline,
            Identifier texture,
            int x,
            int y,
            float u,
            float v,
            int width,
            int height,
            int srcWidth,
            int srcHeight,
            int textureWidth,
            int textureHeight,
            int color
    ) {
        this.delegate.blit(pipeline, texture, x, y, u, v, width, height, srcWidth, srcHeight, textureWidth, textureHeight, normalizeColor(color));
    }

    public void blit(
            Supplier<RenderPipeline> pipeline,
            Identifier texture,
            int x,
            int y,
            float u,
            float v,
            int width,
            int height,
            int textureWidth,
            int textureHeight
    ) {
        this.delegate.blit(pipeline.get(), texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    public void renderItem(ItemStack stack, int x, int y) {
        this.delegate.item(stack, x, y);
    }

    public void renderFakeItem(ItemStack stack, int x, int y) {
        this.delegate.fakeItem(stack, x, y);
    }

    public void renderTooltip(Font font, Component text, int mouseX, int mouseY) {
        this.delegate.setTooltipForNextFrame(font, text, mouseX, mouseY);
    }

    public void renderTooltip(Font font, List<Component> text, Optional<TooltipComponent> component, int mouseX, int mouseY) {
        this.delegate.setTooltipForNextFrame(font, text, component, mouseX, mouseY);
    }

    public void renderTooltip(Font font, List<? extends FormattedCharSequence> text, int mouseX, int mouseY) {
        this.delegate.setTooltipForNextFrame(font, text, mouseX, mouseY);
    }

    public void setTooltipForNextFrame(Font font, Component text, int mouseX, int mouseY) {
        this.delegate.setTooltipForNextFrame(font, text, mouseX, mouseY);
    }

    public void setTooltipForNextFrame(Font font, List<? extends FormattedCharSequence> lines, int mouseX, int mouseY) {
        this.delegate.setTooltipForNextFrame(font, lines, mouseX, mouseY);
    }

    public void setTooltipForNextFrame(Font font, List<Component> lines, Optional<TooltipComponent> component, int mouseX, int mouseY) {
        this.delegate.setTooltipForNextFrame(font, lines, component, mouseX, mouseY);
    }

    public void setComponentTooltipForNextFrame(Font font, List<Component> lines, int mouseX, int mouseY) {
        this.delegate.setComponentTooltipForNextFrame(font, lines, mouseX, mouseY);
    }

    private static int normalizeColor(int color) {
        return (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
    }
}
