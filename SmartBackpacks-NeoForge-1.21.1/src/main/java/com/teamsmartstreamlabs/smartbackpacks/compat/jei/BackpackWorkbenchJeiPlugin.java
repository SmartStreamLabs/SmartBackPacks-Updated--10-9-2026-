package com.teamsmartstreamlabs.smartbackpacks.compat.jei;

import java.util.Arrays;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.recipe.BackpackWorkbenchRecipe;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModRecipeTypes;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

@JeiPlugin
public final class BackpackWorkbenchJeiPlugin implements IModPlugin {
    private static final RecipeType<BackpackWorkbenchRecipe> TYPE =
            RecipeType.create(SmartBackpacks.MOD_ID, "backpack_workbench", BackpackWorkbenchRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "backpack_workbench_jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IDrawable icon = registration.getJeiHelpers().getGuiHelper()
                .createDrawableItemLike(ModItems.BACKPACK_WORKBENCH.get());
        IDrawable background = registration.getJeiHelpers().getGuiHelper().createBlankDrawable(116, 58);
        registration.addRecipeCategories(new IRecipeCategory<BackpackWorkbenchRecipe>() {
            @Override public RecipeType<BackpackWorkbenchRecipe> getRecipeType() { return TYPE; }
            @Override public Component getTitle() { return Component.translatable("block.smartbackpacks.backpack_workbench"); }
            @Override public IDrawable getIcon() { return icon; }
            @Override public IDrawable getBackground() { return background; }
            @Override public int getWidth() { return 116; }
            @Override public int getHeight() { return 58; }

            @Override
            public void setRecipe(IRecipeLayoutBuilder builder, BackpackWorkbenchRecipe recipe, IFocusGroup focuses) {
                for (int index = 0; index < recipe.ingredients().size(); index++) {
                    Ingredient ingredient = recipe.ingredients().get(index);
                    if (!ingredient.isEmpty()) {
                        builder.addSlot(RecipeIngredientRole.INPUT, 1 + (index % recipe.width()) * 18,
                                1 + (index / recipe.width()) * 18)
                                .addItemStacks(Arrays.asList(ingredient.getItems()));
                    }
                }
                if (Minecraft.getInstance().level != null) {
                    builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19).addItemStack(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
                }
            }
        });
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (Minecraft.getInstance().level != null) {
            registration.addRecipes(TYPE, Minecraft.getInstance().level.getRecipeManager()
                    .getAllRecipesFor(ModRecipeTypes.BACKPACK_WORKBENCH.get()).stream().map(RecipeHolder::value).toList());
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModItems.BACKPACK_WORKBENCH.get(), TYPE);
    }
}
