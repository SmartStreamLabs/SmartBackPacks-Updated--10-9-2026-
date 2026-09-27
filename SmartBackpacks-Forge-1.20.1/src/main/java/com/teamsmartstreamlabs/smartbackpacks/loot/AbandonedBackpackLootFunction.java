package com.teamsmartstreamlabs.smartbackpacks.loot;

import com.google.gson.JsonObject;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonSerializationContext;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModLootFunctions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public record AbandonedBackpackLootFunction(AbandonedBackpackProfile profile) implements LootItemFunction {
    public static final Serializer<AbandonedBackpackLootFunction> SERIALIZER = new Serializer<>() {
        @Override
        public void serialize(JsonObject json, AbandonedBackpackLootFunction function, JsonSerializationContext context) {
            json.addProperty("profile", function.profile().id());
        }

        @Override
        public AbandonedBackpackLootFunction deserialize(JsonObject json, JsonDeserializationContext context) {
            return new AbandonedBackpackLootFunction(AbandonedBackpackProfile.byId(json.get("profile").getAsString()));
        }
    };

    @Override
    public ItemStack apply(ItemStack original, LootContext context) {
        return AbandonedBackpackGenerator.generate(this.profile, context);
    }

    @Override
    public LootItemFunctionType getType() {
        return ModLootFunctions.ABANDONED_BACKPACK.get();
    }
}
