package com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.world.item.crafting;

import net.minecraft.world.item.crafting.Recipe;

import net.minecraft.resources.ResourceLocation;

public record RecipeHolder<T extends Recipe<?>>(ResourceLocation id, T value) {
}
