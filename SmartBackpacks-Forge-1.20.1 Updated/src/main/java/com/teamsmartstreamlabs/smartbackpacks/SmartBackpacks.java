package com.teamsmartstreamlabs.smartbackpacks;

import com.mojang.logging.LogUtils;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackCraftingHandler;
import com.teamsmartstreamlabs.smartbackpacks.client.SmartBackpacksClient;
import com.teamsmartstreamlabs.smartbackpacks.loot.ModLootEvents;
import com.teamsmartstreamlabs.smartbackpacks.mobbackpack.MobBackpackCommands;
import com.teamsmartstreamlabs.smartbackpacks.mobbackpack.MobBackpackHandler;
import com.teamsmartstreamlabs.smartbackpacks.network.ModPayloads;
import com.teamsmartstreamlabs.smartbackpacks.pickup.PickupNotifierServer;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModBlockEntities;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModBlocks;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModCreativeTabs;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.AutoFeedUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.AutoSmeltingUpgradeMenuHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BlastFurnaceUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BrewingStandUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BuilderUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacityWarningUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CourierUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CompressionUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.DepositUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FurnaceUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.LightUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.MagnetUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.PickupUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.QuiverUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.RescueUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.RestockUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.SmokerUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.SoulboundUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.SurvivalAssistUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.TorchPlacerUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.VoidUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.VoidboundUpgradeHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingGetProjectileEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(SmartBackpacks.MOD_ID)
public final class SmartBackpacks {
    public static final String MOD_ID = "smartbackpacks";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SmartBackpacks() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SmartBackpacksConfig.SPEC, "smartbackpacksconfig.toml");

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModPayloads.register(modEventBus);

        MinecraftForge.EVENT_BUS.addListener(this::onItemCrafted);
        MinecraftForge.EVENT_BUS.addListener(ModLootEvents::onLootTableLoad);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(this::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(this::onBlockPlaced);
        MinecraftForge.EVENT_BUS.addListener(this::onItemPickup);
        MinecraftForge.EVENT_BUS.addListener(this::onLivingGetProjectile);
        MinecraftForge.EVENT_BUS.addListener(this::onLivingDamage);
        MinecraftForge.EVENT_BUS.addListener(this::onLivingDeath);
        MinecraftForge.EVENT_BUS.addListener(this::onLivingDrops);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerRespawn);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedOut);
        MinecraftForge.EVENT_BUS.addListener(this::onEntityJoinLevel);
        MinecraftForge.EVENT_BUS.addListener(this::onItemExpire);
        MinecraftForge.EVENT_BUS.addListener(this::onLevelTick);
        MinecraftForge.EVENT_BUS.addListener(this::onExplosionDetonate);
        MinecraftForge.EVENT_BUS.addListener(MobBackpackHandler::onSpawnPositionCheck);
        MinecraftForge.EVENT_BUS.addListener(MobBackpackHandler::onStartTracking);
        MinecraftForge.EVENT_BUS.addListener(MobBackpackHandler::onLivingDrops);
        MinecraftForge.EVENT_BUS.addListener(MobBackpackCommands::onRegisterCommands);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SmartBackpacksClient.init(modEventBus));
        LOGGER.info("Initializing {} Forge 1.20.1 bootstrap", MOD_ID);
    }

    private void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        BackpackCraftingHandler.onItemCrafted(new net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent(
                event.getEntity(),
                event.getCrafting(),
                event.getInventory()
        ));
    }

    private void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }

        net.neoforged.neoforge.event.tick.PlayerTickEvent.Post wrapped = new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player);
        AutoSmeltingUpgradeMenuHandler.onPlayerTick(wrapped);
        BlastFurnaceUpgradeHandler.onPlayerTick(wrapped);
        AutoFeedUpgradeHandler.onPlayerTick(wrapped);
        BrewingStandUpgradeHandler.onPlayerTick(wrapped);
        FurnaceUpgradeHandler.onPlayerTick(wrapped);
        CompressionUpgradeHandler.onPlayerTick(wrapped);
        SmokerUpgradeHandler.onPlayerTick(wrapped);
        SurvivalAssistUpgradeHandler.onPlayerTick(wrapped);
        LightUpgradeHandler.onPlayerTick(wrapped);
        MagnetUpgradeHandler.onPlayerTick(wrapped);
        PickupUpgradeHandler.onPlayerTick(wrapped);
        QuiverUpgradeHandler.onPlayerTick(wrapped);
        RescueUpgradeHandler.onPlayerTick(wrapped);
        BuilderUpgradeHandler.onPlayerTick(wrapped);
        TorchPlacerUpgradeHandler.onPlayerTick(wrapped);
        CapacityWarningUpgradeHandler.onPlayerTick(wrapped);
        CourierUpgradeHandler.onPlayerTick(wrapped);
        VoidUpgradeHandler.onPlayerTick(wrapped);
        PickupNotifierServer.flush(player);
    }

    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock wrapped =
                new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock(player, event.getLevel(), event.getPos(), event.getFace(), event.getHand());
        BuilderUpgradeHandler.onRightClickBlock(wrapped);
        CourierUpgradeHandler.onRightClickBlock(wrapped);
        if (!wrapped.isCanceled()) {
            RestockUpgradeHandler.onRightClickBlock(wrapped);
        }
        if (!wrapped.isCanceled()) {
            DepositUpgradeHandler.onRightClickBlock(wrapped);
        }
        if (wrapped.isCanceled()) {
            event.setCanceled(true);
            event.setCancellationResult(wrapped.getCancellationResult());
        }
    }

    private void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        BuilderUpgradeHandler.onBlockPlaced(new net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent(
                event.getEntity(),
                event.getPlacedBlock()
        ));
    }

    private void onItemPickup(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }

        ItemStack original = event.getItem().getItem().copy();
        ItemStack remaining = PickupUpgradeHandler.moveIntoPickupBackpacks(player, original.copy());
        remaining = MagnetUpgradeHandler.moveIntoMagnetBackpacks(player, remaining);
        if (remaining.getCount() == original.getCount()) {
            return;
        }

        if (remaining.isEmpty()) {
            event.getItem().discard();
            event.setCanceled(true);
        } else {
            event.getItem().setItem(remaining);
        }
    }

    private void onLivingDrops(LivingDropsEvent event) {
        SoulboundUpgradeHandler.onLivingDrops(new net.neoforged.neoforge.event.entity.living.LivingDropsEvent(
                event.getEntity(),
                event.getDrops()
        ));
    }

    private void onLivingGetProjectile(LivingGetProjectileEvent event) {
        net.neoforged.neoforge.event.entity.living.LivingGetProjectileEvent wrapped =
                new net.neoforged.neoforge.event.entity.living.LivingGetProjectileEvent(
                        event.getEntity(),
                        event.getProjectileWeaponItemStack(),
                        event.getProjectileItemStack()
                );
        QuiverUpgradeHandler.onLivingGetProjectile(wrapped);
        event.setProjectileItemStack(wrapped.getProjectileItemStack());
    }

    private void onLivingDamage(LivingDamageEvent event) {
        net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre wrapped =
                new net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre(
                        event.getEntity(),
                        event.getSource(),
                        event.getAmount()
                );
        RescueUpgradeHandler.onLivingDamagePre(wrapped);
        event.setAmount(wrapped.getNewDamage());
    }

    private void onLivingDeath(LivingDeathEvent event) {
        net.neoforged.neoforge.event.entity.living.LivingDeathEvent wrapped =
                new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(
                        event.getEntity(),
                        event.getSource()
                );
        RescueUpgradeHandler.onLivingDeath(wrapped);
        if (wrapped.isCanceled()) {
            event.setCanceled(true);
        }
    }

    private void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        SoulboundUpgradeHandler.onPlayerRespawn(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent(event.getEntity()));
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!SmartBackpacksConfig.showWelcomeMessage()) {
            return;
        }

        Component discordLink = Component.literal("https://discord.gg/bMKwxnaFQw")
                .setStyle(Style.EMPTY
                        .withColor(ChatFormatting.AQUA)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/bMKwxnaFQw")));

        event.getEntity().displayClientMessage(Component.literal("Thank you for using Smart Backpacks.")
                .withStyle(ChatFormatting.GOLD), false);
        event.getEntity().displayClientMessage(Component.literal("We really appreciate your support. If you experience any issues or have suggestions, please join our Discord:")
                .withStyle(ChatFormatting.YELLOW), false);
        event.getEntity().displayClientMessage(discordLink, false);
        event.getEntity().displayClientMessage(Component.literal("This message can be disabled in the config.")
                .withStyle(ChatFormatting.GRAY), false);
    }

    private void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        LightUpgradeHandler.onPlayerLoggedOut(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(event.getEntity()));
        BuilderUpgradeHandler.onPlayerLoggedOut(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(event.getEntity()));
        TorchPlacerUpgradeHandler.onPlayerLoggedOut(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(event.getEntity()));
        CapacityWarningUpgradeHandler.onPlayerLoggedOut(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(event.getEntity()));
    }

    private void onEntityJoinLevel(EntityJoinLevelEvent event) {
        VoidboundUpgradeHandler.onEntityJoinLevel(new net.neoforged.neoforge.event.entity.EntityJoinLevelEvent(event.getEntity(), event.getLevel()));
    }

    private void onItemExpire(ItemExpireEvent event) {
        VoidboundUpgradeHandler.onItemExpire(new net.neoforged.neoforge.event.entity.item.ItemExpireEvent(event.getEntity()));
    }

    private void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.level.isClientSide()) {
            return;
        }

        if (!(event.level instanceof ServerLevel serverLevel)) {
            return;
        }

        for (Entity entity : serverLevel.getAllEntities()) {
            if (entity instanceof ItemEntity) {
                VoidboundUpgradeHandler.onEntityTickPre(new net.neoforged.neoforge.event.tick.EntityTickEvent.Pre(entity));
            }
        }
    }

    private void onExplosionDetonate(ExplosionEvent.Detonate event) {
        VoidboundUpgradeHandler.onExplosionDetonate(new net.neoforged.neoforge.event.level.ExplosionEvent.Detonate(
                event.getLevel(),
                event.getAffectedEntities(),
                event.getAffectedBlocks()
        ));
    }
}
