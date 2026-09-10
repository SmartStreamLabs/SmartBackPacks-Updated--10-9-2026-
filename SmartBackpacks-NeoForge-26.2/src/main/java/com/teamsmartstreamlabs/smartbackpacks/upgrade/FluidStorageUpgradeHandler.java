package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.teamsmartstreamlabs.smartbackpacks.item.FluidStorageUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public final class FluidStorageUpgradeHandler {
    public static final int CAPACITY = 500_000;

    private FluidStorageUpgradeHandler() {
    }

    public static boolean isFluidUpgrade(ItemStack stack) {
        return stack.getItem() instanceof FluidStorageUpgradeItem;
    }

    public static FluidStack getFluid(ItemStack upgradeStack) {
        return upgradeStack.getOrDefault(ModDataComponents.FLUID_STORAGE_UPGRADE_DATA.get(), FluidStorageUpgradeData.DEFAULT).fluid().copy();
    }

    public static void setFluid(ItemStack upgradeStack, FluidStack fluid) {
        FluidStack sanitized = fluid.copy();
        sanitized.limitSize(CAPACITY);
        upgradeStack.set(ModDataComponents.FLUID_STORAGE_UPGRADE_DATA.get(), new FluidStorageUpgradeData(sanitized));
    }

    public static int fill(ItemStack upgradeStack, FluidStack resource, IFluidHandler.FluidAction action) {
        if (!isFluidUpgrade(upgradeStack) || resource.isEmpty()) {
            return 0;
        }

        FluidStack stored = getFluid(upgradeStack);
        if (!stored.isEmpty() && !FluidStack.isSameFluidSameComponents(stored, resource)) {
            return 0;
        }

        int filled = Math.min(CAPACITY - stored.getAmount(), resource.getAmount());
        if (filled <= 0) {
            return 0;
        }

        if (action.execute()) {
            FluidStack updated = stored.isEmpty() ? resource.copyWithAmount(filled) : stored.copy();
            if (!stored.isEmpty()) {
                updated.grow(filled);
            }
            setFluid(upgradeStack, updated);
        }

        return filled;
    }

    public static FluidStack drain(ItemStack upgradeStack, FluidStack resource, IFluidHandler.FluidAction action) {
        if (!isFluidUpgrade(upgradeStack) || resource.isEmpty()) {
            return FluidStack.EMPTY;
        }

        FluidStack stored = getFluid(upgradeStack);
        if (stored.isEmpty() || !FluidStack.isSameFluidSameComponents(stored, resource)) {
            return FluidStack.EMPTY;
        }

        return drain(upgradeStack, resource.getAmount(), action);
    }

    public static FluidStack drain(ItemStack upgradeStack, int maxDrain, IFluidHandler.FluidAction action) {
        if (!isFluidUpgrade(upgradeStack) || maxDrain <= 0) {
            return FluidStack.EMPTY;
        }

        FluidStack stored = getFluid(upgradeStack);
        if (stored.isEmpty()) {
            return FluidStack.EMPTY;
        }

        int drained = Math.min(maxDrain, stored.getAmount());
        FluidStack result = stored.copyWithAmount(drained);
        if (action.execute()) {
            stored.shrink(drained);
            setFluid(upgradeStack, stored.getAmount() > 0 ? stored : FluidStack.EMPTY);
        }
        return result;
    }

    public static int findFirstUpgradeSlot(NonNullList<ItemStack> upgrades) {
        for (int slot = 0; slot < upgrades.size(); slot++) {
            if (isFluidUpgrade(upgrades.get(slot))) {
                return slot;
            }
        }
        return -1;
    }
}
