package com.teamsmartstreamlabs.smartbackpacks.registry;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.block.BackpackBlock;
import com.teamsmartstreamlabs.smartbackpacks.block.StorageCableBlock;
import com.teamsmartstreamlabs.smartbackpacks.block.StorageControllerBlock;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
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
    public static final DeferredBlock<Block> STORAGE_CONTROLLER = registerBlock("storage_controller", StorageControllerBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F).sound(SoundType.COPPER));
    public static final DeferredBlock<Block> STORAGE_CABLE = registerBlock("storage_cable", StorageCableBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(0.6F).sound(SoundType.COPPER).noOcclusion());

    private ModBlocks() {
    }

    private static DeferredBlock<Block> registerBackpackBlock(String name, BackpackTier tier, BlockBehaviour.Properties properties) {
        ResourceKey<Block> id = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, name));
        return BLOCKS.register(name, () -> new BackpackBlock(tier, properties.setId(id)));
    }

    private static DeferredBlock<Block> registerBlock(
            String name,
            java.util.function.Function<BlockBehaviour.Properties, ? extends Block> factory,
            BlockBehaviour.Properties properties) {
        ResourceKey<Block> id = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, name));
        return BLOCKS.register(name, () -> factory.apply(properties.setId(id)));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
