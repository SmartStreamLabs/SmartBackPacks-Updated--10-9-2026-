package com.teamsmartstreamlabs.smartbackpacks.item;

import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.network.PickupNotifierPayload;
import com.teamsmartstreamlabs.smartbackpacks.pickup.PickupNotifierDestination;
import com.teamsmartstreamlabs.smartbackpacks.pickup.PickupNotifierSource;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.network.PacketDistributor;

public class PickupNotifierUpgradeItem extends BackpackUpgradeItem {
    public PickupNotifierUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onInstalledRightClicked(ServerPlayer player, BackpackAccess access, int upgradeSlot, ItemStack stack) {
        ItemStack backpack = access.getBackpackStack(player);
        String backpackName = backpack.isEmpty() ? "Backpack" : backpack.getHoverName().getString();
        PacketDistributor.sendToPlayer(player, new PickupNotifierPayload(
                new ItemStack(Items.COBBLESTONE),
                64,
                -1,
                backpackName,
                PickupNotifierDestination.MAIN_STORAGE,
                PickupNotifierSource.MANUAL_TRANSFER));
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.pickup_notifier_upgrade").withStyle(ChatFormatting.GRAY));
        tooltipComponents.accept(Component.translatable("tooltip.smartbackpacks.pickup_notifier_upgrade_preview").withStyle(ChatFormatting.DARK_GRAY));
    }
}
