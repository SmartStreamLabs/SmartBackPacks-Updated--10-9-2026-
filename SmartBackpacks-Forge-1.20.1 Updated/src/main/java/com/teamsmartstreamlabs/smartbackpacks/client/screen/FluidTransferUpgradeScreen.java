package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import com.teamsmartstreamlabs.smartbackpacks.menu.FluidTransferUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.network.FluidTransferActionPayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class FluidTransferUpgradeScreen extends LegacyContainerScreen<FluidTransferUpgradeMenu> {
    private static final int PANEL_X = 7;
    private static final int PANEL_Y = 12;
    private static final int PANEL_WIDTH = 162;
    private static final int PANEL_HEIGHT = 68;
    private static final int PANEL_BORDER = 0xFF8F989D;
    private static final int PANEL_FILL = 0xFFD9DDE0;
    private static final int PANEL_HIGHLIGHT = 0xFFF3F5F6;
    private static final int PANEL_SHADOW = 0xFFB4BBC0;
    private static final int SLOT_FILL = 0xFFC9CED1;
    private static final int SLOT_HIGHLIGHT = 0xFFE8EBED;
    private static final int SLOT_SHADOW = 0xFF8D959A;
    private static final int TANK_X = 50;
    private static final int TANK_Y = 20;
    private static final int TANK_WIDTH = 18;
    private static final int TANK_HEIGHT = 46;
    private static final int CONTAINER_SLOT_X = 14;
    private static final int CONTAINER_SLOT_Y = 34;
    private Button modeButton;
    private Button sourceButton;

    public FluidTransferUpgradeScreen(FluidTransferUpgradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 194);
        this.titleLabelX = PANEL_X + 36;
        this.titleLabelY = PANEL_Y + 4;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 86;
    }

    @Override
    protected void init() {
        super.init();
        this.modeButton = this.addRenderableWidget(Button.builder(this.getModeLabel(), button -> {
                    PacketDistributor.sendToServer(new FluidTransferActionPayload(FluidTransferActionPayload.Action.TOGGLE_MODE));
                    button.setMessage(this.getModeLabel());
                })
                .pos(this.leftPos + 76, this.topPos + 20)
                .size(42, 16)
                .tooltip(Tooltip.create(Component.literal("Switch between pulling fluids in and pushing fluids out")))
                .build());
        this.sourceButton = this.addRenderableWidget(Button.builder(this.getSourceLabel(), button -> {
                    PacketDistributor.sendToServer(new FluidTransferActionPayload(FluidTransferActionPayload.Action.TOGGLE_SOURCE));
                    button.setMessage(this.getSourceLabel());
                })
                .pos(this.leftPos + 76, this.topPos + 41)
                .size(42, 16)
                .tooltip(Tooltip.create(Component.literal("Collect adjacent fluid source blocks into the tank")))
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("Fill"), button ->
                        PacketDistributor.sendToServer(new FluidTransferActionPayload(FluidTransferActionPayload.Action.FILL)))
                .pos(this.leftPos + 76, this.topPos + 62)
                .size(42, 16)
                .tooltip(Tooltip.create(Component.literal("Move fluid from the container into the tank")))
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("Drain"), button ->
                        PacketDistributor.sendToServer(new FluidTransferActionPayload(FluidTransferActionPayload.Action.DRAIN)))
                .pos(this.leftPos + 122, this.topPos + 62)
                .size(42, 16)
                .tooltip(Tooltip.create(Component.literal("Fill the container from the tank")))
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        this.drawPanel(guiGraphics, x + PANEL_X, y + PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT);
        this.drawSlotBox(guiGraphics, x + CONTAINER_SLOT_X, y + CONTAINER_SLOT_Y, 18, 18);
        this.drawTankBox(guiGraphics, x + TANK_X, y + TANK_Y, TANK_WIDTH, TANK_HEIGHT);
        for (int slot = 0; slot < 4; slot++) {
            int filterX = x + 118 + (slot % 2) * 18;
            int filterY = y + 20 + (slot / 2) * 18;
            this.drawSlotBox(guiGraphics, filterX, filterY, 18, 18);
        }
        guiGraphics.renderItem(new ItemStack(ModItems.FLUID_TRANSFER_UPGRADE.get()), x + 13, y + 14);
        this.drawInventoryBackground(guiGraphics, x + 8, y + 96);
        this.drawFluidTank(guiGraphics, x + TANK_X, y + TANK_Y, TANK_WIDTH, TANK_HEIGHT);
    }

    @Override
    protected void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.modeButton.setMessage(this.getModeLabel());
        this.sourceButton.setMessage(this.getSourceLabel());

        if (this.isHovering(TANK_X, TANK_Y, TANK_WIDTH, TANK_HEIGHT, mouseX, mouseY)) {
            FluidStack fluid = this.menu.getDisplayedFluid();
            if (fluid.isEmpty()) {
                guiGraphics.renderTooltip(this.font, Component.literal("Empty"), mouseX, mouseY);
            } else {
                guiGraphics.renderTooltip(this.font,
                        java.util.List.of(fluid.getHoverName(), Component.literal(this.menu.getFluidAmount() + " / " + this.menu.getCapacity() + " mB")),
                        java.util.Optional.empty(),
                        mouseX,
                        mouseY);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, Component.literal(this.menu.getFluidAmount() + " / " + this.menu.getCapacity() + " mB"), 44, 64, 0x404040, false);
        guiGraphics.drawString(this.font, Component.literal("Filters"), 118, 12, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    private Component getModeLabel() {
        return Component.literal(this.menu.isPushMode() ? "Push" : "Pull");
    }

    private Component getSourceLabel() {
        return Component.literal(this.menu.isCollectSourceBlocks() ? "Source" : "No Src");
    }

    private void drawFluidTank(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        FluidStack fluid = this.menu.getDisplayedFluid();
        if (fluid.isEmpty() || this.menu.getCapacity() <= 0) {
            return;
        }

        int fillHeight = Math.max(1, Math.round((fluid.getAmount() / (float) this.menu.getCapacity()) * (height - 2)));
        int color = this.getFluidRenderColor(fluid);
        int renderColor = (color & 0xFF000000) == 0 ? (0xFF000000 | color) : color;
        guiGraphics.fill(x + 1, y + height - 1 - fillHeight, x + width - 1, y + height - 1, renderColor);
    }

    private int getFluidRenderColor(FluidStack fluid) {
        if (fluid.is(FluidTags.WATER)) {
            return 0xFF3F76E4;
        }
        if (fluid.is(FluidTags.LAVA)) {
            return 0xFFFF7A1A;
        }
        return 0xFFFFFFFF;
    }

    private void drawInventoryBackground(GuiGraphics guiGraphics, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.drawSlotBox(guiGraphics, x + column * 18, y + row * 18, 18, 18);
            }
        }

        for (int column = 0; column < 9; column++) {
            this.drawSlotBox(guiGraphics, x + column * 18, y + 58, 18, 18);
        }
    }

    private void drawPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, PANEL_BORDER);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_FILL);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + 2, PANEL_HIGHLIGHT);
        guiGraphics.fill(x + 1, y + 1, x + 2, y + height - 1, PANEL_HIGHLIGHT);
        guiGraphics.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, PANEL_SHADOW);
        guiGraphics.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, PANEL_SHADOW);
    }

    private void drawSlotBox(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, SLOT_SHADOW);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, SLOT_FILL);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + 2, SLOT_HIGHLIGHT);
        guiGraphics.fill(x + 1, y + 1, x + 2, y + height - 1, SLOT_HIGHLIGHT);
    }

    private void drawTankBox(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, SLOT_SHADOW);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF394045);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + 2, SLOT_HIGHLIGHT);
        guiGraphics.fill(x + 1, y + 1, x + 2, y + height - 1, SLOT_HIGHLIGHT);
    }
}
