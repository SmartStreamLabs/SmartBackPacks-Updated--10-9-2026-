package com.teamsmartstreamlabs.smartbackpacks.item;

import java.util.List;
import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackHelper;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.ChunkLoaderUpgradeData;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import com.teamsmartstreamlabs.smartbackpacks.util.PlayerMessageHelper;

public class ChunkLoaderUpgradeItem extends BackpackUpgradeItem {
    public ChunkLoaderUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            cycleRadius(player, stack);
        }
        return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER).heldItemTransformedTo(stack);
    }

    public static void cycleRadius(Player player, ItemStack stack) {
        ChunkLoaderUpgradeData nextData = stack
                .getOrDefault(ModDataComponents.CHUNK_LOADER_UPGRADE_DATA.get(), ChunkLoaderUpgradeData.DEFAULT)
                .nextRadius();
        stack.set(ModDataComponents.CHUNK_LOADER_UPGRADE_DATA.get(), nextData);
        PlayerMessageHelper.sendStatus(player, Component.translatable(
                "message.smartbackpacks.chunk_loader_radius",
                nextData.radius(),
                nextData.loadedChunkCount()).withStyle(ChatFormatting.GREEN));
    }

    @Override
    public boolean onInstalledRightClicked(ServerPlayer player, BackpackAccess access, int upgradeSlot, ItemStack stack) {
        if (upgradeSlot < 0) {
            return false;
        }

        ItemStack backpack = access.getBackpackStack(player);
        if (backpack.isEmpty()) {
            return false;
        }

        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        if (upgradeSlot >= upgrades.size()) {
            return false;
        }

        ItemStack installedUpgrade = upgrades.get(upgradeSlot);
        if (!(installedUpgrade.getItem() instanceof ChunkLoaderUpgradeItem)) {
            return false;
        }

        BackpackHelper.openChunkLoaderUpgrade(player, access, upgradeSlot);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.chunk_loader_upgrade").withStyle(ChatFormatting.GRAY));
        ChunkLoaderUpgradeData data = stack.getOrDefault(ModDataComponents.CHUNK_LOADER_UPGRADE_DATA.get(), ChunkLoaderUpgradeData.DEFAULT);
        tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.chunk_loader_radius",
                data.radius(), data.loadedChunkCount()).withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.chunk_loader_radius_cycle").withStyle(ChatFormatting.DARK_GRAY));
    }
}
