package com.teamsmartstreamlabs.smartbackpacks.progress;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Compact, server-owned counters keyed by player UUID, independent of player entity lifetime. */
public final class SmartBackpacksPlayerStats extends SavedData {
    private static final Codec<Map<String, Map<String, Long>>> PLAYERS_CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, Codec.LONG));
    public static final Codec<SmartBackpacksPlayerStats> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PLAYERS_CODEC.optionalFieldOf("players", Map.of()).forGetter(SmartBackpacksPlayerStats::snapshot)
    ).apply(instance, SmartBackpacksPlayerStats::new));
    public static final SavedDataType<SmartBackpacksPlayerStats> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "player_stats"),
            SmartBackpacksPlayerStats::new, CODEC, DataFixTypes.LEVEL);

    private final Map<String, Map<String, Long>> players = new HashMap<>();

    public SmartBackpacksPlayerStats() {
    }

    private SmartBackpacksPlayerStats(Map<String, Map<String, Long>> players) {
        players.forEach((id, values) -> this.players.put(id, new HashMap<>(values)));
    }

    public static SmartBackpacksPlayerStats forPlayer(ServerPlayer player) {
        return player.level().getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
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
}
