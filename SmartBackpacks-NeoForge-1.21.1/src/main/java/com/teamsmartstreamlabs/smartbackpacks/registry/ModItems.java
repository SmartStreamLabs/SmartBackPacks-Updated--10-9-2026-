package com.teamsmartstreamlabs.smartbackpacks.registry;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.item.AnvilUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.AutoSmeltingUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.AutoFeedUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.AutoToolUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackDisplayHookItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackLinkUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BlastFurnaceUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BrewingStandUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BuilderUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.CartographyTableUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.CapacitorUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.CapacityWarningUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.ChunkLoaderUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.CraftingTableUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.CompressionUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.DepositUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.EnderChestUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.EnchantingTableUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.FilterUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.CourierUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.FluidTransferUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.FluidStorageUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.FurnaceUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.GrindstoneUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.HopperUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.ItemLockUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.JukeboxUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.LoomUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.LightUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.NightVisionUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.FlightUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.RepairUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.FallProtectionUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.LinkCrystalItem;
import com.teamsmartstreamlabs.smartbackpacks.item.MagnetUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.NestedStorageUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.PickupUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.QuiverUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.QuickAccessWheelUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.RestockUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.RescueUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.SmokerUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.SoulboundUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.DeathEmergencyKitUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.SmithingTableUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.StorageUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.StonecutterUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.SurvivalAssistUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.TorchPlacerUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.TrashCanUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.VoidboundUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.VoidUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.WirelessUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.StorageMonitorItem;
import com.teamsmartstreamlabs.smartbackpacks.item.XpTransferUpgradeItem;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SmartBackpacks.MOD_ID);

    public static final DeferredItem<Item> LEATHER_BACKPACK = registerBackpack("leather_backpack", BackpackTier.LEATHER, fireproofBackpackProperties());
    public static final DeferredItem<Item> COAL_BACKPACK = registerBackpack("coal_backpack", BackpackTier.COAL, fireproofBackpackProperties());
    public static final DeferredItem<Item> LAPIS_BACKPACK = registerBackpack("lapis_backpack", BackpackTier.LAPIS, fireproofBackpackProperties());
    public static final DeferredItem<Item> REDSTONE_BACKPACK = registerBackpack("redstone_backpack", BackpackTier.REDSTONE, fireproofBackpackProperties());
    public static final DeferredItem<Item> QUARTZ_BACKPACK = registerBackpack("quartz_backpack", BackpackTier.QUARTZ, fireproofBackpackProperties());
    public static final DeferredItem<Item> COPPER_BACKPACK = registerBackpack("copper_backpack", BackpackTier.COPPER, fireproofBackpackProperties());
    public static final DeferredItem<Item> IRON_BACKPACK = registerBackpack("iron_backpack", BackpackTier.IRON, fireproofBackpackProperties());
    public static final DeferredItem<Item> GOLD_BACKPACK = registerBackpack("gold_backpack", BackpackTier.GOLD, fireproofBackpackProperties());
    public static final DeferredItem<Item> EMERALD_BACKPACK = registerBackpack("emerald_backpack", BackpackTier.EMERALD, fireproofBackpackProperties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> DIAMOND_BACKPACK = registerBackpack("diamond_backpack", BackpackTier.DIAMOND, fireproofBackpackProperties().rarity(Rarity.RARE));
    public static final DeferredItem<Item> NETHERITE_BACKPACK = registerBackpack("netherite_backpack", BackpackTier.NETHERITE, fireproofBackpackProperties().rarity(Rarity.EPIC));
    public static final DeferredItem<Item> ANCIENT_NETHERITE_BACKPACK = registerBackpack("ancient_netherite_backpack", BackpackTier.ANCIENT_NETHERITE, fireproofBackpackProperties().rarity(Rarity.EPIC));
    public static final DeferredItem<Item> ULTIMATE_DIAMOND_BACKPACK = registerBackpack("ultimate_diamond_backpack", BackpackTier.ULTIMATE_DIAMOND, fireproofBackpackProperties().rarity(Rarity.EPIC));
    public static final DeferredItem<Item> NETHERITE_VAULT_BACKPACK = registerBackpack("netherite_vault_backpack", BackpackTier.NETHERITE_VAULT, fireproofBackpackProperties().rarity(Rarity.EPIC));
    public static final DeferredItem<Item> BACKPACK_WORKBENCH = registerBlockItem("backpack_workbench", ModBlocks.BACKPACK_WORKBENCH);
    public static final DeferredItem<Item> BACKPACK_DISPLAY_HOOK = registerItem("backpack_display_hook",
            properties -> new BackpackDisplayHookItem(ModBlocks.BACKPACK_DISPLAY_HOOK.get(), properties),
            new Item.Properties());
    public static final DeferredItem<Item> STORAGE_CONTROLLER = registerBlockItem("storage_controller", ModBlocks.STORAGE_CONTROLLER);
    public static final DeferredItem<Item> STORAGE_CABLE = registerBlockItem("storage_cable", ModBlocks.STORAGE_CABLE);
    public static final DeferredItem<Item> STORAGE_IMPORTER = registerBlockItem("storage_importer", ModBlocks.STORAGE_IMPORTER);
    public static final DeferredItem<Item> STORAGE_EXPORTER = registerBlockItem("storage_exporter", ModBlocks.STORAGE_EXPORTER);
    public static final DeferredItem<Item> STORAGE_MONITOR = ITEMS.register("storage_monitor",
            () -> new StorageMonitorItem(new Item.Properties()));
    public static final DeferredItem<Item> MAGNET_UPGRADE = registerItem("magnet_upgrade", MagnetUpgradeItem::new, false, new Item.Properties());
    public static final DeferredItem<Item> ADVANCED_MAGNET_UPGRADE = registerItem("advanced_magnet_upgrade", MagnetUpgradeItem::new, true, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PICKUP_UPGRADE = registerItem("pickup_upgrade", PickupUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> QUIVER_UPGRADE = registerItem("quiver_upgrade", QuiverUpgradeItem::new, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> QUICK_ACCESS_WHEEL_UPGRADE = registerItem("quick_access_wheel_upgrade", QuickAccessWheelUpgradeItem::new, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<Item> TRASH_CAN_UPGRADE = registerItem("trash_can_upgrade", TrashCanUpgradeItem::new, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> RESCUE_UPGRADE = registerItem("rescue_upgrade", RescueUpgradeItem::new, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<Item> BUILDER_UPGRADE = registerItem("builder_upgrade", BuilderUpgradeItem::new, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> TORCH_PLACER_UPGRADE = registerItem("torch_placer_upgrade", TorchPlacerUpgradeItem::new, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> CAPACITY_WARNING_UPGRADE = registerItem("capacity_warning_upgrade", CapacityWarningUpgradeItem::new, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> ITEM_LOCK_UPGRADE = registerItem("item_lock_upgrade", ItemLockUpgradeItem::new, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<Item> BACKPACK_LINK_UPGRADE = registerItem("backpack_link_upgrade", BackpackLinkUpgradeItem::new, new Item.Properties().rarity(Rarity.EPIC));
    public static final DeferredItem<Item> LINK_CRYSTAL = registerItem("link_crystal", LinkCrystalItem::new, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<Item> HOPPER_UPGRADE = registerItem("hopper_upgrade", HopperUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> NESTED_STORAGE_UPGRADE = registerItem("nested_storage_upgrade", NestedStorageUpgradeItem::new, new Item.Properties().rarity(Rarity.EPIC));
    public static final DeferredItem<Item> AUTO_TOOL_UPGRADE = registerItem("auto_tool_upgrade", AutoToolUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> AUTO_FEED_UPGRADE = registerItem("auto_feed_upgrade", AutoFeedUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> SURVIVAL_ASSIST_UPGRADE = registerItem("survival_assist_upgrade", SurvivalAssistUpgradeItem::new, new Item.Properties());
public static final DeferredItem<Item> LIGHT_UPGRADE = registerItem("light_upgrade", LightUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> NIGHT_VISION_UPGRADE = registerItem("night_vision_upgrade", NightVisionUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> FLIGHT_UPGRADE = registerItem("flight_upgrade", FlightUpgradeItem::new, new Item.Properties().rarity(Rarity.EPIC));
    public static final DeferredItem<Item> REPAIR_UPGRADE = registerItem("repair_upgrade", RepairUpgradeItem::new, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<Item> FALL_PROTECTION_UPGRADE = registerItem("fall_protection_upgrade", FallProtectionUpgradeItem::new, new Item.Properties().durability(32).rarity(Rarity.RARE));
    public static final DeferredItem<Item> DEATH_EMERGENCY_KIT_UPGRADE = registerItem("death_emergency_kit_upgrade", DeathEmergencyKitUpgradeItem::new, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<Item> WIRELESS_UPGRADE = registerItem("wireless_upgrade", WirelessUpgradeItem::new, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> SOULBOUND_UPGRADE = registerItem("soulbound_upgrade", SoulboundUpgradeItem::new, new Item.Properties().rarity(Rarity.EPIC));
    public static final DeferredItem<Item> FILTER_UPGRADE = registerItem("filter_upgrade", FilterUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> COURIER_UPGRADE = registerItem("courier_upgrade", CourierUpgradeItem::new, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<Item> FLUID_STORAGE_UPGRADE = registerItem("fluid_storage_upgrade", FluidStorageUpgradeItem::new, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> FLUID_TRANSFER_UPGRADE = registerItem("fluid_transfer_upgrade", FluidTransferUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> CAPACITOR_UPGRADE = registerItem("capacitor_upgrade", CapacitorUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> XP_TRANSFER_UPGRADE = registerItem("xp_transfer_upgrade", XpTransferUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> RESTOCK_UPGRADE = registerItem("restock_upgrade", RestockUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> DEPOSIT_UPGRADE = registerItem("deposit_upgrade", DepositUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> CHUNK_LOADER_UPGRADE = registerItem("chunk_loader_upgrade", ChunkLoaderUpgradeItem::new, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<Item> CRAFTING_TABLE_UPGRADE = registerItem("crafting_table_upgrade", CraftingTableUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> ENDER_CHEST_UPGRADE = registerItem("ender_chest_upgrade", EnderChestUpgradeItem::new, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<Item> ENCHANTING_TABLE_UPGRADE = registerItem("enchanting_table_upgrade", EnchantingTableUpgradeItem::new, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<Item> CARTOGRAPHY_TABLE_UPGRADE = registerItem("cartography_table_upgrade", CartographyTableUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> SMITHING_TABLE_UPGRADE = registerItem("smithing_table_upgrade", SmithingTableUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> GRINDSTONE_UPGRADE = registerItem("grindstone_upgrade", GrindstoneUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> LOOM_UPGRADE = registerItem("loom_upgrade", LoomUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> STONECUTTER_UPGRADE = registerItem("stonecutter_upgrade", StonecutterUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> ANVIL_UPGRADE = registerItem("anvil_upgrade", AnvilUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> FURNACE_UPGRADE = registerItem("furnace_upgrade", FurnaceUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> AUTO_SMELTING_UPGRADE = registerItem("auto_smelting_upgrade", AutoSmeltingUpgradeItem::new, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> COMPRESSION_UPGRADE = registerItem("compression_upgrade", CompressionUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> STORAGE_UPGRADE_I = registerStorageUpgrade("storage_upgrade_i", "tooltip.smartbackpacks.storage_upgrade_i", new Item.Properties());
    public static final DeferredItem<Item> STORAGE_UPGRADE_II = registerStorageUpgrade("storage_upgrade_ii", "tooltip.smartbackpacks.storage_upgrade_ii", new Item.Properties());
    public static final DeferredItem<Item> STORAGE_UPGRADE_III = registerStorageUpgrade("storage_upgrade_iii", "tooltip.smartbackpacks.storage_upgrade_iii", new Item.Properties());
    public static final DeferredItem<Item> STORAGE_UPGRADE_IV = registerStorageUpgrade("storage_upgrade_iv", "tooltip.smartbackpacks.storage_upgrade_iv", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> STORAGE_UPGRADE_V = registerStorageUpgrade("storage_upgrade_v", "tooltip.smartbackpacks.storage_upgrade_v", new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<Item> ULTIMATE_STORAGE_UPGRADE = registerStorageUpgrade("ultimate_storage_upgrade", "tooltip.smartbackpacks.ultimate_storage_upgrade", new Item.Properties().rarity(Rarity.EPIC));
    public static final DeferredItem<Item> BLAST_FURNACE_UPGRADE = registerItem("blast_furnace_upgrade", BlastFurnaceUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> SMOKER_UPGRADE = registerItem("smoker_upgrade", SmokerUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> BREWING_STAND_UPGRADE = registerItem("brewing_stand_upgrade", BrewingStandUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> JUKEBOX_UPGRADE = registerItem("jukebox_upgrade", JukeboxUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> VOID_UPGRADE = registerItem("void_upgrade", VoidUpgradeItem::new, new Item.Properties());
    public static final DeferredItem<Item> VOIDBOUND_UPGRADE = registerItem("voidbound_upgrade", VoidboundUpgradeItem::new, new Item.Properties().rarity(Rarity.EPIC));

    private ModItems() {
    }

    private static Item.Properties fireproofBackpackProperties() {
        return new Item.Properties().fireResistant();
    }

    private static DeferredItem<Item> registerBackpack(String name, BackpackTier tier, Item.Properties properties) {
        return registerItem(name, props -> new BackpackItem(tier, props), properties);
    }

    private static DeferredItem<Item> registerStorageUpgrade(String name, String tooltipKey, Item.Properties properties) {
        return registerItem(name, props -> new StorageUpgradeItem(tooltipKey, props), properties);
    }

    private static DeferredItem<Item> registerBlockItem(String name,
                                                        java.util.function.Supplier<? extends net.minecraft.world.level.block.Block> block) {
        return registerItem(name, properties -> new BlockItem(block.get(), properties), new Item.Properties());
    }

    private static DeferredItem<Item> registerItem(String name, MagnetItemFactory factory, boolean advanced, Item.Properties properties) {
        return registerItem(name, props -> factory.create(advanced, props), properties);
    }

    private static DeferredItem<Item> registerItem(String name, java.util.function.Function<Item.Properties, ? extends Item> factory, Item.Properties properties) {
        return ITEMS.register(name, () -> factory.apply(properties));
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    @FunctionalInterface
    private interface MagnetItemFactory {
        Item create(boolean advanced, Item.Properties properties);
    }
}
