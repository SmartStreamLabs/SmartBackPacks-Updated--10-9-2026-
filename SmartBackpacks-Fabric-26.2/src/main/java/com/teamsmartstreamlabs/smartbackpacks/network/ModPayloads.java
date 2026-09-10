package com.teamsmartstreamlabs.smartbackpacks.network;

import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ModPayloads {
   private static boolean commonRegistered;
   private static boolean clientRegistered;

   private ModPayloads() {
   }

   public static void register(IEventBus eventBus) {
      registerCommon();
   }

   public static void registerCommon() {
      if (!commonRegistered) {
         commonRegistered = true;
         registerServerbound(OpenWornBackpackPayload.TYPE, OpenWornBackpackPayload.STREAM_CODEC, OpenWornBackpackPayload::handle);
         registerServerbound(SortOpenBackpackPayload.TYPE, SortOpenBackpackPayload.STREAM_CODEC, SortOpenBackpackPayload::handle);
         registerServerbound(SetBackpackScrollOffsetPayload.TYPE, SetBackpackScrollOffsetPayload.STREAM_CODEC, SetBackpackScrollOffsetPayload::handle);
         registerServerbound(PlaceHeldBackpackPayload.TYPE, PlaceHeldBackpackPayload.STREAM_CODEC, PlaceHeldBackpackPayload::handle);
         registerServerbound(PickupPlacedBackpackPayload.TYPE, PickupPlacedBackpackPayload.STREAM_CODEC, PickupPlacedBackpackPayload::handle);
         registerServerbound(ToggleUpgradeEnabledPayload.TYPE, ToggleUpgradeEnabledPayload.STREAM_CODEC, ToggleUpgradeEnabledPayload::handle);
         registerServerbound(UseInstalledUpgradePayload.TYPE, UseInstalledUpgradePayload.STREAM_CODEC, UseInstalledUpgradePayload::handle);
         registerServerbound(RequestAutoToolSwapPayload.TYPE, RequestAutoToolSwapPayload.STREAM_CODEC, RequestAutoToolSwapPayload::handle);
         registerServerbound(AutoFeedSettingsPayload.TYPE, AutoFeedSettingsPayload.STREAM_CODEC, AutoFeedSettingsPayload::handle);
         registerServerbound(SurvivalAssistEffectPayload.TYPE, SurvivalAssistEffectPayload.STREAM_CODEC, SurvivalAssistEffectPayload::handle);
         registerServerbound(FluidStorageActionPayload.TYPE, FluidStorageActionPayload.STREAM_CODEC, FluidStorageActionPayload::handle);
         registerServerbound(FluidTransferActionPayload.TYPE, FluidTransferActionPayload.STREAM_CODEC, FluidTransferActionPayload::handle);
         registerServerbound(CapacitorActionPayload.TYPE, CapacitorActionPayload.STREAM_CODEC, CapacitorActionPayload::handle);
         registerServerbound(XpTransferActionPayload.TYPE, XpTransferActionPayload.STREAM_CODEC, XpTransferActionPayload::handle);
         registerServerbound(JukeboxUpgradeActionPayload.TYPE, JukeboxUpgradeActionPayload.STREAM_CODEC, JukeboxUpgradeActionPayload::handle);
         registerServerbound(ChunkLoaderRadiusPayload.TYPE, ChunkLoaderRadiusPayload.STREAM_CODEC, ChunkLoaderRadiusPayload::handle);
         registerServerbound(QuiverSettingsPayload.TYPE, QuiverSettingsPayload.STREAM_CODEC, QuiverSettingsPayload::handle);
         registerServerbound(QuickAccessSelectPayload.TYPE, QuickAccessSelectPayload.STREAM_CODEC, QuickAccessSelectPayload::handle);
         registerServerbound(QuickAccessWheelRequestPayload.TYPE, QuickAccessWheelRequestPayload.STREAM_CODEC, QuickAccessWheelRequestPayload::handle);
         registerServerbound(TrashCanActionPayload.TYPE, TrashCanActionPayload.STREAM_CODEC, TrashCanActionPayload::handle);
         registerServerbound(RescueSettingsPayload.TYPE, RescueSettingsPayload.STREAM_CODEC, RescueSettingsPayload::handle);
         registerServerbound(BuilderSettingsPayload.TYPE, BuilderSettingsPayload.STREAM_CODEC, BuilderSettingsPayload::handle);
         registerServerbound(RequestBuilderRefillPayload.TYPE, RequestBuilderRefillPayload.STREAM_CODEC, RequestBuilderRefillPayload::handle);
         registerServerbound(TorchPlacerSettingsPayload.TYPE, TorchPlacerSettingsPayload.STREAM_CODEC, TorchPlacerSettingsPayload::handle);
         registerServerbound(CapacityWarningSettingsPayload.TYPE, CapacityWarningSettingsPayload.STREAM_CODEC, CapacityWarningSettingsPayload::handle);
         registerServerbound(ToggleItemLockPayload.TYPE, ToggleItemLockPayload.STREAM_CODEC, ToggleItemLockPayload::handle);
         registerServerbound(BackpackLinkActionPayload.TYPE, BackpackLinkActionPayload.STREAM_CODEC, BackpackLinkActionPayload::handle);
         registerServerbound(SetMagnetUpgradeModePayload.TYPE, SetMagnetUpgradeModePayload.STREAM_CODEC, SetMagnetUpgradeModePayload::handle);
         registerServerbound(SetMagnetFilterInputTypePayload.TYPE, SetMagnetFilterInputTypePayload.STREAM_CODEC, SetMagnetFilterInputTypePayload::handle);
         registerServerbound(SetMagnetUpgradeTogglePayload.TYPE, SetMagnetUpgradeTogglePayload.STREAM_CODEC, SetMagnetUpgradeTogglePayload::handle);
         registerServerbound(AddMagnetUpgradeModPayload.TYPE, AddMagnetUpgradeModPayload.STREAM_CODEC, AddMagnetUpgradeModPayload::handle);
         registerServerbound(RemoveMagnetUpgradeModPayload.TYPE, RemoveMagnetUpgradeModPayload.STREAM_CODEC, RemoveMagnetUpgradeModPayload::handle);
         registerServerbound(AddMagnetUpgradeTagPayload.TYPE, AddMagnetUpgradeTagPayload.STREAM_CODEC, AddMagnetUpgradeTagPayload::handle);
         registerServerbound(RemoveMagnetUpgradeTagPayload.TYPE, RemoveMagnetUpgradeTagPayload.STREAM_CODEC, RemoveMagnetUpgradeTagPayload::handle);
         registerServerbound(StorageControllerActionPayload.TYPE, StorageControllerActionPayload.STREAM_CODEC, StorageControllerActionPayload::handle);
         PayloadTypeRegistry.clientboundPlay().register(
            StorageControllerSnapshotPayload.TYPE, StorageControllerSnapshotPayload.STREAM_CODEC
         );
      }
   }

   public static void registerClient() {
      if (!clientRegistered) {
         clientRegistered = true;
         registerClientbound(PlayPortableJukeboxPayload.TYPE, PlayPortableJukeboxPayload.STREAM_CODEC, PlayPortableJukeboxPayload::handle);
         registerClientbound(CapacityWarningSyncPayload.TYPE, CapacityWarningSyncPayload.STREAM_CODEC, CapacityWarningSyncPayload::handle);
         registerClientbound(PickupNotifierPayload.TYPE, PickupNotifierPayload.STREAM_CODEC, PickupNotifierPayload::handle);
         registerClientbound(BackpackLinkMenuSyncPayload.TYPE, BackpackLinkMenuSyncPayload.STREAM_CODEC, BackpackLinkMenuSyncPayload::handle);
         registerClientbound(BackpackVisibleSlotsSyncPayload.TYPE, BackpackVisibleSlotsSyncPayload.STREAM_CODEC, BackpackVisibleSlotsSyncPayload::handle);
         registerClientbound(QuickAccessWheelStatePayload.TYPE, QuickAccessWheelStatePayload.STREAM_CODEC, QuickAccessWheelStatePayload::handle);
         registerClientboundReceiver(StorageControllerSnapshotPayload.TYPE, StorageControllerSnapshotPayload::handle);
      }
   }

   public static void sendToServer(Object payload) {
      if (payload instanceof CustomPacketPayload customPayload) {
         ClientPlayNetworking.send(customPayload);
      }
   }

   public static void sendToPlayer(Object player, Object payload) {
      if (player instanceof ServerPlayer serverPlayer && payload instanceof CustomPacketPayload customPayload) {
         ServerPlayNetworking.send(serverPlayer, customPayload);
      }
   }

   private static <T extends CustomPacketPayload> void registerServerbound(
      Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, BiConsumer<T, IPayloadContext> handler
   ) {
      PayloadTypeRegistry.serverboundPlay().register(type, codec);
      ServerPlayNetworking.registerGlobalReceiver(
         type, (payload, context) -> handler.accept((T)payload, new ModPayloads.ServerPayloadContext(context.server(), context.player()))
      );
   }

   private static <T extends CustomPacketPayload> void registerClientbound(
      Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, BiConsumer<T, IPayloadContext> handler
   ) {
      PayloadTypeRegistry.clientboundPlay().register(type, codec);
      ClientPlayNetworking.registerGlobalReceiver(
         type, (payload, context) -> handler.accept((T)payload, new ModPayloads.ClientPayloadContext(context.client()))
      );
   }

   private static <T extends CustomPacketPayload> void registerClientboundReceiver(
      Type<T> type, BiConsumer<T, IPayloadContext> handler
   ) {
      ClientPlayNetworking.registerGlobalReceiver(
         type, (payload, context) -> handler.accept((T)payload, new ModPayloads.ClientPayloadContext(context.client()))
      );
   }

   private record ClientPayloadContext(Minecraft client) implements IPayloadContext {
      @Override
      public void enqueueWork(Runnable runnable) {
         this.client.execute(runnable);
      }
   }

   private record ServerPayloadContext(MinecraftServer server, ServerPlayer player) implements IPayloadContext {
      @Override
      public void enqueueWork(Runnable runnable) {
         this.server.execute(runnable);
      }
   }
}
