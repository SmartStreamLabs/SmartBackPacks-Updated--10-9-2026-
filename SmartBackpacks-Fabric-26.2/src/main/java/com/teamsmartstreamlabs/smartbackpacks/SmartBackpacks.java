package com.teamsmartstreamlabs.smartbackpacks;

import com.mojang.logging.LogUtils;
import com.teamsmartstreamlabs.smartbackpacks.loot.ModLootEvents;
import com.teamsmartstreamlabs.smartbackpacks.mobbackpack.MobBackpackAttachments;
import com.teamsmartstreamlabs.smartbackpacks.mobbackpack.MobBackpackCommands;
import com.teamsmartstreamlabs.smartbackpacks.mobbackpack.MobBackpackHandler;
import com.teamsmartstreamlabs.smartbackpacks.network.ModPayloads;
import com.teamsmartstreamlabs.smartbackpacks.pickup.PickupNotifierServer;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModBlockEntities;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModBlocks;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModCreativeTabs;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModLootFunctions;
import com.teamsmartstreamlabs.smartbackpacks.worldgen.ModCampStructures;
import com.teamsmartstreamlabs.smartbackpacks.worldgen.CampTreeClearance;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModRecipeSerializers;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModRecipeTypes;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.AutoFeedUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.AutoSmeltingUpgradeMenuHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BlastFurnaceUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BrewingStandUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacityWarningUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CourierUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CompressionUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.DepositUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FurnaceUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.LightUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.NightVisionUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FlightUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.RepairUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FallProtectionUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.MagnetUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.PickupUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.RestockUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.SmokerUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.SoulboundUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.DeathEmergencyKitHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.SurvivalAssistUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.VoidUpgradeHandler;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.AfterRespawn;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Disconnect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;

public final class SmartBackpacks {
   public static final String MOD_ID = "smartbackpacks";
   public static final Logger LOGGER = LogUtils.getLogger();
   private static final IEventBus FABRIC_REGISTER_BUS = new IEventBus() {};

   private SmartBackpacks() {
   }

   public static void initCommon() {
      ModBlocks.register(FABRIC_REGISTER_BUS);
      ModBlockEntities.register(FABRIC_REGISTER_BUS);
      ModDataComponents.register(FABRIC_REGISTER_BUS);
      ModRecipeSerializers.register(FABRIC_REGISTER_BUS);
        ModRecipeTypes.register(FABRIC_REGISTER_BUS);
      ModItems.register(FABRIC_REGISTER_BUS);
      ModLootFunctions.register(FABRIC_REGISTER_BUS);
        ModCampStructures.register(FABRIC_REGISTER_BUS);
        com.teamsmartstreamlabs.smartbackpacks.registry.ModSounds.register(FABRIC_REGISTER_BUS);
      CampTreeClearance.register();
      ModMenuTypes.register(FABRIC_REGISTER_BUS);
      ModCreativeTabs.register(FABRIC_REGISTER_BUS);
      ModPayloads.registerCommon();
      MobBackpackAttachments.init();
      ModLootEvents.register();
      registerFabricEvents();
      LOGGER.info("Initializing {} Fabric common bootstrap", "smartbackpacks");
   }

   private static void registerFabricEvents() {
      CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> MobBackpackCommands.register(dispatcher));
      ServerLivingEntityEvents.AFTER_DEATH.register(MobBackpackHandler::onAfterDeath);
      ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
         if (entity instanceof ServerPlayer player) {
            DeathEmergencyKitHandler.onPlayerDeath(player);
         }
      });
      ServerTickEvents.END_SERVER_TICK.register((EndTick)server -> {
         for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerTickEvent.Post event = new PlayerTickEvent.Post(player);
            AutoSmeltingUpgradeMenuHandler.onPlayerTick(event);
            BlastFurnaceUpgradeHandler.onPlayerTick(event);
            AutoFeedUpgradeHandler.onPlayerTick(event);
            BrewingStandUpgradeHandler.onPlayerTick(event);
            FurnaceUpgradeHandler.onPlayerTick(event);
            CompressionUpgradeHandler.onPlayerTick(event);
            SmokerUpgradeHandler.onPlayerTick(event);
            SurvivalAssistUpgradeHandler.onPlayerTick(event);
            LightUpgradeHandler.onPlayerTick(event);
            NightVisionUpgradeHandler.onPlayerTick(event);
            FlightUpgradeHandler.onPlayerTick(event);
            RepairUpgradeHandler.onPlayerTick(event);
            FallProtectionUpgradeHandler.onPlayerTick(event);
            DeathEmergencyKitHandler.onPlayerTick(event);
            MagnetUpgradeHandler.onPlayerTick(event);
            PickupUpgradeHandler.onPlayerTick(event);
            CapacityWarningUpgradeHandler.onPlayerTick(event);
            CourierUpgradeHandler.onPlayerTick(event);
            VoidUpgradeHandler.onPlayerTick(event);
            PickupNotifierServer.flush(player);
            com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.onPlayerTick(event);
         }
      });
      UseBlockCallback.EVENT
         .register(
            (UseBlockCallback)(player, world, hand, hitResult) -> {
               if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                  PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(
                     serverPlayer, world, hand, hitResult.getBlockPos(), hitResult.getDirection()
                  );
                  RestockUpgradeHandler.onRightClickBlock(event);
                  DepositUpgradeHandler.onRightClickBlock(event);
                  CourierUpgradeHandler.onRightClickBlock(event);
                  return (InteractionResult)(!event.isCanceled() ? InteractionResult.PASS : mapActionResult(event.getCancellationResult()));
               } else {
                  return InteractionResult.PASS;
               }
            }
         );
      ServerPlayConnectionEvents.DISCONNECT
         .register((Disconnect)(handler, server) -> {
            PlayerEvent.PlayerLoggedOutEvent event = new PlayerEvent.PlayerLoggedOutEvent(handler.player);
            LightUpgradeHandler.onPlayerLoggedOut(event);
            NightVisionUpgradeHandler.onPlayerLoggedOut(event);
            FlightUpgradeHandler.onPlayerLoggedOut(event);
            RepairUpgradeHandler.onPlayerLoggedOut(event);
            FallProtectionUpgradeHandler.onPlayerLoggedOut(event);
            CapacityWarningUpgradeHandler.onPlayerLoggedOut(event);
         });
      ServerPlayerEvents.AFTER_RESPAWN
         .register((AfterRespawn)(oldPlayer, newPlayer, alive) -> SoulboundUpgradeHandler.onPlayerRespawn(new PlayerEvent.PlayerRespawnEvent(newPlayer)));
      ServerPlayerEvents.AFTER_RESPAWN
         .register((AfterRespawn)(oldPlayer, newPlayer, alive) -> DeathEmergencyKitHandler.onPlayerRespawn(new PlayerEvent.PlayerRespawnEvent(newPlayer)));
   }

   private static InteractionResult mapActionResult(InteractionResult result) {
      if (result == InteractionResult.FAIL) {
         return InteractionResult.FAIL;
      } else {
         return (InteractionResult)(result == InteractionResult.PASS ? InteractionResult.PASS : InteractionResult.SUCCESS);
      }
   }
}
