package com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;

public class RegistryFriendlyByteBuf extends FriendlyByteBuf {
    public RegistryFriendlyByteBuf(ByteBuf source) {
        super(source);
    }
}
