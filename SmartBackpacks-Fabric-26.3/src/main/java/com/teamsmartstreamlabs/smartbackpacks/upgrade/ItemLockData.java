package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ItemLockData(
        boolean slotLocking,
        boolean exactItemLocking,
        boolean typeLocking,
        boolean manualAccessAllowed,
        boolean automationAccessAllowed,
        List<Integer> lockedSlots,
        List<String> lockedItemFingerprints,
        List<String> lockedItemTypes) {
    public static final ItemLockData DEFAULT = new ItemLockData(true, true, false, false, false, List.of(), List.of(), List.of());

    public static final Codec<ItemLockData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("slot_locking", true).forGetter(ItemLockData::slotLocking),
            Codec.BOOL.optionalFieldOf("exact_item_locking", true).forGetter(ItemLockData::exactItemLocking),
            Codec.BOOL.optionalFieldOf("type_locking", false).forGetter(ItemLockData::typeLocking),
            Codec.BOOL.optionalFieldOf("manual_access_allowed", false).forGetter(ItemLockData::manualAccessAllowed),
            Codec.BOOL.optionalFieldOf("automation_access_allowed", false).forGetter(ItemLockData::automationAccessAllowed),
            Codec.INT.listOf().optionalFieldOf("locked_slots", List.of()).forGetter(ItemLockData::lockedSlots),
            Codec.STRING.listOf().optionalFieldOf("locked_item_fingerprints", List.of()).forGetter(ItemLockData::lockedItemFingerprints),
            Codec.STRING.listOf().optionalFieldOf("locked_item_types", List.of()).forGetter(ItemLockData::lockedItemTypes)
    ).apply(instance, ItemLockData::new));

    public ItemLockData {
        lockedSlots = sanitizeSlots(lockedSlots);
        lockedItemFingerprints = sanitizeStrings(lockedItemFingerprints);
        lockedItemTypes = sanitizeStrings(lockedItemTypes);
    }

    public boolean hasLockedSlot(int slot) {
        return this.lockedSlots.contains(slot);
    }

    public boolean hasItemFingerprint(String fingerprint) {
        return this.lockedItemFingerprints.contains(fingerprint);
    }

    public boolean hasItemType(String itemType) {
        return this.lockedItemTypes.contains(itemType);
    }

    public ItemLockData toggleSlot(int slot) {
        Set<Integer> slots = new LinkedHashSet<>(this.lockedSlots);
        if (!slots.remove(slot)) {
            slots.add(slot);
        }
        return new ItemLockData(this.slotLocking, this.exactItemLocking, this.typeLocking, this.manualAccessAllowed,
                this.automationAccessAllowed, List.copyOf(slots), this.lockedItemFingerprints, this.lockedItemTypes);
    }

    public ItemLockData toggleItemFingerprint(String fingerprint) {
        Set<String> fingerprints = new LinkedHashSet<>(this.lockedItemFingerprints);
        if (!fingerprints.remove(fingerprint)) {
            fingerprints.add(fingerprint);
        }
        return new ItemLockData(this.slotLocking, this.exactItemLocking, this.typeLocking, this.manualAccessAllowed,
                this.automationAccessAllowed, this.lockedSlots, List.copyOf(fingerprints), this.lockedItemTypes);
    }

    public ItemLockData toggleItemType(String itemType) {
        Set<String> types = new LinkedHashSet<>(this.lockedItemTypes);
        if (!types.remove(itemType)) {
            types.add(itemType);
        }
        return new ItemLockData(this.slotLocking, this.exactItemLocking, this.typeLocking, this.manualAccessAllowed,
                this.automationAccessAllowed, this.lockedSlots, this.lockedItemFingerprints, List.copyOf(types));
    }

    private static List<Integer> sanitizeSlots(List<Integer> slots) {
        return slots.stream()
                .filter(slot -> slot != null && slot >= 0)
                .distinct()
                .toList();
    }

    private static List<String> sanitizeStrings(List<String> values) {
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .toList();
    }
}
