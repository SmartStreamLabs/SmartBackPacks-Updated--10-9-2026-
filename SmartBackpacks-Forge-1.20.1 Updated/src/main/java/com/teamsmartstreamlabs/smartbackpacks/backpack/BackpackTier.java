package com.teamsmartstreamlabs.smartbackpacks.backpack;

public enum BackpackTier {
    LEATHER("leather", 9),
    COAL("coal", 18),
    LAPIS("lapis", 27),
    REDSTONE("redstone", 36),
    QUARTZ("quartz", 45),
    COPPER("copper", 54),
    IRON("iron", 63),
    GOLD("gold", 72),
    EMERALD("emerald", 81),
    DIAMOND("diamond", 90),
    NETHERITE("netherite", 162),
    ANCIENT_NETHERITE("ancient_netherite", 600),
    ULTIMATE_DIAMOND("ultimate_diamond", 250),
    NETHERITE_VAULT("netherite_vault", 2500);

    private final String name;
    private final int slotCount;

    BackpackTier(String name, int slotCount) {
        this.name = name;
        this.slotCount = slotCount;
    }

    public int getSlotCount() {
        return this.slotCount;
    }

    public int getRowCount() {
        return (this.getSlotCount() + 8) / 9;
    }

    public int getColumnsInLastRow() {
        int remainder = this.getSlotCount() % 9;
        return remainder == 0 ? 9 : remainder;
    }

    public String getTranslationKey() {
        return "item.smartbackpacks." + this.name + "_backpack";
    }

    public static BackpackTier byIndex(int index) {
        BackpackTier[] tiers = values();
        if (index < 0 || index >= tiers.length) {
            return LEATHER;
        }
        return tiers[index];
    }
}
