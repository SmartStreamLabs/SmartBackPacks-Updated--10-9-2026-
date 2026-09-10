package com.teamsmartstreamlabs.smartbackpacks.registry;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.blockentity.PlacedBackpackBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SmartBackpacks.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PlacedBackpackBlockEntity>> PLACED_BACKPACK =
            BLOCK_ENTITY_TYPES.register("placed_backpack", () -> BlockEntityType.Builder.of(
                    PlacedBackpackBlockEntity::new,
                    ModBlocks.LEATHER_BACKPACK.get(),
                    ModBlocks.COAL_BACKPACK.get(),
                    ModBlocks.LAPIS_BACKPACK.get(),
                    ModBlocks.REDSTONE_BACKPACK.get(),
                    ModBlocks.QUARTZ_BACKPACK.get(),
                    ModBlocks.COPPER_BACKPACK.get(),
                    ModBlocks.IRON_BACKPACK.get(),
                    ModBlocks.GOLD_BACKPACK.get(),
                    ModBlocks.EMERALD_BACKPACK.get(),
                    ModBlocks.DIAMOND_BACKPACK.get(),
                    ModBlocks.NETHERITE_BACKPACK.get(),
                    ModBlocks.ANCIENT_NETHERITE_BACKPACK.get(),
                    ModBlocks.ULTIMATE_DIAMOND_BACKPACK.get(),
                    ModBlocks.NETHERITE_VAULT_BACKPACK.get()
            ).build(null));
    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
