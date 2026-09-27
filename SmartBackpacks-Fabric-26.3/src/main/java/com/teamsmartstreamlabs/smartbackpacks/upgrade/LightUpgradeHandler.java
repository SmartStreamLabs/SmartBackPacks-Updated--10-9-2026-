package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.LightUpgradeItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class LightUpgradeHandler {
    private static final int TICK_INTERVAL = 2;
    private static final Map<UUID, LightAnchor> ACTIVE_LIGHTS = new HashMap<>();
    private static final Map<DimensionPos, Integer> LIGHT_REFS = new HashMap<>();

    private LightUpgradeHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || player.tickCount % TICK_INTERVAL != 0) {
            return;
        }

        BlockPos targetPos = findDesiredLightPos(player);
        if (!hasLightUpgrade(player) || targetPos == null) {
            clearPlayerLight(player);
            return;
        }

        LightAnchor currentAnchor = ACTIVE_LIGHTS.get(player.getUUID());
        if (currentAnchor != null && currentAnchor.matches(player.level().dimension(), targetPos)) {
            return;
        }

        clearPlayerLight(player);
        placePlayerLight(player, targetPos);
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clearPlayerLight(player);
        }
    }

    private static boolean hasLightUpgrade(ServerPlayer player) {
        for (int slot = 0; slot < 36; slot++) {
            if (hasLightUpgrade(player.getInventory().getItem(slot))) {
                return true;
            }
        }

        if (hasLightUpgrade(player.getOffhandItem()) || hasLightUpgrade(player.getItemBySlot(EquipmentSlot.CHEST))) {
            return true;
        }

        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            if (hasLightUpgrade(CuriosCompat.getBackStack(player, slot))) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasLightUpgrade(ItemStack stack) {
        if (!(stack.getItem() instanceof BackpackItem)) {
            return false;
        }

        return BackpackStackData.loadUpgrades(stack).stream()
                .anyMatch(upgrade -> upgrade.getItem() instanceof LightUpgradeItem);
    }

    private static BlockPos findDesiredLightPos(ServerPlayer player) {
        BlockPos[] candidates = new BlockPos[] {
                player.blockPosition().above(),
                player.blockPosition(),
                player.blockPosition().above(2)
        };

        for (BlockPos candidate : candidates) {
            BlockState state = player.level().getBlockState(candidate);
            if (state.isAir() || state.is(Blocks.LIGHT)) {
                return candidate;
            }
        }

        return null;
    }

    private static void placePlayerLight(ServerPlayer player, BlockPos pos) {
        DimensionPos dimensionPos = new DimensionPos(player.level().dimension(), pos.immutable());
        int refs = LIGHT_REFS.getOrDefault(dimensionPos, 0);
        if (refs == 0) {
            player.level().setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15), 3);
        }
        LIGHT_REFS.put(dimensionPos, refs + 1);
        ACTIVE_LIGHTS.put(player.getUUID(), new LightAnchor(player.level().dimension(), pos.immutable()));
    }

    private static void clearPlayerLight(ServerPlayer player) {
        LightAnchor anchor = ACTIVE_LIGHTS.remove(player.getUUID());
        if (anchor == null) {
            return;
        }

        DimensionPos dimensionPos = new DimensionPos(anchor.dimension(), anchor.pos());
        Integer refs = LIGHT_REFS.get(dimensionPos);
        if (refs == null) {
            return;
        }

        if (refs <= 1) {
            LIGHT_REFS.remove(dimensionPos);
            Level level = player.level().getServer().getLevel(anchor.dimension());
            if (level != null && level.getBlockState(anchor.pos()).is(Blocks.LIGHT)) {
                level.removeBlock(anchor.pos(), false);
            }
        } else {
            LIGHT_REFS.put(dimensionPos, refs - 1);
        }
    }

    private record LightAnchor(ResourceKey<Level> dimension, BlockPos pos) {
        private boolean matches(ResourceKey<Level> otherDimension, BlockPos otherPos) {
            return this.dimension == otherDimension && this.pos.equals(otherPos);
        }
    }

    private record DimensionPos(ResourceKey<Level> dimension, BlockPos pos) {
    }
}
