package com.teamsmartstreamlabs.smartbackpacks.backpack;

import com.teamsmartstreamlabs.smartbackpacks.blockentity.PlacedBackpackBlockEntity;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.ItemStack;

public record BackpackAccess(Source source, int slotIndex, BlockPos blockPos, BackpackTier tier) {
    public static final int OFFHAND_SLOT = 40;
    public static final String CURIO_BACK_SLOT = "back";

    public enum Source {
        MAIN_HAND,
        INVENTORY,
        OFF_HAND,
        CHEST,
        CURIO_BACK,
        BLOCK
    }

    public static BackpackAccess fromHand(Player player, InteractionHand hand, BackpackTier tier) {
        return hand == InteractionHand.MAIN_HAND
                ? new BackpackAccess(Source.MAIN_HAND, player.getInventory().selected, BlockPos.ZERO, tier)
                : new BackpackAccess(Source.OFF_HAND, OFFHAND_SLOT, BlockPos.ZERO, tier);
    }

    public static BackpackAccess chest(BackpackTier tier) {
        return new BackpackAccess(Source.CHEST, EquipmentSlot.CHEST.getIndex(), BlockPos.ZERO, tier);
    }

    public static BackpackAccess inventory(int slot, BackpackTier tier) {
        return new BackpackAccess(Source.INVENTORY, slot, BlockPos.ZERO, tier);
    }

    public static BackpackAccess curioBack(int index, BackpackTier tier) {
        return new BackpackAccess(Source.CURIO_BACK, index, BlockPos.ZERO, tier);
    }

    public static BackpackAccess block(BlockPos pos, BackpackTier tier) {
        return new BackpackAccess(Source.BLOCK, -1, pos, tier);
    }

    public static BackpackAccess fromNetwork(Player player, RegistryFriendlyByteBuf buffer) {
        return new BackpackAccess(
                buffer.readEnum(Source.class),
                buffer.readVarInt(),
                buffer.readBlockPos(),
                BackpackTier.byIndex(buffer.readVarInt())
        );
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeEnum(this.source);
        buffer.writeVarInt(this.slotIndex);
        buffer.writeBlockPos(this.blockPos);
        buffer.writeVarInt(this.tier.ordinal());
    }

    public ItemStack getBackpackStack(Player player) {
        return switch (this.source) {
            case MAIN_HAND, INVENTORY, OFF_HAND -> player.getInventory().getItem(this.slotIndex);
            case CHEST -> player.getItemBySlot(EquipmentSlot.CHEST);
            case CURIO_BACK -> CuriosCompat.getBackStack(player, this.slotIndex);
            case BLOCK -> this.getPlacedBlockEntity(player) != null ? this.getPlacedBlockEntity(player).getStoredBackpack() : ItemStack.EMPTY;
        };
    }

    public void setBackpackStack(Player player, ItemStack stack) {
        switch (this.source) {
            case MAIN_HAND, INVENTORY, OFF_HAND -> player.getInventory().setItem(this.slotIndex, stack);
            case CHEST -> player.setItemSlot(EquipmentSlot.CHEST, stack);
            case CURIO_BACK -> CuriosCompat.setBackStack(player, this.slotIndex, stack);
            case BLOCK -> {
                PlacedBackpackBlockEntity blockEntity = this.getPlacedBlockEntity(player);
                if (blockEntity != null) {
                    blockEntity.setStoredBackpack(stack);
                }
            }
        }
    }

    public boolean isStillValid(Player player) {
        ItemStack stack = this.getBackpackStack(player);
        return stack.getItem() instanceof com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem backpackItem
                && backpackItem.getTier() == this.tier;
    }

    public int getLockedInventorySlot() {
        return switch (this.source) {
            case MAIN_HAND, INVENTORY, OFF_HAND -> this.slotIndex;
            case CHEST, CURIO_BACK, BLOCK -> -1;
        };
    }

    private PlacedBackpackBlockEntity getPlacedBlockEntity(Player player) {
        BlockEntity blockEntity = player.level().getBlockEntity(this.blockPos);
        return blockEntity instanceof PlacedBackpackBlockEntity placedBackpackBlockEntity ? placedBackpackBlockEntity : null;
    }
}
