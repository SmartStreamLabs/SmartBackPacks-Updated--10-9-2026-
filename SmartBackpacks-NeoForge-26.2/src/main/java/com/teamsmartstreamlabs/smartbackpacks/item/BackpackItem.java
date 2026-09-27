package com.teamsmartstreamlabs.smartbackpacks.item;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackContentPreview;

import java.util.List;
import java.util.function.Consumer;



import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackCraftingHandler;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackHelper;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.ItemContainerContentsHelper;

public class BackpackItem extends Item {
    private static BooleanSupplier previewShiftDown = () -> false;

    public static void setPreviewShiftDown(BooleanSupplier shiftDown) {
        previewShiftDown = shiftDown;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return previewShiftDown.getAsBoolean()
                ? Optional.of(BackpackContentPreview.capture(stack, this.tier))
                : super.getTooltipImage(stack);
    }
    private final BackpackTier tier;

    public BackpackItem(BackpackTier tier, Properties properties) {
        super(properties.stacksTo(1));
        this.tier = tier;
    }

    public BackpackTier getTier() {
        return this.tier;
    }

    @Override
    public void onCraftedBy(ItemStack stack, Player player) {
        super.onCraftedBy(stack, player);
        BackpackCraftingHandler.preserveUpgradeData(stack, player);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return BackpackStackData.hasSoulboundUpgrade(stack) || super.isFoil(stack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (!slotChanged && oldStack.getItem() == newStack.getItem()) {
            return false;
        }

        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.CHEST;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
            }

            if (this.tryEquipToChest(player, stack) || this.tryEquipToBack(player, stack)) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 0.8F, 1.0F);
                return InteractionResult.CONSUME.heldItemTransformedTo(stack);
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            BackpackAccess access = BackpackAccess.fromHand(player, hand, this.tier);
            BackpackHelper.openBackpack(serverPlayer, access);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 0.7F, 1.0F);
            return InteractionResult.CONSUME.heldItemTransformedTo(stack);
        }

        return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        long filledStacks = BackpackContentPreview.capture(stack, this.tier).usedSlots();
        tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.slot_count", this.tier.getSlotCount()).withStyle(ChatFormatting.GRAY));
        tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.contents_count", filledStacks).withStyle(ChatFormatting.DARK_GRAY));
        if (!previewShiftDown.getAsBoolean()) {
            tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.preview_hint").withStyle(ChatFormatting.DARK_GRAY));
        }
        if (CuriosCompat.isAvailable()) {
            tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.curios_equip").withStyle(ChatFormatting.GRAY));
        }
    }

    public static boolean isBackpack(ItemStack stack) {
        return stack.getItem() instanceof BackpackItem;
    }

    public static void copyStorageAndAppearance(ItemStack source, ItemStack target) {
        ItemContainerContents contents = source.get(DataComponents.CONTAINER);
        if (contents != null) {
            target.set(DataComponents.CONTAINER, contents);
        }
        var overflow = source.get(ModDataComponents.BACKPACK_STORAGE_OVERFLOW.get());
        if (overflow != null) {
            target.set(ModDataComponents.BACKPACK_STORAGE_OVERFLOW.get(), overflow);
        } else {
            target.remove(ModDataComponents.BACKPACK_STORAGE_OVERFLOW.get());
        }

        DyedItemColor dyedColor = source.get(DataComponents.DYED_COLOR);
        if (dyedColor != null) {
            target.set(DataComponents.DYED_COLOR, dyedColor);
        } else {
            target.remove(DataComponents.DYED_COLOR);
        }

        var upgrades = source.get(ModDataComponents.BACKPACK_UPGRADES.get());
        if (upgrades != null) {
            target.set(ModDataComponents.BACKPACK_UPGRADES.get(), upgrades);
        } else {
            target.remove(ModDataComponents.BACKPACK_UPGRADES.get());
        }

        var linkData = source.get(ModDataComponents.BACKPACK_LINK_DATA.get());
        if (linkData != null) {
            target.set(ModDataComponents.BACKPACK_LINK_DATA.get(), linkData);
        } else {
            target.remove(ModDataComponents.BACKPACK_LINK_DATA.get());
        }
    }

    private boolean tryEquipToChest(Player player, ItemStack handStack) {
        if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            return false;
        }

        ItemStack singleBackpack = handStack.copyWithCount(1);
        player.setItemSlot(EquipmentSlot.CHEST, singleBackpack);
        handStack.shrink(1);
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
            return true;
        }
        return false;
    }
}



