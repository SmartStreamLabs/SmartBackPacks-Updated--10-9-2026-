package com.teamsmartstreamlabs.smartbackpacks.network;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ModPayloads {
    private static final String VERSION = "1";

    private ModPayloads() {
    }

    public static void register(IEventBus eventBus) {
        eventBus.addListener(ModPayloads::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(VERSION);
        registrar.playToServer(OpenWornBackpackPayload.TYPE, OpenWornBackpackPayload.STREAM_CODEC, OpenWornBackpackPayload::handle);
        registrar.playToServer(OpenStorageMonitorPayload.TYPE, OpenStorageMonitorPayload.STREAM_CODEC, OpenStorageMonitorPayload::handle);
        registrar.playToServer(SortOpenBackpackPayload.TYPE, SortOpenBackpackPayload.STREAM_CODEC, SortOpenBackpackPayload::handle);
        registrar.playToServer(SetBackpackScrollOffsetPayload.TYPE, SetBackpackScrollOffsetPayload.STREAM_CODEC, SetBackpackScrollOffsetPayload::handle);
        registrar.playToServer(PlaceHeldBackpackPayload.TYPE, PlaceHeldBackpackPayload.STREAM_CODEC, PlaceHeldBackpackPayload::handle);
        registrar.playToServer(PickupPlacedBackpackPayload.TYPE, PickupPlacedBackpackPayload.STREAM_CODEC, PickupPlacedBackpackPayload::handle);
        registrar.playToServer(ToggleUpgradeEnabledPayload.TYPE, ToggleUpgradeEnabledPayload.STREAM_CODEC, ToggleUpgradeEnabledPayload::handle);
        registrar.playToServer(UseInstalledUpgradePayload.TYPE, UseInstalledUpgradePayload.STREAM_CODEC, UseInstalledUpgradePayload::handle);
        registrar.playToServer(RequestAutoToolSwapPayload.TYPE, RequestAutoToolSwapPayload.STREAM_CODEC, RequestAutoToolSwapPayload::handle);
        registrar.playToServer(AutoFeedSettingsPayload.TYPE, AutoFeedSettingsPayload.STREAM_CODEC, AutoFeedSettingsPayload::handle);
        registrar.playToServer(SurvivalAssistEffectPayload.TYPE, SurvivalAssistEffectPayload.STREAM_CODEC, SurvivalAssistEffectPayload::handle);
        registrar.playToServer(FluidStorageActionPayload.TYPE, FluidStorageActionPayload.STREAM_CODEC, FluidStorageActionPayload::handle);
        registrar.playToServer(FluidTransferActionPayload.TYPE, FluidTransferActionPayload.STREAM_CODEC, FluidTransferActionPayload::handle);
        registrar.playToServer(CapacitorActionPayload.TYPE, CapacitorActionPayload.STREAM_CODEC, CapacitorActionPayload::handle);
        registrar.playToServer(XpTransferActionPayload.TYPE, XpTransferActionPayload.STREAM_CODEC, XpTransferActionPayload::handle);
        registrar.playToServer(JukeboxUpgradeActionPayload.TYPE, JukeboxUpgradeActionPayload.STREAM_CODEC, JukeboxUpgradeActionPayload::handle);
        registrar.playToServer(ChunkLoaderRadiusPayload.TYPE, ChunkLoaderRadiusPayload.STREAM_CODEC, ChunkLoaderRadiusPayload::handle);
        registrar.playToServer(QuiverSettingsPayload.TYPE, QuiverSettingsPayload.STREAM_CODEC, QuiverSettingsPayload::handle);
        registrar.playToServer(QuickAccessSelectPayload.TYPE, QuickAccessSelectPayload.STREAM_CODEC, QuickAccessSelectPayload::handle);
        registrar.playToServer(QuickAccessWheelRequestPayload.TYPE, QuickAccessWheelRequestPayload.STREAM_CODEC, QuickAccessWheelRequestPayload::handle);
        registrar.playToServer(TrashCanActionPayload.TYPE, TrashCanActionPayload.STREAM_CODEC, TrashCanActionPayload::handle);
        registrar.playToServer(RescueSettingsPayload.TYPE, RescueSettingsPayload.STREAM_CODEC, RescueSettingsPayload::handle);
        registrar.playToServer(BuilderSettingsPayload.TYPE, BuilderSettingsPayload.STREAM_CODEC, BuilderSettingsPayload::handle);
        registrar.playToServer(RequestBuilderRefillPayload.TYPE, RequestBuilderRefillPayload.STREAM_CODEC, RequestBuilderRefillPayload::handle);
        registrar.playToServer(TorchPlacerSettingsPayload.TYPE, TorchPlacerSettingsPayload.STREAM_CODEC, TorchPlacerSettingsPayload::handle);
        registrar.playToServer(CapacityWarningSettingsPayload.TYPE, CapacityWarningSettingsPayload.STREAM_CODEC, CapacityWarningSettingsPayload::handle);
        registrar.playToServer(ToggleItemLockPayload.TYPE, ToggleItemLockPayload.STREAM_CODEC, ToggleItemLockPayload::handle);
        registrar.playToServer(BackpackLinkActionPayload.TYPE, BackpackLinkActionPayload.STREAM_CODEC, BackpackLinkActionPayload::handle);
        registrar.playToClient(PlayPortableJukeboxPayload.TYPE, PlayPortableJukeboxPayload.STREAM_CODEC, PlayPortableJukeboxPayload::handle);
        registrar.playToClient(CapacityWarningSyncPayload.TYPE, CapacityWarningSyncPayload.STREAM_CODEC, CapacityWarningSyncPayload::handle);
        registrar.playToClient(PickupNotifierPayload.TYPE, PickupNotifierPayload.STREAM_CODEC, PickupNotifierPayload::handle);
        registrar.playToClient(BackpackLinkMenuSyncPayload.TYPE, BackpackLinkMenuSyncPayload.STREAM_CODEC, BackpackLinkMenuSyncPayload::handle);
        registrar.playToClient(BackpackVisibleSlotsSyncPayload.TYPE, BackpackVisibleSlotsSyncPayload.STREAM_CODEC, BackpackVisibleSlotsSyncPayload::handle);
        registrar.playToClient(QuickAccessWheelStatePayload.TYPE, QuickAccessWheelStatePayload.STREAM_CODEC, QuickAccessWheelStatePayload::handle);
        registrar.playToServer(SetMagnetUpgradeModePayload.TYPE, SetMagnetUpgradeModePayload.STREAM_CODEC, SetMagnetUpgradeModePayload::handle);
        registrar.playToServer(SetMagnetFilterInputTypePayload.TYPE, SetMagnetFilterInputTypePayload.STREAM_CODEC, SetMagnetFilterInputTypePayload::handle);
        registrar.playToServer(SetMagnetUpgradeTogglePayload.TYPE, SetMagnetUpgradeTogglePayload.STREAM_CODEC, SetMagnetUpgradeTogglePayload::handle);
        registrar.playToServer(AddMagnetUpgradeModPayload.TYPE, AddMagnetUpgradeModPayload.STREAM_CODEC, AddMagnetUpgradeModPayload::handle);
        registrar.playToServer(RemoveMagnetUpgradeModPayload.TYPE, RemoveMagnetUpgradeModPayload.STREAM_CODEC, RemoveMagnetUpgradeModPayload::handle);
        registrar.playToServer(AddMagnetUpgradeTagPayload.TYPE, AddMagnetUpgradeTagPayload.STREAM_CODEC, AddMagnetUpgradeTagPayload::handle);
        registrar.playToServer(RemoveMagnetUpgradeTagPayload.TYPE, RemoveMagnetUpgradeTagPayload.STREAM_CODEC, RemoveMagnetUpgradeTagPayload::handle);
        registrar.playToServer(StorageControllerActionPayload.TYPE, StorageControllerActionPayload.STREAM_CODEC, StorageControllerActionPayload::handle);
        registrar.playToClient(StorageControllerSnapshotPayload.TYPE, StorageControllerSnapshotPayload.STREAM_CODEC, StorageControllerSnapshotPayload::handle);
        registrar.playToClient(MobBackpackSyncPayload.TYPE, MobBackpackSyncPayload.STREAM_CODEC, MobBackpackSyncPayload::handle);
    }
}
