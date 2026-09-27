package com.teamsmartstreamlabs.smartbackpacks.backpack;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BackpackStackDataCountRegressionTest {
    public static void main(String[] args) {
        BackpackStackDataCountRegressionTest test = new BackpackStackDataCountRegressionTest();
        test.normalStackRoundTripsWithoutLoss();
        test.itemSpecificSerializedLimitsAreRespected();
        test.multipleAndEmptySlotsRemainIndependent();
        test.missingAndMalformedOverflowAreSafe();
        test.maximumLogicalStackDoesNotOverflow();
        test.saveLoadSaveProducesTheSameRepresentation();
        test.unstackableItemsUseEveryStorageUpgradeTier();
        test.unstackableLogicalStacksRoundTripThroughOverflow();
        System.out.println("All BackpackStackData count regression tests passed.");
    }

    @Test
    void normalStackRoundTripsWithoutLoss() {
        assertRoundTrip(64, 64, 612, 64, 0);
        assertRoundTrip(99, 64, 612, 64, 35);
        assertRoundTrip(256, 64, 612, 64, 192);
        assertRoundTrip(612, 64, 612, 64, 548);
    }

    @Test
    void itemSpecificSerializedLimitsAreRespected() {
        assertRoundTrip(80, 16, 612, 16, 64);
        assertRoundTrip(1, 1, 1, 1, 0);
        assertRoundTrip(384, 128, 612, 128, 256);
    }

    @Test
    void multipleAndEmptySlotsRemainIndependent() {
        int[] counts = {99, 0, 256, 16};
        int[] itemLimits = {64, 64, 64, 16};
        int[] storageLimits = {612, 612, 612, 612};

        for (int slot = 0; slot < counts.length; slot++) {
            BackpackStackData.StoredCounts stored = BackpackStackData.splitStorageCount(
                    counts[slot], storageLimits[slot], itemLimits[slot]);
            assertEquals(counts[slot], BackpackStackData.restoreStorageCount(
                    stored.serialized(), stored.overflow(), storageLimits[slot]), "slot " + slot);
        }
    }

    @Test
    void missingAndMalformedOverflowAreSafe() {
        assertEquals(64, BackpackStackData.restoreStorageCount(64, 0, 612));
        assertEquals(64, BackpackStackData.restoreStorageCount(64, -500, 612));
        assertEquals(0, BackpackStackData.restoreStorageCount(-5, -500, 612));
    }

    @Test
    void maximumLogicalStackDoesNotOverflow() {
        BackpackStackData.StoredCounts stored = BackpackStackData.splitStorageCount(
                Integer.MAX_VALUE, Integer.MAX_VALUE, 64);

        assertEquals(64, stored.serialized());
        assertEquals(Integer.MAX_VALUE - 64, stored.overflow());
        assertEquals(Integer.MAX_VALUE, BackpackStackData.restoreStorageCount(
                stored.serialized(), stored.overflow(), Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, BackpackStackData.restoreStorageCount(
                64, Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Test
    void saveLoadSaveProducesTheSameRepresentation() {
        BackpackStackData.StoredCounts first = BackpackStackData.splitStorageCount(612, 612, 64);
        int restored = BackpackStackData.restoreStorageCount(first.serialized(), first.overflow(), 612);
        BackpackStackData.StoredCounts second = BackpackStackData.splitStorageCount(restored, 612, 64);

        assertEquals(first, second);
    }

    @Test
    void unstackableItemsUseEveryStorageUpgradeTier() {
        assertEquals(1, BackpackStackData.getStorageStackLimit(1, 0));
        assertEquals(96, BackpackStackData.getStorageStackLimit(1, 1));
        assertEquals(256, BackpackStackData.getStorageStackLimit(1, 2));
        assertEquals(356, BackpackStackData.getStorageStackLimit(1, 3));
        assertEquals(612, BackpackStackData.getStorageStackLimit(1, 4));
        assertEquals(2024, BackpackStackData.getStorageStackLimit(1, 5));
        assertEquals(Integer.MAX_VALUE, BackpackStackData.getStorageStackLimit(1, 6));
    }

    @Test
    void unstackableLogicalStacksRoundTripThroughOverflow() {
        int[] storageLimits = {96, 256, 356, 612, 2024};
        for (int storageLimit : storageLimits) {
            assertRoundTrip(storageLimit, 1, storageLimit, 1, storageLimit - 1);
        }
        assertRoundTrip(4096, 1, Integer.MAX_VALUE, 1, 4095);
    }

    private static void assertRoundTrip(int logicalCount, int itemLimit, int storageLimit,
            int expectedSerialized, int expectedOverflow) {
        BackpackStackData.StoredCounts stored = BackpackStackData.splitStorageCount(
                logicalCount, storageLimit, itemLimit);

        assertEquals(expectedSerialized, stored.serialized());
        assertEquals(expectedOverflow, stored.overflow());
        assertEquals(logicalCount, BackpackStackData.restoreStorageCount(
                stored.serialized(), stored.overflow(), storageLimit));
    }
}
