package com.teamsmartstreamlabs.smartbackpacks.mobbackpack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record MobBackpackState(boolean processed, boolean dropped, ItemStack backpack) {
    public static final MobBackpackState UNPROCESSED = new MobBackpackState(false, false, ItemStack.EMPTY);
    public static final Codec<MobBackpackState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("processed", false).forGetter(MobBackpackState::processed),
            Codec.BOOL.optionalFieldOf("dropped", false).forGetter(MobBackpackState::dropped),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("backpack", ItemStack.EMPTY).forGetter(MobBackpackState::backpack)
    ).apply(instance, MobBackpackState::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MobBackpackState> STREAM_CODEC = StreamCodec.of(
            (buffer, state) -> {
                buffer.writeBoolean(state.processed());
                buffer.writeBoolean(state.dropped());
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, state.backpack());
            },
            buffer -> new MobBackpackState(buffer.readBoolean(), buffer.readBoolean(),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer)));

    public MobBackpackState {
        backpack = backpack.copy();
    }

    public MobBackpackState withDropped() {
        return new MobBackpackState(true, true, ItemStack.EMPTY);
    }
}
