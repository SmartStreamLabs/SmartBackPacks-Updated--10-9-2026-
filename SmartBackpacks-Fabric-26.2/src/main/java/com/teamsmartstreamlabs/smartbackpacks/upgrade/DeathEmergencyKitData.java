package com.teamsmartstreamlabs.smartbackpacks.upgrade;

import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public record DeathEmergencyKitData(ItemContainerContents templates, List<Integer> destinations) {
    public static final int SLOT_COUNT = 6;
    public static final DeathEmergencyKitData DEFAULT = new DeathEmergencyKitData(ItemContainerContents.EMPTY,
            List.of(-1, -1, -1, -1, -1, -1));
    public static final Codec<DeathEmergencyKitData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemContainerContents.CODEC.optionalFieldOf("templates", ItemContainerContents.EMPTY).forGetter(DeathEmergencyKitData::templates),
            Codec.INT.listOf().optionalFieldOf("destinations", DEFAULT.destinations()).forGetter(DeathEmergencyKitData::destinations)
    ).apply(instance, DeathEmergencyKitData::new));

    public NonNullList<ItemStack> loadTemplates() {
        NonNullList<ItemStack> result = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        this.templates.copyInto(result);
        return result;
    }

    public int destination(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT || slot >= this.destinations.size()) {
            return -1;
        }
        return Math.clamp(this.destinations.get(slot), -1, 8);
    }
}
