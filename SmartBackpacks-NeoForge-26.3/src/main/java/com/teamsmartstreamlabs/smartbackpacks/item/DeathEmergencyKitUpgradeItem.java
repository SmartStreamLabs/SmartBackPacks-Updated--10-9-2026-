package com.teamsmartstreamlabs.smartbackpacks.item;

import java.util.function.Consumer;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class DeathEmergencyKitUpgradeItem extends BackpackUpgradeItem {
    public DeathEmergencyKitUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onInstalledRightClicked(ServerPlayer player, BackpackAccess access, int upgradeSlot, ItemStack stack) {
        BackpackHelper.openDeathEmergencyKitUpgrade(player, access, upgradeSlot);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("tooltip.smartbackpacks.death_emergency_kit_upgrade").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.smartbackpacks.death_emergency_kit_configure").withStyle(ChatFormatting.AQUA));
    }
}
