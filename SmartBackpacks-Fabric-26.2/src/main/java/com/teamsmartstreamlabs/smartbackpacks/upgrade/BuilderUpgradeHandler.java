package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackTier;
import com.teamsmartstreamlabs.smartbackpacks.compat.CuriosCompat;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BuilderUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.teamsmartstreamlabs.smartbackpacks.util.PlayerMessageHelper;

public final class BuilderUpgradeHandler {
    public static final TagKey<Item> BUILDER_ITEMS = itemTag("builder_items");
    public static final TagKey<Item> BUILDER_REFILL_ALLOWED = itemTag("builder_refill_allowed");
    public static final TagKey<Item> BUILDER_REFILL_BLOCKED = itemTag("builder_refill_blocked");
    public static final TagKey<Item> BUILDER_DANGEROUS_BLOCKS = itemTag("builder_dangerous_blocks");
    public static final TagKey<Item> BUILDER_SCAFFOLDING = itemTag("builder_scaffolding");
    public static final TagKey<Item> BUILDER_CONTAINER_ITEMS = itemTag("builder_container_items");

    private static final int REFILL_REQUEST_LIFETIME_TICKS = 20;
    private static final int FAILURE_FEEDBACK_DELAY_TICKS = 40;
    private static final Map<UUID, PlayerMemory> MEMORIES = new HashMap<>();

