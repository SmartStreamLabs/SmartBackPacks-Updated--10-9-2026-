package com.teamsmartstreamlabs.smartbackpacks.item;

import java.util.List;
import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackAccess;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class BackpackUpgradeItem extends Item {
    public BackpackUpgradeItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    protected BackpackUpgradeItem(Properties properties, boolean durabilitySetsStackSize) {
        super(durabilitySetsStackSize ? properties : properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        this.appendHoverText(stack, TooltipContext.EMPTY, TooltipDisplay.DEFAULT, tooltipComponents::add, tooltipFlag);
    }

    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
    }

    public boolean onInstalledRightClicked(ServerPlayer player, BackpackAccess access, int upgradeSlot, ItemStack stack) {
        return false;
    }

    public boolean onInstalledRightClicked(ServerPlayer player, BackpackAccess access, int upgradeSlot, ItemStack stack, boolean shiftDown) {
        return this.onInstalledRightClicked(player, access, upgradeSlot, stack);
    }

    public static final class TooltipContext {
        private static final TooltipContext EMPTY = new TooltipContext();

        private TooltipContext() {
        }
    }
}

