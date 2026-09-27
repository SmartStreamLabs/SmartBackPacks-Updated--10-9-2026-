package net.neoforged.neoforge.common;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Supplier;

public final class ModConfigSpec {
    private ModConfigSpec() {
    }

    public static final class Builder {
        private final Deque<String> path = new ArrayDeque<>();

        public Builder push(String name) {
            path.push(name);
            return this;
        }

        public Builder pop() {
            if (!path.isEmpty()) {
                path.pop();
            }
            return this;
        }

        public Builder comment(String... comments) {
            return this;
        }

        public BooleanValue define(String name, boolean defaultValue) {
            return new BooleanValue(defaultValue);
        }

        public IntValue defineInRange(String name, int defaultValue, int min, int max) {
            return new IntValue(Math.max(min, Math.min(max, defaultValue)), min, max);
        }

        public ModConfigSpec build() {
            return new ModConfigSpec();
        }
    }

    public abstract static class ConfigValue<T> implements Supplier<T> {
        private T value;

        protected ConfigValue(T value) {
            this.value = Objects.requireNonNull(value);
        }

        @Override
        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = Objects.requireNonNull(value);
        }
    }

    public static final class BooleanValue extends ConfigValue<Boolean> {
        private BooleanValue(boolean value) {
            super(value);
        }
    }

    public static final class IntValue extends ConfigValue<Integer> {
        private final int min;
        private final int max;

        private IntValue(int value, int min, int max) {
            super(value);
            this.min = min;
            this.max = max;
        }

        @Override
        public void set(Integer value) {
            super.set(Math.max(min, Math.min(max, value)));
        }
    }
}
