package com.teamsmartstreamlabs.smartbackpacks.registry;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.block.BackpackBlock;
import com.teamsmartstreamlabs.smartbackpacks.block.BackpackWorkbenchBlock;
import com.teamsmartstreamlabs.smartbackpacks.block.BackpackDisplayHookBlock;
import com.teamsmartstreamlabs.smartbackpacks.block.StorageCableBlock;
import com.teamsmartstreamlabs.smartbackpacks.block.StorageControllerBlock;
import com.teamsmartstreamlabs.smartbackpacks.block.StorageTransferBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SmartBackpacks.MOD_ID);

    public static final DeferredBlock<Block> LEATHER_BACKPACK = registerBackpackBlock("leather_backpack_block", BackpackTier.LEATHER,
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.8F).sound(SoundType.WOOL).noOcclusion());
    public static final DeferredBlock<Block> COAL_BACKPACK = registerBackpackBlock("coal_backpack_block", BackpackTier.COAL,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(0.8F).sound(SoundType.WOOL).noOcclusion());
    public static final DeferredBlock<Block> LAPIS_BACKPACK = registerBackpackBlock("lapis_backpack_block", BackpackTier.LAPIS,
            BlockBehaviour.Properties.of().mapColor(MapColor.LAPIS).strength(0.8F).sound(SoundType.WOOL).noOcclusion());
    public static final DeferredBlock<Block> REDSTONE_BACKPACK = registerBackpackBlock("redstone_backpack_block", BackpackTier.REDSTONE,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(0.8F).sound(SoundType.WOOL).noOcclusion());
    public static final DeferredBlock<Block> QUARTZ_BACKPACK = registerBackpackBlock("quartz_backpack_block", BackpackTier.QUARTZ,
            BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(0.8F).sound(SoundType.WOOL).noOcclusion());
    public static final DeferredBlock<Block> COPPER_BACKPACK = registerBackpackBlock("copper_backpack_block", BackpackTier.COPPER,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.8F).sound(SoundType.WOOL).noOcclusion());
    public static final DeferredBlock<Block> IRON_BACKPACK = registerBackpackBlock("iron_backpack_block", BackpackTier.IRON,
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.8F).sound(SoundType.WOOL).noOcclusion());
    public static final DeferredBlock<Block> GOLD_BACKPACK = registerBackpackBlock("gold_backpack_block", BackpackTier.GOLD,
            BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(0.8F).sound(SoundType.WOOL).noOcclusion());
    public static final DeferredBlock<Block> EMERALD_BACKPACK = registerBackpackBlock("emerald_backpack_block", BackpackTier.EMERALD,
            BlockBehaviour.Properties.of().mapColor(MapColor.EMERALD).strength(0.8F).sound(SoundType.WOOL).noOcclusion());
    public static final DeferredBlock<Block> DIAMOND_BACKPACK = registerBackpackBlock("diamond_backpack_block", BackpackTier.DIAMOND,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(0.8F).sound(SoundType.WOOL).noOcclusion());
    public static final DeferredBlock<Block> NETHERITE_BACKPACK = registerBackpackBlock("netherite_backpack_block", BackpackTier.NETHERITE,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(0.8F).sound(SoundType.WOOL).noOcclusion());
    public static final DeferredBlock<Block> ANCIENT_NETHERITE_BACKPACK = registerBackpackBlock("ancient_netherite_backpack_block", BackpackTier.ANCIENT_NETHERITE,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(0.8F).sound(SoundType.WOOL).noOcclusion());
    public static final DeferredBlock<Block> ULTIMATE_DIAMOND_BACKPACK = registerBackpackBlock("ultimate_diamond_backpack_block", BackpackTier.ULTIMATE_DIAMOND,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(0.8F).sound(SoundType.WOOL).noOcclusion());
    public static final DeferredBlock<Block> NETHERITE_VAULT_BACKPACK = registerBackpackBlock("netherite_vault_backpack_block", BackpackTier.NETHERITE_VAULT,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(0.8F).sound(SoundType.WOOL).noOcclusion());
    public static final DeferredBlock<Block> BACKPACK_WORKBENCH = BLOCKS.register("backpack_workbench",
            () -> new BackpackWorkbenchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                    .strength(2.5F).sound(SoundType.WOOD).noOcclusion()));
    public static final DeferredBlock<Block> BACKPACK_DISPLAY_HOOK = BLOCKS.register("backpack_display_hook",
            () -> new BackpackDisplayHookBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                    .strength(1.5F).sound(SoundType.METAL).noOcclusion().pushReaction(PushReaction.BLOCK)));
    public static final DeferredBlock<Block> STORAGE_CONTROLLER = BLOCKS.register("storage_controller",
            () -> new StorageControllerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD).strength(3.5F).sound(SoundType.COPPER)));
    public static final DeferredBlock<Block> STORAGE_CABLE = BLOCKS.register("storage_cable",
            () -> new StorageCableBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD).strength(1.2F).sound(SoundType.COPPER).noOcclusion()));
    public static final DeferredBlock<Block> STORAGE_IMPORTER = BLOCKS.register("storage_importer",
            () -> new StorageTransferBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                    .strength(2.5F).sound(SoundType.COPPER).noOcclusion(), true));
    public static final DeferredBlock<Block> STORAGE_EXPORTER = BLOCKS.register("storage_exporter",
            () -> new StorageTransferBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                    .strength(2.5F).sound(SoundType.COPPER).noOcclusion(), false));

    private ModBlocks() {
    }

    private static DeferredBlock<Block> registerBackpackBlock(String name, BackpackTier tier, BlockBehaviour.Properties properties) {
        return BLOCKS.register(name, () -> new BackpackBlock(tier, properties));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
