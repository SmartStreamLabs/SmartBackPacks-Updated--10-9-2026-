package com.teamsmartstreamlabs.smartbackpacks.client;

import java.util.List;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.client.render.BackpackRenderLayer;
import com.teamsmartstreamlabs.smartbackpacks.client.render.MobBackpackRenderLayer;
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
import com.teamsmartstreamlabs.smartbackpacks.client.screen.SmartBackpacksConfigScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.SurvivalAssistUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.TorchPlacerUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.XpTransferUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.StorageControllerScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.StorageGuideScreen;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.block.BackpackBlock;
import com.teamsmartstreamlabs.smartbackpacks.blockentity.PlacedBackpackBlockEntity;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackSortMode;
import com.teamsmartstreamlabs.smartbackpacks.network.OpenWornBackpackPayload;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
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
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@Mod(value = SmartBackpacks.MOD_ID, dist = Dist.CLIENT)
public class SmartBackpacksClient {
    private static final int DEFAULT_BACKPACK_TINT = 0xFF7A5330;
    private static final String KEY_CATEGORY = "key.categories.smartbackpacks";
    private static final KeyMapping OPEN_WORN_BACKPACK = new KeyMapping("key.smartbackpacks.open_backpack", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, KEY_CATEGORY);
    private static final KeyMapping MANUAL_AUTO_TOOL_SWAP = new KeyMapping("key.smartbackpacks.auto_tool_swap", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, KEY_CATEGORY);
    private static final KeyMapping MANUAL_BUILDER_REFILL = new KeyMapping("key.smartbackpacks.builder_refill", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, KEY_CATEGORY);
    private static final KeyMapping QUICK_ACCESS_WHEEL = new KeyMapping("key.smartbackpacks.quick_access_wheel", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, KEY_CATEGORY);
    private static final int JUKEBOX_PARTICLE_INTERVAL = 10;
    private static final int FURNACE_PARTICLE_INTERVAL = 4;
    private static final int BLAST_FURNACE_PARTICLE_INTERVAL = 3;
    private static final int SMOKER_PARTICLE_INTERVAL = 4;
    private static final int CHUNK_LOADER_PARTICLE_INTERVAL = 6;
    private static SoundInstance portableJukeboxSound;
    private static boolean quickAccessWheelWasDown;

