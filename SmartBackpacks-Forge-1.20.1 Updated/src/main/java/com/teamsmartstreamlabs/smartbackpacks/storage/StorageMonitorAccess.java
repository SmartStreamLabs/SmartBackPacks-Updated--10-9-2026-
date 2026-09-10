package com.teamsmartstreamlabs.smartbackpacks.storage;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModBlocks;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class StorageMonitorAccess {
    private StorageMonitorAccess() {
    }

    public static Result resolve(ServerPlayer player, StorageMonitorLink link) {
        if (link == null) {
            return new Result(Status.NOT_LINKED, null);
        }
        if (!SmartBackpacksConfig.storageMonitorEnabled()) {
            return new Result(Status.DISABLED, null);
        }
        if (!SmartBackpacksConfig.storageNetworkEnabled()) {
            return new Result(Status.NETWORK_OFFLINE, null);
        }
        if (!SmartBackpacksConfig.storageMonitorCrossDimensionAccess()
                && !player.level().dimension().equals(link.dimension())) {
            return new Result(Status.CROSS_DIMENSION_DISABLED, null);
        }

        ServerLevel targetLevel = player.getServer().getLevel(link.dimension());
        if (targetLevel == null || !targetLevel.hasChunkAt(link.controllerPos())) {
            return new Result(Status.TARGET_UNLOADED, null);
        }
        if (!targetLevel.getBlockState(link.controllerPos()).is(ModBlocks.STORAGE_CONTROLLER.get())) {
            return new Result(Status.CONTROLLER_MISSING, null);
        }
        return new Result(Status.AVAILABLE, targetLevel);
    }

    public enum Status {
        AVAILABLE,
        DISABLED,
        NOT_LINKED,
        NETWORK_OFFLINE,
        CROSS_DIMENSION_DISABLED,
        TARGET_UNLOADED,
        CONTROLLER_MISSING
    }

    public record Result(Status status, ServerLevel targetLevel) {
        public boolean available() {
            return this.status == Status.AVAILABLE && this.targetLevel != null;
        }
    }
}
