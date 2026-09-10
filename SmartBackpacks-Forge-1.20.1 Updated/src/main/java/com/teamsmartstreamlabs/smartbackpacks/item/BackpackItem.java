package com.teamsmartstreamlabs.smartbackpacks.item;

import java.util.List;



import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackCraftingHandler;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackHelper;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.block.BackpackBlock;
import com.teamsmartstreamlabs.smartbackpacks.blockentity.PlacedBackpackBlockEntity;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModBlocks;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.ChatFormatting;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.core.component.DataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.context.UseOnContext;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.component.DyedItemColor;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.ItemContainerContentsHelper;

public class BackpackItem extends Item implements DyeableLeatherItem {
    private final BackpackTier tier;

    public BackpackItem(BackpackTier tier, Properties properties) {
        super(properties.stacksTo(1));
        this.tier = tier;
    }

    public BackpackTier getTier() {
        return this.tier;
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        BackpackCraftingHandler.preserveUpgradeData(stack, player);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return BackpackStackData.hasSoulboundUpgrade(stack) || super.isFoil(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (level.isClientSide()) {
                return InteractionResultHolder.success(stack);
            }

            if (this.tryEquipToChest(player, stack) || this.tryEquipToBack(player, stack)) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 0.8F, 1.0F);
                return InteractionResultHolder.consume(stack);
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            BackpackAccess access = BackpackAccess.fromHand(player, hand, this.tier);
            BackpackHelper.openBackpack(serverPlayer, access);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 0.7F, 1.0F);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        long filledStacks = ItemContainerContentsHelper.nonEmptyStream(ItemStackDataComponents.getOrDefault(stack, DataComponents.CONTAINER, ItemContainerContents.EMPTY)).count();
        tooltipComponents.add(Component.translatable("tooltip.smartbackpacks.slot_count", this.tier.getSlotCount()).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.smartbackpacks.contents_count", filledStacks).withStyle(ChatFormatting.DARK_GRAY));
        if (CuriosCompat.isAvailable()) {
            tooltipComponents.add(Component.translatable("tooltip.smartbackpacks.curios_equip").withStyle(ChatFormatting.GRAY));
        }
    }

    public static boolean isBackpack(ItemStack stack) {
        return stack.getItem() instanceof BackpackItem;
    }

    public static void copyStorageAndAppearance(ItemStack source, ItemStack target) {
        ItemContainerContents contents = ItemStackDataComponents.get(source, DataComponents.CONTAINER);
        if (contents != null) {
            ItemStackDataComponents.set(target, DataComponents.CONTAINER, contents);
        }
        var overflow = ItemStackDataComponents.get(source, ModDataComponents.BACKPACK_STORAGE_OVERFLOW.get());
        if (overflow != null) {
            ItemStackDataComponents.set(target, ModDataComponents.BACKPACK_STORAGE_OVERFLOW.get(), overflow);
        } else {
            ItemStackDataComponents.remove(target, ModDataComponents.BACKPACK_STORAGE_OVERFLOW.get());
        }

        DyedItemColor dyedColor = ItemStackDataComponents.get(source, DataComponents.DYED_COLOR);
        if (dyedColor != null) {
            ItemStackDataComponents.set(target, DataComponents.DYED_COLOR, dyedColor);
        } else {
            ItemStackDataComponents.remove(target, DataComponents.DYED_COLOR);
        }

        var upgrades = ItemStackDataComponents.get(source, ModDataComponents.BACKPACK_UPGRADES.get());
        if (upgrades != null) {
            ItemStackDataComponents.set(target, ModDataComponents.BACKPACK_UPGRADES.get(), upgrades);
        } else {
            ItemStackDataComponents.remove(target, ModDataComponents.BACKPACK_UPGRADES.get());
        }

        var linkData = ItemStackDataComponents.get(source, ModDataComponents.BACKPACK_LINK_DATA.get());
        if (linkData != null) {
            ItemStackDataComponents.set(target, ModDataComponents.BACKPACK_LINK_DATA.get(), linkData);
        } else {
            ItemStackDataComponents.remove(target, ModDataComponents.BACKPACK_LINK_DATA.get());
        }
    }

    private boolean tryEquipToChest(Player player, ItemStack handStack) {
        if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            return false;
        }

        ItemStack singleBackpack = handStack.copyWithCount(1);
        player.setItemSlot(EquipmentSlot.CHEST, singleBackpack);
        handStack.shrink(1);
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        return true;
    }

    private boolean tryEquipToBack(Player player, ItemStack handStack) {
        if (!SmartBackpacksConfig.canWearBackpackOnBack()
                || !SmartBackpacksConfig.allowBackpackInCuriosSlot()
                || !CuriosCompat.isAvailable()) {
            return false;
        }

        ItemStack singleBackpack = handStack.copyWithCount(1);
        if (CuriosCompat.insertIntoFirstEmptyBackSlot(player, singleBackpack)) {
            handStack.shrink(1);
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
            return true;
        }
        return false;
    }

    private Block getPlacedBlock() {
        return switch (this.tier) {
            case LEATHER -> ModBlocks.LEATHER_BACKPACK.get();
            case COAL -> ModBlocks.COAL_BACKPACK.get();
            case LAPIS -> ModBlocks.LAPIS_BACKPACK.get();
            case REDSTONE -> ModBlocks.REDSTONE_BACKPACK.get();
            case QUARTZ -> ModBlocks.QUARTZ_BACKPACK.get();
            case COPPER -> ModBlocks.COPPER_BACKPACK.get();
            case IRON -> ModBlocks.IRON_BACKPACK.get();
            case GOLD -> ModBlocks.GOLD_BACKPACK.get();
            case EMERALD -> ModBlocks.EMERALD_BACKPACK.get();
            case DIAMOND -> ModBlocks.DIAMOND_BACKPACK.get();
            case NETHERITE -> ModBlocks.NETHERITE_BACKPACK.get();
            case ANCIENT_NETHERITE -> ModBlocks.ANCIENT_NETHERITE_BACKPACK.get();
            case ULTIMATE_DIAMOND -> ModBlocks.ULTIMATE_DIAMOND_BACKPACK.get();
            case NETHERITE_VAULT -> ModBlocks.NETHERITE_VAULT_BACKPACK.get();
        };
    }
}




