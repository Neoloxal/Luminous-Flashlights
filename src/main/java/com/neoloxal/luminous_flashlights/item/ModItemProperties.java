package com.neoloxal.luminous_flashlights.item;

import com.mojang.logging.LogUtils;
import com.neoloxal.luminous_flashlights.LuminousFlashlights;
import com.neoloxal.luminous_flashlights.item.data_component.Color;
import com.neoloxal.luminous_flashlights.item.data_component.ModDataComponents;
import com.neoloxal.paint_palette_lib.builtin.LibDataComponents;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;

public class ModItemProperties {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register() {
        Item flashlight = LuminousFlashlights.MOD_ITEMS.getItem("flashlight").get();

        ItemProperties.register(
                flashlight,
                ResourceLocation.fromNamespaceAndPath(LuminousFlashlights.MODID, "enabled"),
                (stack, level, entity, seed) ->
                        stack.getOrDefault(LibDataComponents.TOGGLE.get(), false) ? 1f : 0f
        );

        ItemProperties.register(
                flashlight,
                ResourceLocation.fromNamespaceAndPath(LuminousFlashlights.MODID, "color"),
                ((stack, level, entity, i) -> stack.getOrDefault(
                        ModDataComponents.COLOR.get(),
                        Color.WHITE
                    ).getIdentifier()
                )
        );
    }
}
