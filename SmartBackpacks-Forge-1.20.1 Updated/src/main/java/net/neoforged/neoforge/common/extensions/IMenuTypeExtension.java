package net.neoforged.neoforge.common.extensions;

import io.netty.buffer.Unpooled;
import com.teamsmartstreamlabs.smartbackpacks.compat.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;

public interface IMenuTypeExtension {
    static <T extends AbstractContainerMenu> MenuType<T> create(MenuFactory<T> factory) {
        return IForgeMenuType.create((windowId, inventory, data) -> factory.create(
                windowId,
                inventory,
                data != null ? new RegistryFriendlyByteBuf(data.copy()) : new RegistryFriendlyByteBuf(Unpooled.buffer())
        ));
    }

    @FunctionalInterface
    interface MenuFactory<T extends AbstractContainerMenu> {
        T create(int syncId, Inventory inventory, RegistryFriendlyByteBuf buf);
    }
}
