package com.teamsmartstreamlabs.smartbackpacks.client.screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.mobbackpack.MobBackpackType;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.TrashProtectionLevel;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;

final class SmartBackpacksConfigEntries {
    private static final String PREFIX = "config.smartbackpacks.";
    private static final List<String> DYE_COLORS = Arrays.stream(DyeColor.values())
            .map(DyeColor::getName)
            .toList();

    private SmartBackpacksConfigEntries() {
    }

    static List<ConfigEntry> create() {
        List<ConfigEntry> entries = new ArrayList<>();

        entries.add(bool("wear_on_back", Category.GENERAL, "general", Scope.SERVER, true,
                SmartBackpacksConfig::canWearBackpackOnBack, SmartBackpacksConfig::setCanWearBackpackOnBack,
                "wear equipped back chest slot"));
        entries.add(bool("open_worn_backpacks", Category.GENERAL, "general", Scope.SERVER, false,
                SmartBackpacksConfig::allowOtherPlayersToOpenWornBackpacks, SmartBackpacksConfig::setAllowOtherPlayersToOpenWornBackpacks,
                "other players access worn"));
        entries.add(bool("curios_back_slot", Category.BACKPACKS, "wearing", Scope.SERVER, true,
                SmartBackpacksConfig::allowBackpackInCuriosSlot, SmartBackpacksConfig::setAllowBackpackInCuriosSlot,
                "curios compatibility accessory back slot"));
        entries.add(bool("nested_backpacks", Category.BACKPACKS, "storage", Scope.SERVER, false,
                SmartBackpacksConfig::allowBackpackInBackpack, SmartBackpacksConfig::setAllowBackpackInBackpack,
                "nested backpack in backpack storage"));

        entries.add(bool("pickup_enabled", Category.UPGRADES, "pickup", Scope.SERVER, true,
                SmartBackpacksConfig::pickupNotifierEnabled, SmartBackpacksConfig::setPickupNotifierEnabled,
                "pickup notifier hud"));
        entries.add(bool("pickup_world", Category.UPGRADES, "pickup", Scope.SERVER, true,
                SmartBackpacksConfig::pickupNotifierShowWorldPickupsRaw, SmartBackpacksConfig::setPickupNotifierShowWorldPickups,
                "world item pickups"));
        entries.add(bool("pickup_manual", Category.UPGRADES, "pickup", Scope.SERVER, false,
                SmartBackpacksConfig::pickupNotifierShowManualTransfersRaw, SmartBackpacksConfig::setPickupNotifierShowManualTransfers,
                "manual transfers"));
        entries.add(bool("pickup_upgrade_routing", Category.UPGRADES, "pickup", Scope.SERVER, true,
                SmartBackpacksConfig::pickupNotifierShowUpgradeRoutingRaw, SmartBackpacksConfig::setPickupNotifierShowUpgradeRouting,
                "upgrade routing"));
        entries.add(bool("pickup_automation", Category.UPGRADES, "pickup", Scope.SERVER, false,
                SmartBackpacksConfig::pickupNotifierShowAutomationRaw, SmartBackpacksConfig::setPickupNotifierShowAutomation,
                "automation insertion"));
        entries.add(bool("pickup_processing", Category.UPGRADES, "pickup", Scope.SERVER, true,
                SmartBackpacksConfig::pickupNotifierShowProcessingResultsRaw, SmartBackpacksConfig::setPickupNotifierShowProcessingResults,
                "processing results"));
        entries.add(bool("pickup_container", Category.UPGRADES, "pickup", Scope.SERVER, false,
                SmartBackpacksConfig::pickupNotifierShowContainerTransfersRaw, SmartBackpacksConfig::setPickupNotifierShowContainerTransfers,
                "container restock transfers"));
        entries.add(bool("pickup_name", Category.UPGRADES, "pickup", Scope.CLIENT, true,
                SmartBackpacksConfig::pickupNotifierShowBackpackName, SmartBackpacksConfig::setPickupNotifierShowBackpackName,
                "backpack name display"));
        entries.add(bool("pickup_destination", Category.UPGRADES, "pickup", Scope.CLIENT, true,
                SmartBackpacksConfig::pickupNotifierShowDestination, SmartBackpacksConfig::setPickupNotifierShowDestination,
                "destination name display"));
        entries.add(bool("pickup_total", Category.UPGRADES, "pickup", Scope.CLIENT, true,
                SmartBackpacksConfig::pickupNotifierShowTotalStoredAmount, SmartBackpacksConfig::setPickupNotifierShowTotalStoredAmount,
                "stored total amount display"));
        entries.add(integer("pickup_display_ticks", Category.UPGRADES, "pickup", Scope.CLIENT, 60,
                SmartBackpacksConfig::pickupNotifierDisplayTicks, SmartBackpacksConfig::setPickupNotifierDisplayTicks,
                20, 300, 10, Unit.TICKS, "duration lifetime"));
        entries.add(integer("pickup_grouping_ticks", Category.UPGRADES, "pickup", Scope.CLIENT, 40,
                SmartBackpacksConfig::pickupNotifierGroupingWindowTicks, SmartBackpacksConfig::setPickupNotifierGroupingWindowTicks,
                0, 200, 5, Unit.TICKS, "merge grouping window"));
        entries.add(integer("pickup_visible", Category.UPGRADES, "pickup", Scope.CLIENT, 5,
                SmartBackpacksConfig::pickupNotifierMaxVisible, SmartBackpacksConfig::setPickupNotifierMaxVisible,
                1, 20, 1, Unit.ENTRIES, "maximum visible lines"));

        entries.add(integer("magnet_radius", Category.UPGRADES, "magnet", Scope.SERVER, 10,
                SmartBackpacksConfig::magnetRadius, SmartBackpacksConfig::setMagnetRadius,
                1, 128, 1, Unit.BLOCKS, "pickup collection range radius"));
        entries.add(integer("advanced_magnet_radius", Category.UPGRADES, "advanced_magnet", Scope.SERVER, 35,
                SmartBackpacksConfig::advancedMagnetRadius, SmartBackpacksConfig::setAdvancedMagnetRadius,
                1, 256, 1, Unit.BLOCKS, "pickup collection range radius"));

        entries.add(bool("quiver_multiple", Category.UPGRADES, "quiver", Scope.SERVER, true,
                SmartBackpacksConfig::allowMultipleQuiverUpgrades, SmartBackpacksConfig::setAllowMultipleQuiverUpgrades,
                "multiple upgrades"));
        entries.add(bool("quiver_rockets", Category.UPGRADES, "quiver", Scope.SERVER, true,
                SmartBackpacksConfig::allowQuiverFireworkRockets, SmartBackpacksConfig::setAllowQuiverFireworkRockets,
                "firework rockets projectile"));
        entries.add(bool("quiver_modded", Category.UPGRADES, "quiver", Scope.SERVER, true,
                SmartBackpacksConfig::allowQuiverModdedProjectileTags, SmartBackpacksConfig::setAllowQuiverModdedProjectileTags,
                "modded projectile tags"));

        entries.add(integer("trash_delay", Category.UPGRADES, "trash", Scope.SERVER, 5,
                SmartBackpacksConfig::trashCanDeletionDelaySeconds, SmartBackpacksConfig::setTrashCanDeletionDelaySeconds,
                0, 30, 1, Unit.SECONDS, "deletion pending delay"));
        entries.add(bool("trash_on_close", Category.UPGRADES, "trash", Scope.SERVER, true,
                SmartBackpacksConfig::trashCanDeletePendingItemOnClose, SmartBackpacksConfig::setTrashCanDeletePendingItemOnClose,
                "delete pending close"));
        entries.add(bool("trash_delete_now", Category.UPGRADES, "trash", Scope.SERVER, true,
                SmartBackpacksConfig::trashCanEnableDeleteNowButton, SmartBackpacksConfig::setTrashCanEnableDeleteNowButton,
                "instant delete button"));
        entries.add(bool("trash_multiple", Category.UPGRADES, "trash", Scope.SERVER, false,
                SmartBackpacksConfig::allowMultipleTrashCanUpgrades, SmartBackpacksConfig::setAllowMultipleTrashCanUpgrades,
                "multiple upgrades"));
        entries.add(enumeration("trash_protection", Category.UPGRADES, "trash", Scope.SERVER,
                TrashProtectionLevel.VALUABLE_ITEMS, SmartBackpacksConfig::trashCanProtectionLevel,
                value -> SmartBackpacksConfig.setTrashCanProtectionLevel((TrashProtectionLevel) value),
                List.of(TrashProtectionLevel.values()), "valuable protection safety"));
        entries.add(bool("trash_confirm_modded", Category.UPGRADES, "trash", Scope.SERVER, true,
                SmartBackpacksConfig::trashCanConfirmModdedItems, SmartBackpacksConfig::setTrashCanConfirmModdedItems,
                "confirm modded items"));
        entries.add(bool("trash_confirm_containers", Category.UPGRADES, "trash", Scope.SERVER, true,
                SmartBackpacksConfig::trashCanConfirmContainerItems, SmartBackpacksConfig::setTrashCanConfirmContainerItems,
                "confirm containers contents"));
        entries.add(bool("trash_confirm_damaged", Category.UPGRADES, "trash", Scope.SERVER, true,
                SmartBackpacksConfig::trashCanConfirmDamagedItems, SmartBackpacksConfig::setTrashCanConfirmDamagedItems,
                "confirm damaged durability"));

        entries.add(bool("rescue_enabled", Category.UPGRADES, "rescue", Scope.SERVER, true,
                SmartBackpacksConfig::rescueUpgradeEnabled, SmartBackpacksConfig::setRescueUpgradeEnabled,
                "emergency survival"));
        entries.add(bool("rescue_totem", Category.UPGRADES, "rescue", Scope.SERVER, true,
                SmartBackpacksConfig::rescueAllowBackpackTotemRaw, SmartBackpacksConfig::setRescueAllowBackpackTotem,
                "totem undying"));
        entries.add(bool("rescue_golden_apple", Category.UPGRADES, "rescue", Scope.SERVER, true,
                SmartBackpacksConfig::rescueAllowGoldenAppleRaw, SmartBackpacksConfig::setRescueAllowGoldenApple,
                "golden apple"));
        entries.add(bool("rescue_enchanted_apple", Category.UPGRADES, "rescue", Scope.SERVER, false,
                SmartBackpacksConfig::rescueAllowEnchantedGoldenAppleRaw, SmartBackpacksConfig::setRescueAllowEnchantedGoldenApple,
                "enchanted golden apple"));
        entries.add(bool("rescue_fall", Category.UPGRADES, "rescue", Scope.SERVER, true,
                SmartBackpacksConfig::rescueAllowFallRaw, SmartBackpacksConfig::setRescueAllowFall,
                "fall damage"));
        entries.add(bool("rescue_lava", Category.UPGRADES, "rescue", Scope.SERVER, true,
                SmartBackpacksConfig::rescueAllowLavaRaw, SmartBackpacksConfig::setRescueAllowLava,
                "lava danger"));
        entries.add(bool("rescue_fire", Category.UPGRADES, "rescue", Scope.SERVER, true,
                SmartBackpacksConfig::rescueAllowFireRaw, SmartBackpacksConfig::setRescueAllowFire,
                "fire burning"));
        entries.add(bool("rescue_water_rules", Category.UPGRADES, "rescue", Scope.SERVER, true,
                SmartBackpacksConfig::rescueRespectDimensionWaterRestrictions, SmartBackpacksConfig::setRescueRespectDimensionWaterRestrictions,
                "dimension water restrictions nether"));
        entries.add(bool("rescue_notifications", Category.UPGRADES, "rescue", Scope.SERVER, true,
                SmartBackpacksConfig::rescueEnableNotifications, SmartBackpacksConfig::setRescueEnableNotifications,
                "messages feedback"));
        entries.add(bool("rescue_multiple", Category.UPGRADES, "rescue", Scope.SERVER, false,
                SmartBackpacksConfig::allowMultipleRescueUpgrades, SmartBackpacksConfig::setAllowMultipleRescueUpgrades,
                "multiple upgrades"));
        entries.add(integer("rescue_global_cooldown", Category.UPGRADES, "rescue", Scope.SERVER, 5,
                SmartBackpacksConfig::rescueGlobalCooldownSeconds, SmartBackpacksConfig::setRescueGlobalCooldownSeconds,
                0, 300, 5, Unit.SECONDS, "global cooldown"));
        entries.add(integer("rescue_totem_cooldown", Category.UPGRADES, "rescue", Scope.SERVER, 60,
                SmartBackpacksConfig::rescueTotemCooldownSeconds, SmartBackpacksConfig::setRescueTotemCooldownSeconds,
                0, 900, 10, Unit.SECONDS, "totem cooldown"));
        entries.add(integer("rescue_apple_cooldown", Category.UPGRADES, "rescue", Scope.SERVER, 30,
                SmartBackpacksConfig::rescueGoldenAppleCooldownSeconds, SmartBackpacksConfig::setRescueGoldenAppleCooldownSeconds,
                0, 900, 10, Unit.SECONDS, "apple cooldown"));
        entries.add(integer("rescue_fall_cooldown", Category.UPGRADES, "rescue", Scope.SERVER, 10,
                SmartBackpacksConfig::rescueFallCooldownSeconds, SmartBackpacksConfig::setRescueFallCooldownSeconds,
                0, 900, 10, Unit.SECONDS, "fall cooldown"));
        entries.add(integer("rescue_lava_cooldown", Category.UPGRADES, "rescue", Scope.SERVER, 20,
                SmartBackpacksConfig::rescueLavaCooldownSeconds, SmartBackpacksConfig::setRescueLavaCooldownSeconds,
                0, 900, 10, Unit.SECONDS, "lava fire cooldown"));
        entries.add(integer("rescue_fall_scan", Category.UPGRADES, "rescue", Scope.SERVER, 32,
                SmartBackpacksConfig::rescueMaximumFallScanDistance, SmartBackpacksConfig::setRescueMaximumFallScanDistance,
                4, 128, 4, Unit.BLOCKS, "maximum scan distance"));

        entries.add(bool("builder_enabled", Category.UPGRADES, "builder", Scope.SERVER, true,
                SmartBackpacksConfig::builderUpgradeEnabled, SmartBackpacksConfig::setBuilderUpgradeEnabled,
                "refill building"));
        entries.add(bool("builder_keep_full", Category.UPGRADES, "builder", Scope.SERVER, true,
                SmartBackpacksConfig::builderAllowKeepFullModeRaw, SmartBackpacksConfig::setBuilderAllowKeepFullMode,
                "keep full mode"));
        entries.add(bool("builder_offhand", Category.UPGRADES, "builder", Scope.SERVER, false,
                SmartBackpacksConfig::builderAllowOffhandRefillRaw, SmartBackpacksConfig::setBuilderAllowOffhandRefill,
                "offhand refill"));
        entries.add(bool("builder_modded", Category.UPGRADES, "builder", Scope.SERVER, true,
                SmartBackpacksConfig::builderAllowModdedBlocksRaw, SmartBackpacksConfig::setBuilderAllowModdedBlocks,
                "modded blocks"));
        entries.add(bool("builder_containers", Category.UPGRADES, "builder", Scope.SERVER, false,
                SmartBackpacksConfig::builderAllowContainerRefillRaw, SmartBackpacksConfig::setBuilderAllowContainerRefill,
                "container blocks"));
        entries.add(bool("builder_dangerous", Category.UPGRADES, "builder", Scope.SERVER, false,
                SmartBackpacksConfig::builderAllowDangerousRefillRaw, SmartBackpacksConfig::setBuilderAllowDangerousRefill,
                "dangerous blocks"));
        entries.add(bool("builder_multiple", Category.UPGRADES, "builder", Scope.SERVER, false,
                SmartBackpacksConfig::builderAllowMultipleUpgrades, SmartBackpacksConfig::setBuilderAllowMultipleUpgrades,
                "multiple upgrades"));
        entries.add(bool("builder_feedback", Category.UPGRADES, "builder", Scope.SERVER, true,
                SmartBackpacksConfig::builderFeedbackEnabled, SmartBackpacksConfig::setBuilderFeedbackEnabled,
                "messages feedback"));
        entries.add(integer("builder_anti_spam", Category.UPGRADES, "builder", Scope.SERVER, 2,
                SmartBackpacksConfig::builderAntiSpamDelayTicks, SmartBackpacksConfig::setBuilderAntiSpamDelayTicks,
                1, 20, 1, Unit.TICKS, "delay performance"));

        entries.add(bool("torch_enabled", Category.UPGRADES, "torch", Scope.SERVER, true,
                SmartBackpacksConfig::torchPlacerUpgradeEnabled, SmartBackpacksConfig::setTorchPlacerUpgradeEnabled,
                "light placer"));
        entries.add(bool("torch_modded", Category.UPGRADES, "torch", Scope.SERVER, true,
                SmartBackpacksConfig::torchPlacerAllowModdedLightsRaw, SmartBackpacksConfig::setTorchPlacerAllowModdedLights,
                "modded lights"));
        entries.add(bool("torch_multiple", Category.UPGRADES, "torch", Scope.SERVER, false,
                SmartBackpacksConfig::torchPlacerAllowMultipleUpgrades, SmartBackpacksConfig::setTorchPlacerAllowMultipleUpgrades,
                "multiple upgrades"));
        entries.add(bool("torch_adventure", Category.UPGRADES, "torch", Scope.SERVER, false,
                SmartBackpacksConfig::torchPlacerAllowAdventureRaw, SmartBackpacksConfig::setTorchPlacerAllowAdventure,
                "adventure mode"));
        entries.add(bool("torch_feedback", Category.UPGRADES, "torch", Scope.SERVER, false,
                SmartBackpacksConfig::torchPlacerFeedbackEnabled, SmartBackpacksConfig::setTorchPlacerFeedbackEnabled,
                "messages feedback"));
        entries.add(integer("torch_check_ticks", Category.UPGRADES, "torch", Scope.SERVER, 10,
                SmartBackpacksConfig::torchPlacerCheckIntervalTicks, SmartBackpacksConfig::setTorchPlacerCheckIntervalTicks,
                1, 200, 1, Unit.TICKS, "light check interval"));
        entries.add(integer("torch_placement_cooldown", Category.UPGRADES, "torch", Scope.SERVER, 20,
                SmartBackpacksConfig::torchPlacerPlacementCooldownTicks, SmartBackpacksConfig::setTorchPlacerPlacementCooldownTicks,
                1, 600, 5, Unit.TICKS, "placement cooldown"));
        entries.add(integer("torch_failure_cooldown", Category.UPGRADES, "torch", Scope.SERVER, 20,
                SmartBackpacksConfig::torchPlacerFailureCooldownTicks, SmartBackpacksConfig::setTorchPlacerFailureCooldownTicks,
                1, 600, 5, Unit.TICKS, "failure cooldown"));

        entries.add(bool("capacity_enabled", Category.UPGRADES, "capacity", Scope.SERVER, true,
                SmartBackpacksConfig::capacityWarningUpgradeEnabled, SmartBackpacksConfig::setCapacityWarningUpgradeEnabled,
                "warning full"));
        entries.add(bool("capacity_stack_mode", Category.UPGRADES, "capacity", Scope.SERVER, true,
                SmartBackpacksConfig::capacityWarningAllowStackCapacityModeRaw, SmartBackpacksConfig::setCapacityWarningAllowStackCapacityMode,
                "stack capacity mode"));
        entries.add(bool("capacity_multiple", Category.UPGRADES, "capacity", Scope.SERVER, false,
                SmartBackpacksConfig::capacityWarningAllowMultipleUpgrades, SmartBackpacksConfig::setCapacityWarningAllowMultipleUpgrades,
                "multiple upgrades"));
        entries.add(integer("capacity_notify_cooldown", Category.UPGRADES, "capacity", Scope.SERVER, 5,
                SmartBackpacksConfig::capacityWarningNotificationCooldownSeconds, SmartBackpacksConfig::setCapacityWarningNotificationCooldownSeconds,
                0, 300, 5, Unit.SECONDS, "notification cooldown"));
        entries.add(integer("capacity_failed_cooldown", Category.UPGRADES, "capacity", Scope.SERVER, 60,
                SmartBackpacksConfig::capacityWarningFailedInsertionCooldownTicks, SmartBackpacksConfig::setCapacityWarningFailedInsertionCooldownTicks,
                1, 1200, 20, Unit.TICKS, "failed insertion cooldown"));

        entries.add(bool("lock_enabled", Category.UPGRADES, "locks", Scope.SERVER, true,
                SmartBackpacksConfig::itemLockUpgradeEnabled, SmartBackpacksConfig::setItemLockUpgradeEnabled,
                "item lock"));
        entries.add(bool("lock_multiple", Category.UPGRADES, "locks", Scope.SERVER, false,
                SmartBackpacksConfig::itemLockAllowMultipleUpgrades, SmartBackpacksConfig::setItemLockAllowMultipleUpgrades,
                "multiple upgrades"));
        entries.add(bool("lock_slots", Category.UPGRADES, "locks", Scope.SERVER, true,
                SmartBackpacksConfig::itemLockAllowSlotLocksRaw, SmartBackpacksConfig::setItemLockAllowSlotLocks,
                "slot locks"));
        entries.add(bool("lock_exact", Category.UPGRADES, "locks", Scope.SERVER, true,
                SmartBackpacksConfig::itemLockAllowItemLocksRaw, SmartBackpacksConfig::setItemLockAllowItemLocks,
                "exact item variants"));
        entries.add(bool("lock_types", Category.UPGRADES, "locks", Scope.SERVER, false,
                SmartBackpacksConfig::itemLockAllowTypeLocksRaw, SmartBackpacksConfig::setItemLockAllowTypeLocks,
                "item type locks"));
        entries.add(bool("lock_manual", Category.UPGRADES, "locks", Scope.SERVER, false,
                SmartBackpacksConfig::itemLockAllowManualAccessRaw, SmartBackpacksConfig::setItemLockAllowManualAccess,
                "manual access bypass"));
        entries.add(bool("lock_automation", Category.UPGRADES, "locks", Scope.SERVER, false,
                SmartBackpacksConfig::itemLockAllowAutomationAccessRaw, SmartBackpacksConfig::setItemLockAllowAutomationAccess,
                "automation access bypass"));

        entries.add(bool("link_enabled", Category.UPGRADES, "link", Scope.SERVER, true,
                SmartBackpacksConfig::backpackLinkUpgradeEnabled, SmartBackpacksConfig::setBackpackLinkUpgradeEnabled,
                "wireless teleport anchor"));
        entries.add(bool("link_multiple", Category.UPGRADES, "link", Scope.SERVER, false,
                SmartBackpacksConfig::backpackLinkAllowMultipleUpgrades, SmartBackpacksConfig::setBackpackLinkAllowMultipleUpgrades,
                "multiple upgrades"));
        entries.add(bool("link_public", Category.UPGRADES, "link", Scope.SERVER, true,
                SmartBackpacksConfig::backpackLinkAllowPublicAnchors, SmartBackpacksConfig::setBackpackLinkAllowPublicAnchors,
                "public anchors"));
        entries.add(bool("link_cross_dimension", Category.UPGRADES, "link", Scope.SERVER, true,
                SmartBackpacksConfig::backpackLinkAllowCrossDimension, SmartBackpacksConfig::setBackpackLinkAllowCrossDimension,
                "cross dimension"));
        entries.add(integer("link_destinations", Category.UPGRADES, "link", Scope.SERVER, 4,
                SmartBackpacksConfig::backpackLinkMaxDestinations, SmartBackpacksConfig::setBackpackLinkMaxDestinations,
                1, 32, 1, Unit.ENTRIES, "maximum links destinations"));
        entries.add(integer("link_safe_radius", Category.UPGRADES, "link", Scope.SERVER, 4,
                SmartBackpacksConfig::backpackLinkSafeArrivalRadius, SmartBackpacksConfig::setBackpackLinkSafeArrivalRadius,
                1, 8, 1, Unit.BLOCKS, "safe arrival radius"));
        entries.add(integer("link_base_xp", Category.UPGRADES, "link", Scope.SERVER, 1,
                SmartBackpacksConfig::backpackLinkSameDimensionBaseXpLevels, SmartBackpacksConfig::setBackpackLinkSameDimensionBaseXpLevels,
                0, 100, 1, Unit.LEVELS, "same dimension xp cost"));
        entries.add(integer("link_distance_xp", Category.UPGRADES, "link", Scope.SERVER, 1,
                SmartBackpacksConfig::backpackLinkDistanceXpPer1000Blocks, SmartBackpacksConfig::setBackpackLinkDistanceXpPer1000Blocks,
                0, 100, 1, Unit.LEVELS, "distance xp cost 1000 blocks"));
        entries.add(integer("link_cross_xp", Category.UPGRADES, "link", Scope.SERVER, 8,
                SmartBackpacksConfig::backpackLinkCrossDimensionXpLevels, SmartBackpacksConfig::setBackpackLinkCrossDimensionXpLevels,
                0, 100, 1, Unit.LEVELS, "cross dimension xp cost"));
        entries.add(integer("link_max_xp", Category.UPGRADES, "link", Scope.SERVER, 30,
                SmartBackpacksConfig::backpackLinkMaxXpLevelCost, SmartBackpacksConfig::setBackpackLinkMaxXpLevelCost,
                0, 1000, 5, Unit.LEVELS, "maximum xp cost"));

        entries.add(integer("pickup_queue", Category.PERFORMANCE, "pickup", Scope.CLIENT, 50,
                SmartBackpacksConfig::pickupNotifierQueueLimit, SmartBackpacksConfig::setPickupNotifierQueueLimit,
                5, 200, 5, Unit.ENTRIES, "notification queue memory"));
        entries.add(integer("capacity_tracked", Category.PERFORMANCE, "capacity", Scope.SERVER, 16,
                SmartBackpacksConfig::capacityWarningMaxBackpacksTrackedPerPlayer, SmartBackpacksConfig::setCapacityWarningMaxBackpacksTrackedPerPlayer,
                1, 64, 1, Unit.ENTRIES, "tracked backpacks player"));
        entries.add(integer("capacity_check_ticks", Category.PERFORMANCE, "capacity", Scope.SERVER, 20,
                SmartBackpacksConfig::capacityWarningCheckIntervalTicks, SmartBackpacksConfig::setCapacityWarningCheckIntervalTicks,
                1, 600, 5, Unit.TICKS, "scan interval"));
        entries.add(integer("capacity_hud_ticks", Category.PERFORMANCE, "capacity", Scope.SERVER, 100,
                SmartBackpacksConfig::capacityWarningHudRefreshTicks, SmartBackpacksConfig::setCapacityWarningHudRefreshTicks,
                20, 1200, 20, Unit.TICKS, "hud refresh packet interval"));

        entries.add(bool("network_enabled", Category.STORAGE_NETWORK, "network", Scope.SERVER, true,
                SmartBackpacksConfig::storageNetworkEnabled, SmartBackpacksConfig::setStorageNetworkEnabled,
                "storage controller cable network"));
        entries.add(integer("network_path", Category.STORAGE_NETWORK, "network", Scope.SERVER, 30,
                SmartBackpacksConfig::storageNetworkMaxCablePath, SmartBackpacksConfig::setStorageNetworkMaxCablePath,
                1, 128, 1, Unit.BLOCKS, "maximum cable path steps distance"));
        entries.add(integer("network_nodes", Category.STORAGE_NETWORK, "network", Scope.SERVER, 4096,
                SmartBackpacksConfig::storageNetworkMaxNodes, SmartBackpacksConfig::setStorageNetworkMaxNodes,
                128, 32768, 128, Unit.NODES, "maximum nodes bfs performance safety"));
        entries.add(bool("monitor_enabled", Category.STORAGE_NETWORK, "monitor", Scope.SERVER, true,
                SmartBackpacksConfig::storageMonitorEnabled, SmartBackpacksConfig::setStorageMonitorEnabled,
                "remote terminal storage monitor"));
        entries.add(bool("monitor_cross_dimension", Category.STORAGE_NETWORK, "monitor", Scope.SERVER, true,
                SmartBackpacksConfig::storageMonitorCrossDimensionAccess, SmartBackpacksConfig::setStorageMonitorCrossDimensionAccess,
                "remote access loaded controller chunk"));

        entries.add(bool("mob_enabled", Category.MOB_BACKPACKS, "mob_general", Scope.SERVER, true,
                SmartBackpacksConfig::mobBackpacksEnabled, SmartBackpacksConfig::setMobBackpacksEnabled,
                "hostile mobs spawn backpack"));
        entries.add(percent("mob_spawn_chance", Category.MOB_BACKPACKS, "mob_general", 0.01D,
                SmartBackpacksConfig::mobBackpackSpawnChance, SmartBackpacksConfig::setMobBackpackSpawnChance,
                0.0D, 1.0D, 0.001D, false, "global spawn chance"));
        entries.add(percent("mob_drop_chance", Category.MOB_BACKPACKS, "mob_general", 1.0D,
                SmartBackpacksConfig::mobBackpackDropChance, SmartBackpacksConfig::setMobBackpackDropChance,
                0.0D, 1.0D, 0.01D, false, "drop loot chance"));
        entries.add(bool("mob_natural_only", Category.MOB_BACKPACKS, "mob_general", Scope.SERVER, true,
                SmartBackpacksConfig::mobBackpackNaturalSpawnsOnly, SmartBackpacksConfig::setMobBackpackNaturalSpawnsOnly,
                "natural chunk patrol spawn only spawner egg command"));
        entries.add(integer("mob_min_loot", Category.MOB_BACKPACKS, "mob_general", Scope.SERVER, 2,
                SmartBackpacksConfig::mobBackpackMinLootEntries, SmartBackpacksConfig::setMobBackpackMinLootEntries,
                0, 5, 1, Unit.ENTRIES, "minimum loot entries"));
        entries.add(integer("mob_max_loot", Category.MOB_BACKPACKS, "mob_general", Scope.SERVER, 5,
                SmartBackpacksConfig::mobBackpackMaxLootEntries, SmartBackpacksConfig::setMobBackpackMaxLootEntries,
                0, 5, 1, Unit.ENTRIES, "maximum loot entries"));

        for (MobBackpackType type : MobBackpackType.values()) {
            Component mobName = Component.translatable("entity.minecraft." + type.id());
            entries.add(bool("mob_" + type.id() + "_enabled", Category.MOB_BACKPACKS, "mob_eligible", Scope.SERVER, true,
                    () -> SmartBackpacksConfig.mobBackpackMobEnabled(type),
                    value -> SmartBackpacksConfig.setMobBackpackMobEnabled(type, value),
                    type.id() + " mob eligible", mobName, Component.translatable(PREFIX + "mob.enabled.desc", mobName)));
            entries.add(percent("mob_" + type.id() + "_chance", Category.MOB_BACKPACKS, "mob_advanced",
                    -1.0D, () -> SmartBackpacksConfig.mobBackpackMobChance(type),
                    value -> SmartBackpacksConfig.setMobBackpackMobChance(type, value),
                    -1.0D, 1.0D, 0.001D, true, type.id() + " override",
                    Component.translatable(PREFIX + "mob.chance", mobName),
                    Component.translatable(PREFIX + "mob.chance.desc", mobName)));
            entries.add(dye("mob_" + type.id() + "_color", Category.MOB_BACKPACKS, "mob_advanced",
                    type.defaultColor().getName(), () -> SmartBackpacksConfig.mobBackpackMobColor(type),
                    value -> SmartBackpacksConfig.setMobBackpackMobColor(type, value),
                    type.id() + " dye color",
                    Component.translatable(PREFIX + "mob.color", mobName),
                    Component.translatable(PREFIX + "mob.color.desc", mobName)));
        }

        addTierWeight(entries, BackpackTier.LEATHER, 750);
        addTierWeight(entries, BackpackTier.COAL, 100);
        addTierWeight(entries, BackpackTier.COPPER, 80);
        addTierWeight(entries, BackpackTier.IRON, 50);
        addTierWeight(entries, BackpackTier.GOLD, 15);
        addTierWeight(entries, BackpackTier.EMERALD, 5);

        return List.copyOf(entries);
    }

