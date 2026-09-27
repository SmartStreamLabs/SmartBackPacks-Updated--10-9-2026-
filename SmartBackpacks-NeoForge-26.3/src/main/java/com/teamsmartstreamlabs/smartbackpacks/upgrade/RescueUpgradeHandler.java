package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.RescueUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.teamsmartstreamlabs.smartbackpacks.util.PlayerMessageHelper;

public final class RescueUpgradeHandler {
    public static final TagKey<Item> RESCUE_TOTEMS = itemTag("rescue_totems");
    public static final TagKey<Item> RESCUE_FOODS = itemTag("rescue_foods");
    public static final TagKey<Item> RESCUE_GOLDEN_APPLES = itemTag("rescue_golden_apples");
    public static final TagKey<Item> RESCUE_WATER_ITEMS = itemTag("rescue_water_items");
    public static final TagKey<Item> RESCUE_ITEM_BLACKLIST = itemTag("rescue_item_blacklist");

    private static final int CHECK_INTERVAL = 1;
    private static final java.util.Map<UUID, Long> LAST_RESCUE_TICK = new java.util.HashMap<>();

    private RescueUpgradeHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || !player.isAlive() || isBypassMode(player)) {
            return;
        }

        RescueBackpack backpack = findRescueBackpack(player);
        if (backpack == null) {
            return;
        }

        RescueUpgradeData data = backpack.data().tickCooldowns();
        if (data != backpack.data()) {
            backpack = backpack.withData(data);
            backpack.save();
        }

        if (player.tickCount % CHECK_INTERVAL != 0 || !data.enabled() || !data.globalReady()) {
            return;
        }

        if (tryLavaRescue(player, backpack)) {
            return;
        }
        if (tryFallRescue(player, backpack)) {
            return;
        }
        tryGoldenAppleRescue(player, backpack, false);
    }

    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || isBypassMode(player)) {
            return;
        }
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY) || handledThisTick(player)) {
            return;
        }

        RescueBackpack backpack = findRescueBackpack(player);
        if (backpack == null) {
            return;
        }

        RescueUpgradeData data = backpack.data();
        if (!data.enabled() || !data.globalReady()) {
            return;
        }

        float incoming = Math.max(0.0F, event.getNewDamage());
        float healthPool = player.getHealth() + player.getAbsorptionAmount();
        if (incoming >= healthPool) {
            if (!hasVanillaHandTotem(player) && tryTotemRescue(player, backpack)) {
                event.setNewDamage(0.0F);
            }
            return;
        }

        float damageAfterAbsorption = Math.max(0.0F, incoming - player.getAbsorptionAmount());
        float projectedHealth = player.getHealth() - damageAfterAbsorption;
        if (projectedHealth <= data.healthThreshold()) {
            tryGoldenAppleRescue(player, backpack, true);
        }
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || isBypassMode(player)) {
            return;
        }
        if (!SmartBackpacksConfig.rescueAllowBackpackTotem() || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        if (handledThisTick(player)) {
            return;
        }

        RescueBackpack backpack = findRescueBackpack(player);
        if (backpack == null || hasVanillaHandTotem(player) || !tryTotemRescue(player, backpack)) {
            return;
        }

        event.setCanceled(true);
    }

    private static boolean tryTotemRescue(ServerPlayer player, RescueBackpack backpack) {
        if (!SmartBackpacksConfig.rescueAllowBackpackTotem()) {
            return false;
        }

        RescueUpgradeData data = backpack.data();
        NonNullList<ItemStack> items = data.loadItems();
        int totemSlot = findSlot(items, RescueUpgradeHandler::isTotem);
        if (!data.enabled() || !data.globalReady() || !data.totemEnabled() || data.totemCooldownTicks() > 0 || totemSlot < 0) {
            return false;
        }

        ItemStack totem = items.get(totemSlot);
        totem.shrink(1);
        if (totem.isEmpty()) {
            items.set(totemSlot, ItemStack.EMPTY);
        }

        player.setHealth(1.0F);
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
        player.level().broadcastEntityEvent(player, (byte) 35);

        RescueUpgradeData next = data.withItems(items).withTotemCooldowns(
                SmartBackpacksConfig.rescueGlobalCooldownTicks(), SmartBackpacksConfig.rescueTotemCooldownTicks());
        backpack.withData(next).save();
        markHandled(player);
        notify(player, Component.translatable("message.smartbackpacks.rescue_totem_used"), SoundEvents.TOTEM_USE);
        return true;
    }

    public static boolean isSupportedRescueItem(ItemStack stack) {
        return !stack.isEmpty()
                && !stack.is(RESCUE_ITEM_BLACKLIST)
                && (isTotem(stack) || isGoldenApple(stack) || isWaterItem(stack) || stack.is(RESCUE_FOODS));
    }

    private static boolean tryGoldenAppleRescue(ServerPlayer player, RescueBackpack backpack, boolean thresholdAlreadyMatched) {
        RescueUpgradeData data = backpack.data();
        if (!SmartBackpacksConfig.rescueAllowGoldenApple()
                || !data.goldenAppleEnabled()
                || data.goldenAppleCooldownTicks() > 0
                || (!thresholdAlreadyMatched && player.getHealth() > data.healthThreshold())) {
            return false;
        }

        NonNullList<ItemStack> items = data.loadItems();
        int appleSlot = findSlot(items, RescueUpgradeHandler::canUseGoldenApple);
        if (appleSlot < 0) {
            return false;
        }

        ItemStack apple = items.get(appleSlot);
        ItemStack singleUse = apple.copyWithCount(1);
        ItemStack remainder = singleUse.finishUsingItem(player.level(), player);
        apple.shrink(1);
        if (apple.isEmpty()) {
            items.set(appleSlot, ItemStack.EMPTY);
        }
        if (!remainder.isEmpty()) {
            insertRemainder(player, backpack, items, remainder);
        }

        RescueUpgradeData next = data.withItems(items).withGoldenAppleCooldowns(
                SmartBackpacksConfig.rescueGlobalCooldownTicks(), SmartBackpacksConfig.rescueGoldenAppleCooldownTicks());
        backpack.withData(next).save();
        markHandled(player);
        notify(player, Component.translatable("message.smartbackpacks.rescue_golden_apple_used"), SoundEvents.GENERIC_EAT.value());
        return true;
    }

    private static boolean tryFallRescue(ServerPlayer player, RescueBackpack backpack) {
        RescueUpgradeData data = backpack.data();
        if (!SmartBackpacksConfig.rescueAllowFall()
                || !data.fallEnabled()
                || data.fallCooldownTicks() > 0
                || player.isFallFlying()
                || player.getAbilities().flying
                || player.onGround()
                || player.isInWater()
                || player.fallDistance < data.minimumFallDistance()
                || findSlot(data.loadItems(), RescueUpgradeHandler::isWaterItem) < 0) {
            return false;
        }

        BlockPos waterPos = findFallWaterPlacement(player, data.minimumFallDistance());
        return waterPos != null && placeWaterFromRescueSlot(player, backpack, waterPos,
                data.withFallCooldowns(SmartBackpacksConfig.rescueGlobalCooldownTicks(), SmartBackpacksConfig.rescueFallCooldownTicks()),
                Component.translatable("message.smartbackpacks.rescue_water_fall_used"));
    }

    private static boolean tryLavaRescue(ServerPlayer player, RescueBackpack backpack) {
        RescueUpgradeData data = backpack.data();
        if (!SmartBackpacksConfig.rescueAllowLava()
                || !data.lavaEnabled()
                || data.lavaCooldownTicks() > 0
                || (!player.isInLava() && !(SmartBackpacksConfig.rescueAllowFire() && player.isOnFire() && player.getHealth() <= data.lavaHealthThreshold()))
                || player.getHealth() > data.lavaHealthThreshold()
                || findSlot(data.loadItems(), RescueUpgradeHandler::isWaterItem) < 0) {
            return false;
        }

        BlockPos waterPos = findLavaWaterPlacement(player);
        return waterPos != null && placeWaterFromRescueSlot(player, backpack, waterPos,
                data.withLavaCooldowns(SmartBackpacksConfig.rescueGlobalCooldownTicks(), SmartBackpacksConfig.rescueLavaCooldownTicks()),
                Component.translatable(player.isInLava()
                        ? "message.smartbackpacks.rescue_lava_water_used"
                        : "message.smartbackpacks.rescue_fire_extinguished"));
    }

    private static boolean placeWaterFromRescueSlot(ServerPlayer player, RescueBackpack backpack, BlockPos pos, RescueUpgradeData cooldownData, Component message) {
        RescueUpgradeData data = backpack.data();
        NonNullList<ItemStack> items = data.loadItems();
        int waterSlot = findSlot(items, RescueUpgradeHandler::isWaterItem);
        if (waterSlot < 0 || !canPlaceWater(player, pos)) {
            return false;
        }

        ServerLevel level = player.level();
        if (!level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState())) {
            return false;
        }

        ItemStack water = items.get(waterSlot);
        water.shrink(1);
        if (water.isEmpty()) {
            items.set(waterSlot, ItemStack.EMPTY);
        }
        insertRemainder(player, backpack, items, new ItemStack(Items.BUCKET));
        player.clearFire();
        player.resetFallDistance();

        RescueUpgradeData next = cooldownData.withItems(items);
        backpack.withData(next).save();
        markHandled(player);
        notify(player, message, SoundEvents.BUCKET_EMPTY);
        return true;
    }

    private static BlockPos findFallWaterPlacement(ServerPlayer player, int minimumFallDistance) {
        int scan = Math.min(SmartBackpacksConfig.rescueMaximumFallScanDistance(), Math.max(minimumFallDistance + 8, 16));
        BlockPos start = player.blockPosition();
        for (int offset = 1; offset <= scan; offset++) {
            BlockPos ground = start.below(offset);
            if (!player.level().isLoaded(ground)) {
                return null;
            }

            BlockState groundState = player.level().getBlockState(ground);
            if (!groundState.isAir() && !groundState.liquid()) {
                BlockPos target = ground.above();
                int distanceToTarget = start.getY() - target.getY();
                if (distanceToTarget <= 8 && distanceToTarget >= 0 && canPlaceWater(player, target)) {
                    return target;
                }
                return null;
            }
        }
        return null;
    }

    private static BlockPos findLavaWaterPlacement(ServerPlayer player) {
        BlockPos base = player.blockPosition();
        if (canPlaceWater(player, base)) {
            return base;
        }

        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-1, -1, -1), base.offset(1, 1, 1))) {
            BlockPos immutable = pos.immutable();
            if (canPlaceWater(player, immutable)) {
                return immutable;
            }
        }
        return null;
    }

    private static boolean canPlaceWater(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.level();
        if (!level.isLoaded(pos) || !level.mayInteract(player, pos)) {
            return false;
        }
        if (SmartBackpacksConfig.rescueRespectDimensionWaterRestrictions() && level.dimension() == Level.NETHER) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.liquid() || state.canBeReplaced(Fluids.WATER);
    }

    private static void insertRemainder(ServerPlayer player, RescueBackpack backpack, NonNullList<ItemStack> items, ItemStack remainder) {
        if (remainder.isEmpty()) {
            return;
        }

        for (int slot = 0; slot < items.size() && !remainder.isEmpty(); slot++) {
            ItemStack existing = items.get(slot);
            if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, remainder)) {
                continue;
            }

            int transfer = Math.min(remainder.getCount(), existing.getMaxStackSize() - existing.getCount());
            if (transfer > 0) {
                existing.grow(transfer);
                remainder.shrink(transfer);
            }
        }

        for (int slot = 0; slot < items.size() && !remainder.isEmpty(); slot++) {
            if (!items.get(slot).isEmpty()) {
                continue;
            }

            int transfer = Math.min(remainder.getCount(), remainder.getMaxStackSize());
            items.set(slot, remainder.copyWithCount(transfer));
            remainder.shrink(transfer);
        }

        if (!remainder.isEmpty()) {
            ItemStack leftover = ItemLockProtection.insertIntoStorage(backpack.backpack(), backpack.tier(), remainder.copy(), false);
            remainder.setCount(leftover.getCount());
        }
        if (!remainder.isEmpty() && !player.getInventory().add(remainder.copy())) {
            player.drop(remainder.copy(), false, net.minecraft.util.Prediction.SERVER_ONLY);
        }
    }

    private static RescueBackpack findRescueBackpack(ServerPlayer player) {
        RescueBackpack chest = createRescueBackpack(player.getItemBySlot(EquipmentSlot.CHEST), updated -> player.setItemSlot(EquipmentSlot.CHEST, updated));
        if (chest != null) {
            return chest;
        }

        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            int curioSlot = slot;
            RescueBackpack backpack = createRescueBackpack(CuriosCompat.getBackStack(player, curioSlot),
                    updated -> CuriosCompat.setBackStack(player, curioSlot, updated));
            if (backpack != null) {
                return backpack;
            }
        }

        RescueBackpack offhand = createRescueBackpack(player.getOffhandItem(), updated -> player.getInventory().setChanged());
        if (offhand != null) {
            return offhand;
        }

        for (int slot = 0; slot < 36; slot++) {
            int inventorySlot = slot;
            RescueBackpack backpack = createRescueBackpack(player.getInventory().getItem(slot), updated -> {
                player.getInventory().setItem(inventorySlot, updated);
                player.getInventory().setChanged();
            });
            if (backpack != null) {
                return backpack;
            }
        }

        return null;
    }

    private static RescueBackpack createRescueBackpack(ItemStack backpack, Consumer<ItemStack> saver) {
        if (!(backpack.getItem() instanceof BackpackItem backpackItem)) {
            return null;
        }

        NonNullList<ItemStack> upgrades = BackpackStackData.loadUpgrades(backpack);
        for (int slot = 0; slot < upgrades.size(); slot++) {
            ItemStack upgrade = upgrades.get(slot);
            if (upgrade.getItem() instanceof RescueUpgradeItem) {
                RescueUpgradeData data = upgrade.getOrDefault(ModDataComponents.RESCUE_UPGRADE_DATA.get(), RescueUpgradeData.DEFAULT);
                return new RescueBackpack(backpack, backpackItem.getTier(), upgrades, slot, data, saver);
            }
        }
        return null;
    }

    private static boolean isTotem(ItemStack stack) {
        return stack.is(Items.TOTEM_OF_UNDYING) || stack.is(RESCUE_TOTEMS);
    }

    private static boolean isGoldenApple(ItemStack stack) {
        return stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE) || stack.is(RESCUE_GOLDEN_APPLES);
    }

    private static boolean canUseGoldenApple(ItemStack stack) {
        return isGoldenApple(stack) && (!stack.is(Items.ENCHANTED_GOLDEN_APPLE) || SmartBackpacksConfig.rescueAllowEnchantedGoldenApple());
    }

    private static boolean isWaterItem(ItemStack stack) {
        return stack.is(Items.WATER_BUCKET) || stack.is(RESCUE_WATER_ITEMS);
    }

    private static boolean isBypassMode(Player player) {
        return player.isCreative() || player.isSpectator();
    }

    private static boolean hasVanillaHandTotem(Player player) {
        return isTotem(player.getMainHandItem()) || isTotem(player.getOffhandItem());
    }

    private static int findSlot(NonNullList<ItemStack> items, Predicate<ItemStack> predicate) {
        for (int slot = 0; slot < items.size(); slot++) {
            if (predicate.test(items.get(slot))) {
                return slot;
            }
        }
        return -1;
    }

    private static boolean handledThisTick(ServerPlayer player) {
        return LAST_RESCUE_TICK.getOrDefault(player.getUUID(), Long.MIN_VALUE) == player.level().getGameTime();
    }

    private static void markHandled(ServerPlayer player) {
        LAST_RESCUE_TICK.put(player.getUUID(), player.level().getGameTime());
    }

    private static void notify(ServerPlayer player, Component message, net.minecraft.sounds.SoundEvent sound) {
        if (SmartBackpacksConfig.rescueEnableNotifications()) {
            PlayerMessageHelper.sendStatus(player, message);
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 0.75F, 1.0F);
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, path));
    }

    private record RescueBackpack(
            ItemStack backpack,
            BackpackTier tier,
            NonNullList<ItemStack> upgrades,
            int upgradeSlot,
            RescueUpgradeData data,
            Consumer<ItemStack> saver) {
        private RescueBackpack withData(RescueUpgradeData data) {
            return new RescueBackpack(this.backpack, this.tier, this.upgrades, this.upgradeSlot, data, this.saver);
        }

        private void save() {
            ItemStack upgrade = this.upgrades.get(this.upgradeSlot);
            upgrade.set(ModDataComponents.RESCUE_UPGRADE_DATA.get(), this.data);
            this.upgrades.set(this.upgradeSlot, upgrade);
            BackpackStackData.saveUpgrades(this.backpack, this.upgrades);
            this.saver.accept(this.backpack);
        }
    }
}
