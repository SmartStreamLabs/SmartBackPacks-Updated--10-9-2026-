package net.neoforged.neoforge.event.entity.living;

import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;

public class LivingDropsEvent {
   private final LivingEntity entity;
   private final List<ItemEntity> drops;

   public LivingDropsEvent(LivingEntity entity, List<ItemEntity> drops) {
      this.entity = entity;
      this.drops = drops;
   }

   public LivingEntity getEntity() {
      return this.entity;
   }

   public List<ItemEntity> getDrops() {
      return this.drops;
   }
}
