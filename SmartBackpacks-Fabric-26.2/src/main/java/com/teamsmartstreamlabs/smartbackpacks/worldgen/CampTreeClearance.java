package com.teamsmartstreamlabs.smartbackpacks.worldgen;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;

public final class CampTreeClearance {
    private static final Queue<Pending> PENDING = new ConcurrentLinkedQueue<>();

    private CampTreeClearance() {
    }

    public static void register() {
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> {
            if (!generated) return;
            chunk.getAllStarts().forEach((structure, start) -> {
                if (!(structure instanceof BackpackerCampStructure)) return;
                for (var piece : start.getPieces()) {
                    if (piece instanceof BackpackerCampPiece camp) PENDING.add(new Pending(level, camp));
                }
            });
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Pending pending;
            while ((pending = PENDING.poll()) != null) {
                if (pending.level().getServer() == server) pending.piece().clearLateTrees(pending.level());
            }
        });
    }

    private record Pending(ServerLevel level, BackpackerCampPiece piece) {
    }
}
