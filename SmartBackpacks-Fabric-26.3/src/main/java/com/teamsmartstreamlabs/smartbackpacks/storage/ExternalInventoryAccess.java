package com.teamsmartstreamlabs.smartbackpacks.storage;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

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
        if (!level.hasChunkAt(pos) || ItemStorage.SIDED.find(level, pos, side) == null) {
            return null;
        }
        return new ExternalInventoryAccess(level, pos, side,
                level.getBlockState(pos).getBlock(), level.getBlockEntity(pos));
    }

    public boolean isStillValid() {
        return this.level.hasChunkAt(this.pos)
                && this.level.getBlockState(this.pos).getBlock() == this.block
                && this.level.getBlockEntity(this.pos) == this.blockEntity
                && this.storage() != null;
    }

    public List<ItemStack> contents() {
        Storage<ItemVariant> storage = this.storage();
        if (storage == null) {
            return List.of();
        }
        List<ItemStack> contents = new ArrayList<>();
        for (StorageView<ItemVariant> view : storage) {
            ItemVariant resource = view.getResource();
            if (!resource.isBlank() && view.getAmount() > 0L) {
                contents.add(resource.toStack((int) Math.min(Integer.MAX_VALUE, view.getAmount())));
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
        Storage<ItemVariant> storage = this.storage();
        if (storage == null || stack.isEmpty()) {
            return 0;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
            if (!simulate) {
                transaction.commit();
            }
            return (int) Math.min(Integer.MAX_VALUE, inserted);
        }
    }

    private ItemStack extract(ItemStack template, int maximumAmount, boolean simulate) {
        Storage<ItemVariant> storage = this.storage();
        if (storage == null || template.isEmpty() || maximumAmount <= 0) {
            return ItemStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            long extracted = storage.extract(ItemVariant.of(template), maximumAmount, transaction);
            if (!simulate) {
                transaction.commit();
            }
            return extracted <= 0L
                    ? ItemStack.EMPTY
                    : template.copyWithCount((int) Math.min(Integer.MAX_VALUE, extracted));
        }
    }

    private Storage<ItemVariant> storage() {
        return ItemStorage.SIDED.find(this.level, this.pos, this.side);
    }
}
