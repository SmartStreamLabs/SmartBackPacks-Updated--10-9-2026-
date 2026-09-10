package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.blockentity.PlacedBackpackBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class VoidboundUpgradeHandler {
    private VoidboundUpgradeHandler() {
    }

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ItemEntity itemEntity)) {
            return;
        }

        if (isVoidboundBackpack(itemEntity.getItem())) {
            itemEntity.setUnlimitedLifetime();
        }
    }

    public static void onItemExpire(ItemExpireEvent event) {
        ItemEntity itemEntity = event.getEntity();
        if (isVoidboundBackpack(itemEntity.getItem())) {
            itemEntity.setUnlimitedLifetime();
        }
    }

    public static void onEntityTickPre(EntityTickEvent.Pre event) {
        if (event.getEntity().level().isClientSide() || !(event.getEntity() instanceof ItemEntity itemEntity) || !isVoidboundBackpack(itemEntity.getItem())) {
            return;
        }

        double rescueY = itemEntity.level().dimensionType().minY() + 1.0D;
        if (itemEntity.getY() < rescueY - 32.0D) {
            itemEntity.setPos(itemEntity.getX(), rescueY, itemEntity.getZ());
            itemEntity.setDeltaMovement(0.0D, 0.0D, 0.0D);
        }
    }

    public static void onEntityInvulnerabilityCheck(EntityInvulnerabilityCheckEvent event) {
        if (event.getEntity() instanceof ItemEntity itemEntity && isVoidboundBackpack(itemEntity.getItem())) {
            event.setInvulnerable(true);
        }
    }

    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        event.getAffectedEntities().removeIf(entity -> entity instanceof ItemEntity itemEntity && isVoidboundBackpack(itemEntity.getItem()));
        event.getAffectedBlocks().removeIf(pos -> hasVoidboundPlacedBackpack(event, pos));
    }

    private static boolean hasVoidboundPlacedBackpack(ExplosionEvent.Detonate event, BlockPos pos) {
        if (!(event.getLevel().getBlockEntity(pos) instanceof PlacedBackpackBlockEntity backpackBlockEntity)) {
            return false;
        }

        return isVoidboundBackpack(backpackBlockEntity.getStoredBackpack());
    }

    private static boolean isVoidboundBackpack(ItemStack stack) {
        return BackpackStackData.hasVoidboundUpgrade(stack);
    }
}
