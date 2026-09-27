package com.teamsmartstreamlabs.smartbackpacks.fabric.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ModConfigSpec {
   private static final Path CONFIG_PATH = Path.of("config", "smartbackpacksconfig.toml");
   private final List<ConfigValue<?>> values;
   private boolean loaded;

   private ModConfigSpec(List<ConfigValue<?>> values) {
      this.values = List.copyOf(values);
      this.load();
   }

   public boolean isLoaded() {
      return this.loaded;
   }

   public void load() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.exists(CONFIG_PATH)) {
            Map<String, String> entries = parseToml(CONFIG_PATH);
            for (ConfigValue<?> value : this.values) {
               value.load(entries.get(value.path));
            }
         }
         this.save();
         this.loaded = true;
      } catch (Exception exception) {
         this.loaded = false;
         System.err.println("[Smart Backpacks] Failed to load Fabric config, using safe defaults: " + exception.getMessage());
      }
   }

   public void save() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         StringBuilder builder = new StringBuilder();
         String currentSection = "";
         for (ConfigValue<?> value : this.values) {
            if (!value.section.equals(currentSection)) {
               if (!builder.isEmpty()) {
                  builder.append('\n');
               }
               currentSection = value.section;
               if (!currentSection.isEmpty()) {
                  builder.append('[').append(currentSection).append("]\n");
               }
            }
            for (String comment : value.comments) {
               for (String line : comment.split("\\R")) {
                  builder.append("# ").append(line).append('\n');
               }
            }
            builder.append(value.name).append(" = ").append(value.serialize()).append('\n');
         }
         Files.writeString(CONFIG_PATH, builder.toString(), StandardCharsets.UTF_8);
         this.loaded = true;
      } catch (IOException exception) {
         this.loaded = false;
         System.err.println("[Smart Backpacks] Failed to save Fabric config: " + exception.getMessage());
      }
   }

   private static Map<String, String> parseToml(Path path) throws IOException {
      Map<String, String> result = new LinkedHashMap<>();
      String section = "";
      for (String rawLine : Files.readAllLines(path, StandardCharsets.UTF_8)) {
         String line = stripComment(rawLine).trim();
         if (line.isEmpty()) {
            continue;
         }
         if (line.startsWith("[") && line.endsWith("]")) {
            section = line.substring(1, line.length() - 1).trim();
            continue;
         }
         int equals = line.indexOf('=');
         if (equals < 0) {
            continue;
         }
         String key = line.substring(0, equals).trim();
         String value = line.substring(equals + 1).trim();
         result.put(section.isEmpty() ? key : section + "." + key, value);
      }
      return result;
   }

   private static String stripComment(String line) {
      boolean quoted = false;
      for (int i = 0; i < line.length(); i++) {
         char c = line.charAt(i);
         if (c == '"') {
            quoted = !quoted;
         } else if (c == '#' && !quoted) {
            return line.substring(0, i);
         }
      }
      return line;
   }

   public static final class Builder {
      private final ArrayDeque<String> sections = new ArrayDeque<>();
      private final List<ConfigValue<?>> values = new ArrayList<>();
      private final List<String> pendingComments = new ArrayList<>();

      public Builder push(String name) {
         this.sections.addLast(name);
         return this;
      }

      public Builder pop() {
         if (!this.sections.isEmpty()) {
            this.sections.removeLast();
         }
         return this;
      }

      public Builder comment(String comment) {
         this.pendingComments.add(comment);
         return this;
      }

      public BooleanValue define(String name, boolean defaultValue) {
         BooleanValue value = new BooleanValue(this.path(name), this.section(), name, defaultValue, this.takeComments());
         this.values.add(value);
         return value;
      }

      public IntValue defineInRange(String name, int defaultValue, int min, int max) {
         IntValue value = new IntValue(this.path(name), this.section(), name, defaultValue, min, max, this.takeComments());
         this.values.add(value);
         return value;
      }

      public DoubleValue defineInRange(String name, double defaultValue, double min, double max) {
         DoubleValue value = new DoubleValue(this.path(name), this.section(), name, defaultValue, min, max, this.takeComments());
         this.values.add(value);
         return value;
      }

      public ConfigValue<String> define(String name, String defaultValue) {
         ConfigValue<String> value = new StringValue(this.path(name), this.section(), name, defaultValue, this.takeComments());
         this.values.add(value);
         return value;
      }

      public <T extends Enum<T>> EnumValue<T> defineEnum(String name, T defaultValue) {
         EnumValue<T> value = new EnumValue<>(this.path(name), this.section(), name, defaultValue, this.takeComments());
         this.values.add(value);
         return value;
      }

      public ModConfigSpec build() {
         return new ModConfigSpec(this.values);
      }

      private String section() {
         return String.join(".", this.sections);
      }

      private String path(String name) {
         String section = this.section();
         return section.isEmpty() ? name : section + "." + name;
      }

      private List<String> takeComments() {
         List<String> comments = List.copyOf(this.pendingComments);
         this.pendingComments.clear();
         return comments;
      }
   }

   public abstract static class ConfigValue<T> {
      private final String path;
      private final String section;
      private final String name;
      private final List<String> comments;
      protected final T defaultValue;
      protected T value;

      private ConfigValue(String path, String section, String name, T defaultValue, List<String> comments) {
         this.path = path;
         this.section = section;
         this.name = name;
         this.comments = comments;
         this.defaultValue = defaultValue;
         this.value = defaultValue;
      }

      public T get() {
         return this.value;
      }

      public void set(T value) {
         this.value = this.sanitize(value);
      }

      protected T sanitize(T value) {
         return value;
      }

      protected abstract void load(String rawValue);

      protected abstract String serialize();
   }

   public static final class BooleanValue extends ConfigValue<Boolean> {
      private BooleanValue(String path, String section, String name, boolean defaultValue, List<String> comments) {
         super(path, section, name, defaultValue, comments);
      }

      @Override
      protected void load(String rawValue) {
         if (rawValue == null) {
            return;
         }
         String normalized = rawValue.trim().toLowerCase(Locale.ROOT);
         if ("true".equals(normalized) || "false".equals(normalized)) {
            this.value = Boolean.parseBoolean(normalized);
         }
      }

      @Override
      protected String serialize() {
         return Boolean.toString(this.value);
      }
   }

   public static final class IntValue extends ConfigValue<Integer> {
      private final int min;
      private final int max;

      private IntValue(String path, String section, String name, int defaultValue, int min, int max, List<String> comments) {
         super(path, section, name, defaultValue, comments);
         this.min = min;
         this.max = max;
         this.value = this.sanitize(defaultValue);
      }

      @Override
      protected Integer sanitize(Integer value) {
         return Math.max(this.min, Math.min(this.max, value));
      }

      @Override
      protected void load(String rawValue) {
         if (rawValue == null) {
            return;
         }
         try {
            this.value = this.sanitize(Integer.parseInt(rawValue.trim()));
         } catch (NumberFormatException ignored) {
         }
      }

      @Override
      protected String serialize() {
         return Integer.toString(this.value);
      }
   }

   public static final class EnumValue<T extends Enum<T>> extends ConfigValue<T> {
      private final Class<T> enumClass;

      @SuppressWarnings("unchecked")
      private EnumValue(String path, String section, String name, T defaultValue, List<String> comments) {
         super(path, section, name, defaultValue, comments);
         this.enumClass = (Class<T>)defaultValue.getDeclaringClass();
      }

      @Override
      protected void load(String rawValue) {
         if (rawValue == null) {
            return;
         }
         String normalized = rawValue.trim().replace("\"", "");
         for (T constant : this.enumClass.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(normalized)) {
               this.value = constant;
               return;
            }
         }
      }

      @Override
      protected String serialize() {
         return "\"" + this.value.name() + "\"";
      }
   }

   public static final class DoubleValue extends ConfigValue<Double> {
      private final double min;
      private final double max;

      private DoubleValue(String path, String section, String name, double defaultValue, double min, double max, List<String> comments) {
         super(path, section, name, defaultValue, comments);
         this.min = min;
         this.max = max;
         this.value = this.sanitize(defaultValue);
      }

      @Override
      protected Double sanitize(Double value) {
         return Math.max(this.min, Math.min(this.max, value));
      }

      @Override
      protected void load(String rawValue) {
         if (rawValue == null) return;
         try {
            this.value = this.sanitize(Double.parseDouble(rawValue.trim()));
         } catch (NumberFormatException ignored) {
         }
      }

      @Override
      protected String serialize() {
         return Double.toString(this.value);
      }
   }

   private static final class StringValue extends ConfigValue<String> {
      private StringValue(String path, String section, String name, String defaultValue, List<String> comments) {
         super(path, section, name, defaultValue, comments);
      }

      @Override
      protected void load(String rawValue) {
         if (rawValue != null) this.value = rawValue.trim().replace("\"", "");
      }

      @Override
      protected String serialize() {
         return "\"" + this.value.replace("\"", "") + "\"";
      }
   }
}
