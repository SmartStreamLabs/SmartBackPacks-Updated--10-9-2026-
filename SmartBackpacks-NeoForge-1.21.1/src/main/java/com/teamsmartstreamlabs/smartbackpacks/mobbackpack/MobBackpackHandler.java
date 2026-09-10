package com.teamsmartstreamlabs.smartbackpacks.mobbackpack;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.network.MobBackpackSyncPayload;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModItems;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class MobBackpackHandler {
    private static final Set<String> WARNED_INVALID_COLORS = ConcurrentHashMap.newKeySet();

    private MobBackpackHandler() {
    }

    public static void onSpawnPositionCheck(MobSpawnEvent.PositionCheck event) {
        if (event.getLevel() instanceof ServerLevel level) {
            processSpawn(level, event.getEntity(), event.getSpawnType());
        }
    }

    public static void processSpawn(ServerLevel level, Mob mob, MobSpawnType spawnType) {
        MobBackpackType type = MobBackpackType.from(mob);
        if (type == null || MobBackpackStore.get(mob).processed()) {
            return;
        }

        MobBackpackStore.set(mob, new MobBackpackState(true, false, ItemStack.EMPTY));
        if (!MobBackpackRules.canRoll(SmartBackpacksConfig.mobBackpacksEnabled(),
                SmartBackpacksConfig.mobBackpackMobEnabled(type),
                SmartBackpacksConfig.mobBackpackNaturalSpawnsOnly(), isAllowedNaturalSpawn(spawnType))) {
            return;
        }

        double configuredChance = SmartBackpacksConfig.mobBackpackMobChance(type);
        double chance = configuredChance >= 0.0D ? configuredChance : SmartBackpacksConfig.mobBackpackSpawnChance();
        if (!MobBackpackRules.wins(level.getRandom().nextDouble(), chance)) {
            return;
        }

        assignBackpack(level, mob, type);
    }

    public static boolean assignGuaranteed(ServerLevel level, Mob mob) {
        MobBackpackType type = MobBackpackType.from(mob);
        if (type == null) {
            return false;
        }
        assignBackpack(level, mob, type);
        return true;
    }

    private static void assignBackpack(ServerLevel level, Mob mob, MobBackpackType type) {
        TierChoice choice = chooseTier(level);
        ItemStack backpack = new ItemStack(choice.item());
        applyColor(backpack, configuredColor(type));
        fillLoot(level, mob, type, backpack, choice.tier());
        MobBackpackStore.set(mob, new MobBackpackState(true, false, backpack));
    }

    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getTarget() instanceof Mob mob) {
            MobBackpackState state = MobBackpackStore.get(mob);
            ItemStack displayStack = state.dropped() ? ItemStack.EMPTY : state.backpack();
            PacketDistributor.sendToPlayer(player, new MobBackpackSyncPayload(mob.getId(), displayStack));
        }
    }

    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        MobBackpackState state = MobBackpackStore.get(mob);
        if (!state.processed() || state.dropped() || state.backpack().isEmpty()) {
            return;
        }

        MobBackpackStore.set(mob, state.withDropped());
        if (MobBackpackRules.wins(mob.getRandom().nextDouble(), SmartBackpacksConfig.mobBackpackDropChance())) {
            event.getDrops().add(new ItemEntity(mob.level(), mob.getX(), mob.getY(), mob.getZ(), state.backpack().copy()));
        }
    }

    public static boolean isAllowedNaturalSpawn(MobSpawnType spawnType) {
        return spawnType == MobSpawnType.NATURAL
                || spawnType == MobSpawnType.CHUNK_GENERATION
                || spawnType == MobSpawnType.PATROL;
    }

    private static TierChoice chooseTier(ServerLevel level) {
        int leather = SmartBackpacksConfig.mobBackpackTierWeight(BackpackTier.LEATHER);
        int coal = SmartBackpacksConfig.mobBackpackTierWeight(BackpackTier.COAL);
        int copper = SmartBackpacksConfig.mobBackpackTierWeight(BackpackTier.COPPER);
        int iron = SmartBackpacksConfig.mobBackpackTierWeight(BackpackTier.IRON);
        int gold = SmartBackpacksConfig.mobBackpackTierWeight(BackpackTier.GOLD);
        int emerald = SmartBackpacksConfig.mobBackpackTierWeight(BackpackTier.EMERALD);
        int total = leather + coal + copper + iron + gold + emerald;
        if (total <= 0) return new TierChoice(ModItems.LEATHER_BACKPACK.get(), BackpackTier.LEATHER);

        int roll = level.getRandom().nextInt(total);
        if ((roll -= leather) < 0) return new TierChoice(ModItems.LEATHER_BACKPACK.get(), BackpackTier.LEATHER);
        if ((roll -= coal) < 0) return new TierChoice(ModItems.COAL_BACKPACK.get(), BackpackTier.COAL);
        if ((roll -= copper) < 0) return new TierChoice(ModItems.COPPER_BACKPACK.get(), BackpackTier.COPPER);
        if ((roll -= iron) < 0) return new TierChoice(ModItems.IRON_BACKPACK.get(), BackpackTier.IRON);
        if ((roll -= gold) < 0) return new TierChoice(ModItems.GOLD_BACKPACK.get(), BackpackTier.GOLD);
        return new TierChoice(ModItems.EMERALD_BACKPACK.get(), BackpackTier.EMERALD);
    }

    private static DyeColor configuredColor(MobBackpackType type) {
        String configured = SmartBackpacksConfig.mobBackpackMobColor(type);
        return resolveConfiguredColor(configured, type);
    }

    public static DyeColor resolveConfiguredColor(String configured, MobBackpackType type) {
        for (DyeColor color : DyeColor.values()) {
            if (color.getName().equalsIgnoreCase(configured)) return color;
        }
        if (WARNED_INVALID_COLORS.add(type.id() + ":" + configured)) {
            SmartBackpacks.LOGGER.warn("Invalid Mob Backpacks color '{}' for {}; using '{}'",
                    configured, type.id(), type.defaultColor().getName());
        }
        return type.defaultColor();
    }

    private static void applyColor(ItemStack backpack, DyeColor color) {
        backpack.set(DataComponents.DYED_COLOR, new DyedItemColor(color.getTextureDiffuseColor(), true));
    }

    private static void fillLoot(ServerLevel level, Mob mob, MobBackpackType type,
            ItemStack backpack, BackpackTier tier) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SmartBackpacks.MOD_ID, "mob_backpacks/" + type.id());
        LootTable table = level.getServer().reloadableRegistries().getLootTable(
                ResourceKey.create(Registries.LOOT_TABLE, id));
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, mob.position())
                .withParameter(LootContextParams.THIS_ENTITY, mob)
                .create(LootContextParamSets.CHEST);
        List<ItemStack> generated = table.getRandomItems(params);
        int min = SmartBackpacksConfig.mobBackpackMinLootEntries();
        int max = Math.max(min, SmartBackpacksConfig.mobBackpackMaxLootEntries());
        int wanted = min + level.getRandom().nextInt(max - min + 1);
        for (int index = 0; index < Math.min(wanted, generated.size()); index++) {
            BackpackStackData.insertIntoStorage(backpack, tier, generated.get(index), false);
        }
    }

    private record TierChoice(Item item, BackpackTier tier) {
    }
}
