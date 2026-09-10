package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.teamsmartstreamlabs.smartbackpacks.menu.BackpackLinkUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.network.BackpackLinkActionPayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BackpackLinkData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BackpackLinkDestinationEntry;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BackpackLinkSnapshot;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class BackpackLinkUpgradeScreen extends LegacyContainerScreen<BackpackLinkUpgradeMenu> {
    private static final int TEXT_COLOR = 0xFF3D3127;
    private static final int MUTED_TEXT = 0xFF6E5740;
    private static final int PANEL_BORDER = 0xFF3D2D21;
    private static final int PANEL_FILL = 0xFFD2BC99;
    private static final int PANEL_LIGHT = 0xFFE6D3B0;
    private static final int PANEL_DARK = 0xFF9A7B59;
    private static final int CARD_FILL = 0xFFE1CCA6;
    private static final int CARD_OFFLINE = 0xFFC4A486;
    private static final int READY_COLOR = 0xFF277A3B;
    private static final int ERROR_COLOR = 0xFF8E352D;
    private static final int BUTTON_H = 18;
    private static final int DEST_PANEL_Y = 90;
    private static final int DEST_HEADER_Y = 98;
    private static final int DEST_ROW_Y = 116;
    private static final int DEST_ROW_HEIGHT = 23;

    private final List<Button> rowButtons = new ArrayList<>();
    private EditBox nameBox;
    private Button activeButton;
    private Button visibilityButton;

    public BackpackLinkUpgradeScreen(BackpackLinkUpgradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 290, 220);
        this.titleLabelX = 40;
        this.titleLabelY = 11;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        this.rowButtons.clear();
        BackpackLinkSnapshot snapshot = this.menu.getSnapshot();
        boolean canManage = snapshot.placed() && snapshot.hasUpgrade() && snapshot.configEnabled();

        this.activeButton = this.addRenderableWidget(Button.builder(this.activeLabel(), button -> this.sendSimple(BackpackLinkActionPayload.Action.TOGGLE_ACTIVE))
                .bounds(this.leftPos + 204, this.topPos + 8, 78, BUTTON_H)
                .build());
        this.activeButton.active = canManage;

        this.nameBox = this.addRenderableWidget(new EditBox(this.font, this.leftPos + 8, this.topPos + 34, 136, 18,
                Component.translatable("screen.smartbackpacks.backpack_link.name")));
        this.nameBox.setMaxLength(BackpackLinkData.MAX_NAME_LENGTH);
        this.nameBox.setValue(snapshot.sourceName());
        this.nameBox.setEditable(canManage && snapshot.hasAnchor());

        Button rename = this.addRenderableWidget(Button.builder(Component.translatable("screen.smartbackpacks.backpack_link.rename"), button -> this.rename())
                .bounds(this.leftPos + 150, this.topPos + 34, 48, BUTTON_H)
                .build());
        rename.active = canManage && snapshot.hasAnchor();

        this.visibilityButton = this.addRenderableWidget(Button.builder(this.visibilityLabel(), button -> this.sendSimple(BackpackLinkActionPayload.Action.CYCLE_VISIBILITY))
                .bounds(this.leftPos + 204, this.topPos + 34, 78, BUTTON_H)
                .build());
        this.visibilityButton.active = canManage && snapshot.hasAnchor();

        Button crystal = this.addRenderableWidget(Button.builder(Component.translatable("screen.smartbackpacks.backpack_link.crystal"), button -> this.sendSimple(BackpackLinkActionPayload.Action.BIND_OR_COMPLETE_CRYSTAL))
                .bounds(this.leftPos + 8, this.topPos + 58, 118, BUTTON_H)
                .build());
        crystal.active = canManage && snapshot.active();

        this.addDestinationButtons(snapshot);
    }

    public void refreshAfterSync() {
        this.rebuildWidgets();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        this.drawPanel(guiGraphics, x, y, this.imageWidth, this.imageHeight);
        guiGraphics.renderItem(new ItemStack(ModItems.BACKPACK_LINK_UPGRADE.get()), x + 10, y + 8);

        this.drawInset(guiGraphics, x + 8, y + DEST_PANEL_Y, this.imageWidth - 16, 118);
        List<BackpackLinkDestinationEntry> destinations = this.menu.getSnapshot().destinations();
        for (int index = 0; index < Math.min(4, destinations.size()); index++) {
            int rowY = y + DEST_ROW_Y + index * DEST_ROW_HEIGHT;
            boolean ready = destinations.get(index).canTeleport();
            guiGraphics.fill(x + 12, rowY, x + 278, rowY + 20, PANEL_DARK);
            guiGraphics.fill(x + 13, rowY + 1, x + 277, rowY + 19, ready ? CARD_FILL : CARD_OFFLINE);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        BackpackLinkSnapshot snapshot = this.menu.getSnapshot();
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, TEXT_COLOR, false);
        guiGraphics.drawString(this.font, Component.translatable(snapshot.statusKey()), 130, 63,
                snapshot.active() ? READY_COLOR : ERROR_COLOR, false);
        guiGraphics.drawString(this.font, this.sourceDetails(snapshot), 8, 78, MUTED_TEXT, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.backpack_link.destinations",
                snapshot.destinations().size(), snapshot.maxDestinations()), 12, DEST_HEADER_Y, TEXT_COLOR, false);

        if (snapshot.destinations().isEmpty()) {
            guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.backpack_link.no_destinations"), 18, DEST_ROW_Y + 4, MUTED_TEXT, false);
            return;
        }

        for (int index = 0; index < Math.min(4, snapshot.destinations().size()); index++) {
            BackpackLinkDestinationEntry entry = snapshot.destinations().get(index);
            int y = DEST_ROW_Y + index * DEST_ROW_HEIGHT;
            int color = entry.canTeleport() ? TEXT_COLOR : MUTED_TEXT;
            guiGraphics.drawString(this.font, this.clipped(entry.name(), 106), 16, y + 3, color, false);
            guiGraphics.drawString(this.font, this.clipped(this.destinationDetails(entry).getString(), 182), 16, y + 12, MUTED_TEXT, false);
            guiGraphics.drawString(this.font, this.clipped(this.statusLabel(entry).getString(), 72), 126, y + 3,
                    entry.canTeleport() ? READY_COLOR : ERROR_COLOR, false);
        }
    }

    @Override
    protected void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        BackpackLinkDestinationEntry hovered = this.hoveredDestination(mouseX, mouseY);
        if (hovered != null) {
            guiGraphics.renderComponentTooltip(this.font, this.destinationTooltip(hovered), mouseX, mouseY);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.nameBox != null && this.nameBox.isFocused()) {
            if (this.nameBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                this.rename();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.nameBox != null && this.nameBox.isFocused() && this.nameBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void addDestinationButtons(BackpackLinkSnapshot snapshot) {
        for (int index = 0; index < Math.min(4, snapshot.destinations().size()); index++) {
            BackpackLinkDestinationEntry entry = snapshot.destinations().get(index);
            int rowY = this.topPos + DEST_ROW_Y + index * DEST_ROW_HEIGHT + 1;
            Button teleport = this.addRenderableWidget(Button.builder(Component.translatable("screen.smartbackpacks.backpack_link.teleport"),
                            button -> PacketDistributor.sendToServer(new BackpackLinkActionPayload(BackpackLinkActionPayload.Action.TELEPORT, entry.anchorId(), "")))
                    .bounds(this.leftPos + 208, rowY, 46, BUTTON_H)
                    .tooltip(Tooltip.create(entry.canTeleport()
                            ? Component.translatable("tooltip.smartbackpacks.backpack_link.travel_ready", entry.name())
                            : Component.translatable("tooltip.smartbackpacks.backpack_link.travel_blocked", this.statusLabel(entry))))
                    .build());
            teleport.active = entry.canTeleport();
            this.rowButtons.add(teleport);

            Button unlink = this.addRenderableWidget(Button.builder(Component.literal("X"),
                            button -> PacketDistributor.sendToServer(new BackpackLinkActionPayload(BackpackLinkActionPayload.Action.UNLINK, entry.anchorId(), "")))
                    .bounds(this.leftPos + 258, rowY, 20, BUTTON_H)
                    .tooltip(Tooltip.create(Component.translatable("tooltip.smartbackpacks.backpack_link.unlink", entry.name())))
                    .build());
            unlink.active = snapshot.hasAnchor();
            this.rowButtons.add(unlink);
        }
    }

    private void rename() {
        String value = this.nameBox == null ? "" : this.nameBox.getValue();
        PacketDistributor.sendToServer(new BackpackLinkActionPayload(BackpackLinkActionPayload.Action.RENAME, BackpackLinkData.EMPTY_UUID, value));
    }

    private void sendSimple(BackpackLinkActionPayload.Action action) {
        PacketDistributor.sendToServer(BackpackLinkActionPayload.simple(action));
    }

    private Component activeLabel() {
        return Component.translatable(this.menu.getSnapshot().active()
                ? "screen.smartbackpacks.backpack_link.deactivate"
                : "screen.smartbackpacks.backpack_link.activate");
    }

    private Component visibilityLabel() {
        return Component.translatable("screen.smartbackpacks.backpack_link.visibility." + this.menu.getSnapshot().visibility().getSerializedName());
    }

    private Component sourceDetails(BackpackLinkSnapshot snapshot) {
        if (!snapshot.placed()) {
            return Component.translatable("screen.smartbackpacks.backpack_link.placed_only");
        }
        return Component.literal(this.clipped(snapshot.dimension() + "  " + snapshot.position().toShortString(), 198));
    }

    private Component destinationDetails(BackpackLinkDestinationEntry entry) {
        Component distance = entry.sameDimension()
                ? Component.translatable("screen.smartbackpacks.backpack_link.distance", entry.distanceBlocks())
                : Component.translatable("screen.smartbackpacks.backpack_link.cross_dimension");
        return Component.translatable("screen.smartbackpacks.backpack_link.entry_details", distance, entry.costLevels());
    }

    private Component statusLabel(BackpackLinkDestinationEntry entry) {
        if ("message.smartbackpacks.backpack_link.cooldown_short".equals(entry.statusKey())) {
            return Component.translatable("screen.smartbackpacks.backpack_link.status.cooldown", entry.cooldownSeconds());
        }
        if ("message.smartbackpacks.backpack_link.ready".equals(entry.statusKey())) {
            return Component.translatable("screen.smartbackpacks.backpack_link.status.ready");
        }
        if ("message.smartbackpacks.backpack_link.destination_offline".equals(entry.statusKey())) {
            return Component.translatable("screen.smartbackpacks.backpack_link.status.offline");
        }
        if ("message.smartbackpacks.backpack_link.destination_not_placed".equals(entry.statusKey())) {
            return Component.translatable("screen.smartbackpacks.backpack_link.status.unplaced");
        }
        if ("message.smartbackpacks.backpack_link.destination_chunk_unloaded".equals(entry.statusKey())) {
            return Component.translatable("screen.smartbackpacks.backpack_link.status.unloaded");
        }
        if ("message.smartbackpacks.backpack_link.no_permission".equals(entry.statusKey())) {
            return Component.translatable("screen.smartbackpacks.backpack_link.status.no_access");
        }
        if ("message.smartbackpacks.backpack_link.cross_dimension_disabled".equals(entry.statusKey())) {
            return Component.translatable("screen.smartbackpacks.backpack_link.status.blocked");
        }
        return Component.translatable(entry.statusKey());
    }

    private BackpackLinkDestinationEntry hoveredDestination(int mouseX, int mouseY) {
        List<BackpackLinkDestinationEntry> destinations = this.menu.getSnapshot().destinations();
        for (int index = 0; index < Math.min(4, destinations.size()); index++) {
            int rowX = this.leftPos + 12;
            int rowY = this.topPos + DEST_ROW_Y + index * DEST_ROW_HEIGHT;
            if (mouseX >= rowX && mouseX < rowX + 266 && mouseY >= rowY && mouseY < rowY + 20) {
                return destinations.get(index);
            }
        }
        return null;
    }

    private List<Component> destinationTooltip(BackpackLinkDestinationEntry entry) {
        MutableComponent distance = entry.sameDimension()
                ? Component.translatable("tooltip.smartbackpacks.backpack_link.distance", entry.distanceBlocks())
                : Component.translatable("tooltip.smartbackpacks.backpack_link.cross_dimension");
        return List.of(
                Component.literal(entry.name()).withStyle(ChatFormatting.GOLD),
                Component.translatable("tooltip.smartbackpacks.backpack_link.dimension", entry.dimension()).withStyle(ChatFormatting.GRAY),
                Component.translatable("tooltip.smartbackpacks.backpack_link.position", entry.position().toShortString()).withStyle(ChatFormatting.GRAY),
                distance.withStyle(ChatFormatting.GRAY),
                Component.translatable("tooltip.smartbackpacks.backpack_link.cost", entry.costLevels()).withStyle(ChatFormatting.AQUA),
                Component.translatable("tooltip.smartbackpacks.backpack_link.status", this.statusLabel(entry)).withStyle(entry.canTeleport() ? ChatFormatting.GREEN : ChatFormatting.RED)
        );
    }

    private String clipped(String value, int width) {
        return this.font.width(value) <= width ? value : this.font.plainSubstrByWidth(value, width - this.font.width("...")) + "...";
    }

    private void drawPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, PANEL_BORDER);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_FILL);
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + 3, PANEL_LIGHT);
        guiGraphics.fill(x + 2, y + height - 3, x + width - 2, y + height - 2, PANEL_DARK);
    }

    private void drawInset(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, PANEL_DARK);
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, PANEL_FILL);
    }
}
