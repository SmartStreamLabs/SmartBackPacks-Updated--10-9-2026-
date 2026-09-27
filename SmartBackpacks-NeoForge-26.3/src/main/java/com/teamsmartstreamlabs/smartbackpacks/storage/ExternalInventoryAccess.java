package com.teamsmartstreamlabs.smartbackpacks.storage;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Loader-specific access to the inventory face physically touching an exporter. */
public final class ExternalInventoryAccess {
    private final ServerLevel level;
    private final BlockPos pos;
    private final Direction side;
    private final Block block;
    private final BlockEntity blockEntity;

    private ExternalInventoryAccess(ServerLevel level, BlockPos pos, Direction side, Block block,
            BlockEntity blockEntity) {
        this.level = level;
        this.pos = pos.immutable();
        this.side = side;
        this.block = block;
        this.blockEntity = blockEntity;
    }

    public static ExternalInventoryAccess find(ServerLevel level, BlockPos pos, Direction side) {
        if (!level.hasChunkAt(pos)) {
            return null;
        }
        ResourceHandler<ItemResource> handler = Capabilities.Item.BLOCK.getCapability(
                level, pos, level.getBlockState(pos), level.getBlockEntity(pos), side);
        return handler == null ? null : new ExternalInventoryAccess(
                level, pos, side, level.getBlockState(pos).getBlock(), level.getBlockEntity(pos));
    }

    public boolean isStillValid() {
        return this.level.hasChunkAt(this.pos)
                && this.level.getBlockState(this.pos).getBlock() == this.block
                && this.level.getBlockEntity(this.pos) == this.blockEntity
                && this.handler() != null;
    }

    public List<ItemStack> contents() {
        ResourceHandler<ItemResource> handler = this.handler();
        if (handler == null) {
            return List.of();
        }
        List<ItemStack> contents = new ArrayList<>(handler.size());
        for (int slot = 0; slot < handler.size(); slot++) {
            ItemResource resource = handler.getResource(slot);
            long amount = handler.getAmountAsLong(slot);
            if (!resource.isEmpty() && amount > 0L) {
                contents.add(resource.toStack((int) Math.min(Integer.MAX_VALUE, amount)));
            }
        }
        return contents;
    }

    public int simulateInsert(ItemStack stack) {
        return this.insert(stack, true);
    }

    public int insert(ItemStack stack) {
        return this.insert(stack, false);
    }

    public ItemStack simulateExtract(ItemStack template, int maximumAmount) {
        return this.extract(template, maximumAmount, true);
    }

    public ItemStack extract(ItemStack template, int maximumAmount) {
        return this.extract(template, maximumAmount, false);
    }

    private int insert(ItemStack stack, boolean simulate) {
        ResourceHandler<ItemResource> handler = this.handler();
        if (handler == null || stack.isEmpty()) {
            return 0;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = handler.insert(ItemResource.of(stack), stack.getCount(), transaction);
            if (!simulate) {
                transaction.commit();
            }
            return inserted;
        }
    }

    private ItemStack extract(ItemStack template, int maximumAmount, boolean simulate) {
        ResourceHandler<ItemResource> handler = this.handler();
        if (handler == null || template.isEmpty() || maximumAmount <= 0) {
            return ItemStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = handler.extract(ItemResource.of(template), maximumAmount, transaction);
            if (!simulate) {
                transaction.commit();
            }
            return extracted <= 0 ? ItemStack.EMPTY : template.copyWithCount(extracted);
        }
    }

    private ResourceHandler<ItemResource> handler() {
        return Capabilities.Item.BLOCK.getCapability(
                this.level, this.pos, this.level.getBlockState(this.pos),
                this.level.getBlockEntity(this.pos), this.side);
    }
}
