package com.teamsmartstreamlabs.smartbackpacks.worldgen;

import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

public final class CampTraderOffers {
    private CampTraderOffers() {
    }

    public static void apply(WanderingTrader trader, RandomSource random) {
        trader.getPersistentData().putBoolean("smartbackpacks_camp_trader", true);
        List<Trade> common = new ArrayList<>(List.of(
                new Trade(ModItems.LEATHER_BACKPACK.get(), 12),
                new Trade(ModItems.PICKUP_UPGRADE.get(), 12),
                new Trade(ModItems.FILTER_UPGRADE.get(), 14),
                new Trade(ModItems.DEPOSIT_UPGRADE.get(), 17),
                new Trade(ModItems.RESTOCK_UPGRADE.get(), 17),
                new Trade(ModItems.STORAGE_UPGRADE_I.get(), 18),
                new Trade(Items.LEATHER, 2, 4)));
        List<Trade> uncommon = new ArrayList<>(List.of(
                new Trade(ModItems.MAGNET_UPGRADE.get(), 25),
                new Trade(ModItems.REPAIR_UPGRADE.get(), 30, Items.DIAMOND, 1),
                new Trade(ModItems.NIGHT_VISION_UPGRADE.get(), 25),
                new Trade(ModItems.COMPRESSION_UPGRADE.get(), 24),
                new Trade(ModItems.STORAGE_UPGRADE_II.get(), 29)));
        shuffle(common, random);
        shuffle(uncommon, random);
        var offers = trader.getOffers();
        offers.clear();
        for (int i = 0; i < 4; i++) offers.add(common.get(i).offer());
        for (int i = 0; i < 2; i++) offers.add(uncommon.get(i).offer());
        if (random.nextInt(3) == 0) {
            Trade rare = random.nextBoolean()
                    ? new Trade(ModItems.SOULBOUND_UPGRADE.get(), 44, Items.DIAMOND, 2)
                    : new Trade(ModItems.FALL_PROTECTION_UPGRADE.get(), 42, Items.DIAMOND, 2);
            offers.add(rare.offer());
        }
    }

    private static void shuffle(List<Trade> trades, RandomSource random) {
        for (int i = trades.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Trade previous = trades.get(i);
            trades.set(i, trades.get(j));
            trades.set(j, previous);
        }
    }

    private record Trade(Item item, int emeralds, Item secondary, int secondaryCount, int outputCount) {
        Trade(Item item, int emeralds) { this(item, emeralds, null, 0, 1); }
        Trade(Item item, int emeralds, Item secondary, int secondaryCount) {
            this(item, emeralds, secondary, secondaryCount, 1);
        }
        Trade(Item item, int emeralds, int outputCount) { this(item, emeralds, null, 0, outputCount); }

        MerchantOffer offer() {
            return new MerchantOffer(new ItemCost(Items.EMERALD, emeralds),
                    secondary == null ? Optional.empty() : Optional.of(new ItemCost(secondary, secondaryCount)),
                    new ItemStack(item, outputCount), 4, 2, 0.05F);
        }
    }
}