    private static void addTierWeight(List<ConfigEntry> entries, BackpackTier tier, int defaultValue) {
        String name = tier.name().toLowerCase(Locale.ROOT);
        Component tierName = Component.translatable("config.smartbackpacks.tier." + name);
        entries.add(integer("mob_tier_" + name, Category.MOB_BACKPACKS, "mob_tiers", Scope.SERVER, defaultValue,
                () -> SmartBackpacksConfig.mobBackpackTierWeight(tier),
                value -> SmartBackpacksConfig.setMobBackpackTierWeight(tier, value),
                0, 100000, 5, Unit.WEIGHT, name + " relative tier weight",
                Component.translatable(PREFIX + "mob.tier_weight", tierName),
                Component.translatable(PREFIX + "mob.tier_weight.desc")));
    }

    private static ConfigEntry bool(String id, Category category, String section, Scope scope, boolean defaultValue,
            BooleanSupplier getter, BooleanConsumer setter, String aliases) {
        return bool(id, category, section, scope, defaultValue, getter, setter, aliases,
                settingTitle(id), settingDescription(id));
    }

    private static ConfigEntry bool(String id, Category category, String section, Scope scope, boolean defaultValue,
            BooleanSupplier getter, BooleanConsumer setter, String aliases, Component title, Component description) {
        return new ConfigEntry(id, category, section, scope, Kind.BOOLEAN, Unit.NONE, defaultValue,
                getter::getAsBoolean, value -> setter.accept((Boolean) value),
                0.0D, 1.0D, 1.0D, List.of(), aliases, title, description);
    }

