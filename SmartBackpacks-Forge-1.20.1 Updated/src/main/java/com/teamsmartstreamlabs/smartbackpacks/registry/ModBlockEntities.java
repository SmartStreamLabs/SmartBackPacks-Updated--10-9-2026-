package com.teamsmartstreamlabs.smartbackpacks.registry;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.blockentity.PlacedBackpackBlockEntity;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SmartBackpacks.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PlacedBackpackBlockEntity>> PLACED_BACKPACK =
            BLOCK_ENTITY_TYPES.register("placed_backpack", () -> new BlockEntityType<>(
                    PlacedBackpackBlockEntity::new,
                    Set.of(
                            (Block) ModBlocks.LEATHER_BACKPACK.get(),
                            (Block) ModBlocks.COAL_BACKPACK.get(),
                            (Block) ModBlocks.LAPIS_BACKPACK.get(),
                            (Block) ModBlocks.REDSTONE_BACKPACK.get(),
                            (Block) ModBlocks.QUARTZ_BACKPACK.get(),
                            (Block) ModBlocks.COPPER_BACKPACK.get(),
                            (Block) ModBlocks.IRON_BACKPACK.get(),
                            (Block) ModBlocks.GOLD_BACKPACK.get(),
                            (Block) ModBlocks.EMERALD_BACKPACK.get(),
                            (Block) ModBlocks.DIAMOND_BACKPACK.get(),
                            (Block) ModBlocks.NETHERITE_BACKPACK.get(),
                            (Block) ModBlocks.ANCIENT_NETHERITE_BACKPACK.get(),
                            (Block) ModBlocks.ULTIMATE_DIAMOND_BACKPACK.get(),
                            (Block) ModBlocks.NETHERITE_VAULT_BACKPACK.get()
                    ),
                    null
            ));
    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
