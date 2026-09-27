package com.teamsmartstreamlabs.smartbackpacks.network;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.client.SmartBackpacksClient;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlayPortableJukeboxPayload(ItemStack disc, boolean play) implements CustomPacketPayload {
    public static final Type<PlayPortableJukeboxPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "play_portable_jukebox"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayPortableJukeboxPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeBoolean(payload.play);
                if (payload.play) {
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, payload.disc);
                }
            },
            buffer -> {
                boolean play = buffer.readBoolean();
                ItemStack disc = play ? ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer) : ItemStack.EMPTY;
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