    private static ConfigEntry integer(String id, Category category, String section, Scope scope, int defaultValue,
            IntSupplier getter, IntConsumer setter, int min, int max, int step, Unit unit, String aliases) {
        return integer(id, category, section, scope, defaultValue, getter, setter, min, max, step, unit, aliases,
                settingTitle(id), settingDescription(id));
    }

    private static ConfigEntry integer(String id, Category category, String section, Scope scope, int defaultValue,
            IntSupplier getter, IntConsumer setter, int min, int max, int step, Unit unit, String aliases,
            Component title, Component description) {
        return new ConfigEntry(id, category, section, scope, Kind.INTEGER, unit, defaultValue,
                getter::getAsInt, value -> setter.accept((Integer) value),
                min, max, step, List.of(), aliases, title, description);
    }

    private static ConfigEntry percent(String id, Category category, String section, double defaultValue,
            DoubleSupplier getter, DoubleConsumer setter, double min, double max, double step,
            boolean allowGlobal, String aliases) {
        return percent(id, category, section, defaultValue, getter, setter, min, max, step, allowGlobal, aliases,
                settingTitle(id), settingDescription(id));
    }

    private static ConfigEntry percent(String id, Category category, String section, double defaultValue,
            DoubleSupplier getter, DoubleConsumer setter, double min, double max, double step,
            boolean allowGlobal, String aliases, Component title, Component description) {
        return new ConfigEntry(id, category, section, Scope.SERVER,
                allowGlobal ? Kind.PERCENT_OVERRIDE : Kind.PERCENT, Unit.PERCENT, defaultValue,
                getter::getAsDouble, value -> setter.accept((Double) value),
                min, max, step, List.of(), aliases, title, description);
    }

