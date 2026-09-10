package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class LivingGetProjectileEvent {
   private final LivingEntity entity;
   private final ItemStack projectileWeaponItemStack;
   private ItemStack projectileItemStack;

   public LivingGetProjectileEvent(LivingEntity entity, ItemStack projectileWeaponItemStack, ItemStack projectileItemStack) {
      this.entity = entity;
      this.projectileWeaponItemStack = projectileWeaponItemStack;
      this.projectileItemStack = projectileItemStack;
   }

   public LivingEntity getEntity() {
      return this.entity;
   }

   public ItemStack getProjectileWeaponItemStack() {
      return this.projectileWeaponItemStack;
   }

   public ItemStack getProjectileItemStack() {
      return this.projectileItemStack;
   }

   public void setProjectileItemStack(ItemStack projectileItemStack) {
      this.projectileItemStack = projectileItemStack;
   }
}
