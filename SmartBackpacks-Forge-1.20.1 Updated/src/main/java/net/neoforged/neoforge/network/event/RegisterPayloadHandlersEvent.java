package net.neoforged.neoforge.network.event;

import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.codec.StreamCodec;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class RegisterPayloadHandlersEvent {
    public Registrar registrar(String version) {
        return new Registrar();
    }

    public static final class Registrar {
        public <T extends CustomPacketPayload> void playToServer(CustomPacketPayload.Type<T> type, StreamCodec<?, T> codec,
                PayloadHandler<T> handler) {
        }

        public <T extends CustomPacketPayload> void playToClient(CustomPacketPayload.Type<T> type, StreamCodec<?, T> codec,
                PayloadHandler<T> handler) {
        }
    }

    @FunctionalInterface
    public interface PayloadHandler<T extends CustomPacketPayload> {
        void handle(T payload, IPayloadContext context);
    }
}
