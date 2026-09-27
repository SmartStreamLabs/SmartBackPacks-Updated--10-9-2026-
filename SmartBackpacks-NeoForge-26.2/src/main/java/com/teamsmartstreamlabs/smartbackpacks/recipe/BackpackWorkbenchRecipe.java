package com.teamsmartstreamlabs.smartbackpacks.recipe;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModRecipeSerializers;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModRecipeTypes;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public record BackpackWorkbenchRecipe(ShapedRecipe shaped) implements Recipe<CraftingInput> {
    public static final MapCodec<BackpackWorkbenchRecipe> MAP_CODEC =
            ShapedRecipe.MAP_CODEC.xmap(BackpackWorkbenchRecipe::new, BackpackWorkbenchRecipe::shaped);
    public static final StreamCodec<RegistryFriendlyByteBuf, BackpackWorkbenchRecipe> STREAM_CODEC =
            ShapedRecipe.STREAM_CODEC.map(BackpackWorkbenchRecipe::new, BackpackWorkbenchRecipe::shaped);
    public static final RecipeSerializer<BackpackWorkbenchRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public List<Optional<Ingredient>> ingredients() {
        return this.shaped.getIngredients();
    }

    public int width() {
        return this.shaped.getWidth();
    }

    public int height() {
        return this.shaped.getHeight();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return this.shaped.matches(input, level);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return this.shaped.assemble(input);
    }

    @Override
    public boolean showNotification() {
        return this.shaped.showNotification();
    }

    @Override
    public String group() {
        return this.shaped.group();
    }

    @Override
    public RecipeSerializer<BackpackWorkbenchRecipe> getSerializer() {
        return ModRecipeSerializers.BACKPACK_WORKBENCH.get();
    }

    @Override
    public RecipeType<BackpackWorkbenchRecipe> getType() {
        return ModRecipeTypes.BACKPACK_WORKBENCH.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return this.shaped.placementInfo();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return this.shaped.recipeBookCategory();
    }
}
