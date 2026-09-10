package com.teamsmartstreamlabs.smartbackpacks.recipe;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.MapCodec;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModRecipeSerializers;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class BackpackDyeRecipe extends CustomRecipe {
    public static final BackpackDyeRecipe INSTANCE = new BackpackDyeRecipe();
    public static final MapCodec<BackpackDyeRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, BackpackDyeRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<BackpackDyeRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return this.findResult(input) != ItemStack.EMPTY;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return this.findResult(input);
    }

    @Override
    public RecipeSerializer<BackpackDyeRecipe> getSerializer() {
        return ModRecipeSerializers.BACKPACK_DYE.get();
    }

    private ItemStack findResult(CraftingInput input) {
        ItemStack backpack = ItemStack.EMPTY;
        List<DyeColor> dyes = new ArrayList<>();

        for (int index = 0; index < input.size(); index++) {
            ItemStack stack = input.getItem(index);
            if (stack.isEmpty()) {
                continue;
            }

            if (BackpackItem.isBackpack(stack)) {
                if (!backpack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                backpack = stack;
                continue;
            }

            DyeColor dye = stack.get(DataComponents.DYE);
            if (dye == null) {
                return ItemStack.EMPTY;
            }
            dyes.add(dye);
        }

        if (backpack.isEmpty() || dyes.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = backpack.copyWithCount(1);
        result.set(DataComponents.DYED_COLOR, DyedItemColor.applyDyes(backpack.get(DataComponents.DYED_COLOR), dyes));
        return result;
    }
}
