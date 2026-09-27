package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.BrewingInput;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public final class BrewingStandUpgradeLogic {
    public static final int BOTTLE_SLOT_START = 0;
    public static final int BOTTLE_SLOT_END = 2;
    public static final int INGREDIENT_SLOT = 3;
    public static final int FUEL_SLOT = 4;
    public static final int BREW_TIME_TOTAL = 400;
    public static final int FUEL_USES = 20;

    private BrewingStandUpgradeLogic() {
    }

    public static BrewingStandUpgradeData tick(Level level, BrewingStandUpgradeData current) {
        NonNullList<ItemStack> items = loadItems(current);
        ItemStack ingredient = items.get(INGREDIENT_SLOT);
        ItemStack fuelStack = items.get(FUEL_SLOT);
        int brewTime = current.brewTime();
        int fuel = current.fuel();

        if (fuel <= 0 && fuelStack.is(Items.BLAZE_POWDER)) {
            fuel = FUEL_USES;
            fuelStack.shrink(1);
            if (fuelStack.isEmpty()) {
                items.set(FUEL_SLOT, ItemStack.EMPTY);
            }
        }

        boolean canBrew = canBrew(level, items);
        if (brewTime > 0) {
            brewTime--;
            if (brewTime == 0) {
                if (canBrew) {
                    doBrew(level, items);
                }
            } else if (!canBrew) {
                brewTime = 0;
            }
        } else if (canBrew && fuel > 0) {
            fuel--;
            brewTime = BREW_TIME_TOTAL;
        }

        return new BrewingStandUpgradeData(ItemContainerContents.fromItems(items), brewTime, fuel);
    }

    public static NonNullList<ItemStack> loadItems(BrewingStandUpgradeData data) {
        NonNullList<ItemStack> items = NonNullList.withSize(BrewingStandUpgradeData.SLOT_COUNT, ItemStack.EMPTY);
        data.items().copyInto(items);
        return items;
    }

    private static boolean canBrew(Level level, NonNullList<ItemStack> items) {
        ItemStack ingredient = items.get(INGREDIENT_SLOT);
        if (ingredient.isEmpty()) {
            return false;
        }

        for (int slot = BOTTLE_SLOT_START; slot <= BOTTLE_SLOT_END; slot++) {
            ItemStack bottle = items.get(slot);
            if (!bottle.isEmpty() && findRecipe(level, bottle, ingredient).isPresent()) {
                return true;
            }
        }

        return false;
    }

    private static void doBrew(Level level, NonNullList<ItemStack> items) {
        ItemStack ingredient = items.get(INGREDIENT_SLOT);
        for (int slot = BOTTLE_SLOT_START; slot <= BOTTLE_SLOT_END; slot++) {
            ItemStack bottle = items.get(slot);
            if (!bottle.isEmpty()) {
                Optional<RecipeHolder<BrewingRecipe>> recipe = findRecipe(level, bottle, ingredient);
                if (recipe.isPresent()) {
                    items.set(slot, recipe.get().value().assemble(new BrewingInput(bottle, ingredient)));
                }
            }
        }

        ingredient.shrink(1);
        if (ingredient.isEmpty()) {
            items.set(INGREDIENT_SLOT, ItemStack.EMPTY);
        }
    }

    private static Optional<RecipeHolder<BrewingRecipe>> findRecipe(Level level, ItemStack bottle, ItemStack ingredient) {
        return level.getServer().getRecipeManager().getRecipeFor(RecipeType.BREWING, new BrewingInput(bottle, ingredient), level);
    }
}
