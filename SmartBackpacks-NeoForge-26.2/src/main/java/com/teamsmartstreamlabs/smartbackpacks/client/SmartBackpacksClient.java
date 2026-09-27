package com.teamsmartstreamlabs.smartbackpacks.client;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackContentPreview;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

import java.util.List;
import java.util.function.BiConsumer;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.client.render.BackpackRenderLayer;
import com.teamsmartstreamlabs.smartbackpacks.client.render.MobBackpackRenderData;
import com.teamsmartstreamlabs.smartbackpacks.client.render.MobBackpackRenderLayer;
import com.teamsmartstreamlabs.smartbackpacks.mobbackpack.MobBackpackClientData;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.BackpackScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.AutoFeedUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.BackpackLinkUpgradeScreen;
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
import com.teamsmartstreamlabs.smartbackpacks.client.screen.DeathEmergencyKitUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.SmartBackpacksConfigScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.SurvivalAssistUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.TorchPlacerUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.XpTransferUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.StorageControllerScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.StorageTransferScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.BackpackWorkbenchScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.StorageGuideScreen;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.block.BackpackBlock;
import com.teamsmartstreamlabs.smartbackpacks.blockentity.PlacedBackpackBlockEntity;
import com.teamsmartstreamlabs.smartbackpacks.client.render.BackpackDisplayHookRenderer;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModBlockEntities;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackSortMode;
import com.teamsmartstreamlabs.smartbackpacks.network.OpenWornBackpackPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.OpenStorageMonitorPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.PlaceHeldBackpackPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.PickupPlacedBackpackPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.RequestAutoToolSwapPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.RequestBuilderRefillPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.QuickAccessWheelRequestPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.SortOpenBackpackPayload;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.AutoSmeltingUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BlastFurnaceUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.ChunkLoaderUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.FurnaceUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.SmokerUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BlastFurnaceUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.AutoSmeltingUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FurnaceUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.JukeboxUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.QuickAccessWheelUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.SmokerUpgradeData;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import org.lwjgl.glfw.GLFW;

