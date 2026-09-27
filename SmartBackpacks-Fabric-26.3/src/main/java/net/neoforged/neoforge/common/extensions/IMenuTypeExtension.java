package net.neoforged.neoforge.common.extensions;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public interface IMenuTypeExtension {
   static <T extends AbstractContainerMenu> MenuType<T> create(IMenuTypeExtension.MenuFactory<T> factory) {
      return new ExtendedMenuType<>(
         (int syncId, Inventory inventory, byte[] data) -> factory.create(
            syncId, inventory, new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data), inventory.player.registryAccess())
         ),
         ByteBufCodecs.BYTE_ARRAY
      );
   }

   @FunctionalInterface
   public interface MenuFactory<T extends AbstractContainerMenu> {
      T create(int var1, Inventory var2, RegistryFriendlyByteBuf var3);
   }
}