    public SmartBackpacksClient(IEventBus modEventBus) {
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (container, parent) -> new SmartBackpacksConfigScreen(parent));
        modEventBus.addListener(this::registerScreens);
        modEventBus.addListener(this::addLayers);
        modEventBus.addListener(this::registerKeyMappings);
        modEventBus.addListener(this::registerBackpackItemColors);
        modEventBus.addListener(this::registerBackpackBlockColors);
        modEventBus.addListener(this::onClientSetup);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(this::onMouseButton);
        NeoForge.EVENT_BUS.addListener(this::onRenderGui);
        NeoForge.EVENT_BUS.addListener(this::onRenderScreen);
    }

    private void registerBackpackItemColors(RegisterColorHandlersEvent.Item event) {
        Item[] backpackItems = BuiltInRegistries.ITEM.stream()
                .filter(BackpackItem.class::isInstance)
                .toArray(Item[]::new);
        event.register((stack, tintIndex) -> tintIndex == 0
                ? DyedItemColor.getOrDefault(stack, DEFAULT_BACKPACK_TINT)
                : -1, backpackItems);
    }

    private void registerBackpackBlockColors(RegisterColorHandlersEvent.Block event) {
        Block[] backpackBlocks = BuiltInRegistries.BLOCK.stream()
                .filter(BackpackBlock.class::isInstance)
                .toArray(Block[]::new);
        event.register((state, level, pos, tintIndex) -> {
            if (tintIndex == 0 && level != null && pos != null
                    && level.getBlockEntity(pos) instanceof PlacedBackpackBlockEntity blockEntity) {
                return DyedItemColor.getOrDefault(blockEntity.getStoredBackpack(), DEFAULT_BACKPACK_TINT);
            }
            return tintIndex == 0 ? DEFAULT_BACKPACK_TINT : -1;
        }, backpackBlocks);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> BuiltInRegistries.BLOCK.stream()
                .filter(BackpackBlock.class::isInstance)
                .forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout())));
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
        event.register(ModMenuTypes.BUILDER_UPGRADE.get(), BuilderUpgradeScreen::new);
        event.register(ModMenuTypes.TORCH_PLACER_UPGRADE.get(), TorchPlacerUpgradeScreen::new);
        event.register(ModMenuTypes.CAPACITY_WARNING_UPGRADE.get(), CapacityWarningUpgradeScreen::new);
        event.register(ModMenuTypes.BACKPACK_LINK_UPGRADE.get(), BackpackLinkUpgradeScreen::new);
        event.register(ModMenuTypes.STORAGE_CONTROLLER.get(), StorageControllerScreen::new);
    }

    private void onRenderGui(RenderGuiEvent.Post event) {
        CapacityWarningHud.render(event);
        PickupNotifierHud.render(event);
    }

    private void onRenderScreen(ScreenEvent.Render.Post event) {
        CapacityWarningHud.render(event.getGuiGraphics());
    }

    private void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skinModel : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skinModel);
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T extends LivingEntity> void addMobBackpackLayer(EntityRenderersEvent.AddLayers event,
                                                               EntityType<T> type,
                                                               boolean creeper) {
        if (event.getRenderer(type) instanceof LivingEntityRenderer renderer) {
            renderer.addLayer(new MobBackpackRenderLayer(renderer, creeper));
        }
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_WORN_BACKPACK);
        event.register(MANUAL_AUTO_TOOL_SWAP);
        event.register(MANUAL_BUILDER_REFILL);
        event.register(QUICK_ACCESS_WHEEL);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        StorageGuideScreen.openIfRequested(minecraft);
        boolean quickAccessWheelDown = QUICK_ACCESS_WHEEL.isDown();
        if (minecraft.player == null || minecraft.screen != null) {
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

    private void onMouseButton(InputEvent.MouseButton.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null
                && minecraft.screen == null
                && event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && event.getAction() == InputConstants.PRESS
                && !minecraft.player.isShiftKeyDown()
                && minecraft.hitResult instanceof BlockHitResult blockHitResult
                && blockHitResult.getType() == HitResult.Type.BLOCK) {
            PacketDistributor.sendToServer(new RequestAutoToolSwapPayload(blockHitResult.getBlockPos(), false));
        }

        if (minecraft.player != null
                && minecraft.screen == null
                && event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && event.getAction() == InputConstants.PRESS
                && minecraft.player.isShiftKeyDown()
                && !minecraft.player.isCreative()
                && minecraft.hitResult instanceof BlockHitResult blockHitResult
                && blockHitResult.getType() == HitResult.Type.BLOCK
                && minecraft.level != null
                && minecraft.level.getBlockState(blockHitResult.getBlockPos()).getBlock() instanceof BackpackBlock) {
            PacketDistributor.sendToServer(PickupPlacedBackpackPayload.INSTANCE);
            event.setCanceled(true);
            return;
        }

        if (minecraft.player != null
                && minecraft.screen == null
                && event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && event.getAction() == InputConstants.PRESS
                && minecraft.player.isShiftKeyDown()
                && minecraft.player.getMainHandItem().getItem() instanceof BackpackItem) {
            PacketDistributor.sendToServer(PlaceHeldBackpackPayload.INSTANCE);
            event.setCanceled(true);
            return;
        }

        if (!(minecraft.screen instanceof BackpackScreen backpackScreen) || event.getButton() != GLFW.GLFW_MOUSE_BUTTON_MIDDLE || event.getAction() != InputConstants.PRESS) {
            return;
        }

        BackpackSortMode sortMode = backpackScreen.getSortMode();
        PacketDistributor.sendToServer(new SortOpenBackpackPayload(sortMode));
        event.setCanceled(true);
    }

    public static void playPortableJukebox(ItemStack disc, Vec3 position) {
        Minecraft minecraft = Minecraft.getInstance();
        stopPortableJukebox();
        if (minecraft.level == null) {
            return;
        }

        JukeboxSong.fromStack(minecraft.level.registryAccess(), disc).ifPresent(song -> {
            SoundEvent soundEvent = song.value().soundEvent().value();
            portableJukeboxSound = SimpleSoundInstance.forJukeboxSong(soundEvent, position);
            minecraft.getSoundManager().play(portableJukeboxSound);
        });
    }

    public static void stopPortableJukebox() {
        Minecraft minecraft = Minecraft.getInstance();
        if (portableJukeboxSound != null) {
            minecraft.getSoundManager().stop(portableJukeboxSound);
            portableJukeboxSound = null;
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