    private static ConfigEntry enumeration(String id, Category category, String section, Scope scope,
            Object defaultValue, Supplier<?> getter, Consumer<Object> setter, List<?> choices, String aliases) {
        return new ConfigEntry(id, category, section, scope, Kind.ENUM, Unit.NONE, defaultValue,
                getter, setter, 0.0D, choices.size() - 1.0D, 1.0D, choices, aliases,
                settingTitle(id), settingDescription(id));
    }

    private static ConfigEntry dye(String id, Category category, String section, String defaultValue,
            Supplier<String> getter, Consumer<String> setter, String aliases, Component title, Component description) {
        return new ConfigEntry(id, category, section, Scope.SERVER, Kind.DYE, Unit.NONE, defaultValue,
                getter, value -> setter.accept((String) value), 0.0D, DYE_COLORS.size() - 1.0D, 1.0D,
                DYE_COLORS, aliases, title, description);
    }

    private static Component settingTitle(String id) {
        return Component.translatable(PREFIX + "setting." + id);
    }

    private static Component settingDescription(String id) {
        return Component.translatable(PREFIX + "setting." + id + ".desc");
    }

    enum Category {
        GENERAL("general"),
        BACKPACKS("backpacks"),
        UPGRADES("upgrades"),
        STORAGE_NETWORK("storage_network"),
        MOB_BACKPACKS("mob_backpacks"),
        PERFORMANCE("performance");

