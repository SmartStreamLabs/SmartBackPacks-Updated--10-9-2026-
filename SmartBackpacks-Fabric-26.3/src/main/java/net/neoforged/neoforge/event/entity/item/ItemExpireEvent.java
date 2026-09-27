package net.neoforged.neoforge.event.entity.item;

import net.minecraft.world.entity.item.ItemEntity;

public class ItemExpireEvent {
   private final ItemEntity entity;

   public ItemExpireEvent(ItemEntity entity) {
      this.entity = entity;
   }

   public ItemEntity getEntity() {
      return this.entity;
   }
}
