package com.teamsmartstreamlabs.smartbackpacks.registry;

import com.teamsmartstreamlabs.smartbackpacks.SmartBackpacks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, SmartBackpacks.MOD_ID);
    public static final DeferredHolder<SoundEvent, SoundEvent> CAPACITY_WARNING = registerSound("capacity_warning");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAPACITY_FULL = registerSound("capacity_full");

    private ModSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SmartBackpacks.MOD_ID, name)));
    }

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}

