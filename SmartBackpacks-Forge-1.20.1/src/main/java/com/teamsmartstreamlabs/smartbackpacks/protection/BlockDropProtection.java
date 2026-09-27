package com.teamsmartstreamlabs.smartbackpacks.protection;

import java.util.ArrayDeque;
import java.util.List;
import java.util.UUID;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;

public final class BlockDropProtection {
    private static final String TAG_PREFIX = "smartbackpacks.drop_owner.";
    private static final ThreadLocal<ArrayDeque<UUID>> BREAKERS = ThreadLocal.withInitial(ArrayDeque::new);

    private BlockDropProtection() {
    }

    public static void beginBreak(ServerPlayer player) {
        BREAKERS.get().push(player.getUUID());
    }

    public static void endBreak() {
        ArrayDeque<UUID> breakers = BREAKERS.get();
        breakers.pop();
        if (breakers.isEmpty()) {
            BREAKERS.remove();
        }
    }

    public static void markNewDrop(ItemEntity item) {
        if (item.level().isClientSide() || !SmartBackpacksConfig.blockDropMagnetProtectionEnabled()) {
            return;
        }
        UUID owner = BREAKERS.get().peek();
        if (owner != null) {
            int seconds = SmartBackpacksConfig.blockDropMagnetProtectionSeconds();
            long expiresAt = seconds == 0 ? -1L : item.level().getGameTime() + seconds * 20L;
            item.addTag(TAG_PREFIX + owner + ":" + expiresAt);
        }
    }

    /** Other magnet mods can call this on the server before moving or inserting an item entity. */
    public static boolean canMagnetCollect(ServerPlayer player, ItemEntity item) {
        if (!SmartBackpacksConfig.blockDropMagnetProtectionEnabled()) {
            return true;
        }
        String ownerTag = activeOwnerTag(item);
        return ownerTag == null || ownerTag.startsWith(TAG_PREFIX + player.getUUID() + ":");
    }

    public static boolean canMerge(ItemEntity first, ItemEntity second) {
        if (!SmartBackpacksConfig.blockDropMagnetProtectionEnabled()) {
            return true;
        }
        String firstTag = activeOwnerTag(first);
        String secondTag = activeOwnerTag(second);
        return firstTag == null ? secondTag == null : firstTag.equals(secondTag);
    }

    private static String activeOwnerTag(ItemEntity item) {
        for (String tag : List.copyOf(item.getTags())) {
            if (!tag.startsWith(TAG_PREFIX)) {
                continue;
            }
            int separator = tag.lastIndexOf(':');
            if (separator < TAG_PREFIX.length()) {
                return tag;
            }
            try {
                long expiresAt = Long.parseLong(tag.substring(separator + 1));
                if (expiresAt < 0L || item.level().getGameTime() < expiresAt) {
                    return tag;
                }
                item.removeTag(tag);
            } catch (NumberFormatException ignored) {
                return tag;
            }
        }
        return null;
    }
}
