package com.teamsmartstreamlabs.smartbackpacks.client;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.block.BackpackBlock;
import com.teamsmartstreamlabs.smartbackpacks.blockentity.PlacedBackpackBlockEntity;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.core.component.DataComponents;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.component.DyedItemColor;
import com.teamsmartstreamlabs.smartbackpacks.client.render.BackpackRenderLayer;
import com.teamsmartstreamlabs.smartbackpacks.client.render.MobBackpackRenderLayer;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.AutoFeedUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.BackpackLinkUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.BackpackScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.BuilderUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.CapacitorUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.CapacityWarningUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.ChunkLoaderUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.FluidStorageUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.FluidTransferUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.JukeboxUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.MagnetUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.QuiverUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.QuickAccessWheelScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.QuickAccessWheelUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.RescueUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.SmartBackpacksConfigScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.SurvivalAssistUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.TorchPlacerUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.StorageControllerScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.StorageGuideScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.XpTransferUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackSortMode;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.network.ModPayloads;
import com.teamsmartstreamlabs.smartbackpacks.network.OpenWornBackpackPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.PickupPlacedBackpackPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.PlaceHeldBackpackPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.RequestAutoToolSwapPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.RequestBuilderRefillPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.QuickAccessWheelRequestPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.SortOpenBackpackPayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.QuickAccessWheelUpgradeData;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class SmartBackpacksClient {
    private static final int DEFAULT_BACKPACK_TINT = 0xFF7A5330;
    private static final String KEY_CATEGORY = "key.categories.smartbackpacks";
    private static final KeyMapping OPEN_WORN_BACKPACK = new KeyMapping("key.smartbackpacks.open_backpack", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, KEY_CATEGORY);
    private static final KeyMapping MANUAL_AUTO_TOOL_SWAP = new KeyMapping("key.smartbackpacks.auto_tool_swap", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, KEY_CATEGORY);
    private static final KeyMapping MANUAL_BUILDER_REFILL = new KeyMapping("key.smartbackpacks.builder_refill", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, KEY_CATEGORY);
    private static final KeyMapping QUICK_ACCESS_WHEEL = new KeyMapping("key.smartbackpacks.quick_access_wheel", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, KEY_CATEGORY);
    private static PortableJukeboxSoundInstance portableJukeboxSound;
    private static boolean quickAccessWheelWasDown;

    private SmartBackpacksClient() {
    }

    public static void init(IEventBus modEventBus) {
        ModPayloads.registerClient();
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new SmartBackpacksConfigScreen(parent)));
        modEventBus.addListener(SmartBackpacksClient::onClientSetup);
        modEventBus.addListener(SmartBackpacksClient::addLayers);
        modEventBus.addListener(SmartBackpacksClient::registerKeyMappings);
        modEventBus.addListener(SmartBackpacksClient::registerBackpackItemColors);
        modEventBus.addListener(SmartBackpacksClient::registerBackpackBlockColors);
        MinecraftForge.EVENT_BUS.addListener(SmartBackpacksClient::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(SmartBackpacksClient::onMouseButton);
        MinecraftForge.EVENT_BUS.addListener(SmartBackpacksClient::onRenderGui);
        MinecraftForge.EVENT_BUS.addListener(SmartBackpacksClient::onRenderScreen);
        SmartBackpacks.LOGGER.info("Initializing {} Forge client bootstrap", SmartBackpacks.MOD_ID);
    }

    private static void registerBackpackItemColors(RegisterColorHandlersEvent.Item event) {
        Item[] backpackItems = BuiltInRegistries.ITEM.stream()
                .filter(BackpackItem.class::isInstance)
                .toArray(Item[]::new);
        event.register((stack, tintIndex) -> tintIndex == 0 ? getBackpackColor(stack) : -1, backpackItems);
    }

    private static void registerBackpackBlockColors(RegisterColorHandlersEvent.Block event) {
        Block[] backpackBlocks = BuiltInRegistries.BLOCK.stream()
                .filter(BackpackBlock.class::isInstance)
                .toArray(Block[]::new);
        event.register((state, level, pos, tintIndex) -> {
            if (tintIndex == 0 && level != null && pos != null
                    && level.getBlockEntity(pos) instanceof PlacedBackpackBlockEntity blockEntity) {
                return getBackpackColor(blockEntity.getStoredBackpack());
            }
            return -1;
        }, backpackBlocks);
    }

    private static int getBackpackColor(ItemStack stack) {
        DyedItemColor color = ItemStackDataComponents.get(stack, DataComponents.DYED_COLOR);
        return color == null ? DEFAULT_BACKPACK_TINT : 0xFF000000 | color.rgb();
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BuiltInRegistries.BLOCK.stream()
                    .filter(BackpackBlock.class::isInstance)
                    .forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout()));
            MenuScreens.register(ModMenuTypes.BACKPACK.get(), BackpackScreen::new);
            MenuScreens.register(ModMenuTypes.MAGNET_UPGRADE.get(), MagnetUpgradeScreen::new);
            MenuScreens.register(ModMenuTypes.AUTO_FEED_UPGRADE.get(), AutoFeedUpgradeScreen::new);
            MenuScreens.register(ModMenuTypes.SURVIVAL_ASSIST_UPGRADE.get(), SurvivalAssistUpgradeScreen::new);
            MenuScreens.register(ModMenuTypes.FLUID_STORAGE_UPGRADE.get(), FluidStorageUpgradeScreen::new);
            MenuScreens.register(ModMenuTypes.FLUID_TRANSFER_UPGRADE.get(), FluidTransferUpgradeScreen::new);
            MenuScreens.register(ModMenuTypes.CAPACITOR_UPGRADE.get(), CapacitorUpgradeScreen::new);
            MenuScreens.register(ModMenuTypes.CHUNK_LOADER_UPGRADE.get(), ChunkLoaderUpgradeScreen::new);
            MenuScreens.register(ModMenuTypes.JUKEBOX_UPGRADE.get(), JukeboxUpgradeScreen::new);
            MenuScreens.register(ModMenuTypes.AUTO_SMELTING_UPGRADE.get(), FurnaceScreen::new);
            MenuScreens.register(ModMenuTypes.XP_TRANSFER_UPGRADE.get(), XpTransferUpgradeScreen::new);
            MenuScreens.register(ModMenuTypes.QUIVER_UPGRADE.get(), QuiverUpgradeScreen::new);
            MenuScreens.register(ModMenuTypes.QUICK_ACCESS_WHEEL_UPGRADE.get(), QuickAccessWheelUpgradeScreen::new);
            MenuScreens.register(ModMenuTypes.RESCUE_UPGRADE.get(), RescueUpgradeScreen::new);
            MenuScreens.register(ModMenuTypes.BUILDER_UPGRADE.get(), BuilderUpgradeScreen::new);
            MenuScreens.register(ModMenuTypes.TORCH_PLACER_UPGRADE.get(), TorchPlacerUpgradeScreen::new);
            MenuScreens.register(ModMenuTypes.CAPACITY_WARNING_UPGRADE.get(), CapacityWarningUpgradeScreen::new);
            MenuScreens.register(ModMenuTypes.BACKPACK_LINK_UPGRADE.get(), BackpackLinkUpgradeScreen::new);
            MenuScreens.register(ModMenuTypes.STORAGE_CONTROLLER.get(), StorageControllerScreen::new);
        });
    }

    private static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        CapacityWarningHud.render(event);
        PickupNotifierHud.render(event);
    }

    private static void onRenderScreen(ScreenEvent.Render.Post event) {
        CapacityWarningHud.render(event.getGuiGraphics());
        PickupNotifierHud.render(event.getGuiGraphics());
    }

    private static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new BackpackRenderLayer(renderer));
            }
        }

        addMobBackpackLayer(event, EntityType.ZOMBIE, false);
        addMobBackpackLayer(event, EntityType.HUSK, false);
        addMobBackpackLayer(event, EntityType.DROWNED, false);
        addMobBackpackLayer(event, EntityType.SKELETON, false);
        addMobBackpackLayer(event, EntityType.STRAY, false);
        addMobBackpackLayer(event, EntityType.CREEPER, true);
        addMobBackpackLayer(event, EntityType.PILLAGER, false);
        addMobBackpackLayer(event, EntityType.VINDICATOR, false);
        addMobBackpackLayer(event, EntityType.WITCH, false);
        addMobBackpackLayer(event, EntityType.PIGLIN, false);
        addMobBackpackLayer(event, EntityType.ZOMBIFIED_PIGLIN, false);
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_WORN_BACKPACK);
        event.register(MANUAL_AUTO_TOOL_SWAP);
        event.register(MANUAL_BUILDER_REFILL);
        event.register(QUICK_ACCESS_WHEEL);
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        StorageGuideScreen.openIfRequested(minecraft);
        updatePortableJukebox(minecraft);
        boolean quickAccessWheelDown = QUICK_ACCESS_WHEEL.isDown();
        if (minecraft.player == null || minecraft.screen != null) {
            quickAccessWheelWasDown = quickAccessWheelDown;
            return;
        }

        while (OPEN_WORN_BACKPACK.consumeClick()) {
            PacketDistributor.sendToServer(OpenWornBackpackPayload.INSTANCE);
        }

        while (MANUAL_AUTO_TOOL_SWAP.consumeClick()) {
            if (minecraft.hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getType() == HitResult.Type.BLOCK) {
                PacketDistributor.sendToServer(new RequestAutoToolSwapPayload(blockHitResult.getBlockPos(), true));
            }
        }

        while (MANUAL_BUILDER_REFILL.consumeClick()) {
            PacketDistributor.sendToServer(RequestBuilderRefillPayload.INSTANCE);
        }

        if (quickAccessWheelDown && !quickAccessWheelWasDown) {
            PacketDistributor.sendToServer(QuickAccessWheelRequestPayload.INSTANCE);
        }
        quickAccessWheelWasDown = quickAccessWheelDown;
    }

    public static boolean isQuickAccessWheelKeyDown() {
        return QUICK_ACCESS_WHEEL.isDown();
    }

    public static boolean matchesQuickAccessWheel(int keyCode, int scanCode) {
        return QUICK_ACCESS_WHEEL.matches(keyCode, scanCode);
    }

    public static void openQuickAccessWheel(List<ItemStack> configured, int availableMask) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        ItemStack[] favorites = new ItemStack[QuickAccessWheelUpgradeData.FAVORITE_COUNT];
        boolean[] available = new boolean[QuickAccessWheelUpgradeData.FAVORITE_COUNT];
        for (int slot = 0; slot < favorites.length; slot++) {
            favorites[slot] = configured.get(slot).copy();
            available[slot] = (availableMask & 1 << slot) != 0;
        }
        minecraft.setScreen(new QuickAccessWheelScreen(favorites, available));
    }

    private static ItemStack getVisibleBackpack(LocalPlayer player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (BackpackItem.isBackpack(chest)) {
            return chest;
        }
        return CuriosCompat.findFirstMatchingBackStack(player, BackpackItem::isBackpack)
                .map(CuriosCompat.BackSlotMatch::stack)
                .orElse(ItemStack.EMPTY);
    }

    private static void onMouseButton(InputEvent.MouseButton event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null || event.getAction() != InputConstants.PRESS) {
            return;
        }

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && !minecraft.player.isShiftKeyDown()
                && minecraft.hitResult instanceof BlockHitResult blockHitResult
                && blockHitResult.getType() == HitResult.Type.BLOCK) {
            PacketDistributor.sendToServer(new RequestAutoToolSwapPayload(blockHitResult.getBlockPos(), false));
        }

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && minecraft.player.isShiftKeyDown()
                && !minecraft.player.isCreative()
                && minecraft.hitResult instanceof BlockHitResult blockHitResult
                && blockHitResult.getType() == HitResult.Type.BLOCK
                && minecraft.level != null
                && minecraft.level.getBlockState(blockHitResult.getBlockPos()).getBlock() instanceof BackpackBlock) {
            PacketDistributor.sendToServer(new PickupPlacedBackpackPayload(blockHitResult.getBlockPos()));
            event.setCanceled(true);
            return;
        }

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && minecraft.player.isShiftKeyDown()
                && minecraft.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof BackpackItem
                && minecraft.hitResult instanceof BlockHitResult blockHitResult
                && blockHitResult.getType() == HitResult.Type.BLOCK) {
            PacketDistributor.sendToServer(new PlaceHeldBackpackPayload(blockHitResult.getBlockPos(), blockHitResult.getDirection()));
            event.setCanceled(true);
            return;
        }

        if (minecraft.screen instanceof BackpackScreen backpackScreen
                && event.getButton() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE
                && event.getAction() == InputConstants.PRESS) {
            BackpackSortMode sortMode = backpackScreen.getSortMode();
            PacketDistributor.sendToServer(new SortOpenBackpackPayload(sortMode));
            event.setCanceled(true);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T extends LivingEntity> void addMobBackpackLayer(EntityRenderersEvent.AddLayers event,
                                                                      EntityType<T> type,
                                                                      boolean creeper) {
        LivingEntityRenderer renderer = event.getRenderer(type);
        if (renderer != null) {
            renderer.addLayer(new MobBackpackRenderLayer(renderer, creeper));
        }
    }

    public static void playPortableJukebox(ItemStack disc) {
        Minecraft minecraft = Minecraft.getInstance();
        stopPortableJukebox();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        if (disc.getItem() instanceof RecordItem recordItem) {
            portableJukeboxSound = new PortableJukeboxSoundInstance(recordItem.getSound(), minecraft.player);
            minecraft.getSoundManager().play(portableJukeboxSound);
        }
    }

    public static void stopPortableJukebox() {
        Minecraft minecraft = Minecraft.getInstance();
        if (portableJukeboxSound != null) {
            minecraft.getSoundManager().stop(portableJukeboxSound);
        }
        portableJukeboxSound = null;
    }

    private static void updatePortableJukebox(Minecraft minecraft) {
        if (portableJukeboxSound == null) {
            return;
        }

        if (portableJukeboxSound.isStopped()
                || minecraft.player == null
                || !portableJukeboxSound.isBoundTo(minecraft.player)) {
            stopPortableJukebox();
        }
    }

    private static final class PortableJukeboxSoundInstance extends AbstractTickableSoundInstance {
        private final LocalPlayer player;

        private PortableJukeboxSoundInstance(SoundEvent soundEvent, LocalPlayer player) {
            super(soundEvent, SoundSource.RECORDS, RandomSource.create());
            this.player = player;
            this.volume = 4.0F;
            this.pitch = 1.0F;
            this.updatePosition();
        }

        @Override
        public boolean canPlaySound() {
            return !this.player.isRemoved() && !this.player.isSilent();
        }

        @Override
        public void tick() {
            if (this.player.isRemoved()) {
                this.stop();
                return;
            }
            this.updatePosition();
        }

        private boolean isBoundTo(LocalPlayer player) {
            return this.player == player && !this.player.isRemoved();
        }

        private void updatePosition() {
            this.x = this.player.getX();
            this.y = this.player.getY();
            this.z = this.player.getZ();
        }
    }
}
