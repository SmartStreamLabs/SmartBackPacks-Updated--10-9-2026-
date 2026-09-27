package net.neoforged.neoforge.event.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class PlayerInteractEvent {
    private final Player entity;
    private final Level level;
    private final BlockPos pos;
    private final Direction face;
    private boolean canceled;
    private InteractionResult cancellationResult = InteractionResult.PASS;

    public PlayerInteractEvent(Player entity, Level level, BlockPos pos, Direction face) {
        this.entity = entity;
        this.level = level;
        this.pos = pos;
        this.face = face;
    }

    public Player getEntity() {
        return entity;
    }

    public Level getLevel() {
        return level;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Direction getFace() {
        return face;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public InteractionResult getCancellationResult() {
        return cancellationResult;
    }

    public void setCancellationResult(InteractionResult cancellationResult) {
        this.cancellationResult = cancellationResult;
    }

    public static final class RightClickBlock extends PlayerInteractEvent {
        private final InteractionHand hand;

        public RightClickBlock(Player entity, Level level, BlockPos pos, Direction face) {
            this(entity, level, pos, face, InteractionHand.MAIN_HAND);
        }

        public RightClickBlock(Player entity, Level level, BlockPos pos, Direction face, InteractionHand hand) {
            super(entity, level, pos, face);
            this.hand = hand;
        }

        public InteractionHand getHand() {
            return this.hand;
        }
    }
}
