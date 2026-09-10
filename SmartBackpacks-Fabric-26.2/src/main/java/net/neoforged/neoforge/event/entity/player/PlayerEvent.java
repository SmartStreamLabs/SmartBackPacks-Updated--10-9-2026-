package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PlayerEvent {
   private final Player entity;

   public PlayerEvent(Player entity) {
      this.entity = entity;
   }

   public Player getEntity() {
      return this.entity;
   }

   public static class ItemCraftedEvent extends PlayerEvent {
      private final ItemStack crafting;
      private final Container inventory;

      public ItemCraftedEvent(Player entity, ItemStack crafting, Container inventory) {
         super(entity);
         this.crafting = crafting;
         this.inventory = inventory;
      }

      public ItemStack getCrafting() {
         return this.crafting;
      }

      public Container getInventory() {
         return this.inventory;
      }
   }

   public static class PlayerLoggedOutEvent extends PlayerEvent {
      public PlayerLoggedOutEvent(Player entity) {
         super(entity);
      }
   }

   public static class PlayerRespawnEvent extends PlayerEvent {
      public PlayerRespawnEvent(Player entity) {
         super(entity);
      }
   }
}
