package com.teamsmartstreamlabs.smartbackpacks.backpack;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.menu.JukeboxUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.BackpackLinkUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.BackpackMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.BuilderUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.CapacitorUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.CapacityWarningUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.ChunkLoaderUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.FluidStorageUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.FluidTransferUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.MagnetUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.PortableAnvilMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.PortableAutoSmeltingMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.PortableBlastFurnaceMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.PortableBrewingStandMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.PortableCartographyTableMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.PortableCraftingMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.PortableEnchantingTableMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.PortableFurnaceMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.PortableGrindstoneMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.PortableLoomMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.PortableSmokerMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.PortableSmithingTableMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.PortableStonecutterMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.QuiverUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.QuickAccessWheelUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.RescueUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.AutoFeedUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.SurvivalAssistUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.TorchPlacerUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.StorageControllerMenu;
import com.teamsmartstreamlabs.smartbackpacks.menu.XpTransferUpgradeMenu;
import com.teamsmartstreamlabs.smartbackpacks.blockentity.PlacedBackpackBlockEntity;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.WirelessUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.storage.StorageMonitorLink;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

public final class BackpackHelper {
    private BackpackHelper() {
    }

    public static void openBackpack(ServerPlayer player, BackpackAccess access) {
        openExtendedMenu(player, Component.translatable(access.tier().getTranslationKey()),
                (containerId, inventory) -> new BackpackMenu(containerId, inventory, access),
                (registryBuf, serverPlayer) -> access.write(registryBuf));
    }

    public static void openStorageController(ServerPlayer player, BlockPos controllerPos) {
        openExtendedMenu(player, Component.translatable("block.smartbackpacks.storage_controller"),
                (containerId, inventory) -> new StorageControllerMenu(containerId, inventory, controllerPos),
                (registryBuf, serverPlayer) -> StorageControllerMenu.writeContext(registryBuf, false, controllerPos,
                        new StorageMonitorLink(player.level().dimension(), controllerPos)));
        if (player.containerMenu instanceof StorageControllerMenu menu) {
            menu.refreshAndSync(player);
        }
    }

    public static void openStorageMonitor(ServerPlayer player, StorageMonitorLink link) {
        BlockPos accessPos = player.blockPosition();
        openExtendedMenu(player, Component.translatable("item.smartbackpacks.storage_monitor"),
                (containerId, inventory) -> new StorageControllerMenu(containerId, inventory, accessPos, link),
                (registryBuf, serverPlayer) -> StorageControllerMenu.writeContext(registryBuf, true, accessPos, link));
        if (player.containerMenu instanceof StorageControllerMenu menu) {
            menu.refreshAndSync(player);
        }
    }

