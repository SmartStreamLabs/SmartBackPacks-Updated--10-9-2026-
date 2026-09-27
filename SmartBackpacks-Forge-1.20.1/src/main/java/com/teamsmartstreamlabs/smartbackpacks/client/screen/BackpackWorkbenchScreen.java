package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphics;
import com.teamsmartstreamlabs.smartbackpacks.menu.BackpackWorkbenchMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.BackpackWorkbenchMenu.DisplayRecipe;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public final class BackpackWorkbenchScreen extends LegacyContainerScreen<BackpackWorkbenchMenu> {
    private static final int BORDER = 0xFF38291E;
    private static final int PANEL = 0xFFC7AE8C;
    private static final int PAPER = 0xFFE3D3B4;
    private static final int SLOT = 0xFFB39370;
    private static final int TEXT = 0xFF3D2F22;
    private static final int GOOD = 0xFF376A3D;
    private static final int MISSING = 0xFFA03D32;
    private static final String[] CATEGORIES = {
            "backpacks", "storage", "utility", "automation", "machines", "energy", "survival", "special"
    };

    private final Button[] recipeButtons = new Button[7];
    private Button previousButton;
    private Button nextButton;
    private Button craftButton;
    private EditBox search;
    private String category = "backpacks";
    private DisplayRecipe selected;
    private int page;
    private int ingredientOffset;
    private final List<ItemStack> visibleRequirements = new ArrayList<>();

    public BackpackWorkbenchScreen(BackpackWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, BackpackWorkbenchMenu.WIDTH, BackpackWorkbenchMenu.HEIGHT);
        this.titleLabelX = 14;
        this.titleLabelY = 11;
        this.inventoryLabelX = BackpackWorkbenchMenu.PLAYER_X;
        this.inventoryLabelY = BackpackWorkbenchMenu.PLAYER_Y - 13;
    }

    @Override
    protected void init() {
        super.init();
        this.search = this.addRenderableWidget(new EditBox(this.font,
                this.leftPos + 100, this.topPos + 19, 112, 14,
                Component.translatable("screen.smartbackpacks.workbench.search")));
        this.search.setMaxLength(40);
        this.search.setResponder(value -> {
            this.page = 0;
            this.refreshRecipes();
        });
        for (int index = 0; index < CATEGORIES.length; index++) {
            String categoryName = CATEGORIES[index];
            this.addRenderableWidget(Button.builder(
                    Component.translatable("screen.smartbackpacks.workbench.category." + categoryName),
                    button -> {
                        this.category = categoryName;
                        this.page = 0;
                        this.refreshRecipes();
                    }).bounds(this.leftPos + 10, this.topPos + 34 + index * 14, 80, 13).build());
        }
        for (int index = 0; index < this.recipeButtons.length; index++) {
            int row = index;
            this.recipeButtons[index] = this.addRenderableWidget(Button.builder(Component.empty(), button -> {
                List<DisplayRecipe> filtered = this.filteredRecipes();
                int recipeIndex = this.page * this.recipeButtons.length + row;
                if (recipeIndex < filtered.size()) {
                    this.selected = filtered.get(recipeIndex);
                    this.ingredientOffset = 0;
                }
            }).bounds(this.leftPos + 115, this.topPos + 36 + index * 13, 100, 12).build());
        }
        this.previousButton = this.addRenderableWidget(Button.builder(Component.literal("<"), button -> {
            this.page--;
            this.refreshRecipes();
        }).bounds(this.leftPos + 101, this.topPos + 128, 26, 15).build());
        this.nextButton = this.addRenderableWidget(Button.builder(Component.literal(">"), button -> {
            this.page++;
            this.refreshRecipes();
        }).bounds(this.leftPos + 188, this.topPos + 128, 26, 15).build());
        this.craftButton = this.addRenderableWidget(Button.builder(
                Component.translatable("screen.smartbackpacks.workbench.craft"), button -> this.craft())
                .bounds(this.leftPos + 233, this.topPos + 140, 122, 17).build());
        this.refreshRecipes();
    }

    private List<DisplayRecipe> filteredRecipes() {
        String query = this.search == null ? "" : this.search.getValue().toLowerCase(Locale.ROOT);
        List<DisplayRecipe> filtered = new ArrayList<>();
        for (DisplayRecipe recipe : this.menu.recipes()) {
            if (query.isEmpty() && !categoryOf(recipe).equals(this.category)) {
                continue;
            }
            String name = recipe.output().getHoverName().getString().toLowerCase(Locale.ROOT);
            if (query.isEmpty() || name.contains(query) || recipe.id().toLowerCase(Locale.ROOT).contains(query)) {
                filtered.add(recipe);
            }
        }
        return filtered;
    }

    private static String categoryOf(DisplayRecipe recipe) {
        String id = recipe.id();
        if (id.endsWith("_backpack")) return "backpacks";
        if (id.contains("storage") || id.contains("stack")) return "storage";
        if (id.contains("furnace") || id.contains("smoker") || id.contains("brewing")) return "machines";
        if (id.contains("fluid") || id.contains("capacitor") || id.contains("xp_transfer")) return "energy";
        if (id.contains("fall_protection") || id.contains("death_emergency") || id.contains("repair")
                || id.contains("survival") || id.contains("feed") || id.contains("rescue")) return "survival";
        if (id.contains("hopper") || id.contains("magnet") || id.contains("pickup")
                || id.contains("deposit") || id.contains("restock") || id.contains("auto_")) return "automation";
        if (id.contains("crafting_table") || id.contains("stonecutter") || id.contains("anvil")
                || id.contains("smithing") || id.contains("grindstone") || id.contains("loom")
                || id.contains("cartography") || id.contains("enchanting")) return "utility";
        return "special";
    }

    private void refreshRecipes() {
        if (this.recipeButtons[0] == null) return;
        List<DisplayRecipe> filtered = this.filteredRecipes();
        this.page = Math.max(0, Math.min(this.page, Math.max(0,
                (filtered.size() - 1) / this.recipeButtons.length)));
        for (int index = 0; index < this.recipeButtons.length; index++) {
            int recipeIndex = this.page * this.recipeButtons.length + index;
            Button button = this.recipeButtons[index];
            button.visible = recipeIndex < filtered.size();
            if (button.visible) {
                String name = filtered.get(recipeIndex).output().getHoverName().getString();
                button.setMessage(Component.literal(this.fitLabel(name, 94)));
            }
        }
        this.previousButton.active = this.page > 0;
        this.nextButton.active = (this.page + 1) * this.recipeButtons.length < filtered.size();
        if (this.selected != null && !filtered.contains(this.selected)) {
            this.selected = null;
            this.ingredientOffset = 0;
        }
        if (this.selected == null && !filtered.isEmpty()) {
            this.selected = filtered.get(0);
        }
    }

    private void craft() {
        if (this.selected != null && this.menu.canCraft(this.selected)
                && this.minecraft != null && this.minecraft.gameMode != null) {
            int id = this.menu.recipes().indexOf(this.selected);
            if (id >= 0) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, BORDER);
        graphics.fill(x + 4, y + 4, x + this.imageWidth - 4, y + this.imageHeight - 4, PANEL);
        graphics.fill(x + 96, y + 16, x + 219, y + 158, PAPER);
        graphics.fill(x + 224, y + 4, x + 360, y + 158, PAPER);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                slot(graphics, x + BackpackWorkbenchMenu.PLAYER_X + column * 18,
                        y + BackpackWorkbenchMenu.PLAYER_Y + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            slot(graphics, x + BackpackWorkbenchMenu.PLAYER_X + column * 18,
                    y + BackpackWorkbenchMenu.HOTBAR_Y);
        }
        if (this.selected != null) {
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 3; column++) {
                    int gx = x + 235 + column * 18;
                    int gy = y + 31 + row * 18;
                    slot(graphics, gx, gy);
                    if (row < this.selected.height() && column < this.selected.width()) {
                        int ingredientIndex = row * this.selected.width() + column;
                        Optional<Ingredient> ingredient = this.selected.ingredients().get(ingredientIndex);
                        ingredient.ifPresent(value -> {
                            ItemStack[] items = value.getItems();
                            if (items.length > 0) graphics.renderItem(items[0], gx + 1, gy + 1);
                        });
                    }
                }
            }
            slot(graphics, x + 328, y + 49);
            graphics.renderItem(this.selected.output(), x + 329, y + 50);
        }
    }

    private static void slot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, BORDER);
        graphics.fill(x + 2, y + 2, x + 16, y + 16, SLOT);
    }

    private String fitLabel(String name, int width) {
        if (this.font.width(name) <= width) return name;
        while (!name.isEmpty() && this.font.width(name + "...") > width) {
            name = name.substring(0, name.length() - 1);
        }
        return name + "...";
    }

    @Override
    protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = this.leftPos;
        int y = this.topPos;
        if (this.selected != null) {
            if (mouseX >= x + 328 && mouseX < x + 346 && mouseY >= y + 49 && mouseY < y + 67) {
                graphics.renderTooltip(this.font, this.selected.output().getHoverName(), mouseX, mouseY);
                return;
            }
            for (int row = 0; row < this.selected.height(); row++) {
                for (int column = 0; column < this.selected.width(); column++) {
                    int gx = x + 235 + column * 18;
                    int gy = y + 31 + row * 18;
                    if (mouseX >= gx && mouseX < gx + 18 && mouseY >= gy && mouseY < gy + 18) {
                        Optional<Ingredient> ingredient = this.selected.ingredients().get(row * this.selected.width() + column);
                        ingredient.ifPresent(value -> {
                            ItemStack[] items = value.getItems();
                            if (items.length > 0) graphics.renderTooltip(this.font, items[0].getHoverName(), mouseX, mouseY);
                        });
                        return;
                    }
                }
            }
        }
        if (mouseX >= x + 228 && mouseX < x + 354 && mouseY >= y + 91
                && mouseY < y + 91 + this.visibleRequirements.size() * 10) {
            ItemStack item = this.visibleRequirements.get((mouseY - y - 91) / 10);
            if (!item.isEmpty()) graphics.renderTooltip(this.font, item.getHoverName(), mouseX, mouseY);
            return;
        }
        List<DisplayRecipe> filtered = this.filteredRecipes();
        for (int index = 0; index < this.recipeButtons.length; index++) {
            int recipeIndex = this.page * this.recipeButtons.length + index;
            Button button = this.recipeButtons[index];
            if (button.visible && recipeIndex < filtered.size()
                    && mouseX >= button.getX() && mouseX < button.getX() + button.getWidth()
                    && mouseY >= button.getY() && mouseY < button.getY() + button.getHeight()) {
                graphics.renderTooltip(this.font, filtered.get(recipeIndex).output().getHoverName(), mouseX, mouseY);
                return;
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        this.visibleRequirements.clear();
        this.craftButton.active = this.selected != null && this.menu.canCraft(this.selected);
        graphics.drawString(this.font, this.title, 14, 10, TEXT, false);
        graphics.drawString(this.font, this.playerInventoryTitle,
                this.inventoryLabelX, this.inventoryLabelY, TEXT, false);
        if (this.selected == null) return;
        String outputName = this.selected.output().getHoverName().getString();
        if (outputName.length() > 19) outputName = outputName.substring(0, 18) + "...";
        graphics.drawString(this.font, Component.literal(outputName), 228, 7, TEXT, false);
        boolean craftable = this.menu.canCraft(this.selected);
        graphics.drawString(this.font,
                Component.translatable(craftable
                        ? "screen.smartbackpacks.workbench.available"
                        : "screen.smartbackpacks.workbench.missing"),
                228, 20, craftable ? GOOD : MISSING, false);
        Map<String, Ingredient> distinct = new LinkedHashMap<>();
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Optional<Ingredient> entry : this.selected.ingredients()) {
            if (entry.isEmpty()) continue;
            Ingredient ingredient = entry.get();
            String key = Arrays.stream(ingredient.getItems()).map(ItemStack::toString).sorted().toList().toString();
            distinct.putIfAbsent(key, ingredient);
            counts.merge(key, 1, Integer::sum);
        }
        this.ingredientOffset = Math.min(this.ingredientOffset, Math.max(0, distinct.size() - 5));
        int shown = 0;
        int skipped = 0;
        for (Map.Entry<String, Ingredient> entry : distinct.entrySet()) {
            if (skipped++ < this.ingredientOffset) continue;
            if (shown >= 5) break;
            Ingredient ingredient = entry.getValue();
            int needed = counts.get(entry.getKey());
            ItemStack[] choices = ingredient.getItems();
            ItemStack display = choices.length > 0 ? choices[0] : ItemStack.EMPTY;
            int have = 0;
            for (int slot = 0; slot < 36; slot++) {
                ItemStack stack = this.menu.getSlot(slot).getItem();
                if (ingredient.test(stack)) have += stack.getCount();
            }
            this.visibleRequirements.add(display);
            String count = " " + have + "/" + needed;
            String name = display.isEmpty() ? "?" : display.getHoverName().getString();
            graphics.drawString(this.font, Component.literal(this.fitLabel(name, 124 - this.font.width(count)) + count),
                    228, 91 + shown * 10, have >= needed ? TEXT : MISSING, false);
            shown++;
        }
        if (this.ingredientOffset > 0) graphics.drawString(this.font, Component.literal("^"), 348, 91, TEXT, false);
        if (this.ingredientOffset + 5 < distinct.size()) graphics.drawString(this.font, Component.literal("v"), 348, 131, TEXT, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (this.selected != null && mouseX >= this.leftPos + 224 && mouseX < this.leftPos + 360
                && mouseY >= this.topPos + 86 && mouseY < this.topPos + 140) {
            int unique = (int) this.selected.ingredients().stream().filter(Optional::isPresent)
                    .map(Optional::get).map(ingredient -> Arrays.stream(ingredient.getItems())
                            .map(ItemStack::toString).sorted().toList().toString()).distinct().count();
            this.ingredientOffset = Math.max(0, Math.min(Math.max(0, unique - 5),
                    this.ingredientOffset - (int) Math.signum(scrollY)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.search.isFocused()) {
            if (this.search.keyPressed(keyCode, scanCode, modifiers)) return true;
            if (this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.search.isFocused() && this.search.charTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }
}