    private BuilderUpgradeHandler() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }

        ItemStack handStack = player.getItemInHand(event.getHand());
        if (isPotentialBuilderItem(handStack)) {
            remember(player, event.getHand(), handStack, true);
        }
    }

    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }

        Item placedItem = event.getPlacedBlock().getBlock().asItem();
        if (placedItem == Items.AIR) {
            return;
        }

        markSuccessfulPlacement(player, InteractionHand.MAIN_HAND, placedItem);
        markSuccessfulPlacement(player, InteractionHand.OFF_HAND, placedItem);
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || player.isCreative() || player.isSpectator()) {
            return;
        }
        if (hasContainerOpen(player)) {
            MEMORIES.remove(player.getUUID());
            return;
        }

        handleHandTick(player, InteractionHand.MAIN_HAND);
        handleHandTick(player, InteractionHand.OFF_HAND);
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        MEMORIES.remove(event.getEntity().getUUID());
    }

    public static void requestManualRefill(ServerPlayer player) {
        if (player.level().isClientSide() || player.isCreative() || player.isSpectator() || hasContainerOpen(player)) {
            return;
        }

        tryManualRefill(player, InteractionHand.MAIN_HAND);
        tryManualRefill(player, InteractionHand.OFF_HAND);
    }

    public static boolean isPotentialBuilderItem(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getMaxStackSize() > 1
                && !stack.isDamageableItem()
                && !(stack.getItem() instanceof BackpackItem)
                && !(stack.getItem() instanceof BackpackUpgradeItem)
                && (stack.getItem() instanceof BlockItem || stack.is(BUILDER_ITEMS) || stack.is(BUILDER_SCAFFOLDING));
    }

    private static void handleHandTick(ServerPlayer player, InteractionHand hand) {
        PlayerMemory playerMemory = memory(player);
        HandMemory handMemory = playerMemory.forHand(hand);
        ItemStack current = player.getItemInHand(hand);
        int selectedSlot = selectedSlot(player, hand);
        long gameTime = player.level().getGameTime();

        if (!current.isEmpty()) {
            if (!isPotentialBuilderItem(current)) {
                handMemory.clear();
                return;
            }

            remember(player, hand, current, false);
            BuilderBackpack source = findBuilderBackpack(player);
            if (source == null || !handAllowed(source.data(), hand)) {
                return;
            }

            BuilderRefillMode mode = effectiveMode(source.data().refillMode());
            if (mode == BuilderRefillMode.MANUAL) {
                return;
            }

            boolean recentUse = isRecent(gameTime, handMemory.lastUseTick, REFILL_REQUEST_LIFETIME_TICKS);
            boolean shouldRefill = switch (mode) {
                case WHEN_EMPTY -> false;
                case BELOW_THRESHOLD -> recentUse && current.getCount() < source.data().threshold();
                case KEEP_FULL -> current.getCount() < current.getMaxStackSize()
                        && hasDelayPassed(gameTime, handMemory.lastAttemptTick, SmartBackpacksConfig.builderAntiSpamDelayTicks());
                case MANUAL -> false;
            };
            if (shouldRefill) {
                handMemory.lastAttemptTick = gameTime;
                tryRefillHand(player, hand, current.copyWithCount(1), source, false);
            }
            return;
        }

        if (!handMemory.hasCandidate()
                || handMemory.selectedSlot != selectedSlot
                || !handMemory.hasRecentRefillTrigger(gameTime)
                || !hasDelayPassed(gameTime, handMemory.lastAttemptTick, SmartBackpacksConfig.builderAntiSpamDelayTicks())) {
            return;
        }

        BuilderBackpack source = findBuilderBackpack(player);
        if (source == null || !handAllowed(source.data(), hand) || effectiveMode(source.data().refillMode()) == BuilderRefillMode.MANUAL) {
            sendFailureFeedback(player, handMemory, "message.smartbackpacks.builder_no_backpack");
            return;
        }

        handMemory.lastAttemptTick = gameTime;
        if (tryRefillHand(player, hand, handMemory.candidate, source, false)) {
            handMemory.clear();
        } else {
            sendFailureFeedback(player, handMemory, "message.smartbackpacks.builder_no_match");
        }
    }

    private static void tryManualRefill(ServerPlayer player, InteractionHand hand) {
        BuilderBackpack source = findBuilderBackpack(player);
        if (source == null || !handAllowed(source.data(), hand)) {
            return;
        }

        ItemStack current = player.getItemInHand(hand);
        ItemStack target = current.isEmpty() ? memory(player).forHand(hand).candidate : current;
        if (target.isEmpty()) {
            return;
        }

        tryRefillHand(player, hand, target.copyWithCount(1), source, true);
    }

    private static boolean tryRefillHand(ServerPlayer player, InteractionHand hand, ItemStack target, BuilderBackpack source, boolean manualRequest) {
        BuilderUpgradeData data = source.data();
        if (!SmartBackpacksConfig.builderUpgradeEnabled() || !data.enabled() || !isEligibleBuilderItem(target, data)) {
            return false;
        }

        BuilderRefillMode mode = effectiveMode(data.refillMode());
        ItemStack current = player.getItemInHand(hand);
        if (!current.isEmpty() && !matches(data.matchMode(), current, target)) {
            return false;
        }
        if (!current.isEmpty() && mode == BuilderRefillMode.WHEN_EMPTY && !manualRequest) {
            return false;
        }
        if (!current.isEmpty() && mode == BuilderRefillMode.BELOW_THRESHOLD && current.getCount() >= data.threshold() && !manualRequest) {
            return false;
        }

        int max = target.getMaxStackSize();
        int currentCount = current.isEmpty() ? 0 : current.getCount();
        int needed = max - currentCount;
        if (needed <= 0) {
            return false;
        }

        NonNullList<ItemStack> storage = BackpackStackData.loadStorage(source.backpack(), source.tier());
        for (int slot = 0; slot < storage.size(); slot++) {
            ItemStack stored = storage.get(slot);
            if (stored.isEmpty()
                    || !matches(data.matchMode(), stored, target)
                    || !isEligibleBuilderItem(stored, data)
                    || !filterAllows(data, stored)
                    || !ItemLockProtection.canConsumeForUpgrade(source.backpack(), slot, stored)) {
                continue;
            }

            int transfer = Math.min(needed, stored.getCount());
            if (transfer <= 0) {
                continue;
            }

            ItemStack inserted = stored.copyWithCount(transfer);
            stored.shrink(transfer);
            if (stored.isEmpty()) {
                storage.set(slot, ItemStack.EMPTY);
            }

            ItemStack nextHand = current.isEmpty() ? inserted : current.copy();
            if (!current.isEmpty()) {
                nextHand.grow(transfer);
            }

            player.setItemInHand(hand, nextHand);
            BackpackStackData.saveStorage(source.backpack(), storage);
            source.saver().accept(source.backpack());
            player.getInventory().setChanged();
            player.inventoryMenu.broadcastChanges();
            player.containerMenu.broadcastChanges();
            if (data.feedbackEnabled() && SmartBackpacksConfig.builderFeedbackEnabled()) {
                PlayerMessageHelper.sendStatus(player, Component.translatable("message.smartbackpacks.builder_refilled", transfer, target.getHoverName()));
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.35F, 1.25F);
            }
            return true;
        }

        return false;
    }

    private static BuilderBackpack findBuilderBackpack(ServerPlayer player) {
        BuilderBackpack chest = createBuilderBackpack(player.getItemBySlot(EquipmentSlot.CHEST), backpack -> player.setItemSlot(EquipmentSlot.CHEST, backpack));
        if (chest != null) {
            return chest;
        }

        for (int slot = 0; slot < CuriosCompat.getBackSlotCount(player); slot++) {
            int curioSlot = slot;
            BuilderBackpack backpack = createBuilderBackpack(CuriosCompat.getBackStack(player, curioSlot),
                    updated -> CuriosCompat.setBackStack(player, curioSlot, updated));
            if (backpack != null) {
                return backpack;
            }
        }

        for (int slot = 0; slot < 36; slot++) {
            int inventorySlot = slot;
            BuilderBackpack backpack = createBuilderBackpack(player.getInventory().getItem(slot), updated -> {
                player.getInventory().setItem(inventorySlot, updated);
                player.getInventory().setChanged();
            });
            if (backpack != null) {
                return backpack;
            }
        }

        return createBuilderBackpack(player.getOffhandItem(), backpack -> {
            player.setItemInHand(InteractionHand.OFF_HAND, backpack);
            player.getInventory().setChanged();
        });
    }

    private static BuilderBackpack createBuilderBackpack(ItemStack stack, Consumer<ItemStack> saver) {
        if (!(stack.getItem() instanceof BackpackItem backpackItem)) {
            return null;
        }

        for (ItemStack upgrade : BackpackStackData.loadUpgrades(stack)) {
            if (!(upgrade.getItem() instanceof BuilderUpgradeItem)) {
                continue;
            }

            BuilderUpgradeData data = upgrade.getOrDefault(ModDataComponents.BUILDER_UPGRADE_DATA.get(), BuilderUpgradeData.DEFAULT);
            if (data.refillMode() == BuilderRefillMode.MANUAL) {
                data = data.withRefillMode(BuilderRefillMode.WHEN_EMPTY);
            }
            if (data.enabled() && SmartBackpacksConfig.builderUpgradeEnabled()) {
                return new BuilderBackpack(stack, backpackItem.getTier(), data, saver);
            }
        }
        return null;
    }

    private static boolean isEligibleBuilderItem(ItemStack stack, BuilderUpgradeData data) {
        if (!isPotentialBuilderItem(stack)) {
            return false;
        }
        if (!data.scaffoldingEnabled() && stack.is(BUILDER_SCAFFOLDING)) {
            return false;
        }
        if (!SmartBackpacksConfig.builderAllowModdedBlocks() || !data.moddedBlocksEnabled()) {
            Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (!"minecraft".equals(id.getNamespace())) {
                return false;
            }
        }
        if (!SmartBackpacksConfig.builderAllowContainerRefill()
                && (stack.is(BUILDER_CONTAINER_ITEMS) || stack.has(DataComponents.CONTAINER)
                && !stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).equals(ItemContainerContents.EMPTY))) {
            return false;
        }
        if (data.dangerousProtectionEnabled()
                && !SmartBackpacksConfig.builderAllowDangerousRefill()
                && (stack.is(BUILDER_REFILL_BLOCKED) || stack.is(BUILDER_DANGEROUS_BLOCKS))) {
            return false;
        }

        return !stack.is(Items.WATER_BUCKET) && !stack.is(Items.LAVA_BUCKET) && !stack.is(Items.POWDER_SNOW_BUCKET);
    }

    private static boolean filterAllows(BuilderUpgradeData data, ItemStack stack) {
        if (data.filterMode() == BuilderFilterMode.ALLOW_ALL || stack.is(BUILDER_REFILL_ALLOWED)) {
            return true;
        }

        boolean matched = false;
        for (ItemStack filter : data.loadFilterItems()) {
            if (!filter.isEmpty() && matches(data.matchMode(), stack, filter)) {
                matched = true;
                break;
            }
        }

        return data.filterMode() == BuilderFilterMode.WHITELIST ? matched : !matched;
    }

    private static boolean matches(BuilderMatchMode mode, ItemStack first, ItemStack second) {
        return mode == BuilderMatchMode.ITEM ? first.is(second.getItem()) : ItemStack.isSameItemSameComponents(first, second);
    }

    private static boolean handAllowed(BuilderUpgradeData data, InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND) {
            return SmartBackpacksConfig.builderAllowOffhandRefill() && data.offhandEnabled();
        }
        return data.mainHandEnabled();
    }

    private static BuilderRefillMode effectiveMode(BuilderRefillMode mode) {
        if (mode == BuilderRefillMode.MANUAL) {
            return BuilderRefillMode.WHEN_EMPTY;
        }
        if (mode == BuilderRefillMode.KEEP_FULL && !SmartBackpacksConfig.builderAllowKeepFullMode()) {
            return BuilderRefillMode.BELOW_THRESHOLD;
        }
        return mode;
    }

    private static void markSuccessfulPlacement(ServerPlayer player, InteractionHand hand, Item placedItem) {
        HandMemory memory = memory(player).forHand(hand);
        if (memory.hasCandidate() && memory.candidate.is(placedItem)) {
            memory.lastUseTick = player.level().getGameTime();
            memory.selectedSlot = selectedSlot(player, hand);
            return;
        }

        ItemStack current = player.getItemInHand(hand);
        if (isPotentialBuilderItem(current) && current.is(placedItem)) {
            remember(player, hand, current, true);
        }
    }

    private static void remember(ServerPlayer player, InteractionHand hand, ItemStack stack, boolean used) {
        HandMemory memory = memory(player).forHand(hand);
        memory.candidate = stack.copyWithCount(1);
        memory.selectedSlot = selectedSlot(player, hand);
        memory.lastSeenTick = player.level().getGameTime();
        if (used) {
            memory.lastUseTick = memory.lastSeenTick;
        }
    }

    private static void sendFailureFeedback(ServerPlayer player, HandMemory memory, String translationKey) {
        long gameTime = player.level().getGameTime();
        if (!hasDelayPassed(gameTime, memory.lastFailureFeedbackTick, FAILURE_FEEDBACK_DELAY_TICKS)) {
            return;
        }

        memory.lastFailureFeedbackTick = gameTime;
        PlayerMessageHelper.sendStatus(player, Component.translatable(translationKey, memory.candidate.getHoverName()));
    }

    private static boolean isRecent(long gameTime, long lastTick, int lifetimeTicks) {
        return lastTick != Long.MIN_VALUE && gameTime - lastTick <= lifetimeTicks;
    }

    private static boolean hasDelayPassed(long gameTime, long lastTick, int delayTicks) {
        return lastTick == Long.MIN_VALUE || gameTime - lastTick >= delayTicks;
    }

    private static boolean hasContainerOpen(ServerPlayer player) {
        return player.containerMenu != player.inventoryMenu;
    }

    private static int selectedSlot(ServerPlayer player, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : -1;
    }

    private static PlayerMemory memory(ServerPlayer player) {
        return MEMORIES.computeIfAbsent(player.getUUID(), ignored -> new PlayerMemory());
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(SmartBackpacks.MOD_ID, path));
    }

    private record BuilderBackpack(ItemStack backpack, BackpackTier tier, BuilderUpgradeData data, Consumer<ItemStack> saver) {
    }

    private static final class PlayerMemory {
        private final HandMemory mainHand = new HandMemory();
        private final HandMemory offhand = new HandMemory();

        private HandMemory forHand(InteractionHand hand) {
            return hand == InteractionHand.OFF_HAND ? this.offhand : this.mainHand;
        }
    }

    private static final class HandMemory {
        private ItemStack candidate = ItemStack.EMPTY;
        private int selectedSlot = -1;
        private long lastSeenTick = Long.MIN_VALUE;
        private long lastUseTick = Long.MIN_VALUE;
        private long lastAttemptTick = Long.MIN_VALUE;
        private long lastFailureFeedbackTick = Long.MIN_VALUE;

        private boolean hasCandidate() {
            return !this.candidate.isEmpty();
        }

        private boolean hasRecentRefillTrigger(long gameTime) {
            return isRecent(gameTime, this.lastUseTick, REFILL_REQUEST_LIFETIME_TICKS)
                    || isRecent(gameTime, this.lastSeenTick, REFILL_REQUEST_LIFETIME_TICKS);
        }

        private void clear() {
            this.candidate = ItemStack.EMPTY;
            this.selectedSlot = -1;
            this.lastSeenTick = Long.MIN_VALUE;
            this.lastUseTick = Long.MIN_VALUE;
            this.lastAttemptTick = Long.MIN_VALUE;
            this.lastFailureFeedbackTick = Long.MIN_VALUE;
        }
    }
}
