package com.teamsmartstreamlabs.smartbackpacks.registry;

import com.teamsmartstreamlabs.smartbackpacks.blockentity.PlacedBackpackBlockEntity;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class ModCapabilities {
    private ModCapabilities() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.PLACED_BACKPACK.get(),
                (PlacedBackpackBlockEntity blockEntity, Direction side) -> side == null
                        ? blockEntity.getItemHandler()
                        : blockEntity.getSidedHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.PLACED_BACKPACK.get(),
                (PlacedBackpackBlockEntity blockEntity, Direction side) -> side == null
                        ? blockEntity.getFluidHandler()
                        : blockEntity.getSidedFluidHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.PLACED_BACKPACK.get(),
                (PlacedBackpackBlockEntity blockEntity, Direction side) -> side == null
                        ? blockEntity.getEnergyStorage()
                        : blockEntity.getSidedEnergyStorage(side)
        );
    }
}
