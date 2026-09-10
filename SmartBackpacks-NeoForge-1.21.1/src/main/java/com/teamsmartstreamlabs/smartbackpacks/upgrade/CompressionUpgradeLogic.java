package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.Optional;

import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public final class CompressionUpgradeLogic {
    private CompressionUpgradeLogic() {
    }

    public static boolean tick(Level level, ItemStack backpack, BackpackTier tier) {
        NonNullList<ItemStack> storage = BackpackStackData.loadStorage(backpack, tier);

        for (int slot = 0; slot < storage.size(); slot++) {
            ItemStack stack = storage.get(slot);
            if (stack.isEmpty()) {
                continue;
            }

            if (tryCompress(level, backpack, storage, slot, 9, 3) || tryCompress(level, backpack, storage, slot, 4, 2)) {
                BackpackStackData.saveStorage(backpack, storage);
                return true;
            }
        }

        return false;
    }

    private static boolean tryCompress(Level level, ItemStack backpack, NonNullList<ItemStack> storage, int sourceSlot, int requiredCount, int gridSize) {
        ItemStack source = storage.get(sourceSlot);
        if (source.getCount() < requiredCount || !ItemLockProtection.canConsumeForUpgrade(backpack, sourceSlot, source)) {
            return false;
        }

        Optional<RecipeHolder<CraftingRecipe>> recipeHolder = getCompactingRecipe(level, source, gridSize);
        if (recipeHolder.isEmpty()) {
            return false;
        }

        ItemStack result = recipeHolder.get().value().assemble(createInput(source, gridSize), level.registryAccess());
        if (result.isEmpty()) {
            return false;
        }

        NonNullList<ItemStack> simulated = copyStorage(storage);
        ItemStack simulatedSource = simulated.get(sourceSlot).copy();
        simulatedSource.shrink(requiredCount);
        simulated.set(sourceSlot, simulatedSource.isEmpty() ? ItemStack.EMPTY : simulatedSource);

        ItemStack remainder = insertIntoStorage(backpack, simulated, result);
        if (!remainder.isEmpty()) {
            return false;
        }

        for (int slot = 0; slot < storage.size(); slot++) {
            storage.set(slot, simulated.get(slot));
        }
        return true;
    }

    private static Optional<RecipeHolder<CraftingRecipe>> getCompactingRecipe(Level level, ItemStack source, int gridSize) {
        CraftingInput input = createInput(source, gridSize);
        RecipeManager recipeManager = level.getServer() != null ? level.getServer().getRecipeManager() : null;
        if (recipeManager == null) {
            return Optional.empty();
        }

        Optional<RecipeHolder<CraftingRecipe>> recipe = recipeManager.getRecipeFor(RecipeType.CRAFTING, input, level);
        if (recipe.isEmpty()) {
            return Optional.empty();
        }

        ItemStack result = recipe.get().value().assemble(input, level.registryAccess());
        return result.isEmpty() ? Optional.empty() : recipe;
    }

    private static CraftingInput createInput(ItemStack source, int gridSize) {
        NonNullList<ItemStack> items = NonNullList.withSize(gridSize * gridSize, ItemStack.EMPTY);
        for (int index = 0; index < items.size(); index++) {
            items.set(index, source.copyWithCount(1));
        }
        return CraftingInput.of(gridSize, gridSize, items);
    }

    private static NonNullList<ItemStack> copyStorage(NonNullList<ItemStack> storage) {
        NonNullList<ItemStack> copy = NonNullList.withSize(storage.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < storage.size(); slot++) {
            copy.set(slot, storage.get(slot).copy());
        }
        return copy;
    }

    private static ItemStack insertIntoStorage(ItemStack backpack, NonNullList<ItemStack> storage, ItemStack incoming) {
        ItemStack remaining = incoming.copy();

        for (int slot = 0; slot < storage.size() && !remaining.isEmpty(); slot++) {
            ItemStack existing = storage.get(slot);
            if (existing.isEmpty()
                    || !ItemStack.isSameItemSameComponents(existing, remaining)
                    || !ItemLockProtection.canAutomationInsert(backpack, slot, existing, remaining)) {
                continue;
            }

            int transfer = Math.min(remaining.getCount(), BackpackStackData.getStorageStackLimit(backpack, existing) - existing.getCount());
            if (transfer <= 0) {
                continue;
            }

            existing.grow(transfer);
            remaining.shrink(transfer);
        }

        for (int slot = 0; slot < storage.size() && !remaining.isEmpty(); slot++) {
            ItemStack existing = storage.get(slot);
            if (!existing.isEmpty() || !ItemLockProtection.canAutomationInsert(backpack, slot, existing, remaining)) {
                continue;
            }

            int placed = Math.min(remaining.getCount(), BackpackStackData.getStorageStackLimit(backpack, remaining));
            storage.set(slot, remaining.copyWithCount(placed));
            remaining.shrink(placed);
        }

        return remaining;
    }
}
