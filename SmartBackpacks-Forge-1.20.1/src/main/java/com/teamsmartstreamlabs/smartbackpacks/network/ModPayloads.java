package com.teamsmartstreamlabs.smartbackpacks.network;

import io.netty.buffer.Unpooled;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.codec.StreamCodec;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.eventbus.api.IEventBus;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ModPayloads {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation("smartbackpacks", "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();
    private static final Map<ResourceLocation, StreamCodec<RegistryFriendlyByteBuf, ? extends CustomPacketPayload>> CODECS = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, BiConsumer<CustomPacketPayload, IPayloadContext>> SERVERBOUND_HANDLERS = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, BiConsumer<CustomPacketPayload, IPayloadContext>> CLIENTBOUND_HANDLERS = new ConcurrentHashMap<>();
    private static boolean channelRegistered;
    private static boolean commonRegistered;
    private static boolean clientRegistered;
    private static int packetIndex;

    private ModPayloads() {
    }

    public static void register(IEventBus eventBus) {
        registerCommon();
    }

    public static void registerCommon() {
        ensureChannelRegistered();
        if (commonRegistered) {
            return;
        }
        commonRegistered = true;

        registerServerbound(OpenWornBackpackPayload.TYPE, OpenWornBackpackPayload.STREAM_CODEC, OpenWornBackpackPayload::handle);
        registerServerbound(OpenStorageMonitorPayload.TYPE, OpenStorageMonitorPayload.STREAM_CODEC, OpenStorageMonitorPayload::handle);
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
        CODECS.put(StorageControllerSnapshotPayload.TYPE.id(), StorageControllerSnapshotPayload.STREAM_CODEC);
        CODECS.put(MobBackpackSyncPayload.TYPE.id(), MobBackpackSyncPayload.STREAM_CODEC);
    }

    public static void registerClient() {
        ensureChannelRegistered();
        if (clientRegistered) {
            return;
        }
        clientRegistered = true;
        registerClientbound(PlayPortableJukeboxPayload.TYPE, PlayPortableJukeboxPayload.STREAM_CODEC, PlayPortableJukeboxPayload::handle);
        registerClientbound(CapacityWarningSyncPayload.TYPE, CapacityWarningSyncPayload.STREAM_CODEC, CapacityWarningSyncPayload::handle);
        registerClientbound(PickupNotifierPayload.TYPE, PickupNotifierPayload.STREAM_CODEC, PickupNotifierPayload::handle);
        registerClientbound(BackpackLinkMenuSyncPayload.TYPE, BackpackLinkMenuSyncPayload.STREAM_CODEC, BackpackLinkMenuSyncPayload::handle);
        registerClientbound(BackpackVisibleSlotsSyncPayload.TYPE, BackpackVisibleSlotsSyncPayload.STREAM_CODEC, BackpackVisibleSlotsSyncPayload::handle);
        registerClientbound(QuickAccessWheelStatePayload.TYPE, QuickAccessWheelStatePayload.STREAM_CODEC, QuickAccessWheelStatePayload::handle);
        registerClientbound(StorageControllerSnapshotPayload.TYPE, StorageControllerSnapshotPayload.STREAM_CODEC, StorageControllerSnapshotPayload::handle);
        registerClientbound(MobBackpackSyncPayload.TYPE, MobBackpackSyncPayload.STREAM_CODEC, MobBackpackSyncPayload::handle);
    }

    public static void sendToServer(Object payload) {
        if (!(payload instanceof CustomPacketPayload customPayload)) {
            return;
        }
        CHANNEL.sendToServer(new ServerboundPayloadPacket(customPayload.type().id(), encode(customPayload)));
    }

    public static void sendToPlayer(Object player, Object payload) {
        if (!(player instanceof ServerPlayer serverPlayer) || !(payload instanceof CustomPacketPayload customPayload)) {
            return;
        }
        CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> serverPlayer),
                new ClientboundPayloadPacket(customPayload.type().id(), encode(customPayload)));
    }

    private static void ensureChannelRegistered() {
        if (channelRegistered) {
            return;
        }
        channelRegistered = true;

        CHANNEL.messageBuilder(ServerboundPayloadPacket.class, packetIndex++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundPayloadPacket::encode)
                .decoder(ServerboundPayloadPacket::decode)
                .consumerMainThread(ModPayloads::handleServerbound)
                .add();
        CHANNEL.messageBuilder(ClientboundPayloadPacket.class, packetIndex++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundPayloadPacket::encode)
                .decoder(ClientboundPayloadPacket::decode)
                .consumerMainThread(ModPayloads::handleClientbound)
                .add();
    }

    private static <T extends CustomPacketPayload> void registerServerbound(CustomPacketPayload.Type<T> type,
                                                                            StreamCodec<RegistryFriendlyByteBuf, T> codec,
                                                                            BiConsumer<T, IPayloadContext> handler) {
        CODECS.put(type.id(), codec);
        SERVERBOUND_HANDLERS.put(type.id(), castHandler(handler));
    }

    private static <T extends CustomPacketPayload> void registerClientbound(CustomPacketPayload.Type<T> type,
                                                                            StreamCodec<RegistryFriendlyByteBuf, T> codec,
                                                                            BiConsumer<T, IPayloadContext> handler) {
        CODECS.put(type.id(), codec);
        CLIENTBOUND_HANDLERS.put(type.id(), castHandler(handler));
    }

    @SuppressWarnings("unchecked")
    private static BiConsumer<CustomPacketPayload, IPayloadContext> castHandler(BiConsumer<? extends CustomPacketPayload, IPayloadContext> handler) {
        return (BiConsumer<CustomPacketPayload, IPayloadContext>) handler;
    }

    private static void handleServerbound(ServerboundPayloadPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            CustomPacketPayload payload = decode(packet.typeId, packet.data);
            if (payload == null) {
                return;
            }

            BiConsumer<CustomPacketPayload, IPayloadContext> handler = SERVERBOUND_HANDLERS.get(packet.typeId);
            if (handler != null) {
                handler.accept(payload, new ForgePayloadContext(context));
            }
        });
        context.setPacketHandled(true);
    }

    private static void handleClientbound(ClientboundPayloadPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            CustomPacketPayload payload = decode(packet.typeId, packet.data);
            if (payload == null) {
                return;
            }

            BiConsumer<CustomPacketPayload, IPayloadContext> handler = CLIENTBOUND_HANDLERS.get(packet.typeId);
            if (handler != null) {
                handler.accept(payload, new ClientPayloadContext());
            }
        });
        context.setPacketHandled(true);
    }

    private static byte[] encode(CustomPacketPayload payload) {
        @SuppressWarnings("unchecked")
        StreamCodec<RegistryFriendlyByteBuf, CustomPacketPayload> codec =
                (StreamCodec<RegistryFriendlyByteBuf, CustomPacketPayload>) CODECS.get(payload.type().id());
        if (codec == null) {
            throw new IllegalStateException("No payload codec registered for " + payload.type().id());
        }

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        codec.encode(new RegistryFriendlyByteBuf(buffer), payload);
        byte[] data = new byte[buffer.readableBytes()];
        buffer.getBytes(0, data);
        return data;
    }

    private static CustomPacketPayload decode(ResourceLocation typeId, byte[] data) {
        @SuppressWarnings("unchecked")
        StreamCodec<RegistryFriendlyByteBuf, CustomPacketPayload> codec =
                (StreamCodec<RegistryFriendlyByteBuf, CustomPacketPayload>) CODECS.get(typeId);
        if (codec == null) {
            return null;
        }

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
        return codec.decode(new RegistryFriendlyByteBuf(buffer));
    }

    private record ServerboundPayloadPacket(ResourceLocation typeId, byte[] data) {
        private static void encode(ServerboundPayloadPacket packet, FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(packet.typeId);
            buffer.writeVarInt(packet.data.length);
            buffer.writeBytes(packet.data);
        }

        private static ServerboundPayloadPacket decode(FriendlyByteBuf buffer) {
            ResourceLocation typeId = buffer.readResourceLocation();
            byte[] data = new byte[buffer.readVarInt()];
            buffer.readBytes(data);
            return new ServerboundPayloadPacket(typeId, data);
        }
    }

    private record ClientboundPayloadPacket(ResourceLocation typeId, byte[] data) {
        private static void encode(ClientboundPayloadPacket packet, FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(packet.typeId);
            buffer.writeVarInt(packet.data.length);
            buffer.writeBytes(packet.data);
        }

        private static ClientboundPayloadPacket decode(FriendlyByteBuf buffer) {
            ResourceLocation typeId = buffer.readResourceLocation();
            byte[] data = new byte[buffer.readVarInt()];
            buffer.readBytes(data);
            return new ClientboundPayloadPacket(typeId, data);
        }
    }

    private record ForgePayloadContext(NetworkEvent.Context context) implements IPayloadContext {
        @Override
        public void enqueueWork(Runnable runnable) {
            this.context.enqueueWork(runnable);
        }

        @Override
        public ServerPlayer player() {
            return this.context.getSender();
        }
    }

    private static final class ClientPayloadContext implements IPayloadContext {
        @Override
        public void enqueueWork(Runnable runnable) {
            Minecraft.getInstance().execute(runnable);
        }
    }
}
