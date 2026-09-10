package com.teamsmartstreamlabs.smartbackpacks.compat;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Predicate;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public final class CuriosCompat {
    public record BackSlotMatch(int slot, ItemStack stack) {
    }

    private static final String CURIOS_API_CLASS = "top.theillusivec4.curios.api.CuriosApi";
    private static final boolean AVAILABLE = ModList.get().isLoaded("curios");
    private static final boolean TRINKETS_AVAILABLE = FabricLoader.getInstance().isModLoaded("trinkets_updated");
    private static final Method GET_CURIOS_INVENTORY = resolveStaticMethod(CURIOS_API_CLASS, "getCuriosInventory", LivingEntity.class);

    private CuriosCompat() {
    }

    public static boolean isAvailable() {
        return TRINKETS_AVAILABLE || AVAILABLE && GET_CURIOS_INVENTORY != null;
    }

    public static int getBackSlotCount(Player player) {
        if (TRINKETS_AVAILABLE) {
            return TrinketsCompat.getBackSlotCount(player);
        }
        Object stacks = getBackStacks(player);
        if (stacks == null) {
            return 0;
        }

        Object result = invoke(stacks, "getSlots");
        return result instanceof Number number ? number.intValue() : 0;
    }

    public static ItemStack getBackStack(Player player, int slot) {
        if (TRINKETS_AVAILABLE) {
            return TrinketsCompat.getBackStack(player, slot);
        }
        Object stacks = getBackStacks(player);
        if (stacks == null) {
            return ItemStack.EMPTY;
        }

        Object result = invoke(stacks, "getStackInSlot", int.class, slot);
        return result instanceof ItemStack stack ? stack : ItemStack.EMPTY;
    }

    public static void setBackStack(Player player, int slot, ItemStack stack) {
        if (TRINKETS_AVAILABLE) {
            TrinketsCompat.setBackStack(player, slot, stack);
            return;
        }
        Object stacks = getBackStacks(player);
        if (stacks != null) {
            invoke(stacks, "setStackInSlot", int.class, ItemStack.class, slot, stack);
        }
    }

    public static boolean insertIntoFirstEmptyBackSlot(Player player, ItemStack stack) {
        if (TRINKETS_AVAILABLE) {
            return TrinketsCompat.insertIntoFirstEmptyBackSlot(player, stack);
        }
        Object stacks = getBackStacks(player);
        if (stacks == null || stack.isEmpty()) {
            return false;
        }

        int slotCount = getBackSlotCount(player);
        for (int slot = 0; slot < slotCount; slot++) {
            if (!getBackStack(player, slot).isEmpty()) {
                continue;
            }

            Object remainder = invoke(stacks, "insertItem", new Class<?>[]{int.class, ItemStack.class, boolean.class}, new Object[]{slot, stack, false});
            if (remainder instanceof ItemStack remainderStack && remainderStack.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public static Optional<BackSlotMatch> findFirstMatchingBackStack(Player player, Predicate<ItemStack> predicate) {
        int slotCount = getBackSlotCount(player);
        for (int slot = 0; slot < slotCount; slot++) {
            ItemStack stack = getBackStack(player, slot);
            if (predicate.test(stack)) {
                return Optional.of(new BackSlotMatch(slot, stack));
            }
        }

        return Optional.empty();
    }

    private static Object getBackStacks(Player player) {
        Optional<?> stacksHandler = getBackStacksHandler(player);
        return stacksHandler.map(handler -> invoke(handler, "getStacks")).orElse(null);
    }

    @SuppressWarnings("unchecked")
    private static Optional<?> getBackStacksHandler(Player player) {
        if (!isAvailable()) {
            return Optional.empty();
        }

        try {
            Object curiosInventory = unwrapOptionalLike(GET_CURIOS_INVENTORY.invoke(null, player));
            if (curiosInventory == null) {
                return Optional.empty();
            }

            Object result = invoke(curiosInventory, "getStacksHandler", String.class, "back");
            return result instanceof Optional<?> optional ? optional : Optional.empty();
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private static Object unwrapOptionalLike(Object value) {
        if (value instanceof Optional<?> optional) {
            return optional.orElse(null);
        }

        Object resolved = invoke(value, "resolve");
        return resolved instanceof Optional<?> optional ? optional.orElse(null) : value;
    }

    private static Method resolveStaticMethod(String className, String name, Class<?>... parameterTypes) {
        if (!AVAILABLE) {
            return null;
        }

        try {
            Class<?> type = Class.forName(className);
            Method method = type.getMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Object invoke(Object target, String name) {
        return invoke(target, name, new Class<?>[0], new Object[0]);
    }

    private static Object invoke(Object target, String name, Class<?> parameterType, Object value) {
        return invoke(target, name, new Class<?>[]{parameterType}, new Object[]{value});
    }

    private static Object invoke(Object target, String name, Class<?> first, Class<?> second, Object firstValue, Object secondValue) {
        return invoke(target, name, new Class<?>[]{first, second}, new Object[]{firstValue, secondValue});
    }

    private static Object invoke(Object target, String name, Class<?> first, Class<?> second, Class<?> third, Object firstValue, Object secondValue, Object thirdValue) {
        return invoke(target, name, new Class<?>[]{first, second, third}, new Object[]{firstValue, secondValue, thirdValue});
    }

    private static Object invoke(Object target, String name, Class<?>[] parameterTypes, Object[] values) {
        if (target == null) {
            return null;
        }

        try {
            Method method = target.getClass().getMethod(name, parameterTypes);
            method.setAccessible(true);
            return method.invoke(target, values);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
