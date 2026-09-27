package net.neoforged.neoforge.event.level;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEvent {
   public static final class EntityPlaceEvent extends BlockEvent {
      private final Entity entity;
      private final BlockState placedBlock;

      public EntityPlaceEvent(Entity entity, BlockState placedBlock) {
         this.entity = entity;
         this.placedBlock = placedBlock;
      }

      public Entity getEntity() {
         return this.entity;
      }

      public BlockState getPlacedBlock() {
         return this.placedBlock;
      }
   }
}