        private final String id;

        Category(String id) {
            this.id = id;
        }

        Component title() {
            return Component.translatable(PREFIX + "category." + this.id);
        }

        Component description() {
            return Component.translatable(PREFIX + "category." + this.id + ".desc");
        }
    }

    enum Scope {
        SERVER,
        CLIENT
    }

    enum Kind {
        BOOLEAN,
        INTEGER,
        PERCENT,
        PERCENT_OVERRIDE,
        ENUM,
        DYE
    }

    enum Unit {
        NONE(""),
        TICKS("ticks"),
        SECONDS("seconds"),
        BLOCKS("blocks"),
        ENTRIES("entries"),
        LEVELS("levels"),
        NODES("nodes"),
        WEIGHT("weight"),
        PERCENT("percent");

        private final String id;

        Unit(String id) {
            this.id = id;
        }
    }

    static final class ConfigEntry {
        private final String id;
        private final Category category;
        private final String section;
        private final Scope scope;
        private final Kind kind;
        private final Unit unit;
        private final Object defaultValue;
        private final Supplier<?> getter;
        private final Consumer<Object> setter;
        private final double min;
        private final double max;
        private final double step;
        private final List<?> choices;
        private final String aliases;
        private final Component title;
        private final Component description;

        private ConfigEntry(String id, Category category, String section, Scope scope, Kind kind, Unit unit,
                Object defaultValue, Supplier<?> getter, Consumer<Object> setter, double min, double max,
                double step, List<?> choices, String aliases, Component title, Component description) {
            this.id = id;
            this.category = category;
            this.section = section;
            this.scope = scope;
            this.kind = kind;
            this.unit = unit;
            this.defaultValue = defaultValue;
            this.getter = getter;
            this.setter = setter;
            this.min = min;
            this.max = max;
            this.step = step;
            this.choices = choices;
            this.aliases = aliases;
            this.title = title;
            this.description = description;
        }

