package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.client.SmartBackpacksClient;

import net.minecraft.client.Minecraft;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.codec.StreamCodec;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlayPortableJukeboxPayload(ItemStack disc, boolean play) implements CustomPacketPayload {
    public static final Type<PlayPortableJukeboxPayload> TYPE =
            new Type<>(new ResourceLocation(SmartBackpacks.MOD_ID, "play_portable_jukebox"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayPortableJukeboxPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeBoolean(payload.play);
                if (payload.play) {
                    buffer.writeItem(payload.disc);
                }
            },
            buffer -> {
                boolean play = buffer.readBoolean();
                ItemStack disc = play ? buffer.readItem() : ItemStack.EMPTY;
                return new PlayPortableJukeboxPayload(disc, play);
            }
    );

    @Override
    public Type<PlayPortableJukeboxPayload> type() {
        return TYPE;
    }

    public static void handle(PlayPortableJukeboxPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                if (payload.play) {
                    SmartBackpacksClient.playPortableJukebox(payload.disc());
                } else {
                    SmartBackpacksClient.stopPortableJukebox();
                }
            }
        });
    }
}


