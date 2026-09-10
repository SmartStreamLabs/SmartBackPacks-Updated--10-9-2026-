package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public record TrashCanUpgradeData(
        boolean enabled,
        ItemContainerContents pendingItem,
        ItemContainerContents lastDeletedItem,
        int remainingTicks) {
    public static final int DEFAULT_DELAY_TICKS = 100;
    public static final TrashCanUpgradeData DEFAULT = new TrashCanUpgradeData(
            true,
            ItemContainerContents.EMPTY,
            ItemContainerContents.EMPTY,
            DEFAULT_DELAY_TICKS);

    public static final Codec<TrashCanUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(TrashCanUpgradeData::enabled),
            ItemContainerContents.CODEC.optionalFieldOf("pending_item", ItemContainerContents.EMPTY).forGetter(TrashCanUpgradeData::pendingItem),
            ItemContainerContents.CODEC.optionalFieldOf("last_deleted_item", ItemContainerContents.EMPTY).forGetter(TrashCanUpgradeData::lastDeletedItem),
            Codec.INT.optionalFieldOf("remaining_ticks", DEFAULT_DELAY_TICKS).forGetter(TrashCanUpgradeData::remainingTicks)
    ).apply(instance, TrashCanUpgradeData::new));

    public TrashCanUpgradeData {
        remainingTicks = Math.max(0, remainingTicks);
    }

    public ItemStack loadPendingItem() {
        return this.loadSingle(this.pendingItem);
    }

    public ItemStack loadLastDeletedItem() {
        return this.loadSingle(this.lastDeletedItem);
    }

    public boolean hasPendingItem() {
        return !this.loadPendingItem().isEmpty();
    }

    public boolean hasLastDeletedItem() {
        return !this.loadLastDeletedItem().isEmpty();
    }

    public TrashCanUpgradeData withEnabled(boolean enabled) {
        return new TrashCanUpgradeData(enabled, this.pendingItem, this.lastDeletedItem, this.remainingTicks);
    }

    public TrashCanUpgradeData withPendingItem(ItemStack stack, int delayTicks) {
        return new TrashCanUpgradeData(this.enabled, stackContents(stack), this.lastDeletedItem, Math.max(0, delayTicks));
    }

    public TrashCanUpgradeData withRemainingTicks(int remainingTicks) {
        return new TrashCanUpgradeData(this.enabled, this.pendingItem, this.lastDeletedItem, remainingTicks);
    }

    public TrashCanUpgradeData clearPendingItem() {
        return new TrashCanUpgradeData(this.enabled, ItemContainerContents.EMPTY, this.lastDeletedItem, DEFAULT_DELAY_TICKS);
    }

    public TrashCanUpgradeData withLastDeletedItem(ItemStack stack) {
        return new TrashCanUpgradeData(this.enabled, this.pendingItem, stackContents(stack), this.remainingTicks);
    }

    public TrashCanUpgradeData clearLastDeletedItem() {
        return new TrashCanUpgradeData(this.enabled, this.pendingItem, ItemContainerContents.EMPTY, this.remainingTicks);
    }

    private ItemStack loadSingle(ItemContainerContents contents) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(1, ItemStack.EMPTY);
        contents.copyInto(stacks);
        return stacks.getFirst().copy();
    }

    private static ItemContainerContents stackContents(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemContainerContents.EMPTY;
        }

        NonNullList<ItemStack> stacks = NonNullList.withSize(1, stack.copy());
        return ItemContainerContents.fromItems(stacks);
    }
}
