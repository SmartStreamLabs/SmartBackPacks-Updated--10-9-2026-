package com.teamsmartstreamlabs.smartbackpacks.upgrade;


import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import java.util.Optional;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public final class BlastFurnaceUpgradeLogic {
    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int RESULT_SLOT = 2;

    private BlastFurnaceUpgradeLogic() {
    }

    public static BlastFurnaceUpgradeData tick(Level level, BlastFurnaceUpgradeData current) {
        NonNullList<ItemStack> items = loadItems(current);
        ItemStack input = items.get(INPUT_SLOT);
        ItemStack fuel = items.get(FUEL_SLOT);
        ItemStack output = items.get(RESULT_SLOT);

        int litTime = current.litTime();
        int litDuration = current.litDuration();
        int cookingProgress = current.cookingProgress();
        int cookingTotalTime = Math.max(1, current.cookingTotalTime());
        boolean lit = litTime > 0;

        if (lit) {
            litTime--;
        }

        Optional<BlastingRecipe> recipeHolder = getRecipe(level, input);
        boolean canSmelt = recipeHolder.isPresent() && canSmelt(level, recipeHolder.get(), input, output);

        if (!lit && canSmelt) {
            int burnTime = FurnaceFuelHelper.getBurnTime(fuel);
            if (burnTime <= 0) {
                burnTime = FurnaceFuelHelper.getBurnTime(fuel);
            }
            if (burnTime > 0) {
                litTime = burnTime;
                litDuration = burnTime;
                fuel.shrink(1);
                lit = true;
            }
        }

        if (lit && canSmelt) {
            cookingProgress++;
            if (cookingProgress >= cookingTotalTime) {
                smelt(level, recipeHolder.get(), items);
                cookingProgress = 0;
                cookingTotalTime = BlastFurnaceUpgradeData.DEFAULT_COOK_TIME;
            }
        } else if (cookingProgress > 0) {
            cookingProgress = 0;
        }

        return new BlastFurnaceUpgradeData(ItemContainerContents.fromItems(items), litTime, litDuration, cookingProgress, cookingTotalTime);
    }

    public static NonNullList<ItemStack> loadItems(BlastFurnaceUpgradeData data) {
        NonNullList<ItemStack> items = NonNullList.withSize(BlastFurnaceUpgradeData.SLOT_COUNT, ItemStack.EMPTY);
        data.items().copyInto(items);
        return items;
    }

    private static Optional<BlastingRecipe> getRecipe(Level level, ItemStack input) {
        if (input.isEmpty()) {
            return Optional.empty();
        }

        RecipeManager recipeManager = level.getServer() != null ? level.getServer().getRecipeManager() : null;
        if (recipeManager == null) {
            return Optional.empty();
        }

        return recipeManager.getRecipeFor(RecipeType.BLASTING, new SingleRecipeInput(input), level);
    }

    private static boolean canSmelt(Level level, BlastingRecipe recipeHolder, ItemStack input, ItemStack output) {
        if (input.isEmpty()) {
            return false;
        }

        ItemStack result = recipeHolder.assemble(new SingleRecipeInput(input), level.registryAccess());
        if (result.isEmpty()) {
            return false;
        }

        if (output.isEmpty()) {
            return true;
        }

        if (!com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.isSameItemSameComponents(output, result)) {
            return false;
        }

        return output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private static void smelt(Level level, BlastingRecipe recipeHolder, NonNullList<ItemStack> items) {
        ItemStack input = items.get(INPUT_SLOT);
        ItemStack output = items.get(RESULT_SLOT);
        ItemStack result = recipeHolder.assemble(new SingleRecipeInput(input), level.registryAccess());
        if (result.isEmpty()) {
            return;
        }

        if (output.isEmpty()) {
            items.set(RESULT_SLOT, result.copy());
        } else if (com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.isSameItemSameComponents(output, result)) {
            output.grow(result.getCount());
        }

        input.shrink(1);
        if (input.isEmpty()) {
            items.set(INPUT_SLOT, ItemStack.EMPTY);
        }
        if (items.get(FUEL_SLOT).isEmpty()) {
            items.set(FUEL_SLOT, ItemStack.EMPTY);
        }
    }
}

