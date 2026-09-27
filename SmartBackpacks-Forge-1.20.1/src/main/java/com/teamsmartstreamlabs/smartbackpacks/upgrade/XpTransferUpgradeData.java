package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record XpTransferUpgradeData(int storedMillibuckets) {
    public static final XpTransferUpgradeData DEFAULT = new XpTransferUpgradeData(0);

    public static final Codec<XpTransferUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("stored_millibuckets", 0).forGetter(XpTransferUpgradeData::storedMillibuckets)
    ).apply(instance, XpTransferUpgradeData::new));

    public XpTransferUpgradeData {
        storedMillibuckets = Math.max(0, storedMillibuckets);
    }
}
