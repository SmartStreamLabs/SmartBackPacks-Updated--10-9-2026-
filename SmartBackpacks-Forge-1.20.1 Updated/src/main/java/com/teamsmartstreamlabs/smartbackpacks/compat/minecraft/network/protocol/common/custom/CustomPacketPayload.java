package com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.protocol.common.custom;

import net.minecraft.resources.ResourceLocation;

public interface CustomPacketPayload {
    Type<? extends CustomPacketPayload> type();

    record Type<T extends CustomPacketPayload>(ResourceLocation id) {
    }
}
