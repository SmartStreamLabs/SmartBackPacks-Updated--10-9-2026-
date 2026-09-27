package com.teamsmartstreamlabs.smartbackpacks.blockentity;

import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.backpack.isEmpty()) tag.put("StoredBackpack", this.backpack.save(registries));
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.backpack = tag.contains("StoredBackpack", Tag.TAG_COMPOUND)
                ? ItemStack.parseOptional(registries, tag.getCompound("StoredBackpack")) : ItemStack.EMPTY;
        if (this.backpack.isEmpty() && tag.contains("DisplayBackpack", Tag.TAG_COMPOUND)) {
            this.backpack = ItemStack.parseOptional(registries, tag.getCompound("DisplayBackpack"));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        // NeoForge ignores empty block-entity update tags, so send an explicit empty state.
        tag.putBoolean("HasDisplayBackpack", !this.backpack.isEmpty());
        if (!this.backpack.isEmpty()) {
            ItemStack display = new ItemStack(this.backpack.getItem());
            var color = this.backpack.get(DataComponents.DYED_COLOR);
            if (color != null) display.set(DataComponents.DYED_COLOR, color);
            tag.put("DisplayBackpack", display.save(registries));
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
