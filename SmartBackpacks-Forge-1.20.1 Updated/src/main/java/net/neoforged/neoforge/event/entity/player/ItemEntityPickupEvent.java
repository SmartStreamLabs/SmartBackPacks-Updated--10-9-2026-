package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ItemEntityPickupEvent {
    private final Player player;
    private final ItemEntity itemEntity;

    public ItemEntityPickupEvent(Player player, ItemEntity itemEntity) {
        this.player = player;
        this.itemEntity = itemEntity;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemEntity getItemEntity() {
        return itemEntity;
    }

    public static final class Post extends ItemEntityPickupEvent {
        private final ItemStack originalStack;
        private final ItemStack currentStack;

        public Post(Player player, ItemEntity itemEntity, ItemStack originalStack, ItemStack currentStack) {
            super(player, itemEntity);
            this.originalStack = originalStack;
            this.currentStack = currentStack;
        }

        public ItemStack getOriginalStack() {
            return originalStack;
        }

        public ItemStack getCurrentStack() {
            return currentStack;
        }
    }
}
