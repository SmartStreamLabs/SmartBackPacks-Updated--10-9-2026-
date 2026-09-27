package com.teamsmartstreamlabs.smartbackpacks.recipe;

import com.mojang.serialization.MapCodec;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModRecipeSerializers;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public record BackpackWorkbenchRecipe(ShapedRecipe shaped) implements Recipe<CraftingInput> {
    public static final RecipeSerializer<BackpackWorkbenchRecipe> SERIALIZER = new RecipeSerializer<>() {
        private final MapCodec<BackpackWorkbenchRecipe> codec =
                ShapedRecipe.Serializer.CODEC.xmap(BackpackWorkbenchRecipe::new, BackpackWorkbenchRecipe::shaped);
        private final StreamCodec<RegistryFriendlyByteBuf, BackpackWorkbenchRecipe> streamCodec =
                ShapedRecipe.Serializer.STREAM_CODEC.map(BackpackWorkbenchRecipe::new, BackpackWorkbenchRecipe::shaped);

        @Override
        public MapCodec<BackpackWorkbenchRecipe> codec() {
            return this.codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BackpackWorkbenchRecipe> streamCodec() {
            return this.streamCodec;
        }
    };

    public NonNullList<Ingredient> ingredients() { return this.shaped.getIngredients(); }
    public int width() { return this.shaped.getWidth(); }
    public int height() { return this.shaped.getHeight(); }
    @Override public boolean matches(CraftingInput input, Level level) { return this.shaped.matches(input, level); }
    @Override public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) { return this.shaped.assemble(input, registries); }
    @Override public boolean canCraftInDimensions(int width, int height) { return this.shaped.canCraftInDimensions(width, height); }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return this.shaped.getResultItem(registries); }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipeSerializers.BACKPACK_WORKBENCH.get(); }
    @Override public RecipeType<?> getType() { return ModRecipeTypes.BACKPACK_WORKBENCH.get(); }
    @Override public NonNullList<ItemStack> getRemainingItems(CraftingInput input) { return this.shaped.getRemainingItems(input); }
    @Override public NonNullList<Ingredient> getIngredients() { return this.shaped.getIngredients(); }
    @Override public boolean isSpecial() { return true; }
}