        String id() {
            return this.id;
        }

        Category category() {
            return this.category;
        }

        String section() {
            return this.section;
        }

        Scope scope() {
            return this.scope;
        }

        Kind kind() {
            return this.kind;
        }

        Object defaultValue() {
            return this.defaultValue;
        }

        Object read() {
            return this.getter.get();
        }

        void apply(Object value) {
            this.setter.accept(value);
        }

        Component title() {
            return this.title;
        }

        Component description() {
            return this.description;
        }

        Component sectionTitle() {
            return Component.translatable(PREFIX + "section." + this.section);
        }

        Component tooltip(boolean readOnly) {
            var tooltip = Component.empty()
                    .append(this.description)
                    .append("\n")
                    .append(Component.translatable(PREFIX + "detail.default", this.valueText(this.defaultValue)));
            if (this.kind == Kind.INTEGER || this.kind == Kind.PERCENT || this.kind == Kind.PERCENT_OVERRIDE) {
                Object minimum;
                Object maximum;
                if (this.kind == Kind.INTEGER) {
                    minimum = Integer.valueOf((int) this.min);
                    maximum = Integer.valueOf((int) this.max);
                } else {
                    minimum = Double.valueOf(this.min);
                    maximum = Double.valueOf(this.max);
                }
                tooltip.append("\n").append(Component.translatable(PREFIX + "detail.range",
                        this.valueText(minimum), this.valueText(maximum)));
            }
            tooltip.append("\n").append(Component.translatable(PREFIX + "detail.scope."
                    + this.scope.name().toLowerCase(Locale.ROOT)));
            if (readOnly) {
                tooltip.append("\n").append(Component.translatable(PREFIX + "detail.server_controlled"));
            }
            return tooltip;
        }

