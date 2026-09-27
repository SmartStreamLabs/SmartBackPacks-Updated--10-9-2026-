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
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

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
        BlockEntity blockEntity = level.getBlockEntity(pos);
        IItemHandler handler = Capabilities.ItemHandler.BLOCK.getCapability(
                level, pos, level.getBlockState(pos), blockEntity, side);
        return handler == null ? null : new ExternalInventoryAccess(
                level, pos, side, level.getBlockState(pos).getBlock(), blockEntity);
    }

    public boolean isStillValid() {
        return this.level.hasChunkAt(this.pos)
                && this.level.getBlockState(this.pos).getBlock() == this.block
                && this.level.getBlockEntity(this.pos) == this.blockEntity
                && this.handler() != null;
    }

    public List<ItemStack> contents() {
        IItemHandler handler = this.handler();
        if (handler == null) {
            return List.of();
        }
        List<ItemStack> contents = new ArrayList<>(handler.getSlots());
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            contents.add(handler.getStackInSlot(slot).copy());
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
        IItemHandler handler = this.handler();
        if (handler == null || stack.isEmpty()) {
            return 0;
        }
        ItemStack remainder = ItemHandlerHelper.insertItemStacked(handler, stack.copy(), simulate);
        return Math.max(0, stack.getCount() - remainder.getCount());
    }

    private ItemStack extract(ItemStack template, int maximumAmount, boolean simulate) {
        IItemHandler handler = this.handler();
        if (handler == null || template.isEmpty() || maximumAmount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack result = ItemStack.EMPTY;
        int remaining = maximumAmount;
        for (int slot = 0; slot < handler.getSlots() && remaining > 0; slot++) {
            ItemStack stored = handler.getStackInSlot(slot);
            if (stored.isEmpty() || !ItemStack.isSameItemSameComponents(stored, template)) {
                continue;
            }
            ItemStack extracted = handler.extractItem(slot, remaining, simulate);
            if (extracted.isEmpty() || !ItemStack.isSameItemSameComponents(extracted, template)) {
                continue;
            }
            if (result.isEmpty()) {
                result = extracted.copy();
            } else {
                result.grow(extracted.getCount());
            }
            remaining -= extracted.getCount();
        }
        return result;
    }

    private IItemHandler handler() {
        return Capabilities.ItemHandler.BLOCK.getCapability(
                this.level, this.pos, this.level.getBlockState(this.pos),
                this.level.getBlockEntity(this.pos), this.side);
    }
}
