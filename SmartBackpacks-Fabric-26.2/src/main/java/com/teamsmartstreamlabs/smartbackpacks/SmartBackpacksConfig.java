package com.teamsmartstreamlabs.smartbackpacks;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.loot.AbandonedBackpackProfile;
import com.teamsmartstreamlabs.smartbackpacks.mobbackpack.MobBackpackType;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.TrashProtectionLevel;

import java.util.EnumMap;
import java.util.Map;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class SmartBackpacksConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue CAN_WEAR_BACKPACK_ON_BACK;
    private static final ModConfigSpec.BooleanValue ALLOW_BACKPACK_IN_CURIOS_SLOT;
    private static final ModConfigSpec.BooleanValue ALLOW_BACKPACK_IN_BACKPACK;
    private static final ModConfigSpec.BooleanValue ALLOW_OTHER_PLAYERS_TO_OPEN_WORN_BACKPACKS;
    private static final ModConfigSpec.BooleanValue SHOW_WELCOME_MESSAGE;
    private static final ModConfigSpec.BooleanValue PICKUP_NOTIFIER_ENABLED;
    private static final ModConfigSpec.BooleanValue PICKUP_NOTIFIER_SHOW_WORLD_PICKUPS;
    private static final ModConfigSpec.BooleanValue PICKUP_NOTIFIER_SHOW_MANUAL_TRANSFERS;
    private static final ModConfigSpec.BooleanValue PICKUP_NOTIFIER_SHOW_UPGRADE_ROUTING;
    private static final ModConfigSpec.BooleanValue PICKUP_NOTIFIER_SHOW_AUTOMATION;
    private static final ModConfigSpec.BooleanValue PICKUP_NOTIFIER_SHOW_PROCESSING_RESULTS;
    private static final ModConfigSpec.BooleanValue PICKUP_NOTIFIER_SHOW_CONTAINER_TRANSFERS;
    private static final ModConfigSpec.BooleanValue PICKUP_NOTIFIER_SHOW_BACKPACK_NAME;
    private static final ModConfigSpec.BooleanValue PICKUP_NOTIFIER_SHOW_DESTINATION;
    private static final ModConfigSpec.BooleanValue PICKUP_NOTIFIER_SHOW_TOTAL_STORED_AMOUNT;
    private static final ModConfigSpec.IntValue PICKUP_NOTIFIER_DISPLAY_TICKS;
    private static final ModConfigSpec.IntValue PICKUP_NOTIFIER_GROUPING_WINDOW_TICKS;
    private static final ModConfigSpec.IntValue PICKUP_NOTIFIER_MAX_VISIBLE;
    private static final ModConfigSpec.IntValue PICKUP_NOTIFIER_QUEUE_LIMIT;
    private static final ModConfigSpec.BooleanValue BLOCK_DROP_MAGNET_PROTECTION_ENABLED;
    private static final ModConfigSpec.BooleanValue FLIGHT_UPGRADE_ENABLED;
    private static final ModConfigSpec.BooleanValue REPAIR_UPGRADE_ENABLED;
    private static final ModConfigSpec.BooleanValue FALL_PROTECTION_UPGRADE_ENABLED;
    private static final ModConfigSpec.BooleanValue DEATH_EMERGENCY_KIT_ENABLED;
    private static final ModConfigSpec.IntValue FALL_PROTECTION_COOLDOWN_SECONDS;
    private static final ModConfigSpec.IntValue FALL_PROTECTION_SLOW_FALLING_SECONDS;
    private static final ModConfigSpec.IntValue FALL_PROTECTION_MAX_CHARGES;
    private static final ModConfigSpec.IntValue FALL_PROTECTION_TRIGGER_HEALTH_MARGIN;
    private static final ModConfigSpec.IntValue REPAIR_UPGRADE_TICKS_PER_REPAIR;
    private static final ModConfigSpec.IntValue REPAIR_UPGRADE_DURABILITY_PER_CYCLE;
    private static final ModConfigSpec.IntValue REPAIR_UPGRADE_DURABILITY_PER_XP;
    private static final ModConfigSpec.IntValue BLOCK_DROP_MAGNET_PROTECTION_SECONDS;
    private static final ModConfigSpec.IntValue MAGNET_RADIUS;
    private static final ModConfigSpec.IntValue ADVANCED_MAGNET_RADIUS;
    private static final ModConfigSpec.IntValue QUIVER_SLOT_COUNT;
    private static final ModConfigSpec.BooleanValue ALLOW_MULTIPLE_QUIVER_UPGRADES;
    private static final ModConfigSpec.BooleanValue ALLOW_QUIVER_FIREWORK_ROCKETS;
    private static final ModConfigSpec.BooleanValue ALLOW_QUIVER_MODDED_PROJECTILE_TAGS;
    private static final ModConfigSpec.IntValue TRASH_CAN_DELETION_DELAY_SECONDS;
    private static final ModConfigSpec.BooleanValue TRASH_CAN_DELETE_PENDING_ON_CLOSE;
    private static final ModConfigSpec.BooleanValue TRASH_CAN_ENABLE_DELETE_NOW_BUTTON;
    private static final ModConfigSpec.BooleanValue TRASH_CAN_CONFIRM_MODDED_ITEMS;
    private static final ModConfigSpec.BooleanValue TRASH_CAN_CONFIRM_CONTAINER_ITEMS;
    private static final ModConfigSpec.BooleanValue TRASH_CAN_CONFIRM_DAMAGED_ITEMS;
    private static final ModConfigSpec.BooleanValue ALLOW_MULTIPLE_TRASH_CAN_UPGRADES;
    private static final ModConfigSpec.EnumValue<TrashProtectionLevel> TRASH_CAN_PROTECTION_LEVEL;
    private static final ModConfigSpec.BooleanValue RESCUE_UPGRADE_ENABLED;
    private static final ModConfigSpec.BooleanValue RESCUE_ALLOW_BACKPACK_TOTEM;
    private static final ModConfigSpec.BooleanValue RESCUE_ALLOW_GOLDEN_APPLE;
    private static final ModConfigSpec.BooleanValue RESCUE_ALLOW_ENCHANTED_GOLDEN_APPLE;
    private static final ModConfigSpec.BooleanValue RESCUE_ALLOW_FALL;
    private static final ModConfigSpec.BooleanValue RESCUE_ALLOW_LAVA;
    private static final ModConfigSpec.BooleanValue RESCUE_ALLOW_FIRE;
    private static final ModConfigSpec.BooleanValue RESCUE_RESPECT_DIMENSION_WATER_RESTRICTIONS;
    private static final ModConfigSpec.BooleanValue RESCUE_ENABLE_NOTIFICATIONS;
    private static final ModConfigSpec.BooleanValue ALLOW_MULTIPLE_RESCUE_UPGRADES;
    private static final ModConfigSpec.IntValue RESCUE_GLOBAL_COOLDOWN_SECONDS;
    private static final ModConfigSpec.IntValue RESCUE_TOTEM_COOLDOWN_SECONDS;
    private static final ModConfigSpec.IntValue RESCUE_GOLDEN_APPLE_COOLDOWN_SECONDS;
    private static final ModConfigSpec.IntValue RESCUE_FALL_COOLDOWN_SECONDS;
    private static final ModConfigSpec.IntValue RESCUE_LAVA_COOLDOWN_SECONDS;
    private static final ModConfigSpec.IntValue RESCUE_MAXIMUM_FALL_SCAN_DISTANCE;
    private static final ModConfigSpec.BooleanValue BUILDER_UPGRADE_ENABLED;
    private static final ModConfigSpec.BooleanValue BUILDER_ALLOW_KEEP_FULL_MODE;
    private static final ModConfigSpec.BooleanValue BUILDER_ALLOW_OFFHAND_REFILL;
    private static final ModConfigSpec.BooleanValue BUILDER_ALLOW_MODDED_BLOCKS;
    private static final ModConfigSpec.BooleanValue BUILDER_ALLOW_CONTAINER_REFILL;
    private static final ModConfigSpec.BooleanValue BUILDER_ALLOW_DANGEROUS_REFILL;
    private static final ModConfigSpec.BooleanValue BUILDER_ALLOW_MULTIPLE_UPGRADES;
    private static final ModConfigSpec.BooleanValue BUILDER_FEEDBACK_ENABLED;
    private static final ModConfigSpec.IntValue BUILDER_DEFAULT_REFILL_THRESHOLD;
    private static final ModConfigSpec.IntValue BUILDER_ANTI_SPAM_DELAY_TICKS;
    private static final ModConfigSpec.BooleanValue TORCH_PLACER_UPGRADE_ENABLED;
    private static final ModConfigSpec.BooleanValue TORCH_PLACER_ALLOW_MODDED_LIGHTS;
    private static final ModConfigSpec.BooleanValue TORCH_PLACER_ALLOW_MULTIPLE_UPGRADES;
    private static final ModConfigSpec.BooleanValue TORCH_PLACER_ALLOW_CREATIVE;
    private static final ModConfigSpec.BooleanValue TORCH_PLACER_ALLOW_ADVENTURE;
    private static final ModConfigSpec.BooleanValue TORCH_PLACER_FEEDBACK_ENABLED;
    private static final ModConfigSpec.IntValue TORCH_PLACER_CHECK_INTERVAL_TICKS;
    private static final ModConfigSpec.IntValue TORCH_PLACER_PLACEMENT_COOLDOWN_TICKS;
    private static final ModConfigSpec.IntValue TORCH_PLACER_FAILURE_COOLDOWN_TICKS;
    private static final ModConfigSpec.BooleanValue CAPACITY_WARNING_UPGRADE_ENABLED;
    private static final ModConfigSpec.BooleanValue CAPACITY_WARNING_ALLOW_STACK_CAPACITY_MODE;
    private static final ModConfigSpec.BooleanValue CAPACITY_WARNING_ALLOW_MULTIPLE_UPGRADES;
    private static final ModConfigSpec.BooleanValue CAPACITY_WARNING_ALLOW_PERSISTENT_HUD;
    private static final ModConfigSpec.BooleanValue CAPACITY_WARNING_DEBUG_CONSISTENCY_CHECKS;
    private static final ModConfigSpec.IntValue CAPACITY_WARNING_DEFAULT_THRESHOLD_1;
    private static final ModConfigSpec.IntValue CAPACITY_WARNING_DEFAULT_THRESHOLD_2;
    private static final ModConfigSpec.IntValue CAPACITY_WARNING_DEFAULT_THRESHOLD_3;
    private static final ModConfigSpec.IntValue CAPACITY_WARNING_DEFAULT_RESET_MARGIN;
    private static final ModConfigSpec.IntValue CAPACITY_WARNING_NOTIFICATION_COOLDOWN_SECONDS;
    private static final ModConfigSpec.IntValue CAPACITY_WARNING_FAILED_INSERTION_COOLDOWN_TICKS;
    private static final ModConfigSpec.IntValue CAPACITY_WARNING_MAX_BACKPACKS_TRACKED_PER_PLAYER;
    private static final ModConfigSpec.IntValue CAPACITY_WARNING_CHECK_INTERVAL_TICKS;
    private static final ModConfigSpec.IntValue CAPACITY_WARNING_HUD_REFRESH_TICKS;
    private static final ModConfigSpec.BooleanValue ITEM_LOCK_UPGRADE_ENABLED;
    private static final ModConfigSpec.BooleanValue ITEM_LOCK_ALLOW_MULTIPLE_UPGRADES;
    private static final ModConfigSpec.BooleanValue ITEM_LOCK_ALLOW_SLOT_LOCKS;
    private static final ModConfigSpec.BooleanValue ITEM_LOCK_ALLOW_ITEM_LOCKS;
    private static final ModConfigSpec.BooleanValue ITEM_LOCK_ALLOW_TYPE_LOCKS;
    private static final ModConfigSpec.BooleanValue ITEM_LOCK_ALLOW_MANUAL_ACCESS;
    private static final ModConfigSpec.BooleanValue ITEM_LOCK_ALLOW_AUTOMATION_ACCESS;
    private static final ModConfigSpec.BooleanValue BACKPACK_LINK_UPGRADE_ENABLED;
    private static final ModConfigSpec.BooleanValue BACKPACK_LINK_ALLOW_MULTIPLE_UPGRADES;
    private static final ModConfigSpec.BooleanValue BACKPACK_LINK_ALLOW_PUBLIC_ANCHORS;
    private static final ModConfigSpec.BooleanValue BACKPACK_LINK_ALLOW_CROSS_DIMENSION;
    private static final ModConfigSpec.IntValue BACKPACK_LINK_MAX_DESTINATIONS;
    private static final ModConfigSpec.IntValue BACKPACK_LINK_SAFE_ARRIVAL_RADIUS;
    private static final ModConfigSpec.IntValue BACKPACK_LINK_SAME_DIMENSION_BASE_XP_LEVELS;
    private static final ModConfigSpec.IntValue BACKPACK_LINK_DISTANCE_XP_PER_1000_BLOCKS;
    private static final ModConfigSpec.IntValue BACKPACK_LINK_CROSS_DIMENSION_XP_LEVELS;
    private static final ModConfigSpec.IntValue BACKPACK_LINK_MAX_XP_LEVEL_COST;
    private static final ModConfigSpec.IntValue BACKPACK_LINK_COOLDOWN_SECONDS;
    private static final ModConfigSpec.IntValue BACKPACK_LINK_CROSS_DIMENSION_COOLDOWN_SECONDS;
    private static final ModConfigSpec.BooleanValue STORAGE_MONITOR_ENABLED;
    private static final ModConfigSpec.BooleanValue STORAGE_MONITOR_CROSS_DIMENSION_ACCESS;
    private static final ModConfigSpec.BooleanValue STORAGE_NETWORK_ENABLED;
    private static final ModConfigSpec.IntValue STORAGE_NETWORK_MAX_CABLE_PATH;
    private static final ModConfigSpec.IntValue STORAGE_NETWORK_MAX_NODES;
    private static final ModConfigSpec.BooleanValue STORAGE_IMPORTER_ENABLED;
    private static final ModConfigSpec.BooleanValue STORAGE_EXPORTER_ENABLED;
    private static final ModConfigSpec.IntValue STORAGE_IMPORTER_TRANSFER_INTERVAL;
    private static final ModConfigSpec.IntValue STORAGE_IMPORTER_TRANSFER_AMOUNT;
    private static final ModConfigSpec.IntValue STORAGE_EXPORTER_TRANSFER_INTERVAL;
    private static final ModConfigSpec.IntValue STORAGE_EXPORTER_TRANSFER_AMOUNT;
    private static final ModConfigSpec.BooleanValue STORAGE_TRANSFER_REDSTONE_CONTROL_ENABLED;
    private static final ModConfigSpec.BooleanValue ABANDONED_BACKPACKS_ENABLED;
    private static final ModConfigSpec.BooleanValue BACKPACKER_CAMP_ENABLED;
    private static final ModConfigSpec.IntValue BACKPACKER_CAMP_ABANDONED_WEIGHT;
    private static final ModConfigSpec.IntValue BACKPACKER_CAMP_BACKPACK_WEIGHT;
    private static final ModConfigSpec.IntValue BACKPACKER_CAMP_TRADER_WEIGHT;
    private static final ModConfigSpec.DoubleValue BACKPACKER_CAMP_BACKPACK_LOOT_CHANCE;
    private static final ModConfigSpec.DoubleValue ABANDONED_BACKPACK_UPGRADE_CHANCE;
    private static final ModConfigSpec.DoubleValue ABANDONED_BACKPACK_CUSTOM_NAME_CHANCE;
    private static final Map<AbandonedBackpackProfile, ModConfigSpec.DoubleValue> ABANDONED_BACKPACK_CHANCES = new EnumMap<>(AbandonedBackpackProfile.class);
    private static final ModConfigSpec.BooleanValue MOB_BACKPACKS_ENABLED;
    private static final ModConfigSpec.DoubleValue MOB_BACKPACK_SPAWN_CHANCE;
    private static final ModConfigSpec.DoubleValue MOB_BACKPACK_DROP_CHANCE;
    private static final ModConfigSpec.BooleanValue MOB_BACKPACK_NATURAL_SPAWNS_ONLY;
    private static final ModConfigSpec.IntValue MOB_BACKPACK_MIN_LOOT_ENTRIES;
    private static final ModConfigSpec.IntValue MOB_BACKPACK_MAX_LOOT_ENTRIES;
    private static final Map<MobBackpackType, ModConfigSpec.BooleanValue> MOB_BACKPACK_MOB_ENABLED = new EnumMap<>(MobBackpackType.class);
    private static final Map<MobBackpackType, ModConfigSpec.DoubleValue> MOB_BACKPACK_MOB_CHANCE = new EnumMap<>(MobBackpackType.class);
    private static final Map<MobBackpackType, ModConfigSpec.ConfigValue<String>> MOB_BACKPACK_MOB_COLOR = new EnumMap<>(MobBackpackType.class);
    private static final Map<BackpackTier, ModConfigSpec.IntValue> MOB_BACKPACK_TIER_WEIGHT = new EnumMap<>(BackpackTier.class);
    private static final TierConfig LEATHER_TIER;
    private static final TierConfig COAL_TIER;
    private static final TierConfig LAPIS_TIER;
    private static final TierConfig REDSTONE_TIER;
    private static final TierConfig QUARTZ_TIER;
    private static final TierConfig COPPER_TIER;
    private static final TierConfig IRON_TIER;
    private static final TierConfig GOLD_TIER;
    private static final TierConfig EMERALD_TIER;
    private static final TierConfig DIAMOND_TIER;
    private static final TierConfig NETHERITE_TIER;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("wearing");
        CAN_WEAR_BACKPACK_ON_BACK = builder
                .comment("If true, backpacks can be worn on the player's back.")
                .define("canWearBackpackOnBack", true);
        ALLOW_BACKPACK_IN_CURIOS_SLOT = builder
                .comment("If true, backpacks are allowed to use the Curios back slot.")
                .define("allowBackpackInCuriosSlot", true);
        ALLOW_BACKPACK_IN_BACKPACK = builder
                .comment("If true, backpacks can be stored inside other backpacks.")
                .define("allowBackpackInBackpack", false);
        ALLOW_OTHER_PLAYERS_TO_OPEN_WORN_BACKPACKS = builder
                .comment("If true, other players can open a backpack while it is worn on someone's back.")
                .define("allowOtherPlayersToOpenWornBackpacks", false);
        SHOW_WELCOME_MESSAGE = builder
                .comment("If true, players see the Smart Backpacks welcome message when they join.")
                .define("showWelcomeMessage", false);
        builder.pop();

        builder.push("pickup_notifier");
        PICKUP_NOTIFIER_ENABLED = builder
                .comment("If true, Smart Backpacks sends compact HUD notifications after server-confirmed backpack insertions.")
                .define("enabled", true);
        PICKUP_NOTIFIER_SHOW_WORLD_PICKUPS = builder
                .comment("If true, world item pickups collected by Pickup or Magnet systems produce notifications.")
                .define("showWorldPickups", true);
        PICKUP_NOTIFIER_SHOW_MANUAL_TRANSFERS = builder
                .comment("If true, manual player transfers into backpack storage produce notifications. Disabled by default to avoid organization spam.")
                .define("showManualTransfers", false);
        PICKUP_NOTIFIER_SHOW_UPGRADE_ROUTING = builder
                .comment("If true, Smart Backpacks upgrade routing that stores items produces notifications.")
                .define("showUpgradeRouting", true);
        PICKUP_NOTIFIER_SHOW_AUTOMATION = builder
                .comment("If true, automation insertion into placed backpacks may produce notifications when a player context is available.")
                .define("showAutomation", false);
        PICKUP_NOTIFIER_SHOW_PROCESSING_RESULTS = builder
                .comment("If true, processing outputs such as compression or smelting may produce notifications.")
                .define("showProcessingResults", true);
        PICKUP_NOTIFIER_SHOW_CONTAINER_TRANSFERS = builder
                .comment("If true, container-to-backpack transfers such as Restock produce notifications.")
                .define("showContainerTransfers", false);
        PICKUP_NOTIFIER_SHOW_BACKPACK_NAME = builder
                .comment("If true, notifications include the receiving backpack name.")
                .define("showBackpackName", true);
        PICKUP_NOTIFIER_SHOW_DESTINATION = builder
                .comment("If true, notifications include the destination section such as Main Storage or Quiver Storage.")
                .define("showDestination", true);
        PICKUP_NOTIFIER_SHOW_TOTAL_STORED_AMOUNT = builder
                .comment("If true, notifications include the total matching count in the current backpack when it can be calculated safely.")
                .define("showTotalStoredAmount", true);
        PICKUP_NOTIFIER_DISPLAY_TICKS = builder
                .comment("Ticks a pickup notification stays visible before fading out.")
                .defineInRange("displayTicks", 60, 20, 300);
        PICKUP_NOTIFIER_GROUPING_WINDOW_TICKS = builder
                .comment("Ticks during which compatible pickups merge into one live notification.")
                .defineInRange("groupingWindowTicks", 40, 0, 200);
        PICKUP_NOTIFIER_MAX_VISIBLE = builder
                .comment("Maximum pickup notifications visible on the HUD at once.")
                .defineInRange("maxVisible", 5, 1, 20);
        PICKUP_NOTIFIER_QUEUE_LIMIT = builder
                .comment("Maximum in-memory client notification entries kept before old visuals are dropped.")
                .defineInRange("queueLimit", 50, 5, 200);
        builder.pop();

        builder.push("upgrades");
        FLIGHT_UPGRADE_ENABLED = builder
                .comment("Allows the Flight Upgrade to grant flight while an equipped backpack is active.")
                .define("flight_upgrade_enabled", true);
        REPAIR_UPGRADE_ENABLED = builder.comment("Allows the Repair Upgrade to consume player XP and repair backpack contents.")
                .define("repair_upgrade_enabled", true);
        FALL_PROTECTION_UPGRADE_ENABLED = builder.comment("Allows the Fall Protection Upgrade to prevent dangerous falls.")
                .define("fall_protection_upgrade_enabled", true);
        DEATH_EMERGENCY_KIT_ENABLED = builder.comment("Allows the Death Emergency Kit Upgrade to retrieve stored supplies after respawn.")
                .define("death_emergency_kit_enabled", true);
        FALL_PROTECTION_COOLDOWN_SECONDS = builder.comment("Seconds between Fall Protection activations.")
                .defineInRange("fall_protection_cooldown_seconds", 60, 0, 3600);
        FALL_PROTECTION_SLOW_FALLING_SECONDS = builder.comment("Slow Falling duration per safety activation.")
                .defineInRange("fall_protection_slow_falling_seconds", 5, 1, 60);
        FALL_PROTECTION_MAX_CHARGES = builder.comment("Usable charges before this 32-durability upgrade is depleted.")
                .defineInRange("fall_protection_max_charges", 32, 1, 32);
        FALL_PROTECTION_TRIGGER_HEALTH_MARGIN = builder.comment("Activate if predicted fall damage would leave this much health or less.")
                .defineInRange("fall_protection_trigger_health_margin", 4, 0, 20);
        REPAIR_UPGRADE_TICKS_PER_REPAIR = builder.comment("Ticks between repair cycles.")
                .defineInRange("repair_upgrade_ticks_per_repair", 10, 1, 1200);
        REPAIR_UPGRADE_DURABILITY_PER_CYCLE = builder.comment("Durability contributed by each repair cycle.")
                .defineInRange("repair_upgrade_durability_per_cycle", 1, 1, 64);
        REPAIR_UPGRADE_DURABILITY_PER_XP = builder.comment("Durability restored per raw XP point.")
                .defineInRange("repair_upgrade_durability_per_xp", 2, 1, 64);
        BLOCK_DROP_MAGNET_PROTECTION_ENABLED = builder
                .comment("Prevents other players' magnets from collecting freshly mined block drops.")
                .define("blockDropMagnetProtectionEnabled", true);
        BLOCK_DROP_MAGNET_PROTECTION_SECONDS = builder
                .comment("Seconds of magnet protection for mined block drops; 0 protects until the item disappears.")
                .defineInRange("blockDropMagnetProtectionSeconds", 30, 0, 86400);
        MAGNET_RADIUS = builder
                .comment("Pickup radius for the regular Magnet Upgrade.")
                .defineInRange("magnetRadius", 10, 1, 128);
        ADVANCED_MAGNET_RADIUS = builder
                .comment("Pickup radius for the Advanced Magnet Upgrade.")
                .defineInRange("advancedMagnetRadius", 35, 1, 256);
        QUIVER_SLOT_COUNT = builder
                .comment("Projectile slots per Quiver Upgrade. The current GUI-safe default is 9.")
                .defineInRange("quiverSlotCount", 9, 1, 54);
        ALLOW_MULTIPLE_QUIVER_UPGRADES = builder
                .comment("If true, multiple Quiver Upgrades can be installed in one backpack. Each upgrade keeps its own safe projectile storage.")
                .define("allowMultipleQuiverUpgrades", true);
        ALLOW_QUIVER_FIREWORK_ROCKETS = builder
                .comment("If true, Quiver Upgrades may store firework rockets for crossbows.")
                .define("allowQuiverFireworkRockets", true);
        ALLOW_QUIVER_MODDED_PROJECTILE_TAGS = builder
                .comment("If true, items in smartbackpacks quiver projectile tags can be stored and offered to compatible projectile weapons.")
                .define("allowQuiverModdedProjectileTags", true);
        TRASH_CAN_DELETION_DELAY_SECONDS = builder
                .comment("Seconds before a normal pending Trash Can item is deleted while the backpack GUI stays open.")
                .defineInRange("trashCanDeletionDelaySeconds", 5, 0, 30);
        TRASH_CAN_DELETE_PENDING_ON_CLOSE = builder
                .comment("If true, normal pending Trash Can contents are deleted when the backpack GUI closes. Protected items are returned or kept pending instead of deleted silently.")
                .define("trashCanDeletePendingItemOnClose", true);
        TRASH_CAN_ENABLE_DELETE_NOW_BUTTON = builder
                .comment("If true, the Trash Can panel shows a Delete Now button.")
                .define("trashCanEnableDeleteNowButton", true);
        ALLOW_MULTIPLE_TRASH_CAN_UPGRADES = builder
                .comment("If true, multiple Trash Can Upgrades can be installed. Disabled by default because each backpack only exposes one safe trash slot.")
                .define("allowMultipleTrashCanUpgrades", false);
        TRASH_CAN_PROTECTION_LEVEL = builder
                .comment("Protection level for valuable Trash Can items.")
                .defineEnum("trashCanProtectionLevel", TrashProtectionLevel.VALUABLE_ITEMS);
        TRASH_CAN_CONFIRM_MODDED_ITEMS = builder
                .comment("If true, modded items require confirmation before manual Trash Can deletion.")
                .define("trashCanConfirmModdedItems", true);
        TRASH_CAN_CONFIRM_CONTAINER_ITEMS = builder
                .comment("If true, container-like items with stored contents require confirmation before manual Trash Can deletion.")
                .define("trashCanConfirmContainerItems", true);
        TRASH_CAN_CONFIRM_DAMAGED_ITEMS = builder
                .comment("If true, damaged durability items require confirmation before manual Trash Can deletion.")
                .define("trashCanConfirmDamagedItems", true);
        RESCUE_UPGRADE_ENABLED = builder
                .comment("If true, Rescue Upgrades may activate server-side emergency actions.")
                .define("rescueUpgradeEnabled", true);
        RESCUE_ALLOW_BACKPACK_TOTEM = builder
                .comment("If true, a Totem stored in a Rescue Upgrade may prevent death. Servers can disable this if they want vanilla hand-held Totem rules only.")
                .define("rescueAllowBackpackTotem", true);
        RESCUE_ALLOW_GOLDEN_APPLE = builder
                .comment("If true, Rescue Upgrades may automatically consume stored Golden Apples at low health.")
                .define("rescueAllowGoldenApple", true);
        RESCUE_ALLOW_ENCHANTED_GOLDEN_APPLE = builder
                .comment("If true, Rescue Upgrades may consume Enchanted Golden Apples. Disabled by default because they are very valuable.")
                .define("rescueAllowEnchantedGoldenApple", false);
        RESCUE_ALLOW_FALL = builder
                .comment("If true, Rescue Upgrades may place stored water during dangerous falls.")
                .define("rescueAllowFall", true);
        RESCUE_ALLOW_LAVA = builder
                .comment("If true, Rescue Upgrades may place stored water when the player is in lava and low health.")
                .define("rescueAllowLava", true);
        RESCUE_ALLOW_FIRE = builder
                .comment("If true, Rescue Upgrades may place stored water/extinguish fire when burning at low health.")
                .define("rescueAllowFire", true);
        RESCUE_RESPECT_DIMENSION_WATER_RESTRICTIONS = builder
                .comment("If true, water rescue respects dimensions where water cannot exist, such as the Nether.")
                .define("rescueRespectDimensionWaterRestrictions", true);
        RESCUE_ENABLE_NOTIFICATIONS = builder
                .comment("If true, successful Rescue Upgrade actions show action-bar feedback.")
                .define("rescueEnableNotifications", true);
        ALLOW_MULTIPLE_RESCUE_UPGRADES = builder
                .comment("If true, multiple Rescue Upgrades can be installed in one backpack. Disabled by default to avoid duplicate emergency triggers.")
                .define("allowMultipleRescueUpgrades", false);
        RESCUE_GLOBAL_COOLDOWN_SECONDS = builder
                .comment("Short global anti-spam cooldown after any Rescue Upgrade action.")
                .defineInRange("rescueGlobalCooldownSeconds", 5, 0, 300);
        RESCUE_TOTEM_COOLDOWN_SECONDS = builder
                .comment("Cooldown after a backpack Totem rescue.")
                .defineInRange("rescueTotemCooldownSeconds", 60, 0, 900);
        RESCUE_GOLDEN_APPLE_COOLDOWN_SECONDS = builder
                .comment("Cooldown after a Golden Apple rescue.")
                .defineInRange("rescueGoldenAppleCooldownSeconds", 30, 0, 900);
        RESCUE_FALL_COOLDOWN_SECONDS = builder
                .comment("Cooldown after a fall water rescue.")
                .defineInRange("rescueFallCooldownSeconds", 10, 0, 900);
        RESCUE_LAVA_COOLDOWN_SECONDS = builder
                .comment("Cooldown after a lava/fire water rescue.")
                .defineInRange("rescueLavaCooldownSeconds", 20, 0, 900);
        RESCUE_MAXIMUM_FALL_SCAN_DISTANCE = builder
                .comment("Maximum downward blocks scanned for fall water rescue.")
                .defineInRange("rescueMaximumFallScanDistance", 32, 4, 128);
        BUILDER_UPGRADE_ENABLED = builder
                .comment("If true, Builder Upgrades may refill matching building blocks from normal backpack storage.")
                .define("builderUpgradeEnabled", true);
        BUILDER_ALLOW_KEEP_FULL_MODE = builder
                .comment("If true, Builder Upgrades may use Keep Full mode.")
                .define("builderAllowKeepFullMode", true);
        BUILDER_ALLOW_OFFHAND_REFILL = builder
                .comment("If true, Builder Upgrades may refill the offhand when the upgrade setting is enabled.")
                .define("builderAllowOffhandRefill", false);
        BUILDER_ALLOW_MODDED_BLOCKS = builder
                .comment("If true, Builder Upgrades may refill compatible modded block items.")
                .define("builderAllowModdedBlocks", true);
        BUILDER_ALLOW_CONTAINER_REFILL = builder
                .comment("If true, Builder Upgrades may refill container-like block items. Disabled by default for item safety.")
                .define("builderAllowContainerRefill", false);
        BUILDER_ALLOW_DANGEROUS_REFILL = builder
                .comment("If true, Builder Upgrades may refill items tagged as dangerous. Disabled by default.")
                .define("builderAllowDangerousRefill", false);
        BUILDER_ALLOW_MULTIPLE_UPGRADES = builder
                .comment("If true, multiple Builder Upgrades can be installed in one backpack.")
                .define("builderAllowMultipleUpgrades", false);
        BUILDER_FEEDBACK_ENABLED = builder
                .comment("If true, successful Builder Upgrade refills show action-bar feedback.")
                .define("builderFeedbackEnabled", true);
        BUILDER_DEFAULT_REFILL_THRESHOLD = builder
                .comment("Default low-stack threshold for new Builder Upgrades.")
                .defineInRange("builderDefaultRefillThreshold", 8, 1, 64);
        BUILDER_ANTI_SPAM_DELAY_TICKS = builder
                .comment("Minimum ticks between Builder Upgrade refill attempts per hand.")
                .defineInRange("builderAntiSpamDelayTicks", 2, 1, 20);
        TORCH_PLACER_UPGRADE_ENABLED = builder
                .comment("If true, Torch Placer Upgrades may automatically place light sources from normal backpack storage.")
                .define("torchPlacerUpgradeEnabled", true);
        TORCH_PLACER_ALLOW_MODDED_LIGHTS = builder
                .comment("If true, Torch Placer Upgrades may use modded light-source items that are tagged as smartbackpacks:torch_placer_items.")
                .define("torchPlacerAllowModdedLights", true);
        TORCH_PLACER_ALLOW_MULTIPLE_UPGRADES = builder
                .comment("If true, multiple Torch Placer Upgrades can be installed in one backpack. Disabled by default to avoid duplicate placement attempts.")
                .define("torchPlacerAllowMultipleUpgrades", false);
        TORCH_PLACER_ALLOW_CREATIVE = builder
                .comment("If true, Torch Placer Upgrades may run for creative players. Useful for testing; placed torches still consume backpack storage.")
                .define("torchPlacerAllowCreative", true);
        TORCH_PLACER_ALLOW_ADVENTURE = builder
                .comment("If true, Torch Placer Upgrades may run for adventure players. Disabled by default.")
                .define("torchPlacerAllowAdventure", false);
        TORCH_PLACER_FEEDBACK_ENABLED = builder
                .comment("If true, Torch Placer Upgrade action-bar feedback may be shown when the upgrade setting is enabled.")
                .define("torchPlacerFeedbackEnabled", false);
        TORCH_PLACER_CHECK_INTERVAL_TICKS = builder
                .comment("Ticks between automatic Torch Placer checks per player.")
                .defineInRange("torchPlacerCheckIntervalTicks", 10, 1, 200);
        TORCH_PLACER_PLACEMENT_COOLDOWN_TICKS = builder
                .comment("Minimum ticks between successful Torch Placer placements per player.")
                .defineInRange("torchPlacerPlacementCooldownTicks", 20, 1, 600);
        TORCH_PLACER_FAILURE_COOLDOWN_TICKS = builder
                .comment("Minimum ticks between failed Torch Placer placement attempts per player.")
                .defineInRange("torchPlacerFailureCooldownTicks", 20, 1, 600);
        CAPACITY_WARNING_UPGRADE_ENABLED = builder
                .comment("If true, Capacity Warning Upgrades may calculate backpack fullness and notify players.")
                .define("capacityWarningUpgradeEnabled", true);
        CAPACITY_WARNING_ALLOW_STACK_CAPACITY_MODE = builder
                .comment("If true, players may use Stack Capacity mode. If false, settings are forced to Occupied Slots mode.")
                .define("capacityWarningAllowStackCapacityMode", true);
        CAPACITY_WARNING_ALLOW_MULTIPLE_UPGRADES = builder
                .comment("If true, multiple Capacity Warning Upgrades can be installed in one backpack. Disabled by default to avoid duplicate HUD/messages.")
                .define("capacityWarningAllowMultipleUpgrades", false);
        CAPACITY_WARNING_ALLOW_PERSISTENT_HUD = builder
                .comment("If true, the Always HUD display mode may stay visible. If false, Always behaves like threshold display.")
                .define("capacityWarningAllowPersistentHud", true);
        CAPACITY_WARNING_DEBUG_CONSISTENCY_CHECKS = builder
                .comment("If true, enables extra debug consistency checks for capacity calculations. Kept off by default for performance.")
                .define("capacityWarningDebugConsistencyChecks", false);
        CAPACITY_WARNING_DEFAULT_THRESHOLD_1 = builder
                .comment("Default Capacity Warning threshold 1 percentage.")
                .defineInRange("capacityWarningDefaultThreshold1", 75, 1, 100);
        CAPACITY_WARNING_DEFAULT_THRESHOLD_2 = builder
                .comment("Default Capacity Warning threshold 2 percentage.")
                .defineInRange("capacityWarningDefaultThreshold2", 90, 1, 100);
        CAPACITY_WARNING_DEFAULT_THRESHOLD_3 = builder
                .comment("Default Capacity Warning threshold 3 percentage.")
                .defineInRange("capacityWarningDefaultThreshold3", 100, 1, 100);
        CAPACITY_WARNING_DEFAULT_RESET_MARGIN = builder
                .comment("Default percentage margin a backpack must drop below a threshold before that warning can trigger again.")
                .defineInRange("capacityWarningDefaultResetMargin", 3, 0, 10);
        CAPACITY_WARNING_NOTIFICATION_COOLDOWN_SECONDS = builder
                .comment("Seconds between repeated Capacity Warning notifications of the same threshold for one backpack.")
                .defineInRange("capacityWarningNotificationCooldownSeconds", 5, 0, 300);
        CAPACITY_WARNING_FAILED_INSERTION_COOLDOWN_TICKS = builder
                .comment("Ticks between manual failed-insertion warnings.")
                .defineInRange("capacityWarningFailedInsertionCooldownTicks", 60, 1, 1200);
        CAPACITY_WARNING_MAX_BACKPACKS_TRACKED_PER_PLAYER = builder
                .comment("Maximum carried backpacks with Capacity Warning Upgrades tracked per player.")
                .defineInRange("capacityWarningMaxBackpacksTrackedPerPlayer", 16, 1, 64);
        CAPACITY_WARNING_CHECK_INTERVAL_TICKS = builder
                .comment("Ticks between read-only capacity checks. Avoids scanning large backpacks every tick.")
                .defineInRange("capacityWarningCheckIntervalTicks", 20, 1, 600);
        CAPACITY_WARNING_HUD_REFRESH_TICKS = builder
                .comment("Ticks between HUD refresh packets while persistent Capacity Warning HUD is visible.")
                .defineInRange("capacityWarningHudRefreshTicks", 100, 20, 1200);
        ITEM_LOCK_UPGRADE_ENABLED = builder
                .comment("If true, Item Lock Upgrades may protect backpack storage slots and stored stacks.")
                .define("itemLockUpgradeEnabled", true);
        ITEM_LOCK_ALLOW_MULTIPLE_UPGRADES = builder
                .comment("If true, multiple Item Lock Upgrades can be installed in one backpack. Disabled by default to keep lock metadata unambiguous.")
                .define("itemLockAllowMultipleUpgrades", false);
        ITEM_LOCK_ALLOW_SLOT_LOCKS = builder
                .comment("If true, players may lock stable logical backpack storage slots.")
                .define("itemLockAllowSlotLocks", true);
        ITEM_LOCK_ALLOW_ITEM_LOCKS = builder
                .comment("If true, players may lock exact item variants using their item ID and data components.")
                .define("itemLockAllowItemLocks", true);
        ITEM_LOCK_ALLOW_TYPE_LOCKS = builder
                .comment("If true, players may lock all stacks of one registered item type. Disabled by default because it is broad.")
                .define("itemLockAllowTypeLocks", false);
        ITEM_LOCK_ALLOW_MANUAL_ACCESS = builder
                .comment("If true, locked content may still be moved manually. Disabled by default to prevent accidental split, merge, or delete operations.")
                .define("itemLockAllowManualAccess", false);
        ITEM_LOCK_ALLOW_AUTOMATION_ACCESS = builder
                .comment("If true, automation may access locked content when the backpack lock data also allows it. Disabled by default.")
                .define("itemLockAllowAutomationAccess", false);
        BACKPACK_LINK_UPGRADE_ENABLED = builder
                .comment("If true, Backpack Link Upgrades may turn placed backpacks into teleport anchors.")
                .define("backpackLinkUpgradeEnabled", true);
        BACKPACK_LINK_ALLOW_MULTIPLE_UPGRADES = builder
                .comment("If true, multiple Backpack Link Upgrades can be installed in one backpack. Disabled by default because one anchor identity is stored per backpack.")
                .define("backpackLinkAllowMultipleUpgrades", false);
        BACKPACK_LINK_ALLOW_PUBLIC_ANCHORS = builder
                .comment("If true, anchor owners may set Backpack Link destinations to Public.")
                .define("backpackLinkAllowPublicAnchors", true);
        BACKPACK_LINK_ALLOW_CROSS_DIMENSION = builder
                .comment("If true, Backpack Link teleport may cross dimensions when both anchors are valid.")
                .define("backpackLinkAllowCrossDimension", true);
        BACKPACK_LINK_MAX_DESTINATIONS = builder
                .comment("Maximum linked destinations per Backpack Link anchor.")
                .defineInRange("backpackLinkMaxDestinations", 4, 1, 32);
        BACKPACK_LINK_SAFE_ARRIVAL_RADIUS = builder
                .comment("Maximum nearby block radius searched for a safe Backpack Link arrival position.")
                .defineInRange("backpackLinkSafeArrivalRadius", 4, 1, 8);
        BACKPACK_LINK_SAME_DIMENSION_BASE_XP_LEVELS = builder
                .comment("Base XP level cost for same-dimension Backpack Link teleport.")
                .defineInRange("backpackLinkSameDimensionBaseXpLevels", 1, 0, 100);
        BACKPACK_LINK_DISTANCE_XP_PER_1000_BLOCKS = builder
                .comment("Additional XP levels charged per 1000 same-dimension blocks.")
                .defineInRange("backpackLinkDistanceXpPer1000Blocks", 1, 0, 100);
        BACKPACK_LINK_CROSS_DIMENSION_XP_LEVELS = builder
                .comment("XP level cost for cross-dimension Backpack Link teleport.")
                .defineInRange("backpackLinkCrossDimensionXpLevels", 8, 0, 100);
        BACKPACK_LINK_MAX_XP_LEVEL_COST = builder
                .comment("Maximum XP level cost for one Backpack Link teleport.")
                .defineInRange("backpackLinkMaxXpLevelCost", 30, 0, 1000);
        BACKPACK_LINK_COOLDOWN_SECONDS = builder
                .comment("Legacy setting. Backpack Link teleport cooldown is disabled by default.")
                .defineInRange("backpackLinkCooldownSeconds", 0, 0, 3600);
        BACKPACK_LINK_CROSS_DIMENSION_COOLDOWN_SECONDS = builder
                .comment("Legacy setting. Backpack Link cross-dimension cooldown is disabled by default.")
                .defineInRange("backpackLinkCrossDimensionCooldownSeconds", 0, 0, 3600);
        builder.pop();

        builder.push("storage_network");
        STORAGE_NETWORK_ENABLED = builder
                .comment("Enables Storage Controller and Storage Cable network access.")
                .define("storageNetworkEnabled", true);
        STORAGE_NETWORK_MAX_CABLE_PATH = builder
                .comment("Maximum number of Storage Cable steps between a Controller and a connected Backpack.")
                .defineInRange("maximumCablePath", 30, 1, 128);
        STORAGE_NETWORK_MAX_NODES = builder
                .comment("Maximum number of Storage Cable nodes visited by one network scan.")
                .defineInRange("maximumNetworkNodes", 4096, 128, 32768);
        builder.push("import_export");
        STORAGE_IMPORTER_ENABLED = builder.comment("Enables Storage Importer transfers from placed backpacks into a storage network.")
                .define("storageImporterEnabled", true);
        STORAGE_EXPORTER_ENABLED = builder.comment("Enables Storage Exporter transfers from a storage network into placed backpacks.")
                .define("storageExporterEnabled", true);
        STORAGE_IMPORTER_TRANSFER_INTERVAL = builder.comment("Ticks between Storage Importer operations.")
                .defineInRange("storageImporterTransferInterval", 5, 1, 1200);
        STORAGE_IMPORTER_TRANSFER_AMOUNT = builder.comment("Maximum source stacks moved by one Storage Importer operation; each stack can move in full.")
                .defineInRange("storageImporterTransferAmount", 8, 1, 64);
        STORAGE_EXPORTER_TRANSFER_INTERVAL = builder.comment("Ticks between Storage Exporter operations.")
                .defineInRange("storageExporterTransferInterval", 5, 1, 1200);
        STORAGE_EXPORTER_TRANSFER_AMOUNT = builder.comment("Maximum items moved by one Storage Exporter operation.")
                .defineInRange("storageExporterTransferAmount", 8, 1, 64);
        STORAGE_TRANSFER_REDSTONE_CONTROL_ENABLED = builder.comment("Allows Importer and Exporter redstone control modes.")
                .define("allowRedstoneControl", true);
        builder.pop();
        STORAGE_MONITOR_ENABLED = builder.comment("Enables Storage Monitors and remote Storage Controller access.")
                .define("storageMonitorEnabled", true);
        STORAGE_MONITOR_CROSS_DIMENSION_ACCESS = builder
                .comment("Allows cross-dimension Monitor access when the Controller chunk is already loaded.")
                .define("storageMonitorCrossDimensionAccess", true);
        builder.pop();

        builder.push("mob_backpacks");
        MOB_BACKPACKS_ENABLED = builder.comment("Enables rare Smart Backpacks on supported hostile mobs. Default: true.")
                .define("mobBackpacksEnabled", true);
        MOB_BACKPACK_SPAWN_CHANCE = builder.comment("Chance that an eligible mob receives a backpack. 0.01 = 1%. Range: 0.0 - 1.0.")
                .defineInRange("mobBackpackSpawnChance", 0.01D, 0.0D, 1.0D);
        MOB_BACKPACK_DROP_CHANCE = builder.comment("Chance that a genuine mob backpack drops on death. Range: 0.0 - 1.0.")
                .defineInRange("mobBackpackDropChance", 1.0D, 0.0D, 1.0D);
        MOB_BACKPACK_NATURAL_SPAWNS_ONLY = builder.comment("When true, only natural, chunk-generation and patrol spawns may receive backpacks.")
                .define("mobBackpackNaturalSpawnsOnly", true);
        MOB_BACKPACK_MIN_LOOT_ENTRIES = builder.comment("Minimum thematic loot entries generated in a mob backpack. Range: 0 - 5.")
                .defineInRange("mobBackpackMinLootEntries", 2, 0, 5);
        MOB_BACKPACK_MAX_LOOT_ENTRIES = builder.comment("Maximum thematic loot entries generated in a mob backpack. Range: 0 - 5.")
                .defineInRange("mobBackpackMaxLootEntries", 5, 0, 5);
        for (MobBackpackType type : MobBackpackType.values()) {
            builder.push(type.id());
            MOB_BACKPACK_MOB_ENABLED.put(type, builder.comment("Enables Mob Backpacks for " + type.id() + ". Default: true.")
                    .define("enabled", true));
            MOB_BACKPACK_MOB_CHANCE.put(type, builder.comment("Spawn chance override for this mob. -1 uses the global chance; otherwise 0.0 - 1.0.")
                    .defineInRange("chance", -1.0D, -1.0D, 1.0D));
            MOB_BACKPACK_MOB_COLOR.put(type, builder.comment("Minecraft DyeColor name used for this mob's backpack.")
                    .define("color", type.defaultColor().getName()));
            builder.pop();
        }
        builder.push("tier_weights");
        defineMobBackpackTierWeight(builder, BackpackTier.LEATHER, 750);
        defineMobBackpackTierWeight(builder, BackpackTier.COAL, 100);
        defineMobBackpackTierWeight(builder, BackpackTier.COPPER, 80);
        defineMobBackpackTierWeight(builder, BackpackTier.IRON, 50);
        defineMobBackpackTierWeight(builder, BackpackTier.GOLD, 15);
        defineMobBackpackTierWeight(builder, BackpackTier.EMERALD, 5);
        builder.pop();
        builder.pop();

        builder.push("backpacker_camp");
        BACKPACKER_CAMP_ENABLED = builder.define("enabled", true);
        BACKPACKER_CAMP_ABANDONED_WEIGHT = builder.defineInRange("abandonedWeight", 65, 0, 1000);
        BACKPACKER_CAMP_BACKPACK_WEIGHT = builder.defineInRange("backpackWeight", 25, 0, 1000);
        BACKPACKER_CAMP_TRADER_WEIGHT = builder.defineInRange("traderWeight", 10, 0, 1000);
        BACKPACKER_CAMP_BACKPACK_LOOT_CHANCE = builder.defineInRange("backpackLootChance", 0.08D, 0.0D, 1.0D);
        builder.pop();

        builder.push("abandoned_backpacks");
        ABANDONED_BACKPACKS_ENABLED = builder.comment("Allow rare prefilled backpacks in selected vanilla structure chests.")
                .define("enabled", true);
        ABANDONED_BACKPACK_UPGRADE_CHANCE = builder.comment("Chance for one safe preinstalled upgrade.")
                .defineInRange("upgradeChance", 0.08D, 0.0D, 1.0D);
        ABANDONED_BACKPACK_CUSTOM_NAME_CHANCE = builder.comment("Chance for a cosmetic explorer name.")
                .defineInRange("customNameChance", 0.15D, 0.0D, 1.0D);
        for (AbandonedBackpackProfile profile : AbandonedBackpackProfile.values()) {
            if (profile == AbandonedBackpackProfile.CAMP) continue;
            ABANDONED_BACKPACK_CHANCES.put(profile, builder.comment("Chance per " + profile.id() + " structure chest.")
                    .defineInRange(profile.id() + "Chance", profile.defaultChance(), 0.0D, 1.0D));
        }
        builder.pop();

        builder.push("backpacks");
        LEATHER_TIER = new TierConfig(builder, "leather", 9, true, true, false);
        COAL_TIER = new TierConfig(builder, "coal", 18, true, true, false);
        LAPIS_TIER = new TierConfig(builder, "lapis", 27, true, true, false);
        REDSTONE_TIER = new TierConfig(builder, "redstone", 36, true, true, false);
        QUARTZ_TIER = new TierConfig(builder, "quartz", 45, true, true, false);
        COPPER_TIER = new TierConfig(builder, "copper", 54, true, true, false);
        IRON_TIER = new TierConfig(builder, "iron", 63, true, true, false);
        GOLD_TIER = new TierConfig(builder, "gold", 72, true, true, false);
        EMERALD_TIER = new TierConfig(builder, "emerald", 81, true, true, false);
        DIAMOND_TIER = new TierConfig(builder, "diamond", 90, true, true, false);
        NETHERITE_TIER = new TierConfig(builder, "netherite", 162, true, true, true);
        builder.pop();

        SPEC = builder.build();
    }

    private SmartBackpacksConfig() {
    }

    public enum BackpackTierId {
        LEATHER("Leather"),
        COAL("Coal"),
        LAPIS("Lapis"),
        REDSTONE("Redstone"),
        QUARTZ("Quartz"),
        COPPER("Copper"),
        IRON("Iron"),
        GOLD("Gold"),
        EMERALD("Emerald"),
        DIAMOND("Diamond"),
        NETHERITE("Netherite");

        private final String displayName;

        BackpackTierId(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return this.displayName;
        }
    }

    public static boolean flightUpgradeEnabled() {
        return FLIGHT_UPGRADE_ENABLED.get();
    }
    public static boolean repairUpgradeEnabled() {
        return REPAIR_UPGRADE_ENABLED.get();
    }
    public static boolean fallProtectionUpgradeEnabled() {
        return FALL_PROTECTION_UPGRADE_ENABLED.get();
    }
    public static int fallProtectionCooldownSeconds() {
        return FALL_PROTECTION_COOLDOWN_SECONDS.get();
    }

    public static boolean deathEmergencyKitEnabled() {
        return DEATH_EMERGENCY_KIT_ENABLED.get();
    }
    public static int fallProtectionSlowFallingSeconds() {
        return FALL_PROTECTION_SLOW_FALLING_SECONDS.get();
    }
    public static int fallProtectionMaxCharges() {
        return FALL_PROTECTION_MAX_CHARGES.get();
    }
    public static int fallProtectionTriggerHealthMargin() {
        return FALL_PROTECTION_TRIGGER_HEALTH_MARGIN.get();
    }
    public static int repairUpgradeTicksPerRepair() {
        return REPAIR_UPGRADE_TICKS_PER_REPAIR.get();
    }
    public static int repairUpgradeDurabilityPerCycle() {
        return REPAIR_UPGRADE_DURABILITY_PER_CYCLE.get();
    }
    public static int repairUpgradeDurabilityPerXp() {
        return REPAIR_UPGRADE_DURABILITY_PER_XP.get();
    }

    public static boolean canWearBackpackOnBack() {
        return CAN_WEAR_BACKPACK_ON_BACK.get();
    }

    public static boolean allowBackpackInCuriosSlot() {
        return ALLOW_BACKPACK_IN_CURIOS_SLOT.get();
    }

    public static boolean allowBackpackInBackpack() {
        return ALLOW_BACKPACK_IN_BACKPACK.get();
    }

    public static boolean allowOtherPlayersToOpenWornBackpacks() {
        return ALLOW_OTHER_PLAYERS_TO_OPEN_WORN_BACKPACKS.get();
    }

    public static boolean showWelcomeMessage() {
        return SHOW_WELCOME_MESSAGE.get();
    }

    public static void setShowWelcomeMessage(boolean value) {
        set(SHOW_WELCOME_MESSAGE, value);
    }

    public static boolean pickupNotifierEnabled() {
        return PICKUP_NOTIFIER_ENABLED.get();
    }

    public static boolean pickupNotifierShowWorldPickups() {
        return PICKUP_NOTIFIER_ENABLED.get() && PICKUP_NOTIFIER_SHOW_WORLD_PICKUPS.get();
    }

    public static boolean pickupNotifierShowWorldPickupsRaw() {
        return PICKUP_NOTIFIER_SHOW_WORLD_PICKUPS.get();
    }

    public static boolean pickupNotifierShowManualTransfers() {
        return PICKUP_NOTIFIER_ENABLED.get() && PICKUP_NOTIFIER_SHOW_MANUAL_TRANSFERS.get();
    }

    public static boolean pickupNotifierShowManualTransfersRaw() {
        return PICKUP_NOTIFIER_SHOW_MANUAL_TRANSFERS.get();
    }

    public static boolean pickupNotifierShowUpgradeRouting() {
        return PICKUP_NOTIFIER_ENABLED.get() && PICKUP_NOTIFIER_SHOW_UPGRADE_ROUTING.get();
    }

    public static boolean pickupNotifierShowUpgradeRoutingRaw() {
        return PICKUP_NOTIFIER_SHOW_UPGRADE_ROUTING.get();
    }

    public static boolean pickupNotifierShowAutomation() {
        return PICKUP_NOTIFIER_ENABLED.get() && PICKUP_NOTIFIER_SHOW_AUTOMATION.get();
    }

    public static boolean pickupNotifierShowAutomationRaw() {
        return PICKUP_NOTIFIER_SHOW_AUTOMATION.get();
    }

    public static boolean pickupNotifierShowProcessingResults() {
        return PICKUP_NOTIFIER_ENABLED.get() && PICKUP_NOTIFIER_SHOW_PROCESSING_RESULTS.get();
    }

    public static boolean pickupNotifierShowProcessingResultsRaw() {
        return PICKUP_NOTIFIER_SHOW_PROCESSING_RESULTS.get();
    }

    public static boolean pickupNotifierShowContainerTransfers() {
        return PICKUP_NOTIFIER_ENABLED.get() && PICKUP_NOTIFIER_SHOW_CONTAINER_TRANSFERS.get();
    }

    public static boolean pickupNotifierShowContainerTransfersRaw() {
        return PICKUP_NOTIFIER_SHOW_CONTAINER_TRANSFERS.get();
    }

    public static boolean pickupNotifierShowBackpackName() {
        return PICKUP_NOTIFIER_SHOW_BACKPACK_NAME.get();
    }

    public static boolean pickupNotifierShowDestination() {
        return PICKUP_NOTIFIER_SHOW_DESTINATION.get();
    }

    public static boolean pickupNotifierShowTotalStoredAmount() {
        return PICKUP_NOTIFIER_SHOW_TOTAL_STORED_AMOUNT.get();
    }

    public static int pickupNotifierDisplayTicks() {
        return PICKUP_NOTIFIER_DISPLAY_TICKS.get();
    }

    public static int pickupNotifierGroupingWindowTicks() {
        return PICKUP_NOTIFIER_GROUPING_WINDOW_TICKS.get();
    }

    public static int pickupNotifierMaxVisible() {
        return PICKUP_NOTIFIER_MAX_VISIBLE.get();
    }

    public static int pickupNotifierQueueLimit() {
        return PICKUP_NOTIFIER_QUEUE_LIMIT.get();
    }

    public static int magnetRadius() {
        return MAGNET_RADIUS.get();
    }

    public static boolean blockDropMagnetProtectionEnabled() {
        return BLOCK_DROP_MAGNET_PROTECTION_ENABLED.get();
    }

    public static int blockDropMagnetProtectionSeconds() {
        return BLOCK_DROP_MAGNET_PROTECTION_SECONDS.get();
    }

    public static int advancedMagnetRadius() {
        return ADVANCED_MAGNET_RADIUS.get();
    }

    public static int quiverSlotCount() {
        return QUIVER_SLOT_COUNT.get();
    }

    public static boolean allowMultipleQuiverUpgrades() {
        return ALLOW_MULTIPLE_QUIVER_UPGRADES.get();
    }

    public static boolean allowQuiverFireworkRockets() {
        return ALLOW_QUIVER_FIREWORK_ROCKETS.get();
    }

    public static boolean allowQuiverModdedProjectileTags() {
        return ALLOW_QUIVER_MODDED_PROJECTILE_TAGS.get();
    }

    public static int trashCanDeletionDelaySeconds() {
        return TRASH_CAN_DELETION_DELAY_SECONDS.get();
    }

    public static boolean trashCanDeletePendingItemOnClose() {
        return TRASH_CAN_DELETE_PENDING_ON_CLOSE.get();
    }

    public static boolean trashCanEnableDeleteNowButton() {
        return TRASH_CAN_ENABLE_DELETE_NOW_BUTTON.get();
    }

    public static boolean allowMultipleTrashCanUpgrades() {
        return ALLOW_MULTIPLE_TRASH_CAN_UPGRADES.get();
    }

    public static TrashProtectionLevel trashCanProtectionLevel() {
        return TRASH_CAN_PROTECTION_LEVEL.get();
    }

    public static boolean trashCanConfirmModdedItems() {
        return TRASH_CAN_CONFIRM_MODDED_ITEMS.get();
    }

    public static boolean trashCanConfirmContainerItems() {
        return TRASH_CAN_CONFIRM_CONTAINER_ITEMS.get();
    }

    public static boolean trashCanConfirmDamagedItems() {
        return TRASH_CAN_CONFIRM_DAMAGED_ITEMS.get();
    }

    public static boolean rescueUpgradeEnabled() {
        return RESCUE_UPGRADE_ENABLED.get();
    }

    public static boolean rescueAllowBackpackTotem() {
        return RESCUE_UPGRADE_ENABLED.get() && RESCUE_ALLOW_BACKPACK_TOTEM.get();
    }

    public static boolean rescueAllowBackpackTotemRaw() {
        return RESCUE_ALLOW_BACKPACK_TOTEM.get();
    }

    public static boolean rescueAllowGoldenApple() {
        return RESCUE_UPGRADE_ENABLED.get() && RESCUE_ALLOW_GOLDEN_APPLE.get();
    }

    public static boolean rescueAllowGoldenAppleRaw() {
        return RESCUE_ALLOW_GOLDEN_APPLE.get();
    }

    public static boolean rescueAllowEnchantedGoldenApple() {
        return RESCUE_UPGRADE_ENABLED.get() && RESCUE_ALLOW_ENCHANTED_GOLDEN_APPLE.get();
    }

    public static boolean rescueAllowEnchantedGoldenAppleRaw() {
        return RESCUE_ALLOW_ENCHANTED_GOLDEN_APPLE.get();
    }

    public static boolean rescueAllowFall() {
        return RESCUE_UPGRADE_ENABLED.get() && RESCUE_ALLOW_FALL.get();
    }

    public static boolean rescueAllowFallRaw() {
        return RESCUE_ALLOW_FALL.get();
    }

    public static boolean rescueAllowLava() {
        return RESCUE_UPGRADE_ENABLED.get() && RESCUE_ALLOW_LAVA.get();
    }

    public static boolean rescueAllowLavaRaw() {
        return RESCUE_ALLOW_LAVA.get();
    }

    public static boolean rescueAllowFire() {
        return RESCUE_UPGRADE_ENABLED.get() && RESCUE_ALLOW_FIRE.get();
    }

    public static boolean rescueAllowFireRaw() {
        return RESCUE_ALLOW_FIRE.get();
    }

    public static boolean rescueRespectDimensionWaterRestrictions() {
        return RESCUE_RESPECT_DIMENSION_WATER_RESTRICTIONS.get();
    }

    public static boolean rescueEnableNotifications() {
        return RESCUE_ENABLE_NOTIFICATIONS.get();
    }

    public static boolean allowMultipleRescueUpgrades() {
        return ALLOW_MULTIPLE_RESCUE_UPGRADES.get();
    }

    public static int rescueGlobalCooldownTicks() {
        return RESCUE_GLOBAL_COOLDOWN_SECONDS.get() * 20;
    }

    public static int rescueGlobalCooldownSeconds() {
        return RESCUE_GLOBAL_COOLDOWN_SECONDS.get();
    }

    public static int rescueTotemCooldownTicks() {
        return RESCUE_TOTEM_COOLDOWN_SECONDS.get() * 20;
    }

    public static int rescueTotemCooldownSeconds() {
        return RESCUE_TOTEM_COOLDOWN_SECONDS.get();
    }

    public static int rescueGoldenAppleCooldownTicks() {
        return RESCUE_GOLDEN_APPLE_COOLDOWN_SECONDS.get() * 20;
    }

    public static int rescueGoldenAppleCooldownSeconds() {
        return RESCUE_GOLDEN_APPLE_COOLDOWN_SECONDS.get();
    }

    public static int rescueFallCooldownTicks() {
        return RESCUE_FALL_COOLDOWN_SECONDS.get() * 20;
    }

    public static int rescueFallCooldownSeconds() {
        return RESCUE_FALL_COOLDOWN_SECONDS.get();
    }

    public static int rescueLavaCooldownTicks() {
        return RESCUE_LAVA_COOLDOWN_SECONDS.get() * 20;
    }

    public static int rescueLavaCooldownSeconds() {
        return RESCUE_LAVA_COOLDOWN_SECONDS.get();
    }

    public static int rescueMaximumFallScanDistance() {
        return RESCUE_MAXIMUM_FALL_SCAN_DISTANCE.get();
    }

    public static boolean builderUpgradeEnabled() {
        return BUILDER_UPGRADE_ENABLED.get();
    }

    public static boolean builderAllowKeepFullMode() {
        return BUILDER_UPGRADE_ENABLED.get() && BUILDER_ALLOW_KEEP_FULL_MODE.get();
    }

    public static boolean builderAllowKeepFullModeRaw() {
        return BUILDER_ALLOW_KEEP_FULL_MODE.get();
    }

    public static boolean builderAllowOffhandRefill() {
        return BUILDER_UPGRADE_ENABLED.get() && BUILDER_ALLOW_OFFHAND_REFILL.get();
    }

    public static boolean builderAllowOffhandRefillRaw() {
        return BUILDER_ALLOW_OFFHAND_REFILL.get();
    }

    public static boolean builderAllowModdedBlocks() {
        return BUILDER_UPGRADE_ENABLED.get() && BUILDER_ALLOW_MODDED_BLOCKS.get();
    }

    public static boolean builderAllowModdedBlocksRaw() {
        return BUILDER_ALLOW_MODDED_BLOCKS.get();
    }

    public static boolean builderAllowContainerRefill() {
        return BUILDER_UPGRADE_ENABLED.get() && BUILDER_ALLOW_CONTAINER_REFILL.get();
    }

    public static boolean builderAllowContainerRefillRaw() {
        return BUILDER_ALLOW_CONTAINER_REFILL.get();
    }

    public static boolean builderAllowDangerousRefill() {
        return BUILDER_UPGRADE_ENABLED.get() && BUILDER_ALLOW_DANGEROUS_REFILL.get();
    }

    public static boolean builderAllowDangerousRefillRaw() {
        return BUILDER_ALLOW_DANGEROUS_REFILL.get();
    }

    public static boolean builderAllowMultipleUpgrades() {
        return BUILDER_ALLOW_MULTIPLE_UPGRADES.get();
    }

    public static boolean builderFeedbackEnabled() {
        return BUILDER_FEEDBACK_ENABLED.get();
    }

    public static int builderDefaultRefillThreshold() {
        return BUILDER_DEFAULT_REFILL_THRESHOLD.get();
    }

    public static int builderAntiSpamDelayTicks() {
        return BUILDER_ANTI_SPAM_DELAY_TICKS.get();
    }

    public static boolean torchPlacerUpgradeEnabled() {
        return TORCH_PLACER_UPGRADE_ENABLED.get();
    }

    public static boolean torchPlacerAllowModdedLights() {
        return TORCH_PLACER_UPGRADE_ENABLED.get() && TORCH_PLACER_ALLOW_MODDED_LIGHTS.get();
    }

    public static boolean torchPlacerAllowModdedLightsRaw() {
        return TORCH_PLACER_ALLOW_MODDED_LIGHTS.get();
    }

    public static boolean torchPlacerAllowMultipleUpgrades() {
        return TORCH_PLACER_ALLOW_MULTIPLE_UPGRADES.get();
    }

    public static boolean torchPlacerAllowCreative() {
        return TORCH_PLACER_UPGRADE_ENABLED.get() && TORCH_PLACER_ALLOW_CREATIVE.get();
    }

    public static boolean torchPlacerAllowCreativeRaw() {
        return TORCH_PLACER_ALLOW_CREATIVE.get();
    }

    public static boolean torchPlacerAllowAdventure() {
        return TORCH_PLACER_UPGRADE_ENABLED.get() && TORCH_PLACER_ALLOW_ADVENTURE.get();
    }

    public static boolean torchPlacerAllowAdventureRaw() {
        return TORCH_PLACER_ALLOW_ADVENTURE.get();
    }

    public static boolean torchPlacerFeedbackEnabled() {
        return TORCH_PLACER_FEEDBACK_ENABLED.get();
    }

    public static int torchPlacerCheckIntervalTicks() {
        return TORCH_PLACER_CHECK_INTERVAL_TICKS.get();
    }

    public static int torchPlacerPlacementCooldownTicks() {
        return TORCH_PLACER_PLACEMENT_COOLDOWN_TICKS.get();
    }

    public static int torchPlacerFailureCooldownTicks() {
        return TORCH_PLACER_FAILURE_COOLDOWN_TICKS.get();
    }

    public static boolean capacityWarningUpgradeEnabled() {
        return CAPACITY_WARNING_UPGRADE_ENABLED.get();
    }

    public static boolean capacityWarningAllowStackCapacityMode() {
        return CAPACITY_WARNING_UPGRADE_ENABLED.get() && CAPACITY_WARNING_ALLOW_STACK_CAPACITY_MODE.get();
    }

    public static boolean capacityWarningAllowStackCapacityModeRaw() {
        return CAPACITY_WARNING_ALLOW_STACK_CAPACITY_MODE.get();
    }

    public static boolean capacityWarningAllowMultipleUpgrades() {
        return CAPACITY_WARNING_ALLOW_MULTIPLE_UPGRADES.get();
    }

    public static boolean capacityWarningAllowPersistentHud() {
        return CAPACITY_WARNING_ALLOW_PERSISTENT_HUD.get();
    }

    public static boolean capacityWarningDebugConsistencyChecks() {
        return CAPACITY_WARNING_DEBUG_CONSISTENCY_CHECKS.get();
    }

    public static int capacityWarningDefaultThreshold1() {
        return CAPACITY_WARNING_DEFAULT_THRESHOLD_1.get();
    }

    public static int capacityWarningDefaultThreshold2() {
        return CAPACITY_WARNING_DEFAULT_THRESHOLD_2.get();
    }

    public static int capacityWarningDefaultThreshold3() {
        return CAPACITY_WARNING_DEFAULT_THRESHOLD_3.get();
    }

    public static int capacityWarningDefaultResetMargin() {
        return CAPACITY_WARNING_DEFAULT_RESET_MARGIN.get();
    }

    public static int capacityWarningNotificationCooldownTicks() {
        return CAPACITY_WARNING_NOTIFICATION_COOLDOWN_SECONDS.get() * 20;
    }

    public static int capacityWarningNotificationCooldownSeconds() {
        return CAPACITY_WARNING_NOTIFICATION_COOLDOWN_SECONDS.get();
    }

    public static int capacityWarningFailedInsertionCooldownTicks() {
        return CAPACITY_WARNING_FAILED_INSERTION_COOLDOWN_TICKS.get();
    }

    public static int capacityWarningMaxBackpacksTrackedPerPlayer() {
        return CAPACITY_WARNING_MAX_BACKPACKS_TRACKED_PER_PLAYER.get();
    }

    public static int capacityWarningCheckIntervalTicks() {
        return CAPACITY_WARNING_CHECK_INTERVAL_TICKS.get();
    }

    public static int capacityWarningHudRefreshTicks() {
        return CAPACITY_WARNING_HUD_REFRESH_TICKS.get();
    }

    public static boolean itemLockUpgradeEnabled() {
        return ITEM_LOCK_UPGRADE_ENABLED.get();
    }

    public static boolean itemLockAllowMultipleUpgrades() {
        return ITEM_LOCK_ALLOW_MULTIPLE_UPGRADES.get();
    }

    public static boolean itemLockAllowSlotLocks() {
        return ITEM_LOCK_UPGRADE_ENABLED.get() && ITEM_LOCK_ALLOW_SLOT_LOCKS.get();
    }

    public static boolean itemLockAllowSlotLocksRaw() {
        return ITEM_LOCK_ALLOW_SLOT_LOCKS.get();
    }

    public static boolean itemLockAllowItemLocks() {
        return ITEM_LOCK_UPGRADE_ENABLED.get() && ITEM_LOCK_ALLOW_ITEM_LOCKS.get();
    }

    public static boolean itemLockAllowItemLocksRaw() {
        return ITEM_LOCK_ALLOW_ITEM_LOCKS.get();
    }

    public static boolean itemLockAllowTypeLocks() {
        return ITEM_LOCK_UPGRADE_ENABLED.get() && ITEM_LOCK_ALLOW_TYPE_LOCKS.get();
    }

    public static boolean itemLockAllowTypeLocksRaw() {
        return ITEM_LOCK_ALLOW_TYPE_LOCKS.get();
    }

    public static boolean itemLockAllowManualAccess() {
        return ITEM_LOCK_UPGRADE_ENABLED.get() && ITEM_LOCK_ALLOW_MANUAL_ACCESS.get();
    }

    public static boolean itemLockAllowManualAccessRaw() {
        return ITEM_LOCK_ALLOW_MANUAL_ACCESS.get();
    }

    public static boolean itemLockAllowAutomationAccess() {
        return ITEM_LOCK_UPGRADE_ENABLED.get() && ITEM_LOCK_ALLOW_AUTOMATION_ACCESS.get();
    }

    public static boolean itemLockAllowAutomationAccessRaw() {
        return ITEM_LOCK_ALLOW_AUTOMATION_ACCESS.get();
    }

    public static boolean backpackLinkUpgradeEnabled() {
        return BACKPACK_LINK_UPGRADE_ENABLED.get();
    }

    public static boolean storageMonitorEnabled() {
        return STORAGE_MONITOR_ENABLED.get();
    }

    public static boolean storageNetworkEnabled() {
        return STORAGE_NETWORK_ENABLED.get();
    }

    public static int storageNetworkMaxCablePath() {
        return STORAGE_NETWORK_MAX_CABLE_PATH.get();
    }

    public static int storageNetworkMaxNodes() {
        return STORAGE_NETWORK_MAX_NODES.get();
    }

    public static boolean storageImporterEnabled() {
        return STORAGE_IMPORTER_ENABLED.get();
    }

    public static boolean storageExporterEnabled() {
        return STORAGE_EXPORTER_ENABLED.get();
    }

    public static int storageImporterTransferInterval() {
        return STORAGE_IMPORTER_TRANSFER_INTERVAL.get();
    }

    public static int storageImporterTransferAmount() {
        return STORAGE_IMPORTER_TRANSFER_AMOUNT.get();
    }

    public static int storageExporterTransferInterval() {
        return STORAGE_EXPORTER_TRANSFER_INTERVAL.get();
    }

    public static int storageExporterTransferAmount() {
        return STORAGE_EXPORTER_TRANSFER_AMOUNT.get();
    }

    public static boolean storageTransferRedstoneControlEnabled() {
        return STORAGE_TRANSFER_REDSTONE_CONTROL_ENABLED.get();
    }

    public static boolean storageMonitorCrossDimensionAccess() {
        return STORAGE_MONITOR_CROSS_DIMENSION_ACCESS.get();
    }

    public static boolean backpackLinkAllowMultipleUpgrades() {
        return BACKPACK_LINK_ALLOW_MULTIPLE_UPGRADES.get();
    }

    public static boolean backpackLinkAllowPublicAnchors() {
        return BACKPACK_LINK_ALLOW_PUBLIC_ANCHORS.get();
    }

    public static boolean backpackLinkAllowCrossDimension() {
        return BACKPACK_LINK_ALLOW_CROSS_DIMENSION.get();
    }

    public static int backpackLinkMaxDestinations() {
        return BACKPACK_LINK_MAX_DESTINATIONS.get();
    }

    public static int backpackLinkSafeArrivalRadius() {
        return BACKPACK_LINK_SAFE_ARRIVAL_RADIUS.get();
    }

    public static int backpackLinkSameDimensionBaseXpLevels() {
        return BACKPACK_LINK_SAME_DIMENSION_BASE_XP_LEVELS.get();
    }

    public static int backpackLinkDistanceXpPer1000Blocks() {
        return BACKPACK_LINK_DISTANCE_XP_PER_1000_BLOCKS.get();
    }

    public static int backpackLinkCrossDimensionXpLevels() {
        return BACKPACK_LINK_CROSS_DIMENSION_XP_LEVELS.get();
    }

    public static int backpackLinkMaxXpLevelCost() {
        return BACKPACK_LINK_MAX_XP_LEVEL_COST.get();
    }

    public static int backpackLinkCooldownTicks(boolean crossDimension) {
        return 0;
    }

    public static int backpackLinkCooldownSeconds() {
        return BACKPACK_LINK_COOLDOWN_SECONDS.get();
    }

    public static int backpackLinkCrossDimensionCooldownSeconds() {
        return BACKPACK_LINK_CROSS_DIMENSION_COOLDOWN_SECONDS.get();
    }

    public static boolean abandonedBackpacksEnabled() { return ABANDONED_BACKPACKS_ENABLED.get(); }
    public static boolean backpackerCampEnabled() { return BACKPACKER_CAMP_ENABLED.get(); }
    public static int backpackerCampAbandonedWeight() { return BACKPACKER_CAMP_ABANDONED_WEIGHT.get(); }
    public static int backpackerCampBackpackWeight() { return BACKPACKER_CAMP_BACKPACK_WEIGHT.get(); }
    public static int backpackerCampTraderWeight() { return BACKPACKER_CAMP_TRADER_WEIGHT.get(); }
    public static double backpackerCampBackpackLootChance() { return BACKPACKER_CAMP_BACKPACK_LOOT_CHANCE.get(); }
    public static double abandonedBackpackUpgradeChance() { return ABANDONED_BACKPACK_UPGRADE_CHANCE.get(); }
    public static double abandonedBackpackCustomNameChance() { return ABANDONED_BACKPACK_CUSTOM_NAME_CHANCE.get(); }
    public static double abandonedBackpackChance(AbandonedBackpackProfile profile) {
        return profile == AbandonedBackpackProfile.CAMP
                ? BACKPACKER_CAMP_BACKPACK_LOOT_CHANCE.get() : ABANDONED_BACKPACK_CHANCES.get(profile).get();
    }
    public static boolean mobBackpacksEnabled() { return MOB_BACKPACKS_ENABLED.get(); }
    public static double mobBackpackSpawnChance() { return MOB_BACKPACK_SPAWN_CHANCE.get(); }
    public static double mobBackpackDropChance() { return MOB_BACKPACK_DROP_CHANCE.get(); }
    public static boolean mobBackpackNaturalSpawnsOnly() { return MOB_BACKPACK_NATURAL_SPAWNS_ONLY.get(); }
    public static int mobBackpackMinLootEntries() { return MOB_BACKPACK_MIN_LOOT_ENTRIES.get(); }
    public static int mobBackpackMaxLootEntries() { return MOB_BACKPACK_MAX_LOOT_ENTRIES.get(); }
    public static boolean mobBackpackMobEnabled(MobBackpackType type) { return MOB_BACKPACK_MOB_ENABLED.get(type).get(); }
    public static double mobBackpackMobChance(MobBackpackType type) { return MOB_BACKPACK_MOB_CHANCE.get(type).get(); }
    public static String mobBackpackMobColor(MobBackpackType type) { return MOB_BACKPACK_MOB_COLOR.get(type).get(); }
    public static int mobBackpackTierWeight(BackpackTier tier) {
        ModConfigSpec.IntValue value = MOB_BACKPACK_TIER_WEIGHT.get(tier);
        return value == null ? 0 : Math.max(0, value.get());
    }

    public static void setCanWearBackpackOnBack(boolean value) {
        set(CAN_WEAR_BACKPACK_ON_BACK, value);
    }

    public static void setAllowBackpackInCuriosSlot(boolean value) {
        set(ALLOW_BACKPACK_IN_CURIOS_SLOT, value);
    }

    public static void setAllowBackpackInBackpack(boolean value) {
        set(ALLOW_BACKPACK_IN_BACKPACK, value);
    }

    public static void setAllowOtherPlayersToOpenWornBackpacks(boolean value) {
        set(ALLOW_OTHER_PLAYERS_TO_OPEN_WORN_BACKPACKS, value);
    }

    public static void setPickupNotifierEnabled(boolean value) {
        set(PICKUP_NOTIFIER_ENABLED, value);
    }

    public static void setPickupNotifierShowWorldPickups(boolean value) {
        set(PICKUP_NOTIFIER_SHOW_WORLD_PICKUPS, value);
    }

    public static void setPickupNotifierShowManualTransfers(boolean value) {
        set(PICKUP_NOTIFIER_SHOW_MANUAL_TRANSFERS, value);
    }

    public static void setPickupNotifierShowUpgradeRouting(boolean value) {
        set(PICKUP_NOTIFIER_SHOW_UPGRADE_ROUTING, value);
    }

    public static void setPickupNotifierShowAutomation(boolean value) {
        set(PICKUP_NOTIFIER_SHOW_AUTOMATION, value);
    }

    public static void setPickupNotifierShowProcessingResults(boolean value) {
        set(PICKUP_NOTIFIER_SHOW_PROCESSING_RESULTS, value);
    }

    public static void setPickupNotifierShowContainerTransfers(boolean value) {
        set(PICKUP_NOTIFIER_SHOW_CONTAINER_TRANSFERS, value);
    }

    public static void setPickupNotifierShowBackpackName(boolean value) {
        set(PICKUP_NOTIFIER_SHOW_BACKPACK_NAME, value);
    }

    public static void setPickupNotifierShowDestination(boolean value) {
        set(PICKUP_NOTIFIER_SHOW_DESTINATION, value);
    }

    public static void setPickupNotifierShowTotalStoredAmount(boolean value) {
        set(PICKUP_NOTIFIER_SHOW_TOTAL_STORED_AMOUNT, value);
    }

    public static void setPickupNotifierDisplayTicks(int value) {
        set(PICKUP_NOTIFIER_DISPLAY_TICKS, value);
    }

    public static void setPickupNotifierGroupingWindowTicks(int value) {
        set(PICKUP_NOTIFIER_GROUPING_WINDOW_TICKS, value);
    }

    public static void setPickupNotifierMaxVisible(int value) {
        set(PICKUP_NOTIFIER_MAX_VISIBLE, value);
    }

    public static void setPickupNotifierQueueLimit(int value) {
        set(PICKUP_NOTIFIER_QUEUE_LIMIT, value);
    }

    public static void setMagnetRadius(int value) {
        set(MAGNET_RADIUS, value);
    }

    public static void setBlockDropMagnetProtectionEnabled(boolean value) {
        set(BLOCK_DROP_MAGNET_PROTECTION_ENABLED, value);
    }

    public static void setBlockDropMagnetProtectionSeconds(int value) {
        set(BLOCK_DROP_MAGNET_PROTECTION_SECONDS, value);
    }

    public static void setAdvancedMagnetRadius(int value) {
        set(ADVANCED_MAGNET_RADIUS, value);
    }

    public static void setQuiverSlotCount(int value) {
        set(QUIVER_SLOT_COUNT, value);
    }

    public static void setAllowMultipleQuiverUpgrades(boolean value) {
        set(ALLOW_MULTIPLE_QUIVER_UPGRADES, value);
    }

    public static void setAllowQuiverFireworkRockets(boolean value) {
        set(ALLOW_QUIVER_FIREWORK_ROCKETS, value);
    }

    public static void setAllowQuiverModdedProjectileTags(boolean value) {
        set(ALLOW_QUIVER_MODDED_PROJECTILE_TAGS, value);
    }

    public static void setTrashCanDeletionDelaySeconds(int value) {
        set(TRASH_CAN_DELETION_DELAY_SECONDS, value);
    }

    public static void setTrashCanDeletePendingItemOnClose(boolean value) {
        set(TRASH_CAN_DELETE_PENDING_ON_CLOSE, value);
    }

    public static void setTrashCanEnableDeleteNowButton(boolean value) {
        set(TRASH_CAN_ENABLE_DELETE_NOW_BUTTON, value);
    }

    public static void setAllowMultipleTrashCanUpgrades(boolean value) {
        set(ALLOW_MULTIPLE_TRASH_CAN_UPGRADES, value);
    }

    public static void setTrashCanProtectionLevel(TrashProtectionLevel value) {
        set(TRASH_CAN_PROTECTION_LEVEL, value);
    }

    public static void setTrashCanConfirmModdedItems(boolean value) {
        set(TRASH_CAN_CONFIRM_MODDED_ITEMS, value);
    }

    public static void setTrashCanConfirmContainerItems(boolean value) {
        set(TRASH_CAN_CONFIRM_CONTAINER_ITEMS, value);
    }

    public static void setTrashCanConfirmDamagedItems(boolean value) {
        set(TRASH_CAN_CONFIRM_DAMAGED_ITEMS, value);
    }

    public static void setRescueUpgradeEnabled(boolean value) {
        set(RESCUE_UPGRADE_ENABLED, value);
    }

    public static void setRescueAllowBackpackTotem(boolean value) {
        set(RESCUE_ALLOW_BACKPACK_TOTEM, value);
    }

    public static void setRescueAllowGoldenApple(boolean value) {
        set(RESCUE_ALLOW_GOLDEN_APPLE, value);
    }

    public static void setRescueAllowEnchantedGoldenApple(boolean value) {
        set(RESCUE_ALLOW_ENCHANTED_GOLDEN_APPLE, value);
    }

    public static void setRescueAllowFall(boolean value) {
        set(RESCUE_ALLOW_FALL, value);
    }

    public static void setRescueAllowLava(boolean value) {
        set(RESCUE_ALLOW_LAVA, value);
    }

    public static void setRescueAllowFire(boolean value) {
        set(RESCUE_ALLOW_FIRE, value);
    }

    public static void setRescueRespectDimensionWaterRestrictions(boolean value) {
        set(RESCUE_RESPECT_DIMENSION_WATER_RESTRICTIONS, value);
    }

    public static void setRescueEnableNotifications(boolean value) {
        set(RESCUE_ENABLE_NOTIFICATIONS, value);
    }

    public static void setAllowMultipleRescueUpgrades(boolean value) {
        set(ALLOW_MULTIPLE_RESCUE_UPGRADES, value);
    }

    public static void setRescueGlobalCooldownSeconds(int value) {
        set(RESCUE_GLOBAL_COOLDOWN_SECONDS, value);
    }

    public static void setRescueTotemCooldownSeconds(int value) {
        set(RESCUE_TOTEM_COOLDOWN_SECONDS, value);
    }

    public static void setRescueGoldenAppleCooldownSeconds(int value) {
        set(RESCUE_GOLDEN_APPLE_COOLDOWN_SECONDS, value);
    }

    public static void setRescueFallCooldownSeconds(int value) {
        set(RESCUE_FALL_COOLDOWN_SECONDS, value);
    }

    public static void setRescueLavaCooldownSeconds(int value) {
        set(RESCUE_LAVA_COOLDOWN_SECONDS, value);
    }

    public static void setRescueMaximumFallScanDistance(int value) {
        set(RESCUE_MAXIMUM_FALL_SCAN_DISTANCE, value);
    }

    public static void setBuilderUpgradeEnabled(boolean value) {
        set(BUILDER_UPGRADE_ENABLED, value);
    }

    public static void setBuilderAllowKeepFullMode(boolean value) {
        set(BUILDER_ALLOW_KEEP_FULL_MODE, value);
    }

    public static void setBuilderAllowOffhandRefill(boolean value) {
        set(BUILDER_ALLOW_OFFHAND_REFILL, value);
    }

    public static void setBuilderAllowModdedBlocks(boolean value) {
        set(BUILDER_ALLOW_MODDED_BLOCKS, value);
    }

    public static void setBuilderAllowContainerRefill(boolean value) {
        set(BUILDER_ALLOW_CONTAINER_REFILL, value);
    }

    public static void setBuilderAllowDangerousRefill(boolean value) {
        set(BUILDER_ALLOW_DANGEROUS_REFILL, value);
    }

    public static void setBuilderAllowMultipleUpgrades(boolean value) {
        set(BUILDER_ALLOW_MULTIPLE_UPGRADES, value);
    }

    public static void setBuilderFeedbackEnabled(boolean value) {
        set(BUILDER_FEEDBACK_ENABLED, value);
    }

    public static void setBuilderDefaultRefillThreshold(int value) {
        set(BUILDER_DEFAULT_REFILL_THRESHOLD, value);
    }

    public static void setBuilderAntiSpamDelayTicks(int value) {
        set(BUILDER_ANTI_SPAM_DELAY_TICKS, value);
    }

    public static void setTorchPlacerUpgradeEnabled(boolean value) {
        set(TORCH_PLACER_UPGRADE_ENABLED, value);
    }

    public static void setTorchPlacerAllowModdedLights(boolean value) {
        set(TORCH_PLACER_ALLOW_MODDED_LIGHTS, value);
    }

    public static void setTorchPlacerAllowMultipleUpgrades(boolean value) {
        set(TORCH_PLACER_ALLOW_MULTIPLE_UPGRADES, value);
    }

    public static void setTorchPlacerAllowCreative(boolean value) {
        set(TORCH_PLACER_ALLOW_CREATIVE, value);
    }

    public static void setTorchPlacerAllowAdventure(boolean value) {
        set(TORCH_PLACER_ALLOW_ADVENTURE, value);
    }

    public static void setTorchPlacerFeedbackEnabled(boolean value) {
        set(TORCH_PLACER_FEEDBACK_ENABLED, value);
    }

    public static void setTorchPlacerCheckIntervalTicks(int value) {
        set(TORCH_PLACER_CHECK_INTERVAL_TICKS, value);
    }

    public static void setTorchPlacerPlacementCooldownTicks(int value) {
        set(TORCH_PLACER_PLACEMENT_COOLDOWN_TICKS, value);
    }

    public static void setTorchPlacerFailureCooldownTicks(int value) {
        set(TORCH_PLACER_FAILURE_COOLDOWN_TICKS, value);
    }

    public static void setCapacityWarningUpgradeEnabled(boolean value) {
        set(CAPACITY_WARNING_UPGRADE_ENABLED, value);
    }

    public static void setCapacityWarningAllowStackCapacityMode(boolean value) {
        set(CAPACITY_WARNING_ALLOW_STACK_CAPACITY_MODE, value);
    }

    public static void setCapacityWarningAllowPersistentHud(boolean value) {
        set(CAPACITY_WARNING_ALLOW_PERSISTENT_HUD, value);
    }

    public static void setCapacityWarningAllowMultipleUpgrades(boolean value) {
        set(CAPACITY_WARNING_ALLOW_MULTIPLE_UPGRADES, value);
    }

    public static void setCapacityWarningDebugConsistencyChecks(boolean value) {
        set(CAPACITY_WARNING_DEBUG_CONSISTENCY_CHECKS, value);
    }

    public static void setCapacityWarningDefaultThreshold1(int value) {
        set(CAPACITY_WARNING_DEFAULT_THRESHOLD_1, value);
    }

    public static void setCapacityWarningDefaultThreshold2(int value) {
        set(CAPACITY_WARNING_DEFAULT_THRESHOLD_2, value);
    }

    public static void setCapacityWarningDefaultThreshold3(int value) {
        set(CAPACITY_WARNING_DEFAULT_THRESHOLD_3, value);
    }

    public static void setCapacityWarningDefaultResetMargin(int value) {
        set(CAPACITY_WARNING_DEFAULT_RESET_MARGIN, value);
    }

    public static void setCapacityWarningNotificationCooldownSeconds(int value) {
        set(CAPACITY_WARNING_NOTIFICATION_COOLDOWN_SECONDS, value);
    }

    public static void setCapacityWarningFailedInsertionCooldownTicks(int value) {
        set(CAPACITY_WARNING_FAILED_INSERTION_COOLDOWN_TICKS, value);
    }

    public static void setCapacityWarningMaxBackpacksTrackedPerPlayer(int value) {
        set(CAPACITY_WARNING_MAX_BACKPACKS_TRACKED_PER_PLAYER, value);
    }

    public static void setCapacityWarningCheckIntervalTicks(int value) {
        set(CAPACITY_WARNING_CHECK_INTERVAL_TICKS, value);
    }

    public static void setCapacityWarningHudRefreshTicks(int value) {
        set(CAPACITY_WARNING_HUD_REFRESH_TICKS, value);
    }

    public static void setItemLockUpgradeEnabled(boolean value) {
        set(ITEM_LOCK_UPGRADE_ENABLED, value);
    }

    public static void setItemLockAllowMultipleUpgrades(boolean value) {
        set(ITEM_LOCK_ALLOW_MULTIPLE_UPGRADES, value);
    }

    public static void setItemLockAllowSlotLocks(boolean value) {
        set(ITEM_LOCK_ALLOW_SLOT_LOCKS, value);
    }

    public static void setItemLockAllowItemLocks(boolean value) {
        set(ITEM_LOCK_ALLOW_ITEM_LOCKS, value);
    }

    public static void setItemLockAllowTypeLocks(boolean value) {
        set(ITEM_LOCK_ALLOW_TYPE_LOCKS, value);
    }

    public static void setItemLockAllowManualAccess(boolean value) {
        set(ITEM_LOCK_ALLOW_MANUAL_ACCESS, value);
    }

    public static void setItemLockAllowAutomationAccess(boolean value) {
        set(ITEM_LOCK_ALLOW_AUTOMATION_ACCESS, value);
    }

    public static void setBackpackLinkUpgradeEnabled(boolean value) {
        set(BACKPACK_LINK_UPGRADE_ENABLED, value);
    }

    public static void setStorageMonitorEnabled(boolean value) {
        set(STORAGE_MONITOR_ENABLED, value);
    }

    public static void setStorageNetworkEnabled(boolean value) {
        set(STORAGE_NETWORK_ENABLED, value);
    }

    public static void setStorageNetworkMaxCablePath(int value) {
        set(STORAGE_NETWORK_MAX_CABLE_PATH, value);
    }

    public static void setStorageNetworkMaxNodes(int value) {
        set(STORAGE_NETWORK_MAX_NODES, value);
    }

    public static void setStorageImporterEnabled(boolean value) {
        set(STORAGE_IMPORTER_ENABLED, value);
    }

    public static void setStorageExporterEnabled(boolean value) {
        set(STORAGE_EXPORTER_ENABLED, value);
    }

    public static void setStorageImporterTransferInterval(int value) {
        set(STORAGE_IMPORTER_TRANSFER_INTERVAL, value);
    }

    public static void setStorageImporterTransferAmount(int value) {
        set(STORAGE_IMPORTER_TRANSFER_AMOUNT, value);
    }

    public static void setStorageExporterTransferInterval(int value) {
        set(STORAGE_EXPORTER_TRANSFER_INTERVAL, value);
    }

    public static void setStorageExporterTransferAmount(int value) {
        set(STORAGE_EXPORTER_TRANSFER_AMOUNT, value);
    }

    public static void setStorageTransferRedstoneControlEnabled(boolean value) {
        set(STORAGE_TRANSFER_REDSTONE_CONTROL_ENABLED, value);
    }

    public static void setStorageMonitorCrossDimensionAccess(boolean value) {
        set(STORAGE_MONITOR_CROSS_DIMENSION_ACCESS, value);
    }

    public static void setBackpackLinkAllowMultipleUpgrades(boolean value) {
        set(BACKPACK_LINK_ALLOW_MULTIPLE_UPGRADES, value);
    }

    public static void setBackpackLinkAllowPublicAnchors(boolean value) {
        set(BACKPACK_LINK_ALLOW_PUBLIC_ANCHORS, value);
    }

    public static void setBackpackLinkAllowCrossDimension(boolean value) {
        set(BACKPACK_LINK_ALLOW_CROSS_DIMENSION, value);
    }

    public static void setBackpackLinkMaxDestinations(int value) {
        set(BACKPACK_LINK_MAX_DESTINATIONS, value);
    }

    public static void setBackpackLinkSafeArrivalRadius(int value) {
        set(BACKPACK_LINK_SAFE_ARRIVAL_RADIUS, value);
    }

    public static void setBackpackLinkSameDimensionBaseXpLevels(int value) {
        set(BACKPACK_LINK_SAME_DIMENSION_BASE_XP_LEVELS, value);
    }

    public static void setBackpackLinkDistanceXpPer1000Blocks(int value) {
        set(BACKPACK_LINK_DISTANCE_XP_PER_1000_BLOCKS, value);
    }

    public static void setBackpackLinkCrossDimensionXpLevels(int value) {
        set(BACKPACK_LINK_CROSS_DIMENSION_XP_LEVELS, value);
    }

    public static void setBackpackLinkMaxXpLevelCost(int value) {
        set(BACKPACK_LINK_MAX_XP_LEVEL_COST, value);
    }

    public static void setBackpackLinkCooldownSeconds(int value) {
        set(BACKPACK_LINK_COOLDOWN_SECONDS, value);
    }

    public static void setBackpackLinkCrossDimensionCooldownSeconds(int value) {
        set(BACKPACK_LINK_CROSS_DIMENSION_COOLDOWN_SECONDS, value);
    }

    public static void setMobBackpacksEnabled(boolean value) {
        set(MOB_BACKPACKS_ENABLED, value);
    }

    public static void setMobBackpackSpawnChance(double value) {
        MOB_BACKPACK_SPAWN_CHANCE.set(value);
        save();
    }

    public static void setMobBackpackDropChance(double value) {
        MOB_BACKPACK_DROP_CHANCE.set(value);
        save();
    }

    public static void setMobBackpackNaturalSpawnsOnly(boolean value) {
        set(MOB_BACKPACK_NATURAL_SPAWNS_ONLY, value);
    }

    public static void setMobBackpackMinLootEntries(int value) {
        set(MOB_BACKPACK_MIN_LOOT_ENTRIES, value);
    }

    public static void setMobBackpackMaxLootEntries(int value) {
        set(MOB_BACKPACK_MAX_LOOT_ENTRIES, value);
    }

    public static void setMobBackpackMobEnabled(MobBackpackType type, boolean value) {
        set(MOB_BACKPACK_MOB_ENABLED.get(type), value);
    }

    public static void setMobBackpackMobChance(MobBackpackType type, double value) {
        MOB_BACKPACK_MOB_CHANCE.get(type).set(value);
        save();
    }

    public static void setMobBackpackMobColor(MobBackpackType type, String value) {
        MOB_BACKPACK_MOB_COLOR.get(type).set(value);
        save();
    }

    public static void setMobBackpackTierWeight(BackpackTier tier, int value) {
        ModConfigSpec.IntValue setting = MOB_BACKPACK_TIER_WEIGHT.get(tier);
        if (setting != null) {
            set(setting, value);
        }
    }

    public static boolean backpackTierEnabled(BackpackTierId tier) {
        return tier(tier).enabled.get();
    }

    public static int backpackTierSlots(BackpackTierId tier) {
        return tier(tier).slots.get();
    }

    public static boolean backpackTierWearable(BackpackTierId tier) {
        return tier(tier).wearable.get();
    }

    public static boolean backpackTierCraftingEnabled(BackpackTierId tier) {
        return tier(tier).craftingEnabled.get();
    }

    public static boolean backpackTierFireproof(BackpackTierId tier) {
        return tier(tier).fireproof.get();
    }

    public static void setBackpackTierEnabled(BackpackTierId tier, boolean value) {
        set(tier(tier).enabled, value);
    }

    public static void setBackpackTierSlots(BackpackTierId tier, int value) {
        set(tier(tier).slots, value);
    }

    public static void setBackpackTierWearable(BackpackTierId tier, boolean value) {
        set(tier(tier).wearable, value);
    }

    public static void setBackpackTierCraftingEnabled(BackpackTierId tier, boolean value) {
        set(tier(tier).craftingEnabled, value);
    }

    public static void setBackpackTierFireproof(BackpackTierId tier, boolean value) {
        set(tier(tier).fireproof, value);
    }

    public static void save() {
        if (SPEC.isLoaded()) {
            SPEC.save();
        }
    }

    private static void set(ModConfigSpec.BooleanValue setting, boolean value) {
        setting.set(value);
        save();
    }

    private static void set(ModConfigSpec.IntValue setting, int value) {
        setting.set(value);
        save();
    }

    private static <T extends Enum<T>> void set(ModConfigSpec.EnumValue<T> setting, T value) {
        setting.set(value);
        save();
    }

    private static TierConfig tier(BackpackTierId tier) {
        return switch (tier) {
            case LEATHER -> LEATHER_TIER;
            case COAL -> COAL_TIER;
            case LAPIS -> LAPIS_TIER;
            case REDSTONE -> REDSTONE_TIER;
            case QUARTZ -> QUARTZ_TIER;
            case COPPER -> COPPER_TIER;
            case IRON -> IRON_TIER;
            case GOLD -> GOLD_TIER;
            case EMERALD -> EMERALD_TIER;
            case DIAMOND -> DIAMOND_TIER;
            case NETHERITE -> NETHERITE_TIER;
        };
    }

    private static void defineMobBackpackTierWeight(ModConfigSpec.Builder builder, BackpackTier tier, int defaultWeight) {
        MOB_BACKPACK_TIER_WEIGHT.put(tier, builder.comment("Relative Mob Backpacks tier weight. Range: 0 - 100000.")
                .defineInRange(tier.name().toLowerCase() + "Weight", defaultWeight, 0, 100000));
    }

    private static final class TierConfig {
        private final ModConfigSpec.BooleanValue enabled;
        private final ModConfigSpec.IntValue slots;
        private final ModConfigSpec.BooleanValue wearable;
        private final ModConfigSpec.BooleanValue craftingEnabled;
        private final ModConfigSpec.BooleanValue fireproof;

        private TierConfig(ModConfigSpec.Builder builder, String name, int defaultSlots, boolean defaultWearable, boolean defaultCraftingEnabled, boolean defaultFireproof) {
            builder.push(name);
            this.enabled = builder
                    .comment("If true, this backpack tier stays enabled.")
                    .define("enabled", true);
            this.slots = builder
                    .comment("Stored slot count for this backpack tier. This is currently informational only and does not change existing backpacks yet.")
                    .defineInRange("slots", defaultSlots, 1, 999);
            this.wearable = builder
                    .comment("If true, this backpack tier is marked as wearable.")
                    .define("wearable", defaultWearable);
            this.craftingEnabled = builder
                    .comment("If true, this backpack tier is marked as craftable.")
                    .define("craftingEnabled", defaultCraftingEnabled);
            this.fireproof = builder
                    .comment("If true, this backpack tier is marked as fireproof.")
                    .define("fireproof", defaultFireproof);
            builder.pop();
        }
    }
}
