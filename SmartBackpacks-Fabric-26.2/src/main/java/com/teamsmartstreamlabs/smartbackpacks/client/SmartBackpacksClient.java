package com.teamsmartstreamlabs.smartbackpacks.client;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.block.BackpackBlock;
import com.teamsmartstreamlabs.smartbackpacks.blockentity.PlacedBackpackBlockEntity;
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
import com.teamsmartstreamlabs.smartbackpacks.client.screen.SurvivalAssistUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.TorchPlacerUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.XpTransferUpgradeScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.StorageControllerScreen;
import com.teamsmartstreamlabs.smartbackpacks.client.screen.StorageGuideScreen;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackSortMode;
import com.teamsmartstreamlabs.smartbackpacks.item.AutoSmeltingUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BlastFurnaceUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.ChunkLoaderUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.FurnaceUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.SmokerUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.network.OpenWornBackpackPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.PickupPlacedBackpackPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.PlaceHeldBackpackPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.RequestAutoToolSwapPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.RequestBuilderRefillPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.QuickAccessWheelRequestPayload;
import com.teamsmartstreamlabs.smartbackpacks.network.SortOpenBackpackPayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.AutoSmeltingUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BlastFurnaceUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FurnaceUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.JukeboxUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.QuickAccessWheelUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.SmokerUpgradeData;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class SmartBackpacksClient {
    private static final int DEFAULT_BACKPACK_TINT = 0xFF7A5330;
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "smartbackpacks"));
    private static final KeyMapping OPEN_WORN_BACKPACK = new KeyMapping("key.smartbackpacks.open_backpack", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, KEY_CATEGORY);
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

    private SmartBackpacksClient() {
    }

    public static void initClient() {
        ModPayloadsBridge.registerClient();
        registerBackpackBlockColors();
        registerScreens();
        registerBackpackRenderLayer();
        KeyMappingHelper.registerKeyMapping(OPEN_WORN_BACKPACK);
        KeyMappingHelper.registerKeyMapping(MANUAL_AUTO_TOOL_SWAP);
        KeyMappingHelper.registerKeyMapping(MANUAL_BUILDER_REFILL);
        KeyMappingHelper.registerKeyMapping(QUICK_ACCESS_WHEEL);
        ClientTickEvents.END_CLIENT_TICK.register(SmartBackpacksClient::onClientTick);
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "backpack_huds"), (extractor, tickDelta) -> {
            CapacityWarningHud.render(extractor);
            PickupNotifierHud.render(extractor);
        });
        registerMouseHooks();
        SmartBackpacks.LOGGER.info("Initializing {} Fabric client bootstrap", SmartBackpacks.MOD_ID);
    }

    private static void registerBackpackBlockColors() {
        Block[] backpackBlocks = BuiltInRegistries.BLOCK.stream()
                .filter(BackpackBlock.class::isInstance)
                .toArray(Block[]::new);
        BlockColorRegistry.register(List.of(new BlockTintSource() {
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

    private static void registerScreens() {
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
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerBackpackRenderLayer() {
        LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityRenderer instanceof AvatarRenderer<?> avatarRenderer) {
                registrationHelper.register(new BackpackRenderLayer((RenderLayerParent) avatarRenderer));
            }
            if (isMobBackpackType(entityType)) {
                registrationHelper.register(new MobBackpackRenderLayer((RenderLayerParent) entityRenderer,
                        entityType == EntityTypes.CREEPER));
            }
        });
    }

    private static boolean isMobBackpackType(EntityType<?> type) {
        return type == EntityTypes.ZOMBIE
                || type == EntityTypes.HUSK
                || type == EntityTypes.DROWNED
                || type == EntityTypes.SKELETON
                || type == EntityTypes.STRAY
                || type == EntityTypes.CREEPER
                || type == EntityTypes.PILLAGER
                || type == EntityTypes.VINDICATOR
                || type == EntityTypes.WITCH
                || type == EntityTypes.PIGLIN
                || type == EntityTypes.ZOMBIFIED_PIGLIN;
    }

    private static void registerMouseHooks() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!world.isClientSide() || hand != InteractionHand.MAIN_HAND || player != Minecraft.getInstance().player) {
                return InteractionResult.PASS;
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.gui.screen() != null) {
                return InteractionResult.PASS;
            }
            if (player.isShiftKeyDown() && !player.isCreative() && world.getBlockState(pos).getBlock() instanceof BackpackBlock) {
                PacketDistributor.sendToServer(PickupPlacedBackpackPayload.INSTANCE);
                return InteractionResult.FAIL;
            }
            if (player.isShiftKeyDown() && player.getMainHandItem().getItem() instanceof BackpackItem) {
                PacketDistributor.sendToServer(PlaceHeldBackpackPayload.INSTANCE);
                return InteractionResult.FAIL;
            }
            PacketDistributor.sendToServer(new RequestAutoToolSwapPayload(pos, false));
            return InteractionResult.PASS;
        });

        ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> {
            if (clickCount == 0 || client.gui.screen() != null || player == null || !(client.hitResult instanceof BlockHitResult blockHitResult)
                    || blockHitResult.getType() != HitResult.Type.BLOCK || !player.isShiftKeyDown()) {
                return false;
            }
            if (!player.isCreative() && client.level != null && client.level.getBlockState(blockHitResult.getBlockPos()).getBlock() instanceof BackpackBlock) {
                PacketDistributor.sendToServer(PickupPlacedBackpackPayload.INSTANCE);
                return true;
            }
            if (player.getMainHandItem().getItem() instanceof BackpackItem) {
                PacketDistributor.sendToServer(PlaceHeldBackpackPayload.INSTANCE);
                return true;
            }
            return false;
        });
    }

    private static void onClientTick(Minecraft minecraft) {
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
        if (minecraft.gui.screen() instanceof BackpackScreen backpackScreen
                && GLFW.glfwGetMouseButton(minecraft.getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS) {
            BackpackSortMode sortMode = backpackScreen.getSortMode();
            PacketDistributor.sendToServer(new SortOpenBackpackPayload(sortMode));
        }
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
        spawnBackpackParticle(minecraft, ParticleTypes.NOTE, 0.0D);
    }

    private static void spawnFurnaceBackpackParticles(Minecraft minecraft) {
        ItemStack backpack = getVisibleBackpack(minecraft.player);
        if (backpack.isEmpty() || !hasActiveFurnaceUpgrade(backpack)) {
            return;
        }
        spawnBackpackParticle(minecraft, ParticleTypes.SMOKE, 0.03D);
    }

    private static void spawnAutoSmeltingBackpackParticles(Minecraft minecraft) {
        ItemStack backpack = getVisibleBackpack(minecraft.player);
        if (backpack.isEmpty() || !hasActiveAutoSmeltingUpgrade(backpack)) {
            return;
        }
        spawnBackpackParticle(minecraft, ParticleTypes.SMOKE, 0.03D);
    }

    private static void spawnSmokerBackpackParticles(Minecraft minecraft) {
        ItemStack backpack = getVisibleBackpack(minecraft.player);
        if (backpack.isEmpty() || !hasActiveSmokerUpgrade(backpack)) {
            return;
        }
        spawnBackpackParticle(minecraft, ParticleTypes.CAMPFIRE_COSY_SMOKE, 0.025D);
    }

    private static void spawnBlastFurnaceBackpackParticles(Minecraft minecraft) {
        ItemStack backpack = getVisibleBackpack(minecraft.player);
        if (backpack.isEmpty() || !hasActiveBlastFurnaceUpgrade(backpack)) {
            return;
        }
        spawnBackpackParticle(minecraft, ParticleTypes.SMOKE, 0.04D);
    }

    private static void spawnChunkLoaderBackpackParticles(Minecraft minecraft) {
        ItemStack backpack = getVisibleBackpack(minecraft.player);
        if (backpack.isEmpty() || !hasChunkLoaderUpgrade(backpack)) {
            return;
        }
        spawnBackpackParticle(minecraft, ParticleTypes.PORTAL, 0.02D);
    }

    private static void spawnBackpackParticle(Minecraft minecraft, net.minecraft.core.particles.ParticleOptions particle, double upwardSpeed) {
        float bodyRot = minecraft.player.yBodyRot;
        double radians = Math.toRadians(bodyRot);
        double offsetX = -Mth.sin((float) radians) * 0.24D;
        double offsetZ = Mth.cos((float) radians) * 0.24D;
        double x = minecraft.player.getX() - offsetX;
        double y = minecraft.player.getY() + 0.98D;
        double z = minecraft.player.getZ() - offsetZ;
        minecraft.level.addParticle(particle, x, y, z, 0.0D, upwardSpeed, 0.0D);
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

    private static ItemStack getVisibleBackpack(LocalPlayer player) {
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
            if (!upgrade.isEmpty() && upgrade.getOrDefault(ModDataComponents.JUKEBOX_UPGRADE_DATA.get(), JukeboxUpgradeData.DEFAULT).playing()) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasActiveFurnaceUpgrade(ItemStack backpack) {
        for (ItemStack upgrade : BackpackStackData.loadUpgrades(backpack)) {
            if (upgrade.getItem() instanceof FurnaceUpgradeItem
                    && upgrade.getOrDefault(ModDataComponents.FURNACE_UPGRADE_DATA.get(), FurnaceUpgradeData.DEFAULT).litTime() > 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasActiveAutoSmeltingUpgrade(ItemStack backpack) {
        for (ItemStack upgrade : BackpackStackData.loadUpgrades(backpack)) {
            if (upgrade.getItem() instanceof AutoSmeltingUpgradeItem
                    && upgrade.getOrDefault(ModDataComponents.AUTO_SMELTING_UPGRADE_DATA.get(), AutoSmeltingUpgradeData.DEFAULT).litTime() > 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasActiveSmokerUpgrade(ItemStack backpack) {
        for (ItemStack upgrade : BackpackStackData.loadUpgrades(backpack)) {
            if (upgrade.getItem() instanceof SmokerUpgradeItem
                    && upgrade.getOrDefault(ModDataComponents.SMOKER_UPGRADE_DATA.get(), SmokerUpgradeData.DEFAULT).litTime() > 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasActiveBlastFurnaceUpgrade(ItemStack backpack) {
        for (ItemStack upgrade : BackpackStackData.loadUpgrades(backpack)) {
            if (upgrade.getItem() instanceof BlastFurnaceUpgradeItem
                    && upgrade.getOrDefault(ModDataComponents.BLAST_FURNACE_UPGRADE_DATA.get(), BlastFurnaceUpgradeData.DEFAULT).litTime() > 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasChunkLoaderUpgrade(ItemStack backpack) {
        for (ItemStack upgrade : BackpackStackData.loadUpgrades(backpack)) {
            if (upgrade.getItem() instanceof ChunkLoaderUpgradeItem) {
                return true;
            }
        }
        return false;
    }

    private static final class ModPayloadsBridge {
        private static void registerClient() {
            com.teamsmartstreamlabs.smartbackpacks.network.ModPayloads.registerClient();
        }
    }
}
