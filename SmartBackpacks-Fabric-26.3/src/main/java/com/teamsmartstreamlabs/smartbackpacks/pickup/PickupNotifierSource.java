package com.teamsmartstreamlabs.smartbackpacks.pickup;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;

public enum PickupNotifierSource {
    WORLD_PICKUP,
    MANUAL_TRANSFER,
    UPGRADE_ROUTING,
    AUTOMATION,
    PROCESSING_RESULT,
    CONTAINER_TRANSFER;

    public boolean isEnabledByConfig() {
        return switch (this) {
            case WORLD_PICKUP -> SmartBackpacksConfig.pickupNotifierShowWorldPickups();
            case MANUAL_TRANSFER -> SmartBackpacksConfig.pickupNotifierShowManualTransfers();
            case UPGRADE_ROUTING -> SmartBackpacksConfig.pickupNotifierShowUpgradeRouting();
            case AUTOMATION -> SmartBackpacksConfig.pickupNotifierShowAutomation();
            case PROCESSING_RESULT -> SmartBackpacksConfig.pickupNotifierShowProcessingResults();
            case CONTAINER_TRANSFER -> SmartBackpacksConfig.pickupNotifierShowContainerTransfers();
        };
    }
}
