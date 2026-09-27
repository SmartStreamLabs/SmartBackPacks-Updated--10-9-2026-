package com.teamsmartstreamlabs.smartbackpacks.worldgen;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.TickEvent;

public final class CampTreeClearance {
    private static final Queue<Pending> PENDING = new ConcurrentLinkedQueue<>();

    private CampTreeClearance() {
    }

    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.isNewChunk() || !(event.getLevel() instanceof ServerLevel level)) return;
        // ChunkEvent.Load fires before promotion to FULL, so world access waits for the next server tick.
        event.getChunk().getAllStarts().forEach((structure, start) -> {
            if (!(structure instanceof BackpackerCampStructure)) return;
            for (var piece : start.getPieces()) {
                if (piece instanceof BackpackerCampPiece camp) PENDING.add(new Pending(level, camp));
            }
        });
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Pending pending;
        while ((pending = PENDING.poll()) != null) {
            Pending current = pending;
            if (current.level().getServer() != event.getServer()) continue;
            current.piece().clearLateTrees(current.level());
        }
    }

    private record Pending(ServerLevel level, BackpackerCampPiece piece) {
    }
}
