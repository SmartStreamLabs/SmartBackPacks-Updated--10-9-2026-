package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.AutoToolUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

public final class AutoToolUpgradeHandler {
    private static final String[] WRENCH_KEYWORDS = {"wrench", "spanner", "hammer"};

    private AutoToolUpgradeHandler() {
    }

    public static void trySwapBestTool(ServerPlayer player, BlockPos blockPos, boolean manualRequest) {
        if (player.level().isClientSide()) {
            return;
        }

        BlockState state = player.level().getBlockState(blockPos);
        if (state.isAir()) {
            return;
        }

        ToolBackpack toolBackpack = findToolBackpack(player);
        if (toolBackpack == null) {
            return;
        }

        int selectedSlot = player.getInventory().getSelectedSlot();
        ItemStack currentMainHand = player.getInventory().getItem(selectedSlot);
        if (BackpackItem.isBackpack(currentMainHand)) {
            return;
        }

        NonNullList<ItemStack> storage = BackpackStackData.loadStorage(toolBackpack.backpack(), toolBackpack.tier());
        int bestSlot = -1;
        int bestScore = Integer.MIN_VALUE;

        for (int slot = 0; slot < storage.size(); slot++) {
            ItemStack candidate = storage.get(slot);
            if (candidate.isEmpty()
                    || !AutoToolFilterMatcher.allows(toolBackpack.data(), candidate)
                    || !ItemLockProtection.canConsumeForUpgrade(toolBackpack.backpack(), slot, candidate)) {
                continue;
            }

            int score = getToolScore(player, state, blockPos, candidate, manualRequest);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = slot;
            }
        }

        if (bestSlot < 0 || bestScore <= getToolScore(player, state, blockPos, currentMainHand, manualRequest)) {
            return;
        }
        if (!currentMainHand.isEmpty()
                && !ItemLockProtection.canAutomationInsert(toolBackpack.backpack(), bestSlot, storage.get(bestSlot), currentMainHand)) {
            return;
        }

        ItemStack bestTool = storage.get(bestSlot).copy();
        storage.set(bestSlot, currentMainHand.copy());
        player.getInventory().setItem(selectedSlot, bestTool);
        BackpackStackData.saveStorage(toolBackpack.backpack(), storage);
        toolBackpack.saver().accept(toolBackpack.backpack());
        player.getInventory().setChanged();
        if (!manualRequest) {
            com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.add(player, "auto_tool_swaps", 1);
        }
    }

    private static ToolBackpack findToolBackpack(ServerPlayer player) {
        for (int slot = 0; slot < 36; slot++) {
            int inventorySlot = slot;
            ToolBackpack toolBackpack = createToolBackpack(player.getInventory().getItem(slot), backpack -> {
                player.getInventory().setItem(inventorySlot, backpack);
                player.getInventory().setChanged();
            });
            if (toolBackpack != null) {
                return toolBackpack;
            }
        }

        ToolBackpack offhand = createToolBackpack(player.getOffhandItem(), backpack -> player.getInventory().setChanged());
        if (offhand != null) {
            return offhand;
        }

        ToolBackpack chest = createToolBackpack(player.getItemBySlot(EquipmentSlot.CHEST), backpack -> player.setItemSlot(EquipmentSlot.CHEST, backpack));
        if (chest != null) {
            return chest;
        }

        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            int curioSlot = slot;
            ToolBackpack toolBackpack = createToolBackpack(CuriosCompat.getBackStack(player, curioSlot),
                    backpack -> CuriosCompat.setBackStack(player, curioSlot, backpack));
            if (toolBackpack != null) {
                return toolBackpack;
            }
        }

        return null;
    }

    private static ToolBackpack createToolBackpack(ItemStack stack, Consumer<ItemStack> saver) {
        if (!(stack.getItem() instanceof BackpackItem backpackItem)) {
            return null;
        }

        for (ItemStack upgradeStack : BackpackStackData.loadUpgrades(stack)) {
            if (!(upgradeStack.getItem() instanceof AutoToolUpgradeItem)) {
                continue;
            }

            AutoToolUpgradeData data = upgradeStack.getOrDefault(ModDataComponents.AUTO_TOOL_UPGRADE_DATA.get(), AutoToolUpgradeData.DEFAULT);
            if (data.enabled()) {
                return new ToolBackpack(stack, backpackItem.getTier(), data, saver);
            }
        }

        return null;
    }

    private static int getToolScore(ServerPlayer player, BlockState state, BlockPos pos, ItemStack stack, boolean manualRequest) {
        if (stack.isEmpty()) {
            return Integer.MIN_VALUE;
        }

        double destroySpeed = stack.getDestroySpeed(state);
        boolean correctTool = stack.isCorrectToolForDrops(state);
        boolean wrenchLike = isWrenchLike(stack);
        boolean wrenchRelevant = manualRequest && isWrenchTarget(player.level().getBlockEntity(pos));

        int score = (int) Math.round(destroySpeed * 100.0D);
        if (correctTool) {
            score += 10_000;
        }
        if (wrenchLike && wrenchRelevant) {
            score += 12_000;
        } else if (wrenchLike && manualRequest) {
            score += 3_000;
        }

        int efficiency = EnchantmentHelper.getItemEnchantmentLevel(player.registryAccess().getOrThrow(Enchantments.EFFICIENCY), stack);
        int silkTouch = EnchantmentHelper.getItemEnchantmentLevel(player.registryAccess().getOrThrow(Enchantments.SILK_TOUCH), stack);
        score += efficiency * 250;
        score += silkTouch * 75;

        if (!correctTool && !wrenchLike && destroySpeed <= 1.0D) {
            score -= 8_000;
        }

        return score;
    }

    private static boolean isWrenchTarget(BlockEntity blockEntity) {
        return blockEntity != null;
    }

    private static boolean isWrenchLike(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        for (String keyword : WRENCH_KEYWORDS) {
            if (path.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private record ToolBackpack(ItemStack backpack, BackpackTier tier, AutoToolUpgradeData data, Consumer<ItemStack> saver) {
    }
}
