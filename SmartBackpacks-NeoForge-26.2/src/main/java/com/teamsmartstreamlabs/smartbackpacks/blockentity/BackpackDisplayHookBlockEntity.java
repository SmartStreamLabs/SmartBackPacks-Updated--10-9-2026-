package com.teamsmartstreamlabs.smartbackpacks.blockentity;

import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class BackpackDisplayHookBlockEntity extends BlockEntity {
    private ItemStack backpack = ItemStack.EMPTY;

    public BackpackDisplayHookBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BACKPACK_DISPLAY_HOOK.get(), pos, state);
    }

    public ItemStack getStoredBackpack() {
        return this.backpack;
    }

    public void setStoredBackpack(ItemStack stack) {
        if (!stack.isEmpty() && !(stack.getItem() instanceof BackpackItem)) return;
        this.backpack = stack == this.backpack ? stack : stack.copyWithCount(1);
        this.notifyChanged();
    }

    public ItemStack takeBackpack() {
        ItemStack result = this.backpack;
        this.backpack = ItemStack.EMPTY;
        this.notifyChanged();
        return result;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (this.level != null && !this.level.isClientSide()) {
            ItemStack stored = this.takeBackpack();
            if (!stored.isEmpty()) Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), stored);
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.backpack.isEmpty()) output.store("StoredBackpack", ItemStack.CODEC, this.backpack);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.backpack = input.read("StoredBackpack", ItemStack.CODEC)
                .or(() -> input.read("DisplayBackpack", ItemStack.CODEC)).orElse(ItemStack.EMPTY);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        if (!this.backpack.isEmpty()) {
            ItemStack display = new ItemStack(this.backpack.getItem());
            var color = this.backpack.get(DataComponents.DYED_COLOR);
            if (color != null) display.set(DataComponents.DYED_COLOR, color);
            tag.store("DisplayBackpack", ItemStack.CODEC,
                    registries.createSerializationContext(NbtOps.INSTANCE), display);
        }
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void notifyChanged() {
        this.setChanged();
        if (this.level != null) this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }
}

