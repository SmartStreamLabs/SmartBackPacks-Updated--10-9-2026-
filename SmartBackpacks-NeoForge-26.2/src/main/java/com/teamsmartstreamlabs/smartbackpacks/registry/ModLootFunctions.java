package com.teamsmartstreamlabs.smartbackpacks.registry;

import com.mojang.serialization.MapCodec;
import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.loot.AbandonedBackpackLootFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModLootFunctions {
    private static final DeferredRegister<MapCodec<? extends LootItemFunction>> FUNCTIONS =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, SmartBackpacks.MOD_ID);

    static {
        FUNCTIONS.register("abandoned_backpack", () -> AbandonedBackpackLootFunction.CODEC);
    }

    private ModLootFunctions() {
    }

    public static void register(IEventBus bus) {
        FUNCTIONS.register(bus);
    }
}
