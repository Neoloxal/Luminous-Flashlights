package com.neoloxal.luminous_flashlights.sound;

import com.neoloxal.luminous_flashlights.LuminousFlashlights;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, LuminousFlashlights.MODID);

    public static final Supplier<SoundEvent> FLASHLIGHT_ON = registerSoundEvent("flashlight_on");
    public static final Supplier<SoundEvent> FLASHLIGHT_OFF = registerSoundEvent("flashlight_off");
    public static final Supplier<SoundEvent> SWAP_LENS = registerSoundEvent("swap_lens");
    public static final Supplier<SoundEvent> FOCUS_CHANGE = registerSoundEvent("focus_change");

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(LuminousFlashlights.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
