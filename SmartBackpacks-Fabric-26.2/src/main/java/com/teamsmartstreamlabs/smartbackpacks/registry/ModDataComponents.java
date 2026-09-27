package com.teamsmartstreamlabs.smartbackpacks.registry;

import java.util.List;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.AutoToolUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.AutoFeedUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BackpackLinkData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BlastFurnaceUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BuilderUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.AutoSmeltingUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BrewingStandUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacitorUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacityWarningUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.ChunkLoaderUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FilterUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CourierDestinationData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FluidTransferUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FluidStorageUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FurnaceUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.HopperUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.ItemLockData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.JukeboxUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.LinkCrystalData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.MagnetUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.PickupUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.QuiverUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.QuickAccessWheelUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.RescueUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.DeathEmergencyKitData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.SmokerUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.SurvivalAssistUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.TorchPlacerUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.TrashCanUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.VoidUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.XpTransferUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.storage.StorageMonitorLink;

import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, SmartBackpacks.MOD_ID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> NIGHT_VISION_UPGRADE_ENABLED =
            COMPONENTS.registerComponentType("night_vision_upgrade_enabled", builder -> builder.persistent(Codec.BOOL));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> FLIGHT_UPGRADE_ENABLED =
            COMPONENTS.registerComponentType("flight_upgrade_enabled", builder -> builder.persistent(Codec.BOOL));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DeathEmergencyKitData>> DEATH_EMERGENCY_KIT_DATA =
            COMPONENTS.registerComponentType("death_emergency_kit_data", builder -> builder.persistent(DeathEmergencyKitData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> FALL_PROTECTION_COOLDOWN_UNTIL =
            COMPONENTS.registerComponentType("fall_protection_cooldown_until", builder -> builder.persistent(Codec.LONG));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> BACKPACK_UPGRADES =
            COMPONENTS.registerComponentType("backpack_upgrades", builder -> builder.persistent(ItemContainerContents.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<Integer>>> BACKPACK_STORAGE_OVERFLOW =
            COMPONENTS.registerComponentType("backpack_storage_overflow", builder -> builder.persistent(Codec.INT.listOf()));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BackpackLinkData>> BACKPACK_LINK_DATA =
            COMPONENTS.registerComponentType("backpack_link_data", builder -> builder.persistent(BackpackLinkData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LinkCrystalData>> LINK_CRYSTAL_DATA =
            COMPONENTS.registerComponentType("link_crystal_data", builder -> builder.persistent(LinkCrystalData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<StorageMonitorLink>> STORAGE_MONITOR_LINK =
            COMPONENTS.registerComponentType("storage_monitor_link", builder -> builder.persistent(StorageMonitorLink.CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MagnetUpgradeData>> MAGNET_UPGRADE_DATA =
            COMPONENTS.registerComponentType("magnet_upgrade_data", builder -> builder.persistent(MagnetUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PickupUpgradeData>> PICKUP_UPGRADE_DATA =
            COMPONENTS.registerComponentType("pickup_upgrade_data", builder -> builder.persistent(PickupUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<HopperUpgradeData>> HOPPER_UPGRADE_DATA =
            COMPONENTS.registerComponentType("hopper_upgrade_data", builder -> builder.persistent(HopperUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AutoToolUpgradeData>> AUTO_TOOL_UPGRADE_DATA =
            COMPONENTS.registerComponentType("auto_tool_upgrade_data", builder -> builder.persistent(AutoToolUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AutoFeedUpgradeData>> AUTO_FEED_UPGRADE_DATA =
            COMPONENTS.registerComponentType("auto_feed_upgrade_data", builder -> builder.persistent(AutoFeedUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SurvivalAssistUpgradeData>> SURVIVAL_ASSIST_UPGRADE_DATA =
            COMPONENTS.registerComponentType("survival_assist_upgrade_data", builder -> builder.persistent(SurvivalAssistUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AutoSmeltingUpgradeData>> AUTO_SMELTING_UPGRADE_DATA =
            COMPONENTS.registerComponentType("auto_smelting_upgrade_data", builder -> builder.persistent(AutoSmeltingUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FurnaceUpgradeData>> FURNACE_UPGRADE_DATA =
            COMPONENTS.registerComponentType("furnace_upgrade_data", builder -> builder.persistent(FurnaceUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlastFurnaceUpgradeData>> BLAST_FURNACE_UPGRADE_DATA =
            COMPONENTS.registerComponentType("blast_furnace_upgrade_data", builder -> builder.persistent(BlastFurnaceUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SmokerUpgradeData>> SMOKER_UPGRADE_DATA =
            COMPONENTS.registerComponentType("smoker_upgrade_data", builder -> builder.persistent(SmokerUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BrewingStandUpgradeData>> BREWING_STAND_UPGRADE_DATA =
            COMPONENTS.registerComponentType("brewing_stand_upgrade_data", builder -> builder.persistent(BrewingStandUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FilterUpgradeData>> FILTER_UPGRADE_DATA =
            COMPONENTS.registerComponentType("filter_upgrade_data", builder -> builder.persistent(FilterUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CourierDestinationData>> COURIER_DESTINATION_DATA =
            COMPONENTS.registerComponentType("courier_destination_data", builder -> builder.persistent(CourierDestinationData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FluidStorageUpgradeData>> FLUID_STORAGE_UPGRADE_DATA =
            COMPONENTS.registerComponentType("fluid_storage_upgrade_data", builder -> builder.persistent(FluidStorageUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FluidTransferUpgradeData>> FLUID_TRANSFER_UPGRADE_DATA =
            COMPONENTS.registerComponentType("fluid_transfer_upgrade_data", builder -> builder.persistent(FluidTransferUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CapacitorUpgradeData>> CAPACITOR_UPGRADE_DATA =
            COMPONENTS.registerComponentType("capacitor_upgrade_data", builder -> builder.persistent(CapacitorUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ChunkLoaderUpgradeData>> CHUNK_LOADER_UPGRADE_DATA =
            COMPONENTS.registerComponentType("chunk_loader_upgrade_data", builder -> builder.persistent(ChunkLoaderUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<JukeboxUpgradeData>> JUKEBOX_UPGRADE_DATA =
            COMPONENTS.registerComponentType("jukebox_upgrade_data", builder -> builder.persistent(JukeboxUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<VoidUpgradeData>> VOID_UPGRADE_DATA =
            COMPONENTS.registerComponentType("void_upgrade_data", builder -> builder.persistent(VoidUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<XpTransferUpgradeData>> XP_TRANSFER_UPGRADE_DATA =
            COMPONENTS.registerComponentType("xp_transfer_upgrade_data", builder -> builder.persistent(XpTransferUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<QuiverUpgradeData>> QUIVER_UPGRADE_DATA =
            COMPONENTS.registerComponentType("quiver_upgrade_data", builder -> builder.persistent(QuiverUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<QuickAccessWheelUpgradeData>> QUICK_ACCESS_WHEEL_UPGRADE_DATA =
            COMPONENTS.registerComponentType("quick_access_wheel_upgrade_data", builder -> builder.persistent(QuickAccessWheelUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TrashCanUpgradeData>> TRASH_CAN_UPGRADE_DATA =
            COMPONENTS.registerComponentType("trash_can_upgrade_data", builder -> builder.persistent(TrashCanUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RescueUpgradeData>> RESCUE_UPGRADE_DATA =
            COMPONENTS.registerComponentType("rescue_upgrade_data", builder -> builder.persistent(RescueUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BuilderUpgradeData>> BUILDER_UPGRADE_DATA =
            COMPONENTS.registerComponentType("builder_upgrade_data", builder -> builder.persistent(BuilderUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TorchPlacerUpgradeData>> TORCH_PLACER_UPGRADE_DATA =
            COMPONENTS.registerComponentType("torch_placer_upgrade_data", builder -> builder.persistent(TorchPlacerUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CapacityWarningUpgradeData>> CAPACITY_WARNING_UPGRADE_DATA =
            COMPONENTS.registerComponentType("capacity_warning_upgrade_data", builder -> builder.persistent(CapacityWarningUpgradeData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemLockData>> ITEM_LOCK_DATA =
            COMPONENTS.registerComponentType("item_lock_data", builder -> builder.persistent(ItemLockData.CODEC));

    private ModDataComponents() {
    }

    public static void register(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }
}
