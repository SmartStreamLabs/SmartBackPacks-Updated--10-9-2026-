package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.teamsmartstreamlabs.smartbackpacks.menu.StorageControllerMenu;
import com.teamsmartstreamlabs.smartbackpacks.network.StorageControllerActionPayload;
import com.teamsmartstreamlabs.smartbackpacks.storage.StorageNetworkSnapshot;
import com.teamsmartstreamlabs.smartbackpacks.storage.StorageNetworkSnapshot.Entry;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class StorageControllerScreen extends LegacyContainerScreen<StorageControllerMenu> {
    private static final int WIDTH = 320;
    private static final int HEIGHT = 252;
    private static final int GRID_X = 70;
    private static final int GRID_Y = 52;
    private static final int GRID_COLUMNS = 10;
    private static final int GRID_ROWS = 4;
    private static final int SLOT_SIZE = 18;
    private static final float ICON_SCALE = 0.75F;
    private static final float COUNT_SCALE = 0.70F;
    private static final int COUNT_PADDING = 2;
    private static final int CAPACITY_BAR_X = 176;
    private static final int CAPACITY_BAR_Y = 10;
    private static final int CAPACITY_BAR_WIDTH = 132;
    private static final int CAPACITY_BAR_HEIGHT = 11;
    private static final int TEXT_COLOR = 0xFF3B2C20;
    private static final int MUTED_COLOR = 0xFF6F5944;
    private static final int PANEL_DARK = 0xFF3A291E;
    private static final int PANEL_MID = 0xFF9A7955;
    private static final int PANEL_LIGHT = 0xFFD9C39B;
    private static final int SLOT_FILL = 0xFFC7AE85;
    private static final int ONLINE_COLOR = 0xFF2B7D43;
    private static final int ERROR_COLOR = 0xFF9B3028;

    private EditBox searchBox;
    private int firstRow;

    public StorageControllerScreen(StorageControllerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, HEIGHT);
        this.titleLabelX = 12;
        this.titleLabelY = 11;
        this.inventoryLabelX = StorageControllerMenu.PLAYER_INVENTORY_X;
        this.inventoryLabelY = StorageControllerMenu.PLAYER_INVENTORY_Y - 13;
    }

    @Override
    protected void init() {
        super.init();
        this.searchBox = this.addRenderableWidget(new EditBox(
                this.font, this.leftPos + 86, this.topPos + 27, 164, 18,
                Component.translatable("screen.smartbackpacks.storage_controller.search")));
        this.searchBox.setHint(Component.translatable("screen.smartbackpacks.storage_controller.search_hint"));
        this.searchBox.setMaxLength(80);
        this.searchBox.setResponder(value -> this.firstRow = 0);
        this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.smartbackpacks.storage_controller.refresh"),
                        button -> PacketDistributor.sendToServer(StorageControllerActionPayload.refresh(this.menu.containerId)))
                .bounds(this.leftPos + 258, this.topPos + 27, 50, 18)
                .build());
    }

    public void refreshAfterSync() {
        this.clampFirstRow();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchBox != null && this.searchBox.isFocused()) {
            if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            if (this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.searchBox != null && this.searchBox.isFocused() && this.searchBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.fill(x, y, x + WIDTH, y + HEIGHT, PANEL_DARK);
        guiGraphics.fill(x + 3, y + 3, x + WIDTH - 3, y + HEIGHT - 3, PANEL_MID);
        guiGraphics.fill(x + 6, y + 6, x + WIDTH - 6, y + HEIGHT - 6, PANEL_LIGHT);

        guiGraphics.fill(x + GRID_X - 4, y + GRID_Y - 4,
                x + GRID_X + GRID_COLUMNS * SLOT_SIZE + 4,
                y + GRID_Y + GRID_ROWS * SLOT_SIZE + 4, PANEL_DARK);
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int column = 0; column < GRID_COLUMNS; column++) {
                drawSlot(guiGraphics, x + GRID_X + column * SLOT_SIZE, y + GRID_Y + row * SLOT_SIZE);
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlot(guiGraphics,
                        x + StorageControllerMenu.PLAYER_INVENTORY_X + column * SLOT_SIZE,
                        y + StorageControllerMenu.PLAYER_INVENTORY_Y + row * SLOT_SIZE);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawSlot(guiGraphics,
                    x + StorageControllerMenu.PLAYER_INVENTORY_X + column * SLOT_SIZE,
                    y + StorageControllerMenu.HOTBAR_Y);
        }

        List<Entry> entries = this.filteredEntries();
        int start = this.firstRow * GRID_COLUMNS;
        for (int visibleIndex = 0; visibleIndex < GRID_COLUMNS * GRID_ROWS; visibleIndex++) {
            int entryIndex = start + visibleIndex;
            if (entryIndex >= entries.size()) {
                break;
            }
            Entry entry = entries.get(entryIndex);
            int cellX = x + GRID_X + (visibleIndex % GRID_COLUMNS) * SLOT_SIZE;
            int cellY = y + GRID_Y + (visibleIndex / GRID_COLUMNS) * SLOT_SIZE;
            this.renderGridEntry(guiGraphics, entry, cellX, cellY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        StorageNetworkSnapshot snapshot = this.menu.getSnapshot();
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, TEXT_COLOR, false);
        guiGraphics.drawString(this.font,
                Component.translatable("screen.smartbackpacks.storage_controller.search"),
                12, 32, TEXT_COLOR, false);

        Component status = statusLabel(snapshot.status());
        int statusColor = snapshot.status() == StorageNetworkSnapshot.Status.ONLINE ? ONLINE_COLOR : ERROR_COLOR;
        guiGraphics.drawString(this.font, status, 12, 132, statusColor, false);
        guiGraphics.drawString(this.font,
                Component.translatable("screen.smartbackpacks.storage_controller.connected", snapshot.connectedBackpacks()),
                12, 143, MUTED_COLOR, false);
        guiGraphics.drawString(this.font,
                Component.translatable("screen.smartbackpacks.storage_controller.total_slots", snapshot.totalSlots()),
                176, 143, MUTED_COLOR, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle,
                this.inventoryLabelX, this.inventoryLabelY, TEXT_COLOR, false);

        if (snapshot.truncated()) {
            guiGraphics.drawString(this.font,
                    Component.translatable("screen.smartbackpacks.storage_controller.refine_search"),
                    176, 132, ERROR_COLOR, false);
        } else {
            this.renderCapacityBar(guiGraphics, snapshot);
        }
    }

    @Override
    protected void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Entry entry = this.hoveredEntry(mouseX, mouseY);
        if (entry == null) {
            return;
        }
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(entry.icon().getHoverName());
        tooltip.add(Component.translatable("screen.smartbackpacks.storage_controller.exact_count",
                String.format(Locale.US, "%,d", entry.count())));
        tooltip.add(Component.translatable("screen.smartbackpacks.storage_controller.extract_hint"));
        guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Entry entry = this.hoveredEntry(mouseX, mouseY);
        if (entry != null && (button == 0 || button == 1)) {
            int amount = Screen.hasShiftDown()
                    ? Integer.MAX_VALUE
                    : button == 1 ? 1 : entry.icon().getMaxStackSize();
            PacketDistributor.sendToServer(StorageControllerActionPayload.extract(
                    this.menu.containerId, entry.icon(), amount));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= this.leftPos + GRID_X
                && mouseX < this.leftPos + GRID_X + GRID_COLUMNS * SLOT_SIZE
                && mouseY >= this.topPos + GRID_Y
                && mouseY < this.topPos + GRID_Y + GRID_ROWS * SLOT_SIZE) {
            int maxRow = this.maxFirstRow();
            this.firstRow = Math.max(0, Math.min(maxRow, this.firstRow + (scrollY < 0.0D ? 1 : -1)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private List<Entry> filteredEntries() {
        String query = this.searchBox == null ? "" : this.searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            return this.menu.getSnapshot().entries();
        }
        List<Entry> filtered = new ArrayList<>();
        for (Entry entry : this.menu.getSnapshot().entries()) {
            String displayName = entry.icon().getHoverName().getString().toLowerCase(Locale.ROOT);
            String registryId = BuiltInRegistries.ITEM.getKey(entry.icon().getItem()).toString().toLowerCase(Locale.ROOT);
            if (displayName.contains(query) || registryId.contains(query)) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    private Entry hoveredEntry(double mouseX, double mouseY) {
        int relativeX = (int) mouseX - this.leftPos - GRID_X;
        int relativeY = (int) mouseY - this.topPos - GRID_Y;
        if (relativeX < 0 || relativeY < 0
                || relativeX >= GRID_COLUMNS * SLOT_SIZE
                || relativeY >= GRID_ROWS * SLOT_SIZE) {
            return null;
        }
        int visibleIndex = relativeY / SLOT_SIZE * GRID_COLUMNS + relativeX / SLOT_SIZE;
        int index = this.firstRow * GRID_COLUMNS + visibleIndex;
        List<Entry> entries = this.filteredEntries();
        return index >= 0 && index < entries.size() ? entries.get(index) : null;
    }

    private int maxFirstRow() {
        int rows = (this.filteredEntries().size() + GRID_COLUMNS - 1) / GRID_COLUMNS;
        return Math.max(0, rows - GRID_ROWS);
    }

    private void clampFirstRow() {
        this.firstRow = Math.min(this.firstRow, this.maxFirstRow());
    }

    private void renderCapacityBar(GuiGraphics guiGraphics, StorageNetworkSnapshot snapshot) {
        float fullness = snapshot.totalSlots() == 0
                ? 0.0F
                : Math.min(1.0F, (float) snapshot.usedSlots() / snapshot.totalSlots());
        int innerWidth = CAPACITY_BAR_WIDTH - 2;
        int fillWidth = Math.round(innerWidth * fullness);

        guiGraphics.fill(CAPACITY_BAR_X, CAPACITY_BAR_Y,
                CAPACITY_BAR_X + CAPACITY_BAR_WIDTH, CAPACITY_BAR_Y + CAPACITY_BAR_HEIGHT, PANEL_DARK);
        guiGraphics.fill(CAPACITY_BAR_X + 1, CAPACITY_BAR_Y + 1,
                CAPACITY_BAR_X + CAPACITY_BAR_WIDTH - 1, CAPACITY_BAR_Y + CAPACITY_BAR_HEIGHT - 1, SLOT_FILL);
        if (fillWidth > 0) {
            guiGraphics.fill(CAPACITY_BAR_X + 1, CAPACITY_BAR_Y + 1,
                    CAPACITY_BAR_X + 1 + fillWidth, CAPACITY_BAR_Y + CAPACITY_BAR_HEIGHT - 1,
                    capacityColor(fullness));
        }

        String percentage = Math.round(fullness * 100.0F) + "%";
        int textX = CAPACITY_BAR_X + (CAPACITY_BAR_WIDTH - this.font.width(percentage)) / 2;
        guiGraphics.drawString(this.font, percentage, textX, CAPACITY_BAR_Y + 1, 0xFFFFFFFF, true);
    }

    private static int capacityColor(float fullness) {
        if (fullness <= 0.5F) {
            return lerpColor(0xFF2EAC48, 0xFFE4B83F, fullness * 2.0F);
        }
        return lerpColor(0xFFE4B83F, 0xFFC73D32, (fullness - 0.5F) * 2.0F);
    }

    private static int lerpColor(int start, int end, float amount) {
        int red = Math.round(((start >> 16) & 0xFF) + (((end >> 16) & 0xFF) - ((start >> 16) & 0xFF)) * amount);
        int green = Math.round(((start >> 8) & 0xFF) + (((end >> 8) & 0xFF) - ((start >> 8) & 0xFF)) * amount);
        int blue = Math.round((start & 0xFF) + ((end & 0xFF) - (start & 0xFF)) * amount);
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    private static Component statusLabel(StorageNetworkSnapshot.Status status) {
        return Component.translatable("screen.smartbackpacks.storage_controller.status." + status.name().toLowerCase(Locale.ROOT));
    }

    private void renderGridEntry(GuiGraphics guiGraphics, Entry entry, int cellX, int cellY) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(cellX + 3.0F, cellY + 1.0F, 0.0F);
        guiGraphics.pose().scale(ICON_SCALE, ICON_SCALE, 1.0F);
        guiGraphics.renderItem(entry.icon(), 0, 0);
        guiGraphics.pose().popPose();

        String count = formatCount(entry.count());
        int countRight = cellX + SLOT_SIZE - COUNT_PADDING;
        int countBottom = cellY + SLOT_SIZE - COUNT_PADDING;
        int countWidth = Math.round(this.font.width(count) * COUNT_SCALE);
        int countHeight = Math.round(this.font.lineHeight * COUNT_SCALE);
        int countX = countRight - countWidth;
        int countY = countBottom - countHeight;

        guiGraphics.fill(countX - 1, countY - 1, countRight + 1, countBottom + 1, 0x90000000);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(countX, countY, 0.0F);
        guiGraphics.pose().scale(COUNT_SCALE, COUNT_SCALE, 1.0F);
        guiGraphics.drawString(this.font, count, 0, 0, 0xFFFFFFFF, true);
        guiGraphics.pose().popPose();
    }

    private static String formatCount(long count) {
        if (count < 1_000L) return Long.toString(count);
        if (count < 1_000_000L) return formatCompact(count, 1_000L, "K");
        if (count < 1_000_000_000L) return formatCompact(count, 1_000_000L, "M");
        if (count < 1_000_000_000_000L) return formatCompact(count, 1_000_000_000L, "B");
        if (count < 1_000_000_000_000_000L) return formatCompact(count, 1_000_000_000_000L, "T");
        return formatCompact(count, 1_000_000_000_000_000L, "Q");
    }

    private static String formatCompact(long count, long unit, String suffix) {
        double value = Math.floor(count / (double) unit * 10.0D) / 10.0D;
        if (value >= 100.0D || value == Math.floor(value)) {
            return Long.toString((long) value) + suffix;
        }
        return String.format(Locale.ROOT, "%.1f%s", value, suffix);
    }

    private static void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, PANEL_DARK);
        guiGraphics.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, PANEL_MID);
        guiGraphics.fill(x + 2, y + 2, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, SLOT_FILL);
    }
}
