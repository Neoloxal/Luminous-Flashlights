package com.neoloxal.luminous_flashlights.item.data_component;

import com.neoloxal.luminous_flashlights.LuminousFlashlights;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, LuminousFlashlights.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Color>> COLOR = DATA_COMPONENTS.registerComponentType(
            "color",
            builder -> builder.persistent(Color.CODEC).networkSynchronized(Color.STREAM_CODEC)
    );

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}
