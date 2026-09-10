package com.teamsmartstreamlabs.smartbackpacks.fabric.client;

import com.teamsmartstreamlabs.smartbackpacks.client.SmartBackpacksClient;
import net.fabricmc.api.ClientModInitializer;

public final class SmartBackpacksFabricClient implements ClientModInitializer {
   public void onInitializeClient() {
      SmartBackpacksClient.initClient();
   }
}
