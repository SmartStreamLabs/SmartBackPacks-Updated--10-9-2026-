package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacksConfig;
import com.teamsmartstreamlabs.smartbackpacks.backpack.BackpackStackData;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackItem;
import com.teamsmartstreamlabs.smartbackpacks.item.BackpackUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.item.TrashCanUpgradeItem;
import com.teamsmartstreamlabs.smartbackpacks.registry.ModDataComponents;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;

public final class TrashCanUpgradeHandler {
    public static final TagKey<Item> TRASH_PROTECTED = itemTag("trash_protected");
    public static final TagKey<Item> TRASH_CONFIRMATION_REQUIRED = itemTag("trash_confirmation_required");
    public static final TagKey<Item> TRASH_NEVER_ALLOWED = itemTag("trash_never_allowed");

    private TrashCanUpgradeHandler() {
    }

    public static int deletionDelayTicks() {
        return SmartBackpacksConfig.trashCanDeletionDelaySeconds() * 20;
    }

    public static boolean canEnterTrash(ItemStack stack, ItemStack openBackpack) {
        return !stack.isEmpty() && !isNeverAllowed(stack, openBackpack);
    }

    public static boolean isNeverAllowed(ItemStack stack, ItemStack openBackpack) {
        if (stack.isEmpty()) {
            return true;
        }
        if (stack.is(TRASH_NEVER_ALLOWED)) {
            return true;
        }
        if (stack.getItem() instanceof BackpackUpgradeItem) {
            return true;
        }
        if (stack.getItem() instanceof TrashCanUpgradeItem
                && stack.getOrDefault(ModDataComponents.TRASH_CAN_UPGRADE_DATA.get(), TrashCanUpgradeData.DEFAULT).hasLastDeletedItem()) {
            return true;
        }
        if (!openBackpack.isEmpty() && ItemStack.isSameItemSameComponents(stack, openBackpack)) {
            return true;
        }
        return false;
    }

    public static boolean requiresConfirmation(ItemStack stack) {
        TrashProtectionLevel level = SmartBackpacksConfig.trashCanProtectionLevel();
        if (stack.isEmpty() || level == TrashProtectionLevel.OFF) {
            return false;
        }
        if (stack.is(TRASH_CONFIRMATION_REQUIRED) || stack.is(TRASH_PROTECTED)) {
            return true;
        }
        if (level == TrashProtectionLevel.ALWAYS_CONFIRM) {
            return true;
        }
        if (level == TrashProtectionLevel.STRICT && isNonBasic(stack)) {
            return true;
        }
        return level == TrashProtectionLevel.VALUABLE_ITEMS && isValuable(stack);
    }

    private static boolean isValuable(ItemStack stack) {
        Item item = stack.getItem();
        if (stack.isEnchanted() || stack.has(DataComponents.CUSTOM_NAME)) {
            return true;
        }
        if (SmartBackpacksConfig.trashCanConfirmDamagedItems() && stack.isDamageableItem() && stack.isDamaged()) {
            return true;
        }
        if (SmartBackpacksConfig.trashCanConfirmContainerItems() && hasContainerContents(stack)) {
            return true;
        }
        if (SmartBackpacksConfig.trashCanConfirmModdedItems() && isModded(stack)) {
            return true;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        if (item instanceof BackpackItem || item instanceof BackpackUpgradeItem || isShulkerBox(itemId)) {
            return true;
        }
        if (item instanceof ArmorItem || item instanceof SwordItem || item instanceof DiggerItem || item instanceof BowItem
                || item instanceof CrossbowItem || item instanceof TridentItem) {
            return true;
        }
        if (stack.is(Items.TOTEM_OF_UNDYING) || stack.is(Items.ELYTRA) || stack.is(Items.WRITTEN_BOOK)
                || stack.is(Items.FILLED_MAP) || stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION)
                || stack.is(Items.LINGERING_POTION)) {
            return true;
        }
        return itemId != null && itemId.getPath().contains("netherite");
    }

    private static boolean isNonBasic(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA) || isValuable(stack);
    }

    private static boolean isModded(ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return itemId != null && !itemId.getNamespace().equals("minecraft");
    }

    private static boolean isShulkerBox(ResourceLocation itemId) {
        return itemId != null && itemId.getPath().endsWith("shulker_box");
    }

    private static boolean hasContainerContents(ItemStack stack) {
        if (stack.getItem() instanceof BackpackItem && BackpackStackData.loadStorage(stack, ((BackpackItem) stack.getItem()).getTier()).stream().anyMatch(item -> !item.isEmpty())) {
            return true;
        }
        return stack.has(DataComponents.CONTAINER)
                && ItemContainerContentsHelper.nonEmptyStream(stack.getOrDefault(DataComponents.CONTAINER, net.minecraft.world.item.component.ItemContainerContents.EMPTY)).findAny().isPresent();
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(SmartBackpacks.MOD_ID, path));
    }
}
