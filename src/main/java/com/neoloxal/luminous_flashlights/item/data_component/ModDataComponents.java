package com.neoloxal.luminous_flashlights.item.data_component;

import com.mojang.serialization.Codec;
import com.neoloxal.luminous_flashlights.LuminousFlashlights;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, LuminousFlashlights.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Color>> COLOR = DATA_COMPONENTS.registerComponentType(
            "color",
            builder -> builder.persistent(Color.CODEC).networkSynchronized(Color.STREAM_CODEC)
    );

    public static Double maxFocus = 25.0;

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> FOCUS = DATA_COMPONENTS.registerComponentType(
            "focus",
            builder -> builder.persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE)
    );

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }

    public static class Helper {
        public static double scrollStack(ItemStack stack, double scrollDelta) {
            double oldValue = stack.getOrDefault(FOCUS.get(), 0.0);
            double newValue = oldValue + scrollDelta;
            double fixedValue = Math.max(-maxFocus, Math.min(maxFocus, newValue));
            stack.set(
                    FOCUS.get(),
                    fixedValue
            );
            return oldValue - fixedValue;
        }
    }
}
