package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.teamsmartstreamlabs.smartbackpacks.client.screen.SmartBackpacksConfigEntries.Category;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.SmartBackpacksConfigEntries.ConfigEntry;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.SmartBackpacksConfigEntries.Kind;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.SmartBackpacksConfigEntries.Scope;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;

import com.teamsmartstreamlabs.smartbackpacks.client.PickupNotifierHud;
import com.teamsmartstreamlabs.smartbackpacks.client.gui.GuiGraphics;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class SmartBackpacksConfigScreen extends Screen {
    private static final int PANEL_BORDER = 0xFF4A3324;
    private static final int PANEL_FILL = 0xFFD9C29A;
    private static final int PANEL_LIGHT = 0xFFF0DDB7;
    private static final int PANEL_DARK = 0xFF8A6645;
    private static final int SIDEBAR_FILL = 0xFF6A4C35;
    private static final int SIDEBAR_CARD = 0xFF7B5A3E;
    private static final int CARD_FILL = 0xFFC7A57D;
    private static final int CARD_HOVER = 0xFFD5B78C;
    private static final int TEAL = 0xFF2A9D94;
    private static final int TEAL_DARK = 0xFF176C68;
    private static final int GOLD = 0xFFD5A84B;
    private static final int TEXT = 0xFF332419;
    private static final int MUTED_TEXT = 0xFF70563E;
    private static final int LIGHT_TEXT = 0xFFF4E7CC;
    private static final int ERROR_TEXT = 0xFF9B3028;
    private static final int INPUT_TEXT = 0xE0E0E0;
    private static final int INPUT_ERROR_TEXT = 0xE05048;
    private static final int ROW_HEIGHT = 50;
    private static final int HEADER_HEIGHT = 50;
    private static final int FOOTER_HEIGHT = 34;

    private static final List<String> UPGRADE_SECTIONS = List.of(
            "pickup", "magnet", "advanced_magnet", "quiver", "trash", "rescue",
            "builder", "torch", "capacity", "locks", "link");
    private static final List<String> MOB_SECTIONS = List.of(
            "mob_general", "mob_eligible", "mob_advanced", "mob_tiers");

    private final Screen parent;
    private final List<ConfigEntry> entries = SmartBackpacksConfigEntries.create();
    private final Map<String, Object> baseline = new LinkedHashMap<>();
    private final Map<String, Object> staged = new LinkedHashMap<>();
    private final Map<Category, String> selectedSections = new EnumMap<>(Category.class);
    private final Set<String> invalidEntries = new HashSet<>();

    private Category selectedCategory = Category.GENERAL;
    private String searchQuery = "";
    private int scrollOffset;
    private int leftPos;
    private int topPos;
    private int imageWidth;
    private int imageHeight;
    private int sidebarWidth;
    private EditBox searchBox;
    private Button applyButton;
    private Button doneButton;
    private ConfigEntry hoveredEntry;
    private ConfirmAction confirmAction = ConfirmAction.NONE;
    private boolean rebuildPending;
    private boolean refocusSearch;
    private long statusUntil;
    private Component statusMessage = Component.empty();
    private int statusColor = TEAL_DARK;

    public SmartBackpacksConfigScreen(Screen parent) {
        super(Component.translatable("config.smartbackpacks.title"));
        this.parent = parent;
        this.selectedSections.put(Category.UPGRADES, UPGRADE_SECTIONS.get(0));
        this.selectedSections.put(Category.MOB_BACKPACKS, MOB_SECTIONS.get(0));
    }

    @Override
    protected void init() {
        this.ensureSnapshot();
        this.imageWidth = Math.min(720, Math.max(304, this.width - 16));
        this.imageHeight = Math.min(430, Math.max(214, this.height - 16));
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.sidebarWidth = this.imageWidth < 440 ? 94 : 118;
        this.scrollOffset = this.clampedScroll(this.scrollOffset);

        if (this.confirmAction != ConfirmAction.NONE) {
            this.addConfirmationButtons();
            return;
        }

        this.addSearchBox();
        this.addCategoryButtons();
        this.addSectionButton();
        this.addSettingControls();
        this.addFooterButtons();
    }

    private void ensureSnapshot() {
        if (!this.baseline.isEmpty()) {
            return;
        }
        for (ConfigEntry entry : this.entries) {
            Object value = entry.read();
            this.baseline.put(entry.id(), value);
            this.staged.put(entry.id(), value);
        }
    }

    private void addSearchBox() {
        int width = Math.min(228, Math.max(126, this.imageWidth - this.sidebarWidth - 160));
        this.searchBox = this.addRenderableWidget(new EditBox(this.font,
                this.leftPos + this.imageWidth - width - 12, this.topPos + 14, width, 20,
                Component.translatable("config.smartbackpacks.search")));
        this.searchBox.setMaxLength(64);
        this.searchBox.setHint(Component.translatable("config.smartbackpacks.search"));
        this.searchBox.setValue(this.searchQuery);
        this.searchBox.setResponder(value -> {
            this.searchQuery = value;
            this.scrollOffset = 0;
            this.refocusSearch = true;
            this.rebuildPending = true;
        });
        if (this.refocusSearch) {
            this.setFocused(this.searchBox);
            this.searchBox.setFocused(true);
            this.searchBox.moveCursorToEnd(false);
            this.refocusSearch = false;
        }
    }

    private void addCategoryButtons() {
        int x = this.leftPos + 8;
        int y = this.topPos + HEADER_HEIGHT + 8;
        int width = this.sidebarWidth - 16;
        for (Category category : Category.values()) {
            Button button = Button.builder(category.title(), clicked -> {
                this.selectedCategory = category;
                this.searchQuery = "";
                this.scrollOffset = 0;
                this.scheduleRebuild(false);
            }).bounds(x, y, width, 20).tooltip(Tooltip.create(category.description())).build();
            button.active = category != this.selectedCategory || !this.searchQuery.isBlank();
            this.addRenderableWidget(button);
            y += 23;
        }
    }

    private void addSectionButton() {
        List<String> sections = this.sectionsFor(this.selectedCategory);
        if (!this.searchQuery.isBlank() || sections.size() < 2) {
            return;
        }
        String section = this.selectedSections.get(this.selectedCategory);
        Component label = Component.translatable("config.smartbackpacks.section_selector",
                Component.translatable("config.smartbackpacks.section." + section));
        int width = Math.min(220, this.contentWidth());
        this.addRenderableWidget(Button.builder(label, clicked -> this.cycleSection(1))
                .bounds(this.contentX(), this.topPos + HEADER_HEIGHT + 21, width, 20)
                .tooltip(Tooltip.create(Component.translatable("config.smartbackpacks.section_selector.desc")))
                .build());
    }

    private void cycleSection(int direction) {
        List<String> sections = this.sectionsFor(this.selectedCategory);
        String current = this.selectedSections.get(this.selectedCategory);
        int index = Math.max(0, sections.indexOf(current));
        this.selectedSections.put(this.selectedCategory,
                sections.get(Math.floorMod(index + direction, sections.size())));
        this.scrollOffset = 0;
        this.scheduleRebuild(false);
    }

    private void addSettingControls() {
        List<ConfigEntry> visible = this.filteredEntries();
        int count = Math.min(this.maxVisibleRows(), Math.max(0, visible.size() - this.scrollOffset));
        for (int i = 0; i < count; i++) {
            ConfigEntry entry = visible.get(this.scrollOffset + i);
            int rowY = this.rowsTop() + i * ROW_HEIGHT;
            this.addEntryControl(entry, rowY);
        }
    }

    private void addEntryControl(ConfigEntry entry, int rowY) {
        int right = this.contentX() + this.contentWidth() - 27;
        boolean editable = this.isEditable(entry);
        Object value = this.staged.get(entry.id());
        Component tooltip = entry.tooltip(!editable);

        if (!editable && entry.scope() == Scope.SERVER) {
            Button controlled = Button.builder(Component.translatable("config.smartbackpacks.server_controlled_short"),
                    clicked -> {
                    }).bounds(right - 136, rowY + 15, 136, 20).tooltip(Tooltip.create(tooltip)).build();
            controlled.active = false;
            this.addRenderableWidget(controlled);
            return;
        }

        if (entry.kind() == Kind.BOOLEAN) {
            Button button = Button.builder(entry.valueText(value), clicked -> {
                this.stage(entry, !((Boolean) this.staged.get(entry.id())));
                this.scheduleRebuild(false);
            }).bounds(right - 76, rowY + 15, 76, 20).tooltip(Tooltip.create(tooltip)).build();
            button.active = editable;
            this.addRenderableWidget(button);
        } else if (entry.kind() == Kind.INTEGER || entry.kind() == Kind.PERCENT
                || entry.kind() == Kind.PERCENT_OVERRIDE) {
            Button minus = Button.builder(Component.literal("-"), clicked -> {
                this.stage(entry, entry.adjusted(this.staged.get(entry.id()), -1, false));
                this.scheduleRebuild(false);
            }).bounds(right - 136, rowY + 15, 22, 20).tooltip(Tooltip.create(tooltip)).build();
            minus.active = editable;
            this.addRenderableWidget(minus);

            EditBox field = new EditBox(this.font, right - 110, rowY + 15, 82, 20, entry.title());
            field.setMaxLength(12);
            field.setValue(entry.editText(value));
            if (entry.kind() == Kind.PERCENT_OVERRIDE) {
                field.setHint(Component.translatable("config.smartbackpacks.value.use_global"));
            }
            field.setResponder(text -> {
                Object parsed = entry.parse(text);
                if (parsed != null && this.isEditable(entry)) {
                    this.stage(entry, parsed);
                    this.invalidEntries.remove(entry.id());
                    field.setTextColor(INPUT_TEXT);
                } else if (this.isEditable(entry)) {
                    this.invalidEntries.add(entry.id());
                    field.setTextColor(INPUT_ERROR_TEXT);
                }
                this.updateActionButtons();
            });
            field.setTooltip(Tooltip.create(tooltip));
            field.setEditable(editable);
            this.addRenderableWidget(field);

            Button plus = Button.builder(Component.literal("+"), clicked -> {
                this.stage(entry, entry.adjusted(this.staged.get(entry.id()), 1, false));
                this.scheduleRebuild(false);
            }).bounds(right - 24, rowY + 15, 24, 20).tooltip(Tooltip.create(tooltip)).build();
            plus.active = editable;
            this.addRenderableWidget(plus);
        } else {
            Button button = Button.builder(entry.valueText(value), clicked -> {
                this.stage(entry, entry.next(this.staged.get(entry.id()), 1));
                this.scheduleRebuild(false);
            }).bounds(right - 136, rowY + 15, 136, 20).tooltip(Tooltip.create(tooltip)).build();
            button.active = editable;
            this.addRenderableWidget(button);
        }

        if (editable && !Objects.equals(value, entry.defaultValue())) {
            this.addRenderableWidget(Button.builder(Component.translatable("config.smartbackpacks.reset_short"), clicked -> {
                this.stage(entry, entry.defaultValue());
                this.scheduleRebuild(false);
            }).bounds(this.contentX() + this.contentWidth() - 20, rowY + 17, 16, 16)
                    .tooltip(Tooltip.create(Component.translatable("config.smartbackpacks.reset_setting",
                            entry.valueText(entry.defaultValue()))))
                    .build());
        }
    }

    private void addFooterButtons() {
        int y = this.topPos + this.imageHeight - 27;
        int resetCategoryWidth = this.imageWidth < 440 ? 72 : 94;
        int resetAllWidth = this.imageWidth < 440 ? 56 : 70;
        int actionWidth = this.imageWidth < 440 ? 50 : 60;

        this.addRenderableWidget(Button.builder(Component.translatable("config.smartbackpacks.reset_category"),
                clicked -> this.openConfirmation(ConfirmAction.RESET_CATEGORY))
                .bounds(this.leftPos + 8, y, resetCategoryWidth, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("config.smartbackpacks.reset_all"),
                clicked -> this.openConfirmation(ConfirmAction.RESET_ALL))
                .bounds(this.leftPos + 12 + resetCategoryWidth, y, resetAllWidth, 20).build());

        int doneX = this.leftPos + this.imageWidth - 8 - actionWidth;
        this.doneButton = Button.builder(Component.translatable("gui.done"), clicked -> {
            if (this.applyChanges()) {
                this.closeParent();
            }
        }).bounds(doneX, y, actionWidth, 20).build();
        this.addRenderableWidget(this.doneButton);

        this.applyButton = Button.builder(Component.translatable("config.smartbackpacks.apply"),
                clicked -> this.applyChanges())
                .bounds(doneX - actionWidth - 4, y, actionWidth, 20).build();
        this.addRenderableWidget(this.applyButton);
        this.updateActionButtons();

        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), clicked -> this.requestClose())
                .bounds(doneX - (actionWidth + 4) * 2, y, actionWidth, 20).build());
    }

    private void addConfirmationButtons() {
        int y = this.topPos + this.imageHeight / 2 + 34;
        int center = this.leftPos + this.imageWidth / 2;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), clicked -> {
            this.confirmAction = ConfirmAction.NONE;
            this.scheduleRebuild(false);
        }).bounds(center - 104, y, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("config.smartbackpacks.confirm"),
                clicked -> this.confirmPendingAction())
                .bounds(center + 4, y, 100, 20).build());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.rebuildPending) {
            this.rebuildPending = false;
            this.rebuildWidgets();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        GuiGraphics graphics = new GuiGraphics(extractor);
        this.extractTransparentBackground(extractor);
        this.hoveredEntry = null;
        this.drawPanel(graphics);
        this.drawHeader(graphics);
        this.drawSidebar(graphics);
        this.drawContent(graphics, mouseX, mouseY);
        this.drawFooterStatus(graphics);

        super.extractRenderState(extractor, mouseX, mouseY, partialTick);

        if (this.confirmAction != ConfirmAction.NONE) {
            this.drawConfirmation(graphics);
        }
        if (this.hoveredEntry != null && this.confirmAction == ConfirmAction.NONE) {
            graphics.setTooltipForNextFrame(this.font,
                    this.hoveredEntry.tooltip(!this.isEditable(this.hoveredEntry)), mouseX, mouseY);
        }
        PickupNotifierHud.render(graphics);
    }

    private void drawPanel(GuiGraphics graphics) {
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, PANEL_BORDER);
        graphics.fill(this.leftPos + 2, this.topPos + 2, this.leftPos + this.imageWidth - 2,
                this.topPos + this.imageHeight - 2, PANEL_FILL);
        graphics.fill(this.leftPos + 4, this.topPos + 4, this.leftPos + this.imageWidth - 4, this.topPos + 6, PANEL_LIGHT);
        graphics.fill(this.leftPos + 4, this.topPos + this.imageHeight - 6,
                this.leftPos + this.imageWidth - 4, this.topPos + this.imageHeight - 4, PANEL_DARK);
        graphics.fill(this.leftPos + this.sidebarWidth, this.topPos + HEADER_HEIGHT,
                this.leftPos + this.sidebarWidth + 2, this.topPos + this.imageHeight - FOOTER_HEIGHT, PANEL_DARK);
        graphics.fill(this.leftPos + 2, this.topPos + HEADER_HEIGHT - 1,
                this.leftPos + this.imageWidth - 2, this.topPos + HEADER_HEIGHT + 1, PANEL_DARK);
    }

    private void drawHeader(GuiGraphics graphics) {
        ItemStack icon = this.configIcon();
        if (!icon.isEmpty()) {
            graphics.renderItem(icon, this.leftPos + 12, this.topPos + 15);
        }
        graphics.drawString(this.font, this.title, this.leftPos + 34, this.topPos + 13, TEXT, false);
        graphics.drawString(this.font, Component.translatable("config.smartbackpacks.subtitle"),
                this.leftPos + 34, this.topPos + 27, MUTED_TEXT, false);
    }

    private void drawSidebar(GuiGraphics graphics) {
        int x = this.leftPos + 2;
        int y = this.topPos + HEADER_HEIGHT + 1;
        graphics.fill(x, y, this.leftPos + this.sidebarWidth, this.topPos + this.imageHeight - FOOTER_HEIGHT, SIDEBAR_FILL);
        int selectedY = this.topPos + HEADER_HEIGHT + 8 + this.selectedCategory.ordinal() * 23;
        if (this.searchQuery.isBlank()) {
            graphics.fill(this.leftPos + 6, selectedY - 1, this.leftPos + this.sidebarWidth - 5,
                    selectedY + 21, SIDEBAR_CARD);
            graphics.fill(this.leftPos + 6, selectedY - 1, this.leftPos + 9, selectedY + 21, TEAL);
        }
    }

    private void drawContent(GuiGraphics graphics, int mouseX, int mouseY) {
        Component heading = this.searchQuery.isBlank()
                ? this.selectedCategory.title()
                : Component.translatable("config.smartbackpacks.search_results");
        Component description = this.searchQuery.isBlank()
                ? this.selectedCategory.description()
                : Component.translatable("config.smartbackpacks.search_results.count", this.filteredEntries().size());
        graphics.drawString(this.font, heading, this.contentX(), this.topPos + HEADER_HEIGHT + 7, TEXT, false);
        graphics.drawString(this.font, this.clipped(description, this.contentWidth()),
                this.contentX(), this.topPos + HEADER_HEIGHT + 18, MUTED_TEXT, false);

        List<ConfigEntry> visible = this.filteredEntries();
        int count = Math.min(this.maxVisibleRows(), Math.max(0, visible.size() - this.scrollOffset));
        if (visible.isEmpty()) {
            graphics.drawCenteredString(this.font, Component.translatable("config.smartbackpacks.no_results"),
                    this.contentX() + this.contentWidth() / 2, this.rowsTop() + 22, MUTED_TEXT);
            return;
        }

        for (int i = 0; i < count; i++) {
            ConfigEntry entry = visible.get(this.scrollOffset + i);
            int rowY = this.rowsTop() + i * ROW_HEIGHT;
            boolean hovered = mouseX >= this.contentX() && mouseX < this.contentX() + this.contentWidth()
                    && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT - 3;
            graphics.fill(this.contentX(), rowY, this.contentX() + this.contentWidth(),
                    rowY + ROW_HEIGHT - 3, hovered ? CARD_HOVER : CARD_FILL);
            if (this.isChanged(entry)) {
                graphics.fill(this.contentX(), rowY, this.contentX() + 3, rowY + ROW_HEIGHT - 3, TEAL);
            }

            int controlWidth = 174;
            int textWidth = Math.max(64, this.contentWidth() - controlWidth);
            graphics.drawString(this.font, this.clipped(entry.title(), textWidth - 10),
                    this.contentX() + 8, rowY + 6, TEXT, false);
            graphics.drawString(this.font, this.clipped(entry.description(), textWidth - 10),
                    this.contentX() + 8, rowY + 19, MUTED_TEXT, false);
            Component breadcrumb = Component.translatable("config.smartbackpacks.breadcrumb",
                    entry.category().title(), entry.sectionTitle());
            graphics.drawString(this.font, this.clipped(breadcrumb, textWidth - 10),
                    this.contentX() + 8, rowY + 33, TEAL_DARK, false);

            Component scope = Component.translatable("config.smartbackpacks.scope."
                    + entry.scope().name().toLowerCase());
            int scopeX = this.contentX() + this.contentWidth() - 27 - this.font.width(scope);
            graphics.drawString(this.font, scope, scopeX, rowY + 3,
                    entry.scope() == Scope.SERVER ? PANEL_DARK : TEAL_DARK, false);
            if (hovered) {
                this.hoveredEntry = entry;
            }
        }

        if (visible.size() > this.maxVisibleRows()) {
            int trackX = this.contentX() + this.contentWidth() - 3;
            int trackTop = this.rowsTop();
            int trackHeight = this.maxVisibleRows() * ROW_HEIGHT - 3;
            graphics.fill(trackX, trackTop, trackX + 2, trackTop + trackHeight, PANEL_DARK);
            int thumbHeight = Math.max(12, trackHeight * this.maxVisibleRows() / visible.size());
            int maxScroll = Math.max(1, visible.size() - this.maxVisibleRows());
            int thumbY = trackTop + (trackHeight - thumbHeight) * this.scrollOffset / maxScroll;
            graphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbHeight, TEAL);
        }
    }

    private void drawFooterStatus(GuiGraphics graphics) {
        if (this.hasChanges()) {
            graphics.drawString(this.font,
                    Component.translatable("config.smartbackpacks.unsaved", this.changedCount()),
                    this.leftPos + this.sidebarWidth + 8, this.topPos + this.imageHeight - 25, ERROR_TEXT, false);
        } else if (System.currentTimeMillis() < this.statusUntil) {
            graphics.drawString(this.font, this.statusMessage,
                    this.leftPos + this.sidebarWidth + 8, this.topPos + this.imageHeight - 25, this.statusColor, false);
        }
    }

    private void drawConfirmation(GuiGraphics graphics) {
        graphics.fill(this.leftPos + 2, this.topPos + 2, this.leftPos + this.imageWidth - 2,
                this.topPos + this.imageHeight - 2, 0x99000000);
        int width = Math.min(380, this.imageWidth - 32);
        int height = 112;
        int x = this.leftPos + (this.imageWidth - width) / 2;
        int y = this.topPos + (this.imageHeight - height) / 2;
        graphics.fill(x, y, x + width, y + height, PANEL_BORDER);
        graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, PANEL_LIGHT);
        Component title = Component.translatable("config.smartbackpacks.confirm." + this.confirmAction.id + ".title");
        Component message = this.confirmAction == ConfirmAction.RESET_CATEGORY
                ? Component.translatable("config.smartbackpacks.confirm.reset_category.message", this.selectedCategory.title())
                : Component.translatable("config.smartbackpacks.confirm." + this.confirmAction.id + ".message");
        graphics.drawCenteredString(this.font, title, x + width / 2, y + 16, TEXT);
        List<?> lines = this.font.split(message, width - 24);
        int lineY = y + 38;
        for (Object line : lines) {
            graphics.drawCenteredString(this.font, (net.minecraft.util.FormattedCharSequence) line,
                    x + width / 2, lineY, MUTED_TEXT);
            lineY += 11;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.confirmAction == ConfirmAction.NONE && this.isInsideRows(mouseX, mouseY)) {
            int requested = this.scrollOffset + (scrollY < 0.0D ? 1 : -1);
            int clamped = this.clampedScroll(requested);
            if (clamped != this.scrollOffset) {
                this.scrollOffset = clamped;
                this.scheduleRebuild(false);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            if (this.confirmAction != ConfirmAction.NONE) {
                this.confirmAction = ConfirmAction.NONE;
                this.scheduleRebuild(false);
            } else {
                this.requestClose();
            }
            return true;
        }
        if (event.hasControlDown() && event.key() == GLFW.GLFW_KEY_F && this.searchBox != null) {
            this.setFocused(this.searchBox);
            this.searchBox.setFocused(true);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        this.requestClose();
    }

    private void requestClose() {
        if (this.hasChanges()) {
            this.openConfirmation(ConfirmAction.DISCARD);
        } else {
            this.closeParent();
        }
    }

    private void closeParent() {
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }

    private void openConfirmation(ConfirmAction action) {
        this.confirmAction = action;
        this.scheduleRebuild(false);
    }

    private void confirmPendingAction() {
        ConfirmAction action = this.confirmAction;
        this.confirmAction = ConfirmAction.NONE;
        if (action == ConfirmAction.DISCARD) {
            this.closeParent();
            return;
        }
        for (ConfigEntry entry : this.entries) {
            if (!this.isEditable(entry)) {
                continue;
            }
            if (action == ConfirmAction.RESET_ALL || entry.category() == this.selectedCategory) {
                this.staged.put(entry.id(), entry.defaultValue());
                this.invalidEntries.remove(entry.id());
            }
        }
        this.scrollOffset = 0;
        this.scheduleRebuild(false);
    }

    private boolean applyChanges() {
        if (!this.invalidEntries.isEmpty()) {
            this.statusMessage = Component.translatable("config.smartbackpacks.error.invalid_number");
            this.statusColor = ERROR_TEXT;
            this.statusUntil = System.currentTimeMillis() + 5000L;
            return false;
        }
        int minLoot = ((Number) this.staged.get("mob_min_loot")).intValue();
        int maxLoot = ((Number) this.staged.get("mob_max_loot")).intValue();
        if (minLoot > maxLoot) {
            this.statusMessage = Component.translatable("config.smartbackpacks.error.loot_range");
            this.statusColor = ERROR_TEXT;
            this.statusUntil = System.currentTimeMillis() + 5000L;
            return false;
        }

        try {
            for (ConfigEntry entry : this.entries) {
                Object value = this.staged.get(entry.id());
                if (this.isEditable(entry) && !Objects.equals(value, this.baseline.get(entry.id()))) {
                    entry.apply(value);
                }
            }
            for (ConfigEntry entry : this.entries) {
                Object accepted = entry.read();
                this.baseline.put(entry.id(), accepted);
                this.staged.put(entry.id(), accepted);
            }
            this.statusMessage = Component.translatable("config.smartbackpacks.saved");
            this.statusColor = TEAL_DARK;
            this.statusUntil = System.currentTimeMillis() + 3000L;
            this.scheduleRebuild(false);
            return true;
        } catch (RuntimeException exception) {
            this.statusMessage = Component.translatable("config.smartbackpacks.error.save");
            this.statusColor = ERROR_TEXT;
            this.statusUntil = System.currentTimeMillis() + 5000L;
            return false;
        }
    }

    private void stage(ConfigEntry entry, Object value) {
        if (this.isEditable(entry)) {
            this.staged.put(entry.id(), value);
            this.invalidEntries.remove(entry.id());
            this.updateActionButtons();
        }
    }

    private void updateActionButtons() {
        boolean valid = this.invalidEntries.isEmpty();
        if (this.applyButton != null) {
            this.applyButton.active = valid && this.hasChanges();
        }
        if (this.doneButton != null) {
            this.doneButton.active = valid;
        }
    }

    private boolean isEditable(ConfigEntry entry) {
        if (entry.scope() == Scope.CLIENT || this.minecraft == null || this.minecraft.level == null) {
            return true;
        }
        return this.minecraft.hasSingleplayerServer();
    }

    private boolean isChanged(ConfigEntry entry) {
        return !Objects.equals(this.baseline.get(entry.id()), this.staged.get(entry.id()));
    }

    private boolean hasChanges() {
        return this.entries.stream().anyMatch(this::isChanged);
    }

    private int changedCount() {
        return (int) this.entries.stream().filter(this::isChanged).count();
    }

    private List<ConfigEntry> filteredEntries() {
        List<ConfigEntry> filtered = new ArrayList<>();
        String section = this.selectedSections.get(this.selectedCategory);
        for (ConfigEntry entry : this.entries) {
            if (!this.searchQuery.isBlank()) {
                if (entry.matches(this.searchQuery)) {
                    filtered.add(entry);
                }
                continue;
            }
            if (entry.category() == this.selectedCategory
                    && (section == null || section.equals(entry.section()))) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    private List<String> sectionsFor(Category category) {
        if (category == Category.UPGRADES) {
            return UPGRADE_SECTIONS;
        }
        if (category == Category.MOB_BACKPACKS) {
            return MOB_SECTIONS;
        }
        return List.of();
    }

    private int contentX() {
        return this.leftPos + this.sidebarWidth + 10;
    }

    private int contentWidth() {
        return this.imageWidth - this.sidebarWidth - 20;
    }

    private int rowsTop() {
        boolean sectionSelector = this.searchQuery.isBlank() && this.sectionsFor(this.selectedCategory).size() > 1;
        return this.topPos + HEADER_HEIGHT + (sectionSelector ? 45 : 31);
    }

    private int maxVisibleRows() {
        int bottom = this.topPos + this.imageHeight - FOOTER_HEIGHT - 4;
        return Math.max(1, (bottom - this.rowsTop()) / ROW_HEIGHT);
    }

    private int clampedScroll(int requested) {
        return Mth.clamp(requested, 0, Math.max(0, this.filteredEntries().size() - this.maxVisibleRows()));
    }

    private boolean isInsideRows(double mouseX, double mouseY) {
        return mouseX >= this.contentX() && mouseX < this.contentX() + this.contentWidth()
                && mouseY >= this.rowsTop()
                && mouseY < this.rowsTop() + this.maxVisibleRows() * ROW_HEIGHT;
    }

    private Component clipped(Component component, int maxWidth) {
        if (this.font.width(component) <= maxWidth) {
            return component;
        }
        String shortened = this.font.plainSubstrByWidth(component.getString(),
                Math.max(0, maxWidth - this.font.width("...")));
        return Component.literal(shortened + "...");
    }

    private ItemStack configIcon() {
        try {
            return new ItemStack(ModItems.NETHERITE_BACKPACK.get());
        } catch (RuntimeException ignored) {
            return ItemStack.EMPTY;
        }
    }

    private void scheduleRebuild(boolean searchFocus) {
        this.refocusSearch = searchFocus;
        this.rebuildPending = true;
    }

    @Override
    protected void rebuildWidgets() {
        this.invalidEntries.clear();
        this.clearWidgets();
        this.init();
    }

    private enum ConfirmAction {
        NONE("none"),
        RESET_CATEGORY("reset_category"),
        RESET_ALL("reset_all"),
        DISCARD("discard");

        private final String id;

        ConfirmAction(String id) {
            this.id = id;
        }
    }
}
