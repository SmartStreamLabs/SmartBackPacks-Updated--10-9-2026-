package com.teamsmartstreamlabs.smartbackpacks.loot;

import java.util.List;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.inventory.BackpackUpgradeInventory;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public final class AbandonedBackpackGenerator {
    private AbandonedBackpackGenerator() {
    }

    public static ItemStack generate(AbandonedBackpackProfile profile, LootContext context) {
        RandomSource random = context.getRandom();
        if (!SmartBackpacksConfig.abandonedBackpacksEnabled()
                || random.nextDouble() >= SmartBackpacksConfig.abandonedBackpackChance(profile)) {
            return ItemStack.EMPTY;
        }

        return generateForCamp(profile, context);
    }

    public static ItemStack generateForCamp(AbandonedBackpackProfile profile, LootContext context) {
        RandomSource random = context.getRandom();

        BackpackTier tier = profile.chooseTier(random);
        ItemStack backpack = new ItemStack(itemFor(tier));
        DyeColor[] colors = DyeColor.values();
        backpack.set(DataComponents.DYED_COLOR,
                new DyedItemColor(colors[random.nextInt(colors.length)].getTextureDiffuseColor()));
        fillStorage(profile, context, backpack, tier);

        if (random.nextDouble() < SmartBackpacksConfig.abandonedBackpackUpgradeChance()) {
            Item[] options = profile.isEarly()
                    ? new Item[] {ModItems.PICKUP_UPGRADE.get(), ModItems.LIGHT_UPGRADE.get()}
                    : profile.isEndgame()
                            ? new Item[] {ModItems.MAGNET_UPGRADE.get(), ModItems.NIGHT_VISION_UPGRADE.get(), ModItems.REPAIR_UPGRADE.get()}
                            : new Item[] {ModItems.FILTER_UPGRADE.get(), ModItems.RESTOCK_UPGRADE.get(), ModItems.DEPOSIT_UPGRADE.get()};
            NonNullList<ItemStack> upgrades = NonNullList.withSize(BackpackUpgradeInventory.UPGRADE_SLOT_COUNT, ItemStack.EMPTY);
            upgrades.set(random.nextInt(upgrades.size()), new ItemStack(options[random.nextInt(options.length)]));
            BackpackStackData.saveUpgrades(backpack, upgrades);
        }

        if (random.nextDouble() < SmartBackpacksConfig.abandonedBackpackCustomNameChance()) {
            backpack.set(DataComponents.CUSTOM_NAME, Component.literal(profile.customName()));
        }
        boolean prefilled = BackpackStackData.loadStorage(backpack, tier).stream().anyMatch(stack -> !stack.isEmpty());
        AbandonedBackpackOrigin.mark(backpack, context.getOptionalParameter(LootContextParams.BLOCK_ENTITY) == null, prefilled);
        return backpack;
    }

    private static void fillStorage(AbandonedBackpackProfile profile, LootContext context,
            ItemStack backpack, BackpackTier tier) {
        ServerLevel level = context.getLevel();
        RandomSource random = context.getRandom();
        Identifier id = Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID,
                "abandoned_backpacks/" + (profile == AbandonedBackpackProfile.CAMP ? "village" : profile.id()));
        LootTable table = level.getServer().reloadableRegistries()
                .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, id));
        Vec3 origin = context.getOptionalParameter(LootContextParams.ORIGIN);
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, origin == null ? Vec3.ZERO : origin)
                .create(LootContextParamSets.CHEST);
        NonNullList<ItemStack> contents = NonNullList.withSize(tier.getSlotCount(), ItemStack.EMPTY);
        int wanted = Math.min(contents.size(), profile.minSlots()
                + random.nextInt(profile.maxSlots() - profile.minSlots() + 1));
        int filled = 0;
        for (int attempt = 0; attempt < wanted * 4 && filled < wanted; attempt++) {
            List<ItemStack> generated = table.getRandomItems(params, random);
            if (generated.isEmpty()) continue;
            ItemStack item = generated.get(0);
            if (item.isEmpty() || BackpackItem.isBackpack(item)
                    || item.get(DataComponents.CONTAINER) != null
                    || item.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock
                    || !BackpackStackData.isValidStorageItem(backpack, item)) {
                continue;
            }
            int emptySlot = random.nextInt(contents.size() - filled);
            for (int slot = 0; slot < contents.size(); slot++) {
                if (!contents.get(slot).isEmpty()) continue;
                if (emptySlot-- == 0) {
                    contents.set(slot, item.copy());
                    filled++;
                    break;
                }
            }
        }
        BackpackStackData.saveStorage(backpack, contents);
    }

    private static Item itemFor(BackpackTier tier) {
        return switch (tier) {
            case LEATHER -> ModItems.LEATHER_BACKPACK.get();
            case COAL -> ModItems.COAL_BACKPACK.get();
            case LAPIS -> ModItems.LAPIS_BACKPACK.get();
            case COPPER -> ModItems.COPPER_BACKPACK.get();
            case IRON -> ModItems.IRON_BACKPACK.get();
            case GOLD -> ModItems.GOLD_BACKPACK.get();
            case EMERALD -> ModItems.EMERALD_BACKPACK.get();
            case DIAMOND -> ModItems.DIAMOND_BACKPACK.get();
            case NETHERITE -> ModItems.NETHERITE_BACKPACK.get();
            default -> throw new IllegalArgumentException("Tier is not allowed as abandoned loot: " + tier);
        };
    }
}
