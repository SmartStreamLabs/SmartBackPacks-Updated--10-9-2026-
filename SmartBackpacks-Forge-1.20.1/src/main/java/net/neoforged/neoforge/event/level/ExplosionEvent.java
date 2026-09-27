package net.neoforged.neoforge.event.level;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ExplosionEvent {
    private final Level level;

    public ExplosionEvent(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }

    public static final class Detonate extends ExplosionEvent {
        private final List<Entity> affectedEntities;
        private final List<BlockPos> affectedBlocks;

        public Detonate(Level level, List<Entity> affectedEntities, List<BlockPos> affectedBlocks) {
            super(level);
            this.affectedEntities = affectedEntities;
            this.affectedBlocks = affectedBlocks;
        }

        public List<Entity> getAffectedEntities() {
            return affectedEntities;
        }

        public List<BlockPos> getAffectedBlocks() {
            return affectedBlocks;
        }
    }
}
