package com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.codec;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface StreamCodec<B, T> {
    void encode(B buffer, T value);

    T decode(B buffer);

    static <B, T> StreamCodec<B, T> of(BiConsumer<B, T> writer, Function<B, T> reader) {
        return new StreamCodec<>() {
            @Override
            public void encode(B buffer, T value) {
                writer.accept(buffer, value);
            }

            @Override
            public T decode(B buffer) {
                return reader.apply(buffer);
            }
        };
    }

    static <B, T> StreamCodec<B, T> unit(T value) {
        return new StreamCodec<>() {
            @Override
            public void encode(B buffer, T ignored) {
            }

            @Override
            public T decode(B buffer) {
                return value;
            }
        };
    }
}
