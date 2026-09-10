package net.neoforged.neoforge.network.handling;

import net.minecraft.server.level.ServerPlayer;

public interface IPayloadContext {
    default void enqueueWork(Runnable runnable) {
        runnable.run();
    }

    default ServerPlayer player() {
        return null;
    }
}
