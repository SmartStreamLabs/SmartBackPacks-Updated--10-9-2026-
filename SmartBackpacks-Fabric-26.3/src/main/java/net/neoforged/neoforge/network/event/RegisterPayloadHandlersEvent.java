package net.neoforged.neoforge.network.event;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class RegisterPayloadHandlersEvent {
   public RegisterPayloadHandlersEvent.Registrar registrar(String version) {
      return new RegisterPayloadHandlersEvent.Registrar();
   }

   @FunctionalInterface
   public interface PayloadHandler<T extends CustomPacketPayload> {
      void handle(T var1, IPayloadContext var2);
   }

   public static final class Registrar {
      public <T extends CustomPacketPayload> void playToServer(Type<T> type, StreamCodec<?, T> codec, RegisterPayloadHandlersEvent.PayloadHandler<T> handler) {
      }

      public <T extends CustomPacketPayload> void playToClient(Type<T> type, StreamCodec<?, T> codec, RegisterPayloadHandlersEvent.PayloadHandler<T> handler) {
      }
   }
}
