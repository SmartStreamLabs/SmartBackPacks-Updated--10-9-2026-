package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.teamsmartstreamlabs.smartbackpacks.menu.SurvivalAssistUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.network.SurvivalAssistEffectPayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;

import com.teamsmartstreamlabs.smartbackpacks.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class SurvivalAssistUpgradeScreen extends LegacyContainerScreen<SurvivalAssistUpgradeMenu> {
    private static final int PANEL_X = 7;
    private static final int PANEL_Y = 12;
    private static final int PANEL_WIDTH = 162;
    private static final int PANEL_HEIGHT = 104;
    private static final int PANEL_BORDER = 0xFF8F989D;
    private static final int PANEL_FILL = 0xFFD9DDE0;
    private static final int PANEL_HIGHLIGHT = 0xFFF3F5F6;
    private static final int PANEL_SHADOW = 0xFFB4BBC0;
    private static final int SLOT_FILL = 0xFFC9CED1;
    private static final int SLOT_HIGHLIGHT = 0xFFE8EBED;
    private static final int SLOT_SHADOW = 0xFF8D959A;

    private final List<Button> removeEffectButtons = new ArrayList<>();
    private List<Identifier> effectFilters;
    private EditBox effectInput;

    public SurvivalAssistUpgradeScreen(SurvivalAssistUpgradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 226);
        this.effectFilters = new ArrayList<>(menu.getEffectFilters());
        this.titleLabelX = PANEL_X + 36;
        this.titleLabelY = PANEL_Y + 4;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 118;
    }

    @Override
    protected void init() {
        super.init();
        this.effectInput = this.addRenderableWidget(new EditBox(this.font, this.leftPos + 14, this.topPos + 74, 116, 14,
                Component.translatable("screen.smartbackpacks.survival_assist.effect_input")));
        this.effectInput.setMaxLength(50);
        this.addRenderableWidget(Button.builder(Component.translatable("screen.smartbackpacks.survival_assist.add"), button -> this.addEffect())
                .pos(this.leftPos + 134, this.topPos + 73)
                .size(30, 16)
                .tooltip(Tooltip.create(Component.literal("Add an effect id like minecraft:fire_resistance")))
                .build());
        this.refreshRemoveButtons();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        this.drawPanel(guiGraphics, x + PANEL_X, y + PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT);
        guiGraphics.renderItem(new ItemStack(ModItems.SURVIVAL_ASSIST_UPGRADE.get()), x + 13, y + 14);
        for (int slot = 0; slot < 8; slot++) {
            int slotX = x + 76 + (slot % 4) * 18;
            int slotY = y + 22 + (slot / 4) * 18;
            this.drawSlotBox(guiGraphics, slotX, slotY, 18, 18);
        }
        this.drawInventoryBackground(guiGraphics, x + 8, y + 128);
    }

    @Override
    protected void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.survival_assist.effects"), 14, 58, 0x404040, false);
        for (int index = 0; index < Math.min(this.effectFilters.size(), 4); index++) {
            guiGraphics.drawString(this.font, this.effectFilters.get(index).toString(), 14, 92 + index * 10, 0x404040, false);
        }
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.effectInput.isFocused()) {
            if (this.effectInput.keyPressed(event)) {
                return true;
            }
            if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) {
                this.addEffect();
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.effectInput.isFocused() && this.effectInput.charTyped(event)) {
            return true;
        }
        return super.charTyped(event);
    }

    private void addEffect() {
        String value = this.effectInput.getValue().trim();
        Identifier effectId = Identifier.tryParse(value);
        if (effectId == null || this.effectFilters.contains(effectId)) {
            return;
        }

        this.effectFilters = new ArrayList<>(this.effectFilters);
        this.effectFilters.add(effectId);
        ClientPacketDistributor.sendToServer(new SurvivalAssistEffectPayload(SurvivalAssistEffectPayload.Action.ADD_EFFECT, effectId.toString(), -1));
        this.effectInput.setValue("");
        this.refreshRemoveButtons();
    }

    private void removeEffect(int index) {
        if (index < 0 || index >= this.effectFilters.size()) {
            return;
        }

        this.effectFilters = new ArrayList<>(this.effectFilters);
        this.effectFilters.remove(index);
        ClientPacketDistributor.sendToServer(new SurvivalAssistEffectPayload(SurvivalAssistEffectPayload.Action.REMOVE_EFFECT, "", index));
        this.refreshRemoveButtons();
    }

    private void refreshRemoveButtons() {
        this.removeEffectButtons.forEach(this::removeWidget);
        this.removeEffectButtons.clear();

        int visible = Math.min(this.effectFilters.size(), 4);
        for (int index = 0; index < visible; index++) {
            int effectIndex = index;
            Button button = this.addRenderableWidget(Button.builder(Component.literal("x"), ignored -> this.removeEffect(effectIndex))
                    .pos(this.leftPos + 154, this.topPos + 89 + index * 10)
                    .size(12, 10)
                    .build());
            this.removeEffectButtons.add(button);
        }
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
}

