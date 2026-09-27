package com.teamsmartstreamlabs.smartbackpacks.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record StorageMonitorLink(ResourceKey<Level> dimension, BlockPos controllerPos) {
    public static final Codec<StorageMonitorLink> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(StorageMonitorLink::dimension),
            BlockPos.CODEC.fieldOf("controller_pos").forGetter(StorageMonitorLink::controllerPos)
    ).apply(instance, StorageMonitorLink::new));

    public StorageMonitorLink {
        controllerPos = controllerPos.immutable();
    }
}
