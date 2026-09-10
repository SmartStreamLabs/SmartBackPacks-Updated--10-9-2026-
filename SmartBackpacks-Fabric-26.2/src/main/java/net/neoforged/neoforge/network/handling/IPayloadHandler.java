package net.neoforged.neoforge.network.handling;

@FunctionalInterface
public interface IPayloadHandler<T> {
   void handle(T var1, IPayloadContext var2);
}
