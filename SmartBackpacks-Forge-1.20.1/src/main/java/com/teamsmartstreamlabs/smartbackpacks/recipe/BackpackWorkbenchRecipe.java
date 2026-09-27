package com.teamsmartstreamlabs.smartbackpacks.recipe;

import com.google.gson.JsonObject;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModRecipeSerializers;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public record BackpackWorkbenchRecipe(ShapedRecipe shaped) implements Recipe<CraftingContainer> {
    public static final RecipeSerializer<BackpackWorkbenchRecipe> SERIALIZER = new RecipeSerializer<>() {
        @Override
        public BackpackWorkbenchRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new BackpackWorkbenchRecipe(RecipeSerializer.SHAPED_RECIPE.fromJson(id, json));
        }

        @Override
        public BackpackWorkbenchRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new BackpackWorkbenchRecipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buffer));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, BackpackWorkbenchRecipe recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe.shaped());
        }
    };

    public NonNullList<Ingredient> ingredients() { return this.shaped.getIngredients(); }
    public int width() { return this.shaped.getWidth(); }
    public int height() { return this.shaped.getHeight(); }
    @Override public boolean matches(CraftingContainer input, Level level) { return this.shaped.matches(input, level); }
    @Override public ItemStack assemble(CraftingContainer input, RegistryAccess registries) { return this.shaped.assemble(input, registries); }
    @Override public boolean canCraftInDimensions(int width, int height) { return this.shaped.canCraftInDimensions(width, height); }
    @Override public ItemStack getResultItem(RegistryAccess registries) { return this.shaped.getResultItem(registries); }
    @Override public ResourceLocation getId() { return this.shaped.getId(); }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipeSerializers.BACKPACK_WORKBENCH.get(); }
    @Override public RecipeType<?> getType() { return ModRecipeTypes.BACKPACK_WORKBENCH.get(); }
    @Override public NonNullList<ItemStack> getRemainingItems(CraftingContainer input) { return this.shaped.getRemainingItems(input); }
    @Override public NonNullList<Ingredient> getIngredients() { return this.shaped.getIngredients(); }
    @Override public boolean isSpecial() { return true; }
}
