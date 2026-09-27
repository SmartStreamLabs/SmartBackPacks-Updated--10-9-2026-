package com.teamsmartstreamlabs.smartbackpacks.blockentity;

import com.teamsmartstreamlabs.smartbackpacks.upgrade.CourierUpgradeHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.block.BackpackBlock;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.AutoSmeltingUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BlastFurnaceUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BrewingStandUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.CapacitorUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.ChunkLoaderUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.CompressionUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.FilterUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.FluidTransferUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.FluidStorageUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.FurnaceUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.HopperUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.JukeboxUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.MagnetUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.SmokerUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModBlockEntities;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BlastFurnaceUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BlastFurnaceUpgradeLogic;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.AutoSmeltingUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.AutoSmeltingUpgradeLogic;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BrewingStandUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BrewingStandUpgradeLogic;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.ChunkLoaderUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CompressionUpgradeLogic;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.CapacitorUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FilterUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FilterUpgradeMatcher;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FluidTransferUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FluidTransferUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FluidStorageUpgradeHandler;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.SmokerUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.SmokerUpgradeLogic;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.JukeboxUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.MagnetFilterMatcher;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.MagnetUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FurnaceUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.FurnaceUpgradeLogic;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.HopperFilterMatcher;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.HopperUpgradeData;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.ItemLockProtection;
import com.teamsmartstreamlabs.smartbackpacks.upgrade.BackpackLinkManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class PlacedBackpackBlockEntity extends BlockEntity implements WorldlyContainer {
    private static final int[] NO_SLOTS = new int[0];
    private static final Direction[] HOPPER_TRANSFER_DIRECTIONS = {
            Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
    };

    private ItemStack storedBackpack = ItemStack.EMPTY;
    private long lastHopperReceiveGameTime = Long.MIN_VALUE;
    private long lastHopperTransferGameTime = Long.MIN_VALUE;
    private long lastStorageTransferGameTime = Long.MIN_VALUE;
    private int openMenuCount;
    private boolean chunkForced;
    private int forcedChunkRadius;
    private final BackpackAutomationItemHandler itemHandler = new BackpackAutomationItemHandler(null);
    private final BackpackAutomationItemHandler[] sidedHandlers = new BackpackAutomationItemHandler[Direction.values().length];
    private final BackpackFluidHandler fluidHandler = new BackpackFluidHandler();
    private final BackpackFluidHandler[] sidedFluidHandlers = new BackpackFluidHandler[Direction.values().length];
    private final BackpackEnergyStorage energyHandler = new BackpackEnergyStorage();
    private final BackpackEnergyStorage[] sidedEnergyHandlers = new BackpackEnergyStorage[Direction.values().length];

    public PlacedBackpackBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PLACED_BACKPACK.get(), pos, blockState);
        for (Direction direction : Direction.values()) {
            this.sidedHandlers[direction.ordinal()] = new BackpackAutomationItemHandler(direction);
            this.sidedFluidHandlers[direction.ordinal()] = new BackpackFluidHandler();
            this.sidedEnergyHandlers[direction.ordinal()] = new BackpackEnergyStorage();
        }
    }

    public void setStoredBackpack(ItemStack storedBackpack) {
        this.storedBackpack = storedBackpack.copyWithCount(1);
        this.syncChunkForceState();
        this.refreshBackpackLinkAnchor();
        this.notifyInventoryChanged();
    }

    public ItemStack getStoredBackpack() {
        return this.storedBackpack;
    }

    public ItemStack extractStoredBackpack() {
        if (this.storedBackpack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack extracted = this.storedBackpack.copy();
        this.markBackpackLinkAnchorOffline(extracted);
        this.storedBackpack = ItemStack.EMPTY;
        this.syncChunkForceState();
        this.notifyInventoryChanged();
        return extracted;
    }

    public IItemHandlerModifiable getItemHandler() {
        return this.itemHandler;
    }

    public IItemHandlerModifiable getSidedHandler(Direction side) {
        return this.sidedHandlers[side.ordinal()];
    }

    public IFluidHandler getFluidHandler() {
        return this.hasFluidStorageUpgrade() ? this.fluidHandler : null;
    }

    public IFluidHandler getSidedFluidHandler(Direction side) {
        return this.hasFluidStorageUpgrade() ? this.sidedFluidHandlers[side.ordinal()] : null;
    }

    public IEnergyStorage getEnergyStorage() {
        return this.hasCapacitorUpgrade() ? this.energyHandler : null;
    }

    public IEnergyStorage getSidedEnergyStorage(Direction side) {
        return this.hasCapacitorUpgrade() ? this.sidedEnergyHandlers[side.ordinal()] : null;
    }

    public void dropStoredBackpack(Level level, BlockPos pos) {
        ItemStack extracted = this.extractStoredBackpack();
        if (!extracted.isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), extracted);
        }
    }

    public void startOpen() {
        this.openMenuCount++;
    }

    public void stopOpen() {
        if (this.openMenuCount > 0) {
            this.openMenuCount--;
        }
    }

    public boolean isOpen() {
        return this.openMenuCount > 0;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PlacedBackpackBlockEntity blockEntity) {
        if (level.isClientSide() || blockEntity.storedBackpack.isEmpty()) {
            return;
        }

        blockEntity.syncChunkForceState();
        if (level.getGameTime() % 40L == 0L && level instanceof ServerLevel serverLevel) {
            BackpackLinkManager.refreshPlacedAnchor(serverLevel, pos, blockEntity.storedBackpack);
        }

        BackpackTier tier = blockEntity.getBackpackTier();
        if (tier == null) {
            return;
        }

        blockEntity.tickProcessingUpgrades(level);
        if (level instanceof ServerLevel serverLevel) {
            CourierUpgradeHandler.processPlaced(serverLevel, pos, blockEntity.storedBackpack, blockEntity::setStoredBackpack);
        }
        if (blockEntity.isOpen()) {
            return;
        }

        List<InstalledMagnet> magnets = blockEntity.getInstalledMagnets();
        if (magnets.isEmpty()) {
            return;
        }

        int tickInterval = magnets.stream().mapToInt(magnet -> magnet.item().getTickInterval()).min().orElse(5);
        if (level.getGameTime() % tickInterval != 0L) {
            return;
        }

        Vec3 center = Vec3.atCenterOf(pos).add(0.0D, 0.25D, 0.0D);
        int maxRange = magnets.stream().mapToInt(magnet -> magnet.item().getRange()).max().orElse(10);
        AABB range = new AABB(center, center).inflate(maxRange);
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, range, entity -> entity != null && entity.isAlive() && !entity.getItem().isEmpty())) {
            ItemStack entityStack = itemEntity.getItem();
            InstalledMagnet magnet = blockEntity.findMatchingMagnet(itemEntity, tier, magnets, center);
            if (magnet == null) {
                continue;
            }

            blockEntity.pullTowardBackpack(itemEntity, center, magnet.item().getPullStrength());
            if (itemEntity.position().distanceToSqr(center) > magnet.item().getPickupDistanceSqr()) {
                continue;
            }

            ItemStack remainder = ItemLockProtection.insertIntoStorage(blockEntity.storedBackpack, tier, entityStack, false);
            if (remainder.getCount() == entityStack.getCount()) {
                continue;
            }

            blockEntity.notifyInventoryChanged();
            if (remainder.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(remainder);
            }
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, PlacedBackpackBlockEntity blockEntity) {
        if (!level.isClientSide() || blockEntity.storedBackpack.isEmpty()) {
            return;
        }

        var random = level.getRandom();

        if (level.getGameTime() % 10L == 0L && blockEntity.hasPlayingJukeboxUpgrade()) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D;
            double y = pos.getY() + 0.95D + random.nextDouble() * 0.2D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D;
            level.addParticle(ParticleTypes.NOTE, x, y, z, random.nextDouble(), 0.0D, 0.0D);
        }

        if (level.getGameTime() % 4L == 0L && blockEntity.hasActiveFurnaceUpgrade()) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.18D;
            double y = pos.getY() + 0.82D + random.nextDouble() * 0.08D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.18D;
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.03D, 0.0D);
            if (random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.FLAME, x, y - 0.02D, z, 0.0D, 0.01D, 0.0D);
            }
        }

        if (level.getGameTime() % 4L == 0L && blockEntity.hasActiveAutoSmeltingUpgrade()) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.18D;
            double y = pos.getY() + 0.82D + random.nextDouble() * 0.08D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.18D;
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.03D, 0.0D);
            if (random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.FLAME, x, y - 0.02D, z, 0.0D, 0.01D, 0.0D);
            }
        }

        if (level.getGameTime() % 3L == 0L && blockEntity.hasActiveBlastFurnaceUpgrade()) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.18D;
            double y = pos.getY() + 0.82D + random.nextDouble() * 0.08D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.18D;
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.04D, 0.0D);
            level.addParticle(ParticleTypes.SMALL_FLAME, x, y - 0.01D, z, 0.0D, 0.015D, 0.0D);
        }

        if (level.getGameTime() % 4L == 0L && blockEntity.hasActiveSmokerUpgrade()) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.18D;
            double y = pos.getY() + 0.84D + random.nextDouble() * 0.08D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.18D;
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 0.0D, 0.025D, 0.0D);
            if (random.nextInt(4) == 0) {
                level.addParticle(ParticleTypes.SMOKE, x, y - 0.01D, z, 0.0D, 0.01D, 0.0D);
            }
        }

        if (level.getGameTime() % 6L == 0L && blockEntity.hasChunkLoaderUpgrade()) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.18D;
            double y = pos.getY() + 0.86D + random.nextDouble() * 0.14D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.18D;
            double velocityX = (random.nextDouble() - 0.5D) * 0.02D;
            double velocityY = 0.01D + random.nextDouble() * 0.02D;
            double velocityZ = (random.nextDouble() - 0.5D) * 0.02D;
            level.addParticle(ParticleTypes.PORTAL, x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    public boolean hasPlayingJukeboxUpgrade() {
        for (ItemStack upgradeStack : BackpackStackData.loadUpgrades(this.storedBackpack)) {
            if (upgradeStack.getItem() instanceof JukeboxUpgradeItem) {
                JukeboxUpgradeData data = upgradeStack.getOrDefault(ModDataComponents.JUKEBOX_UPGRADE_DATA.get(), JukeboxUpgradeData.DEFAULT);
                if (data.playing()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasActiveFurnaceUpgrade() {
        for (ItemStack upgradeStack : BackpackStackData.loadUpgrades(this.storedBackpack)) {
            if (upgradeStack.getItem() instanceof FurnaceUpgradeItem) {
                FurnaceUpgradeData data = upgradeStack.getOrDefault(ModDataComponents.FURNACE_UPGRADE_DATA.get(), FurnaceUpgradeData.DEFAULT);
                if (data.litTime() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasActiveAutoSmeltingUpgrade() {
        for (ItemStack upgradeStack : BackpackStackData.loadUpgrades(this.storedBackpack)) {
            if (upgradeStack.getItem() instanceof AutoSmeltingUpgradeItem) {
                AutoSmeltingUpgradeData data = upgradeStack.getOrDefault(ModDataComponents.AUTO_SMELTING_UPGRADE_DATA.get(), AutoSmeltingUpgradeData.DEFAULT);
                if (data.litTime() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasActiveSmokerUpgrade() {
        for (ItemStack upgradeStack : BackpackStackData.loadUpgrades(this.storedBackpack)) {
            if (upgradeStack.getItem() instanceof SmokerUpgradeItem) {
                SmokerUpgradeData data = upgradeStack.getOrDefault(ModDataComponents.SMOKER_UPGRADE_DATA.get(), SmokerUpgradeData.DEFAULT);
                if (data.litTime() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasActiveBlastFurnaceUpgrade() {
        for (ItemStack upgradeStack : BackpackStackData.loadUpgrades(this.storedBackpack)) {
            if (upgradeStack.getItem() instanceof BlastFurnaceUpgradeItem) {
                BlastFurnaceUpgradeData data = upgradeStack.getOrDefault(ModDataComponents.BLAST_FURNACE_UPGRADE_DATA.get(), BlastFurnaceUpgradeData.DEFAULT);
                if (data.litTime() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void refreshBackpackLinkAnchor() {
        if (this.level instanceof ServerLevel serverLevel && !this.storedBackpack.isEmpty()) {
            BackpackLinkManager.refreshPlacedAnchor(serverLevel, this.worldPosition, this.storedBackpack);
        }
    }

    private void markBackpackLinkAnchorOffline(ItemStack backpack) {
        if (this.level instanceof ServerLevel serverLevel && !backpack.isEmpty()) {
            BackpackLinkManager.markOffline(serverLevel, backpack);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.storedBackpack.isEmpty()) {
            output.store("StoredBackpack", ItemStack.CODEC, this.storedBackpack);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.storedBackpack = input.read("StoredBackpack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.chunkForced = false;
        this.forcedChunkRadius = 0;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        if (!this.storedBackpack.isEmpty()) {
            tag.store("StoredBackpack", ItemStack.CODEC, registries.createSerializationContext(NbtOps.INSTANCE), this.storedBackpack);
        }
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public int getContainerSize() {
        BackpackTier tier = this.getBackpackTier();
        return tier != null ? tier.getSlotCount() : 0;
    }

    @Override
    public boolean isEmpty() {
        NonNullList<ItemStack> items = this.readItems();
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        NonNullList<ItemStack> items = this.readItems();
        return slot >= 0 && slot < items.size() ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        NonNullList<ItemStack> items = this.readItems();
        if (slot < 0 || slot >= items.size()) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = items.get(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (!ItemLockProtection.canAutomationExtract(this.storedBackpack, slot, existing)) {
            return ItemStack.EMPTY;
        }

        ItemStack result = existing.split(amount);
        if (existing.isEmpty()) {
            items.set(slot, ItemStack.EMPTY);
        }
        this.writeItems(items);
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        NonNullList<ItemStack> items = this.readItems();
        if (slot < 0 || slot >= items.size()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = items.get(slot);
        if (result.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!ItemLockProtection.canAutomationExtract(this.storedBackpack, slot, result)) {
            return ItemStack.EMPTY;
        }

        items.set(slot, ItemStack.EMPTY);
        this.writeItems(items);
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        NonNullList<ItemStack> items = this.readItems();
        if (slot < 0 || slot >= items.size()) {
            return;
        }
        ItemStack existing = items.get(slot);
        if (!ItemLockProtection.canAutomationInsert(this.storedBackpack, slot, existing, stack)) {
            return;
        }

        ItemStack copy = stack.copy();
        copy.limitSize(this.getMaxStackSize(copy));
        items.set(slot, copy);
        this.writeItems(items);
    }

    @Override
    public int getMaxStackSize() {
        return BackpackStackData.getBackpackMaxStackSize(this.storedBackpack);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return BackpackStackData.getStorageStackLimit(this.storedBackpack, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.level != null
                && this.level.getBlockEntity(this.worldPosition) == this
                && player.distanceToSqr(
                        this.worldPosition.getX() + 0.5D,
                        this.worldPosition.getY() + 0.5D,
                        this.worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {
        int size = this.getContainerSize();
        if (size <= 0) {
            return;
        }

        NonNullList<ItemStack> items = this.readItems();
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (ItemLockProtection.isProtected(this.storedBackpack, slot, stack)) {
                continue;
            }
            items.set(slot, ItemStack.EMPTY);
        }
        this.writeItems(items);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        int size = this.getContainerSize();
        return size <= 0 ? NO_SLOTS : IntStream.range(0, size).toArray();
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return this.canPlaceItem(slot, stack) && this.canAutomationInsert(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return this.canAutomationExtract(slot, stack);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return BackpackStackData.isValidStorageItem(this.storedBackpack, stack)
                && slot >= 0
                && slot < this.getContainerSize();
    }

    private BackpackTier getBackpackTier() {
        if (this.storedBackpack.getItem() instanceof BackpackItem backpackItem) {
            return backpackItem.getTier();
        }

        if (this.getBlockState().getBlock() instanceof BackpackBlock backpackBlock) {
            return backpackBlock.getTier();
        }

        return null;
    }

    private FilterUpgradeData getFilterUpgradeData() {
        for (ItemStack upgradeStack : BackpackStackData.loadUpgrades(this.storedBackpack)) {
            if (upgradeStack.getItem() instanceof FilterUpgradeItem) {
                return upgradeStack.getOrDefault(ModDataComponents.FILTER_UPGRADE_DATA.get(), FilterUpgradeData.DEFAULT);
            }
        }
        return null;
    }

    private boolean canAutomationInsert(int slot, ItemStack stack) {
        FilterUpgradeData data = this.getFilterUpgradeData();
        return (data == null || !data.enabled() || FilterUpgradeMatcher.allows(data, stack))
                && ItemLockProtection.canAutomationInsert(this.storedBackpack, slot, this.getItem(slot), stack);
    }

    private boolean canAutomationExtract(int slot, ItemStack stack) {
        FilterUpgradeData data = this.getFilterUpgradeData();
        return (data == null || !data.enabled() || FilterUpgradeMatcher.allows(data, stack))
                && ItemLockProtection.canAutomationExtract(this.storedBackpack, slot, stack);
    }

    private void tickProcessingUpgrades(Level level) {
        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(this.storedBackpack);
        boolean changed = false;

        for (int slot = 0; slot < upgrades.size(); slot++) {
            ItemStack upgrade = upgrades.get(slot);
            if (upgrade.getItem() instanceof FurnaceUpgradeItem) {
                FurnaceUpgradeData current = upgrade.getOrDefault(ModDataComponents.FURNACE_UPGRADE_DATA.get(), FurnaceUpgradeData.DEFAULT);
                FurnaceUpgradeData updated = FurnaceUpgradeLogic.tick(level, current);
                if (!updated.equals(current)) {
                    upgrade.set(ModDataComponents.FURNACE_UPGRADE_DATA.get(), updated);
                    upgrades.set(slot, upgrade);
                    changed = true;
                }
                continue;
            }

            if (upgrade.getItem() instanceof AutoSmeltingUpgradeItem) {
                AutoSmeltingUpgradeData current = upgrade.getOrDefault(ModDataComponents.AUTO_SMELTING_UPGRADE_DATA.get(), AutoSmeltingUpgradeData.DEFAULT);
                AutoSmeltingUpgradeData updated = AutoSmeltingUpgradeLogic.tick(level, this.storedBackpack, this.getBackpackTier(), current);
                if (!updated.equals(current)) {
                    upgrade.set(ModDataComponents.AUTO_SMELTING_UPGRADE_DATA.get(), updated);
                    upgrades.set(slot, upgrade);
                    changed = true;
                }
                continue;
            }

            if (upgrade.getItem() instanceof SmokerUpgradeItem) {
                SmokerUpgradeData current = upgrade.getOrDefault(ModDataComponents.SMOKER_UPGRADE_DATA.get(), SmokerUpgradeData.DEFAULT);
                SmokerUpgradeData updated = SmokerUpgradeLogic.tick(level, current);
                if (!updated.equals(current)) {
                    upgrade.set(ModDataComponents.SMOKER_UPGRADE_DATA.get(), updated);
                    upgrades.set(slot, upgrade);
                    changed = true;
                }
                continue;
            }

            if (upgrade.getItem() instanceof BlastFurnaceUpgradeItem) {
                BlastFurnaceUpgradeData current = upgrade.getOrDefault(ModDataComponents.BLAST_FURNACE_UPGRADE_DATA.get(), BlastFurnaceUpgradeData.DEFAULT);
                BlastFurnaceUpgradeData updated = BlastFurnaceUpgradeLogic.tick(level, current);
                if (!updated.equals(current)) {
                    upgrade.set(ModDataComponents.BLAST_FURNACE_UPGRADE_DATA.get(), updated);
                    upgrades.set(slot, upgrade);
                    changed = true;
                }
                continue;
            }

            if (upgrade.getItem() instanceof BrewingStandUpgradeItem) {
                BrewingStandUpgradeData current = upgrade.getOrDefault(ModDataComponents.BREWING_STAND_UPGRADE_DATA.get(), BrewingStandUpgradeData.DEFAULT);
                BrewingStandUpgradeData updated = BrewingStandUpgradeLogic.tick(level, current);
                if (!updated.equals(current)) {
                    upgrade.set(ModDataComponents.BREWING_STAND_UPGRADE_DATA.get(), updated);
                    upgrades.set(slot, upgrade);
                    changed = true;
                }
                continue;
            }

            if (upgrade.getItem() instanceof CompressionUpgradeItem) {
                if (CompressionUpgradeLogic.tick(level, this.storedBackpack, this.getBackpackTier())) {
                    changed = true;
                }
                continue;
            }

            if (upgrade.getItem() instanceof FluidTransferUpgradeItem) {
                if (this.transferFluids(level, upgrades, upgrade)) {
                    changed = true;
                }
                continue;
            }

            if (upgrade.getItem() instanceof HopperUpgradeItem) {
                if (this.transferHopperItems(level, upgrade)) {
                    changed = true;
                }
            }
        }

        if (changed) {
            BackpackStackData.saveUpgrades(this.storedBackpack, upgrades);
            this.notifyInventoryChanged();
        }
    }

    private NonNullList<ItemStack> readItems() {
        BackpackTier tier = this.getBackpackTier();
        return tier != null ? BackpackStackData.loadStorage(this.storedBackpack, tier) : NonNullList.create();
    }

    private void writeItems(NonNullList<ItemStack> items) {
        this.writeItems(items, true);
    }

    private void writeItems(NonNullList<ItemStack> items, boolean notify) {
        if (this.storedBackpack.isEmpty()) {
            return;
        }

        BackpackStackData.saveStorage(this.storedBackpack, items);
        if (notify) {
            this.notifyInventoryChanged();
        }
    }

    public NonNullList<ItemStack> createControllerStorageSnapshot() {
        NonNullList<ItemStack> source = this.readItems();
        NonNullList<ItemStack> copy = NonNullList.withSize(source.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < source.size(); slot++) {
            copy.set(slot, source.get(slot).copy());
        }
        return copy;
    }

    public void applyControllerStorageSnapshot(NonNullList<ItemStack> items) {
        this.writeItems(items);
    }

    public boolean canControllerInsert(int slot, ItemStack existing, ItemStack incoming) {
        return BackpackStackData.isValidStorageItem(this.storedBackpack, incoming)
                && slot >= 0
                && slot < this.getContainerSize()
                && (existing.isEmpty() || ItemStack.isSameItemSameComponents(existing, incoming))
                && this.canAutomationInsert(slot, incoming);
    }

    public boolean canControllerExtract(int slot, ItemStack stack) {
        return slot >= 0 && slot < this.getContainerSize() && this.canAutomationExtract(slot, stack);
    }

    public int getControllerStackLimit(ItemStack stack) {
        return this.getMaxStackSize(stack);
    }

    public boolean claimStorageTransfer(long gameTime) {
        if (this.lastStorageTransferGameTime == gameTime) {
            return false;
        }
        this.lastStorageTransferGameTime = gameTime;
        return true;
    }

    private void notifyInventoryChanged() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            this.level.invalidateCapabilities(this.worldPosition);
        }
    }

    public void releaseChunkForce() {
        if (this.chunkForced && this.level instanceof ServerLevel serverLevel) {
            this.setChunkForceRadius(serverLevel, this.forcedChunkRadius, false);
            this.chunkForced = false;
            this.forcedChunkRadius = 0;
        }
    }

    private void syncChunkForceState() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }

        int radius = this.getChunkLoaderRadius();
        boolean shouldForce = radius >= 0;
        if (shouldForce == this.chunkForced && (!shouldForce || radius == this.forcedChunkRadius)) {
            return;
        }

        if (this.chunkForced) {
            this.setChunkForceRadius(serverLevel, this.forcedChunkRadius, false);
            this.chunkForced = false;
            this.forcedChunkRadius = 0;
        }

        if (shouldForce) {
            this.setChunkForceRadius(serverLevel, radius, true);
            this.forcedChunkRadius = radius;
            this.chunkForced = true;
        }
    }

    private void setChunkForceRadius(ServerLevel serverLevel, int radius, boolean forced) {
        ChunkPos center = new ChunkPos(this.worldPosition.getX() >> 4, this.worldPosition.getZ() >> 4);
        for (int chunkX = center.x() - radius; chunkX <= center.x() + radius; chunkX++) {
            for (int chunkZ = center.z() - radius; chunkZ <= center.z() + radius; chunkZ++) {
                serverLevel.setChunkForced(chunkX, chunkZ, forced);
            }
        }
    }

    private int getChunkLoaderRadius() {
        for (ItemStack upgradeStack : BackpackStackData.loadUpgrades(this.storedBackpack)) {
            if (upgradeStack.getItem() instanceof ChunkLoaderUpgradeItem) {
                ChunkLoaderUpgradeData data = upgradeStack.getOrDefault(ModDataComponents.CHUNK_LOADER_UPGRADE_DATA.get(), ChunkLoaderUpgradeData.DEFAULT);
                return data.radius();
            }
        }

        return -1;
    }

    private boolean hasChunkLoaderUpgrade() {
        return this.getChunkLoaderRadius() >= 0;
    }

    private boolean hasFluidStorageUpgrade() {
        for (ItemStack upgradeStack : BackpackStackData.loadUpgrades(this.storedBackpack)) {
            if (upgradeStack.getItem() instanceof FluidStorageUpgradeItem) {
                return true;
            }
        }
        return false;
    }

    private int getFluidStorageUpgradeSlot() {
        return FluidStorageUpgradeHandler.findFirstUpgradeSlot(BackpackStackData.loadUpgrades(this.storedBackpack));
    }

    private ItemStack getFluidStorageUpgradeStack() {
        int slot = this.getFluidStorageUpgradeSlot();
        if (slot < 0) {
            return ItemStack.EMPTY;
        }
        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(this.storedBackpack);
        return slot < upgrades.size() ? upgrades.get(slot) : ItemStack.EMPTY;
    }

    private void saveFluidStorageUpgradeStack(ItemStack upgradeStack) {
        this.saveFluidStorageUpgradeStack(upgradeStack, true);
    }

    private void saveFluidStorageUpgradeStack(ItemStack upgradeStack, boolean notify) {
        int slot = this.getFluidStorageUpgradeSlot();
        if (slot < 0) {
            return;
        }
        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(this.storedBackpack);
        if (slot >= upgrades.size()) {
            return;
        }
        upgrades.set(slot, upgradeStack);
        BackpackStackData.saveUpgrades(this.storedBackpack, upgrades);
        if (notify) {
            this.notifyInventoryChanged();
        }
    }

    private boolean hasCapacitorUpgrade() {
        for (ItemStack upgradeStack : BackpackStackData.loadUpgrades(this.storedBackpack)) {
            if (upgradeStack.getItem() instanceof CapacitorUpgradeItem) {
                return true;
            }
        }
        return false;
    }

    private int getCapacitorUpgradeSlot() {
        return CapacitorUpgradeHandler.findFirstUpgradeSlot(BackpackStackData.loadUpgrades(this.storedBackpack));
    }

    private ItemStack getCapacitorUpgradeStack() {
        int slot = this.getCapacitorUpgradeSlot();
        if (slot < 0) {
            return ItemStack.EMPTY;
        }

        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(this.storedBackpack);
        return upgrades.get(slot);
    }

    private void saveCapacitorUpgradeStack(ItemStack upgradeStack) {
        this.saveCapacitorUpgradeStack(upgradeStack, true);
    }

    private void saveCapacitorUpgradeStack(ItemStack upgradeStack, boolean notify) {
        int slot = this.getCapacitorUpgradeSlot();
        if (slot < 0) {
            return;
        }

        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(this.storedBackpack);
        if (slot >= upgrades.size()) {
            return;
        }

        upgrades.set(slot, upgradeStack);
        BackpackStackData.saveUpgrades(this.storedBackpack, upgrades);
        if (notify) {
            this.notifyInventoryChanged();
        }
    }

    private List<InstalledMagnet> getInstalledMagnets() {
        List<InstalledMagnet> magnets = new ArrayList<>();
        for (ItemStack upgradeStack : BackpackStackData.loadUpgrades(this.storedBackpack)) {
            if (upgradeStack.getItem() instanceof MagnetUpgradeItem magnetItem) {
                MagnetUpgradeData data = upgradeStack.getOrDefault(ModDataComponents.MAGNET_UPGRADE_DATA.get(), MagnetUpgradeData.DEFAULT);
                if (data.enabled()) {
                    magnets.add(new InstalledMagnet(magnetItem, data));
                }
            }
        }
        return magnets;
    }

    private InstalledMagnet findMatchingMagnet(ItemEntity itemEntity, BackpackTier tier, List<InstalledMagnet> magnets, Vec3 center) {
        ItemStack stack = itemEntity.getItem();
            if (ItemLockProtection.insertIntoStorage(this.storedBackpack, tier, stack, true).getCount() == stack.getCount()) {
                return null;
            }

        for (InstalledMagnet magnet : magnets) {
            int range = magnet.item().getRange();
            if (itemEntity.position().distanceToSqr(center) > range * range) {
                continue;
            }
            if (MagnetFilterMatcher.allows(magnet.item(), magnet.data(), stack, this.storedBackpack, tier)) {
                return magnet;
            }
        }
        return null;
    }

    private void pullTowardBackpack(ItemEntity itemEntity, Vec3 target, double pullStrength) {
        Vec3 direction = target.subtract(itemEntity.position());
        if (direction.lengthSqr() < 0.001D) {
            return;
        }

        Vec3 motion = direction.normalize().scale(pullStrength);
        itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().scale(0.6D).add(motion));
        itemEntity.hurtMarked = true;
    }

    private boolean transferFluids(Level level, NonNullList<ItemStack> upgrades, ItemStack upgrade) {
        if (this.isOpen() || level.getGameTime() % FluidTransferUpgradeHandler.TICK_INTERVAL != 0L) {
            return false;
        }

        FluidTransferUpgradeData data = FluidTransferUpgradeHandler.getData(upgrade);
        int tankSlot = FluidStorageUpgradeHandler.findFirstUpgradeSlot(upgrades);
        if (tankSlot < 0 || tankSlot >= upgrades.size()) {
            return false;
        }

        ItemStack tankUpgrade = upgrades.get(tankSlot).copy();
        if (tankUpgrade.isEmpty()) {
            return false;
        }

        for (Direction direction : HOPPER_TRANSFER_DIRECTIONS) {
            BlockPos targetPos = this.worldPosition.relative(direction);
            BlockState targetState = level.getBlockState(targetPos);
            BlockEntity targetBlockEntity = level.getBlockEntity(targetPos);
            var adjacentResourceHandler = Capabilities.Fluid.BLOCK.getCapability(level, targetPos, targetState, targetBlockEntity, direction.getOpposite());
            IFluidHandler adjacentHandler = adjacentResourceHandler == null ? null : IFluidHandler.of(adjacentResourceHandler);

            boolean transferred = data.pushMode()
                    ? this.pushFluidIntoAdjacent(tankUpgrade, adjacentHandler, data)
                    : this.pullFluidFromAdjacent(level, targetPos, targetState, tankUpgrade, adjacentHandler, data);
            if (!transferred) {
                continue;
            }

            upgrades.set(tankSlot, tankUpgrade);
            return true;
        }

        return false;
    }

    private boolean pushFluidIntoAdjacent(ItemStack tankUpgrade, IFluidHandler adjacentHandler, FluidTransferUpgradeData data) {
        if (adjacentHandler == null) {
            return false;
        }

        FluidStack stored = FluidStorageUpgradeHandler.getFluid(tankUpgrade);
        if (stored.isEmpty() || !FluidTransferUpgradeHandler.allows(data, stored)) {
            return false;
        }

        FluidStack attempt = stored.copyWithAmount(Math.min(stored.getAmount(), FluidTransferUpgradeHandler.TRANSFER_AMOUNT));
        int transferred = adjacentHandler.fill(attempt, IFluidHandler.FluidAction.EXECUTE);
        if (transferred <= 0) {
            return false;
        }

        FluidStorageUpgradeHandler.drain(tankUpgrade, transferred, IFluidHandler.FluidAction.EXECUTE);
        return true;
    }

    private boolean pullFluidFromAdjacent(Level level, BlockPos targetPos, BlockState targetState, ItemStack tankUpgrade, IFluidHandler adjacentHandler, FluidTransferUpgradeData data) {
        if (adjacentHandler != null && this.pullFluidFromHandler(tankUpgrade, adjacentHandler, data)) {
            return true;
        }

        return data.collectSourceBlocks() && this.collectSourceFluid(level, targetPos, targetState, tankUpgrade, data);
    }

    private boolean pullFluidFromHandler(ItemStack tankUpgrade, IFluidHandler adjacentHandler, FluidTransferUpgradeData data) {
        for (int tank = 0; tank < adjacentHandler.getTanks(); tank++) {
            FluidStack stored = adjacentHandler.getFluidInTank(tank);
            int available = Math.min(stored.getAmount(), FluidTransferUpgradeHandler.TRANSFER_AMOUNT);
            if (stored.isEmpty() || available <= 0) {
                continue;
            }

            FluidStack candidate = stored.copyWithAmount(available);
            if (!FluidTransferUpgradeHandler.allows(data, candidate)) {
                continue;
            }

            int accepted = FluidStorageUpgradeHandler.fill(tankUpgrade, candidate, IFluidHandler.FluidAction.SIMULATE);
            if (accepted <= 0) {
                continue;
            }

            FluidStack drained = adjacentHandler.drain(candidate.copyWithAmount(accepted), IFluidHandler.FluidAction.EXECUTE);
            if (drained.isEmpty()) {
                continue;
            }

            FluidStorageUpgradeHandler.fill(tankUpgrade, drained, IFluidHandler.FluidAction.EXECUTE);
            return true;
        }

        return false;
    }

    private boolean collectSourceFluid(Level level, BlockPos targetPos, BlockState targetState, ItemStack tankUpgrade, FluidTransferUpgradeData data) {
        FluidState fluidState = targetState.getFluidState();
        if (fluidState.isEmpty() || !fluidState.isSource() || !(targetState.getBlock() instanceof LiquidBlock)) {
            return false;
        }

        FluidStack sourceFluid = new FluidStack(fluidState.getType(), FluidType.BUCKET_VOLUME);
        if (!FluidTransferUpgradeHandler.allows(data, sourceFluid)) {
            return false;
        }

        int accepted = FluidStorageUpgradeHandler.fill(tankUpgrade, sourceFluid, IFluidHandler.FluidAction.SIMULATE);
        if (accepted < FluidType.BUCKET_VOLUME) {
            return false;
        }

        if (!level.setBlockAndUpdate(targetPos, Blocks.AIR.defaultBlockState())) {
            return false;
        }

        FluidStorageUpgradeHandler.fill(tankUpgrade, sourceFluid, IFluidHandler.FluidAction.EXECUTE);
        return true;
    }

    private boolean transferHopperItems(Level level, ItemStack upgrade) {
        HopperUpgradeData data = upgrade.getOrDefault(ModDataComponents.HOPPER_UPGRADE_DATA.get(), HopperUpgradeData.DEFAULT);
        long gameTime = level.getGameTime();
        if (!data.enabled() || this.isOpen() || gameTime % 8L != 0L || this.lastHopperReceiveGameTime == gameTime || this.lastHopperTransferGameTime == gameTime) {
            return false;
        }

        BackpackTier sourceTier = this.getBackpackTier();
        if (sourceTier == null) {
            return false;
        }

        for (Direction direction : HOPPER_TRANSFER_DIRECTIONS) {
            BlockPos targetPos = this.worldPosition.relative(direction);
            BlockEntity blockEntity = level.getBlockEntity(targetPos);
            if (!(blockEntity instanceof PlacedBackpackBlockEntity targetBackpack) || targetBackpack == this || targetBackpack.storedBackpack.isEmpty()) {
                Container container = this.getAdjacentContainer(level, targetPos);
                if (container != null && this.transferOneMatchingStack(container, sourceTier, data)) {
                    this.lastHopperTransferGameTime = gameTime;
                    container.setChanged();
                    return true;
                }
                continue;
            }

            BackpackTier targetTier = targetBackpack.getBackpackTier();
            if (targetTier == null) {
                continue;
            }

            if (targetBackpack.isOpen()) {
                continue;
            }

            if (this.transferOneMatchingStack(targetBackpack, sourceTier, targetTier, data)) {
                this.lastHopperTransferGameTime = gameTime;
                targetBackpack.lastHopperReceiveGameTime = gameTime;
                targetBackpack.lastHopperTransferGameTime = gameTime;
                targetBackpack.notifyInventoryChanged();
                return true;
            }
        }

        return false;
    }

    private Container getAdjacentContainer(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock chestBlock) {
            return ChestBlock.getContainer(chestBlock, state, level, pos, true);
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof PlacedBackpackBlockEntity) {
            return null;
        }

        return blockEntity instanceof Container container ? container : null;
    }

    private boolean transferOneMatchingStack(PlacedBackpackBlockEntity targetBackpack, BackpackTier sourceTier, BackpackTier targetTier, HopperUpgradeData data) {
        NonNullList<ItemStack> sourceItems = BackpackStackData.loadStorage(this.storedBackpack, sourceTier);
        NonNullList<ItemStack> targetItems = BackpackStackData.loadStorage(targetBackpack.storedBackpack, targetTier);

        for (int slot = 0; slot < sourceItems.size(); slot++) {
            ItemStack sourceStack = sourceItems.get(slot);
            if (sourceStack.isEmpty()
                    || !HopperFilterMatcher.allows(data, sourceStack)
                    || !ItemLockProtection.canAutomationExtract(this.storedBackpack, slot, sourceStack)) {
                continue;
            }

            ItemStack remainder = this.insertIntoItems(targetBackpack.storedBackpack, targetItems, sourceStack);
            if (remainder.getCount() == sourceStack.getCount()) {
                continue;
            }

            sourceItems.set(slot, remainder);
            BackpackStackData.saveStorage(this.storedBackpack, sourceItems);
            BackpackStackData.saveStorage(targetBackpack.storedBackpack, targetItems);
            return true;
        }

        return false;
    }

    private boolean transferOneMatchingStack(Container targetContainer, BackpackTier sourceTier, HopperUpgradeData data) {
        NonNullList<ItemStack> sourceItems = BackpackStackData.loadStorage(this.storedBackpack, sourceTier);

        for (int slot = 0; slot < sourceItems.size(); slot++) {
            ItemStack sourceStack = sourceItems.get(slot);
            if (sourceStack.isEmpty()
                    || !HopperFilterMatcher.allows(data, sourceStack)
                    || !ItemLockProtection.canAutomationExtract(this.storedBackpack, slot, sourceStack)) {
                continue;
            }

            ItemStack remainder = this.insertIntoContainer(targetContainer, sourceStack);
            if (remainder.getCount() == sourceStack.getCount()) {
                continue;
            }

            sourceItems.set(slot, remainder);
            BackpackStackData.saveStorage(this.storedBackpack, sourceItems);
            return true;
        }

        return false;
    }

    private ItemStack insertIntoItems(ItemStack targetBackpack, NonNullList<ItemStack> targetItems, ItemStack incoming) {
        if (incoming.isEmpty() || !BackpackStackData.isValidStorageItem(targetBackpack, incoming)) {
            return incoming;
        }

        ItemStack remaining = incoming.copy();

        for (int slot = 0; slot < targetItems.size() && !remaining.isEmpty(); slot++) {
            ItemStack existing = targetItems.get(slot);
            if (existing.isEmpty()
                    || !ItemStack.isSameItemSameComponents(existing, remaining)
                    || !ItemLockProtection.canAutomationInsert(targetBackpack, slot, existing, remaining)) {
                continue;
            }

            int transfer = Math.min(remaining.getCount(), BackpackStackData.getStorageStackLimit(targetBackpack, existing) - existing.getCount());
            if (transfer <= 0) {
                continue;
            }

            existing.grow(transfer);
            remaining.shrink(transfer);
        }

        for (int slot = 0; slot < targetItems.size() && !remaining.isEmpty(); slot++) {
            ItemStack existing = targetItems.get(slot);
            if (!existing.isEmpty()
                    || !ItemLockProtection.canAutomationInsert(targetBackpack, slot, existing, remaining)) {
                continue;
            }

            int placed = Math.min(remaining.getCount(), BackpackStackData.getStorageStackLimit(targetBackpack, remaining));
            targetItems.set(slot, remaining.copyWithCount(placed));
            remaining.shrink(placed);
        }

        return remaining;
    }

    private ItemStack insertIntoContainer(Container container, ItemStack incoming) {
        if (incoming.isEmpty() || !BackpackStackData.isValidStorageItem(incoming)) {
            return incoming;
        }

        ItemStack remaining = incoming.copy();

        for (int slot = 0; slot < container.getContainerSize() && !remaining.isEmpty(); slot++) {
            ItemStack existing = container.getItem(slot);
            if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, remaining) || !container.canPlaceItem(slot, remaining)) {
                continue;
            }

            int transfer = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
            if (transfer <= 0) {
                continue;
            }

            existing.grow(transfer);
            remaining.shrink(transfer);
            container.setItem(slot, existing);
        }

        for (int slot = 0; slot < container.getContainerSize() && !remaining.isEmpty(); slot++) {
            ItemStack existing = container.getItem(slot);
            if (!existing.isEmpty() || !container.canPlaceItem(slot, remaining)) {
                continue;
            }

            int placed = Math.min(remaining.getCount(), remaining.getMaxStackSize());
            container.setItem(slot, remaining.copyWithCount(placed));
            remaining.shrink(placed);
        }

        return remaining;
    }

    private record InstalledMagnet(MagnetUpgradeItem item, MagnetUpgradeData data) {
    }

    private final class BackpackFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return PlacedBackpackBlockEntity.this.hasFluidStorageUpgrade() ? 1 : 0;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            if (tank != 0) {
                return FluidStack.EMPTY;
            }
            return FluidStorageUpgradeHandler.getFluid(PlacedBackpackBlockEntity.this.getFluidStorageUpgradeStack());
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? FluidStorageUpgradeHandler.CAPACITY : 0;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && !stack.isEmpty();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            ItemStack upgradeStack = PlacedBackpackBlockEntity.this.getFluidStorageUpgradeStack().copy();
            if (upgradeStack.isEmpty()) {
                return 0;
            }

            int filled = FluidStorageUpgradeHandler.fill(upgradeStack, resource, action);
            if (filled > 0 && action.execute()) {
                PlacedBackpackBlockEntity.this.saveFluidStorageUpgradeStack(upgradeStack);
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            ItemStack upgradeStack = PlacedBackpackBlockEntity.this.getFluidStorageUpgradeStack().copy();
            if (upgradeStack.isEmpty()) {
                return FluidStack.EMPTY;
            }

            FluidStack drained = FluidStorageUpgradeHandler.drain(upgradeStack, resource, action);
            if (!drained.isEmpty() && action.execute()) {
                PlacedBackpackBlockEntity.this.saveFluidStorageUpgradeStack(upgradeStack);
            }
            return drained;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            ItemStack upgradeStack = PlacedBackpackBlockEntity.this.getFluidStorageUpgradeStack().copy();
            if (upgradeStack.isEmpty()) {
                return FluidStack.EMPTY;
            }

            FluidStack drained = FluidStorageUpgradeHandler.drain(upgradeStack, maxDrain, action);
            if (!drained.isEmpty() && action.execute()) {
                PlacedBackpackBlockEntity.this.saveFluidStorageUpgradeStack(upgradeStack);
            }
            return drained;
        }
    }

    private final class BackpackEnergyStorage implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            ItemStack upgradeStack = PlacedBackpackBlockEntity.this.getCapacitorUpgradeStack().copy();
            if (upgradeStack.isEmpty()) {
                return 0;
            }

            int received = CapacitorUpgradeHandler.receiveEnergy(upgradeStack, maxReceive, simulate);
            if (received > 0 && !simulate) {
                PlacedBackpackBlockEntity.this.saveCapacitorUpgradeStack(upgradeStack);
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            ItemStack upgradeStack = PlacedBackpackBlockEntity.this.getCapacitorUpgradeStack().copy();
            if (upgradeStack.isEmpty()) {
                return 0;
            }

            int extracted = CapacitorUpgradeHandler.extractEnergy(upgradeStack, maxExtract, simulate);
            if (extracted > 0 && !simulate) {
                PlacedBackpackBlockEntity.this.saveCapacitorUpgradeStack(upgradeStack);
            }
            return extracted;
        }

        @Override
        public int getEnergyStored() {
            return CapacitorUpgradeHandler.getEnergyStored(PlacedBackpackBlockEntity.this.getCapacitorUpgradeStack());
        }

        @Override
        public int getMaxEnergyStored() {
            return CapacitorUpgradeHandler.CAPACITY;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }

    private final class BackpackAutomationItemHandler implements IItemHandlerModifiable {
        private final Direction side;

        private BackpackAutomationItemHandler(Direction side) {
            this.side = side;
        }

        @Override
        public int getSlots() {
            return PlacedBackpackBlockEntity.this.getContainerSize();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            ItemStack stack = PlacedBackpackBlockEntity.this.getItem(slot);
            return stack.isEmpty()
                    ? ItemStack.EMPTY
                    : stack.copyWithCount(Math.min(stack.getCount(), Math.max(1, stack.getMaxStackSize())));
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty() || slot < 0 || slot >= this.getSlots()) {
                return stack;
            }

            NonNullList<ItemStack> items = PlacedBackpackBlockEntity.this.readItems();
            ItemStack existing = items.get(slot);
            if (!PlacedBackpackBlockEntity.this.canAutomationInsert(slot, stack)) {
                return stack;
            }
            ItemStack remaining = stack.copy();

            if (!existing.isEmpty()) {
                if (!ItemStack.isSameItemSameComponents(existing, remaining)) {
                    return stack;
                }

                int transfer = Math.min(remaining.getCount(), BackpackStackData.getStorageStackLimit(PlacedBackpackBlockEntity.this.storedBackpack, existing) - existing.getCount());
                if (transfer <= 0) {
                    return stack;
                }

                if (!simulate) {
                    existing.grow(transfer);
                    PlacedBackpackBlockEntity.this.writeItems(items);
                }
                remaining.shrink(transfer);
                return remaining;
            }

            int placed = Math.min(remaining.getCount(), BackpackStackData.getStorageStackLimit(PlacedBackpackBlockEntity.this.storedBackpack, remaining));
            if (!simulate) {
                items.set(slot, remaining.copyWithCount(placed));
                PlacedBackpackBlockEntity.this.writeItems(items);
            }
            remaining.shrink(placed);
            return remaining;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount <= 0 || slot < 0 || slot >= this.getSlots()) {
                return ItemStack.EMPTY;
            }

            NonNullList<ItemStack> items = PlacedBackpackBlockEntity.this.readItems();
            ItemStack existing = items.get(slot);
            if (existing.isEmpty() || !PlacedBackpackBlockEntity.this.canAutomationExtract(slot, existing)) {
                return ItemStack.EMPTY;
            }

            int extracted = Math.min(amount, Math.min(existing.getCount(), existing.getMaxStackSize()));
            ItemStack result = existing.copyWithCount(extracted);
            if (!simulate) {
                existing.shrink(extracted);
                if (existing.isEmpty()) {
                    items.set(slot, ItemStack.EMPTY);
                }
                PlacedBackpackBlockEntity.this.writeItems(items);
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return BackpackStackData.getBackpackMaxStackSize(PlacedBackpackBlockEntity.this.storedBackpack);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot >= 0
                    && slot < this.getSlots()
                    && PlacedBackpackBlockEntity.this.canPlaceItem(slot, stack)
                    && PlacedBackpackBlockEntity.this.canAutomationInsert(slot, stack);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (slot < 0 || slot >= this.getSlots()) {
                return;
            }

            ItemStack existing = PlacedBackpackBlockEntity.this.getItem(slot);
            if (!ItemLockProtection.canAutomationInsert(PlacedBackpackBlockEntity.this.storedBackpack, slot, existing, stack)
                    || (!stack.isEmpty() && !this.isItemValid(slot, stack))) {
                return;
            }

            PlacedBackpackBlockEntity.this.setItem(slot, stack);
        }
    }
}
