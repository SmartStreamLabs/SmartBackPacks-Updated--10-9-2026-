package com.teamsmartstreamlabs.smartbackpacks.registry;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SmartBackpacks.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.smartbackpacks"))
            .withTabsBefore(CreativeModeTabs.TOOLS_AND_UTILITIES)
            .icon(() -> ModItems.NETHERITE_BACKPACK.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.LEATHER_BACKPACK.get());
                output.accept(ModItems.COAL_BACKPACK.get());
                output.accept(ModItems.LAPIS_BACKPACK.get());
                output.accept(ModItems.REDSTONE_BACKPACK.get());
                output.accept(ModItems.QUARTZ_BACKPACK.get());
                output.accept(ModItems.COPPER_BACKPACK.get());
                output.accept(ModItems.IRON_BACKPACK.get());
                output.accept(ModItems.GOLD_BACKPACK.get());
                output.accept(ModItems.EMERALD_BACKPACK.get());
                output.accept(ModItems.DIAMOND_BACKPACK.get());
                output.accept(ModItems.NETHERITE_BACKPACK.get());
                output.accept(ModItems.ANCIENT_NETHERITE_BACKPACK.get());
                output.accept(ModItems.ULTIMATE_DIAMOND_BACKPACK.get());
                output.accept(ModItems.NETHERITE_VAULT_BACKPACK.get());
                output.accept(ModItems.STORAGE_CONTROLLER.get());
                output.accept(ModItems.STORAGE_CABLE.get());
                output.accept(ModItems.STORAGE_MONITOR.get());
                output.accept(ModItems.MAGNET_UPGRADE.get());
                output.accept(ModItems.ADVANCED_MAGNET_UPGRADE.get());
                output.accept(ModItems.PICKUP_UPGRADE.get());
                output.accept(ModItems.PICKUP_NOTIFIER_UPGRADE.get());
                output.accept(ModItems.QUIVER_UPGRADE.get());
                output.accept(ModItems.QUICK_ACCESS_WHEEL_UPGRADE.get());
                output.accept(ModItems.TRASH_CAN_UPGRADE.get());
                output.accept(ModItems.RESCUE_UPGRADE.get());
                output.accept(ModItems.BUILDER_UPGRADE.get());
                output.accept(ModItems.TORCH_PLACER_UPGRADE.get());
                output.accept(ModItems.CAPACITY_WARNING_UPGRADE.get());
                output.accept(ModItems.ITEM_LOCK_UPGRADE.get());
                output.accept(ModItems.BACKPACK_LINK_UPGRADE.get());
                output.accept(ModItems.LINK_CRYSTAL.get());
                output.accept(ModItems.HOPPER_UPGRADE.get());
                output.accept(ModItems.NESTED_STORAGE_UPGRADE.get());
                output.accept(ModItems.AUTO_TOOL_UPGRADE.get());
                output.accept(ModItems.AUTO_FEED_UPGRADE.get());
                output.accept(ModItems.SURVIVAL_ASSIST_UPGRADE.get());
                output.accept(ModItems.LIGHT_UPGRADE.get());
                output.accept(ModItems.WIRELESS_UPGRADE.get());
                output.accept(ModItems.SOULBOUND_UPGRADE.get());
                output.accept(ModItems.FILTER_UPGRADE.get());
                output.accept(ModItems.COURIER_UPGRADE.get());
                output.accept(ModItems.FLUID_STORAGE_UPGRADE.get());
                output.accept(ModItems.FLUID_TRANSFER_UPGRADE.get());
                output.accept(ModItems.CAPACITOR_UPGRADE.get());
                output.accept(ModItems.XP_TRANSFER_UPGRADE.get());
                output.accept(ModItems.RESTOCK_UPGRADE.get());
                output.accept(ModItems.DEPOSIT_UPGRADE.get());
                output.accept(ModItems.CHUNK_LOADER_UPGRADE.get());
                output.accept(ModItems.CRAFTING_TABLE_UPGRADE.get());
                output.accept(ModItems.ENDER_CHEST_UPGRADE.get());
                output.accept(ModItems.ENCHANTING_TABLE_UPGRADE.get());
                output.accept(ModItems.CARTOGRAPHY_TABLE_UPGRADE.get());
                output.accept(ModItems.SMITHING_TABLE_UPGRADE.get());
                output.accept(ModItems.GRINDSTONE_UPGRADE.get());
                output.accept(ModItems.LOOM_UPGRADE.get());
                output.accept(ModItems.STONECUTTER_UPGRADE.get());
                output.accept(ModItems.ANVIL_UPGRADE.get());
                output.accept(ModItems.FURNACE_UPGRADE.get());
                output.accept(ModItems.AUTO_SMELTING_UPGRADE.get());
                output.accept(ModItems.COMPRESSION_UPGRADE.get());
                output.accept(ModItems.STORAGE_UPGRADE_I.get());
                output.accept(ModItems.STORAGE_UPGRADE_II.get());
                output.accept(ModItems.STORAGE_UPGRADE_III.get());
                output.accept(ModItems.STORAGE_UPGRADE_IV.get());
                output.accept(ModItems.STORAGE_UPGRADE_V.get());
                output.accept(ModItems.ULTIMATE_STORAGE_UPGRADE.get());
                output.accept(ModItems.BLAST_FURNACE_UPGRADE.get());
                output.accept(ModItems.SMOKER_UPGRADE.get());
                output.accept(ModItems.BREWING_STAND_UPGRADE.get());
                output.accept(ModItems.JUKEBOX_UPGRADE.get());
                output.accept(ModItems.VOID_UPGRADE.get());
                output.accept(ModItems.VOIDBOUND_UPGRADE.get());
            })
            .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