@Mod(value = SmartBackpacks.MOD_ID, dist = Dist.CLIENT)
public class SmartBackpacksClient {
    private static final int DEFAULT_BACKPACK_TINT = 0xFF7A5330;
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "smartbackpacks"));
    private static final KeyMapping OPEN_WORN_BACKPACK = new KeyMapping("key.smartbackpacks.open_backpack", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, KEY_CATEGORY);
    private static final KeyMapping OPEN_STORAGE_MONITOR = new KeyMapping("key.smartbackpacks.open_storage_monitor", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, KEY_CATEGORY);
    private static final KeyMapping MANUAL_AUTO_TOOL_SWAP = new KeyMapping("key.smartbackpacks.auto_tool_swap", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, KEY_CATEGORY);
    private static final KeyMapping MANUAL_BUILDER_REFILL = new KeyMapping("key.smartbackpacks.builder_refill", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, KEY_CATEGORY);
    private static final KeyMapping QUICK_ACCESS_WHEEL = new KeyMapping("key.smartbackpacks.quick_access_wheel", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, KEY_CATEGORY);
    private static final int JUKEBOX_PARTICLE_INTERVAL = 10;
    private static final int FURNACE_PARTICLE_INTERVAL = 4;
    private static final int BLAST_FURNACE_PARTICLE_INTERVAL = 3;
    private static final int SMOKER_PARTICLE_INTERVAL = 4;
    private static final int CHUNK_LOADER_PARTICLE_INTERVAL = 6;
    private static EntityBoundSoundInstance portableJukeboxSound;
    private static LocalPlayer portableJukeboxPlayer;
    private static boolean quickAccessWheelWasDown;

    public SmartBackpacksClient(IEventBus modEventBus) {
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (container, parent) -> new SmartBackpacksConfigScreen(parent));
        modEventBus.addListener(this::registerScreens);
        modEventBus.addListener(this::registerDisplayHookRenderer);
        modEventBus.addListener(this::registerPreviewTooltip);
        BackpackItem.setPreviewShiftDown(() -> InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT));
        modEventBus.addListener(this::addLayers);
        modEventBus.addListener(this::registerMobBackpackRenderData);
        modEventBus.addListener(this::registerKeyMappings);
        modEventBus.addListener(this::registerBackpackBlockColors);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(this::onMouseButton);
        NeoForge.EVENT_BUS.addListener(this::onRenderGui);
        NeoForge.EVENT_BUS.addListener(this::onRenderScreen);
    }

    private void registerBackpackBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        Block[] backpackBlocks = BuiltInRegistries.BLOCK.stream()
                .filter(BackpackBlock.class::isInstance)
                .toArray(Block[]::new);
        event.register(List.of(new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return DEFAULT_BACKPACK_TINT;
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                if (level.getBlockEntity(pos) instanceof PlacedBackpackBlockEntity blockEntity) {
                    return DyedItemColor.getOrDefault(blockEntity.getStoredBackpack(), DEFAULT_BACKPACK_TINT);
                }
                return DEFAULT_BACKPACK_TINT;
            }
        }), backpackBlocks);
    }

    private void registerPreviewTooltip(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(BackpackContentPreview.class, BackpackPreviewTooltip::new);
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.BACKPACK.get(), BackpackScreen::new);
        event.register(ModMenuTypes.MAGNET_UPGRADE.get(), MagnetUpgradeScreen::new);
        event.register(ModMenuTypes.AUTO_FEED_UPGRADE.get(), AutoFeedUpgradeScreen::new);
        event.register(ModMenuTypes.SURVIVAL_ASSIST_UPGRADE.get(), SurvivalAssistUpgradeScreen::new);
        event.register(ModMenuTypes.FLUID_STORAGE_UPGRADE.get(), FluidStorageUpgradeScreen::new);
        event.register(ModMenuTypes.FLUID_TRANSFER_UPGRADE.get(), FluidTransferUpgradeScreen::new);
        event.register(ModMenuTypes.CAPACITOR_UPGRADE.get(), CapacitorUpgradeScreen::new);
        event.register(ModMenuTypes.CHUNK_LOADER_UPGRADE.get(), ChunkLoaderUpgradeScreen::new);
        event.register(ModMenuTypes.JUKEBOX_UPGRADE.get(), JukeboxUpgradeScreen::new);
        event.register(ModMenuTypes.AUTO_SMELTING_UPGRADE.get(), FurnaceScreen::new);
        event.register(ModMenuTypes.XP_TRANSFER_UPGRADE.get(), XpTransferUpgradeScreen::new);
        event.register(ModMenuTypes.QUIVER_UPGRADE.get(), QuiverUpgradeScreen::new);
        event.register(ModMenuTypes.QUICK_ACCESS_WHEEL_UPGRADE.get(), QuickAccessWheelUpgradeScreen::new);
        event.register(ModMenuTypes.RESCUE_UPGRADE.get(), RescueUpgradeScreen::new);
        event.register(ModMenuTypes.DEATH_EMERGENCY_KIT_UPGRADE.get(), DeathEmergencyKitUpgradeScreen::new);
        event.register(ModMenuTypes.BUILDER_UPGRADE.get(), BuilderUpgradeScreen::new);
        event.register(ModMenuTypes.TORCH_PLACER_UPGRADE.get(), TorchPlacerUpgradeScreen::new);
        event.register(ModMenuTypes.CAPACITY_WARNING_UPGRADE.get(), CapacityWarningUpgradeScreen::new);
        event.register(ModMenuTypes.BACKPACK_LINK_UPGRADE.get(), BackpackLinkUpgradeScreen::new);
        event.register(ModMenuTypes.STORAGE_CONTROLLER.get(), StorageControllerScreen::new);
        event.register(ModMenuTypes.STORAGE_TRANSFER.get(), StorageTransferScreen::new);
        event.register(ModMenuTypes.BACKPACK_WORKBENCH.get(), BackpackWorkbenchScreen::new);
    }

    private void registerDisplayHookRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.BACKPACK_DISPLAY_HOOK.get(), BackpackDisplayHookRenderer::new);
    }

    private void onRenderGui(RenderGuiEvent.Post event) {
        CapacityWarningHud.render(event);
        PickupNotifierHud.render(event);
    }

    private void onRenderScreen(ScreenEvent.Render.Post event) {
        CapacityWarningHud.render(event.getGuiGraphics());
        PickupNotifierHud.render(event.getGuiGraphics());
    }

    private void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerModelType skinModel : event.getSkins()) {
            AvatarRenderer<?> renderer = event.getPlayerRenderer(skinModel);
            if (renderer != null) {
                renderer.addLayer(new BackpackRenderLayer(renderer));
            }
        }

        addMobBackpackLayer(event, EntityTypes.ZOMBIE, false);
        addMobBackpackLayer(event, EntityTypes.HUSK, false);
        addMobBackpackLayer(event, EntityTypes.DROWNED, false);
        addMobBackpackLayer(event, EntityTypes.SKELETON, false);
        addMobBackpackLayer(event, EntityTypes.STRAY, false);
        addMobBackpackLayer(event, EntityTypes.CREEPER, true);
        addMobBackpackLayer(event, EntityTypes.PILLAGER, false);
        addMobBackpackLayer(event, EntityTypes.VINDICATOR, false);
        addMobBackpackLayer(event, EntityTypes.WITCH, false);
        addMobBackpackLayer(event, EntityTypes.PIGLIN, false);
        addMobBackpackLayer(event, EntityTypes.ZOMBIFIED_PIGLIN, false);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T extends LivingEntity> void addMobBackpackLayer(EntityRenderersEvent.AddLayers event,
                                                               EntityType<T> type,
                                                               boolean creeper) {
        if (event.getRenderer(type) instanceof LivingEntityRenderer renderer) {
            renderer.addLayer(new MobBackpackRenderLayer(renderer, creeper));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void registerMobBackpackRenderData(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier((Class) LivingEntityRenderer.class,
                (BiConsumer<LivingEntity, LivingEntityRenderState>) (entity, state) ->
                        state.setRenderData(MobBackpackRenderData.BACKPACK,
                                MobBackpackClientData.get(entity.getId())));
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_WORN_BACKPACK);
        event.register(OPEN_STORAGE_MONITOR);
        event.register(MANUAL_AUTO_TOOL_SWAP);
        event.register(MANUAL_BUILDER_REFILL);
        event.register(QUICK_ACCESS_WHEEL);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        StorageGuideScreen.openIfRequested(minecraft);
        updatePortableJukebox(minecraft);
        boolean quickAccessWheelDown = QUICK_ACCESS_WHEEL.isDown();
        if (minecraft.player == null || minecraft.gui.screen() != null) {
            quickAccessWheelWasDown = quickAccessWheelDown;
            return;
        }

        if (minecraft.level != null && minecraft.player.tickCount % JUKEBOX_PARTICLE_INTERVAL == 0) {
            spawnJukeboxBackpackParticles(minecraft);
        }
        if (minecraft.level != null && minecraft.player.tickCount % FURNACE_PARTICLE_INTERVAL == 0) {
            spawnFurnaceBackpackParticles(minecraft);
            spawnAutoSmeltingBackpackParticles(minecraft);
        }
        if (minecraft.level != null && minecraft.player.tickCount % BLAST_FURNACE_PARTICLE_INTERVAL == 0) {
            spawnBlastFurnaceBackpackParticles(minecraft);
        }
        if (minecraft.level != null && minecraft.player.tickCount % SMOKER_PARTICLE_INTERVAL == 0) {
            spawnSmokerBackpackParticles(minecraft);
        }
        if (minecraft.level != null && minecraft.player.tickCount % CHUNK_LOADER_PARTICLE_INTERVAL == 0) {
            spawnChunkLoaderBackpackParticles(minecraft);
        }

        boolean backpackPressed = false;
        while (OPEN_WORN_BACKPACK.consumeClick()) {
            backpackPressed = true;
        }
        boolean monitorPressed = false;
        while (OPEN_STORAGE_MONITOR.consumeClick()) {
            monitorPressed = true;
        }
        if (backpackPressed && (!monitorPressed || BackpackItem.isBackpack(getVisibleBackpack(minecraft.player)))) {
            ClientPacketDistributor.sendToServer(OpenWornBackpackPayload.INSTANCE);
        } else if (monitorPressed) {
            ClientPacketDistributor.sendToServer(OpenStorageMonitorPayload.INSTANCE);
        }

        while (MANUAL_AUTO_TOOL_SWAP.consumeClick()) {
            if (minecraft.hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getType() == HitResult.Type.BLOCK) {
                ClientPacketDistributor.sendToServer(new RequestAutoToolSwapPayload(blockHitResult.getBlockPos(), true));
            }
        }

        while (MANUAL_BUILDER_REFILL.consumeClick()) {
            ClientPacketDistributor.sendToServer(RequestBuilderRefillPayload.INSTANCE);
        }

        if (quickAccessWheelDown && !quickAccessWheelWasDown) {
            ClientPacketDistributor.sendToServer(QuickAccessWheelRequestPayload.INSTANCE);
        }
        quickAccessWheelWasDown = quickAccessWheelDown;
    }

    public static boolean isQuickAccessWheelKeyDown() {
        return QUICK_ACCESS_WHEEL.isDown();
    }

    public static boolean matchesQuickAccessWheel(KeyEvent event) {
        return QUICK_ACCESS_WHEEL.matches(event);
    }

    public static void openQuickAccessWheel(List<ItemStack> configured, int availableMask) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.gui.screen() != null) {
            return;
        }
        ItemStack[] favorites = new ItemStack[QuickAccessWheelUpgradeData.FAVORITE_COUNT];
        boolean[] available = new boolean[QuickAccessWheelUpgradeData.FAVORITE_COUNT];
        for (int slot = 0; slot < favorites.length; slot++) {
            favorites[slot] = configured.get(slot).copy();
            available[slot] = (availableMask & 1 << slot) != 0;
        }
        minecraft.gui.setScreen(new QuickAccessWheelScreen(favorites, available));
    }

    private void onMouseButton(InputEvent.MouseButton.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null
                && minecraft.gui.screen() == null
                && event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && event.getAction() == InputConstants.PRESS
                && !minecraft.player.isShiftKeyDown()
                && minecraft.hitResult instanceof BlockHitResult blockHitResult
                && blockHitResult.getType() == HitResult.Type.BLOCK) {
            ClientPacketDistributor.sendToServer(new RequestAutoToolSwapPayload(blockHitResult.getBlockPos(), false));
        }

        if (minecraft.player != null
                && minecraft.gui.screen() == null
                && event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && event.getAction() == InputConstants.PRESS
                && minecraft.player.isShiftKeyDown()
                && !minecraft.player.isCreative()
                && minecraft.hitResult instanceof BlockHitResult blockHitResult
                && blockHitResult.getType() == HitResult.Type.BLOCK
                && minecraft.level != null
                && minecraft.level.getBlockState(blockHitResult.getBlockPos()).getBlock() instanceof BackpackBlock) {
            ClientPacketDistributor.sendToServer(PickupPlacedBackpackPayload.INSTANCE);
            event.setCanceled(true);
            return;
        }

        if (minecraft.player != null
                && minecraft.gui.screen() == null
                && event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && event.getAction() == InputConstants.PRESS
                && minecraft.player.isShiftKeyDown()
                && minecraft.player.getMainHandItem().getItem() instanceof BackpackItem) {
            ClientPacketDistributor.sendToServer(PlaceHeldBackpackPayload.INSTANCE);
            event.setCanceled(true);
            return;
        }

        if (!(minecraft.gui.screen() instanceof BackpackScreen backpackScreen) || event.getButton() != GLFW.GLFW_MOUSE_BUTTON_MIDDLE || event.getAction() != InputConstants.PRESS) {
            return;
        }

        BackpackSortMode sortMode = backpackScreen.getSortMode();
        ClientPacketDistributor.sendToServer(new SortOpenBackpackPayload(sortMode));
        event.setCanceled(true);
    }

    public static void playPortableJukebox(ItemStack disc) {
        Minecraft minecraft = Minecraft.getInstance();
        stopPortableJukebox();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        JukeboxSong.fromStack(disc).ifPresent(song -> {
            SoundEvent soundEvent = song.value().soundEvent().value();
            portableJukeboxPlayer = minecraft.player;
            portableJukeboxSound = new EntityBoundSoundInstance(
                    soundEvent,
                    SoundSource.RECORDS,
                    4.0F,
                    1.0F,
                    portableJukeboxPlayer,
                    SoundInstance.createUnseededRandom().nextLong()
            );
            minecraft.getSoundManager().play(portableJukeboxSound);
        });
    }

    public static void stopPortableJukebox() {
        Minecraft minecraft = Minecraft.getInstance();
        if (portableJukeboxSound != null) {
            minecraft.getSoundManager().stop(portableJukeboxSound);
            portableJukeboxSound = null;
        }
        portableJukeboxPlayer = null;
    }

    private static void updatePortableJukebox(Minecraft minecraft) {
        if (portableJukeboxSound == null) {
            return;
        }

        if (portableJukeboxSound.isStopped()
                || minecraft.player == null
                || minecraft.player != portableJukeboxPlayer
                || portableJukeboxPlayer == null
                || portableJukeboxPlayer.isRemoved()) {
            stopPortableJukebox();
        }
    }

    private static void spawnJukeboxBackpackParticles(Minecraft minecraft) {
        ItemStack backpack = getVisibleBackpack(minecraft.player);
        if (backpack.isEmpty() || !hasPlayingJukeboxUpgrade(backpack)) {
            return;
        }

        float bodyRot = minecraft.player.yBodyRot;
        double radians = Math.toRadians(bodyRot);
        double offsetX = -Mth.sin((float) radians) * 0.28D;
        double offsetZ = Mth.cos((float) radians) * 0.28D;
        double x = minecraft.player.getX() - offsetX;
        double y = minecraft.player.getY() + 1.15D;
        double z = minecraft.player.getZ() - offsetZ;
        var random = minecraft.player.level().getRandom();
        double noteColor = random.nextDouble();
        minecraft.level.addParticle(ParticleTypes.NOTE, x, y, z, noteColor, 0.0D, 0.0D);
    }

    private static void spawnFurnaceBackpackParticles(Minecraft minecraft) {
        ItemStack backpack = getVisibleBackpack(minecraft.player);
        if (backpack.isEmpty() || !hasActiveFurnaceUpgrade(backpack)) {
            return;
        }

        float bodyRot = minecraft.player.yBodyRot;
        double radians = Math.toRadians(bodyRot);
        double offsetX = -Mth.sin((float) radians) * 0.24D;
        double offsetZ = Mth.cos((float) radians) * 0.24D;
        double x = minecraft.player.getX() - offsetX;
        double y = minecraft.player.getY() + 0.95D;
        double z = minecraft.player.getZ() - offsetZ;
        minecraft.level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.03D, 0.0D);
        if (minecraft.player.level().getRandom().nextInt(3) == 0) {
            minecraft.level.addParticle(ParticleTypes.FLAME, x, y - 0.02D, z, 0.0D, 0.01D, 0.0D);
        }
    }

    private static void spawnAutoSmeltingBackpackParticles(Minecraft minecraft) {
        ItemStack backpack = getVisibleBackpack(minecraft.player);
        if (backpack.isEmpty() || !hasActiveAutoSmeltingUpgrade(backpack)) {
            return;
        }

        float bodyRot = minecraft.player.yBodyRot;
        double radians = Math.toRadians(bodyRot);
        double offsetX = -Mth.sin((float) radians) * 0.24D;
        double offsetZ = Mth.cos((float) radians) * 0.24D;
        double x = minecraft.player.getX() - offsetX;
        double y = minecraft.player.getY() + 0.95D;
        double z = minecraft.player.getZ() - offsetZ;
        minecraft.level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.03D, 0.0D);
        if (minecraft.player.level().getRandom().nextInt(3) == 0) {
            minecraft.level.addParticle(ParticleTypes.FLAME, x, y - 0.02D, z, 0.0D, 0.01D, 0.0D);
        }
    }

    private static void spawnSmokerBackpackParticles(Minecraft minecraft) {
        ItemStack backpack = getVisibleBackpack(minecraft.player);
        if (backpack.isEmpty() || !hasActiveSmokerUpgrade(backpack)) {
            return;
        }

        float bodyRot = minecraft.player.yBodyRot;
        double radians = Math.toRadians(bodyRot);
        double offsetX = -Mth.sin((float) radians) * 0.24D;
        double offsetZ = Mth.cos((float) radians) * 0.24D;
        double x = minecraft.player.getX() - offsetX;
        double y = minecraft.player.getY() + 0.98D;
        double z = minecraft.player.getZ() - offsetZ;
        minecraft.level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 0.0D, 0.025D, 0.0D);
        if (minecraft.player.level().getRandom().nextInt(4) == 0) {
            minecraft.level.addParticle(ParticleTypes.SMOKE, x, y - 0.02D, z, 0.0D, 0.01D, 0.0D);
        }
    }

    private static void spawnBlastFurnaceBackpackParticles(Minecraft minecraft) {
        ItemStack backpack = getVisibleBackpack(minecraft.player);
        if (backpack.isEmpty() || !hasActiveBlastFurnaceUpgrade(backpack)) {
            return;
        }

        float bodyRot = minecraft.player.yBodyRot;
        double radians = Math.toRadians(bodyRot);
        double offsetX = -Mth.sin((float) radians) * 0.24D;
        double offsetZ = Mth.cos((float) radians) * 0.24D;
        double x = minecraft.player.getX() - offsetX;
        double y = minecraft.player.getY() + 0.95D;
        double z = minecraft.player.getZ() - offsetZ;
        minecraft.level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.04D, 0.0D);
        minecraft.level.addParticle(ParticleTypes.SMALL_FLAME, x, y - 0.01D, z, 0.0D, 0.015D, 0.0D);
    }

    private static void spawnChunkLoaderBackpackParticles(Minecraft minecraft) {
        ItemStack backpack = getVisibleBackpack(minecraft.player);
        if (backpack.isEmpty() || !hasChunkLoaderUpgrade(backpack)) {
            return;
        }

        float bodyRot = minecraft.player.yBodyRot;
        double radians = Math.toRadians(bodyRot);
        double offsetX = -Mth.sin((float) radians) * 0.27D;
        double offsetZ = Mth.cos((float) radians) * 0.27D;
        var random = minecraft.player.level().getRandom();
        double x = minecraft.player.getX() - offsetX + (random.nextDouble() - 0.5D) * 0.08D;
        double y = minecraft.player.getY() + 0.98D + random.nextDouble() * 0.14D;
        double z = minecraft.player.getZ() - offsetZ + (random.nextDouble() - 0.5D) * 0.08D;
        double velocityX = (random.nextDouble() - 0.5D) * 0.02D;
        double velocityY = 0.01D + random.nextDouble() * 0.02D;
        double velocityZ = (random.nextDouble() - 0.5D) * 0.02D;
        minecraft.level.addParticle(ParticleTypes.PORTAL, x, y, z, velocityX, velocityY, velocityZ);
    }

    private static ItemStack getVisibleBackpack(net.minecraft.client.player.LocalPlayer player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (BackpackItem.isBackpack(chest)) {
            return chest;
        }

        return CuriosCompat.findFirstMatchingBackStack(player, BackpackItem::isBackpack)
                .map(CuriosCompat.BackSlotMatch::stack)
                .filter(BackpackItem::isBackpack)
                .orElse(ItemStack.EMPTY);
    }

    private static boolean hasPlayingJukeboxUpgrade(ItemStack backpack) {
        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        for (ItemStack upgrade : upgrades) {
            if (upgrade.isEmpty()) {
                continue;
            }

            JukeboxUpgradeData data = upgrade.getOrDefault(ModDataComponents.JUKEBOX_UPGRADE_DATA.get(), JukeboxUpgradeData.DEFAULT);
            if (data.playing()) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasActiveFurnaceUpgrade(ItemStack backpack) {
        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        for (ItemStack upgrade : upgrades) {
            if (!(upgrade.getItem() instanceof FurnaceUpgradeItem)) {
                continue;
            }

            FurnaceUpgradeData data = upgrade.getOrDefault(ModDataComponents.FURNACE_UPGRADE_DATA.get(), FurnaceUpgradeData.DEFAULT);
            if (data.litTime() > 0) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasActiveAutoSmeltingUpgrade(ItemStack backpack) {
        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        for (ItemStack upgrade : upgrades) {
            if (!(upgrade.getItem() instanceof AutoSmeltingUpgradeItem)) {
                continue;
            }

            AutoSmeltingUpgradeData data = upgrade.getOrDefault(ModDataComponents.AUTO_SMELTING_UPGRADE_DATA.get(), AutoSmeltingUpgradeData.DEFAULT);
            if (data.litTime() > 0) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasActiveSmokerUpgrade(ItemStack backpack) {
        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        for (ItemStack upgrade : upgrades) {
            if (!(upgrade.getItem() instanceof SmokerUpgradeItem)) {
                continue;
            }

            SmokerUpgradeData data = upgrade.getOrDefault(ModDataComponents.SMOKER_UPGRADE_DATA.get(), SmokerUpgradeData.DEFAULT);
            if (data.litTime() > 0) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasActiveBlastFurnaceUpgrade(ItemStack backpack) {
        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        for (ItemStack upgrade : upgrades) {
            if (!(upgrade.getItem() instanceof BlastFurnaceUpgradeItem)) {
                continue;
            }

            BlastFurnaceUpgradeData data = upgrade.getOrDefault(ModDataComponents.BLAST_FURNACE_UPGRADE_DATA.get(), BlastFurnaceUpgradeData.DEFAULT);
            if (data.litTime() > 0) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasChunkLoaderUpgrade(ItemStack backpack) {
        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        for (ItemStack upgrade : upgrades) {
            if (upgrade.getItem() instanceof ChunkLoaderUpgradeItem) {
                return true;
            }
        }

        return false;
    }
}
