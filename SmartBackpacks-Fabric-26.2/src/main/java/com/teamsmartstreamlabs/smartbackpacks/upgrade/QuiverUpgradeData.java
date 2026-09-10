package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public record QuiverUpgradeData(
        boolean enabled,
        ItemContainerContents projectiles,
        QuiverSelectionMode selectionMode,
        QuiverSourcePriority sourcePriority,
        int preferredSlot,
        List<Integer> prioritySlots) {
    public static final int SLOT_COUNT = 9;
    public static final List<Integer> DEFAULT_PRIORITY = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8);
    public static final QuiverUpgradeData DEFAULT = new QuiverUpgradeData(
            true,
            ItemContainerContents.EMPTY,
            QuiverSelectionMode.SLOT_ORDER,
            QuiverSourcePriority.PLAYER_INVENTORY_FIRST,
            0,
            DEFAULT_PRIORITY);

    public static final Codec<QuiverUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(QuiverUpgradeData::enabled),
            ItemContainerContents.CODEC.optionalFieldOf("projectiles", ItemContainerContents.EMPTY).forGetter(QuiverUpgradeData::projectiles),
            QuiverSelectionMode.CODEC.optionalFieldOf("selection_mode", QuiverSelectionMode.SLOT_ORDER).forGetter(QuiverUpgradeData::selectionMode),
            QuiverSourcePriority.CODEC.optionalFieldOf("source_priority", QuiverSourcePriority.PLAYER_INVENTORY_FIRST).forGetter(QuiverUpgradeData::sourcePriority),
            Codec.INT.optionalFieldOf("preferred_slot", 0).forGetter(QuiverUpgradeData::preferredSlot),
            Codec.INT.listOf().optionalFieldOf("priority_slots", DEFAULT_PRIORITY).forGetter(QuiverUpgradeData::prioritySlots)
    ).apply(instance, QuiverUpgradeData::new));

    public QuiverUpgradeData {
        preferredSlot = clampSlot(preferredSlot);
        prioritySlots = sanitizePriority(prioritySlots);
    }

    public NonNullList<ItemStack> loadProjectiles() {
        NonNullList<ItemStack> stacks = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        this.projectiles.copyInto(stacks);
        return stacks;
    }

    public boolean hasAnyProjectiles() {
        return ItemContainerContentsHelper.nonEmptyStream(this.projectiles).findAny().isPresent();
    }

    public QuiverUpgradeData withProjectiles(List<ItemStack> stacks) {
        return new QuiverUpgradeData(this.enabled, ItemContainerContents.fromItems(stacks),
                this.selectionMode, this.sourcePriority, this.preferredSlot, this.prioritySlots);
    }

    public QuiverUpgradeData withEnabled(boolean enabled) {
        return new QuiverUpgradeData(enabled, this.projectiles, this.selectionMode, this.sourcePriority, this.preferredSlot, this.prioritySlots);
    }

    public QuiverUpgradeData withSelectionMode(QuiverSelectionMode selectionMode) {
        return new QuiverUpgradeData(this.enabled, this.projectiles, selectionMode, this.sourcePriority, this.preferredSlot, this.prioritySlots);
    }

    public QuiverUpgradeData withSourcePriority(QuiverSourcePriority sourcePriority) {
        return new QuiverUpgradeData(this.enabled, this.projectiles, this.selectionMode, sourcePriority, this.preferredSlot, this.prioritySlots);
    }

    public QuiverUpgradeData withPreferredSlot(int preferredSlot) {
        int clamped = clampSlot(preferredSlot);
        List<Integer> priority = new ArrayList<>(this.prioritySlots);
        if (!priority.contains(clamped)) {
            priority.add(0, clamped);
        }
        return new QuiverUpgradeData(this.enabled, this.projectiles, this.selectionMode, this.sourcePriority, clamped, priority);
    }

    public QuiverUpgradeData movePreferredPriority(int delta) {
        int preferred = clampSlot(this.preferredSlot);
        List<Integer> priority = new ArrayList<>(sanitizePriority(this.prioritySlots));
        int index = priority.indexOf(preferred);
        if (index < 0) {
            priority.add(0, preferred);
            index = 0;
        }

        int target = Math.max(0, Math.min(priority.size() - 1, index + delta));
        if (target != index) {
            priority.remove(index);
            priority.add(target, preferred);
        }
        return new QuiverUpgradeData(this.enabled, this.projectiles, this.selectionMode, this.sourcePriority, preferred, priority);
    }

    public QuiverUpgradeData resetSettings() {
        return new QuiverUpgradeData(true, this.projectiles, QuiverSelectionMode.SLOT_ORDER,
                QuiverSourcePriority.PLAYER_INVENTORY_FIRST, 0, DEFAULT_PRIORITY);
    }

    private static int clampSlot(int slot) {
        return Math.max(0, Math.min(SLOT_COUNT - 1, slot));
    }

    private static List<Integer> sanitizePriority(List<Integer> source) {
        List<Integer> sanitized = new ArrayList<>(SLOT_COUNT);
        for (Integer slot : source) {
            if (slot == null || slot < 0 || slot >= SLOT_COUNT || sanitized.contains(slot)) {
                continue;
            }
            sanitized.add(slot);
        }

        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (!sanitized.contains(slot)) {
                sanitized.add(slot);
            }
        }
        return List.copyOf(sanitized);
    }
}
