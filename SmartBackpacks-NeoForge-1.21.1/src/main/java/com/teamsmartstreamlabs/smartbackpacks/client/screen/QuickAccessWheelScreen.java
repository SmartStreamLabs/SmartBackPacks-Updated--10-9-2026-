package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import com.teamsmartstreamlabs.smartbackpacks.client.SmartBackpacksClient;
import com.teamsmartstreamlabs.smartbackpacks.network.QuickAccessSelectPayload;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.QuickAccessWheelUpgradeData;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class QuickAccessWheelScreen extends Screen {
    private static final int[] OFFSET_X = {0, 48, 68, 48, 0, -48, -68, -48};
    private static final int[] OFFSET_Y = {-68, -48, 0, 48, 68, 48, 0, -48};
    private final ItemStack[] favorites;
    private final boolean[] available;
    private int highlighted = -1;
    private boolean cancelled;

    public QuickAccessWheelScreen(ItemStack[] favorites, boolean[] available) {
        super(Component.translatable("screen.smartbackpacks.quick_access.title"));
        this.favorites = favorites;
        this.available = available;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (SmartBackpacksClient.matchesQuickAccessWheel(keyCode, scanCode)) {
            this.selectAndClose();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return true;
    }

    private void selectAndClose() {
        if (!this.cancelled && this.highlighted >= 0 && !this.favorites[this.highlighted].isEmpty()
                && this.available[this.highlighted]) {
            PacketDistributor.sendToServer(new QuickAccessSelectPayload(this.highlighted));
        }
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        this.highlighted = findSegment(mouseX - centerX, mouseY - centerY);
        graphics.fill(0, 0, this.width, this.height, 0x66000000);
        for (int slot = 0; slot < QuickAccessWheelUpgradeData.FAVORITE_COUNT; slot++) {
            int x = centerX + OFFSET_X[slot] - 13;
            int y = centerY + OFFSET_Y[slot] - 13;
            graphics.fill(x, y, x + 26, y + 26, slot == this.highlighted ? 0xFFFFD45A : 0xFF5A432B);
            graphics.fill(x + 2, y + 2, x + 24, y + 24,
                    this.favorites[slot].isEmpty() ? 0xAA3D3D3D : this.available[slot] ? 0xE0D7BF98 : 0xAA6A6060);
            if (!this.favorites[slot].isEmpty()) {
                graphics.renderItem(this.favorites[slot], x + 5, y + 5);
                if (!this.available[slot]) {
                    graphics.fill(x + 4, y + 11, x + 22, y + 15, 0xCC8B2020);
                }
            }
        }
        Component center = this.highlighted >= 0 && !this.favorites[this.highlighted].isEmpty()
                ? this.favorites[this.highlighted].getHoverName() : this.title;
        graphics.drawCenteredString(this.font, center, centerX, centerY - 4,
                this.highlighted >= 0 && !this.favorites[this.highlighted].isEmpty() && !this.available[this.highlighted]
                        ? 0xFFFF7777 : 0xFFFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.cancelled = true;
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static int findSegment(double x, double y) {
        if (x * x + y * y < 576.0D) {
            return -1;
        }
        return Math.floorMod((int) Math.round((Math.atan2(y, x) + Math.PI / 2.0D) / (Math.PI / 4.0D)),
                QuickAccessWheelUpgradeData.FAVORITE_COUNT);
    }
}
