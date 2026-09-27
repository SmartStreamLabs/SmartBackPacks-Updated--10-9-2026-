package com.teamsmartstreamlabs.smartbackpacks.progress;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

/** Compact, server-owned counters keyed by player UUID, independent of player entity lifetime. */
public final class SmartBackpacksPlayerStats extends SavedData {
    private static final Codec<Map<String, Map<String, Long>>> PLAYERS_CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, Codec.LONG));
    public static final Codec<SmartBackpacksPlayerStats> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PLAYERS_CODEC.optionalFieldOf("players", Map.of()).forGetter(SmartBackpacksPlayerStats::snapshot)
    ).apply(instance, SmartBackpacksPlayerStats::new));
    public static final String ID = SmartBackpacks.MOD_ID + "_player_stats";
    public static final SavedData.Factory<SmartBackpacksPlayerStats> FACTORY =
            new SavedData.Factory<>(SmartBackpacksPlayerStats::new, SmartBackpacksPlayerStats::load, DataFixTypes.LEVEL);

    private final Map<String, Map<String, Long>> players = new HashMap<>();

    public SmartBackpacksPlayerStats() {
    }

    private SmartBackpacksPlayerStats(Map<String, Map<String, Long>> players) {
        players.forEach((id, values) -> this.players.put(id, new HashMap<>(values)));
    }

    public static SmartBackpacksPlayerStats forPlayer(ServerPlayer player) {
        return player.level().getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, ID);
    }

    private Map<String, Map<String, Long>> snapshot() {
        Map<String, Map<String, Long>> copy = new HashMap<>();
        this.players.forEach((id, values) -> copy.put(id, Map.copyOf(values)));
        return Map.copyOf(copy);
    }

    public long get(UUID player, String counter) {
        return this.players.getOrDefault(player.toString(), Map.of()).getOrDefault(counter, 0L);
    }

    public long add(UUID player, String counter, long amount) {
        if (amount <= 0) return get(player, counter);
        Map<String, Long> values = this.players.computeIfAbsent(player.toString(), ignored -> new HashMap<>());
        long current = values.getOrDefault(counter, 0L);
        long next = amount > Long.MAX_VALUE - current ? Long.MAX_VALUE : current + amount;
        values.put(counter, next);
        setDirty();
        return next;
    }
    private static SmartBackpacksPlayerStats load(CompoundTag tag, HolderLookup.Provider registries) {
        return CODEC.parse(NbtOps.INSTANCE, tag).result().orElseGet(SmartBackpacksPlayerStats::new);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CODEC.encodeStart(NbtOps.INSTANCE, this).result()
                .filter(CompoundTag.class::isInstance).map(CompoundTag.class::cast).ifPresent(tag::merge);
        return tag;
    }

}
