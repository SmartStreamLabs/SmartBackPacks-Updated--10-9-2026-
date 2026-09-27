package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;

import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public record CapacityWarningSnapshot(
        double percentage,
        int displayPercentage,
        int occupiedSlots,
        int totalSlots,
        int freeSlots,
        CapacityWarningCalculationMode mode) {
    public static CapacityWarningSnapshot empty(CapacityWarningCalculationMode mode) {
        return new CapacityWarningSnapshot(0.0D, 0, 0, 0, 0, mode);
    }

    public static CapacityWarningSnapshot calculate(ItemStack backpack, BackpackTier tier, CapacityWarningCalculationMode mode) {
        if (backpack.isEmpty() || tier == null) {
            return empty(mode);
        }

        NonNullList<ItemStack> storage = BackpackStackData.loadStorage(backpack, tier);
        int totalSlots = storage.size();
        if (totalSlots <= 0) {
            return empty(mode);
        }

        int occupiedSlots = 0;
        double filledUnits = 0.0D;
        for (ItemStack stack : storage) {
            if (stack.isEmpty()) {
                continue;
            }

            occupiedSlots++;
            if (mode == CapacityWarningCalculationMode.STACK_CAPACITY) {
                int stackLimit = Math.max(1, BackpackStackData.getStorageStackLimit(backpack, stack));
                filledUnits += stackLimit <= 1 ? 1.0D : Mth.clamp(stack.getCount() / (double) stackLimit, 0.0D, 1.0D);
            }
        }

        double percentage = mode == CapacityWarningCalculationMode.STACK_CAPACITY
                ? filledUnits * 100.0D / totalSlots
                : occupiedSlots * 100.0D / totalSlots;
        percentage = Mth.clamp(percentage, 0.0D, 100.0D);
        int displayPercentage = percentage <= 0.0D
                ? 0
                : Mth.clamp((int) Math.ceil(percentage - 0.000001D), 1, 100);
        return new CapacityWarningSnapshot(percentage, displayPercentage, occupiedSlots, totalSlots,
                Math.max(0, totalSlots - occupiedSlots), mode);
    }

    public CapacityWarningState stateFor(CapacityWarningUpgradeData data) {
        if (data.threshold3Enabled() && this.percentage >= data.threshold3()) {
            return CapacityWarningState.FULL;
        }
        if (data.threshold2Enabled() && this.percentage >= data.threshold2()) {
            return CapacityWarningState.CRITICAL;
        }
        if (data.threshold1Enabled() && this.percentage >= data.threshold1()) {
            return CapacityWarningState.WARNING;
        }
        return CapacityWarningState.NORMAL;
    }
}
