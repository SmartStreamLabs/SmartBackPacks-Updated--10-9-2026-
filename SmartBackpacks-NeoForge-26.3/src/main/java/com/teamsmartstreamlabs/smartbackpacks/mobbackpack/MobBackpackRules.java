package com.teamsmartstreamlabs.smartbackpacks.mobbackpack;

public final class MobBackpackRules {
    private MobBackpackRules() {
    }

    public static boolean canRoll(boolean featureEnabled, boolean mobEnabled,
            boolean naturalSpawnsOnly, boolean allowedNaturalSpawn) {
        return featureEnabled && mobEnabled && (!naturalSpawnsOnly || allowedNaturalSpawn);
    }

    public static boolean wins(double roll, double chance) {
        double clampedChance = Math.max(0.0D, Math.min(1.0D, chance));
        return roll >= 0.0D && roll < clampedChance;
    }
}
