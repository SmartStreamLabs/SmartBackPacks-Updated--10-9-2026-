package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mojang.blaze3d.platform.InputConstants;
import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackSortMode;
import com.teamsmartstreamlabs.smartbackpacks.menu.BackpackMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.BackpackScreenLayout;
import com.teamsmartstreamlabs.smartbackpacks.network.SetBackpackScrollOffsetPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.SortOpenBackpackPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.ToggleItemLockPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.ToggleUpgradeEnabledPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.TrashCanActionPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.UseInstalledUpgradePayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacityWarningSnapshot;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacityWarningState;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.TrashCanUpgradeHandler;

import com.teamsmartstreamlabs.smartbackpacks.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public class BackpackScreen extends LegacyContainerScreen<BackpackMenu> {
    private static final float TITLE_SCALE = 0.8F;

    private static final int TEXT_COLOR = 0xFF3D3127;
    private static final int MUTED_TEXT_COLOR = 0xFF6F6155;
    private static final int SEARCH_TEXT_COLOR = 0xFFEADCC7;
    private static final int SEARCH_HINT_COLOR = 0xFFC0B09D;
    private static final int PANEL_BORDER = 0xFF3D2D21;
    private static final int PANEL_SHADOW = 0xFF241A13;
    private static final int PANEL_FILL = 0xFFCAB28F;
    private static final int PANEL_LIGHT = 0xFFE2D0AE;
    private static final int PANEL_DARK = 0xFF9A7B59;
    private static final int TOP_BAR_FILL = 0xFFD8C19D;
    private static final int UPGRADE_PANEL_FILL = 0xFFB8956B;
    private static final int STORAGE_PANEL_FILL = 0xFFD2BC99;
    private static final int PLAYER_PANEL_FILL = 0xFFC7AD87;
    private static final int SEARCH_FILL = 0xFF3B3028;
    private static final int SEARCH_BORDER = 0xFF6A5745;
    private static final int SLOT_FILL = 0xFFC5AA84;
    private static final int SLOT_INNER = 0xFFD9C7A8;
    private static final int SLOT_DISABLED = 0xFF9B8468;
    private static final int SLOT_HOVER = 0xFFE8D9BB;
    private static final int BUTTON_FILL = 0xFF6C5B4A;
    private static final int BUTTON_HOVER_FILL = 0xFF84715E;
    private static final int BUTTON_DISABLED_FILL = 0xFF4E443B;
    private static final int BUTTON_BORDER = 0xFF2E241C;
    private static final int BUTTON_LIGHT = 0xFFC6B59C;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_MIN_HEIGHT = 15;
    private static final int SCROLLBAR_TRACK_COLOR = 0xFF8E7760;
    private static final int SCROLLBAR_THUMB_COLOR = 0xFFD9C8B1;
    private static final int SCROLLBAR_BORDER_COLOR = 0xFF5E4935;
    private static final int STACK_TEXT_BACKGROUND = 0xFFD3C1A7;
    private static final int TRASH_PANEL_FILL = 0xFFB89075;
    private static final int TRASH_SLOT_FILL = 0xFF70554B;
    private static final int TRASH_WARNING_FILL = 0xFF8E3B2D;
    private static final int TRASH_PROGRESS_FILL = 0xFFE55E47;
    private static final int CAPACITY_BAR_BG = 0xFF6B5440;

    private static final int TITLE_ICON_X = BackpackScreenLayout.UPGRADE_PANEL_WIDTH + 9;
    private static final int TITLE_ICON_Y = 7;
    private static final int TITLE_LABEL_X = TITLE_ICON_X + 22;
    private static final int TITLE_LABEL_Y = 10;
    private static final int TOP_BUTTON_SIZE = 16;
    private static final int TOP_RIGHT_PADDING = 8;
    private static final int SEARCH_FIELD_WIDTH = 72;
    private static final int SEARCH_FIELD_HEIGHT = 16;
    private static final int SEARCH_FIELD_Y = 6;
    private static final int SORT_BUTTON_X = BackpackScreenLayout.IMAGE_WIDTH - TOP_RIGHT_PADDING - TOP_BUTTON_SIZE;
    private static final int SEARCH_FIELD_X = SORT_BUTTON_X - 4 - SEARCH_FIELD_WIDTH;
    private static final int LOCK_BUTTON_X = SEARCH_FIELD_X - 4 - TOP_BUTTON_SIZE;
    private static final int SEARCH_TEXT_X = SEARCH_FIELD_X + 16;
    private static final int SEARCH_TEXT_Y = SEARCH_FIELD_Y + 4;
    private static final int SEARCH_TEXT_WIDTH = SEARCH_FIELD_WIDTH - 20;

    private static final NumberFormat STACK_COUNT_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

    private BackpackSortMode sortMode;
    private Button sortButton;
    private LockModeButton itemLockButton;
    private Button trashRestoreButton;
    private Button trashDeleteButton;
    private UpgradeToggleButton[] upgradeToggleButtons = new UpgradeToggleButton[0];
    private EditBox searchBox;
    private boolean scrolling;
    private boolean itemLockMode;

    public BackpackScreen(BackpackMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, BackpackScreenLayout.IMAGE_WIDTH, menu.getImageHeight());
        this.titleLabelX = TITLE_LABEL_X;
        this.titleLabelY = TITLE_LABEL_Y;
        this.inventoryLabelX = BackpackScreenLayout.PLAYER_INVENTORY_X;
        this.inventoryLabelY = BackpackScreenLayout.inventoryLabelY(menu.getVisibleBackpackRows());
        this.sortMode = menu.getSortMode();
    }

    @Override
    protected void init() {
        super.init();
        this.searchBox = this.addRenderableWidget(new EditBox(this.font,
                this.leftPos + SEARCH_TEXT_X,
                this.topPos + SEARCH_TEXT_Y,
                SEARCH_TEXT_WIDTH,
                10,
                Component.translatable("screen.smartbackpacks.search")));
        this.searchBox.setBordered(false);
        this.searchBox.setTextColor(SEARCH_TEXT_COLOR);
        this.searchBox.setTextColorUneditable(SEARCH_HINT_COLOR);
        this.searchBox.setMaxLength(50);
        this.searchBox.setResponder(value -> this.applySearchFilter());
        this.searchBox.setCanLoseFocus(true);

        this.sortButton = this.addRenderableWidget(new SortIconButton(
                this.leftPos + SORT_BUTTON_X,
                this.topPos + SEARCH_FIELD_Y,
                this.getSortButtonLabel(),
                button -> this.cycleSortMode(),
                this.getSortButtonTooltip()));
        this.itemLockButton = this.addRenderableWidget(new LockModeButton(
                this.leftPos + LOCK_BUTTON_X,
                this.topPos + SEARCH_FIELD_Y,
                button -> this.toggleItemLockMode()));
        this.initUpgradeButtons();
        this.initTrashButtons();
        this.applySearchFilter();
    }

    public BackpackSortMode getSortMode() {
        return this.sortMode;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        this.updateUpgradeButtons();
        this.updateTrashButtons();
        this.updateItemLockButton();

        int x = this.leftPos;
        int y = this.topPos;
        int accent = this.getTierAccentColor();
        int visibleRows = this.menu.getVisibleBackpackRows();
        int gridBottom = y + BackpackScreenLayout.BACKPACK_SLOT_Y + visibleRows * BackpackScreenLayout.SLOT_SIZE;
        int playerInventoryY = BackpackScreenLayout.playerInventoryY(visibleRows);
        int inventoryDividerY = y + playerInventoryY - 16;

        drawPanel(guiGraphics, x, y, this.imageWidth, this.imageHeight, PANEL_FILL);
        guiGraphics.fill(x + 1, y + 1, x + this.imageWidth - 1, y + BackpackScreenLayout.TOP_BAR_HEIGHT, TOP_BAR_FILL);
        guiGraphics.fill(x + 1, y + BackpackScreenLayout.TOP_BAR_HEIGHT, x + BackpackScreenLayout.UPGRADE_PANEL_WIDTH, y + this.imageHeight - 1, UPGRADE_PANEL_FILL);
        guiGraphics.fill(x + BackpackScreenLayout.UPGRADE_PANEL_WIDTH, y + BackpackScreenLayout.TOP_BAR_HEIGHT,
                x + this.imageWidth - 1, inventoryDividerY, STORAGE_PANEL_FILL);
        guiGraphics.fill(x + BackpackScreenLayout.UPGRADE_PANEL_WIDTH, inventoryDividerY,
                x + this.imageWidth - 1, y + this.imageHeight - 1, PLAYER_PANEL_FILL);

        guiGraphics.fill(x + BackpackScreenLayout.UPGRADE_PANEL_WIDTH - 1, y + 1,
                x + BackpackScreenLayout.UPGRADE_PANEL_WIDTH + 1, y + this.imageHeight - 1, PANEL_DARK);
        guiGraphics.fill(x + 1, y + BackpackScreenLayout.TOP_BAR_HEIGHT,
                x + this.imageWidth - 1, y + BackpackScreenLayout.TOP_BAR_HEIGHT + 1, PANEL_DARK);
        guiGraphics.fill(x + BackpackScreenLayout.UPGRADE_PANEL_WIDTH + 4, inventoryDividerY,
                x + this.imageWidth - 6, inventoryDividerY + 1, PANEL_DARK);
        guiGraphics.fill(x + BackpackScreenLayout.UPGRADE_PANEL_WIDTH + 8, y + BackpackScreenLayout.TOP_BAR_HEIGHT - 2,
                x + this.imageWidth - 10, y + BackpackScreenLayout.TOP_BAR_HEIGHT, accent);

        drawInsetPanel(guiGraphics,
                x + BackpackScreenLayout.BACKPACK_SLOT_X - 6,
                y + BackpackScreenLayout.BACKPACK_SLOT_Y - 6,
                BackpackScreenLayout.BACKPACK_COLUMNS * BackpackScreenLayout.SLOT_SIZE + 24,
                visibleRows * BackpackScreenLayout.SLOT_SIZE + 12,
                0xFFC1A27A);
        drawInsetPanel(guiGraphics,
                x + BackpackScreenLayout.PLAYER_INVENTORY_X - 6,
                y + playerInventoryY - 4,
                BackpackScreenLayout.BACKPACK_COLUMNS * BackpackScreenLayout.SLOT_SIZE + 12,
                BackpackScreenLayout.PLAYER_HOTBAR_OFFSET_Y + BackpackScreenLayout.SLOT_SIZE + 8,
                0xFFB99670);

        this.renderTitleIcon(guiGraphics, x, y, accent);
        this.renderTrashPanel(guiGraphics, x, y);
        this.renderSlotBackgrounds(guiGraphics, accent);
        this.renderSearchField(guiGraphics, x, y, accent);
        this.renderScrollBar(guiGraphics);
        this.renderCapacityIndicator(guiGraphics, x, y);
    }

    @Override
    protected void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderItemLockOverlays(guiGraphics);
        this.renderCompactBackpackCounts(guiGraphics);
        this.renderTrashOverlay(guiGraphics);
        if (this.searchBox.getValue().isEmpty() && !this.searchBox.isFocused()) {
            guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.search_hint"),
                    this.leftPos + SEARCH_TEXT_X, this.topPos + SEARCH_TEXT_Y + 1, SEARCH_HINT_COLOR, false);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int maxTitleWidth = Math.round((SEARCH_FIELD_X - this.titleLabelX - 8) / TITLE_SCALE);
        maxTitleWidth = Math.min(maxTitleWidth, Math.round((LOCK_BUTTON_X - this.titleLabelX - 8) / TITLE_SCALE));
        Component titleComponent = this.title;
        if (this.font.width(titleComponent) > maxTitleWidth) {
            String shortened = this.font.plainSubstrByWidth(titleComponent.getString(), Math.max(0, maxTitleWidth - this.font.width("...")));
            titleComponent = Component.literal(shortened + "...");
        }

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(TITLE_SCALE, TITLE_SCALE);
        guiGraphics.drawString(this.font, titleComponent,
                Math.round(this.titleLabelX / TITLE_SCALE),
                Math.round(this.titleLabelY / TITLE_SCALE),
                TEXT_COLOR, false);
        guiGraphics.pose().popMatrix();

        guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.upgrades"),
                4, 10, TEXT_COLOR, false);
        this.renderCapacityLabel(guiGraphics);
        guiGraphics.drawString(this.font, this.playerInventoryTitle,
                this.inventoryLabelX, this.inventoryLabelY, TEXT_COLOR, false);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.searchBox.isFocused()) {
            if (this.searchBox.keyPressed(event)) {
                return true;
            }

            if (this.minecraft != null && this.minecraft.options.keyInventory.matches(event)) {
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.searchBox.isFocused() && this.searchBox.charTyped(event)) {
            return true;
        }
        return super.charTyped(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (this.isMouseOverSearchField(mouseX, mouseY)) {
            this.setFocused(this.searchBox);
            this.searchBox.setFocused(true);
            this.searchBox.mouseClicked(event, doubleClick);
            return true;
        }

        if (this.searchBox.mouseClicked(event, doubleClick)) {
            this.setFocused(this.searchBox);
            return true;
        }

        if (button == 1) {
            int upgradeSlot = this.getHoveredUpgradeSlot(mouseX, mouseY);
            if (upgradeSlot >= 0) {
                PacketDistributor.sendToServer(new UseInstalledUpgradePayload(upgradeSlot, event.hasShiftDown()));
                return true;
            }
        }

        if (this.itemLockMode && button == 0 && !this.searchBox.isFocused()) {
            int slotIndex = this.getHoveredBackpackMenuSlot(mouseX, mouseY);
            if (slotIndex >= 0) {
                ToggleItemLockPayload.Action action = event.hasControlDown()
                        ? ToggleItemLockPayload.Action.TOGGLE_TYPE_LOCK
                        : event.hasShiftDown()
                        ? ToggleItemLockPayload.Action.TOGGLE_ITEM_LOCK
                        : ToggleItemLockPayload.Action.TOGGLE_SLOT_LOCK;
                PacketDistributor.sendToServer(new ToggleItemLockPayload(action, this.menu.getLogicalBackpackSlot(slotIndex)));
                return true;
            }
        }

        if (button == 0 && this.isTrashShortcutDown() && !this.searchBox.isFocused()) {
            int slotIndex = this.getHoveredMenuSlot(mouseX, mouseY);
            if (slotIndex >= 0 && !this.menu.isTrashSlotIndex(slotIndex)) {
                PacketDistributor.sendToServer(new TrashCanActionPayload(TrashCanActionPayload.Action.TRASH_SLOT, slotIndex));
                return true;
            }
        }

        if (button == 0 && this.isMouseOverScrollBar(mouseX, mouseY)) {
            this.scrolling = true;
            this.scrollTo(mouseY);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private int getHoveredUpgradeSlot(double mouseX, double mouseY) {
        int backpackSlots = this.menu.getBackpackSlotCount();
        int upgradeEnd = backpackSlots + this.menu.getUpgradeSlotCount();
        for (int slotIndex = backpackSlots; slotIndex < upgradeEnd; slotIndex++) {
            Slot slot = this.menu.slots.get(slotIndex);
            if (slot.hasItem() && this.isHovering(slot.x, slot.y, BackpackScreenLayout.SLOT_SIZE, BackpackScreenLayout.SLOT_SIZE, mouseX, mouseY)) {
                return slotIndex - backpackSlots;
            }
        }
        return -1;
    }

    private int getHoveredMenuSlot(double mouseX, double mouseY) {
        for (int slotIndex = 0; slotIndex < this.menu.slots.size(); slotIndex++) {
            Slot slot = this.menu.slots.get(slotIndex);
            if (slot.isActive() && slot.hasItem()
                    && this.isHovering(slot.x, slot.y, BackpackScreenLayout.SLOT_SIZE, BackpackScreenLayout.SLOT_SIZE, mouseX, mouseY)) {
                return slotIndex;
            }
        }
        return -1;
    }

    private int getHoveredBackpackMenuSlot(double mouseX, double mouseY) {
        for (int slotIndex = 0; slotIndex < this.menu.getBackpackSlotCount(); slotIndex++) {
            Slot slot = this.menu.slots.get(slotIndex);
            if (slot.isActive()
                    && this.isHovering(slot.x, slot.y, BackpackScreenLayout.SLOT_SIZE, BackpackScreenLayout.SLOT_SIZE, mouseX, mouseY)) {
                return slotIndex;
            }
        }
        return -1;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!this.menu.canScroll() || scrollY == 0 || !this.isMouseOverBackpackArea(mouseX, mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        this.menu.scrollRows(scrollY < 0 ? 1 : -1);
        this.syncScrollOffset();
        this.applySearchFilter();
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.scrolling) {
            this.scrollTo(event.y());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.scrolling = false;
        return super.mouseReleased(event);
    }

    private void cycleSortMode() {
        this.sortMode = this.sortMode.next();
        this.sortButton.setMessage(this.getSortButtonLabel());
        this.sortButton.setTooltip(this.getSortButtonTooltip());
        PacketDistributor.sendToServer(new SortOpenBackpackPayload(this.sortMode));
    }

    private void applySearchFilter() {
        String query = this.searchBox.getValue().trim().toLowerCase();
        this.menu.applyClientSearchFilter(stack -> query.isEmpty() || this.matchesSearch(stack, query));
    }

    private boolean matchesSearch(ItemStack stack, String query) {
        if (stack.isEmpty()) {
            return false;
        }

        String itemName = stack.getHoverName().getString().toLowerCase();
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().toLowerCase();
        String modId = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().toLowerCase();

        if (query.startsWith("@")) {
            String modQuery = query.substring(1).trim();
            return !modQuery.isEmpty() && modId.contains(modQuery);
        }

        return itemName.contains(query) || itemId.contains(query);
    }

    private Component getSortButtonLabel() {
        return Component.literal(this.sortMode.getShortLabel());
    }

    private Tooltip getSortButtonTooltip() {
        return Tooltip.create(Component.translatable("tooltip.smartbackpacks.sort_button",
                Component.translatable(this.sortMode.getTranslationKey())));
    }

    private void toggleItemLockMode() {
        this.itemLockMode = !this.itemLockMode;
        this.updateItemLockButton();
    }

    private void initUpgradeButtons() {
        this.upgradeToggleButtons = new UpgradeToggleButton[this.menu.getUpgradeSlotCount()];
        for (int slot = 0; slot < this.upgradeToggleButtons.length; slot++) {
            int upgradeSlot = slot;
            UpgradeToggleButton button = this.addRenderableWidget(new UpgradeToggleButton(
                    this.leftPos + BackpackScreenLayout.UPGRADE_TOGGLE_X,
                    this.topPos + BackpackScreenLayout.UPGRADE_SLOT_Y + slot * BackpackScreenLayout.UPGRADE_SLOT_STEP_Y + 5,
                    ignored -> this.toggleUpgrade(upgradeSlot)));
            this.upgradeToggleButtons[slot] = button;
        }
        this.updateUpgradeButtons();
    }

    private void initTrashButtons() {
        this.trashRestoreButton = this.addRenderableWidget(Button.builder(Component.literal("R"), button -> this.restoreLastTrashedItem())
                .bounds(this.leftPos + BackpackScreenLayout.TRASH_BUTTON_X,
                        this.topPos + BackpackScreenLayout.TRASH_RESTORE_BUTTON_Y,
                        BackpackScreenLayout.TRASH_BUTTON_WIDTH,
                        BackpackScreenLayout.TRASH_BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("tooltip.smartbackpacks.trash_can_restore_button")))
                .build());
        this.trashDeleteButton = this.addRenderableWidget(Button.builder(Component.literal("X"), button -> this.requestTrashDeleteNow())
                .bounds(this.leftPos + BackpackScreenLayout.TRASH_BUTTON_X,
                        this.topPos + BackpackScreenLayout.TRASH_DELETE_BUTTON_Y,
                        BackpackScreenLayout.TRASH_BUTTON_WIDTH,
                        BackpackScreenLayout.TRASH_BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("tooltip.smartbackpacks.trash_can_delete_now_button")))
                .build());
        this.updateTrashButtons();
    }

    private void updateUpgradeButtons() {
        for (int slot = 0; slot < this.upgradeToggleButtons.length; slot++) {
            UpgradeToggleButton button = this.upgradeToggleButtons[slot];
            ItemStack stack = this.menu.getUpgradeStack(slot);
            boolean hasUpgrade = !stack.isEmpty();
            boolean enabled = this.menu.isUpgradeEnabled(slot);
            button.visible = hasUpgrade;
            button.active = hasUpgrade;
            button.setEnabledState(enabled);
            button.setTooltip(Tooltip.create(Component.translatable(
                    enabled ? "tooltip.smartbackpacks.upgrade_enabled" : "tooltip.smartbackpacks.upgrade_disabled")));
        }
    }

    private void updateTrashButtons() {
        boolean hasTrashCan = this.menu.hasTrashCanUpgrade();
        boolean hasPending = this.menu.hasTrashPendingItem();
        boolean hasRecovery = this.menu.hasTrashRecoveryItem();
        if (this.trashRestoreButton != null) {
            this.trashRestoreButton.visible = hasTrashCan;
            this.trashRestoreButton.active = hasTrashCan && hasRecovery;
        }
        if (this.trashDeleteButton != null) {
            this.trashDeleteButton.visible = hasTrashCan && SmartBackpacksConfig.trashCanEnableDeleteNowButton();
            this.trashDeleteButton.active = hasTrashCan && hasPending;
        }
    }

    private void updateItemLockButton() {
        if (this.itemLockButton == null) {
            return;
        }

        boolean available = this.menu.hasItemLockUpgrade();
        this.itemLockButton.visible = available;
        this.itemLockButton.active = available;
        if (!available) {
            this.itemLockMode = false;
        }
        this.itemLockButton.setMode(this.itemLockMode);
        this.itemLockButton.setTooltip(Tooltip.create(Component.translatable(this.itemLockMode
                ? "tooltip.smartbackpacks.item_lock_button_on"
                : "tooltip.smartbackpacks.item_lock_button_off")));
    }

    private void toggleUpgrade(int upgradeSlot) {
        PacketDistributor.sendToServer(new ToggleUpgradeEnabledPayload(upgradeSlot));
    }

    private void restoreLastTrashedItem() {
        PacketDistributor.sendToServer(new TrashCanActionPayload(TrashCanActionPayload.Action.RESTORE_LAST, -1));
    }

    private void requestTrashDeleteNow() {
        if (!this.menu.hasTrashPendingItem()) {
            return;
        }

        if (this.menu.isTrashPendingProtected()) {
            this.openTrashConfirmScreen();
            return;
        }

        PacketDistributor.sendToServer(new TrashCanActionPayload(TrashCanActionPayload.Action.DELETE_NOW, -1));
    }

    private void openTrashConfirmScreen() {
        if (this.minecraft == null) {
            return;
        }

        ItemStack pending = this.menu.getTrashCanData().loadPendingItem();
        Component message = Component.translatable("screen.smartbackpacks.trash_can.confirm_message",
                pending.getCount(), pending.getHoverName());
        this.minecraft.gui.setScreen(new ConfirmScreen(confirmed -> {
            if (this.minecraft != null) {
                this.minecraft.gui.setScreen(this);
            }
            if (confirmed) {
                PacketDistributor.sendToServer(new TrashCanActionPayload(TrashCanActionPayload.Action.CONFIRM_DELETE, -1));
            }
        }, Component.translatable("screen.smartbackpacks.trash_can.confirm_title"), message,
                Component.translatable("screen.smartbackpacks.trash_can.confirm"),
                Component.translatable("screen.smartbackpacks.trash_can.cancel")));
    }

    private void renderSlotBackgrounds(GuiGraphics guiGraphics, int accent) {
        int backpackSlots = this.menu.getBackpackSlotCount();
        int upgradeStart = backpackSlots;
        int upgradeEnd = upgradeStart + this.menu.getUpgradeSlotCount();

        for (int index = 0; index < this.menu.slots.size(); index++) {
            Slot slot = this.menu.slots.get(index);
            int x = this.leftPos + slot.x - 1;
            int y = this.topPos + slot.y - 1;
            boolean active = slot.isActive();
            boolean accented = index >= upgradeStart && index < upgradeEnd && slot.hasItem();
            if (this.menu.isTrashSlotIndex(index)) {
                drawSlot(guiGraphics, x, y, this.menu.isTrashPendingProtected() ? TRASH_WARNING_FILL : TRASH_SLOT_FILL, active);
            } else {
                drawSlot(guiGraphics, x, y, accented ? accent : SLOT_FILL, active);
            }
        }
    }

    private void renderTrashPanel(GuiGraphics guiGraphics, int x, int y) {
        if (!this.menu.hasTrashCanUpgrade()) {
            return;
        }

        int panelX = x + BackpackScreenLayout.TRASH_PANEL_X;
        int panelY = y + BackpackScreenLayout.TRASH_PANEL_Y;
        drawInsetPanel(guiGraphics, panelX, panelY, BackpackScreenLayout.TRASH_PANEL_WIDTH, BackpackScreenLayout.TRASH_PANEL_HEIGHT, TRASH_PANEL_FILL);
        guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.trash_can.short_label"),
                panelX + 9, panelY + 6, TEXT_COLOR, false);

        Slot trashSlot = this.menu.slots.get(this.menu.getTrashSlotIndex());
        if (!trashSlot.hasItem()) {
            this.drawTrashGlyph(guiGraphics, x + trashSlot.x + 4, y + trashSlot.y + 3, 0xFF3D3127);
        }
    }

    private void renderTrashOverlay(GuiGraphics guiGraphics) {
        if (!this.menu.hasTrashCanUpgrade() || this.menu.getTrashSlotIndex() < 0) {
            return;
        }

        Slot trashSlot = this.menu.slots.get(this.menu.getTrashSlotIndex());
        if (!trashSlot.hasItem()) {
            return;
        }

        int slotX = this.leftPos + trashSlot.x;
        int slotY = this.topPos + trashSlot.y;
        if (this.menu.isTrashPendingProtected()) {
            guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0x66B31D16);
            guiGraphics.drawString(this.font, "!", slotX + 7, slotY + 5, 0xFFFFD0C8, false);
            return;
        }

        int delayTicks = Math.max(1, TrashCanUpgradeHandler.deletionDelayTicks());
        int remainingTicks = Math.max(0, this.menu.getTrashRemainingTicks());
        int progressWidth = Mth.clamp(Math.round((remainingTicks / (float) delayTicks) * 16), 0, 16);
        guiGraphics.fill(slotX + 1, slotY + 15, slotX + 17, slotY + 17, 0xAA2E241C);
        guiGraphics.fill(slotX + 1, slotY + 15, slotX + 1 + progressWidth, slotY + 17, TRASH_PROGRESS_FILL);
        int seconds = Math.max(0, (remainingTicks + 19) / 20);
        guiGraphics.drawString(this.font, Integer.toString(seconds), slotX + 2, slotY + 2, 0xFFFFFFFF, true);
    }

    private void renderItemLockOverlays(GuiGraphics guiGraphics) {
        if (!this.menu.hasItemLockUpgrade()) {
            return;
        }

        for (int index = 0; index < this.menu.getBackpackSlotCount(); index++) {
            Slot slot = this.menu.slots.get(index);
            if (!slot.isActive()) {
                continue;
            }

            boolean slotLocked = this.menu.isBackpackMenuSlotLocked(index);
            boolean itemLocked = slot.hasItem() && this.menu.isItemLocked(slot.getItem());
            if (!slotLocked && !itemLocked && !this.itemLockMode) {
                continue;
            }

            int slotX = this.leftPos + slot.x;
            int slotY = this.topPos + slot.y;
            if (this.itemLockMode) {
                guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 1, 0xFF55D46A);
                guiGraphics.fill(slotX, slotY + 17, slotX + 18, slotY + 18, 0xFF55D46A);
                guiGraphics.fill(slotX, slotY, slotX + 1, slotY + 18, 0xFF55D46A);
                guiGraphics.fill(slotX + 17, slotY, slotX + 18, slotY + 18, 0xFF55D46A);
            }
            if (slotLocked || itemLocked) {
                int color = slotLocked ? 0xFFFFD84A : 0xFFFF9F3D;
                guiGraphics.fill(slotX + 1, slotY + 1, slotX + 9, slotY + 9, 0xAA2E241C);
                drawLockGlyph(guiGraphics, slotX + 2, slotY + 2, color);
            }
        }
    }

    private void renderTitleIcon(GuiGraphics guiGraphics, int x, int y, int accent) {
        drawSlot(guiGraphics, x + TITLE_ICON_X - 2, y + TITLE_ICON_Y - 2, accent, true);
        guiGraphics.renderFakeItem(new ItemStack(this.getBackpackIconItem()), x + TITLE_ICON_X, y + TITLE_ICON_Y);
    }

    private void renderSearchField(GuiGraphics guiGraphics, int x, int y, int accent) {
        int fieldX = x + SEARCH_FIELD_X;
        int fieldY = y + SEARCH_FIELD_Y;
        int border = this.searchBox != null && this.searchBox.isFocused() ? accent : SEARCH_BORDER;
        guiGraphics.fill(fieldX, fieldY, fieldX + SEARCH_FIELD_WIDTH, fieldY + SEARCH_FIELD_HEIGHT, PANEL_SHADOW);
        guiGraphics.fill(fieldX + 1, fieldY + 1, fieldX + SEARCH_FIELD_WIDTH - 1, fieldY + SEARCH_FIELD_HEIGHT - 1, border);
        guiGraphics.fill(fieldX + 2, fieldY + 2, fieldX + SEARCH_FIELD_WIDTH - 2, fieldY + SEARCH_FIELD_HEIGHT - 2, SEARCH_FILL);
        drawSearchGlyph(guiGraphics, fieldX + 5, fieldY + 4, SEARCH_HINT_COLOR);
    }

    private void renderScrollBar(GuiGraphics guiGraphics) {
        if (!this.menu.canScroll()) {
            return;
        }

        int trackX = this.getScrollBarX();
        int trackY = this.getScrollBarY();
        int trackHeight = this.getScrollBarTrackHeight();
        int thumbHeight = this.getScrollBarThumbHeight();
        int thumbY = this.getScrollBarThumbY();

        guiGraphics.fill(trackX - 1, trackY - 1, trackX + SCROLLBAR_WIDTH + 1, trackY + trackHeight + 1, SCROLLBAR_BORDER_COLOR);
        guiGraphics.fill(trackX, trackY, trackX + SCROLLBAR_WIDTH, trackY + trackHeight, SCROLLBAR_TRACK_COLOR);
        guiGraphics.fill(trackX, thumbY, trackX + SCROLLBAR_WIDTH, thumbY + thumbHeight, SCROLLBAR_THUMB_COLOR);
    }

    private void renderCapacityIndicator(GuiGraphics guiGraphics, int x, int y) {
        if (!this.menu.hasCapacityWarningUpgrade()) {
            return;
        }

        CapacityWarningSnapshot snapshot = this.menu.getCapacityWarningSnapshot();
        CapacityWarningState state = snapshot.stateFor(this.menu.getCapacityWarningData());
        int gridBottom = BackpackScreenLayout.BACKPACK_SLOT_Y + this.menu.getVisibleBackpackRows() * BackpackScreenLayout.SLOT_SIZE;
        int barX = x + BackpackScreenLayout.BACKPACK_SLOT_X + 76;
        int barY = y + gridBottom + 7;
        int barWidth = 84;
        guiGraphics.fill(barX, barY, barX + barWidth, barY + 5, PANEL_DARK);
        guiGraphics.fill(barX + 1, barY + 1, barX + barWidth - 1, barY + 4, CAPACITY_BAR_BG);
        guiGraphics.fill(barX + 1, barY + 1, barX + 1 + Math.round((barWidth - 2) * snapshot.displayPercentage() / 100.0F),
                barY + 4, this.capacityColor(state));
    }

    private void renderCapacityLabel(GuiGraphics guiGraphics) {
        if (!this.menu.hasCapacityWarningUpgrade()) {
            return;
        }

        CapacityWarningSnapshot snapshot = this.menu.getCapacityWarningSnapshot();
        int y = BackpackScreenLayout.BACKPACK_SLOT_Y + this.menu.getVisibleBackpackRows() * BackpackScreenLayout.SLOT_SIZE + 4;
        guiGraphics.drawString(this.font,
                Component.translatable("screen.smartbackpacks.capacity_warning.compact", snapshot.displayPercentage()),
                BackpackScreenLayout.BACKPACK_SLOT_X, y, MUTED_TEXT_COLOR, false);
    }

    private boolean isMouseOverBackpackArea(double mouseX, double mouseY) {
        int backpackX = this.leftPos + BackpackScreenLayout.BACKPACK_SLOT_X;
        int backpackY = this.topPos + BackpackScreenLayout.BACKPACK_SLOT_Y;
        int backpackWidth = BackpackScreenLayout.BACKPACK_COLUMNS * BackpackScreenLayout.SLOT_SIZE;
        int backpackHeight = this.menu.getVisibleBackpackRows() * BackpackScreenLayout.SLOT_SIZE;
        return mouseX >= backpackX
                && mouseX < backpackX + backpackWidth
                && mouseY >= backpackY
                && mouseY < backpackY + backpackHeight;
    }

    private boolean isMouseOverSearchField(double mouseX, double mouseY) {
        int fieldX = this.leftPos + SEARCH_FIELD_X;
        int fieldY = this.topPos + SEARCH_FIELD_Y;
        return mouseX >= fieldX
                && mouseX < fieldX + SEARCH_FIELD_WIDTH
                && mouseY >= fieldY
                && mouseY < fieldY + SEARCH_FIELD_HEIGHT;
    }

    private boolean isTrashShortcutDown() {
        if (this.minecraft == null) {
            return false;
        }
        return (InputConstants.isKeyDown(InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(InputConstants.KEY_RSHIFT))
                && InputConstants.isKeyDown(GLFW.GLFW_KEY_DELETE);
    }

    private boolean isMouseOverScrollBar(double mouseX, double mouseY) {
        if (!this.menu.canScroll()) {
            return false;
        }

        int trackX = this.getScrollBarX();
        int trackY = this.getScrollBarY();
        int trackHeight = this.getScrollBarTrackHeight();
        return mouseX >= trackX
                && mouseX < trackX + SCROLLBAR_WIDTH
                && mouseY >= trackY
                && mouseY < trackY + trackHeight;
    }

    private void scrollTo(double mouseY) {
        int maxOffset = this.menu.getMaxScrollRowOffset();
        if (maxOffset <= 0) {
            this.menu.setFirstVisibleRow(0);
            this.syncScrollOffset();
            return;
        }

        int trackY = this.getScrollBarY();
        int trackHeight = this.getScrollBarTrackHeight();
        int thumbHeight = this.getScrollBarThumbHeight();
        int maxThumbTravel = Math.max(1, trackHeight - thumbHeight);
        double thumbCenterOffset = thumbHeight / 2.0D;
        double relative = Mth.clamp(mouseY - trackY - thumbCenterOffset, 0.0D, maxThumbTravel);
        int rowOffset = Mth.floor((relative / maxThumbTravel) * maxOffset + 0.5D);
        this.menu.setFirstVisibleRow(rowOffset);
        this.syncScrollOffset();
        this.applySearchFilter();
    }

    private int getScrollBarX() {
        return this.leftPos + BackpackScreenLayout.BACKPACK_SLOT_X
                + BackpackScreenLayout.BACKPACK_COLUMNS * BackpackScreenLayout.SLOT_SIZE + 8;
    }

    private int getScrollBarY() {
        return this.topPos + BackpackScreenLayout.BACKPACK_SLOT_Y;
    }

    private int getScrollBarTrackHeight() {
        return this.menu.getVisibleBackpackRows() * BackpackScreenLayout.SLOT_SIZE;
    }

    private int getScrollBarThumbHeight() {
        int totalRows = this.menu.getTotalBackpackRows();
        int visibleRows = this.menu.getVisibleBackpackRows();
        int trackHeight = this.getScrollBarTrackHeight();
        if (totalRows <= 0) {
            return trackHeight;
        }

        return Math.max(SCROLLBAR_MIN_HEIGHT, Math.round((visibleRows / (float) totalRows) * trackHeight));
    }

    private int getScrollBarThumbY() {
        int trackY = this.getScrollBarY();
        int trackHeight = this.getScrollBarTrackHeight();
        int thumbHeight = this.getScrollBarThumbHeight();
        int maxTravel = Math.max(0, trackHeight - thumbHeight);
        return trackY + Math.round(this.menu.getScrollProgress() * maxTravel);
    }

    private void syncScrollOffset() {
        PacketDistributor.sendToServer(new SetBackpackScrollOffsetPayload(this.menu.getFirstVisibleRow()));
    }

    private void renderCompactBackpackCounts(GuiGraphics guiGraphics) {
        int backpackSlots = this.menu.getBackpackSlotCount();
        for (int index = 0; index < backpackSlots; index++) {
            Slot slot = this.menu.slots.get(index);
            if (!slot.isActive() || !slot.hasItem()) {
                continue;
            }

            int count = slot.getItem().getCount();
            if (count < 1000) {
                continue;
            }

            String label = this.formatCompactCount(count);
            int slotX = this.leftPos + slot.x;
            int slotY = this.topPos + slot.y;
            int textWidth = this.font.width(label);
            int textX = slotX + 17 - textWidth;
            int textY = slotY + 9;
            guiGraphics.fill(textX - 1, textY - 1, slotX + 17, slotY + 17, STACK_TEXT_BACKGROUND);
            guiGraphics.drawString(this.font, label, textX + 1, textY + 1, 0x3F3F3F, false);
            guiGraphics.drawString(this.font, label, textX, textY, 0xFFFFFF, false);
        }
    }

    private String formatCompactCount(int count) {
        if (count < 10_000) {
            return STACK_COUNT_FORMAT.format(count);
        }
        if (count >= 1_000_000_000) {
            return this.formatWithSuffix(count, 1_000_000_000, "b");
        }
        if (count >= 1_000_000) {
            return this.formatWithSuffix(count, 1_000_000, "m");
        }
        return this.formatWithSuffix(count, 1_000, "k");
    }

    private String formatWithSuffix(int count, int divisor, String suffix) {
        int whole = count / divisor;
        int tenth = (count % divisor) * 10 / divisor;
        if (whole >= 100) {
            return whole + suffix;
        }
        if (whole >= 10 && tenth == 0) {
            return whole + suffix;
        }
        return whole + "." + tenth + suffix;
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack itemStack) {
        List<Component> tooltip = new ArrayList<>(super.getTooltipFromContainerItem(itemStack));
        if (this.hoveredSlot != null
                && this.hoveredSlot.index < this.menu.getBackpackSlotCount()
                && itemStack.getCount() >= 1000) {
            tooltip.add(Component.literal("Count: " + STACK_COUNT_FORMAT.format(itemStack.getCount())));
        }
        if (this.hoveredSlot != null && this.menu.isTrashSlotIndex(this.hoveredSlot.index)) {
            if (this.menu.isTrashPendingProtected()) {
                tooltip.add(Component.translatable("tooltip.smartbackpacks.trash_can_protected").withStyle(net.minecraft.ChatFormatting.RED));
                tooltip.add(Component.translatable("tooltip.smartbackpacks.trash_can_confirm_needed").withStyle(net.minecraft.ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.translatable("tooltip.smartbackpacks.trash_can_countdown",
                        Math.max(0, (this.menu.getTrashRemainingTicks() + 19) / 20)).withStyle(net.minecraft.ChatFormatting.GRAY));
            }
            tooltip.add(Component.translatable("tooltip.smartbackpacks.trash_can_remove_pending").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        }
        if (this.hoveredSlot != null && this.hoveredSlot.index < this.menu.getBackpackSlotCount()) {
            boolean slotLocked = this.menu.isBackpackMenuSlotLocked(this.hoveredSlot.index);
            boolean itemLocked = this.menu.isItemLocked(itemStack);
            if (slotLocked) {
                tooltip.add(Component.translatable("tooltip.smartbackpacks.item_lock_locked_slot").withStyle(net.minecraft.ChatFormatting.GOLD));
            }
            if (itemLocked) {
                tooltip.add(Component.translatable("tooltip.smartbackpacks.item_lock_locked_item").withStyle(net.minecraft.ChatFormatting.GOLD));
            }
            if (slotLocked || itemLocked) {
                tooltip.add(Component.translatable("tooltip.smartbackpacks.item_lock_blocked").withStyle(net.minecraft.ChatFormatting.GRAY));
            }
        }
        return tooltip;
    }

    private int getTierAccentColor() {
        return switch (this.menu.getTier()) {
            case LEATHER -> 0xFF7B4F32;
            case COAL -> 0xFF4D4B43;
            case LAPIS -> 0xFF415AA8;
            case REDSTONE -> 0xFFB64537;
            case QUARTZ -> 0xFFE5DED0;
            case COPPER -> 0xFFC8753C;
            case IRON -> 0xFFC7C7C7;
            case GOLD -> 0xFFE5B948;
            case EMERALD -> 0xFF54B76A;
            case DIAMOND -> 0xFF58C9D4;
            case NETHERITE -> 0xFF59435B;
            case ANCIENT_NETHERITE -> 0xFF3B2D3D;
            case ULTIMATE_DIAMOND -> 0xFF35D6E6;
            case NETHERITE_VAULT -> 0xFF24182A;
        };
    }

    private int capacityColor(CapacityWarningState state) {
        return switch (state) {
            case FULL -> 0xFFE34A38;
            case CRITICAL -> 0xFFE8892F;
            case WARNING -> 0xFFE2C44D;
            case NORMAL -> this.getTierAccentColor();
        };
    }

    private Item getBackpackIconItem() {
        BackpackTier tier = this.menu.getTier();
        return switch (tier) {
            case LEATHER -> ModItems.LEATHER_BACKPACK.get();
            case COAL -> ModItems.COAL_BACKPACK.get();
            case LAPIS -> ModItems.LAPIS_BACKPACK.get();
            case REDSTONE -> ModItems.REDSTONE_BACKPACK.get();
            case QUARTZ -> ModItems.QUARTZ_BACKPACK.get();
            case COPPER -> ModItems.COPPER_BACKPACK.get();
            case IRON -> ModItems.IRON_BACKPACK.get();
            case GOLD -> ModItems.GOLD_BACKPACK.get();
            case EMERALD -> ModItems.EMERALD_BACKPACK.get();
            case DIAMOND -> ModItems.DIAMOND_BACKPACK.get();
            case NETHERITE -> ModItems.NETHERITE_BACKPACK.get();
            case ANCIENT_NETHERITE -> ModItems.ANCIENT_NETHERITE_BACKPACK.get();
            case ULTIMATE_DIAMOND -> ModItems.ULTIMATE_DIAMOND_BACKPACK.get();
            case NETHERITE_VAULT -> ModItems.NETHERITE_VAULT_BACKPACK.get();
        };
    }

    private static void drawPanel(GuiGraphics guiGraphics, int x, int y, int width, int height, int fill) {
        guiGraphics.fill(x + 2, y + 2, x + width + 2, y + height + 2, PANEL_SHADOW);
        guiGraphics.fill(x, y, x + width, y + height, PANEL_BORDER);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, fill);
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + 3, PANEL_LIGHT);
        guiGraphics.fill(x + 2, y + 2, x + 3, y + height - 2, PANEL_LIGHT);
        guiGraphics.fill(x + 2, y + height - 3, x + width - 2, y + height - 2, PANEL_DARK);
        guiGraphics.fill(x + width - 3, y + 2, x + width - 2, y + height - 2, PANEL_DARK);
    }

    private static void drawInsetPanel(GuiGraphics guiGraphics, int x, int y, int width, int height, int fill) {
        guiGraphics.fill(x, y, x + width, y + height, PANEL_DARK);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_LIGHT);
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, fill);
    }

    private static void drawSlot(GuiGraphics guiGraphics, int x, int y, int border, boolean active) {
        int inner = active ? SLOT_INNER : SLOT_DISABLED;
        guiGraphics.fill(x, y, x + BackpackScreenLayout.SLOT_SIZE, y + BackpackScreenLayout.SLOT_SIZE, PANEL_BORDER);
        guiGraphics.fill(x + 1, y + 1, x + BackpackScreenLayout.SLOT_SIZE - 1, y + BackpackScreenLayout.SLOT_SIZE - 1, border);
        guiGraphics.fill(x + 2, y + 2, x + BackpackScreenLayout.SLOT_SIZE - 2, y + BackpackScreenLayout.SLOT_SIZE - 2, inner);
        guiGraphics.fill(x + 2, y + 2, x + BackpackScreenLayout.SLOT_SIZE - 2, y + 3, SLOT_HOVER);
    }

    private static void drawSearchGlyph(GuiGraphics guiGraphics, int x, int y, int color) {
        guiGraphics.fill(x + 2, y, x + 7, y + 1, color);
        guiGraphics.fill(x + 1, y + 1, x + 2, y + 5, color);
        guiGraphics.fill(x + 7, y + 1, x + 8, y + 5, color);
        guiGraphics.fill(x + 2, y + 5, x + 7, y + 6, color);
        guiGraphics.fill(x + 6, y + 6, x + 8, y + 8, color);
        guiGraphics.fill(x + 8, y + 8, x + 10, y + 10, color);
    }

    private void drawTrashGlyph(GuiGraphics guiGraphics, int x, int y, int color) {
        guiGraphics.fill(x + 2, y, x + 10, y + 1, color);
        guiGraphics.fill(x + 4, y - 1, x + 8, y, color);
        guiGraphics.fill(x + 1, y + 2, x + 11, y + 3, color);
        guiGraphics.fill(x + 2, y + 3, x + 10, y + 12, color);
        guiGraphics.fill(x + 4, y + 4, x + 5, y + 11, 0xFFD9C8B1);
        guiGraphics.fill(x + 7, y + 4, x + 8, y + 11, 0xFFD9C8B1);
    }

    private static void drawSortGlyph(GuiGraphics guiGraphics, int x, int y, int color) {
        guiGraphics.fill(x + 3, y + 3, x + 11, y + 4, color);
        guiGraphics.fill(x + 3, y + 6, x + 9, y + 7, color);
        guiGraphics.fill(x + 3, y + 9, x + 7, y + 10, color);
        guiGraphics.fill(x + 12, y + 3, x + 13, y + 10, color);
        guiGraphics.fill(x + 10, y + 8, x + 15, y + 9, color);
        guiGraphics.fill(x + 11, y + 9, x + 14, y + 10, color);
        guiGraphics.fill(x + 12, y + 10, x + 13, y + 11, color);
    }

    private static void drawLockGlyph(GuiGraphics guiGraphics, int x, int y, int color) {
        guiGraphics.fill(x + 2, y, x + 5, y + 1, color);
        guiGraphics.fill(x + 1, y + 1, x + 2, y + 4, color);
        guiGraphics.fill(x + 5, y + 1, x + 6, y + 4, color);
        guiGraphics.fill(x, y + 4, x + 7, y + 8, color);
        guiGraphics.fill(x + 3, y + 5, x + 4, y + 7, 0xFF2E241C);
    }

    private static void drawPixelButton(GuiGraphics guiGraphics, int x, int y, int width, int height, boolean active, boolean hovered) {
        int fill = !active ? BUTTON_DISABLED_FILL : hovered ? BUTTON_HOVER_FILL : BUTTON_FILL;
        guiGraphics.fill(x, y, x + width, y + height, BUTTON_BORDER);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, fill);
        if (active) {
            guiGraphics.fill(x + 1, y + 1, x + width - 1, y + 2, BUTTON_LIGHT);
            guiGraphics.fill(x + 1, y + 1, x + 2, y + height - 1, BUTTON_LIGHT);
        }
    }

    private static final class SortIconButton extends Button {
        private SortIconButton(int x, int y, Component message, OnPress onPress, Tooltip tooltip) {
            super(x, y, TOP_BUTTON_SIZE, TOP_BUTTON_SIZE, message, onPress, DEFAULT_NARRATION);
            this.setTooltip(tooltip);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
            GuiGraphics guiGraphics = new GuiGraphics(extractor);
            drawPixelButton(guiGraphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.active, this.isHoveredOrFocused());
            drawSortGlyph(guiGraphics, this.getX() + 1, this.getY() + 1, this.active ? SEARCH_TEXT_COLOR : MUTED_TEXT_COLOR);
        }
    }

    private static final class LockModeButton extends Button {
        private boolean mode;

        private LockModeButton(int x, int y, OnPress onPress) {
            super(x, y, TOP_BUTTON_SIZE, TOP_BUTTON_SIZE, Component.literal("L"), onPress, DEFAULT_NARRATION);
        }

        private void setMode(boolean mode) {
            this.mode = mode;
            this.setMessage(Component.literal(mode ? "L*" : "L"));
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
            GuiGraphics guiGraphics = new GuiGraphics(extractor);
            drawPixelButton(guiGraphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(),
                    this.active, this.isHoveredOrFocused() || this.mode);
            drawLockGlyph(guiGraphics, this.getX() + 5, this.getY() + 4, this.mode ? 0xFFFFD84A : SEARCH_TEXT_COLOR);
        }
    }

    private static final class UpgradeToggleButton extends Button {
        private boolean enabledState;

        private UpgradeToggleButton(int x, int y, OnPress onPress) {
            super(x, y, BackpackScreenLayout.UPGRADE_TOGGLE_SIZE, BackpackScreenLayout.UPGRADE_TOGGLE_SIZE,
                    Component.empty(), onPress, DEFAULT_NARRATION);
        }

        private void setEnabledState(boolean enabledState) {
            this.enabledState = enabledState;
            this.setMessage(Component.translatable(enabledState
                    ? "tooltip.smartbackpacks.upgrade_enabled"
                    : "tooltip.smartbackpacks.upgrade_disabled"));
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
            GuiGraphics guiGraphics = new GuiGraphics(extractor);
            drawPixelButton(guiGraphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.active, this.isHoveredOrFocused());
            int color = this.enabledState ? 0xFF55D46A : 0xFFD95D4F;
            guiGraphics.fill(this.getX() + 3, this.getY() + 2, this.getX() + 5, this.getY() + 6, color);
            guiGraphics.fill(this.getX() + 2, this.getY() + 3, this.getX() + 6, this.getY() + 5, color);
        }
    }
}
