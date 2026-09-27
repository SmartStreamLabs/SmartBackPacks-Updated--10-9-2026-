package com.teamsmartstreamlabs.smartbackpacks.backpack;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

public record BackpackContentPreview(List<ItemStack> items, int usedSlots, int totalSlots) implements TooltipComponent {
    private static final int MAX_ITEMS = 12;

    public static BackpackContentPreview capture(ItemStack backpack, BackpackTier tier) {
        int capacity = tier.getSlotCount();
        int used = 0;
        List<ItemStack> shown = new ArrayList<>(MAX_ITEMS);
        BackpackStorageContents stored = backpack.get(ModDataComponents.BACKPACK_STORAGE.get());
        if (stored != null) {
            for (int slot = 0; slot < Math.min(capacity, stored.storedSlotCount()); slot++) {
                if (!stored.occupiedAt(slot)) {
                    continue;
                }
                used++;
                if (shown.size() < MAX_ITEMS) {
                    ItemStack copy = stored.copyStoredItemAt(slot);
                    int extra = stored.overflowAt(slot);
                    if (extra > 0) {
                        copy.setCount(BackpackStackData.restoreStorageCount(
                                copy.getCount(), extra, BackpackStackData.getStorageStackLimit(backpack, copy)));
                    }
                    shown.add(copy);
                }
            }
            return new BackpackContentPreview(List.copyOf(shown), used, capacity);
        }

        // Older backpacks still use the vanilla container component.
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
