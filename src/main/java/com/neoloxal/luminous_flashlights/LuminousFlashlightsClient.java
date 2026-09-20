package com.neoloxal.luminous_flashlights;

import com.neoloxal.luminous_flashlights.hud.FocusOverlay;
import com.neoloxal.luminous_flashlights.item.ModItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = LuminousFlashlights.MODID, value = Dist.CLIENT)
public class LuminousFlashlightsClient {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ModItemProperties::register);
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerBelow(
                VanillaGuiLayers.CHAT,
                ResourceLocation.fromNamespaceAndPath(LuminousFlashlights.MODID, "flashlight_focus"),
                new FocusOverlay()
        );
    }
}
