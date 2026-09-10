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
   private final InteractionHand hand;
   private final BlockPos pos;
   private final Direction face;
   private boolean canceled;
   private InteractionResult cancellationResult = InteractionResult.PASS;

   public PlayerInteractEvent(Player entity, Level level, InteractionHand hand, BlockPos pos, Direction face) {
      this.entity = entity;
      this.level = level;
      this.hand = hand;
      this.pos = pos;
      this.face = face;
   }

   public Player getEntity() {
      return this.entity;
   }

   public Level getLevel() {
      return this.level;
   }

   public InteractionHand getHand() {
      return this.hand;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public Direction getFace() {
      return this.face;
   }

   public boolean isCanceled() {
      return this.canceled;
   }

   public void setCanceled(boolean canceled) {
      this.canceled = canceled;
   }

   public InteractionResult getCancellationResult() {
      return this.cancellationResult;
   }

   public void setCancellationResult(InteractionResult cancellationResult) {
      this.cancellationResult = cancellationResult;
   }

   public static final class RightClickBlock extends PlayerInteractEvent {
      public RightClickBlock(Player entity, Level level, InteractionHand hand, BlockPos pos, Direction face) {
         super(entity, level, hand, pos, face);
      }
   }
}
