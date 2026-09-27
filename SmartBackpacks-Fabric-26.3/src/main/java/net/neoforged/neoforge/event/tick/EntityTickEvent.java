package net.neoforged.neoforge.event.tick;

import net.minecraft.world.entity.Entity;

public class EntityTickEvent {
   private final Entity entity;

   public EntityTickEvent(Entity entity) {
      this.entity = entity;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public static final class Pre extends EntityTickEvent {
      public Pre(Entity entity) {
         super(entity);
      }
   }
}