    public static void openMagnetUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openUpgradeMenu(player, Component.translatable("screen.smartbackpacks.magnet.title"),
                (containerId, inventory) -> new MagnetUpgradeMenu(containerId, inventory, access, upgradeSlot),
                access, upgradeSlot);
    }

    public static void openPickupUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openUpgradeMenu(player, Component.translatable("item.smartbackpacks.pickup_upgrade"),
                (containerId, inventory) -> new MagnetUpgradeMenu(containerId, inventory, access, upgradeSlot),
                access, upgradeSlot);
    }

    public static void openHopperUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openUpgradeMenu(player, Component.translatable("item.smartbackpacks.hopper_upgrade"),
                (containerId, inventory) -> new MagnetUpgradeMenu(containerId, inventory, access, upgradeSlot),
                access, upgradeSlot);
    }

    public static void openAutoToolUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openUpgradeMenu(player, Component.translatable("item.smartbackpacks.auto_tool_upgrade"),
                (containerId, inventory) -> new MagnetUpgradeMenu(containerId, inventory, access, upgradeSlot),
                access, upgradeSlot);
    }

    public static void openSurvivalAssistUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openUpgradeMenu(player, Component.translatable("item.smartbackpacks.survival_assist_upgrade"),
                (containerId, inventory) -> new SurvivalAssistUpgradeMenu(containerId, inventory, access, upgradeSlot),
                access, upgradeSlot);
    }

    public static void openAutoFeedUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openUpgradeMenu(player, Component.translatable("item.smartbackpacks.auto_feed_upgrade"),
                (containerId, inventory) -> new AutoFeedUpgradeMenu(containerId, inventory, access, upgradeSlot),
                access, upgradeSlot);
    }

    public static void openFilterUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openUpgradeMenu(player, Component.translatable("item.smartbackpacks.filter_upgrade"),
                (containerId, inventory) -> new MagnetUpgradeMenu(containerId, inventory, access, upgradeSlot),
                access, upgradeSlot);
    }

    public static void openFluidStorageUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openUpgradeMenu(player, Component.translatable("item.smartbackpacks.fluid_storage_upgrade"),
                (containerId, inventory) -> new FluidStorageUpgradeMenu(containerId, inventory, access, upgradeSlot),
                access, upgradeSlot);
    }

    public static void openFluidTransferUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openUpgradeMenu(player, Component.translatable("item.smartbackpacks.fluid_transfer_upgrade"),
                (containerId, inventory) -> new FluidTransferUpgradeMenu(containerId, inventory, access, upgradeSlot),
                access, upgradeSlot);
    }

    public static void openCapacitorUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openUpgradeMenu(player, Component.translatable("item.smartbackpacks.capacitor_upgrade"),
                (containerId, inventory) -> new CapacitorUpgradeMenu(containerId, inventory, access, upgradeSlot),
                access, upgradeSlot);
    }

    public static void openXpTransferUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openUpgradeMenu(player, Component.translatable("item.smartbackpacks.xp_transfer_upgrade"),
                (containerId, inventory) -> new XpTransferUpgradeMenu(containerId, inventory, access, upgradeSlot),
                access, upgradeSlot);
    }

    public static void openVoidUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openUpgradeMenu(player, Component.translatable("item.smartbackpacks.void_upgrade"),
                (containerId, inventory) -> new MagnetUpgradeMenu(containerId, inventory, access, upgradeSlot),
                access, upgradeSlot);
    }

    public static void openJukeboxUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openUpgradeMenu(player, Component.translatable("item.smartbackpacks.jukebox_upgrade"),
                (containerId, inventory) -> new JukeboxUpgradeMenu(containerId, inventory, access, upgradeSlot),
                access, upgradeSlot);
    }

    public static void openCourierUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openUpgradeMenu(
                player,
                Component.translatable("item.smartbackpacks.courier_upgrade"),
                (containerId, inventory) -> new MagnetUpgradeMenu(containerId, inventory, access, upgradeSlot),
                access,
                upgradeSlot
        );
    }

    public static void openChunkLoaderUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openUpgradeMenu(player, Component.translatable("item.smartbackpacks.chunk_loader_upgrade"),
                (containerId, inventory) -> new ChunkLoaderUpgradeMenu(containerId, inventory, access, upgradeSlot),
                access, upgradeSlot);
    }

    public static void openQuiverUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openUpgradeMenu(player, Component.translatable("item.smartbackpacks.quiver_upgrade"),
                (containerId, inventory) -> new QuiverUpgradeMenu(containerId, inventory, access, upgradeSlot),
                access, upgradeSlot);
    }

    public static void openQuickAccessWheelUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openUpgradeMenu(player, Component.translatable("item.smartbackpacks.quick_access_wheel_upgrade"),
                (containerId, inventory) -> new QuickAccessWheelUpgradeMenu(containerId, inventory, access, upgradeSlot),
                access, upgradeSlot);
    }

    public static void openRescueUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openUpgradeMenu(player, Component.translatable("item.smartbackpacks.rescue_upgrade"),
                (containerId, inventory) -> new RescueUpgradeMenu(containerId, inventory, access, upgradeSlot),
                access, upgradeSlot);
    }

    public static void openBuilderUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openUpgradeMenu(player, Component.translatable("item.smartbackpacks.builder_upgrade"),
                (containerId, inventory) -> new BuilderUpgradeMenu(containerId, inventory, access, upgradeSlot),
                access, upgradeSlot);
    }

    public static void openTorchPlacerUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openUpgradeMenu(player, Component.translatable("item.smartbackpacks.torch_placer_upgrade"),
                (containerId, inventory) -> new TorchPlacerUpgradeMenu(containerId, inventory, access, upgradeSlot),
                access, upgradeSlot);
    }

    public static void openCapacityWarningUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openUpgradeMenu(player, Component.translatable("item.smartbackpacks.capacity_warning_upgrade"),
                (containerId, inventory) -> new CapacityWarningUpgradeMenu(containerId, inventory, access, upgradeSlot),
                access, upgradeSlot);
    }

    public static void openBackpackLinkUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openExtendedMenu(player, Component.translatable("item.smartbackpacks.backpack_link_upgrade"),
                (containerId, inventory) -> new BackpackLinkUpgradeMenu(containerId, inventory, access, upgradeSlot),
                (registryBuf, serverPlayer) -> {
                    access.write(registryBuf);
                    registryBuf.writeVarInt(upgradeSlot);
                    BackpackLinkUpgradeMenu.writeInitialData(serverPlayer, access, registryBuf);
                });
    }

    public static void openCraftingTableUpgrade(ServerPlayer player) {
        MenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, menuPlayer) -> new PortableCraftingMenu(containerId, inventory),
                Component.translatable("container.crafting")
        );
        player.openMenu(provider);
    }

    public static void openEnchantingTableUpgrade(ServerPlayer player) {
        MenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, menuPlayer) -> new PortableEnchantingTableMenu(containerId, inventory),
                Component.translatable("container.enchant")
        );
        player.openMenu(provider);
    }

    public static void openCartographyTableUpgrade(ServerPlayer player) {
        MenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, menuPlayer) -> new PortableCartographyTableMenu(containerId, inventory),
                Component.translatable("container.cartography_table")
        );
        player.openMenu(provider);
    }

    public static void openSmithingTableUpgrade(ServerPlayer player) {
        MenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, menuPlayer) -> new PortableSmithingTableMenu(containerId, inventory),
                Component.translatable("container.upgrade")
        );
        player.openMenu(provider);
    }

    public static void openGrindstoneUpgrade(ServerPlayer player) {
        MenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, menuPlayer) -> new PortableGrindstoneMenu(containerId, inventory),
                Component.translatable("container.grindstone_title")
        );
        player.openMenu(provider);
    }

    public static void openLoomUpgrade(ServerPlayer player) {
        MenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, menuPlayer) -> new PortableLoomMenu(containerId, inventory),
                Component.translatable("container.loom")
        );
        player.openMenu(provider);
    }

    public static void openStonecutterUpgrade(ServerPlayer player) {
        MenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, menuPlayer) -> new PortableStonecutterMenu(containerId, inventory),
                Component.translatable("container.stonecutter")
        );
        player.openMenu(provider);
    }

    public static void openAnvilUpgrade(ServerPlayer player) {
        MenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, menuPlayer) -> new PortableAnvilMenu(containerId, inventory),
                Component.translatable("container.repair")
        );
        player.openMenu(provider);
    }

    public static void openFurnaceUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openSimpleMenu(player, Component.translatable("container.furnace"),
                (containerId, inventory) -> new PortableFurnaceMenu(containerId, inventory, access, upgradeSlot));
    }

    public static void openAutoSmeltingUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openSimpleMenu(player, Component.translatable("item.smartbackpacks.auto_smelting_upgrade"),
                (containerId, inventory) -> new PortableAutoSmeltingMenu(containerId, inventory, access, upgradeSlot));
    }

    public static void openBlastFurnaceUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openSimpleMenu(player, Component.translatable("container.blast_furnace"),
                (containerId, inventory) -> new PortableBlastFurnaceMenu(containerId, inventory, access, upgradeSlot));
    }

    public static void openSmokerUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openSimpleMenu(player, Component.translatable("container.smoker"),
                (containerId, inventory) -> new PortableSmokerMenu(containerId, inventory, access, upgradeSlot));
    }

    public static void openBrewingStandUpgrade(ServerPlayer player, BackpackAccess access, int upgradeSlot) {
        openSimpleMenu(player, Component.translatable("container.brewing"),
                (containerId, inventory) -> new PortableBrewingStandMenu(containerId, inventory, access, upgradeSlot));
    }

    public static void openEnderChestUpgrade(ServerPlayer player) {
        MenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, menuPlayer) -> ChestMenu.threeRows(containerId, inventory, menuPlayer.getEnderChestInventory()),
                Component.translatable("container.enderchest")
        );
        player.openMenu(provider);
    }

    public static BackpackAccess findWornBackpackAccess(ServerPlayer player) {
        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestStack.getItem() instanceof BackpackItem backpackItem) {
            return BackpackAccess.chest(backpackItem.getTier());
        }

        return CuriosCompat.findFirstMatchingBackStack(player, BackpackItem::isBackpack)
                .map(match -> {
                    BackpackItem backpackItem = (BackpackItem) match.stack().getItem();
                    return BackpackAccess.curioBack(match.slot(), backpackItem.getTier());
                })
                .orElse(null);
    }

    private static void openUpgradeMenu(ServerPlayer player,
                                        Component title,
                                        BiFunction<Integer, Inventory, AbstractContainerMenu> factory,
                                        BackpackAccess access,
                                        int upgradeSlot) {
        openExtendedMenu(player, title, factory, (registryBuf, serverPlayer) -> {
            access.write(registryBuf);
            registryBuf.writeVarInt(upgradeSlot);
        });
    }

    private static void openSimpleMenu(ServerPlayer player,
                                       Component title,
                                       BiFunction<Integer, Inventory, AbstractContainerMenu> factory) {
        MenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, menuPlayer) -> factory.apply(containerId, inventory),
                title
        );
        player.openMenu(provider);
    }

    private static void openExtendedMenu(ServerPlayer player,
                                         Component title,
                                         BiFunction<Integer, Inventory, AbstractContainerMenu> factory,
                                         BiConsumer<RegistryFriendlyByteBuf, ServerPlayer> writer) {
        MenuProvider provider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, net.minecraft.world.entity.player.Player menuPlayer) {
                return factory.apply(containerId, inventory);
            }
        };
        NetworkHooks.openScreen(player, provider, buf -> writer.accept(new RegistryFriendlyByteBuf(buf), player));
    }

    public static BackpackAccess findWirelessBackpackAccess(ServerPlayer player) {
        BackpackAccess wornAccess = findWornBackpackAccess(player);
        if (wornAccess != null && hasWirelessUpgrade(wornAccess.getBackpackStack(player))) {
            return wornAccess;
        }

        return findNearestPlacedWirelessBackpack(player);
    }

    private static boolean hasWirelessUpgrade(ItemStack backpackStack) {
        return BackpackStackData.loadUpgrades(backpackStack).stream()
                .anyMatch(upgrade -> upgrade.getItem() instanceof WirelessUpgradeItem);
    }

    private static BackpackAccess findNearestPlacedWirelessBackpack(ServerPlayer player) {
        final int chunkRadius = 8;
        BlockPos playerPos = player.blockPosition();
        int playerChunkX = playerPos.getX() >> 4;
        int playerChunkZ = playerPos.getZ() >> 4;

        BackpackAccess nearestAccess = null;
        double nearestDistanceSqr = Double.MAX_VALUE;

        for (int chunkX = playerChunkX - chunkRadius; chunkX <= playerChunkX + chunkRadius; chunkX++) {
            for (int chunkZ = playerChunkZ - chunkRadius; chunkZ <= playerChunkZ + chunkRadius; chunkZ++) {
                LevelChunk chunk = ((ServerLevel) player.level()).getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }

                for (var blockEntity : chunk.getBlockEntities().values()) {
                    if (!(blockEntity instanceof PlacedBackpackBlockEntity placedBackpack)) {
                        continue;
                    }

                    ItemStack storedBackpack = placedBackpack.getStoredBackpack();
                    if (!(storedBackpack.getItem() instanceof BackpackItem backpackItem) || !hasWirelessUpgrade(storedBackpack)) {
                        continue;
                    }

                    double distanceSqr = placedBackpack.getBlockPos().distSqr(playerPos);
                    if (distanceSqr >= nearestDistanceSqr) {
                        continue;
                    }

                    nearestDistanceSqr = distanceSqr;
                    nearestAccess = BackpackAccess.block(placedBackpack.getBlockPos(), backpackItem.getTier());
                }
            }
        }

        return nearestAccess;
    }

    public static boolean canOpenWornBackpack(ServerPlayer opener, ServerPlayer wearer) {
        return opener == wearer || SmartBackpacksConfig.allowOtherPlayersToOpenWornBackpacks();
    }
}
