package com.teamsmartstreamlabs.smartbackpacks.upgrade;


import com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat;
import java.util.Optional;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;

public final class AutoSmeltingUpgradeLogic {
    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int RESULT_SLOT = 2;

    private AutoSmeltingUpgradeLogic() {
    }

    public static AutoSmeltingUpgradeData tick(Level level, ItemStack backpack, BackpackTier tier, AutoSmeltingUpgradeData current) {
        NonNullList<ItemStack> storage = BackpackStackData.loadStorage(backpack, tier);
        NonNullList<ItemStack> items = loadItems(current);

        moveOutputToBackpack(storage, items);
        pullInputFromBackpack(level, storage, items);
        pullFuelFromBackpack(level, storage, items, current.litTime());

        AutoSmeltingUpgradeData updated = tickInternal(level, items, current);
        items = loadItems(updated);

        moveOutputToBackpack(storage, items);
        pullInputFromBackpack(level, storage, items);
        pullFuelFromBackpack(level, storage, items, updated.litTime());

        BackpackStackData.saveStorage(backpack, storage);
        return new AutoSmeltingUpgradeData(
                ItemContainerContents.fromItems(items),
                updated.litTime(),
                updated.litDuration(),
                updated.cookingProgress(),
                updated.cookingTotalTime());
    }

    public static NonNullList<ItemStack> loadItems(AutoSmeltingUpgradeData data) {
        NonNullList<ItemStack> items = NonNullList.withSize(AutoSmeltingUpgradeData.SLOT_COUNT, ItemStack.EMPTY);
        data.items().copyInto(items);
        return items;
    }

    private static AutoSmeltingUpgradeData tickInternal(Level level, NonNullList<ItemStack> items, AutoSmeltingUpgradeData current) {
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

        Optional<SmeltingRecipe> recipeHolder = getRecipe(level, input);
        boolean canSmelt = recipeHolder.isPresent() && canSmelt(level, recipeHolder.get(), input, output);

        if (!lit && canSmelt) {
            int burnTime = FurnaceFuelHelper.getBurnTime(fuel);
            if (burnTime > 0) {
                litTime = burnTime;
                litDuration = burnTime;
                fuel.shrink(1);
                if (fuel.isEmpty()) {
                    items.set(FUEL_SLOT, ItemStack.EMPTY);
                }
                lit = true;
            }
        }

        if (lit && canSmelt) {
            cookingProgress++;
            if (cookingProgress >= cookingTotalTime) {
                smelt(level, recipeHolder.get(), items);
                cookingProgress = 0;
                cookingTotalTime = AutoSmeltingUpgradeData.DEFAULT_COOK_TIME;
            }
        } else if (cookingProgress > 0) {
            cookingProgress = 0;
        }

        return new AutoSmeltingUpgradeData(ItemContainerContents.fromItems(items), litTime, litDuration, cookingProgress, cookingTotalTime);
    }

    private static void pullInputFromBackpack(Level level, NonNullList<ItemStack> storage, NonNullList<ItemStack> items) {
        if (!items.get(INPUT_SLOT).isEmpty()) {
            return;
        }

        for (int slot = 0; slot < storage.size(); slot++) {
            ItemStack stored = storage.get(slot);
            if (stored.isEmpty() || getRecipe(level, stored).isEmpty()) {
                continue;
            }

            items.set(INPUT_SLOT, stored.split(1));
            if (stored.isEmpty()) {
                storage.set(slot, ItemStack.EMPTY);
            }
            return;
        }
    }

    private static void pullFuelFromBackpack(Level level, NonNullList<ItemStack> storage, NonNullList<ItemStack> items, int litTime) {
        if (!items.get(FUEL_SLOT).isEmpty() || litTime > 0) {
            return;
        }

        for (int slot = 0; slot < storage.size(); slot++) {
            ItemStack stored = storage.get(slot);
            if (stored.isEmpty() || FurnaceFuelHelper.getBurnTime(stored) <= 0) {
                continue;
            }

            items.set(FUEL_SLOT, stored.split(1));
            if (stored.isEmpty()) {
                storage.set(slot, ItemStack.EMPTY);
            }
            return;
        }
    }

    private static void moveOutputToBackpack(NonNullList<ItemStack> storage, NonNullList<ItemStack> items) {
        ItemStack output = items.get(RESULT_SLOT);
        if (output.isEmpty()) {
            return;
        }

        ItemStack resultRemainder = insertIntoStorage(storage, output);
        items.set(RESULT_SLOT, resultRemainder);
    }

    private static ItemStack insertIntoStorage(NonNullList<ItemStack> storage, ItemStack incoming) {
        ItemStack remaining = incoming.copy();

        for (int slot = 0; slot < storage.size() && !remaining.isEmpty(); slot++) {
            ItemStack existing = storage.get(slot);
            if (existing.isEmpty() || !com.teamsmartstreamlabs.smartbackpacks.fabric.data.ItemStackCompat.isSameItemSameComponents(existing, remaining)) {
                continue;
            }

            int transfer = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
            if (transfer <= 0) {
                continue;
            }

            existing.grow(transfer);
            remaining.shrink(transfer);
        }

        for (int slot = 0; slot < storage.size() && !remaining.isEmpty(); slot++) {
            if (!storage.get(slot).isEmpty()) {
                continue;
            }

            int placed = Math.min(remaining.getCount(), remaining.getMaxStackSize());
            storage.set(slot, remaining.copyWithCount(placed));
            remaining.shrink(placed);
        }

        return remaining;
    }

    private static Optional<SmeltingRecipe> getRecipe(Level level, ItemStack input) {
        if (input.isEmpty()) {
            return Optional.empty();
        }

        RecipeManager recipeManager = level.getServer() != null ? level.getServer().getRecipeManager() : null;
        if (recipeManager == null) {
            return Optional.empty();
        }

        return recipeManager.getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level);
    }

    private static boolean canSmelt(Level level, SmeltingRecipe recipeHolder, ItemStack input, ItemStack output) {
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

    private static void smelt(Level level, SmeltingRecipe recipeHolder, NonNullList<ItemStack> items) {
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
    }
}

