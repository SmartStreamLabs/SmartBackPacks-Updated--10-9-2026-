package com.teamsmartstreamlabs.smartbackpacks.fabric;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import net.fabricmc.api.ModInitializer;

public final class SmartBackpacksFabric implements ModInitializer {
   public void onInitialize() {
      SmartBackpacksConfig.SPEC.load();
      SmartBackpacks.LOGGER.info("Loaded Smart Backpacks Fabric config");
      SmartBackpacks.initCommon();
   }
}
