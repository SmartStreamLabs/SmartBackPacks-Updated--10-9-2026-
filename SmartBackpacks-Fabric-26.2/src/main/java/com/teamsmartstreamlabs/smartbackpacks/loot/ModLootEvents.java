package com.teamsmartstreamlabs.smartbackpacks.loot;

import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public final class ModLootEvents {
    private ModLootEvents() {
    }

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            AbandonedBackpackProfile profile = AbandonedBackpackProfile.forChest(key);
            if (profile != null) {
                tableBuilder.withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ModItems.LEATHER_BACKPACK.get())
                                .apply(() -> new AbandonedBackpackLootFunction(profile))));
            }
            if (!key.equals(BuiltInLootTables.SPAWN_BONUS_CHEST)) return;

            tableBuilder.withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(LootItem.lootTableItem(ModItems.LEATHER_BACKPACK.get())));
        });
    }
}
