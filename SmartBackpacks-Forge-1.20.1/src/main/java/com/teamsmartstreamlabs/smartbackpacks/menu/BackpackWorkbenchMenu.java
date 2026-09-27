package com.teamsmartstreamlabs.smartbackpacks.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.recipe.BackpackWorkbenchRecipe;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModBlocks;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModMenuTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public final class BackpackWorkbenchMenu extends AbstractContainerMenu {
    public static final int WIDTH = 370;
    public static final int HEIGHT = 240;
    public static final int PLAYER_X = 104;
    public static final int PLAYER_Y = 160;
    public static final int HOTBAR_Y = 218;

    public record DisplayRecipe(String id, ItemStack output, int width, int height,
            List<Optional<Ingredient>> ingredients) {
        private static DisplayRecipe read(RegistryFriendlyByteBuf buffer) {
            String id = buffer.readUtf(256);
            ItemStack output = buffer.readItem();
            int width = buffer.readVarInt();
            int height = buffer.readVarInt();
            List<Optional<Ingredient>> ingredients = new ArrayList<>();
            for (int index = 0, size = buffer.readVarInt(); index < size; index++) {
                ingredients.add(buffer.readBoolean() ? Optional.of(Ingredient.fromNetwork(buffer)) : Optional.empty());
            }
            return new DisplayRecipe(id, output, width, height, List.copyOf(ingredients));
        }

        private void write(RegistryFriendlyByteBuf buffer) {
            buffer.writeUtf(this.id, 256);
            buffer.writeItem(this.output);
            buffer.writeVarInt(this.width);
            buffer.writeVarInt(this.height);
            buffer.writeVarInt(this.ingredients.size());
            for (Optional<Ingredient> ingredient : this.ingredients) {
                buffer.writeBoolean(ingredient.isPresent());
                ingredient.ifPresent(value -> value.toNetwork(buffer));
            }
        }
    }

    private final BlockPos controllerPos;
    private final List<DisplayRecipe> recipes;
    private final Inventory inventory;

    public BackpackWorkbenchMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos(), readRecipes(buffer));
    }

    public BackpackWorkbenchMenu(int id, Inventory inventory, BlockPos controllerPos) {
        this(id, inventory, controllerPos, inventory.player instanceof ServerPlayer serverPlayer
                ? collectRecipes(serverPlayer) : List.of());
    }

    private BackpackWorkbenchMenu(int id, Inventory inventory, BlockPos controllerPos, List<DisplayRecipe> recipes) {
        super(ModMenuTypes.BACKPACK_WORKBENCH.get(), id);
        this.controllerPos = controllerPos;
        this.inventory = inventory;
        this.recipes = recipes;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = column + row * 9 + 9;
                this.addSlot(new Slot(inventory, slot, PLAYER_X + column * 18, PLAYER_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column, PLAYER_X + column * 18, HOTBAR_Y));
        }
    }

    public static void writeContext(RegistryFriendlyByteBuf buffer, ServerPlayer player, BlockPos pos) {
        buffer.writeBlockPos(pos);
        List<DisplayRecipe> recipes = collectRecipes(player);
        buffer.writeVarInt(recipes.size());
        for (DisplayRecipe recipe : recipes) {
            recipe.write(buffer);
        }
    }

    private static List<DisplayRecipe> readRecipes(RegistryFriendlyByteBuf buffer) {
        int count = Math.min(buffer.readVarInt(), 4096);
        List<DisplayRecipe> recipes = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            recipes.add(DisplayRecipe.read(buffer));
        }
        return List.copyOf(recipes);
    }

    private static List<DisplayRecipe> collectRecipes(ServerPlayer player) {
        List<DisplayRecipe> recipes = new ArrayList<>();
        for (Recipe<?> holder : player.server.getRecipeManager().getRecipes()) {
            if (holder instanceof BackpackWorkbenchRecipe recipe) {
                recipes.add(new DisplayRecipe(recipe.getId().toString(),
                        recipe.getResultItem(player.level().registryAccess()).copy(), recipe.width(), recipe.height(),
                        recipe.ingredients().stream().map(value -> value.isEmpty() ? Optional.<Ingredient>empty() : Optional.of(value)).toList()));
            }
        }
        recipes.sort(Comparator.comparing(DisplayRecipe::id));
        return List.copyOf(recipes);
    }

    public List<DisplayRecipe> recipes() {
        return this.recipes;
    }

    public boolean canCraft(DisplayRecipe recipe) {
        return assignIngredients(recipe.ingredients(), this.inventory) != null;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (!(player instanceof ServerPlayer serverPlayer) || !this.stillValid(player)
                || buttonId < 0 || buttonId >= this.recipes.size()) {
            return false;
        }
        DisplayRecipe selected = this.recipes.get(buttonId);
        Optional<? extends Recipe<?>> found = serverPlayer.server.getRecipeManager().byKey(new ResourceLocation(selected.id()));
        if (found.isEmpty() || !(found.get() instanceof BackpackWorkbenchRecipe recipe)) {
            return false;
        }
        List<Optional<Ingredient>> ingredients = recipe.ingredients().stream()
                .map(value -> value.isEmpty() ? Optional.<Ingredient>empty() : Optional.of(value)).toList();
        int[] assigned = assignIngredients(ingredients, serverPlayer.getInventory());
        if (assigned == null) {
            return false;
        }

        List<ItemStack> inputItems = new ArrayList<>(ingredients.size());
        for (int inventorySlot : assigned) {
            ItemStack stack = inventorySlot < 0 ? ItemStack.EMPTY : serverPlayer.getInventory().getItem(inventorySlot).copy();
            if (!stack.isEmpty()) stack.setCount(1);
            inputItems.add(stack);
        }
        TransientCraftingContainer input = new TransientCraftingContainer(this, recipe.width(), recipe.height());
        for (int index = 0; index < inputItems.size(); index++) input.setItem(index, inputItems.get(index));
        if (!recipe.matches(input, serverPlayer.level())) {
            return false;
        }
        ItemStack result = recipe.assemble(input, serverPlayer.level().registryAccess());
        if (result.isEmpty()) {
            return false;
        }
        if (result.getItem() instanceof BackpackItem targetBackpack) {
            for (ItemStack ingredient : inputItems) {
                if (ingredient.getItem() instanceof BackpackItem sourceBackpack
                        && sourceBackpack.getTier() != targetBackpack.getTier()
                        && sourceBackpack.getTier().getSlotCount() <= targetBackpack.getTier().getSlotCount()) {
                    BackpackItem.copyStorageAndAppearance(ingredient, result);
                    break;
                }
            }
        }
        NonNullList<ItemStack> remainders = recipe.getRemainingItems(input);

        // The server thread serializes all inventory mutations; no client-provided stack is accepted.
        for (int inventorySlot : assigned) {
            if (inventorySlot >= 0) {
                serverPlayer.getInventory().getItem(inventorySlot).shrink(1);
            }
        }
        serverPlayer.getInventory().setChanged();
        int craftedCount = result.getCount();
        boolean modItem = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(result.getItem()).getNamespace().equals("smartbackpacks");
        giveOrDrop(serverPlayer, result);
        if (modItem) com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.grant(serverPlayer, "backpack_engineer");
        com.teamsmartstreamlabs.smartbackpacks.progress.BackpackProgression.add(serverPlayer, "workbench_crafts", craftedCount);
        for (ItemStack remainder : remainders) {
            if (!remainder.isEmpty()) {
                giveOrDrop(serverPlayer, remainder.copy());
            }
        }
        this.broadcastChanges();
        return true;
    }

    private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        player.getInventory().add(stack);
        if (!stack.isEmpty()) {
            player.drop(stack, false);
        }
    }

    private static int[] assignIngredients(List<Optional<Ingredient>> ingredients, Inventory inventory) {
        int[] assigned = new int[ingredients.size()];
        Arrays.fill(assigned, -1);
        return assignNext(ingredients, inventory, new int[36], assigned, 0) ? assigned : null;
    }

    private static boolean assignNext(List<Optional<Ingredient>> ingredients, Inventory inventory,
            int[] used, int[] assigned, int index) {
        if (index == ingredients.size()) {
            return true;
        }
        Optional<Ingredient> ingredient = ingredients.get(index);
        if (ingredient.isEmpty()) {
            return assignNext(ingredients, inventory, used, assigned, index + 1);
        }
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (used[slot] >= stack.getCount() || !ingredient.get().test(stack)) {
                continue;
            }
            used[slot]++;
            assigned[index] = slot;
            if (assignNext(ingredients, inventory, used, assigned, index + 1)) {
                return true;
            }
            used[slot]--;
            assigned[index] = -1;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(ContainerLevelAccess.create(player.level(), this.controllerPos),
                player, ModBlocks.BACKPACK_WORKBENCH.get());
    }
}
