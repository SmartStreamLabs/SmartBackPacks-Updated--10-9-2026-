package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.teamsmartstreamlabs.smartbackpacks.client.StorageGuidePrompt;
import com.teamsmartstreamlabs.smartbackpacks.client.gui.GuiGraphics;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModBlocks;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public final class StorageGuideScreen extends Screen {
    private static final long STAGE_DURATION_MS = 2400L;
    private static final int[][] BACKPACK_OFFSETS = {
            {-112, 54}, {112, 54}, {-64, 118}, {64, 118}
    };
    private static final int[][] CABLE_OFFSETS = {
            {0, 102}, {0, 86}, {0, 70}, {0, 54},
            {-16, 54}, {-32, 54}, {-48, 54}, {-64, 54}, {-80, 54}, {-96, 54},
            {16, 54}, {32, 54}, {48, 54}, {64, 54}, {80, 54}, {96, 54},
            {-64, 70}, {-64, 86}, {-64, 102},
            {64, 70}, {64, 86}, {64, 102}
    };
    private final Screen parent;
    private long openedAt;

    private StorageGuideScreen(Screen parent) {
        super(Component.translatable("screen.smartbackpacks.storage_guide.title"));
        this.parent = parent;
    }

    public static void openIfRequested(Minecraft minecraft) {
        Screen current = minecraft.gui.screen();
        if (current != null && !(current instanceof StorageGuideScreen)
                && StorageGuidePrompt.wasHoveredRecently() && isGuideKeyDown(minecraft)) {
            minecraft.gui.setScreen(new StorageGuideScreen(current));
        }
    }

    @Override
    protected void init() {
        this.openedAt = System.currentTimeMillis();
    }

    @Override
    public void tick() {
        if (this.minecraft != null && !isGuideKeyDown(this.minecraft)) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        GuiGraphics graphics = new GuiGraphics(extractor);
        long elapsed = Math.max(0L, System.currentTimeMillis() - this.openedAt);
        int stage = (int) ((elapsed / STAGE_DURATION_MS) % 4L);
        float progress = (elapsed % STAGE_DURATION_MS) / (float) STAGE_DURATION_MS;
        int panelWidth = Math.min(470, this.width - 20);
        int panelHeight = Math.min(300, this.height - 20);
        int left = (this.width - panelWidth) / 2;
        int top = (this.height - panelHeight) / 2;
        int centerX = this.width / 2;
        int sceneTop = top + Math.max(0, (panelHeight - 196) / 2)
                - (stage == 3 ? Math.min(4, (int) (progress * 8.0F)) : 0);

        graphics.fill(0, 0, this.width, this.height, 0xB0000000);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xFFF1D7A5);
        graphics.fill(left + 3, top + 3, left + panelWidth - 3, top + panelHeight - 3, 0xFFB6966C);
        graphics.fill(left + 7, top + 7, left + panelWidth - 7, top + panelHeight - 7, 0xFFE5C997);
        graphics.drawCenteredString(this.font, this.title, centerX, top + 16, 0xFF3B2418);
        graphics.drawCenteredString(this.font, Component.translatable("screen.smartbackpacks.storage_guide.step", stage + 1, 4),
                centerX, top + 32, 0xFF775236);

        drawRoom(graphics, centerX, sceneTop);
        drawPlacedItem(graphics, centerX, sceneTop + 118, new ItemStack(ModBlocks.STORAGE_CONTROLLER.get()),
                stage == 0 || stage == 3);
        int visibleBackpacks = stage == 0 ? 0 : stage == 1
                ? Math.min(BACKPACK_OFFSETS.length, (int) (progress * (BACKPACK_OFFSETS.length + 1)))
                : BACKPACK_OFFSETS.length;
        ItemStack[] backpacks = {
                new ItemStack(ModItems.LEATHER_BACKPACK.get()), new ItemStack(ModItems.COAL_BACKPACK.get()),
                new ItemStack(ModItems.LAPIS_BACKPACK.get()), new ItemStack(ModItems.REDSTONE_BACKPACK.get())
        };
        int visibleCables = stage < 2 ? 0 : stage == 2
                ? Math.min(CABLE_OFFSETS.length, (int) (progress * (CABLE_OFFSETS.length + 1)))
                : CABLE_OFFSETS.length;
        for (int index = 0; index < visibleCables; index++) {
            int[] offset = CABLE_OFFSETS[index];
            drawPlacedItem(graphics, centerX + offset[0], sceneTop + offset[1],
                    new ItemStack(ModBlocks.STORAGE_CABLE.get()), index == visibleCables - 1 && stage == 2);
        }
        for (int index = 0; index < visibleBackpacks; index++) {
            int[] offset = BACKPACK_OFFSETS[index];
            boolean connected = stage == 2 && backpackJustConnected(index, visibleCables);
            drawPlacedItem(graphics, centerX + offset[0], sceneTop + offset[1], backpacks[index],
                    stage == 1 && index == visibleBackpacks - 1 || connected);
        }
        drawStepText(graphics, centerX, top + panelHeight - 50, stage, progress);
        graphics.drawCenteredString(this.font, Component.translatable("screen.smartbackpacks.storage_guide.release"),
                centerX, top + panelHeight - 22, 0xFF775236);
        for (int index = 0; index < 4; index++) {
            int dotX = centerX - 18 + index * 12;
            graphics.fill(dotX, top + panelHeight - 8, dotX + 7, top + panelHeight - 5,
                    index == stage ? 0xFF32AFA2 : 0xFF8B6D4B);
        }
        super.extractRenderState(extractor, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void drawStepText(GuiGraphics graphics, int centerX, int y, int stage, float progress) {
        int variant = switch (stage) {
            case 1, 3 -> progress < 0.5F ? 0 : 1;
            case 2 -> Math.min(2, (int) (progress * 3.0F));
            default -> 0;
        };
        int lines = stage == 2 && variant == 2 ? 1 : 2;
        for (int line = 0; line < lines; line++) {
            graphics.drawCenteredString(this.font,
                    Component.translatable("screen.smartbackpacks.storage_guide.message."
                            + stage + "." + variant + "." + line),
                    centerX, y + line * 12, line == 0 ? 0xFF3B2418 : 0xFF775236);
        }
    }

    private static void drawRoom(GuiGraphics graphics, int centerX, int top) {
        graphics.fill(centerX - 124, top + 42, centerX + 124, top + 130, 0xFF6B5542);
        graphics.fill(centerX - 120, top + 46, centerX + 120, top + 126, 0xFF8D7658);
        for (int x = centerX - 120; x <= centerX + 120; x += 16) {
            graphics.fill(x, top + 46, x + 1, top + 126, 0xFF725D46);
        }
        for (int y = top + 46; y <= top + 126; y += 16) {
            graphics.fill(centerX - 120, y, centerX + 120, y + 1, 0xFF725D46);
        }
    }

    private static void drawPlacedItem(GuiGraphics graphics, int centerX, int centerY, ItemStack stack, boolean highlighted) {
        if (highlighted) {
            graphics.fill(centerX - 8, centerY - 8, centerX + 8, centerY + 8, 0xFF55E6D5);
            graphics.fill(centerX - 7, centerY - 7, centerX + 7, centerY + 7, 0xFFBDEFE7);
        }
        graphics.renderItem(stack, centerX - 8, centerY - 8);
    }

    private static boolean backpackJustConnected(int backpackIndex, int visibleCables) {
        int connectionPoint = switch (backpackIndex) {
            case 0 -> 10;
            case 1 -> 16;
            case 2 -> 19;
            case 3 -> 22;
            default -> Integer.MAX_VALUE;
        };
        return visibleCables >= connectionPoint && visibleCables <= connectionPoint + 1;
    }

    private static boolean isGuideKeyDown(Minecraft minecraft) {
        return InputConstants.isKeyDown(GLFW.GLFW_KEY_S);
    }
}
