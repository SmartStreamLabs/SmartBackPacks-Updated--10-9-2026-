package com.teamsmartstreamlabs.smartbackpacks.registry;

import com.mojang.serialization.MapCodec;
import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.loot.AbandonedBackpackLootFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModLootFunctions {
    private static final DeferredRegister<LootItemFunctionType<?>> FUNCTIONS =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, SmartBackpacks.MOD_ID);

    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<AbandonedBackpackLootFunction>> ABANDONED_BACKPACK =
            FUNCTIONS.register("abandoned_backpack", () -> new LootItemFunctionType<>(AbandonedBackpackLootFunction.CODEC));

    private ModLootFunctions() {
    }

    public static void register(IEventBus bus) {
        FUNCTIONS.register(bus);
    }
}