        Component valueText(Object value) {
            if (this.kind == Kind.BOOLEAN) {
                return Component.translatable(PREFIX + ((Boolean) value ? "value.on" : "value.off"));
            }
            if (this.kind == Kind.PERCENT_OVERRIDE && ((Number) value).doubleValue() < 0.0D) {
                return Component.translatable(PREFIX + "value.use_global");
            }
            if (this.kind == Kind.PERCENT || this.kind == Kind.PERCENT_OVERRIDE) {
                return Component.literal(trim(((Number) value).doubleValue() * 100.0D) + "%");
            }
            if (this.kind == Kind.DYE) {
                return Component.translatable("color.minecraft." + value);
            }
            if (this.kind == Kind.ENUM) {
                String enumName = ((Enum<?>) value).name().toLowerCase(Locale.ROOT);
                return Component.translatable(PREFIX + "value.enum." + enumName);
            }
            if (this.unit == Unit.NONE) {
                return Component.literal(value.toString());
            }
            return Component.translatable(PREFIX + "value." + this.unit.id, value);
        }

        String editText(Object value) {
            if (this.kind == Kind.PERCENT_OVERRIDE && ((Number) value).doubleValue() < 0.0D) {
                return "";
            }
            if (this.kind == Kind.PERCENT || this.kind == Kind.PERCENT_OVERRIDE) {
                return trim(((Number) value).doubleValue() * 100.0D);
            }
            return value.toString();
        }

