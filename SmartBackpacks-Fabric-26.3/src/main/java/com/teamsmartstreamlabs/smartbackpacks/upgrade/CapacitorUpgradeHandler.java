package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.teamsmartstreamlabs.smartbackpacks.item.CapacitorUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public final class CapacitorUpgradeHandler {
    public static final int CAPACITY = 5_000_000;
    public static final int TRANSFER_RATE = 100_000;

    private CapacitorUpgradeHandler() {
    }

    public static boolean isCapacitorUpgrade(ItemStack stack) {
        return stack.getItem() instanceof CapacitorUpgradeItem;
    }

    public static int getEnergyStored(ItemStack upgradeStack) {
        return upgradeStack.getOrDefault(ModDataComponents.CAPACITOR_UPGRADE_DATA.get(), CapacitorUpgradeData.DEFAULT).energyStored();
    }

    public static void setEnergyStored(ItemStack upgradeStack, int energyStored) {
        int clamped = Mth.clamp(energyStored, 0, CAPACITY);
        upgradeStack.set(ModDataComponents.CAPACITOR_UPGRADE_DATA.get(), new CapacitorUpgradeData(clamped));
    }

    public static int receiveEnergy(ItemStack upgradeStack, int maxReceive, boolean simulate) {
        if (!isCapacitorUpgrade(upgradeStack) || maxReceive <= 0) {
            return 0;
        }

        int stored = getEnergyStored(upgradeStack);
        int received = Math.min(CAPACITY - stored, maxReceive);
        if (received <= 0) {
            return 0;
        }

        if (!simulate) {
            setEnergyStored(upgradeStack, stored + received);
        }
        return received;
    }

    public static int extractEnergy(ItemStack upgradeStack, int maxExtract, boolean simulate) {
        if (!isCapacitorUpgrade(upgradeStack) || maxExtract <= 0) {
            return 0;
        }

        int stored = getEnergyStored(upgradeStack);
        int extracted = Math.min(stored, maxExtract);
        if (extracted <= 0) {
            return 0;
        }

        if (!simulate) {
            setEnergyStored(upgradeStack, stored - extracted);
        }
        return extracted;
    }

    public static int findFirstUpgradeSlot(NonNullList<ItemStack> upgrades) {
        for (int slot = 0; slot < upgrades.size(); slot++) {
            if (isCapacitorUpgrade(upgrades.get(slot))) {
                return slot;
            }
        }
        return -1;
    }
}
