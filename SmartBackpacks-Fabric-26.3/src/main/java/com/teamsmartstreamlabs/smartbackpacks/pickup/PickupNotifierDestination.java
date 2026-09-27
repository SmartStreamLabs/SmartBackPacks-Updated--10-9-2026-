package com.teamsmartstreamlabs.smartbackpacks.pickup;

public enum PickupNotifierDestination {
    MAIN_STORAGE("hud.smartbackpacks.pickup_notifier.destination.main_storage"),
    QUIVER_STORAGE("hud.smartbackpacks.pickup_notifier.destination.quiver_storage"),
    RESCUE_STORAGE("hud.smartbackpacks.pickup_notifier.destination.rescue_storage"),
    PROCESSING("hud.smartbackpacks.pickup_notifier.destination.processing");

    private final String translationKey;

    PickupNotifierDestination(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return this.translationKey;
    }
}
