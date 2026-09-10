package net.neoforged.neoforge.network;

import com.teamsmartstreamlabs.smartbackpacks.network.ModPayloads;

public final class PacketDistributor {
   private PacketDistributor() {
   }

   public static void sendToServer(Object payload) {
      ModPayloads.sendToServer(payload);
   }

   public static void sendToPlayer(Object player, Object payload) {
      ModPayloads.sendToPlayer(player, payload);
   }
}
