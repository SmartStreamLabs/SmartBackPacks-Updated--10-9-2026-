package com.teamsmartstreamlabs.smartbackpacks.loot;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootTable;

public enum AbandonedBackpackProfile {
    CAMP("camp", 0.08D, Pool.EARLY, 3, 8, "Forgotten Traveler's Backpack"),
    VILLAGE("village", 0.05D, Pool.EARLY, 3, 8, "Forgotten Traveler's Backpack"),
    MINESHAFT("mineshaft", 0.10D, Pool.EARLY, 3, 8, "Abandoned Mining Pack"),
    SHIPWRECK("shipwreck", 0.08D, Pool.EARLY, 3, 8, "Lost Sailor's Backpack"),
    DESERT_PYRAMID("desert_pyramid", 0.08D, Pool.MID, 5, 12, "Desert Explorer's Pack"),
    JUNGLE_TEMPLE("jungle_temple", 0.08D, Pool.MID, 5, 12, "Jungle Explorer's Pack"),
    STRONGHOLD("stronghold", 0.10D, Pool.MID, 5, 12, "Old Expedition Backpack"),
    RUINED_PORTAL("ruined_portal", 0.06D, Pool.EARLY, 3, 8, "Forgotten Traveler's Backpack"),
    ANCIENT_CITY("ancient_city", 0.12D, Pool.ENDGAME, 7, 16, "Lost Deep Explorer's Pack"),
    BASTION("bastion", 0.10D, Pool.ENDGAME, 7, 16, "Nether Expedition Pack"),
    END_CITY("end_city", 0.12D, Pool.ENDGAME, 7, 16, "End Explorer's Backpack");

    private final String id;
    private final double defaultChance;
    private final Pool pool;
    private final int minSlots;
    private final int maxSlots;
    private final String customName;

    AbandonedBackpackProfile(String id, double defaultChance, Pool pool, int minSlots, int maxSlots, String customName) {
        this.id = id;
        this.defaultChance = defaultChance;
        this.pool = pool;
        this.minSlots = minSlots;
        this.maxSlots = maxSlots;
        this.customName = customName;
    }

    public String id() { return this.id; }
    public double defaultChance() { return this.defaultChance; }
    public int minSlots() { return this.minSlots; }
    public int maxSlots() { return this.maxSlots; }
    public String customName() { return this.customName; }
    public boolean isEarly() { return this.pool == Pool.EARLY; }
    public boolean isEndgame() { return this.pool == Pool.ENDGAME; }

    public BackpackTier chooseTier(RandomSource random) {
        return this.pool.choose(random);
    }

    public static AbandonedBackpackProfile byId(String id) {
        for (AbandonedBackpackProfile profile : values()) {
            if (profile.id.equals(id)) return profile;
        }
        throw new IllegalArgumentException("Unknown abandoned backpack profile: " + id);
    }

    public static AbandonedBackpackProfile forChest(ResourceKey<LootTable> key) {
        if (!key.location().getNamespace().equals("minecraft")) return null;
        String path = key.location().getPath();
        if (path.startsWith("chests/village/")) return VILLAGE;
        if (path.equals("chests/abandoned_mineshaft")) return MINESHAFT;
        if (path.startsWith("chests/shipwreck_")) return SHIPWRECK;
        if (path.equals("chests/desert_pyramid")) return DESERT_PYRAMID;
        if (path.equals("chests/jungle_temple")) return JUNGLE_TEMPLE;
        if (path.startsWith("chests/stronghold_")) return STRONGHOLD;
        if (path.equals("chests/ruined_portal")) return RUINED_PORTAL;
        if (path.equals("chests/ancient_city") || path.equals("chests/ancient_city_ice_box")) return ANCIENT_CITY;
        if (path.startsWith("chests/bastion_")) return BASTION;
        if (path.equals("chests/end_city_treasure")) return END_CITY;
        return null;
    }

    private enum Pool {
        EARLY(new BackpackTier[] {BackpackTier.LEATHER, BackpackTier.COAL, BackpackTier.LAPIS, BackpackTier.COPPER, BackpackTier.IRON},
                new int[] {35, 25, 20, 15, 5}),
        MID(new BackpackTier[] {BackpackTier.COPPER, BackpackTier.IRON, BackpackTier.GOLD, BackpackTier.EMERALD, BackpackTier.DIAMOND},
                new int[] {25, 35, 20, 15, 5}),
        ENDGAME(new BackpackTier[] {BackpackTier.IRON, BackpackTier.GOLD, BackpackTier.EMERALD, BackpackTier.DIAMOND, BackpackTier.NETHERITE},
                new int[] {15, 20, 25, 35, 5});

        private final BackpackTier[] tiers;
        private final int[] weights;

        Pool(BackpackTier[] tiers, int[] weights) {
            this.tiers = tiers;
            this.weights = weights;
        }

        BackpackTier choose(RandomSource random) {
            int roll = random.nextInt(100);
            for (int index = 0; index < this.tiers.length; index++) {
                roll -= this.weights[index];
                if (roll < 0) return this.tiers[index];
            }
            return this.tiers[this.tiers.length - 1];
        }
    }
}