        Object parse(String value) {
            try {
                if (this.kind == Kind.INTEGER) {
                    return Mth.clamp(Integer.parseInt(value.trim()), (int) this.min, (int) this.max);
                }
                if (this.kind == Kind.PERCENT || this.kind == Kind.PERCENT_OVERRIDE) {
                    if (value.isBlank() && this.kind == Kind.PERCENT_OVERRIDE) {
                        return -1.0D;
                    }
                    return Mth.clamp(Double.parseDouble(value.trim().replace("%", "")) / 100.0D, this.min, this.max);
                }
            } catch (NumberFormatException ignored) {
                return null;
            }
            return null;
        }

        Object adjusted(Object current, int direction, boolean accelerated) {
            if (this.kind == Kind.INTEGER) {
                int amount = (int) this.step * (accelerated ? 10 : 1);
                return Mth.clamp(((Number) current).intValue() + direction * amount, (int) this.min, (int) this.max);
            }
            if (this.kind == Kind.PERCENT || this.kind == Kind.PERCENT_OVERRIDE) {
                double currentValue = ((Number) current).doubleValue();
                if (this.kind == Kind.PERCENT_OVERRIDE && currentValue < 0.0D) {
                    return direction > 0 ? 0.0D : -1.0D;
                }
                double amount = this.step * (accelerated ? 10.0D : 1.0D);
                double adjusted = currentValue + direction * amount;
                if (this.kind == Kind.PERCENT_OVERRIDE && direction < 0 && adjusted < 0.0D) {
                    return -1.0D;
                }
                return Math.max(this.min, Math.min(this.max, Math.round(adjusted * 10000.0D) / 10000.0D));
            }
            return current;
        }

        Object next(Object current, int direction) {
            if (this.choices.isEmpty()) {
                return current;
            }
            int index = this.choices.indexOf(current);
            int next = Math.floorMod(index + direction, this.choices.size());
            return this.choices.get(next);
        }

        boolean matches(String query) {
            if (query.isBlank()) {
                return true;
            }
            String searchable = this.id + " " + this.aliases + " " + this.title.getString() + " "
                    + this.description.getString() + " " + this.category.title().getString() + " "
                    + this.sectionTitle().getString();
            return searchable.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT).trim());
        }

        private static String trim(double value) {
            if (Math.abs(value - Math.rint(value)) < 0.00001D) {
                return Long.toString(Math.round(value));
            }
            return String.format(Locale.ROOT, "%.1f", value);
        }
    }

    @FunctionalInterface
    private interface BooleanConsumer {
        void accept(boolean value);
    }

    @FunctionalInterface
    private interface DoubleSupplier {
        double getAsDouble();
    }

    @FunctionalInterface
    private interface DoubleConsumer {
        void accept(double value);
    }
}
