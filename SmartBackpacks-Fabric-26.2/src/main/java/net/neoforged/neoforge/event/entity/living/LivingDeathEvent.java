package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class LivingDeathEvent {
   private final LivingEntity entity;
   private final DamageSource source;
   private boolean canceled;

   public LivingDeathEvent(LivingEntity entity, DamageSource source) {
      this.entity = entity;
      this.source = source;
   }

   public LivingEntity getEntity() {
      return this.entity;
   }

   public DamageSource getSource() {
      return this.source;
   }

   public boolean isCanceled() {
      return this.canceled;
   }

   public void setCanceled(boolean canceled) {
      this.canceled = canceled;
   }
}
