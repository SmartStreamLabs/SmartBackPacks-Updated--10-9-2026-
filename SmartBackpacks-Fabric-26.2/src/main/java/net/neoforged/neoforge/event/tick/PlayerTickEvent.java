package net.neoforged.neoforge.event.tick;

import net.minecraft.world.entity.player.Player;

public class PlayerTickEvent {
   private final Player entity;

   public PlayerTickEvent(Player entity) {
      this.entity = entity;
   }

   public Player getEntity() {
      return this.entity;
   }

   public static final class Post extends PlayerTickEvent {
      public Post(Player entity) {
         super(entity);
      }
   }
}
