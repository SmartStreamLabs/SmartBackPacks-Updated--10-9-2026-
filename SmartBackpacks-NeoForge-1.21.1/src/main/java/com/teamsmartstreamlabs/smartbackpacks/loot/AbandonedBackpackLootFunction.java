package com.teamsmartstreamlabs.smartbackpacks.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModLootFunctions;

public record AbandonedBackpackLootFunction(AbandonedBackpackProfile profile) implements LootItemFunction {
    public static final MapCodec<AbandonedBackpackLootFunction> CODEC = Codec.STRING.fieldOf("profile")
            .xmap(id -> new AbandonedBackpackLootFunction(AbandonedBackpackProfile.byId(id)),
                    function -> function.profile().id());

    @Override
    public ItemStack apply(ItemStack original, LootContext context) {
        return AbandonedBackpackGenerator.generate(this.profile, context);
    }

    @Override
    public LootItemFunctionType<? extends LootItemFunction> getType() {
        return ModLootFunctions.ABANDONED_BACKPACK.get();
    }
}
