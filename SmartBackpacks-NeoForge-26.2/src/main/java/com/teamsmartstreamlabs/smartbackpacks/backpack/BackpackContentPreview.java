package com.teamsmartstreamlabs.smartbackpacks.backpack;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record BackpackContentPreview(List<ItemStack> items, int usedSlots, int totalSlots) implements TooltipComponent {
    private static final int MAX_ITEMS = 12;

    public static BackpackContentPreview capture(ItemStack backpack, BackpackTier tier) {
        int capacity = tier.getSlotCount();
        int used = 0;
        List<ItemStack> shown = new ArrayList<>(MAX_ITEMS);
        for (ItemStack item : BackpackStackData.loadStorage(backpack, tier)) {
            if (!item.isEmpty()) {
                used++;
                if (shown.size() < MAX_ITEMS) {
                    shown.add(item.copy());
                }
            }
        }
        return new BackpackContentPreview(List.copyOf(shown), used, capacity);
    }
}
