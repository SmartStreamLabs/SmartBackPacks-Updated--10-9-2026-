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
        int remainingTicks,
        int pendingExtraCount,
        int lastDeletedExtraCount) {
    public static final int DEFAULT_DELAY_TICKS = 100;
    public static final TrashCanUpgradeData DEFAULT = new TrashCanUpgradeData(
            true,
            ItemContainerContents.EMPTY,
            ItemContainerContents.EMPTY,
            DEFAULT_DELAY_TICKS,
            0,
            0);

    public static final Codec<TrashCanUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(TrashCanUpgradeData::enabled),
            ItemContainerContents.CODEC.optionalFieldOf("pending_item", ItemContainerContents.EMPTY).forGetter(TrashCanUpgradeData::pendingItem),
            ItemContainerContents.CODEC.optionalFieldOf("last_deleted_item", ItemContainerContents.EMPTY).forGetter(TrashCanUpgradeData::lastDeletedItem),
            Codec.INT.optionalFieldOf("remaining_ticks", DEFAULT_DELAY_TICKS).forGetter(TrashCanUpgradeData::remainingTicks),
            Codec.INT.optionalFieldOf("pending_extra_count", 0).forGetter(TrashCanUpgradeData::pendingExtraCount),
            Codec.INT.optionalFieldOf("last_deleted_extra_count", 0).forGetter(TrashCanUpgradeData::lastDeletedExtraCount)
    ).apply(instance, TrashCanUpgradeData::new));

    public TrashCanUpgradeData {
        remainingTicks = Math.max(0, remainingTicks);
        pendingExtraCount = Math.max(0, pendingExtraCount);
        lastDeletedExtraCount = Math.max(0, lastDeletedExtraCount);
    }

    public ItemStack loadPendingItem() {
        return this.loadSingle(this.pendingItem, this.pendingExtraCount);
    }

    public ItemStack loadLastDeletedItem() {
        return this.loadSingle(this.lastDeletedItem, this.lastDeletedExtraCount);
    }

    public boolean hasPendingItem() {
        return !this.loadPendingItem().isEmpty();
    }

    public boolean hasLastDeletedItem() {
        return !this.loadLastDeletedItem().isEmpty();
    }

    public TrashCanUpgradeData withEnabled(boolean enabled) {
        return new TrashCanUpgradeData(enabled, this.pendingItem, this.lastDeletedItem, this.remainingTicks,
                this.pendingExtraCount, this.lastDeletedExtraCount);
    }

    public TrashCanUpgradeData withPendingItem(ItemStack stack, int delayTicks) {
        return new TrashCanUpgradeData(this.enabled, stackContents(stack), this.lastDeletedItem, Math.max(0, delayTicks),
                extraCount(stack), this.lastDeletedExtraCount);
    }

    public TrashCanUpgradeData withRemainingTicks(int remainingTicks) {
        return new TrashCanUpgradeData(this.enabled, this.pendingItem, this.lastDeletedItem, remainingTicks,
                this.pendingExtraCount, this.lastDeletedExtraCount);
    }

    public TrashCanUpgradeData clearPendingItem() {
        return new TrashCanUpgradeData(this.enabled, ItemContainerContents.EMPTY, this.lastDeletedItem, DEFAULT_DELAY_TICKS,
                0, this.lastDeletedExtraCount);
    }

    public TrashCanUpgradeData withLastDeletedItem(ItemStack stack) {
        return new TrashCanUpgradeData(this.enabled, this.pendingItem, stackContents(stack), this.remainingTicks,
                this.pendingExtraCount, extraCount(stack));
    }

    public TrashCanUpgradeData clearLastDeletedItem() {
        return new TrashCanUpgradeData(this.enabled, this.pendingItem, ItemContainerContents.EMPTY, this.remainingTicks,
                this.pendingExtraCount, 0);
    }

    private ItemStack loadSingle(ItemContainerContents contents, int extraCount) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(1, ItemStack.EMPTY);
        contents.copyInto(stacks);
        ItemStack stack = stacks.getFirst().copy();
        if (!stack.isEmpty() && extraCount > 0) {
            stack.setCount(stack.getCount() + extraCount);
        }
        return stack;
    }

    private static ItemContainerContents stackContents(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemContainerContents.EMPTY;
        }

        NonNullList<ItemStack> stacks = NonNullList.withSize(1, stack.copyWithCount(serializableCount(stack)));
        return ItemContainerContents.fromItems(stacks);
    }

    private static int extraCount(ItemStack stack) {
        return stack.isEmpty() ? 0 : Math.max(0, stack.getCount() - serializableCount(stack));
    }

    private static int serializableCount(ItemStack stack) {
        return Math.max(1, Math.min(stack.getCount(), stack.getMaxStackSize()));
    }
}
