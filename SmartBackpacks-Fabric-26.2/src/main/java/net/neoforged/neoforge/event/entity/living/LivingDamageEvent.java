package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class LivingDamageEvent {
   public static final class Pre extends LivingDamageEvent {
      private final LivingEntity entity;
      private final DamageSource source;
      private float newDamage;

      public Pre(LivingEntity entity, DamageSource source, float originalDamage) {
         this.entity = entity;
         this.source = source;
         this.newDamage = originalDamage;
      }

      public LivingEntity getEntity() {
         return this.entity;
      }

      public DamageSource getSource() {
         return this.source;
      }

      public float getNewDamage() {
         return this.newDamage;
      }

      public void setNewDamage(float newDamage) {
         this.newDamage = newDamage;
      }
   }
}
