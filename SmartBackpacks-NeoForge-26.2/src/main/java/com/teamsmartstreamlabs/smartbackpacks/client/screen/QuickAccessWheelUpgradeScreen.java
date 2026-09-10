package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import com.teamsmartstreamlabs.smartbackpacks.client.gui.GuiGraphics;
import com.teamsmartstreamlabs.smartbackpacks.menu.QuickAccessWheelUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.QuickAccessWheelUpgradeData;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class QuickAccessWheelUpgradeScreen extends LegacyContainerScreen<QuickAccessWheelUpgradeMenu> {
    private static final int PANEL = 0xFFD7BF98;
    private static final int BORDER = 0xFF5A432B;
    private static final int SLOT = 0xFF8B8B8B;
    private static final int SLOT_INNER = 0xFFC6C6C6;
    private static final float INSTRUCTION_SCALE = 0.65F;

    public QuickAccessWheelUpgradeScreen(QuickAccessWheelUpgradeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 202);
        this.titleLabelX = 32;
        this.titleLabelY = 8;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 98;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, BORDER);
        graphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + this.imageHeight - 2, PANEL);
        graphics.renderItem(new ItemStack(ModItems.QUICK_ACCESS_WHEEL_UPGRADE.get()), x + 10, y + 5);
        for (int slot = 0; slot < QuickAccessWheelUpgradeData.FAVORITE_COUNT; slot++) {
            this.drawSlot(graphics, x + QuickAccessWheelUpgradeMenu.FAVORITE_POSITIONS[slot][0],
                    y + QuickAccessWheelUpgradeMenu.FAVORITE_POSITIONS[slot][1]);
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.drawSlot(graphics, x + 8 + column * 18, y + QuickAccessWheelUpgradeMenu.PLAYER_INVENTORY_Y + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            this.drawSlot(graphics, x + 8 + column * 18, y + QuickAccessWheelUpgradeMenu.PLAYER_INVENTORY_Y + 58);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFF3C2B1C, false);
        this.drawCenteredInstruction(graphics, Component.translatable("screen.smartbackpacks.quick_access.configure_set"), 49);
        this.drawCenteredInstruction(graphics, Component.translatable("screen.smartbackpacks.quick_access.configure_clear"), 61);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFF3C2B1C, false);
    }

    private void drawCenteredInstruction(GuiGraphics graphics, Component text, int y) {
        graphics.pose().pushMatrix();
        graphics.pose().scale(INSTRUCTION_SCALE, INSTRUCTION_SCALE);
        graphics.drawCenteredString(this.font, text, Math.round(88.0F / INSTRUCTION_SCALE),
                Math.round(y / INSTRUCTION_SCALE), 0xFF755D44);
        graphics.pose().popMatrix();
    }

    private void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, SLOT);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, SLOT_INNER);
    }
}
