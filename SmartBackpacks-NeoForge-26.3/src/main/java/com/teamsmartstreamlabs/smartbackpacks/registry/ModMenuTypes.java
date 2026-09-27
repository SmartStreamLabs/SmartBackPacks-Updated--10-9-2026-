package com.teamsmartstreamlabs.smartbackpacks.registry;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.menu.BackpackMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.BackpackLinkUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.AutoFeedUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.BuilderUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.CapacitorUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.CapacityWarningUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.ChunkLoaderUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.FluidStorageUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.FluidTransferUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.JukeboxUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.MagnetUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.PortableAutoSmeltingMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.QuiverUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.QuickAccessWheelUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.RescueUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.DeathEmergencyKitUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.SurvivalAssistUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.TorchPlacerUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.XpTransferUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.StorageControllerMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.StorageTransferMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.BackpackWorkbenchMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenuTypes {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, SmartBackpacks.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<BackpackMenu>> BACKPACK = MENUS.register("backpack", () -> IMenuTypeExtension.create(BackpackMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<MagnetUpgradeMenu>> MAGNET_UPGRADE =
            MENUS.register("magnet_upgrade", () -> IMenuTypeExtension.create(MagnetUpgradeMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AutoFeedUpgradeMenu>> AUTO_FEED_UPGRADE =
            MENUS.register("auto_feed_upgrade", () -> IMenuTypeExtension.create(AutoFeedUpgradeMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<SurvivalAssistUpgradeMenu>> SURVIVAL_ASSIST_UPGRADE =
            MENUS.register("survival_assist_upgrade", () -> IMenuTypeExtension.create(SurvivalAssistUpgradeMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<FluidStorageUpgradeMenu>> FLUID_STORAGE_UPGRADE =
            MENUS.register("fluid_storage_upgrade", () -> IMenuTypeExtension.create(FluidStorageUpgradeMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<FluidTransferUpgradeMenu>> FLUID_TRANSFER_UPGRADE =
            MENUS.register("fluid_transfer_upgrade", () -> IMenuTypeExtension.create(FluidTransferUpgradeMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<CapacitorUpgradeMenu>> CAPACITOR_UPGRADE =
            MENUS.register("capacitor_upgrade", () -> IMenuTypeExtension.create(CapacitorUpgradeMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ChunkLoaderUpgradeMenu>> CHUNK_LOADER_UPGRADE =
            MENUS.register("chunk_loader_upgrade", () -> IMenuTypeExtension.create(ChunkLoaderUpgradeMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<JukeboxUpgradeMenu>> JUKEBOX_UPGRADE =
            MENUS.register("jukebox_upgrade", () -> IMenuTypeExtension.create(JukeboxUpgradeMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<PortableAutoSmeltingMenu>> AUTO_SMELTING_UPGRADE =
            MENUS.register("auto_smelting_upgrade", () -> IMenuTypeExtension.create(PortableAutoSmeltingMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<XpTransferUpgradeMenu>> XP_TRANSFER_UPGRADE =
            MENUS.register("xp_transfer_upgrade", () -> IMenuTypeExtension.create(XpTransferUpgradeMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<QuiverUpgradeMenu>> QUIVER_UPGRADE =
            MENUS.register("quiver_upgrade", () -> IMenuTypeExtension.create(QuiverUpgradeMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<QuickAccessWheelUpgradeMenu>> QUICK_ACCESS_WHEEL_UPGRADE =
            MENUS.register("quick_access_wheel_upgrade", () -> IMenuTypeExtension.create(QuickAccessWheelUpgradeMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<RescueUpgradeMenu>> RESCUE_UPGRADE =
            MENUS.register("rescue_upgrade", () -> IMenuTypeExtension.create(RescueUpgradeMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<DeathEmergencyKitUpgradeMenu>> DEATH_EMERGENCY_KIT_UPGRADE =
            MENUS.register("death_emergency_kit_upgrade", () -> IMenuTypeExtension.create(DeathEmergencyKitUpgradeMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<BuilderUpgradeMenu>> BUILDER_UPGRADE =
            MENUS.register("builder_upgrade", () -> IMenuTypeExtension.create(BuilderUpgradeMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<TorchPlacerUpgradeMenu>> TORCH_PLACER_UPGRADE =
            MENUS.register("torch_placer_upgrade", () -> IMenuTypeExtension.create(TorchPlacerUpgradeMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<CapacityWarningUpgradeMenu>> CAPACITY_WARNING_UPGRADE =
            MENUS.register("capacity_warning_upgrade", () -> IMenuTypeExtension.create(CapacityWarningUpgradeMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<BackpackLinkUpgradeMenu>> BACKPACK_LINK_UPGRADE =
            MENUS.register("backpack_link_upgrade", () -> IMenuTypeExtension.create(BackpackLinkUpgradeMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<StorageControllerMenu>> STORAGE_CONTROLLER =
            MENUS.register("storage_controller", () -> IMenuTypeExtension.create(StorageControllerMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<StorageTransferMenu>> STORAGE_TRANSFER =
            MENUS.register("storage_transfer", () -> IMenuTypeExtension.create(StorageTransferMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<BackpackWorkbenchMenu>> BACKPACK_WORKBENCH =
            MENUS.register("backpack_workbench", () -> IMenuTypeExtension.create(BackpackWorkbenchMenu::new));

    private ModMenuTypes() {
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
