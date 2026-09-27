package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import com.teamsmartstreamlabs.smartbackpacks.menu.DeathEmergencyKitUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.DeathEmergencyKitData;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class DeathEmergencyKitUpgradeScreen extends LegacyContainerScreen<DeathEmergencyKitUpgradeMenu> {
    private static final int BORDER = 0xFF5A432B;
    private static final int PANEL = 0xFFD7BF98;
    private static final int SLOT = 0xFF8B8B8B;
    private static final int SLOT_INNER = 0xFFC6C6C6;
    private int selectedSlot;
    private Button destinationButton;

    public DeathEmergencyKitUpgradeScreen(DeathEmergencyKitUpgradeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 232);
        this.titleLabelX = 31;
        this.titleLabelY = 9;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 126;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.literal("-"), button -> this.change(0))
                .bounds(this.leftPos + 46, this.topPos + 88, 20, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), button -> this.change(1))
                .bounds(this.leftPos + 112, this.topPos + 88, 20, 18).build());
        this.destinationButton = this.addRenderableWidget(Button.builder(this.destinationLabel(), button -> this.change(2))
                .bounds(this.leftPos + 30, this.topPos + 109, 116, 18).build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.smartbackpacks.death_emergency_kit.done"),
                button -> this.onClose()).bounds(this.leftPos + 138, this.topPos + 6, 32, 18).build());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int slot = 0; slot < DeathEmergencyKitData.SLOT_COUNT; slot++) {
            int x = this.leftPos + DeathEmergencyKitUpgradeMenu.TEMPLATE_POSITIONS[slot][0];
            int y = this.topPos + DeathEmergencyKitUpgradeMenu.TEMPLATE_POSITIONS[slot][1];
            if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18) {
                this.selectedSlot = slot;
                this.destinationButton.setMessage(this.destinationLabel());
                break;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void change(int action) {
        int id = this.selectedSlot * 3 + action;
        if (this.minecraft == null || this.minecraft.player == null || this.minecraft.gameMode == null) {
            return;
        }
        this.menu.clickMenuButton(this.minecraft.player, id);
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
        this.destinationButton.setMessage(this.destinationLabel());
    }

    private Component destinationLabel() {
        int destination = this.menu.destination(this.selectedSlot);
        return destination < 0
                ? Component.translatable("screen.smartbackpacks.death_emergency_kit.inventory")
                : Component.translatable("screen.smartbackpacks.death_emergency_kit.hotbar", destination + 1);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, BORDER);
        graphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + this.imageHeight - 2, PANEL);
        graphics.renderItem(new ItemStack(ModItems.DEATH_EMERGENCY_KIT_UPGRADE.get()), x + 10, y + 6);
        for (int slot = 0; slot < DeathEmergencyKitData.SLOT_COUNT; slot++) {
            int sx = x + DeathEmergencyKitUpgradeMenu.TEMPLATE_POSITIONS[slot][0];
            int sy = y + DeathEmergencyKitUpgradeMenu.TEMPLATE_POSITIONS[slot][1];
            graphics.fill(sx, sy, sx + 18, sy + 18, slot == this.selectedSlot ? 0xFF26B8BF : SLOT);
            graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, SLOT_INNER);
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.drawSlot(graphics, x + 8 + column * 18, y + DeathEmergencyKitUpgradeMenu.PLAYER_INVENTORY_Y + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            this.drawSlot(graphics, x + 8 + column * 18, y + DeathEmergencyKitUpgradeMenu.PLAYER_INVENTORY_Y + 58);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, Component.translatable("screen.smartbackpacks.death_emergency_kit.title"),
                this.titleLabelX, this.titleLabelY, 0xFF3C2B1C, false);
        graphics.drawString(this.font, Component.translatable("screen.smartbackpacks.death_emergency_kit.hint"),
                8, 26, 0xFF70553E, false);
        graphics.drawString(this.font, Component.translatable("screen.smartbackpacks.death_emergency_kit.selected",
                this.selectedSlot + 1), 8, 82, 0xFF3C2B1C, false);
        graphics.drawString(this.font, Integer.toString(this.menu.amount(this.selectedSlot)),
                82, 93, 0xFF3C2B1C, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX,
                this.inventoryLabelY, 0xFF3C2B1C, false);
    }

    private void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, SLOT);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, SLOT_INNER);
    }
}
