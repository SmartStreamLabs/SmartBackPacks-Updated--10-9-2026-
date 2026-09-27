package com.teamsmartstreamlabs.smartbackpacks.registry;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.recipe.BackpackWorkbenchRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeSerializers {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, SmartBackpacks.MOD_ID);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BackpackWorkbenchRecipe>> BACKPACK_WORKBENCH =
            SERIALIZERS.register("backpack_workbench", () -> BackpackWorkbenchRecipe.SERIALIZER);

    private ModRecipeSerializers() {}
    public static void register(IEventBus bus) { SERIALIZERS.register(bus); }
}
