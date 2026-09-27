package com.teamsmartstreamlabs.smartbackpacks.loot;

import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;
import net.neoforged.neoforge.event.LootTableLoadEvent;

public final class ModLootEvents {
    private static final String BONUS_CHEST_POOL = "smartbackpacks_bonus_chest";
    private static final String ABANDONED_POOL = "smartbackpacks_abandoned_backpack";

    private ModLootEvents() {
    }

    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (event.getKey().equals(BuiltInLootTables.SPAWN_BONUS_CHEST)
                && event.getTable().getPool(BONUS_CHEST_POOL) == null) {
            event.getTable().addPool(LootPool.lootPool()
                    .name(BONUS_CHEST_POOL)
                    .setRolls(Holder.direct(new ConstantValue(1)))
                    .add(LootItem.lootTableItem(ModItems.LEATHER_BACKPACK.get()))
                    .build());
        }
        AbandonedBackpackProfile profile = AbandonedBackpackProfile.forChest(event.getKey());
        if (profile != null && event.getTable().getPool(ABANDONED_POOL) == null) {
            event.getTable().addPool(LootPool.lootPool()
                    .name(ABANDONED_POOL)
                    .setRolls(Holder.direct(new ConstantValue(1)))
                    .add(LootItem.lootTableItem(ModItems.LEATHER_BACKPACK.get())
                            .apply(Holder.direct(new AbandonedBackpackLootFunction(profile))))
                    .build());
        }
    }
}
