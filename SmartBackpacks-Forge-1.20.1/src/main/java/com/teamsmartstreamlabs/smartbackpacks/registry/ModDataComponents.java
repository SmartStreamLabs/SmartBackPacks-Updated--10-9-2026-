package com.teamsmartstreamlabs.smartbackpacks.registry;

import java.util.List;

import com.mojang.serialization.Codec;
import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.core.component.DataComponentType;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.component.ItemContainerContents;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.AutoFeedUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.AutoSmeltingUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.AutoToolUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BackpackLinkData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BlastFurnaceUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BrewingStandUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BuilderUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacitorUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacityWarningUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.ChunkLoaderUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FilterUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CourierDestinationData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FluidStorageUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FluidTransferUpgradeData;
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

public final class ModDataComponents {
    public static final KeyHandle<Boolean> NIGHT_VISION_UPGRADE_ENABLED = register("night_vision_upgrade_enabled", Codec.BOOL);
    public static final KeyHandle<Boolean> FLIGHT_UPGRADE_ENABLED = register("flight_upgrade_enabled", Codec.BOOL);
    public static final KeyHandle<DeathEmergencyKitData> DEATH_EMERGENCY_KIT_DATA =
            register("death_emergency_kit_data", DeathEmergencyKitData.CODEC);
    public static final KeyHandle<Long> FALL_PROTECTION_COOLDOWN_UNTIL =
            register("fall_protection_cooldown_until", Codec.LONG);
    public static final KeyHandle<ItemContainerContents> BACKPACK_UPGRADES =
            register("backpack_upgrades", ItemContainerContents.CODEC);
    public static final KeyHandle<List<Integer>> BACKPACK_STORAGE_OVERFLOW =
            register("backpack_storage_overflow", Codec.INT.listOf());
    public static final KeyHandle<BackpackLinkData> BACKPACK_LINK_DATA =
            register("backpack_link_data", BackpackLinkData.CODEC);
    public static final KeyHandle<LinkCrystalData> LINK_CRYSTAL_DATA =
            register("link_crystal_data", LinkCrystalData.CODEC);
    public static final KeyHandle<StorageMonitorLink> STORAGE_MONITOR_LINK =
            register("storage_monitor_link", StorageMonitorLink.CODEC);

