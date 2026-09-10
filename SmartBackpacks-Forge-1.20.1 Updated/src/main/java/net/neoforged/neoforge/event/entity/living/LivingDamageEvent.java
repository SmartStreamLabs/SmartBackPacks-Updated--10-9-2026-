package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class LivingDamageEvent {
    private final LivingEntity entity;
    private final DamageSource source;

    public LivingDamageEvent(LivingEntity entity, DamageSource source) {
        this.entity = entity;
        this.source = source;
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    public DamageSource getSource() {
        return this.source;
    }

    public static final class Pre extends LivingDamageEvent {
        private float newDamage;

        public Pre(LivingEntity entity, DamageSource source, float newDamage) {
            super(entity, source);
            this.newDamage = newDamage;
        }

        public float getNewDamage() {
            return this.newDamage;
        }

        public void setNewDamage(float newDamage) {
            this.newDamage = newDamage;
        }
    }
}
