package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.teamsmartstreamlabs.smartbackpacks.item.FluidTransferUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;

public final class FluidTransferUpgradeHandler {
    public static final int TRANSFER_AMOUNT = FluidType.BUCKET_VOLUME;
    public static final int TICK_INTERVAL = 8;

    private FluidTransferUpgradeHandler() {
    }

    public static boolean isFluidTransferUpgrade(ItemStack stack) {
        return stack.getItem() instanceof FluidTransferUpgradeItem;
    }

    public static FluidTransferUpgradeData getData(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.FLUID_TRANSFER_UPGRADE_DATA.get(), FluidTransferUpgradeData.DEFAULT);
    }

    public static void setData(ItemStack stack, FluidTransferUpgradeData data) {
        stack.set(ModDataComponents.FLUID_TRANSFER_UPGRADE_DATA.get(), data);
    }

    public static boolean allows(FluidTransferUpgradeData data, FluidStack candidate) {
        if (candidate.isEmpty()) {
            return false;
        }

        boolean hasFilters = false;
        for (ItemStack filterStack : ItemContainerContentsHelper.nonEmptyStream(data.fluidFilters()).toList()) {
            if (filterStack.isEmpty()) {
                continue;
            }

            FluidStack filterFluid = FluidUtil.getFluidContained(filterStack.copyWithCount(1)).orElse(FluidStack.EMPTY);
            if (filterFluid.isEmpty()) {
                continue;
            }

            hasFilters = true;
            if (FluidStack.isSameFluidSameComponents(filterFluid, candidate)) {
                return true;
            }
        }

        return !hasFilters;
    }

    public static int findFirstUpgradeSlot(NonNullList<ItemStack> upgrades) {
        for (int slot = 0; slot < upgrades.size(); slot++) {
            if (isFluidTransferUpgrade(upgrades.get(slot))) {
                return slot;
            }
        }
        return -1;
    }
}