    public static final KeyHandle<MagnetUpgradeData> MAGNET_UPGRADE_DATA =
            register("magnet_upgrade_data", MagnetUpgradeData.CODEC);
    public static final KeyHandle<PickupUpgradeData> PICKUP_UPGRADE_DATA =
            register("pickup_upgrade_data", PickupUpgradeData.CODEC);
    public static final KeyHandle<HopperUpgradeData> HOPPER_UPGRADE_DATA =
            register("hopper_upgrade_data", HopperUpgradeData.CODEC);
    public static final KeyHandle<AutoToolUpgradeData> AUTO_TOOL_UPGRADE_DATA =
            register("auto_tool_upgrade_data", AutoToolUpgradeData.CODEC);
    public static final KeyHandle<AutoFeedUpgradeData> AUTO_FEED_UPGRADE_DATA =
            register("auto_feed_upgrade_data", AutoFeedUpgradeData.CODEC);
    public static final KeyHandle<SurvivalAssistUpgradeData> SURVIVAL_ASSIST_UPGRADE_DATA =
            register("survival_assist_upgrade_data", SurvivalAssistUpgradeData.CODEC);
    public static final KeyHandle<AutoSmeltingUpgradeData> AUTO_SMELTING_UPGRADE_DATA =
            register("auto_smelting_upgrade_data", AutoSmeltingUpgradeData.CODEC);
    public static final KeyHandle<FurnaceUpgradeData> FURNACE_UPGRADE_DATA =
            register("furnace_upgrade_data", FurnaceUpgradeData.CODEC);
    public static final KeyHandle<BlastFurnaceUpgradeData> BLAST_FURNACE_UPGRADE_DATA =
            register("blast_furnace_upgrade_data", BlastFurnaceUpgradeData.CODEC);
    public static final KeyHandle<SmokerUpgradeData> SMOKER_UPGRADE_DATA =
            register("smoker_upgrade_data", SmokerUpgradeData.CODEC);
    public static final KeyHandle<BrewingStandUpgradeData> BREWING_STAND_UPGRADE_DATA =
            register("brewing_stand_upgrade_data", BrewingStandUpgradeData.CODEC);
    public static final KeyHandle<FilterUpgradeData> FILTER_UPGRADE_DATA =
            register("filter_upgrade_data", FilterUpgradeData.CODEC);
    public static final KeyHandle<CourierDestinationData> COURIER_DESTINATION_DATA =
            register("courier_destination_data", CourierDestinationData.CODEC);
    public static final KeyHandle<FluidStorageUpgradeData> FLUID_STORAGE_UPGRADE_DATA =
            register("fluid_storage_upgrade_data", FluidStorageUpgradeData.CODEC);
    public static final KeyHandle<FluidTransferUpgradeData> FLUID_TRANSFER_UPGRADE_DATA =
            register("fluid_transfer_upgrade_data", FluidTransferUpgradeData.CODEC);
    public static final KeyHandle<CapacitorUpgradeData> CAPACITOR_UPGRADE_DATA =
            register("capacitor_upgrade_data", CapacitorUpgradeData.CODEC);
    public static final KeyHandle<ChunkLoaderUpgradeData> CHUNK_LOADER_UPGRADE_DATA =
            register("chunk_loader_upgrade_data", ChunkLoaderUpgradeData.CODEC);
    public static final KeyHandle<JukeboxUpgradeData> JUKEBOX_UPGRADE_DATA =
            register("jukebox_upgrade_data", JukeboxUpgradeData.CODEC);
    public static final KeyHandle<VoidUpgradeData> VOID_UPGRADE_DATA =
            register("void_upgrade_data", VoidUpgradeData.CODEC);
    public static final KeyHandle<XpTransferUpgradeData> XP_TRANSFER_UPGRADE_DATA =
            register("xp_transfer_upgrade_data", XpTransferUpgradeData.CODEC);
    public static final KeyHandle<QuiverUpgradeData> QUIVER_UPGRADE_DATA =
            register("quiver_upgrade_data", QuiverUpgradeData.CODEC);
    public static final KeyHandle<QuickAccessWheelUpgradeData> QUICK_ACCESS_WHEEL_UPGRADE_DATA =
            register("quick_access_wheel_upgrade_data", QuickAccessWheelUpgradeData.CODEC);
    public static final KeyHandle<TrashCanUpgradeData> TRASH_CAN_UPGRADE_DATA =
            register("trash_can_upgrade_data", TrashCanUpgradeData.CODEC);
    public static final KeyHandle<RescueUpgradeData> RESCUE_UPGRADE_DATA =
            register("rescue_upgrade_data", RescueUpgradeData.CODEC);
    public static final KeyHandle<BuilderUpgradeData> BUILDER_UPGRADE_DATA =
            register("builder_upgrade_data", BuilderUpgradeData.CODEC);
    public static final KeyHandle<TorchPlacerUpgradeData> TORCH_PLACER_UPGRADE_DATA =
            register("torch_placer_upgrade_data", TorchPlacerUpgradeData.CODEC);
    public static final KeyHandle<CapacityWarningUpgradeData> CAPACITY_WARNING_UPGRADE_DATA =
            register("capacity_warning_upgrade_data", CapacityWarningUpgradeData.CODEC);
    public static final KeyHandle<ItemLockData> ITEM_LOCK_DATA =
            register("item_lock_data", ItemLockData.CODEC);

    private ModDataComponents() {
    }

    public static void register(Object ignored) {
        // 1.20.1 has no vanilla item component registry; ItemStackCompat stores these keys in NBT.
    }

    private static <T> KeyHandle<T> register(String path, Codec<T> codec) {
        return new KeyHandle<>(new DataComponentType<>(SmartBackpacks.MOD_ID + ":" + path, codec));
    }

    public record KeyHandle<T>(DataComponentType<T> value) {
        public DataComponentType<T> get() {
            return this.value;
        }
    }
}
