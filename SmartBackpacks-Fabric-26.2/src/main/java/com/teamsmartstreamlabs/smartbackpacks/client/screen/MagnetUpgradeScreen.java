package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.MagnetUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.network.AddMagnetUpgradeModPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.AddMagnetUpgradeTagPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.RemoveMagnetUpgradeModPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.RemoveMagnetUpgradeTagPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.SetMagnetFilterInputTypePayload;
import com.teamsmartstreamlabs.smartbackpacks.network.SetMagnetUpgradeModePayload;
import com.teamsmartstreamlabs.smartbackpacks.network.SetMagnetUpgradeTogglePayload;
import com.teamsmartstreamlabs.smartbackpacks.network.UseInstalledUpgradePayload;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.MagnetFilterInputType;

import com.teamsmartstreamlabs.smartbackpacks.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class MagnetUpgradeScreen extends LegacyContainerScreen<MagnetUpgradeMenu> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "textures/gui/container/backpack.png");

    private final List<Button> removeTagButtons = new ArrayList<>();
    private final List<Button> removeModButtons = new ArrayList<>();
    private EditBox textInput;
    private Button modeButton;
    private Button itemTypeButton;
    private Button modTypeButton;
    private Button tagTypeButton;
    private Button nbtButton;
    private Button damageButton;
    private Button contentsButton;
    private boolean allowlist;
    private MagnetFilterInputType selectedInputType;
    private boolean matchNbt;
    private boolean matchDamage;
    private boolean matchBackpackContentsOnly;
    private boolean blockModdedItems;
    private boolean onlyWhenFull;
    private List<String> modFilters;
    private List<Identifier> tags;

    public MagnetUpgradeScreen(MagnetUpgradeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, menu.isAdvanced() ? 251 : 167);
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = menu.isAdvanced() ? 157 : 73;
        this.allowlist = menu.isAllowlist();
        this.selectedInputType = menu.getSelectedInputType();
        this.matchNbt = menu.isMatchNbt();
        this.matchDamage = menu.isMatchDamage();
        this.matchBackpackContentsOnly = menu.isMatchBackpackContentsOnly();
        this.blockModdedItems = menu.isBlockModdedItems();
        this.onlyWhenFull = menu.isOnlyWhenFull();
        this.modFilters = new ArrayList<>(menu.getModFilters());
        this.tags = new ArrayList<>(menu.getTagFilters());
    }

    @Override
    protected void init() {
        super.init();
        this.modeButton = this.addRenderableWidget(Button.builder(this.getTopRightButtonLabel(), button -> this.onTopRightButton())
                .pos(this.leftPos + 118, this.topPos + 3)
                .size(50, 12)
                .tooltip(Tooltip.create(Component.translatable(this.menu.isAdvanced()
                        ? "tooltip.smartbackpacks.magnet_mode"
                        : "tooltip.smartbackpacks.magnet_regular_mode")))
                .build());

        if (this.menu.isAdvanced()) {
            this.initAdvancedControls();
        } else {
            this.textInput = this.addRenderableWidget(new EditBox(this.font, this.leftPos + 8, this.topPos + 42, 114, 14,
                    Component.translatable("screen.smartbackpacks.magnet.tag_input")));
            this.textInput.setVisible(false);
        }

        if (this.menu.isCourierUpgrade()) {
            boolean linked = this.menu.getCourierDestination().linked();
            Component label = Component.translatable(linked
                    ? "screen.smartbackpacks.courier.unlink_destination"
                    : "screen.smartbackpacks.courier.link_destination");
            Component tooltip = linked
                    ? Component.translatable("screen.smartbackpacks.courier.destination", this.menu.getCourierDestination().name())
                    : Component.translatable("message.smartbackpacks.courier.select_destination");
            this.addRenderableWidget(Button.builder(label, button ->
                            PacketDistributor.sendToServer(new UseInstalledUpgradePayload(-1, true)))
                    .pos(this.leftPos + 8, this.topPos + 114)
                    .size(160, 16)
                    .tooltip(Tooltip.create(tooltip))
                    .build());
        }

        this.refreshRemoveButtons();
    }

    private void initAdvancedControls() {
        int y = this.topPos + 94;
        this.itemTypeButton = this.addRenderableWidget(this.makeTypeButton(Component.translatable("screen.smartbackpacks.magnet.filter_type.item"), MagnetFilterInputType.ITEM, this.leftPos + 8, y));
        this.modTypeButton = this.addRenderableWidget(this.makeTypeButton(Component.translatable("screen.smartbackpacks.magnet.filter_type.mod"), MagnetFilterInputType.MOD, this.leftPos + 46, y));
        this.tagTypeButton = this.addRenderableWidget(this.makeTypeButton(Component.translatable("screen.smartbackpacks.magnet.filter_type.tag"), MagnetFilterInputType.TAG, this.leftPos + 84, y));
        this.nbtButton = this.addRenderableWidget(Button.builder(this.getToggleLabel("NBT", this.matchNbt), button -> this.toggleAdvancedOption("match_nbt"))
                .pos(this.leftPos + 122, y)
                .size(22, 16)
                .tooltip(Tooltip.create(Component.translatable("tooltip.smartbackpacks.magnet_match_nbt")))
                .build());
        this.damageButton = this.addRenderableWidget(Button.builder(this.getToggleLabel("DMG", this.matchDamage), button -> this.toggleAdvancedOption("match_damage"))
                .pos(this.leftPos + 146, y)
                .size(22, 16)
                .tooltip(Tooltip.create(Component.translatable("tooltip.smartbackpacks.magnet_match_damage")))
                .build());
        this.contentsButton = this.addRenderableWidget(Button.builder(
                        this.getToggleLabel(Component.translatable("screen.smartbackpacks.magnet.match_contents_short").getString(), this.matchBackpackContentsOnly),
                        button -> this.toggleAdvancedOption("match_backpack_contents_only"))
                .pos(this.leftPos + 8, this.topPos + 114)
                .size(82, 16)
                .tooltip(Tooltip.create(Component.translatable(this.menu.isVoidUpgrade()
                        ? "tooltip.smartbackpacks.void_only_when_full"
                        : "tooltip.smartbackpacks.magnet_match_contents")))
                .build());
        if (this.menu.isCourierUpgrade()) {
            this.contentsButton.visible = false;
        }
        this.textInput = this.addRenderableWidget(new EditBox(this.font, this.leftPos + 8, this.topPos + 133, 114, 14,
                Component.translatable("screen.smartbackpacks.magnet.entry_input")));
        this.textInput.setMaxLength(50);
        this.addRenderableWidget(Button.builder(Component.translatable("screen.smartbackpacks.magnet.add_entry"),
                        button -> this.addEntry())
                .pos(this.leftPos + 126, this.topPos + 132)
                .size(42, 16)
                .build());
        this.refreshTypeButtons();
    }

    private Button makeTypeButton(Component text, MagnetFilterInputType type, int x, int y) {
        return Button.builder(text, button -> this.setSelectedInputType(type))
                .pos(x, y)
                .size(36, 16)
                .build();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        int topHeight = 17;
        int footerY = this.menu.isAdvanced() ? 155 : 71;
        int bodyRows = this.menu.isAdvanced() ? 7 : 3;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, topHeight, 176, 131);
        for (int row = 0; row < bodyRows; row++) {
            guiGraphics.blit(TEXTURE, x, y + 17 + row * 18, 0, 17, this.imageWidth, 18, 176, 131);
        }
        guiGraphics.blit(TEXTURE, x, y + footerY, 0, 35, this.imageWidth, 96, 176, 131);

        if (this.menu.isAutoToolUpgrade()) {
            guiGraphics.fill(x + 80, y + 17, x + 168, y + 89, 0xFFD3BFA3);
            guiGraphics.fill(x + 8, y + 89, x + 168, y + 155, 0xFFD3BFA3);
        }
    }

    @Override
    protected void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.menu.isAdvanced()) {
            guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.magnet.filter_items"), this.leftPos + 8, this.topPos + 79, 0x404040, false);
            guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.magnet.mods"), this.leftPos + 8, this.topPos + 151, 0x404040, false);
            guiGraphics.drawString(this.font, Component.translatable("screen.smartbackpacks.magnet.tags"), this.leftPos + 92, this.topPos + 151, 0x404040, false);
            this.renderTextList(guiGraphics, this.modFilters, this.leftPos + 8, this.topPos + 161, 5);
            this.renderTextList(guiGraphics, this.tags.stream().map(tag -> "#" + tag).toList(), this.leftPos + 92, this.topPos + 161, 5);
        }
    }

    private void renderTextList(GuiGraphics guiGraphics, List<?> entries, int x, int y, int maxRows) {
        for (int index = 0; index < Math.min(entries.size(), maxRows); index++) {
            guiGraphics.drawString(this.font, String.valueOf(entries.get(index)), x, y + index * 10, 0x404040, false);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.textInput != null && this.textInput.isFocused()) {
            if (this.textInput.keyPressed(event)) {
                return true;
            }

            if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) {
                this.addEntry();
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.textInput != null && this.textInput.isFocused() && this.textInput.charTyped(event)) {
            return true;
        }
        return super.charTyped(event);
    }

    private void toggleMode() {
        this.allowlist = !this.allowlist;
        this.modeButton.setMessage(this.getTopRightButtonLabel());
        PacketDistributor.sendToServer(new SetMagnetUpgradeModePayload(this.allowlist));
    }

    private void toggleBlockModdedItems() {
        this.blockModdedItems = !this.blockModdedItems;
        this.modeButton.setMessage(this.getTopRightButtonLabel());
        PacketDistributor.sendToServer(new SetMagnetUpgradeTogglePayload("block_modded_items", this.blockModdedItems));
    }

    private void onTopRightButton() {
        if (this.menu.isAdvanced()) {
            this.toggleMode();
        } else {
            this.cycleRegularMode();
        }
    }

    private void cycleRegularMode() {
        if (this.blockModdedItems) {
            this.blockModdedItems = false;
            this.allowlist = true;
            PacketDistributor.sendToServer(new SetMagnetUpgradeTogglePayload("block_modded_items", false));
            PacketDistributor.sendToServer(new SetMagnetUpgradeModePayload(true));
        } else if (this.allowlist) {
            this.allowlist = false;
            PacketDistributor.sendToServer(new SetMagnetUpgradeModePayload(false));
        } else {
            this.blockModdedItems = true;
            PacketDistributor.sendToServer(new SetMagnetUpgradeTogglePayload("block_modded_items", true));
        }

        this.modeButton.setMessage(this.getTopRightButtonLabel());
    }

    private void setSelectedInputType(MagnetFilterInputType type) {
        this.selectedInputType = type;
        this.refreshTypeButtons();
        PacketDistributor.sendToServer(new SetMagnetFilterInputTypePayload(type.getSerializedName()));
    }

    private void toggleAdvancedOption(String option) {
        switch (option) {
            case "match_nbt" -> {
                this.matchNbt = !this.matchNbt;
                this.nbtButton.setMessage(this.getToggleLabel("NBT", this.matchNbt));
                PacketDistributor.sendToServer(new SetMagnetUpgradeTogglePayload(option, this.matchNbt));
            }
            case "match_damage" -> {
                this.matchDamage = !this.matchDamage;
                this.damageButton.setMessage(this.getToggleLabel("DMG", this.matchDamage));
                PacketDistributor.sendToServer(new SetMagnetUpgradeTogglePayload(option, this.matchDamage));
            }
            case "match_backpack_contents_only" -> {
                if (this.menu.isVoidUpgrade()) {
                    this.onlyWhenFull = !this.onlyWhenFull;
                    this.contentsButton.setMessage(this.getToggleLabel(Component.translatable("screen.smartbackpacks.void.only_when_full_short").getString(), this.onlyWhenFull));
                    PacketDistributor.sendToServer(new SetMagnetUpgradeTogglePayload("only_when_full", this.onlyWhenFull));
                } else {
                    this.matchBackpackContentsOnly = !this.matchBackpackContentsOnly;
                    this.contentsButton.setMessage(this.getToggleLabel(Component.translatable("screen.smartbackpacks.magnet.match_contents_short").getString(), this.matchBackpackContentsOnly));
                    PacketDistributor.sendToServer(new SetMagnetUpgradeTogglePayload(option, this.matchBackpackContentsOnly));
                }
            }
            default -> {
            }
        }
    }

    private void addEntry() {
        if (this.textInput == null) {
            return;
        }

        String value = this.textInput.getValue().trim();
        if (value.isEmpty()) {
            return;
        }

        if (this.selectedInputType == MagnetFilterInputType.MOD) {
            this.modFilters.add(value);
            PacketDistributor.sendToServer(new AddMagnetUpgradeModPayload(value));
        } else if (this.selectedInputType == MagnetFilterInputType.TAG) {
            Identifier tagId = Identifier.tryParse(value);
            if (tagId != null) {
                this.tags.add(tagId);
                PacketDistributor.sendToServer(new AddMagnetUpgradeTagPayload(tagId.toString()));
            }
        }

        this.textInput.setValue("");
        this.refreshRemoveButtons();
    }

    private void removeMod(int index) {
        if (index < 0 || index >= this.modFilters.size()) {
            return;
        }

        this.modFilters.remove(index);
        this.refreshRemoveButtons();
        PacketDistributor.sendToServer(new RemoveMagnetUpgradeModPayload(index));
    }

    private void removeTag(int index) {
        if (index < 0 || index >= this.tags.size()) {
            return;
        }

        this.tags.remove(index);
        this.refreshRemoveButtons();
        PacketDistributor.sendToServer(new RemoveMagnetUpgradeTagPayload(index));
    }

    private void refreshRemoveButtons() {
        this.removeModButtons.forEach(this::removeWidget);
        this.removeTagButtons.forEach(this::removeWidget);
        this.removeModButtons.clear();
        this.removeTagButtons.clear();

        if (!this.menu.isAdvanced()) {
            return;
        }

        int visibleMods = Math.min(this.modFilters.size(), 5);
        for (int index = 0; index < visibleMods; index++) {
            int modIndex = index;
            Button button = this.addRenderableWidget(Button.builder(Component.literal("x"), ignored -> this.removeMod(modIndex))
                    .pos(this.leftPos + 74, this.topPos + 158 + index * 10)
                    .size(12, 10)
                    .build());
            this.removeModButtons.add(button);
        }

        int visibleTags = Math.min(this.tags.size(), 5);
        for (int index = 0; index < visibleTags; index++) {
            int tagIndex = index;
            Button button = this.addRenderableWidget(Button.builder(Component.literal("x"), ignored -> this.removeTag(tagIndex))
                    .pos(this.leftPos + 158, this.topPos + 158 + index * 10)
                    .size(12, 10)
                    .build());
            this.removeTagButtons.add(button);
        }
    }

    private void refreshTypeButtons() {
        if (!this.menu.isAdvanced()) {
            return;
        }

        this.itemTypeButton.active = this.selectedInputType != MagnetFilterInputType.ITEM;
        this.modTypeButton.active = this.selectedInputType != MagnetFilterInputType.MOD;
        this.tagTypeButton.active = this.selectedInputType != MagnetFilterInputType.TAG;
    }

    private Component getTopRightButtonLabel() {
        if (!this.menu.isAdvanced()) {
            return Component.translatable(this.blockModdedItems
                    ? "screen.smartbackpacks.magnet.mode.no_mods"
                    : (this.allowlist
                    ? "screen.smartbackpacks.magnet.mode.allowlist"
                    : "screen.smartbackpacks.magnet.mode.blacklist"));
        }

        return Component.translatable(this.allowlist
                ? "screen.smartbackpacks.magnet.mode.allowlist"
                : "screen.smartbackpacks.magnet.mode.blacklist");
    }

    private Component getToggleLabel(String label, boolean enabled) {
        return Component.literal((enabled ? "[+]" : "[-]") + label);
    }
}

