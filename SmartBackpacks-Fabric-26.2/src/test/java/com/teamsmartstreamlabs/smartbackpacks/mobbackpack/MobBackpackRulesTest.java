package com.teamsmartstreamlabs.smartbackpacks.mobbackpack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import java.util.Random;
import java.util.Set;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.DyeColor;
import org.junit.jupiter.api.Test;

class MobBackpackRulesTest {
    @Test
    void defaultSpawnChanceIsApproximatelyOnePercent() {
        Random random = new Random(0x5BACCAL);
        int successes = 0;
        for (int roll = 0; roll < 10_000; roll++) {
            if (MobBackpackRules.wins(random.nextDouble(), 0.01D)) successes++;
        }

        assertTrue(successes >= 50 && successes <= 150, "unexpected successes: " + successes);
    }

    @Test
    void chanceAndEligibilityBoundariesAreSafe() {
        assertFalse(MobBackpackRules.canRoll(false, true, true, true));
        assertFalse(MobBackpackRules.canRoll(true, false, true, true));
        assertFalse(MobBackpackRules.canRoll(true, true, true, false));
        assertTrue(MobBackpackRules.canRoll(true, true, true, true));
        assertTrue(MobBackpackRules.canRoll(true, true, false, false));
        assertFalse(MobBackpackRules.wins(0.0D, 0.0D));
        assertTrue(MobBackpackRules.wins(0.999999D, 1.0D));
        assertFalse(MobBackpackRules.wins(0.5D, -10.0D));
        assertTrue(MobBackpackRules.wins(0.5D, 10.0D));
    }

    @Test
    void onlySafeWorldSpawnReasonsAreAllowed() {
        Set<String> allowed = Set.of("NATURAL", "CHUNK_GENERATION", "PATROL");
        for (EntitySpawnReason reason : EntitySpawnReason.values()) {
            assertEquals(allowed.contains(reason.name()), MobBackpackHandler.isAllowedNaturalSpawn(reason), reason.name());
        }
    }

    @Test
    void defaultsMatchTheFeatureBalance() {
        assertTrue(SmartBackpacksConfig.mobBackpacksEnabled());
        assertEquals(0.01D, SmartBackpacksConfig.mobBackpackSpawnChance());
        assertEquals(1.0D, SmartBackpacksConfig.mobBackpackDropChance());
        assertTrue(SmartBackpacksConfig.mobBackpackNaturalSpawnsOnly());
        assertEquals(2, SmartBackpacksConfig.mobBackpackMinLootEntries());
        assertEquals(5, SmartBackpacksConfig.mobBackpackMaxLootEntries());
        assertEquals(750, SmartBackpacksConfig.mobBackpackTierWeight(BackpackTier.LEATHER));
        assertEquals(100, SmartBackpacksConfig.mobBackpackTierWeight(BackpackTier.COAL));
        assertEquals(80, SmartBackpacksConfig.mobBackpackTierWeight(BackpackTier.COPPER));
        assertEquals(50, SmartBackpacksConfig.mobBackpackTierWeight(BackpackTier.IRON));
        assertEquals(15, SmartBackpacksConfig.mobBackpackTierWeight(BackpackTier.GOLD));
        assertEquals(5, SmartBackpacksConfig.mobBackpackTierWeight(BackpackTier.EMERALD));
        assertEquals(0, SmartBackpacksConfig.mobBackpackTierWeight(BackpackTier.DIAMOND));
        assertEquals(0, SmartBackpacksConfig.mobBackpackTierWeight(BackpackTier.NETHERITE));
    }

    @Test
    void colorsMatchMobsAndInvalidValuesFallBack() {
        assertEquals(DyeColor.GREEN, MobBackpackType.ZOMBIE.defaultColor());
        assertEquals(DyeColor.YELLOW, MobBackpackType.HUSK.defaultColor());
        assertEquals(DyeColor.CYAN, MobBackpackType.DROWNED.defaultColor());
        assertEquals(DyeColor.LIGHT_GRAY, MobBackpackType.SKELETON.defaultColor());
        assertEquals(DyeColor.LIGHT_BLUE, MobBackpackType.STRAY.defaultColor());
        assertEquals(DyeColor.LIME, MobBackpackType.CREEPER.defaultColor());
        assertEquals(DyeColor.GRAY, MobBackpackType.PILLAGER.defaultColor());
        assertEquals(DyeColor.GRAY, MobBackpackType.VINDICATOR.defaultColor());
        assertEquals(DyeColor.PURPLE, MobBackpackType.WITCH.defaultColor());
        assertEquals(DyeColor.ORANGE, MobBackpackType.PIGLIN.defaultColor());
        assertEquals(DyeColor.PINK, MobBackpackType.ZOMBIFIED_PIGLIN.defaultColor());
        assertEquals(DyeColor.LIME,
                MobBackpackHandler.resolveConfiguredColor("LiMe", MobBackpackType.CREEPER));
        assertEquals(DyeColor.LIME,
                MobBackpackHandler.resolveConfiguredColor("not_a_color", MobBackpackType.CREEPER));
    }
}
